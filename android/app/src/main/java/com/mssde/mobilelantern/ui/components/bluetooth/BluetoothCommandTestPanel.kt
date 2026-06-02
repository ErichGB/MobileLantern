package com.mssde.mobilelantern.ui.components.bluetooth

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mssde.mobilelantern.ui.theme.MobileLanternTheme

data class BleCommandRow(
    val label: String,
    val command: String,
    val releaseCommand: String? = null
)

private val ledColorCommands = listOf(
    BleCommandRow("Color rojo", "COLOR:255,0,0", releaseCommand = "LED_OFF"),
    BleCommandRow("Color verde", "COLOR:0,255,0", releaseCommand = "LED_OFF"),
    BleCommandRow("Color azul", "COLOR:0,0,255", releaseCommand = "LED_OFF"),
    BleCommandRow("Color blanco", "COLOR:255,255,255", releaseCommand = "LED_OFF")
)

private val ledEffectCommands = listOf(
    BleCommandRow("Efecto arcoíris", "EFFECT:RAINBOW", releaseCommand = "LED_OFF"),
    BleCommandRow("Efecto pulso", "EFFECT:PULSE", releaseCommand = "LED_OFF"),
    BleCommandRow("Efecto spin", "EFFECT:SPIN", releaseCommand = "LED_OFF"),
    BleCommandRow("Efecto respiración", "EFFECT:BREATHE", releaseCommand = "LED_OFF")
)

private val buzzerCommands = listOf(
    BleCommandRow("Buzzer encendido", "BUZZER:ON", releaseCommand = "BUZZER:OFF"),
    BleCommandRow("Un pitido", "BUZZER:BEEP"),
    BleCommandRow("3 pitidos", "BUZZER:BEEP:3"),
    BleCommandRow("5 pitidos", "BUZZER:BEEP:5")
)

private val buzzerToneCommands = listOf(
    BleCommandRow("Bienvenida", "BUZZER:TONE:WELCOME"),
    BleCommandRow("Despedida", "BUZZER:TONE:GOODBYE"),
    BleCommandRow("Notificación general", "BUZZER:TONE:NOTIFY"),
    BleCommandRow("Éxito / tarea completada", "BUZZER:TONE:SUCCESS"),
    BleCommandRow("Alerta / atención requerida", "BUZZER:TONE:ALERT")
)

@Composable
fun BluetoothCommandTestPanel(
    isConnected: Boolean,
    onSendCommand: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val latestConnected = rememberUpdatedState(isConnected)
    val latestOnSend = rememberUpdatedState(onSendCommand)

    DisposableEffect(Unit) {
        onDispose {
            if (latestConnected.value) {
                latestOnSend.value("LED_OFF")
                latestOnSend.value("BUZZER:OFF")
            }
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = if (isConnected) {
                    "Mantiene pulsado para probar. Suelta para detener."
                } else {
                    "Conecta un dispositivo para enviar comandos."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        item {
            CommandSectionTitle("LED — color y brillo")
        }
        items(ledColorCommands) { row ->
            CommandRowButton(
                row = row,
                enabled = isConnected,
                onSendCommand = onSendCommand
            )
        }

        item {
            CommandSectionTitle("LED — efectos")
        }
        items(ledEffectCommands) { row ->
            CommandRowButton(
                row = row,
                enabled = isConnected,
                onSendCommand = onSendCommand
            )
        }

        item {
            CommandSectionTitle("Buzzer")
        }
        items(buzzerCommands) { row ->
            CommandRowButton(
                row = row,
                enabled = isConnected,
                onSendCommand = onSendCommand
            )
        }

        item {
            CommandSectionTitle("Buzzer — melodías (TONE)")
        }
        items(buzzerToneCommands) { row ->
            CommandRowButton(
                row = row,
                enabled = isConnected,
                onSendCommand = onSendCommand
            )
        }
    }
}

@Composable
private fun CommandSectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 4.dp)
    )
}

@Composable
private fun CommandRowButton(
    row: BleCommandRow,
    enabled: Boolean,
    onSendCommand: (String) -> Unit
) {
    if (row.releaseCommand != null) {
        HoldToTestButton(
            row = row,
            enabled = enabled,
            onSendCommand = onSendCommand
        )
    } else {
        CommandSendButton(
            row = row,
            enabled = enabled,
            onSendCommand = onSendCommand
        )
    }
}

@Composable
private fun HoldToTestButton(
    row: BleCommandRow,
    enabled: Boolean,
    onSendCommand: (String) -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val sendUpdated = rememberUpdatedState(onSendCommand)
    val rowUpdated = rememberUpdatedState(row)
    val scheme = MaterialTheme.colorScheme
    val shape = MaterialTheme.shapes.medium

    val bgColor = if (isPressed) {
        val infiniteTransition = rememberInfiniteTransition(label = "holdBreathe")
        val breathe by infiniteTransition.animateFloat(
            initialValue = 0.88f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 800, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "breathe"
        )
        scheme.primaryContainer.copy(alpha = scheme.primaryContainer.alpha * breathe)
    } else {
        scheme.surface
    }

    val borderColor = if (isPressed) scheme.primary else scheme.outline

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.38f)
            .pointerInput(enabled, row.command, row.releaseCommand) {
                if (!enabled) return@pointerInput
                detectTapGestures(
                    onPress = {
                        val currentRow = rowUpdated.value
                        isPressed = true
                        sendUpdated.value(currentRow.command)
                        try {
                            tryAwaitRelease()
                        } finally {
                            isPressed = false
                            currentRow.releaseCommand?.let { sendUpdated.value(it) }
                        }
                    }
                )
            },
        shape = shape,
        color = bgColor,
        border = BorderStroke(1.dp, borderColor),
        tonalElevation = if (isPressed) 2.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = row.label, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = row.command,
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = if (isPressed) Icons.Filled.TouchApp else Icons.Outlined.TouchApp,
                contentDescription = null,
                tint = if (isPressed) scheme.primary else scheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CommandSendButton(
    row: BleCommandRow,
    enabled: Boolean,
    onSendCommand: (String) -> Unit
) {
    FilledTonalButton(
        onClick = { onSendCommand(row.command) },
        enabled = enabled,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = row.label, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = row.command,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 640)
@Composable
fun PreviewBluetoothCommandTestPanelConnected() {
    MobileLanternTheme {
        BluetoothCommandTestPanel(
            isConnected = true,
            onSendCommand = {}
        )
    }
}

@Preview(showBackground = true, heightDp = 200)
@Composable
fun PreviewBluetoothCommandTestPanelDisconnected() {
    MobileLanternTheme {
        BluetoothCommandTestPanel(
            isConnected = false,
            onSendCommand = {}
        )
    }
}
