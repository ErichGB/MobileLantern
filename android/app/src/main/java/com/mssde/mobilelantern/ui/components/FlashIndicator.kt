package com.mssde.mobilelantern.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mssde.mobilelantern.data.local.LanternPreferenceManager
import com.mssde.mobilelantern.hardware.FlashState
import com.mssde.mobilelantern.hardware.HardwareController
import com.mssde.mobilelantern.hardware.LedColor
import com.mssde.mobilelantern.viewmodel.BluetoothViewModel
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.isActive

@Composable
fun FlashIndicator(
    modifier: Modifier = Modifier,
    size: Dp = 12.dp,
    activeColor: Color = MaterialTheme.colorScheme.tertiary,
    inactiveColor: Color = MaterialTheme.colorScheme.outlineVariant,
    inactiveAlpha: Float = 0.25f
) {
    val flashState by rememberFlashState()
    val lanternType by rememberLanternType()
    val isBluetoothConnected by rememberBluetoothConnected()
    FlashIndicatorContent(
        flashState = flashState,
        lanternType = lanternType,
        isBluetoothConnected = isBluetoothConnected,
        modifier = modifier,
        size = size,
        activeColor = activeColor,
        inactiveColor = inactiveColor,
        inactiveAlpha = inactiveAlpha
    )
}

@Composable
fun FlashIndicatorContent(
    flashState: FlashState,
    lanternType: LanternType,
    isBluetoothConnected: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 12.dp,
    activeColor: Color = MaterialTheme.colorScheme.tertiary,
    inactiveColor: Color = MaterialTheme.colorScheme.outlineVariant,
    inactiveAlpha: Float = 0.25f
) {
    val animatableAlpha = remember { Animatable(inactiveAlpha) }
    val color = when (val state = flashState) {
        FlashState.Off -> inactiveColor
        is FlashState.On -> state.color.toComposeColor() ?: activeColor
        is FlashState.Flashing -> state.color.toComposeColor() ?: activeColor
    }

    LaunchedEffect(flashState) {
        when (flashState) {
            FlashState.Off -> {
                animatableAlpha.animateTo(
                    targetValue = inactiveAlpha,
                    animationSpec = tween(durationMillis = 200, easing = LinearEasing)
                )
            }
            is FlashState.On -> {
                animatableAlpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 200, easing = LinearEasing)
                )
            }
            is FlashState.Flashing -> {
                val flashingState = flashState as FlashState.Flashing
                val onDuration = flashingState.onDurationMs.coerceAtLeast(0L)
                val offDuration = (flashingState.periodMs - flashingState.onDurationMs).coerceAtLeast(0L)
                while (isActive) {
                    if (onDuration > 0L) {
                        animatableAlpha.animateTo(
                            targetValue = 1f,
                            animationSpec = tween(
                                durationMillis = onDuration.toInt().coerceAtLeast(0),
                                easing = LinearEasing
                            )
                        )
                    } else {
                        animatableAlpha.snapTo(1f)
                    }

                    if (offDuration > 0L) {
                        animatableAlpha.animateTo(
                            targetValue = inactiveAlpha,
                            animationSpec = tween(
                                durationMillis = offDuration.toInt().coerceAtLeast(0),
                                easing = LinearEasing
                            )
                        )
                    } else {
                        animatableAlpha.snapTo(inactiveAlpha)
                    }
                }
            }
        }
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(color.copy(alpha = animatableAlpha.value))
        )

        if (lanternType == LanternType.EXTERNAL) {
            val btIcon = if (isBluetoothConnected) Icons.Default.Bluetooth else Icons.Default.BluetoothDisabled
            val btTint = if (isBluetoothConnected)
                Color(0xFF4CAF50)
            else
                MaterialTheme.colorScheme.error

            Icon(
                imageVector = btIcon,
                contentDescription = if (isBluetoothConnected) "Bluetooth conectado" else "Bluetooth desconectado",
                modifier = Modifier.size(size),
                tint = btTint
            )
        }
    }
}

@Composable
fun rememberFlashState(): State<FlashState> {
    val isPreview = LocalInspectionMode.current
    if (isPreview) {
        return remember { mutableStateOf<FlashState>(FlashState.Off) }
    }
    val hardwareController = rememberHardwareController()
    return hardwareController.flashState.collectAsState()
}

@Composable
private fun rememberLanternType(): State<LanternType> {
    val isPreview = LocalInspectionMode.current
    if (isPreview) {
        return remember { mutableStateOf(LanternType.NONE) }
    }
    val lanternPreferenceManager = rememberLanternPreferenceManager()
    return lanternPreferenceManager.selectedLanternType.collectAsState()
}

@Composable
private fun rememberBluetoothConnected(): State<Boolean> {
    val isPreview = LocalInspectionMode.current
    if (isPreview) {
        return remember { mutableStateOf(false) }
    }
    val bluetoothViewModel = rememberBluetoothViewModelFromIndicator()
    val connectionState by bluetoothViewModel.connectionState.collectAsState()
    return remember(connectionState.isConnected) { mutableStateOf(connectionState.isConnected) }
}

@Composable
private fun rememberHardwareController(): HardwareController {
    val context = LocalContext.current.applicationContext
    return remember(context) {
        EntryPointAccessors.fromApplication(
            context,
            FlashIndicatorEntryPoint::class.java
        ).hardwareController()
    }
}

@Composable
private fun rememberLanternPreferenceManager(): LanternPreferenceManager {
    val context = LocalContext.current.applicationContext
    return remember(context) {
        EntryPointAccessors.fromApplication(
            context,
            FlashIndicatorEntryPoint::class.java
        ).lanternPreferenceManager()
    }
}

@Composable
private fun rememberBluetoothViewModelFromIndicator(): BluetoothViewModel {
    val context = LocalContext.current.applicationContext
    return remember(context) {
        EntryPointAccessors.fromApplication(
            context,
            FlashIndicatorEntryPoint::class.java
        ).bluetoothViewModel()
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
internal interface FlashIndicatorEntryPoint {
    fun hardwareController(): HardwareController
    fun lanternPreferenceManager(): LanternPreferenceManager
    fun bluetoothViewModel(): BluetoothViewModel
}

private fun LedColor.toComposeColor(): Color? = when (this) {
    LedColor.GREEN -> Color(0xFF4CAF50)
    LedColor.RED -> Color(0xFFF44336)
    LedColor.WHITE -> null
}
