package com.mssde.mobilelantern

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mssde.mobilelantern.ui.components.bluetooth.BluetoothCommandTestPanel
import com.mssde.mobilelantern.ui.components.bluetooth.BluetoothDisabledDialog
import com.mssde.mobilelantern.ui.components.bluetooth.BluetoothHistoryList
import com.mssde.mobilelantern.ui.components.bluetooth.BluetoothLanternScanBottomSheet
import com.mssde.mobilelantern.ui.components.bluetooth.ConnectionStatusCard
import com.mssde.mobilelantern.ui.components.CommonTopBar
import com.mssde.mobilelantern.ui.components.StepCard
import com.mssde.mobilelantern.ui.components.bluetooth.QRScannerBottomSheet
import com.mssde.mobilelantern.hardware.HardwareController
import com.mssde.mobilelantern.ui.theme.MobileLanternTheme
import com.mssde.mobilelantern.viewmodel.BluetoothConnectionState
import com.mssde.mobilelantern.viewmodel.BluetoothHistoryItem
import com.mssde.mobilelantern.viewmodel.BluetoothViewModel
import com.mssde.mobilelantern.viewmodel.ConnectionStatus
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LanternActivity : ComponentActivity() {
    @Inject
    lateinit var bluetoothViewModel: BluetoothViewModel
    
    private var showBluetoothDialog by mutableStateOf(false)
    
    @Inject
    lateinit var hardwareController: HardwareController

    private val bluetoothStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action != BluetoothAdapter.ACTION_STATE_CHANGED) return
            val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
            when (state) {
                BluetoothAdapter.STATE_OFF,
                BluetoothAdapter.STATE_TURNING_OFF -> showBluetoothDialog = true
                BluetoothAdapter.STATE_ON -> showBluetoothDialog = false
            }
        }
    }
    
    private val bluetoothPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            syncBluetoothDialogVisibility()
            handleDeepLink(intent)
        } else {
            Toast.makeText(
                this,
                "Permisos de Bluetooth requeridos para conectar",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        requestBluetoothPermissions()
        
        setContent {
            var showScanSheet by remember { mutableStateOf(false) }
            val scanUiState by bluetoothViewModel.scanUiState.collectAsStateWithLifecycle()

            MobileLanternTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val connectionState by bluetoothViewModel.connectionState.collectAsStateWithLifecycle()

                    LaunchedEffect(connectionState.error, showScanSheet) {
                        connectionState.error?.let { error ->
                            if (!showScanSheet) {
                                Toast.makeText(
                                    this@LanternActivity,
                                    error,
                                    Toast.LENGTH_LONG
                                ).show()
                                bluetoothViewModel.clearError()
                            }
                        }
                    }

                    LaunchedEffect(connectionState.isConnected, showScanSheet) {
                        if (showScanSheet && connectionState.isConnected) {
                            showScanSheet = false
                            bluetoothViewModel.stopLanternScan()
                            bluetoothViewModel.clearError()
                        }
                    }

                    LanternScreen(
                        connectionState = connectionState,
                        onBackClick = {
                            finish()
                        },
                        onOpenLanternScan = {
                            if (isBluetoothEnabled()) {
                                showScanSheet = true
                                bluetoothViewModel.startLanternScan()
                            } else {
                                showBluetoothDialog = true
                            }
                        },
                        onConnectClick = { deviceName ->
                            if (isBluetoothEnabled()) {
                                bluetoothViewModel.connectToDevice(deviceName)
                            } else {
                                showBluetoothDialog = true
                            }
                        },
                        onDeepLinkConnect = { deviceName, macAddress ->
                            if (isBluetoothEnabled()) {
                                bluetoothViewModel.pairAndConnect(deviceName, macAddress)
                            } else {
                                showBluetoothDialog = true
                            }
                        },
                        onDisconnectClick = {
                            bluetoothViewModel.disconnect()
                        },
                        onSendCommand = { command ->
                            bluetoothViewModel.sendCommand(command)
                        }
                    )
                    
                    if (showBluetoothDialog) {
                        BluetoothDisabledDialog(
                            onDismiss = {
                                showBluetoothDialog = false
                                finish()
                            },
                            onOpenSettings = {
                                showBluetoothDialog = false
                                openBluetoothSettings()
                            }
                        )
                    }

                    if (showScanSheet) {
                        BluetoothLanternScanBottomSheet(
                            scanUiState = scanUiState,
                            connectionState = connectionState,
                            onDismiss = {
                                showScanSheet = false
                                bluetoothViewModel.stopLanternScan()
                                bluetoothViewModel.clearError()
                            },
                            onScanAgain = { bluetoothViewModel.startLanternScan() },
                            onDismissError = { bluetoothViewModel.clearError() },
                            onDeviceSelected = { name, mac ->
                                bluetoothViewModel.pairAndConnect(name, mac)
                            }
                        )
                    }
                }
            }
        }
        
        handleDeepLink(intent)
    }

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(bluetoothStateReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            registerReceiver(bluetoothStateReceiver, filter)
        }
        syncBluetoothDialogVisibility()
    }

    override fun onStop() {
        try {
            unregisterReceiver(bluetoothStateReceiver)
        } catch (_: IllegalArgumentException) {
        }
        super.onStop()
    }
    
    override fun onResume() {
        super.onResume()
        syncBluetoothDialogVisibility()
    }
    
    override fun onDestroy() {
        super.onDestroy()
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLink(intent)
    }
    
    private fun requestBluetoothPermissions() {
        val permissionsToRequest = mutableListOf<String>()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) 
                != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_CONNECT)
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) 
                != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_SCAN)
            }
        }
        
        // ACCESS_FINE_LOCATION es necesario para BluetoothAdapter.startDiscovery()
        // en todas las versiones de Android (6-11 directamente, 12+ si BLUETOOTH_SCAN
        // no tiene neverForLocation)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (permissionsToRequest.isNotEmpty()) {
            bluetoothPermissionsLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }
    
    private fun handleDeepLink(intent: Intent?) {
        val data: Uri? = intent?.data
        if (data != null && data.scheme == "mobilelantern" && data.host == "connect") {
            val deviceName = data.getQueryParameter("device")
            val macAddress = data.getQueryParameter("mac")
            
            if (deviceName != null && macAddress != null) {
                if (isBluetoothEnabled()) {
                    bluetoothViewModel.pairAndConnect(deviceName, macAddress)
                    Toast.makeText(
                        this,
                        "Conectando a $deviceName...",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    showBluetoothDialog = true
                }
            } else {
                Toast.makeText(
                    this,
                    "Enlace inválido: falta device o mac",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    
    private fun isBluetoothEnabled(): Boolean {
        val bluetoothManager = getSystemService(BluetoothManager::class.java)
        val bluetoothAdapter = bluetoothManager?.adapter
        return bluetoothAdapter?.isEnabled == true
    }
    
    private fun syncBluetoothDialogVisibility() {
        showBluetoothDialog = !isBluetoothEnabled()
    }
    
    private fun openBluetoothSettings() {
        try {
            val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "No se pudo abrir la configuración de Bluetooth",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanternScreen(
    connectionState: BluetoothConnectionState,
    onBackClick: () -> Unit,
    onOpenLanternScan: () -> Unit,
    onConnectClick: (String) -> Unit,
    onDeepLinkConnect: (String, String) -> Unit,
    onDisconnectClick: () -> Unit,
    onSendCommand: (String) -> Unit
) {
    var showQRScanner by remember { mutableStateOf(false) }
    var showHistorySheet by remember { mutableStateOf(false) }
    var showCommandsSheet by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    val showStatusCard = connectionState.isConnected ||
        connectionState.isConnecting ||
        connectionState.isPairing

    Scaffold(
        topBar = {
            CommonTopBar(
                title = "Linterna externa",
                onBackClick = onBackClick,
                showConfirmDialog = false,
                showFlashIndicator = false,
                onReconnectBluetooth = onOpenLanternScan,
                trailingActions = {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "Más opciones"
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Historial de conexiones") },
                                onClick = {
                                    menuExpanded = false
                                    showHistorySheet = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Panel de diagnóstico") },
                                onClick = {
                                    menuExpanded = false
                                    showCommandsSheet = true
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (showStatusCard) {
                ConnectionStatusCard(
                    connectionState = connectionState,
                    onDisconnectClick = onDisconnectClick
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onOpenLanternScan,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Buscar")
                    }
                    OutlinedButton(
                        onClick = { showQRScanner = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Escanear QR")
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Cómo usar tu linterna",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Tu linterna muestra al profesor en qué fase estás (colaboración, necesidad de ayuda, etc.). La app controla luz y avisos del dispositivo.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )

                StepCard(
                    stepNumber = 1,
                    title = "Conecta tu linterna",
                    description = "Escanea el QR único del dispositivo o usa «Buscar» si ya estaba emparejada.",
                    leadingIcon = Icons.Filled.Bluetooth
                )
                StepCard(
                    stepNumber = 2,
                    title = "Coloca la linterna en tu mesa",
                    description = "🚨 Déjala en un lugar visible para que el profesor pueda ver la señal."
                )
                StepCard(
                    stepNumber = 3,
                    title = "Luz verde: colaboración",
                    description = "Verde = estoy colaborando con mis compañeros.",
                    accentColor = Color(0xFF2E7D32)
                )
                StepCard(
                    stepNumber = 4,
                    title = "Luz roja: ayuda",
                    description = "Rojo = necesito más ayuda. Puede parpadear o sonar.",
                    accentColor = MaterialTheme.colorScheme.error
                )

                Text(
                    text = "Ilustraciones",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 8.dp)
                )
                LanternIllustrationPlaceholder(
                    label = "Espacio para imagen: dispositivo en la mesa"
                )
                LanternIllustrationPlaceholder(
                    label = "Espacio para imagen: vasos de colores y linterna"
                )
            }
        }
    }

    if (showQRScanner) {
        QRScannerBottomSheet(
            onDismiss = { showQRScanner = false },
            onQRScanned = { scannedContent ->
                showQRScanner = false

                if (scannedContent.startsWith("mobilelantern://connect")) {
                    try {
                        val uri = Uri.parse(scannedContent)
                        val deviceName = uri.getQueryParameter("device")
                        val macAddress = uri.getQueryParameter("mac")

                        if (deviceName != null && macAddress != null) {
                            onDeepLinkConnect(deviceName, macAddress)
                        } else {
                            onConnectClick(scannedContent)
                        }
                    } catch (e: Exception) {
                        onConnectClick(scannedContent)
                    }
                } else {
                    onConnectClick(scannedContent)
                }
            }
        )
    }

    if (showHistorySheet) {
        val historySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showHistorySheet = false },
            sheetState = historySheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Historial de conexiones",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Últimos eventos de emparejamiento y conexión con tu MobileLantern.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                BluetoothHistoryList(
                    history = connectionState.history,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 200.dp, max = 480.dp)
                )
            }
        }
    }

    if (showCommandsSheet) {
        val commandsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showCommandsSheet = false },
            sheetState = commandsSheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Panel de diagnóstico",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Comandos de prueba hacia la linterna conectada. Uso solo para comprobar el hardware.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Dispositivo",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (connectionState.isConnected) {
                    DeviceInfoRow(
                        label = connectionState.deviceName ?: "Linterna",
                        value = "Firmware ${connectionState.firmwareVersion ?: "No disponible"}"
                    )
                } else {
                    Text(
                        text = "Conecta una linterna para ver los detalles del dispositivo.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                BluetoothCommandTestPanel(
                    isConnected = connectionState.isConnected,
                    onSendCommand = onSendCommand,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 280.dp, max = 560.dp)
                )
            }
        }
    }
}

@Composable
private fun DeviceInfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun LanternIllustrationPlaceholder(
    label: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerHighest
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 1100, name = "Desconectado")
@Composable
fun PreviewLanternScreenDisconnected() {
    MobileLanternTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            LanternScreen(
                connectionState = BluetoothConnectionState(),
                onBackClick = {},
                onOpenLanternScan = {},
                onConnectClick = {},
                onDeepLinkConnect = { _, _ -> },
                onDisconnectClick = {},
                onSendCommand = {}
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 1100, name = "Conectando")
@Composable
fun PreviewLanternScreenConnecting() {
    MobileLanternTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            LanternScreen(
                connectionState = BluetoothConnectionState(
                    isConnected = false,
                    isConnecting = true,
                    isPairing = false
                ),
                onBackClick = {},
                onOpenLanternScan = {},
                onConnectClick = {},
                onDeepLinkConnect = { _, _ -> },
                onDisconnectClick = {},
                onSendCommand = {}
            )
        }
    }
}

@Preview(showBackground = true, heightDp = 1100, name = "Conectado")
@Composable
fun PreviewLanternScreenConnected() {
    val sampleHistory = listOf(
        BluetoothHistoryItem("ESP32_LANTERN_01", 1_700_000_000_000L, ConnectionStatus.CONNECTED),
        BluetoothHistoryItem("ESP32_LANTERN_02", 1_699_999_400_000L, ConnectionStatus.DISCONNECTED)
    )
    MobileLanternTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            LanternScreen(
                connectionState = BluetoothConnectionState(
                    isConnected = true,
                    deviceName = "ESP32_LANTERN_01",
                    deviceMac = "30:C6:F7:1F:D8:CA",
                    firmwareVersion = "1.0.0",
                    history = sampleHistory
                ),
                onBackClick = {},
                onOpenLanternScan = {},
                onConnectClick = {},
                onDeepLinkConnect = { _, _ -> },
                onDisconnectClick = {},
                onSendCommand = {}
            )
        }
    }
}
