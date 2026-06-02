package com.mssde.mobilelantern.ui.components.bluetooth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mssde.mobilelantern.ui.theme.MobileLanternTheme
import com.mssde.mobilelantern.viewmodel.BluetoothConnectionState

@Composable
fun ConnectionStatusCard(
    connectionState: BluetoothConnectionState,
    onDisconnectClick: () -> Unit
) {
    val titleName = connectionState.deviceName ?: "Linterna"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = MaterialTheme.shapes.extraLarge,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "\uD83D\uDEA8",
                fontSize = 28.sp
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = titleName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                when {
                    connectionState.isPairing -> {
                        Text(
                            text = "Emparejando...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    connectionState.isConnecting -> {
                        Text(
                            text = "Conectando...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    connectionState.isConnected -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Card(
                                    modifier = Modifier.size(8.dp),
                                    shape = CircleShape,
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.tertiary
                                    )
                                ) {}
                            }
                            Text(
                                text = "Conectada",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            when {
                connectionState.isPairing || connectionState.isConnecting -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                }
                connectionState.isConnected -> {
                    Button(
                        onClick = onDisconnectClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text("Desconectar")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewConnectionStatusCardConnecting() {
    MobileLanternTheme {
        ConnectionStatusCard(
            connectionState = BluetoothConnectionState(
                isConnected = false,
                isConnecting = true,
                isPairing = false,
                deviceName = "ESP32_LANTERN_01"
            ),
            onDisconnectClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewConnectionStatusCardPairing() {
    MobileLanternTheme {
        ConnectionStatusCard(
            connectionState = BluetoothConnectionState(
                isConnected = false,
                isConnecting = false,
                isPairing = true,
                deviceName = "ESP32_LANTERN_01",
                deviceMac = "30:C6:F7:1F:D8:CA"
            ),
            onDisconnectClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewConnectionStatusCardConnected() {
    MobileLanternTheme {
        ConnectionStatusCard(
            connectionState = BluetoothConnectionState(
                isConnected = true,
                isConnecting = false,
                isPairing = false,
                deviceName = "ESP32_LANTERN_01"
            ),
            onDisconnectClick = {}
        )
    }
}
