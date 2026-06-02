package com.mssde.mobilelantern.ui.components.bluetooth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mssde.mobilelantern.viewmodel.BluetoothConnectionState
import com.mssde.mobilelantern.ui.theme.MobileLanternTheme
import com.mssde.mobilelantern.viewmodel.BluetoothScanUiState
import com.mssde.mobilelantern.viewmodel.ScannedBluetoothDevice

private fun ScannedBluetoothDevice.isMobileLanternBluetoothName(): Boolean =
    displayName.startsWith("ML-")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BluetoothLanternScanBottomSheet(
    scanUiState: BluetoothScanUiState,
    connectionState: BluetoothConnectionState,
    onDismiss: () -> Unit,
    onScanAgain: () -> Unit,
    onDismissError: () -> Unit,
    onDeviceSelected: (displayName: String, address: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        BluetoothLanternScanSheetContent(
            scanUiState = scanUiState,
            connectionState = connectionState,
            onScanAgain = onScanAgain,
            onDismissError = onDismissError,
            onDeviceSelected = onDeviceSelected,
            modifier = Modifier.padding(bottom = 24.dp)
        )
    }
}

@Composable
fun BluetoothLanternScanSheetContent(
    scanUiState: BluetoothScanUiState,
    connectionState: BluetoothConnectionState,
    onScanAgain: () -> Unit,
    onDismissError: () -> Unit,
    onDeviceSelected: (displayName: String, address: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val devicesToShow = remember(scanUiState.devices) {
        scanUiState.devices.filter { it.isMobileLanternBluetoothName() }
    }
    val inProgress = connectionState.isPairing || connectionState.isConnecting
    val selectedAddress = connectionState.deviceMac
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Linternas cercanas",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Buscando linternas bluetooth cercanas.\nElige una para emparejar y conectar.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onScanAgain,
            enabled = !scanUiState.isScanning && !inProgress,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (scanUiState.isScanning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Escaneando…")
                } else {
                    Text("Escanear de nuevo")
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        val canRetryInline = !inProgress &&
            !connectionState.deviceName.isNullOrBlank() &&
            !connectionState.deviceMac.isNullOrBlank()
        if (!inProgress && connectionState.error != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = connectionState.error,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f)
                )
                if (canRetryInline) {
                    TextButton(
                        onClick = {
                            onDeviceSelected(
                                connectionState.deviceName.orEmpty(),
                                connectionState.deviceMac.orEmpty()
                            )
                        }
                    ) {
                        Text("Reintentar")
                    }
                }
                TextButton(onClick = onDismissError) {
                    Text("Cerrar")
                }
            }
        }

        if (devicesToShow.isEmpty() && !scanUiState.isScanning) {
            Text(
                text = "Aún no hay resultados. Pulsa «Escanear de nuevo» o acerca la linterna.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
        ) {
            items(devicesToShow) { device ->
                val isSelected = selectedAddress != null && device.address.equals(selectedAddress, ignoreCase = true)
                ListItem(
                    headlineContent = { Text(device.displayName) },
                    supportingContent = {
                        if (isSelected && inProgress) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(if (connectionState.isPairing) "Emparejando…" else "Conectando…")
                            }
                        } else {
                            Text(device.address)
                        }
                    },
                    modifier = Modifier.clickable(enabled = !inProgress) {
                        onDeviceSelected(device.displayName, device.address)
                    }
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 480)
@Composable
private fun PreviewBluetoothLanternScanSheetContent() {
    MobileLanternTheme {
        BluetoothLanternScanSheetContent(
            scanUiState = BluetoothScanUiState(
                isScanning = false,
                devices = listOf(
                    ScannedBluetoothDevice("ML-A1B2", "30:C6:F7:1F:D8:CA"),
                    ScannedBluetoothDevice("ML-Aula101", "AA:BB:CC:DD:EE:FF"),
                    ScannedBluetoothDevice("Dispositivo (D8:CA:12)", "11:22:33:44:55:66")
                )
            ),
            connectionState = BluetoothConnectionState(),
            onScanAgain = {},
            onDismissError = {},
            onDeviceSelected = { _, _ -> }
        )
    }
}

@Preview(showBackground = true, heightDp = 480)
@Composable
private fun PreviewBluetoothLanternScanSheetContentPairing() {
    MobileLanternTheme {
        BluetoothLanternScanSheetContent(
            scanUiState = BluetoothScanUiState(
                isScanning = false,
                devices = listOf(
                    ScannedBluetoothDevice("ML-A1B2", "30:C6:F7:1F:D8:CA"),
                    ScannedBluetoothDevice("ML-Aula101", "AA:BB:CC:DD:EE:FF")
                )
            ),
            connectionState = BluetoothConnectionState(
                isPairing = true,
                deviceName = "ML-A1B2",
                deviceMac = "30:C6:F7:1F:D8:CA"
            ),
            onScanAgain = {},
            onDismissError = {},
            onDeviceSelected = { _, _ -> }
        )
    }
}

@Preview(showBackground = true, heightDp = 480)
@Composable
private fun PreviewBluetoothLanternScanSheetContentConnecting() {
    MobileLanternTheme {
        BluetoothLanternScanSheetContent(
            scanUiState = BluetoothScanUiState(
                isScanning = false,
                devices = listOf(
                    ScannedBluetoothDevice("ML-A1B2", "30:C6:F7:1F:D8:CA"),
                    ScannedBluetoothDevice("ML-Aula101", "AA:BB:CC:DD:EE:FF")
                )
            ),
            connectionState = BluetoothConnectionState(
                isConnecting = true,
                deviceName = "ML-A1B2",
                deviceMac = "30:C6:F7:1F:D8:CA"
            ),
            onScanAgain = {},
            onDismissError = {},
            onDeviceSelected = { _, _ -> }
        )
    }
}

@Preview(showBackground = true, heightDp = 480)
@Composable
private fun PreviewBluetoothLanternScanSheetContentError() {
    MobileLanternTheme {
        BluetoothLanternScanSheetContent(
            scanUiState = BluetoothScanUiState(
                isScanning = false,
                devices = listOf(
                    ScannedBluetoothDevice("ML-A1B2", "30:C6:F7:1F:D8:CA"),
                    ScannedBluetoothDevice("ML-Aula101", "AA:BB:CC:DD:EE:FF")
                )
            ),
            connectionState = BluetoothConnectionState(
                deviceName = "ML-A1B2",
                deviceMac = "30:C6:F7:1F:D8:CA",
                error = "No se pudo conectar al dispositivo"
            ),
            onScanAgain = {},
            onDismissError = {},
            onDeviceSelected = { _, _ -> }
        )
    }
}
