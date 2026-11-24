package com.example

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.LockDestination
import com.example.ui.LockViewModel
import com.example.ui.RecoveryScreen
import com.example.ui.SetupScreen
import com.example.ui.UnlockScreen
import com.example.ui.UnlockedScreen
import com.example.ui.VaultBackdrop
import com.example.ui.VaultBrandHeader
import com.example.ui.theme.MyLockTheme
import com.example.ui.theme.SkyBlueLight
import com.example.ui.theme.VaultBackground
import com.example.ui.theme.VaultSurfaceElevated

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(VaultBackground.toArgb()),
            navigationBarStyle = SystemBarStyle.dark(VaultBackground.toArgb())
        )
        setContent {
            MyLockTheme {
                MyLockApp()
            }
        }
    }
}

@Composable
fun MyLockApp(
    viewModel: LockViewModel = viewModel(
        factory = LockViewModel.factory(LocalContext.current.applicationContext as Application)
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarNotice?.id) {
        val notice = uiState.snackbarNotice ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message = context.getString(notice.messageResId))
        viewModel.consumeSnackbarNotice()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = VaultSurfaceElevated,
                    contentColor = Color.White
                )
            }
        }
    ) { innerPadding ->
        VaultBackdrop {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .imePadding(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 460.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    VaultBrandHeader(
                        isUnlocked = uiState.destination == LockDestination.Unlocked
                    )

                    Spacer(modifier = Modifier.height(22.dp))

                    AnimatedContent(
                        targetState = uiState.destination,
                        transitionSpec = {
                            (fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) +
                                scaleIn(initialScale = 0.96f, animationSpec = spring(stiffness = Spring.StiffnessMediumLow)))
                                .togetherWith(fadeOut(spring(stiffness = Spring.StiffnessMedium)))
                        },
                        label = "lock_destination_transition"
                    ) { destination ->
                        when (destination) {
                            LockDestination.Loading -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(220.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = SkyBlueLight)
                                }
                            }

                            LockDestination.Setup -> {
                                SetupScreen(
                                    uiState = uiState,
                                    onPinChange = viewModel::onSetupPinChange,
                                    onBackupChange = viewModel::onSetupBackupChange,
                                    onSaveSetup = viewModel::saveSetup
                                )
                            }

                            LockDestination.Unlock -> {
                                UnlockScreen(
                                    uiState = uiState,
                                    onDigitClick = viewModel::onKeypadDigit,
                                    onBackspaceClick = viewModel::onKeypadBackspace,
                                    onClearClick = viewModel::onKeypadClear,
                                    onSubmitUnlock = viewModel::submitUnlock,
                                    onOpenRecovery = viewModel::openRecovery
                                )
                            }

                            LockDestination.Recovery -> {
                                RecoveryScreen(
                                    uiState = uiState,
                                    onBackupChange = viewModel::onRecoveryBackupChange,
                                    onNewPinChange = viewModel::onRecoveryNewPinChange,
                                    onSubmitRecovery = viewModel::submitRecovery,
                                    onBackToUnlock = viewModel::returnToUnlock
                                )
                            }

                            LockDestination.Unlocked -> {
                                UnlockedScreen(
                                    uiState = uiState,
                                    onSelectTab = viewModel::selectUnlockedTab,
                                    onAddPhotos = { uris ->
                                        viewModel.onAddVaultUris(uris.map { it.toString() }, forceImage = true)
                                    },
                                    onAddFiles = { uris ->
                                        viewModel.onAddVaultUris(uris.map { it.toString() }, forceImage = false)
                                    },
                                    onSavePhoto = viewModel::savePhotoToPhone,
                                    onLegacyPermissionDenied = viewModel::onLegacyStoragePermissionDenied,
                                    onRemoveItem = viewModel::onRemoveVaultItem,
                                    onOpenImagePreview = viewModel::openImagePreview,
                                    onDismissImagePreview = viewModel::dismissImagePreview,
                                    onProfilePhotoPicked = { uri ->
                                        viewModel.onProfilePhotoPicked(uri.toString())
                                    },
                                    onRemoveProfilePhoto = viewModel::onRemoveProfilePhoto,
                                    onProfileNameChange = viewModel::onProfileNameChange,
                                    onProfileUsernameChange = viewModel::onProfileUsernameChange,
                                    onProfileBioChange = viewModel::onProfileBioChange,
                                    onSaveProfile = viewModel::saveProfile,
                                    onLockAgain = viewModel::lockAgain,
                                    onOpenRecovery = viewModel::openRecovery,
                                    onRequestReset = viewModel::requestResetVault,
                                    onDismissReset = viewModel::dismissResetVault,
                                    onConfirmReset = viewModel::confirmResetVault
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
