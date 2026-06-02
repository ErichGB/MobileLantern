package com.mssde.mobilelantern.ui.components.bluetooth

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.DialogProperties
import com.mssde.mobilelantern.ui.theme.MobileLanternTheme

@Composable
fun BluetoothConnectionAlert(
    deviceName: String?,
    onReconnect: () -> Unit,
    onSwitchToIntegrated: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnClickOutside = false),
        icon = {
            Icon(
                imageVector = Icons.Default.BluetoothDisabled,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = { Text("Conexión perdida") },
        text = {
            Text(
                if (deviceName != null)
                    "Se ha perdido la conexión con la linterna externa \"$deviceName\"."
                else
                    "Se ha perdido la conexión con la linterna externa."
            )
        },
        confirmButton = {
            TextButton(onClick = onReconnect) {
                Text("Reconectar")
            }
        },
        dismissButton = {
            TextButton(onClick = onSwitchToIntegrated) {
                Text("Cambiar a integrada")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun BluetoothConnectionAlertPreview() {
    MobileLanternTheme {
        BluetoothConnectionAlert(
            deviceName = "ESP32_LANTERN_01",
            onReconnect = {},
            onSwitchToIntegrated = {},
            onDismiss = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BluetoothConnectionAlertNoNamePreview() {
    MobileLanternTheme {
        BluetoothConnectionAlert(
            deviceName = null,
            onReconnect = {},
            onSwitchToIntegrated = {},
            onDismiss = {}
        )
    }
}
