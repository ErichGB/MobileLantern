package com.mssde.mobilelantern.ui.components.bluetooth

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.mssde.mobilelantern.ui.theme.MobileLanternTheme

@Composable
fun BluetoothDisabledDialog(
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Filled.Bluetooth,
                contentDescription = "Bluetooth",
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(text = "Bluetooth desactivado")
        },
        text = {
            Text(
                text = "Para conectar dispositivos necesitas activar el Bluetooth. ¿Deseas ir a la configuración?",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(onClick = onOpenSettings) {
                Text("Ir a configuración")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Regresar")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewBluetoothDisabledDialog() {
    MobileLanternTheme {
        BluetoothDisabledDialog(
            onDismiss = {},
            onOpenSettings = {}
        )
    }
}

