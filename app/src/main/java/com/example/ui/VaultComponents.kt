package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.CobaltPrimary
import com.example.ui.theme.CoralContainer
import com.example.ui.theme.CoralError
import com.example.ui.theme.DisplayLatinFontFamily
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.FcBlackDeep
import com.example.ui.theme.FcGoldPrimary
import com.example.ui.theme.FcPurpleNeon
import com.example.ui.theme.SkyBlueLight
import com.example.ui.theme.VaultBackground
import com.example.ui.theme.VaultOutline
import com.example.ui.theme.VaultSurface
import com.example.ui.theme.VaultSurfaceElevated
import kotlin.math.roundToInt

@Composable
fun VaultBackdrop(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(VaultBackground)
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            CobaltPrimary.copy(alpha = 0.22f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.5f, size.height * 0.16f),
                        radius = size.maxDimension * 0.55f
                    )
                )
            }
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_vault_banner),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .alpha(0.32f)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(270.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            VaultBackground.copy(alpha = 0.2f),
                            VaultBackground.copy(alpha = 0.78f),
                            VaultBackground
                        )
                    )
                )
        )
        content()
    }
}

@Composable
fun VaultBrandHeader(
    isUnlocked: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = FcBlackDeep,
            border = BorderStroke(
                width = 2.dp,
                brush = Brush.linearGradient(
                    colors = if (isUnlocked) {
                        listOf(EmeraldSuccess, FcGoldPrimary, EmeraldSuccess)
                    } else {
                        listOf(FcPurpleNeon, FcGoldPrimary, FcPurpleNeon)
                    }
                )
            ),
            shadowElevation = 16.dp,
            modifier = Modifier
                .size(width = 130.dp, height = 112.dp)
                .testTag("top_fc_logo")
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_fc_header_logo),
                contentDescription = stringResource(R.string.cd_fc_logo),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(24.dp))
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineLarge,
            color = SkyBlueLight,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stringResource(R.string.app_subtitle),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun FeedbackBannerCard(
    feedback: FeedbackBanner?,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = feedback != null,
        enter = fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
        if (feedback != null) {
            val isError = feedback is FeedbackBanner.Error
            val containerColor = if (isError) CoralContainer else EmeraldContainer
            val accentColor = if (isError) CoralError else AmberAccent
            val messageText = when (feedback) {
                is FeedbackBanner.Error -> stringResource(feedback.messageResId)
                is FeedbackBanner.Info -> {
                    val count = feedback.countArg
                    if (count != null) stringResource(feedback.messageResId, count)
                    else stringResource(feedback.messageResId)
                }
            }
            val icon = if (isError) Icons.Rounded.ErrorOutline else Icons.Rounded.CheckCircle

            Surface(
                color = containerColor,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.45f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("feedback_message")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = messageText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = accentColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun PinDotsIndicator(
    enteredLength: Int,
    wrongAttemptTrigger: Int,
    modifier: Modifier = Modifier
) {
    val shakeOffset = remember { Animatable(0f) }

    LaunchedEffect(wrongAttemptTrigger) {
        if (wrongAttemptTrigger > 0) {
            val offsets = listOf(-18f, 18f, -14f, 14f, -8f, 8f, 0f)
            offsets.forEach { target ->
                shakeOffset.animateTo(
                    targetValue = target,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessHigh
                    )
                )
            }
        }
    }

    val progressDesc = stringResource(R.string.pin_dot_progress, enteredLength)

    Row(
        modifier = modifier
            .offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
            .semantics { contentDescription = progressDesc }
            .testTag("pin_dots_indicator"),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(4) { index ->
            val isFilled = index < enteredLength
            val scale by animateFloatAsState(
                targetValue = if (isFilled) 1.18f else 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                label = "pin_dot_scale"
            )
            val fillColor by animateColorAsState(
                targetValue = if (isFilled) SkyBlueLight else VaultSurface,
                label = "pin_dot_color"
            )
            val borderColor by animateColorAsState(
                targetValue = if (isFilled) CobaltPrimary else VaultOutline,
                label = "pin_dot_border"
            )

            Box(
                modifier = Modifier
                    .size(20.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(fillColor)
                    .border(2.dp, borderColor, CircleShape)
            )
        }
    }
}

@Composable
fun NumericKeypad(
    onDigitClick: (Char) -> Unit,
    onBackspaceClick: () -> Unit,
    onClearClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current
    val rows = listOf(
        listOf('1', '2', '3'),
        listOf('4', '5', '6'),
        listOf('7', '8', '9')
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        rows.forEach { rowDigits ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowDigits.forEach { digit ->
                    KeypadDigitButton(
                        digit = digit,
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onDigitClick(digit)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            KeypadActionButton(
                icon = Icons.Rounded.Close,
                contentDescription = stringResource(R.string.cd_clear_pin),
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClearClick()
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("keypad_clear")
            )

            KeypadDigitButton(
                digit = '0',
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onDigitClick('0')
                },
                modifier = Modifier.weight(1f)
            )

            KeypadActionButton(
                icon = Icons.AutoMirrored.Rounded.Backspace,
                contentDescription = stringResource(R.string.cd_backspace),
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onBackspaceClick()
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("keypad_backspace")
            )
        }
    }
}

@Composable
private fun KeypadDigitButton(
    digit: Char,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = VaultSurfaceElevated,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, VaultOutline.copy(alpha = 0.7f)),
        modifier = modifier
            .height(58.dp)
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("keypad_$digit")
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = digit.toString(),
                fontFamily = DisplayLatinFontFamily,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun KeypadActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = VaultSurface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, VaultOutline.copy(alpha = 0.5f)),
        modifier = modifier
            .height(58.dp)
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = SkyBlueLight,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
