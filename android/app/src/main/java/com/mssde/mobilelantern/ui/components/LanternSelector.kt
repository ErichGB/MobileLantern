package com.mssde.mobilelantern.ui.components

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mssde.mobilelantern.R
import com.mssde.mobilelantern.ui.theme.MobileLanternTheme
import com.mssde.mobilelantern.viewmodel.BluetoothConnectionState
import com.mssde.mobilelantern.viewmodel.BluetoothViewModel
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.delay

enum class LanternType { NONE, INTEGRATED, EXTERNAL }

enum class BluetoothLanternAvailability {
    NO_HARDWARE,
    DISABLED,
    AVAILABLE
}

@Composable
fun rememberBluetoothLanternAvailability(): BluetoothLanternAvailability {
    val context = LocalContext.current
    if (LocalInspectionMode.current) {
        return BluetoothLanternAvailability.AVAILABLE
    }
    val adapter = (context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter
    if (adapter == null) {
        return BluetoothLanternAvailability.NO_HARDWARE
    }
    var isEnabled by remember(adapter) { mutableStateOf(adapter.isEnabled) }
    DisposableEffect(adapter) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                if (intent.action != BluetoothAdapter.ACTION_STATE_CHANGED) return
                val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                isEnabled = state == BluetoothAdapter.STATE_ON
            }
        }
        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            context.registerReceiver(receiver, filter)
        }
        onDispose {
            context.unregisterReceiver(receiver)
        }
    }
    return if (isEnabled) BluetoothLanternAvailability.AVAILABLE else BluetoothLanternAvailability.DISABLED
}

@Composable
fun LanternSelector(
    selectedType: LanternType,
    onSelectIntegrated: () -> Unit,
    onNavigateIntegrated: () -> Unit,
    onSelectExternal: () -> Unit,
    onNavigateExternal: () -> Unit,
    modifier: Modifier = Modifier,
    bluetoothLanternAvailability: BluetoothLanternAvailability? = null,
    bluetoothConnectionStateOverride: BluetoothConnectionState? = null,
    highlightJustConnected: Boolean = false
) {
    val resolvedBluetoothAvailability =
        bluetoothLanternAvailability ?: rememberBluetoothLanternAvailability()
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Linterna",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IntegratedLanternCard(
                isSelected = selectedType == LanternType.INTEGRATED,
                onSelect = onSelectIntegrated,
                onNavigate = onNavigateIntegrated,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            ExternalLanternCard(
                isSelected = selectedType == LanternType.EXTERNAL,
                onSelect = onSelectExternal,
                onNavigate = onNavigateExternal,
                btAvailability = resolvedBluetoothAvailability,
                btStateOverride = bluetoothConnectionStateOverride,
                highlightJustConnected = highlightJustConnected,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }
    }
}

@Composable
private fun LanternSelectionIndicator(
    isSelected: Boolean,
    enabled: Boolean,
    onSelect: () -> Unit,
    onBlockedSelect: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    }
    val clickModifier = when {
        enabled -> Modifier.clickable { onSelect() }
        onBlockedSelect != null -> Modifier.clickable { onBlockedSelect() }
        else -> Modifier
    }
    Box(
        modifier = modifier
            .size(22.dp)
            .alpha(if (enabled) 1f else 0.38f)
            .clip(CircleShape)
            .border(2.dp, borderColor, CircleShape)
            .then(clickModifier),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Box(
                Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}

@Composable
private fun LanternOptionCard(
    isSelected: Boolean,
    dimCard: Boolean,
    navigationEnabled: Boolean,
    selectionEnabled: Boolean,
    onSelect: () -> Unit,
    onNavigate: () -> Unit,
    onSelectionBlocked: (() -> Unit)? = null,
    borderColorOverride: Color? = null,
    modifier: Modifier = Modifier,
    leading: @Composable () -> Unit,
    title: @Composable () -> Unit,
    subtitle: @Composable () -> Unit
) {
    val border = if (isSelected) {
        BorderStroke(2.dp, borderColorOverride ?: MaterialTheme.colorScheme.primary)
    } else {
        null
    }

    Card(
        modifier = modifier.alpha(if (dimCard) 0.55f else 1f),
        shape = MaterialTheme.shapes.extraLarge,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = border
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 10.dp, end = 10.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clickable(enabled = navigationEnabled) { onNavigate() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                leading()
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            title()
                            Icon(
                                imageVector = Icons.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    subtitle()
                }
            }
            LanternSelectionIndicator(
                isSelected = isSelected,
                enabled = selectionEnabled,
                onSelect = onSelect,
                onBlockedSelect = onSelectionBlocked
            )
        }
    }
}

@Composable
private fun IntegratedLanternCard(
    isSelected: Boolean,
    onSelect: () -> Unit,
    onNavigate: () -> Unit,
    modifier: Modifier = Modifier
) {
    LanternOptionCard(
        isSelected = isSelected,
        dimCard = false,
        navigationEnabled = true,
        selectionEnabled = true,
        onSelect = onSelect,
        onNavigate = onNavigate,
        modifier = modifier,
        leading = {
            Icon(
                painter = painterResource(id = R.drawable.ic_flashlight),
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = if (isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        title = {
            Text(
                text = "Integrada",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        },
        subtitle = {
            Text(
                text = "Teléfono",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    )
}

@Composable
private fun ExternalLanternCard(
    isSelected: Boolean,
    onSelect: () -> Unit,
    onNavigate: () -> Unit,
    btAvailability: BluetoothLanternAvailability,
    btStateOverride: BluetoothConnectionState? = null,
    highlightJustConnected: Boolean,
    modifier: Modifier = Modifier
) {
    val btState = btStateOverride ?: rememberBluetoothConnectionState()
    val isConnected = btState.isConnected
    val canUseExternal = btAvailability == BluetoothLanternAvailability.AVAILABLE
    val canSelectExternal = canUseExternal && isConnected
    val effectiveSelected = isSelected && canSelectExternal
    var showPulseHighlight by remember { mutableStateOf(false) }
    val pulseTransition = rememberInfiniteTransition(label = "externalCardPulse")
    val pulseAlpha by pulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 300, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "externalCardPulseAlpha"
    )
    val highlightedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = pulseAlpha)

    LaunchedEffect(highlightJustConnected, effectiveSelected) {
        if (highlightJustConnected && effectiveSelected) {
            showPulseHighlight = true
            delay(1200)
            showPulseHighlight = false
        } else if (!effectiveSelected) {
            showPulseHighlight = false
        }
    }

    val context = LocalContext.current
    val onSelectionBlocked =
        if (canUseExternal && !isConnected) {
            {
                Toast.makeText(
                    context,
                    "Conecta un dispositivo para usarla",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            null
        }

    LanternOptionCard(
        isSelected = effectiveSelected,
        dimCard = !canUseExternal,
        navigationEnabled = canUseExternal,
        selectionEnabled = canSelectExternal,
        onSelect = if (canSelectExternal) onSelect else ({}),
        onNavigate = if (canUseExternal) onNavigate else ({}),
        onSelectionBlocked = onSelectionBlocked,
        borderColorOverride = if (showPulseHighlight) highlightedBorderColor else null,
        modifier = modifier,
        leading = {
            if (canUseExternal) {
                Text(
                    text = "\uD83D\uDEA8",
                    fontSize = 28.sp
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.BluetoothDisabled,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        title = {
            Text(
                text = "Externa",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        },
        subtitle = {
            when (btAvailability) {
                BluetoothLanternAvailability.NO_HARDWARE -> Text(
                    text = "No disponible",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                BluetoothLanternAvailability.DISABLED -> Text(
                    text = "Bluetooth desactivado",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                BluetoothLanternAvailability.AVAILABLE -> if (isConnected) {
                    Text(
                        text = btState.deviceName ?: "Conectado",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Text(
                        text = "Conectar",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    )
}

@Composable
internal fun rememberBluetoothConnectionState(): BluetoothConnectionState {
    val isPreview = LocalInspectionMode.current
    if (isPreview) {
        return BluetoothConnectionState()
    }
    val bluetoothViewModel = rememberBluetoothViewModel()
    val state by bluetoothViewModel.connectionState.collectAsState()
    return state
}

@Composable
private fun rememberBluetoothViewModel(): BluetoothViewModel {
    val context = LocalContext.current.applicationContext
    return remember(context) {
        EntryPointAccessors.fromApplication(
            context,
            BluetoothViewModelEntryPoint::class.java
        ).bluetoothViewModel()
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
internal interface BluetoothViewModelEntryPoint {
    fun bluetoothViewModel(): BluetoothViewModel
}

@Preview(showBackground = true)
@Composable
private fun LanternSelectorIntegratedPreview() {
    MobileLanternTheme {
        LanternSelector(
            selectedType = LanternType.INTEGRATED,
            onSelectIntegrated = {},
            onNavigateIntegrated = {},
            onSelectExternal = {},
            onNavigateExternal = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "Externa BT disponible sin conexión")
@Composable
private fun LanternSelectorExternalPreview() {
    MobileLanternTheme {
        LanternSelector(
            selectedType = LanternType.NONE,
            onSelectIntegrated = {},
            onNavigateIntegrated = {},
            onSelectExternal = {},
            onNavigateExternal = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "Externa recién conectada")
@Composable
private fun LanternSelectorExternalJustConnectedPreview() {
    MobileLanternTheme {
        LanternSelector(
            selectedType = LanternType.EXTERNAL,
            onSelectIntegrated = {},
            onNavigateIntegrated = {},
            onSelectExternal = {},
            onNavigateExternal = {},
            bluetoothLanternAvailability = BluetoothLanternAvailability.AVAILABLE,
            bluetoothConnectionStateOverride = BluetoothConnectionState(
                isConnected = true,
                deviceName = "ESP32_LANTERN_01",
                deviceMac = "30:C6:F7:1F:D8:CA"
            ),
            highlightJustConnected = true,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LanternSelectorNonePreview() {
    MobileLanternTheme {
        LanternSelector(
            selectedType = LanternType.NONE,
            onSelectIntegrated = {},
            onNavigateIntegrated = {},
            onSelectExternal = {},
            onNavigateExternal = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "Externa BT desactivado")
@Composable
private fun LanternSelectorExternalBtDisabledPreview() {
    MobileLanternTheme {
        LanternSelector(
            selectedType = LanternType.EXTERNAL,
            onSelectIntegrated = {},
            onNavigateIntegrated = {},
            onSelectExternal = {},
            onNavigateExternal = {},
            bluetoothLanternAvailability = BluetoothLanternAvailability.DISABLED,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "Externa sin hardware BT")
@Composable
private fun LanternSelectorExternalNoHardwarePreview() {
    MobileLanternTheme {
        LanternSelector(
            selectedType = LanternType.NONE,
            onSelectIntegrated = {},
            onNavigateIntegrated = {},
            onSelectExternal = {},
            onNavigateExternal = {},
            bluetoothLanternAvailability = BluetoothLanternAvailability.NO_HARDWARE,
            modifier = Modifier.padding(16.dp)
        )
    }
}
