package com.example.ui

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.DataStoreLockRepository
import com.example.data.LockRepository
import com.example.data.SavePhotoResult
import com.example.data.VaultItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class LockDestination {
    Loading,
    Setup,
    Unlock,
    Recovery,
    Unlocked
}

sealed interface FeedbackBanner {
    data class Info(val messageResId: Int, val countArg: Int? = null) : FeedbackBanner
    data class Error(val messageResId: Int) : FeedbackBanner
}

data class SnackbarNotice(
    val id: Long = System.nanoTime(),
    val messageResId: Int
)

data class LockUiState(
    val destination: LockDestination = LockDestination.Loading,
    val setupPinInput: String = "",
    val setupBackupInput: String = "",
    val unlockPinInput: String = "",
    val recoveryBackupInput: String = "",
    val recoveryNewPinInput: String = "",
    val feedback: FeedbackBanner? = null,
    val snackbarNotice: SnackbarNotice? = null,
    val wrongPinAttemptCount: Int = 0,
    val lastUnlockedAtMillis: Long? = null,
    val pinUpdatedAtMillis: Long? = null,
    val vaultItems: List<VaultItem> = emptyList(),
    val isImportingItems: Boolean = false,
    val savingItemId: String? = null,
    val previewImageItem: VaultItem? = null,
    val showResetConfirmDialog: Boolean = false
)

class LockViewModel(
    private val repository: LockRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LockUiState())
    val uiState: StateFlow<LockUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val setupDone = repository.isSetupDone()
            _uiState.update { state ->
                state.copy(
                    destination = if (setupDone) LockDestination.Unlock else LockDestination.Setup
                )
            }
            repository.stateFlow.collect { creds ->
                _uiState.update { state ->
                    state.copy(
                        lastUnlockedAtMillis = creds.lastUnlockedAtMillis,
                        pinUpdatedAtMillis = creds.pinUpdatedAtMillis,
                        vaultItems = creds.vaultItems
                    )
                }
            }
        }
    }

    fun onSetupPinChange(value: String) {
        val filtered = value.filter { it.isDigit() }.take(4)
        _uiState.update { it.copy(setupPinInput = filtered, feedback = null) }
    }

    fun onSetupBackupChange(value: String) {
        _uiState.update { it.copy(setupBackupInput = value, feedback = null) }
    }

    fun saveSetup() {
        val current = _uiState.value
        val pin = current.setupPinInput
        val backup = current.setupBackupInput.trim()

        if (!PIN_REGEX.matches(pin) || backup.length < MIN_BACKUP_LENGTH) {
            _uiState.update {
                it.copy(feedback = FeedbackBanner.Error(R.string.err_invalid_setup))
            }
            return
        }

        viewModelScope.launch {
            repository.saveSetup(pin = pin, backupCode = backup)
            _uiState.update {
                it.copy(
                    destination = LockDestination.Unlock,
                    setupPinInput = "",
                    setupBackupInput = "",
                    unlockPinInput = "",
                    feedback = FeedbackBanner.Info(R.string.msg_pin_set)
                )
            }
        }
    }

    fun onKeypadDigit(digit: Char) {
        if (!digit.isDigit()) return
        val current = _uiState.value
        if (current.destination != LockDestination.Unlock) return
        if (current.unlockPinInput.length >= 4) return

        val updatedPin = current.unlockPinInput + digit
        _uiState.update {
            it.copy(unlockPinInput = updatedPin, feedback = null)
        }
    }

    fun onKeypadBackspace() {
        _uiState.update { state ->
            if (state.unlockPinInput.isEmpty()) state
            else state.copy(unlockPinInput = state.unlockPinInput.dropLast(1), feedback = null)
        }
    }

    fun onKeypadClear() {
        _uiState.update { it.copy(unlockPinInput = "", feedback = null) }
    }

    fun onUnlockPinChange(value: String) {
        val filtered = value.filter { it.isDigit() }.take(4)
        _uiState.update { it.copy(unlockPinInput = filtered, feedback = null) }
    }

    fun submitUnlock() {
        val pin = _uiState.value.unlockPinInput
        if (!PIN_REGEX.matches(pin)) {
            _uiState.update {
                it.copy(
                    feedback = FeedbackBanner.Error(R.string.err_incomplete_pin),
                    wrongPinAttemptCount = it.wrongPinAttemptCount + 1
                )
            }
            return
        }

        viewModelScope.launch {
            val verified = repository.verifyPin(pin)
            if (verified) {
                repository.recordUnlockTimestamp()
                _uiState.update {
                    it.copy(
                        destination = LockDestination.Unlocked,
                        unlockPinInput = "",
                        feedback = null
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        unlockPinInput = "",
                        feedback = FeedbackBanner.Error(R.string.err_wrong_pin),
                        wrongPinAttemptCount = it.wrongPinAttemptCount + 1
                    )
                }
            }
        }
    }

    fun openRecovery() {
        _uiState.update {
            it.copy(
                destination = LockDestination.Recovery,
                unlockPinInput = "",
                recoveryBackupInput = "",
                recoveryNewPinInput = "",
                feedback = null,
                previewImageItem = null
            )
        }
    }

    fun returnToUnlock() {
        _uiState.update {
            it.copy(
                destination = LockDestination.Unlock,
                unlockPinInput = "",
                recoveryBackupInput = "",
                recoveryNewPinInput = "",
                feedback = null
            )
        }
    }

    fun onRecoveryBackupChange(value: String) {
        _uiState.update { it.copy(recoveryBackupInput = value, feedback = null) }
    }

    fun onRecoveryNewPinChange(value: String) {
        val filtered = value.filter { it.isDigit() }.take(4)
        _uiState.update { it.copy(recoveryNewPinInput = filtered, feedback = null) }
    }

    fun submitRecovery() {
        val current = _uiState.value
        val backup = current.recoveryBackupInput.trim()
        val newPin = current.recoveryNewPinInput

        viewModelScope.launch {
            if (!repository.verifyBackupCode(backup)) {
                _uiState.update {
                    it.copy(feedback = FeedbackBanner.Error(R.string.err_backup_mismatch))
                }
                return@launch
            }

            if (!PIN_REGEX.matches(newPin)) {
                _uiState.update {
                    it.copy(feedback = FeedbackBanner.Error(R.string.err_new_pin_length))
                }
                return@launch
            }

            val resetSuccess = repository.resetPinWithBackup(backupCode = backup, newPin = newPin)
            if (resetSuccess) {
                _uiState.update {
                    it.copy(
                        destination = LockDestination.Unlock,
                        unlockPinInput = "",
                        recoveryBackupInput = "",
                        recoveryNewPinInput = "",
                        feedback = FeedbackBanner.Info(R.string.msg_new_pin_set)
                    )
                }
            } else {
                _uiState.update {
                    it.copy(feedback = FeedbackBanner.Error(R.string.err_backup_mismatch))
                }
            }
        }
    }

    fun onAddVaultUris(uriStrings: List<String>, forceImage: Boolean) {
        if (uriStrings.isEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isImportingItems = true, feedback = null) }
            val addedCount = repository.addVaultItems(uriStrings = uriStrings, forceImage = forceImage)
            _uiState.update {
                it.copy(
                    isImportingItems = false,
                    feedback = if (addedCount > 0) {
                        FeedbackBanner.Info(R.string.msg_items_added, countArg = addedCount)
                    } else {
                        FeedbackBanner.Error(R.string.err_import_failed)
                    }
                )
            }
        }
    }

    fun onRemoveVaultItem(itemId: String) {
        viewModelScope.launch {
            repository.removeVaultItem(itemId)
            _uiState.update { state ->
                val clearedPreview = if (state.previewImageItem?.id == itemId) null else state.previewImageItem
                state.copy(previewImageItem = clearedPreview, feedback = null)
            }
        }
    }

    fun savePhotoToPhone(itemId: String) {
        if (_uiState.value.savingItemId != null) return
        viewModelScope.launch {
            _uiState.update { it.copy(savingItemId = itemId, feedback = null) }
            val result = repository.savePhotoToGallery(itemId)
            _uiState.update { state ->
                val banner = when (result) {
                    is SavePhotoResult.Saved -> FeedbackBanner.Info(R.string.msg_photo_saved)
                    SavePhotoResult.AlreadySaved -> FeedbackBanner.Info(R.string.msg_photo_already_saved)
                    SavePhotoResult.Failed -> FeedbackBanner.Error(R.string.err_photo_save_failed)
                }
                val noticeResId = when (result) {
                    is SavePhotoResult.Saved -> R.string.msg_photo_saved
                    SavePhotoResult.AlreadySaved -> R.string.msg_photo_already_saved
                    SavePhotoResult.Failed -> R.string.err_photo_save_failed
                }
                state.copy(
                    savingItemId = null,
                    feedback = banner,
                    snackbarNotice = SnackbarNotice(messageResId = noticeResId)
                )
            }
        }
    }

    fun onLegacyStoragePermissionDenied() {
        _uiState.update {
            it.copy(
                feedback = FeedbackBanner.Error(R.string.err_save_permission_denied),
                snackbarNotice = SnackbarNotice(messageResId = R.string.err_save_permission_denied)
            )
        }
    }

    fun consumeSnackbarNotice() {
        _uiState.update { it.copy(snackbarNotice = null) }
    }

    fun openImagePreview(item: VaultItem) {
        if (item.isImage) {
            _uiState.update { it.copy(previewImageItem = item) }
        }
    }

    fun dismissImagePreview() {
        _uiState.update { it.copy(previewImageItem = null) }
    }

    fun lockAgain() {
        _uiState.update {
            it.copy(
                destination = LockDestination.Unlock,
                unlockPinInput = "",
                feedback = null,
                snackbarNotice = null,
                previewImageItem = null,
                showResetConfirmDialog = false
            )
        }
    }

    fun requestResetVault() {
        _uiState.update { it.copy(showResetConfirmDialog = true) }
    }

    fun dismissResetVault() {
        _uiState.update { it.copy(showResetConfirmDialog = false) }
    }

    fun confirmResetVault() {
        viewModelScope.launch {
            repository.clearAll()
            _uiState.update {
                LockUiState(destination = LockDestination.Setup)
            }
        }
    }

    companion object {
        private val PIN_REGEX = Regex("^\\d{4}$")
        private const val MIN_BACKUP_LENGTH = 5

        fun factory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return LockViewModel(DataStoreLockRepository(application.applicationContext)) as T
                }
            }
    }
}
