package com.mssde.mobilelantern.viewmodel

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.InputStream
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import kotlin.coroutines.coroutineContext
import javax.inject.Singleton

data class ScannedBluetoothDevice(
    val displayName: String,
    val address: String
)

data class BluetoothScanUiState(
    val isScanning: Boolean = false,
    val devices: List<ScannedBluetoothDevice> = emptyList()
)

data class BluetoothConnectionState(
    val isConnected: Boolean = false,
    val isConnecting: Boolean = false,
    val isPairing: Boolean = false,
    val deviceName: String? = null,
    val deviceMac: String? = null,
    val firmwareVersion: String? = null,
    val error: String? = null,
    val history: List<BluetoothHistoryItem> = emptyList()
)

data class BluetoothHistoryItem(
    val deviceName: String,
    val timestamp: Long,
    val status: ConnectionStatus
)

enum class ConnectionStatus {
    CONNECTED, DISCONNECTED, FAILED, PAIRING, PAIRED
}

@Singleton
class BluetoothViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context
) {

    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    private val _connectionState = MutableStateFlow(BluetoothConnectionState())
    val connectionState: StateFlow<BluetoothConnectionState> = _connectionState.asStateFlow()

    private val _scanUiState = MutableStateFlow(BluetoothScanUiState())
    val scanUiState: StateFlow<BluetoothScanUiState> = _scanUiState.asStateFlow()

    private var bluetoothSocket: BluetoothSocket? = null
    private val bluetoothAdapter: BluetoothAdapter? =
        (appContext.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter
    private val commandMutex = Mutex()
    private val connectionLostMutex = Mutex()
    private var connectionWatchJob: Job? = null
    private var aclDisconnectReceiver: BroadcastReceiver? = null
    private var discoveryReceiver: BroadcastReceiver? = null
    private var lanternScanSetupJob: Job? = null
    private val discoveredByAddress = LinkedHashMap<String, ScannedBluetoothDevice>()
    
    private val ESP32_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private companion object {
        private const val CMD_VERSION = "VERSION"
        private const val CMD_BUZZER_WELCOME = "BUZZER:TONE:WELCOME"
        private const val VERSION_READ_TIMEOUT_MS = 2_000L
        private const val PAIR_TIMEOUT_MS = 60_000L
        private const val CONNECT_DELAY_AFTER_BOND_MS = 1_000L
        private const val CONNECT_RETRY_DELAY_MS = 1_200L
        private const val CONNECT_ATTEMPTS = 2
        private val VERSION_OK_REGEX = Regex("^OK:VERSION:(.+)$")
    }

    private suspend fun writeCommandLine(socket: BluetoothSocket, command: String) {
        val commandWithNewLine = "$command\n"
        socket.outputStream?.write(commandWithNewLine.toByteArray())
        socket.outputStream?.flush()
        delay(50)
    }

    private suspend fun readFirmwareVersionLine(input: InputStream, timeoutMs: Long): String? {
        val pending = StringBuilder(128)
        val buf = ByteArray(256)
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            coroutineContext.ensureActive()
            val avail = try {
                input.available()
            } catch (_: Exception) {
                0
            }
            if (avail > 0) {
                val toRead = minOf(buf.size, avail)
                val n = input.read(buf, 0, toRead)
                if (n == -1) return null
                if (n > 0) {
                    pending.append(String(buf, 0, n, Charsets.UTF_8))
                    while (true) {
                        val nl = pending.indexOf('\n')
                        if (nl < 0) break
                        val line = pending.substring(0, nl).trim()
                        pending.delete(0, nl + 1)
                        val match = VERSION_OK_REGEX.matchEntire(line)
                        if (match != null) return match.groupValues[1].trim()
                    }
                }
            } else {
                delay(15)
            }
        }
        return null
    }

    private suspend fun runPostConnectHandshake() {
        commandMutex.withLock {
            val socket = bluetoothSocket
            if (socket == null || !socket.isConnected) return@withLock

            try {
                writeCommandLine(socket, CMD_VERSION)
                val version = readFirmwareVersionLine(socket.inputStream, VERSION_READ_TIMEOUT_MS)
                if (version != null) {
                    withContext(Dispatchers.Main) {
                        _connectionState.value = _connectionState.value.copy(firmwareVersion = version)
                    }
                } else {
                    Log.w("BluetoothViewModel", "Sin respuesta VERSION en ${VERSION_READ_TIMEOUT_MS}ms")
                }
                writeCommandLine(socket, CMD_BUZZER_WELCOME)
            } catch (e: IOException) {
                Log.e("BluetoothViewModel", "Handshake post-conexión: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    handleConnectionLost()
                }
            }
        }
    }

    private suspend fun prepareForConnectionAttempt() {
        connectionLostMutex.withLock {
            connectionWatchJob?.cancel()
            connectionWatchJob = null
            unregisterAclDisconnectReceiver()
            try {
                bluetoothSocket?.close()
            } catch (_: Exception) {
            }
            bluetoothSocket = null
        }
    }

    private fun unregisterAclDisconnectReceiver() {
        val receiver = aclDisconnectReceiver ?: return
        try {
            appContext.unregisterReceiver(receiver)
        } catch (_: Exception) {
        }
        aclDisconnectReceiver = null
    }

    @SuppressLint("MissingPermission")
    private fun registerAclDisconnectReceiver(expectedAddress: String) {
        unregisterAclDisconnectReceiver()
        val normalized = expectedAddress.uppercase()
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action != BluetoothDevice.ACTION_ACL_DISCONNECTED) return
                val device: BluetoothDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                }
                val addr = device?.address?.uppercase() ?: return
                if (addr != normalized) return
                viewModelScope.launch {
                    handleConnectionLost()
                }
            }
        }
        aclDisconnectReceiver = receiver
        val filter = IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            appContext.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            appContext.registerReceiver(receiver, filter)
        }
    }

    private fun startConnectionWatch() {
        connectionWatchJob?.cancel()
        val sock = bluetoothSocket ?: return
        connectionWatchJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val input = sock.inputStream
                val buf = ByteArray(256)
                while (isActive) {
                    val n = input.read(buf)
                    if (n == -1) break
                }
                if (!isActive) return@launch
                withContext(Dispatchers.Main) {
                    handleConnectionLost()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                if (isActive) {
                    withContext(Dispatchers.Main) {
                        handleConnectionLost()
                    }
                }
            }
        }
    }

    private suspend fun handleConnectionLost() {
        connectionLostMutex.withLock {
            if (!_connectionState.value.isConnected) return
            connectionWatchJob?.cancel()
            connectionWatchJob = null
            unregisterAclDisconnectReceiver()
            try {
                bluetoothSocket?.close()
            } catch (_: Exception) {
            }
            bluetoothSocket = null
            val name = _connectionState.value.deviceName
            if (name != null) {
                addToHistory(name, ConnectionStatus.DISCONNECTED)
            }
            _connectionState.value = _connectionState.value.copy(
                isConnected = false,
                isConnecting = false,
                isPairing = false,
                firmwareVersion = null,
                error = "Conexión perdida"
            )
            Log.w("BluetoothViewModel", "Conexión Bluetooth perdida")
        }
    }

    private fun onConnectedLinkReady(macAddress: String) {
        if (macAddress.isNotEmpty()) {
            registerAclDisconnectReceiver(macAddress)
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                runPostConnectHandshake()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("BluetoothViewModel", "Post-connect handshake", e)
            }
            val sock = bluetoothSocket
            if (sock != null && sock.isConnected) {
                startConnectionWatch()
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun connectToDevice(deviceName: String) {
        if (_connectionState.value.isConnecting) {
            Log.w("BluetoothViewModel", "Ya hay una conexión en proceso")
            return
        }

        viewModelScope.launch {
            prepareForConnectionAttempt()
            _connectionState.value = _connectionState.value.copy(
                isConnecting = true,
                error = null,
                firmwareVersion = null
            )

            try {
                withContext(Dispatchers.IO) {
                    val pairedDevices = bluetoothAdapter?.bondedDevices
                    val device = pairedDevices?.firstOrNull { it.name == deviceName }

                    if (device == null) {
                        throw IOException("Dispositivo no encontrado en dispositivos emparejados")
                    }

                    bluetoothSocket = device.createRfcommSocketToServiceRecord(ESP32_UUID)
                    bluetoothSocket?.connect()
                }

                @SuppressLint("MissingPermission")
                val mac = bluetoothSocket?.remoteDevice?.address.orEmpty()

                addToHistory(deviceName, ConnectionStatus.CONNECTED)
                _connectionState.value = _connectionState.value.copy(
                    isConnected = true,
                    isConnecting = false,
                    deviceName = deviceName,
                    deviceMac = mac.ifEmpty { null },
                    error = null
                )
                onConnectedLinkReady(mac)
                
                Log.d("BluetoothViewModel", "Conectado exitosamente a $deviceName")
            } catch (e: Exception) {
                Log.e("BluetoothViewModel", "Error al conectar", e)
                prepareForConnectionAttempt()
                addToHistory(deviceName, ConnectionStatus.FAILED)
                _connectionState.value = _connectionState.value.copy(
                    isConnected = false,
                    isConnecting = false,
                    firmwareVersion = null,
                    error = e.message ?: "Error al conectar"
                )
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun pairAndConnect(deviceName: String, macAddress: String) {
        if (_connectionState.value.isConnecting || _connectionState.value.isPairing) {
            Log.w("BluetoothViewModel", "Ya hay una operación en proceso")
            return
        }

        viewModelScope.launch {
            prepareForConnectionAttempt()
            _connectionState.value = _connectionState.value.copy(
                isPairing = true,
                isConnecting = false,
                error = null,
                deviceName = deviceName,
                deviceMac = macAddress,
                firmwareVersion = null
            )

            try {
                val device = withContext(Dispatchers.IO) {
                    bluetoothAdapter?.getRemoteDevice(macAddress)
                } ?: throw IOException("No se pudo obtener el dispositivo Bluetooth")

                val bondState = device.bondState

                if (bondState == BluetoothDevice.BOND_NONE) {
                    addToHistory(deviceName, ConnectionStatus.PAIRING)
                    Log.d("BluetoothViewModel", "Iniciando emparejamiento con $deviceName ($macAddress)")

                    val pairResult = withContext(Dispatchers.IO) {
                        device.createBond()
                    }

                    if (!pairResult) {
                        throw IOException("No se pudo iniciar el emparejamiento")
                    }

                    val paired = awaitBond(device, PAIR_TIMEOUT_MS)
                    if (!paired) {
                        throw IOException("Emparejamiento cancelado")
                    }

                    addToHistory(deviceName, ConnectionStatus.PAIRED)
                    Log.d("BluetoothViewModel", "Emparejamiento exitoso con $deviceName")
                } else if (bondState == BluetoothDevice.BOND_BONDING) {
                    addToHistory(deviceName, ConnectionStatus.PAIRING)
                    val paired = awaitBond(device, PAIR_TIMEOUT_MS)
                    if (!paired) {
                        throw IOException("Emparejamiento cancelado")
                    }
                    addToHistory(deviceName, ConnectionStatus.PAIRED)
                }

                _connectionState.value = _connectionState.value.copy(
                    isPairing = false,
                    isConnecting = true
                )

                connectWithRetry(device, CONNECT_ATTEMPTS)

                addToHistory(deviceName, ConnectionStatus.CONNECTED)
                _connectionState.value = _connectionState.value.copy(
                    isConnected = true,
                    isConnecting = false,
                    isPairing = false,
                    deviceName = deviceName,
                    deviceMac = macAddress,
                    error = null
                )
                onConnectedLinkReady(macAddress)
                
                Log.d("BluetoothViewModel", "Conectado exitosamente a $deviceName")
            } catch (e: Exception) {
                Log.e("BluetoothViewModel", "Error al emparejar/conectar", e)
                prepareForConnectionAttempt()
                addToHistory(deviceName, ConnectionStatus.FAILED)
                _connectionState.value = _connectionState.value.copy(
                    isConnected = false,
                    isConnecting = false,
                    isPairing = false,
                    firmwareVersion = null,
                    error = e.message ?: "Error al emparejar/conectar"
                )
            }
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun awaitBond(device: BluetoothDevice, timeoutMs: Long): Boolean {
        if (device.bondState == BluetoothDevice.BOND_BONDED) return true

        val address = device.address?.uppercase() ?: return false
        val result = CompletableDeferred<Boolean>()
        var hasBeenBonding = device.bondState == BluetoothDevice.BOND_BONDING
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action != BluetoothDevice.ACTION_BOND_STATE_CHANGED) return
                val changedDevice: BluetoothDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                }
                val changedAddress = changedDevice?.address?.uppercase() ?: return
                if (changedAddress != address) return

                val state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR)
                val previousState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR)
                when (state) {
                    BluetoothDevice.BOND_BONDED -> result.complete(true)
                    BluetoothDevice.BOND_BONDING -> hasBeenBonding = true
                    BluetoothDevice.BOND_NONE -> {
                        if (hasBeenBonding || previousState == BluetoothDevice.BOND_BONDING) {
                            result.complete(false)
                        }
                    }
                }
            }
        }

        val filter = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            appContext.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            @Suppress("DEPRECATION")
            appContext.registerReceiver(receiver, filter)
        }

        return try {
            withTimeoutOrNull(timeoutMs) {
                result.await()
            } ?: false
        } finally {
            try {
                appContext.unregisterReceiver(receiver)
            } catch (_: Exception) {
            }
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun connectWithRetry(device: BluetoothDevice, attempts: Int) {
        withContext(Dispatchers.IO) {
            bluetoothAdapter?.cancelDiscovery()
            delay(CONNECT_DELAY_AFTER_BOND_MS)

            var lastError: IOException? = null
            repeat(attempts) { attempt ->
                try {
                    bluetoothSocket = device.createRfcommSocketToServiceRecord(ESP32_UUID)
                    bluetoothSocket?.connect()
                    return@withContext
                } catch (e: IOException) {
                    lastError = e
                    try {
                        bluetoothSocket?.close()
                    } catch (_: Exception) {
                    }
                    bluetoothSocket = null
                    if (attempt < attempts - 1) {
                        delay(CONNECT_RETRY_DELAY_MS)
                    }
                }
            }

            throw lastError ?: IOException("No se pudo conectar al dispositivo")
        }
    }

    fun disconnect() {
        viewModelScope.launch {
            connectionLostMutex.withLock {
                connectionWatchJob?.cancel()
                connectionWatchJob = null
                unregisterAclDisconnectReceiver()
                val currentDeviceName = _connectionState.value.deviceName
                try {
                    bluetoothSocket?.close()
                } catch (_: Exception) {
                }
                bluetoothSocket = null
                if (currentDeviceName != null) {
                    addToHistory(currentDeviceName, ConnectionStatus.DISCONNECTED)
                }
                _connectionState.value = _connectionState.value.copy(
                    isConnected = false,
                    isConnecting = false,
                    deviceName = null,
                    firmwareVersion = null
                )
                Log.d("BluetoothViewModel", "Desconectado exitosamente")
            }
        }
    }

    fun sendCommand(command: String) {
        viewModelScope.launch(Dispatchers.IO) {
            commandMutex.withLock {
                try {
                    val socket = bluetoothSocket
                    if (socket == null || !socket.isConnected) {
                        Log.e("BluetoothViewModel", "Socket no conectado")
                        withContext(Dispatchers.Main) {
                            handleConnectionLost()
                        }
                        return@withLock
                    }

                    writeCommandLine(socket, command)
                    Log.d("BluetoothViewModel", "Comando enviado exitosamente: $command")
                } catch (e: IOException) {
                    Log.e("BluetoothViewModel", "Error al enviar comando: ${e.message}", e)
                    withContext(Dispatchers.Main) {
                        handleConnectionLost()
                    }
                }
            }
        }
    }

    private fun addToHistory(deviceName: String, status: ConnectionStatus) {
        val newItem = BluetoothHistoryItem(
            deviceName = deviceName,
            timestamp = System.currentTimeMillis(),
            status = status
        )
        
        _connectionState.value = _connectionState.value.copy(
            history = listOf(newItem) + _connectionState.value.history
        )
    }

    fun clearError() {
        _connectionState.value = _connectionState.value.copy(error = null)
    }

    private fun cancelLanternDiscoveryAndUnregister() {
        lanternScanSetupJob?.cancel()
        lanternScanSetupJob = null
        try {
            bluetoothAdapter?.cancelDiscovery()
        } catch (_: Exception) {
        }
        unregisterDiscoveryReceiver()
    }

    @SuppressLint("MissingPermission")
    fun startLanternScan() {
        val adapter = bluetoothAdapter ?: return
        if (!adapter.isEnabled) return

        cancelLanternDiscoveryAndUnregister()
        discoveredByAddress.clear()
        _scanUiState.value = BluetoothScanUiState(isScanning = true, devices = emptyList())

        lanternScanSetupJob = viewModelScope.launch {
            delay(320)
            if (!isActive) return@launch

            val receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    when (intent?.action) {
                        BluetoothDevice.ACTION_FOUND -> {
                            val device: BluetoothDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                            } else {
                                @Suppress("DEPRECATION")
                                intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                            }
                            if (device == null) return
                            val addr = device.address ?: return
                            val rawName = device.name?.trim()?.takeIf { it.isNotEmpty() }
                            val displayName = rawName ?: "Dispositivo (${addr.takeLast(8)})"
                            discoveredByAddress[addr] = ScannedBluetoothDevice(displayName, addr)
                            _scanUiState.value = _scanUiState.value.copy(
                                devices = discoveredByAddress.values.sortedBy { it.displayName.lowercase() }
                            )
                        }
                        BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                            _scanUiState.value = _scanUiState.value.copy(isScanning = false)
                            unregisterDiscoveryReceiver()
                        }
                    }
                }
            }
            discoveryReceiver = receiver
            val filter = IntentFilter().apply {
                addAction(BluetoothDevice.ACTION_FOUND)
                addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                appContext.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
            } else {
                @Suppress("DEPRECATION")
                appContext.registerReceiver(receiver, filter)
            }

            val started = try {
                adapter.startDiscovery()
            } catch (_: Exception) {
                false
            }
            if (!started) {
                _scanUiState.value = _scanUiState.value.copy(isScanning = false)
                unregisterDiscoveryReceiver()
            }
        }
    }

    fun stopLanternScan() {
        stopLanternScanInternal()
    }

    @SuppressLint("MissingPermission")
    private fun stopLanternScanInternal() {
        cancelLanternDiscoveryAndUnregister()
        _scanUiState.value = _scanUiState.value.copy(isScanning = false)
    }

    private fun unregisterDiscoveryReceiver() {
        val receiver = discoveryReceiver ?: return
        try {
            appContext.unregisterReceiver(receiver)
        } catch (_: Exception) {
        }
        discoveryReceiver = null
    }
}

