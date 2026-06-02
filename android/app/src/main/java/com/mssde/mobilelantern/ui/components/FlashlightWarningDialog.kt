package com.mssde.mobilelantern.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mssde.mobilelantern.ui.theme.MobileLanternTheme

@Composable
fun FlashlightWarningDialog(
    isFlashlightOn: Boolean
) {
    if (isFlashlightOn) {
        AlertDialog(
            onDismissRequest = { },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("🔦", style = MaterialTheme.typography.titleLarge)
                    Text("Linterna encendida")
                }
            },
            text = {
                Text(
                    "La linterna está encendida. Por favor, apágala para un correcto funcionamiento de la aplicación.",
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { },
                    enabled = false
                ) {
                    Text("Esperando...")
                }
            }
        )
    }
}

@Preview(showBackground = true, name = "Flashlight Warning Dialog")
@Composable
fun FlashlightWarningDialogPreview() {
    MobileLanternTheme {
        FlashlightWarningDialog(isFlashlightOn = true)
    }
}
