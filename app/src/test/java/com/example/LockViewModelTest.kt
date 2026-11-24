package com.example

import com.example.data.LockCredentialsState
import com.example.data.LockRepository
import com.example.data.SavePhotoResult
import com.example.data.VaultItem
import com.example.ui.FeedbackBanner
import com.example.ui.LockDestination
import com.example.ui.LockViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private class FakeLockRepository : LockRepository {
    private var pin: String? = null
    private var backupCode: String? = null
    private val state = MutableStateFlow(LockCredentialsState())

    override val stateFlow: Flow<LockCredentialsState> = state

    override suspend fun isSetupDone(): Boolean = pin != null && backupCode != null

    override suspend fun saveSetup(pin: String, backupCode: String) {
        this.pin = pin
        this.backupCode = backupCode.trim()
        state.value = state.value.copy(
            isSetupComplete = true,
            pinUpdatedAtMillis = System.currentTimeMillis()
        )
    }

    override suspend fun verifyPin(pin: String): Boolean = this.pin == pin

    override suspend fun verifyBackupCode(backupCode: String): Boolean =
        this.backupCode == backupCode.trim()

    override suspend fun resetPinWithBackup(backupCode: String, newPin: String): Boolean {
        if (!verifyBackupCode(backupCode)) return false
        this.pin = newPin
        state.value = state.value.copy(pinUpdatedAtMillis = System.currentTimeMillis())
        return true
    }

    override suspend fun recordUnlockTimestamp() {
        state.value = state.value.copy(lastUnlockedAtMillis = System.currentTimeMillis())
    }

    override suspend fun addVaultItems(uriStrings: List<String>, forceImage: Boolean): Int {
        val added = uriStrings.mapIndexed { idx, uriStr ->
            VaultItem(
                id = "item_${state.value.vaultItems.size + idx + 1}",
                name = uriStr.substringAfterLast('/'),
                sizeBytes = 2048L,
                mimeType = if (forceImage) "image/jpeg" else "application/pdf",
                localPath = "/tmp/fake_${idx}",
                isImage = forceImage,
                addedAtMillis = System.currentTimeMillis()
            )
        }
        state.value = state.value.copy(vaultItems = state.value.vaultItems + added)
        return added.size
    }

    override suspend fun savePhotoToGallery(itemId: String): SavePhotoResult {
        val item = state.value.vaultItems.firstOrNull { it.id == itemId } ?: return SavePhotoResult.Failed
        if (!item.isImage) return SavePhotoResult.Failed
        if (item.savedGalleryUri != null) return SavePhotoResult.AlreadySaved
        val galleryUri = "content://media/external/images/media/${item.id}"
        state.value = state.value.copy(
            vaultItems = state.value.vaultItems.map {
                if (it.id == itemId) it.copy(savedGalleryUri = galleryUri) else it
            }
        )
        return SavePhotoResult.Saved(galleryUri)
    }

    override suspend fun removeVaultItem(itemId: String) {
        state.value = state.value.copy(
            vaultItems = state.value.vaultItems.filterNot { it.id == itemId }
        )
    }

    override suspend fun clearAll() {
        pin = null
        backupCode = null
        state.value = LockCredentialsState()
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class LockViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `setup unlock recovery and reset flow works end to end`() = runTest(testDispatcher) {
        val repository = FakeLockRepository()
        val viewModel = LockViewModel(repository)
        advanceUntilIdle()
        assertEquals(LockDestination.Setup, viewModel.uiState.value.destination)

        // Reject invalid PIN/backup length
        viewModel.onSetupPinChange("12")
        viewModel.onSetupBackupChange("abc")
        viewModel.saveSetup()
        assertEquals(
            FeedbackBanner.Error(R.string.err_invalid_setup),
            viewModel.uiState.value.feedback
        )

        // Valid setup saves and transitions to Unlock
        viewModel.onSetupPinChange("4829")
        viewModel.onSetupBackupChange("dhaka2026")
        viewModel.saveSetup()
        advanceUntilIdle()
        assertEquals(LockDestination.Unlock, viewModel.uiState.value.destination)
        assertEquals(
            FeedbackBanner.Info(R.string.msg_pin_set),
            viewModel.uiState.value.feedback
        )

        // Wrong PIN fails and increments attempt trigger
        "1111".forEach { viewModel.onKeypadDigit(it) }
        viewModel.submitUnlock()
        advanceUntilIdle()
        assertEquals(LockDestination.Unlock, viewModel.uiState.value.destination)
        assertEquals(
            FeedbackBanner.Error(R.string.err_wrong_pin),
            viewModel.uiState.value.feedback
        )
        assertEquals(1, viewModel.uiState.value.wrongPinAttemptCount)

        // Correct PIN unlocks
        "4829".forEach { viewModel.onKeypadDigit(it) }
        viewModel.submitUnlock()
        advanceUntilIdle()
        assertEquals(LockDestination.Unlocked, viewModel.uiState.value.destination)

        // Lock again and open recovery
        viewModel.lockAgain()
        assertEquals(LockDestination.Unlock, viewModel.uiState.value.destination)

        viewModel.openRecovery()
        assertEquals(LockDestination.Recovery, viewModel.uiState.value.destination)

        // Backup mismatch rejected
        viewModel.onRecoveryBackupChange("wrong")
        viewModel.onRecoveryNewPinChange("9999")
        viewModel.submitRecovery()
        advanceUntilIdle()
        assertEquals(
            FeedbackBanner.Error(R.string.err_backup_mismatch),
            viewModel.uiState.value.feedback
        )

        // Valid backup + invalid new PIN rejected
        viewModel.onRecoveryBackupChange("dhaka2026")
        viewModel.onRecoveryNewPinChange("99")
        viewModel.submitRecovery()
        advanceUntilIdle()
        assertEquals(
            FeedbackBanner.Error(R.string.err_new_pin_length),
            viewModel.uiState.value.feedback
        )

        // Valid backup + valid 4-digit PIN resets PIN
        viewModel.onRecoveryNewPinChange("9012")
        viewModel.submitRecovery()
        advanceUntilIdle()
        assertEquals(LockDestination.Unlock, viewModel.uiState.value.destination)
        assertEquals(
            FeedbackBanner.Info(R.string.msg_new_pin_set),
            viewModel.uiState.value.feedback
        )

        // New PIN unlocks
        "9012".forEach { viewModel.onKeypadDigit(it) }
        viewModel.submitUnlock()
        advanceUntilIdle()
        assertEquals(LockDestination.Unlocked, viewModel.uiState.value.destination)
        assertTrue((viewModel.uiState.value.lastUnlockedAtMillis ?: 0L) > 0L)

        // Reset vault clears credentials and returns to Setup
        viewModel.requestResetVault()
        viewModel.confirmResetVault()
        advanceUntilIdle()
        assertEquals(LockDestination.Setup, viewModel.uiState.value.destination)
    }

    @Test
    fun `vault photo and file items can be added previewed and removed`() = runTest(testDispatcher) {
        val repository = FakeLockRepository()
        repository.saveSetup("1234", "mybackup")
        val viewModel = LockViewModel(repository)
        advanceUntilIdle()

        "1234".forEach { viewModel.onKeypadDigit(it) }
        viewModel.submitUnlock()
        advanceUntilIdle()
        assertEquals(LockDestination.Unlocked, viewModel.uiState.value.destination)

        // Add photos & files
        viewModel.onAddVaultUris(listOf("content://media/photo1.jpg"), forceImage = true)
        advanceUntilIdle()
        viewModel.onAddVaultUris(listOf("content://docs/notes.pdf"), forceImage = false)
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.vaultItems.size)
        val photoItem = viewModel.uiState.value.vaultItems.first()
        assertTrue(photoItem.isImage)
        assertEquals(2L, photoItem.sizeKb)

        // Open and dismiss preview
        viewModel.openImagePreview(photoItem)
        assertEquals(photoItem, viewModel.uiState.value.previewImageItem)
        viewModel.dismissImagePreview()
        assertNull(viewModel.uiState.value.previewImageItem)

        // Remove photo item
        viewModel.onRemoveVaultItem(photoItem.id)
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.vaultItems.size)
        assertEquals("notes.pdf", viewModel.uiState.value.vaultItems.first().name)
    }

    @Test
    fun `save photo to phone succeeds prevents duplicates keeps original and handles failures`() =
        runTest(testDispatcher) {
            val repository = FakeLockRepository()
            repository.saveSetup("1234", "mybackup")
            val viewModel = LockViewModel(repository)
            advanceUntilIdle()

            "1234".forEach { viewModel.onKeypadDigit(it) }
            viewModel.submitUnlock()
            advanceUntilIdle()

            viewModel.onAddVaultUris(listOf("content://media/vacation.jpg"), forceImage = true)
            viewModel.onAddVaultUris(listOf("content://docs/report.pdf"), forceImage = false)
            advanceUntilIdle()

            val photo = viewModel.uiState.value.vaultItems.first { it.isImage }
            val file = viewModel.uiState.value.vaultItems.first { !it.isImage }

            // First save succeeds and keeps original photo in vault
            viewModel.savePhotoToPhone(photo.id)
            advanceUntilIdle()
            assertEquals(
                FeedbackBanner.Info(R.string.msg_photo_saved),
                viewModel.uiState.value.feedback
            )
            assertEquals(2, viewModel.uiState.value.vaultItems.size)
            assertTrue(
                viewModel.uiState.value.vaultItems.first { it.id == photo.id }.savedGalleryUri != null
            )

            // Second save prevents duplicate
            viewModel.savePhotoToPhone(photo.id)
            advanceUntilIdle()
            assertEquals(
                FeedbackBanner.Info(R.string.msg_photo_already_saved),
                viewModel.uiState.value.feedback
            )

            // Non-image / missing item shows understandable error
            viewModel.savePhotoToPhone(file.id)
            advanceUntilIdle()
            assertEquals(
                FeedbackBanner.Error(R.string.err_photo_save_failed),
                viewModel.uiState.value.feedback
            )
        }
}
