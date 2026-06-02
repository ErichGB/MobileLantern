package com.mssde.mobilelantern

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mssde.mobilelantern.domain.model.UiState
import com.mssde.mobilelantern.data.local.LanternPreferenceManager
import com.mssde.mobilelantern.hardware.HardwareController
import com.mssde.mobilelantern.ui.components.CommonTopBar
import com.mssde.mobilelantern.ui.components.FlashlightWarningDialog
import com.mssde.mobilelantern.ui.components.BluetoothLanternAvailability
import com.mssde.mobilelantern.ui.components.LanternSelector
import com.mssde.mobilelantern.ui.components.LanternType
import com.mssde.mobilelantern.ui.components.rememberBluetoothLanternAvailability
import com.mssde.mobilelantern.ui.components.rememberBluetoothConnectionState
import com.mssde.mobilelantern.ui.components.checkin.CheckInBottomSheet
import com.mssde.mobilelantern.ui.theme.MobileLanternTheme
import com.mssde.mobilelantern.ui.welcome.WelcomeSessionState
import com.mssde.mobilelantern.ui.welcome.primaryButtonEnabled
import com.mssde.mobilelantern.ui.welcome.primaryButtonLabel
import com.mssde.mobilelantern.viewmodel.AuthViewModel
import com.mssde.mobilelantern.viewmodel.WelcomeViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@AndroidEntryPoint
class WelcomeActivity : ComponentActivity() {

    private val welcomeViewModel: WelcomeViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

    @Inject
    lateinit var hardwareController: HardwareController

    @Inject
    lateinit var lanternPreferenceManager: LanternPreferenceManager

    private val pendingCheckInDeepLink = MutableStateFlow<Uri?>(null)

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(
                this,
                "Permiso de cámara necesario para usar la linterna",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleDeepLink(intent)

        setContent {
            val lifecycleOwner = LocalLifecycleOwner.current
            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        welcomeViewModel.refreshSession()
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
            }

            val sessionState by welcomeViewModel.sessionState.collectAsStateWithLifecycle()
            val authState by authViewModel.uiState.collectAsStateWithLifecycle()
            val systemFlashlightState by hardwareController.systemFlashlightState.collectAsState()
            val savedLanternType by lanternPreferenceManager.selectedLanternType.collectAsState()
            val deepLinkUri by pendingCheckInDeepLink.collectAsStateWithLifecycle()
            var showCheckInSheet by remember { mutableStateOf(false) }

            LaunchedEffect(deepLinkUri) {
                val uri = deepLinkUri ?: return@LaunchedEffect
                authViewModel.loginWithDeepLink(uri)
                showCheckInSheet = true
                pendingCheckInDeepLink.value = null
            }

            LaunchedEffect(authState, showCheckInSheet, savedLanternType) {
                if (authState is UiState.Success && showCheckInSheet) {
                    if (savedLanternType != LanternType.NONE) {
                        startActivity(Intent(this@WelcomeActivity, QuestionActivity::class.java))
                        finish()
                    } else {
                        showCheckInSheet = false
                    }
                }
            }

            MobileLanternTheme {
                WelcomeScreen(
                    sessionState = sessionState,
                    sessionExpired = welcomeViewModel.sessionExpired,
                    systemFlashlightOn = systemFlashlightState,
                    initialSelectedLanternType = savedLanternType,
                    onInvalidateExternalLanternSelection = {
                        lanternPreferenceManager.saveSelectedType(LanternType.NONE)
                    },
                    onSelectIntegrated = {
                        lanternPreferenceManager.saveSelectedType(LanternType.INTEGRATED)
                        ensureCameraPermission { }
                    },
                    onNavigateIntegrated = {
                        startActivity(Intent(this, IntegratedLanternActivity::class.java))
                    },
                    onSelectExternal = {
                        lanternPreferenceManager.saveSelectedType(LanternType.EXTERNAL)
                    },
                    onNavigateExternal = {
                        startActivity(Intent(this, LanternActivity::class.java))
                    },
                    onStartClick = {
                        showCheckInSheet = true
                    },
                    onContinueClick = {
                        welcomeViewModel.onContinueRequested {
                            startActivity(Intent(this, QuestionActivity::class.java))
                            finish()
                        }
                    }
                )
                if (showCheckInSheet) {
                    CheckInBottomSheet(
                        authState = authState,
                        onDismiss = {
                            showCheckInSheet = false
                            authViewModel.resetState()
                        },
                        onQRScanned = { authViewModel.loginWithQRContent(it) },
                        onSecretLogin = { authViewModel.login() }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleDeepLink(intent)
    }

    private fun ensureCameraPermission(onGranted: () -> Unit) {
        val granted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            onGranted()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun handleDeepLink(intent: Intent?) {
        val uri = intent?.data ?: return
        if (uri.scheme == "mobilelantern" && uri.host == "checkin") {
            pendingCheckInDeepLink.value = uri
        }
    }
}

@Composable
fun WelcomeScreen(
    sessionState: WelcomeSessionState = WelcomeSessionState.NoCredentials,
    sessionExpired: Flow<Unit>? = null,
    systemFlashlightOn: Boolean = false,
    initialSelectedLanternType: LanternType = LanternType.NONE,
    bluetoothAvailabilityOverride: BluetoothLanternAvailability? = null,
    onInvalidateExternalLanternSelection: () -> Unit = {},
    onSelectIntegrated: () -> Unit,
    onNavigateIntegrated: () -> Unit,
    onSelectExternal: () -> Unit,
    onNavigateExternal: () -> Unit,
    onStartClick: () -> Unit,
    onContinueClick: () -> Unit = {}
) {
    var selectedType by remember(initialSelectedLanternType) {
        mutableStateOf(initialSelectedLanternType)
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val defaultExpiredFlow = remember { emptyFlow<Unit>() }
    val expiredFlow = sessionExpired ?: defaultExpiredFlow

    LaunchedEffect(expiredFlow) {
        expiredFlow.collect {
            snackbarHostState.showSnackbar("La sesión ha expirado")
        }
    }

    val btAvailability =
        bluetoothAvailabilityOverride ?: rememberBluetoothLanternAvailability()
    val bluetoothConnectionState = rememberBluetoothConnectionState()
    val isBtLanternConnected = bluetoothConnectionState.isConnected
    var justConnectedDeviceName by remember { mutableStateOf<String?>(null) }
    var lastAutoSelectedDeviceMac by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(btAvailability, selectedType, isBtLanternConnected) {
        if (selectedType != LanternType.EXTERNAL) return@LaunchedEffect
        val shouldClearExternal = when (btAvailability) {
            BluetoothLanternAvailability.AVAILABLE -> !isBtLanternConnected
            BluetoothLanternAvailability.NO_HARDWARE,
            BluetoothLanternAvailability.DISABLED -> true
        }
        if (shouldClearExternal) {
            selectedType = LanternType.NONE
            onInvalidateExternalLanternSelection()
        }
    }

    LaunchedEffect(btAvailability, selectedType, isBtLanternConnected, bluetoothConnectionState.deviceMac) {
        if (!isBtLanternConnected) {
            lastAutoSelectedDeviceMac = null
            return@LaunchedEffect
        }
        if (btAvailability != BluetoothLanternAvailability.AVAILABLE) return@LaunchedEffect
        if (selectedType != LanternType.NONE) return@LaunchedEffect
        val deviceMac = bluetoothConnectionState.deviceMac ?: return@LaunchedEffect
        if (deviceMac == lastAutoSelectedDeviceMac) return@LaunchedEffect

        selectedType = LanternType.EXTERNAL
        onSelectExternal()
        justConnectedDeviceName = bluetoothConnectionState.deviceName ?: "externa"
        lastAutoSelectedDeviceMac = deviceMac
    }

    LaunchedEffect(justConnectedDeviceName) {
        val deviceName = justConnectedDeviceName ?: return@LaunchedEffect
        snackbarHostState.showSnackbar("Linterna $deviceName conectada")
        justConnectedDeviceName = null
    }

    val hasLanternSelected = selectedType != LanternType.NONE

    if (selectedType == LanternType.INTEGRATED && systemFlashlightOn) {
        FlashlightWarningDialog(isFlashlightOn = systemFlashlightOn)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CommonTopBar(
                title = "Mobile Lantern",
                onBackClick = { },
                showBackButton = false,
                showMenuButton = false,
                showFlashIndicator = false
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Aplicación para orquestación de turnos de clase y gestión racional de IA generativa para la resolución de dudas.\n\n" +
                        "Esta aplicación podría utiliza la linterna, filtros de colores, y alertas acústicas para priorizar los turnos de pregunta.\n\n" +
                        "Selecciona una opción de linterna para saber más sobre su uso",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            LanternSelector(
                selectedType = selectedType,
                onSelectIntegrated = {
                    selectedType = LanternType.INTEGRATED
                    onSelectIntegrated()
                },
                onNavigateIntegrated = onNavigateIntegrated,
                onSelectExternal = {
                    selectedType = LanternType.EXTERNAL
                    onSelectExternal()
                },
                onNavigateExternal = onNavigateExternal,
                bluetoothLanternAvailability = btAvailability,
                highlightJustConnected = justConnectedDeviceName != null,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            val primaryActionLabel = sessionState.primaryButtonLabel()
            val primaryEnabled = sessionState.primaryButtonEnabled(hasLanternSelected)
            val onPrimaryAction = {
                when (sessionState) {
                    WelcomeSessionState.NoCredentials,
                    WelcomeSessionState.Unauthenticated -> onStartClick()
                    WelcomeSessionState.Ready,
                    WelcomeSessionState.ValidationUnavailable -> onContinueClick()
                    WelcomeSessionState.Validating -> Unit
                }
            }
            OutlinedButton(
                onClick = onPrimaryAction,
                enabled = primaryEnabled,
                modifier = Modifier
                    .height(48.dp)
                    .fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Text(
                    text = primaryActionLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (sessionState == WelcomeSessionState.ValidationUnavailable) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "No se pudo comprobar la sesión",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "En colaboración con",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_upm),
                    contentDescription = "Universidad Politécnica de Madrid",
                    modifier = Modifier.height(70.dp),
                    contentScale = ContentScale.Fit
                )
                Image(
                    painter = painterResource(id = R.drawable.logo_upf),
                    contentDescription = "Universitat Pompeu Fabra",
                    modifier = Modifier.height(44.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Estado inicial")
@Composable
fun WelcomeScreenPreview() {
    MobileLanternTheme {
        WelcomeScreen(
            sessionState = WelcomeSessionState.NoCredentials,
            onInvalidateExternalLanternSelection = {},
            onSelectIntegrated = {},
            onNavigateIntegrated = {},
            onSelectExternal = {},
            onNavigateExternal = {},
            onStartClick = {},
            onContinueClick = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Sesión lista")
@Composable
fun WelcomeScreenWithSessionPreview() {
    MobileLanternTheme {
        WelcomeScreen(
            sessionState = WelcomeSessionState.Ready,
            onInvalidateExternalLanternSelection = {},
            onSelectIntegrated = {},
            onNavigateIntegrated = {},
            onSelectExternal = {},
            onNavigateExternal = {},
            onStartClick = {},
            onContinueClick = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Comprobando sesión")
@Composable
fun WelcomeScreenValidatingPreview() {
    MobileLanternTheme {
        WelcomeScreen(
            sessionState = WelcomeSessionState.Validating,
            initialSelectedLanternType = LanternType.INTEGRATED,
            onInvalidateExternalLanternSelection = {},
            onSelectIntegrated = {},
            onNavigateIntegrated = {},
            onSelectExternal = {},
            onNavigateExternal = {},
            onStartClick = {},
            onContinueClick = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Validación no disponible")
@Composable
fun WelcomeScreenValidationUnavailablePreview() {
    MobileLanternTheme {
        WelcomeScreen(
            sessionState = WelcomeSessionState.ValidationUnavailable,
            initialSelectedLanternType = LanternType.INTEGRATED,
            onInvalidateExternalLanternSelection = {},
            onSelectIntegrated = {},
            onNavigateIntegrated = {},
            onSelectExternal = {},
            onNavigateExternal = {},
            onStartClick = {},
            onContinueClick = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Modal linterna encendida")
@Composable
fun WelcomeScreenWithFlashlightWarningPreview() {
    MobileLanternTheme {
        WelcomeScreen(
            sessionState = WelcomeSessionState.NoCredentials,
            systemFlashlightOn = true,
            initialSelectedLanternType = LanternType.INTEGRATED,
            onInvalidateExternalLanternSelection = {},
            onSelectIntegrated = {},
            onNavigateIntegrated = {},
            onSelectExternal = {},
            onNavigateExternal = {},
            onStartClick = {},
            onContinueClick = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "BT no disponible, EXTERNAL previa")
@Composable
fun WelcomeScreenExternalBtUnavailablePreview() {
    MobileLanternTheme {
        WelcomeScreen(
            sessionState = WelcomeSessionState.NoCredentials,
            initialSelectedLanternType = LanternType.EXTERNAL,
            bluetoothAvailabilityOverride = BluetoothLanternAvailability.DISABLED,
            onInvalidateExternalLanternSelection = {},
            onSelectIntegrated = {},
            onNavigateIntegrated = {},
            onSelectExternal = {},
            onNavigateExternal = {},
            onStartClick = {},
            onContinueClick = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "BT disponible, externa sin conectar")
@Composable
fun WelcomeScreenExternalAvailableNotConnectedPreview() {
    MobileLanternTheme {
        WelcomeScreen(
            sessionState = WelcomeSessionState.NoCredentials,
            initialSelectedLanternType = LanternType.NONE,
            bluetoothAvailabilityOverride = BluetoothLanternAvailability.AVAILABLE,
            onInvalidateExternalLanternSelection = {},
            onSelectIntegrated = {},
            onNavigateIntegrated = {},
            onSelectExternal = {},
            onNavigateExternal = {},
            onStartClick = {},
            onContinueClick = {}
        )
    }
}
