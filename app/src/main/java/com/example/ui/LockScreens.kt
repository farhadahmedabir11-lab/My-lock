package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.InsertDriveFile
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockReset
import androidx.compose.material.icons.rounded.Pin
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.R
import com.example.data.VaultItem
import com.example.ui.theme.CobaltPrimary
import com.example.ui.theme.CoralError
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.FcBlackCard
import com.example.ui.theme.FcGoldPrimary
import com.example.ui.theme.FcPurpleNeon
import com.example.ui.theme.SkyBlueLight
import com.example.ui.theme.SlateMuted
import com.example.ui.theme.SlateSecondary
import com.example.ui.theme.VaultBackground
import com.example.ui.theme.VaultOutline
import com.example.ui.theme.VaultOutlineSubtle
import com.example.ui.theme.VaultSurface
import com.example.ui.theme.VaultSurfaceElevated
import com.example.ui.theme.VaultSurfaceHighest
import java.io.File
import java.text.DateFormat
import java.util.Date

@Composable
fun VaultCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        color = VaultSurface.copy(alpha = 0.96f),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, VaultOutline.copy(alpha = 0.75f)),
        shadowElevation = 16.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            content()
        }
    }
}

@Composable
fun SetupScreen(
    uiState: LockUiState,
    onPinChange: (String) -> Unit,
    onBackupChange: (String) -> Unit,
    onSaveSetup: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    var showPin by rememberSaveable { mutableStateOf(false) }
    var showBackup by rememberSaveable { mutableStateOf(false) }

    VaultCard(modifier = modifier) {
        Text(
            text = stringResource(R.string.setup_title),
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = stringResource(R.string.setup_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        VaultTextField(
            value = uiState.setupPinInput,
            onValueChange = onPinChange,
            label = stringResource(R.string.hint_four_digit_pin),
            leadingIcon = Icons.Rounded.Pin,
            isSecret = !showPin,
            onToggleVisibility = { showPin = !showPin },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.NumberPassword,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.testTag("setup_pin_input")
        )

        Spacer(modifier = Modifier.height(14.dp))

        VaultTextField(
            value = uiState.setupBackupInput,
            onValueChange = onBackupChange,
            label = stringResource(R.string.hint_backup_code_create),
            supportingText = stringResource(R.string.helper_backup_code),
            leadingIcon = Icons.Rounded.Key,
            isSecret = !showBackup,
            onToggleVisibility = { showBackup = !showBackup },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    onSaveSetup()
                }
            ),
            modifier = Modifier.testTag("setup_backup_input")
        )

        Spacer(modifier = Modifier.height(14.dp))

        FeedbackBannerCard(feedback = uiState.feedback)

        Spacer(modifier = Modifier.height(18.dp))

        Button(
            onClick = {
                focusManager.clearFocus()
                onSaveSetup()
            },
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CobaltPrimary,
                contentColor = Color.White
            ),
            contentPadding = PaddingValues(vertical = 15.dp),
            modifier = Modifier
                .fillMaxWidth()
                .minimumInteractiveComponentSize()
                .testTag("setup_submit_button")
        ) {
            Icon(
                imageVector = Icons.Rounded.Lock,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.btn_set_pin),
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
fun UnlockScreen(
    uiState: LockUiState,
    onDigitClick: (Char) -> Unit,
    onBackspaceClick: () -> Unit,
    onClearClick: () -> Unit,
    onSubmitUnlock: () -> Unit,
    onOpenRecovery: () -> Unit,
    modifier: Modifier = Modifier
) {
    VaultCard(modifier = modifier) {
        Text(
            text = stringResource(R.string.unlock_title),
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stringResource(R.string.unlock_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(22.dp))

        PinDotsIndicator(
            enteredLength = uiState.unlockPinInput.length,
            wrongAttemptTrigger = uiState.wrongPinAttemptCount
        )

        Spacer(modifier = Modifier.height(18.dp))

        FeedbackBannerCard(
            feedback = uiState.feedback,
            modifier = Modifier.padding(bottom = 14.dp)
        )

        NumericKeypad(
            onDigitClick = onDigitClick,
            onBackspaceClick = onBackspaceClick,
            onClearClick = onClearClick
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onSubmitUnlock,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CobaltPrimary,
                contentColor = Color.White
            ),
            contentPadding = PaddingValues(vertical = 15.dp),
            modifier = Modifier
                .fillMaxWidth()
                .minimumInteractiveComponentSize()
                .testTag("unlock_submit_button")
        ) {
            Text(
                text = stringResource(R.string.btn_unlock),
                style = MaterialTheme.typography.labelLarge
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = onOpenRecovery,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = SlateSecondary,
                contentColor = Color.White
            ),
            contentPadding = PaddingValues(vertical = 14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .minimumInteractiveComponentSize()
                .testTag("go_to_recovery_button")
        ) {
            Icon(
                imageVector = Icons.Rounded.LockReset,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.btn_recover_with_backup),
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
fun RecoveryScreen(
    uiState: LockUiState,
    onBackupChange: (String) -> Unit,
    onNewPinChange: (String) -> Unit,
    onSubmitRecovery: () -> Unit,
    onBackToUnlock: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBackToUnlock)

    val focusManager = LocalFocusManager.current
    var showNewPin by rememberSaveable { mutableStateOf(false) }

    VaultCard(modifier = modifier) {
        Text(
            text = stringResource(R.string.recovery_title),
            style = MaterialTheme.typography.titleLarge,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = stringResource(R.string.recovery_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        VaultTextField(
            value = uiState.recoveryBackupInput,
            onValueChange = onBackupChange,
            label = stringResource(R.string.hint_backup_code),
            leadingIcon = Icons.Rounded.Key,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.testTag("recovery_backup_input")
        )

        Spacer(modifier = Modifier.height(14.dp))

        VaultTextField(
            value = uiState.recoveryNewPinInput,
            onValueChange = onNewPinChange,
            label = stringResource(R.string.hint_new_four_digit_pin),
            leadingIcon = Icons.Rounded.Pin,
            isSecret = !showNewPin,
            onToggleVisibility = { showNewPin = !showNewPin },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.NumberPassword,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    onSubmitRecovery()
                }
            ),
            modifier = Modifier.testTag("recovery_new_pin_input")
        )

        Spacer(modifier = Modifier.height(14.dp))

        FeedbackBannerCard(feedback = uiState.feedback)

        Spacer(modifier = Modifier.height(18.dp))

        Button(
            onClick = {
                focusManager.clearFocus()
                onSubmitRecovery()
            },
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CobaltPrimary,
                contentColor = Color.White
            ),
            contentPadding = PaddingValues(vertical = 15.dp),
            modifier = Modifier
                .fillMaxWidth()
                .minimumInteractiveComponentSize()
                .testTag("recovery_submit_button")
        ) {
            Text(
                text = stringResource(R.string.btn_set_new_pin),
                style = MaterialTheme.typography.labelLarge
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = onBackToUnlock,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = SlateSecondary,
                contentColor = Color.White
            ),
            contentPadding = PaddingValues(vertical = 14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .minimumInteractiveComponentSize()
                .testTag("recovery_back_button")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = stringResource(R.string.cd_back_to_unlock),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.btn_go_back),
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
fun UnlockedScreen(
    uiState: LockUiState,
    onSelectTab: (UnlockedTab) -> Unit,
    onAddPhotos: (List<Uri>) -> Unit,
    onAddFiles: (List<Uri>) -> Unit,
    onSavePhoto: (String) -> Unit,
    onLegacyPermissionDenied: () -> Unit,
    onRemoveItem: (String) -> Unit,
    onOpenImagePreview: (VaultItem) -> Unit,
    onDismissImagePreview: () -> Unit,
    onProfilePhotoPicked: (Uri) -> Unit,
    onRemoveProfilePhoto: () -> Unit,
    onProfileNameChange: (String) -> Unit,
    onProfileUsernameChange: (String) -> Unit,
    onProfileBioChange: (String) -> Unit,
    onSaveProfile: () -> Unit,
    onLockAgain: () -> Unit,
    onOpenRecovery: () -> Unit,
    onRequestReset: () -> Unit,
    onDismissReset: () -> Unit,
    onConfirmReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        if (uiState.unlockedTab == UnlockedTab.Profile) {
            onSelectTab(UnlockedTab.Vault)
        } else {
            onLockAgain()
        }
    }

    val context = LocalContext.current
    var pendingSaveItemId by remember { mutableStateOf<String?>(null) }

    val legacyWritePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        val itemId = pendingSaveItemId
        pendingSaveItemId = null
        if (granted && itemId != null) {
            onSavePhoto(itemId)
        } else if (!granted) {
            onLegacyPermissionDenied()
        }
    }

    val handleSavePhotoClick: (String) -> Unit = { itemId ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            onSavePhoto(itemId)
        } else {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
            if (hasPermission) {
                onSavePhoto(itemId)
            } else {
                pendingSaveItemId = itemId
                legacyWritePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
    ) { uris ->
        if (uris.isNotEmpty()) {
            onAddPhotos(uris)
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) {
            onAddFiles(uris)
        }
    }

    val formattedTime = remember(uiState.lastUnlockedAtMillis) {
        uiState.lastUnlockedAtMillis?.let { millis ->
            DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(millis))
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        UnlockedTabBar(
            selectedTab = uiState.unlockedTab,
            profilePhotoPath = uiState.profile.photoPath,
            onSelectTab = onSelectTab
        )

        Spacer(modifier = Modifier.height(14.dp))

        if (uiState.unlockedTab == UnlockedTab.Profile) {
            FcProfileSection(
                uiState = uiState,
                onProfilePhotoPicked = onProfilePhotoPicked,
                onRemoveProfilePhoto = onRemoveProfilePhoto,
                onNameChange = onProfileNameChange,
                onUsernameChange = onProfileUsernameChange,
                onBioChange = onProfileBioChange,
                onSaveProfile = onSaveProfile
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onLockAgain,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SlateSecondary,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(vertical = 15.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .minimumInteractiveComponentSize()
                    .testTag("profile_lock_again_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.btn_lock_again),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        } else {
            VaultCard {
        Surface(
            color = EmeraldContainer,
            shape = RoundedCornerShape(99.dp),
            border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.45f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.VerifiedUser,
                    contentDescription = null,
                    tint = EmeraldSuccess,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.unlocked_badge),
                    style = MaterialTheme.typography.labelMedium,
                    color = EmeraldSuccess
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.unlocked_title),
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.testTag("unlocked_title_text")
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stringResource(R.string.unlocked_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = CobaltPrimary,
                contentColor = Color.White
            ),
            contentPadding = PaddingValues(vertical = 14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .minimumInteractiveComponentSize()
                .testTag("add_photo_button")
        ) {
            Icon(
                imageVector = Icons.Rounded.AddPhotoAlternate,
                contentDescription = null,
                modifier = Modifier.size(19.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.btn_add_photo),
                style = MaterialTheme.typography.labelLarge
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = {
                filePickerLauncher.launch(arrayOf("*/*"))
            },
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = SlateSecondary,
                contentColor = Color.White
            ),
            contentPadding = PaddingValues(vertical = 14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .minimumInteractiveComponentSize()
                .testTag("add_file_button")
        ) {
            Icon(
                imageVector = Icons.Rounded.AttachFile,
                contentDescription = null,
                modifier = Modifier.size(19.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.btn_add_file),
                style = MaterialTheme.typography.labelLarge
            )
        }

        if (uiState.isImportingItems) {
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                color = SkyBlueLight,
                trackColor = VaultSurfaceElevated,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(99.dp))
            )
        }

        if (uiState.feedback != null) {
            Spacer(modifier = Modifier.height(12.dp))
            FeedbackBannerCard(feedback = uiState.feedback)
        }

        Spacer(modifier = Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.vault_items_header, uiState.vaultItems.size),
                style = MaterialTheme.typography.titleMedium,
                color = SkyBlueLight,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (uiState.vaultItems.isEmpty()) {
            Surface(
                color = VaultSurfaceElevated.copy(alpha = 0.7f),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, VaultOutlineSubtle),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("vault_empty_state")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Rounded.FolderOpen,
                        contentDescription = null,
                        tint = SlateMuted,
                        modifier = Modifier.size(30.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.vault_empty_title),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.vault_empty_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = SlateMuted,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("vault_items_list"),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                uiState.vaultItems.forEach { item ->
                    VaultItemCard(
                        item = item,
                        isSaving = uiState.savingItemId == item.id,
                        onPreviewClick = { onOpenImagePreview(item) },
                        onSavePhotoClick = { handleSavePhotoClick(item.id) },
                        onRemoveClick = { onRemoveItem(item.id) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Surface(
            color = VaultSurfaceElevated,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, VaultOutlineSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                StatusRow(
                    icon = Icons.Rounded.Shield,
                    label = stringResource(R.string.status_pin_protection_label),
                    value = stringResource(R.string.status_pin_protection_value),
                    valueColor = EmeraldSuccess
                )
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 10.dp),
                    color = VaultOutlineSubtle
                )
                StatusRow(
                    icon = Icons.Rounded.Key,
                    label = stringResource(R.string.status_backup_label),
                    value = stringResource(R.string.status_backup_value),
                    valueColor = SkyBlueLight
                )
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 10.dp),
                    color = VaultOutlineSubtle
                )
                StatusRow(
                    icon = Icons.Rounded.Schedule,
                    label = stringResource(R.string.status_last_unlocked_label),
                    value = formattedTime ?: stringResource(R.string.status_just_now),
                    valueColor = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Button(
            onClick = onLockAgain,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = SlateSecondary,
                contentColor = Color.White
            ),
            contentPadding = PaddingValues(vertical = 15.dp),
            modifier = Modifier
                .fillMaxWidth()
                .minimumInteractiveComponentSize()
                .testTag("lock_again_button")
        ) {
            Icon(
                imageVector = Icons.Rounded.Lock,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.btn_lock_again),
                style = MaterialTheme.typography.labelLarge
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = onOpenRecovery,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, VaultOutline),
                contentPadding = PaddingValues(vertical = 13.dp, horizontal = 10.dp),
                modifier = Modifier
                    .weight(1f)
                    .minimumInteractiveComponentSize()
                    .testTag("unlocked_change_pin_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.LockReset,
                    contentDescription = null,
                    tint = SkyBlueLight,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.btn_change_pin_shortcut),
                    style = MaterialTheme.typography.bodyMedium,
                    color = SkyBlueLight,
                    fontWeight = FontWeight.Medium
                )
            }

            OutlinedButton(
                onClick = onRequestReset,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, CoralError.copy(alpha = 0.45f)),
                contentPadding = PaddingValues(vertical = 13.dp, horizontal = 10.dp),
                modifier = Modifier
                    .weight(1f)
                    .minimumInteractiveComponentSize()
                    .testTag("reset_vault_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.DeleteOutline,
                    contentDescription = null,
                    tint = CoralError,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.btn_reset_vault),
                    style = MaterialTheme.typography.bodyMedium,
                    color = CoralError,
                    fontWeight = FontWeight.Medium
                )
                }
            }
        }
        }
    }

    uiState.previewImageItem?.let { item ->
        ImagePreviewDialog(
            item = item,
            isSaving = uiState.savingItemId == item.id,
            onSaveClick = { handleSavePhotoClick(item.id) },
            onDismiss = onDismissImagePreview
        )
    }

    if (uiState.showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = onDismissReset,
            containerColor = VaultSurfaceElevated,
            title = {
                Text(
                    text = stringResource(R.string.dialog_reset_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.dialog_reset_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = onConfirmReset,
                    modifier = Modifier.testTag("confirm_reset_button")
                ) {
                    Text(
                        text = stringResource(R.string.dialog_confirm_reset),
                        color = CoralError,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissReset) {
                    Text(
                        text = stringResource(R.string.dialog_cancel),
                        color = SkyBlueLight
                    )
                }
            }
        )
    }
}

@Composable
private fun UnlockedTabBar(
    selectedTab: UnlockedTab,
    profilePhotoPath: String?,
    onSelectTab: (UnlockedTab) -> Unit
) {
    Surface(
        color = VaultSurface,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, VaultOutline.copy(alpha = 0.75f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val isVaultSelected = selectedTab == UnlockedTab.Vault
            Surface(
                color = if (isVaultSelected) CobaltPrimary else Color.Transparent,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .minimumInteractiveComponentSize()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onSelectTab(UnlockedTab.Vault) }
                    .testTag("tab_vault")
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Shield,
                        contentDescription = null,
                        tint = if (isVaultSelected) Color.White else SlateMuted,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.tab_vault),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isVaultSelected) Color.White else SlateMuted
                    )
                }
            }

            val isProfileSelected = selectedTab == UnlockedTab.Profile
            Surface(
                color = if (isProfileSelected) FcBlackCard else Color.Transparent,
                shape = RoundedCornerShape(14.dp),
                border = if (isProfileSelected) {
                    BorderStroke(1.dp, FcPurpleNeon.copy(alpha = 0.85f))
                } else {
                    null
                },
                modifier = Modifier
                    .weight(1f)
                    .minimumInteractiveComponentSize()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onSelectTab(UnlockedTab.Profile) }
                    .testTag("tab_profile")
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (!profilePhotoPath.isNullOrBlank()) {
                        AsyncImage(
                            model = File(profilePhotoPath),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.img_fc_app_logo),
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .size(22.dp)
                                .clip(RoundedCornerShape(6.dp))
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.tab_profile),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isProfileSelected) FcGoldPrimary else SlateMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun VaultItemCard(
    item: VaultItem,
    isSaving: Boolean,
    onPreviewClick: () -> Unit,
    onSavePhotoClick: () -> Unit,
    onRemoveClick: () -> Unit
) {
    Surface(
        color = VaultSurfaceHighest,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, VaultOutline.copy(alpha = 0.7f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("vault_item_${item.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = VaultSurface,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (item.isImage) Icons.Rounded.Image else Icons.AutoMirrored.Rounded.InsertDriveFile,
                            contentDescription = stringResource(R.string.cd_vault_file_icon),
                            tint = SkyBlueLight,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = stringResource(R.string.item_size_kb, item.sizeKb),
                        style = MaterialTheme.typography.labelMedium,
                        color = SlateMuted
                    )
                }
            }

            if (item.isImage) {
                Spacer(modifier = Modifier.height(12.dp))
                AsyncImage(
                    model = File(item.localPath),
                    contentDescription = stringResource(R.string.cd_vault_photo_preview, item.name),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp, max = 220.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(VaultSurface)
                        .clickable(onClick = onPreviewClick)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (item.isImage) {
                val isSaved = item.savedGalleryUri != null
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onSavePhotoClick,
                        enabled = !isSaving,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSaved) EmeraldContainer else CobaltPrimary,
                            contentColor = if (isSaved) EmeraldSuccess else Color.White,
                            disabledContainerColor = CobaltPrimary.copy(alpha = 0.6f),
                            disabledContentColor = Color.White
                        ),
                        border = if (isSaved) BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.5f)) else null,
                        contentPadding = PaddingValues(vertical = 10.dp, horizontal = 8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("save_photo_${item.id}")
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Icon(
                                imageVector = if (isSaved) Icons.Rounded.CheckCircle else Icons.Rounded.Download,
                                contentDescription = stringResource(R.string.cd_save_to_phone),
                                modifier = Modifier.size(17.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(
                                if (isSaved) R.string.btn_saved_to_gallery else R.string.btn_save_to_phone
                            ),
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Button(
                        onClick = onRemoveClick,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SlateSecondary,
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(vertical = 10.dp, horizontal = 8.dp),
                        modifier = Modifier
                            .weight(0.85f)
                            .minimumInteractiveComponentSize()
                            .testTag("remove_item_${item.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = null,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.btn_remove_item),
                            style = MaterialTheme.typography.labelLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            } else {
                Button(
                    onClick = onRemoveClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SlateSecondary,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(vertical = 10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .testTag("remove_item_${item.id}")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.btn_remove_item),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun ImagePreviewDialog(
    item: VaultItem,
    isSaving: Boolean,
    onSaveClick: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(VaultBackground.copy(alpha = 0.94f))
                .clickable(onClick = onDismiss)
                .padding(20.dp)
        ) {
            AsyncImage(
                model = File(item.localPath),
                contentDescription = stringResource(R.string.cd_vault_photo_preview, item.name),
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(16.dp))
            )

            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = VaultSurfaceElevated.copy(alpha = 0.92f),
                    shape = CircleShape,
                    border = BorderStroke(1.dp, VaultOutline)
                ) {
                    IconButton(
                        onClick = onSaveClick,
                        enabled = !isSaving,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("preview_save_photo_button")
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                color = SkyBlueLight,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Icon(
                                imageVector = if (item.savedGalleryUri != null) {
                                    Icons.Rounded.CheckCircle
                                } else {
                                    Icons.Rounded.Download
                                },
                                contentDescription = stringResource(R.string.cd_save_to_phone),
                                tint = if (item.savedGalleryUri != null) EmeraldSuccess else SkyBlueLight
                            )
                        }
                    }
                }

                Surface(
                    color = VaultSurfaceElevated.copy(alpha = 0.92f),
                    shape = CircleShape,
                    border = BorderStroke(1.dp, VaultOutline)
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = stringResource(R.string.cd_close_preview),
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusRow(
    icon: ImageVector,
    label: String,
    value: String,
    valueColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = SlateMuted,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = valueColor,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun VaultTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    isSecret: Boolean = false,
    onToggleVisibility: (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        label = { Text(text = label, style = MaterialTheme.typography.bodyMedium) },
        supportingText = supportingText?.let { text ->
            {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SlateMuted
                )
            }
        },
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = SkyBlueLight
            )
        },
        trailingIcon = onToggleVisibility?.let { toggle ->
            {
                IconButton(
                    onClick = toggle,
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Icon(
                        imageVector = if (isSecret) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                        contentDescription = stringResource(R.string.cd_toggle_visibility),
                        tint = SlateMuted
                    )
                }
            }
        },
        visualTransformation = if (isSecret) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CobaltPrimary,
            unfocusedBorderColor = VaultOutline,
            focusedContainerColor = VaultSurfaceElevated,
            unfocusedContainerColor = VaultSurfaceElevated,
            focusedLabelColor = SkyBlueLight,
            unfocusedLabelColor = SlateMuted,
            cursorColor = SkyBlueLight,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        ),
        modifier = modifier.fillMaxWidth()
    )
}
