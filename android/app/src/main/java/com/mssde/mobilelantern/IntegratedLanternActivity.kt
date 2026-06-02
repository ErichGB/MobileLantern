package com.mssde.mobilelantern

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.mssde.mobilelantern.ui.components.CommonTopBar
import com.mssde.mobilelantern.ui.components.StepCard
import com.mssde.mobilelantern.ui.theme.MobileLanternTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class IntegratedLanternActivity : ComponentActivity() {

    private var hasCameraPermission by mutableStateOf(false)
    private var flashlightOn by mutableStateOf(false)
    private var cameraManager: CameraManager? = null
    private var cameraId: String? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
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

        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            cameraId = cameraManager?.cameraIdList?.get(0)
        } catch (e: Exception) {
            Log.e("IntegratedLantern", "Error obteniendo cameraId", e)
        }

        hasCameraPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        setContent {
            MobileLanternTheme {
                IntegratedLanternScreen(
                    flashlightOn = flashlightOn,
                    hasCameraPermission = hasCameraPermission,
                    onBackClick = { finish() },
                    onToggleFlashlight = { turnOn ->
                        toggleFlashlight(turnOn)
                    },
                    onRequestPermission = {
                        requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                )
            }
        }
    }

    private fun toggleFlashlight(turnOn: Boolean) {
        if (!hasCameraPermission) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            return
        }
        try {
            cameraId?.let { id ->
                cameraManager?.setTorchMode(id, turnOn)
                flashlightOn = turnOn
            }
        } catch (e: Exception) {
            Log.e("IntegratedLantern", "Error al controlar linterna", e)
            Toast.makeText(this, "Error al controlar la linterna", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPause() {
        super.onPause()
        if (flashlightOn) toggleFlashlight(false)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (flashlightOn) {
            try {
                cameraId?.let { cameraManager?.setTorchMode(it, false) }
            } catch (_: Exception) {}
        }
    }
}

@Composable
fun IntegratedLanternScreen(
    flashlightOn: Boolean,
    hasCameraPermission: Boolean,
    onBackClick: () -> Unit,
    onToggleFlashlight: (Boolean) -> Unit,
    onRequestPermission: () -> Unit
) {
    Scaffold(
        topBar = {
            CommonTopBar(
                title = "Linterna integrada",
                onBackClick = onBackClick,
                showConfirmDialog = false,
                showFlashIndicator = false
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        if (hasCameraPermission) {
                            onToggleFlashlight(!flashlightOn)
                        } else {
                            onRequestPermission()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (flashlightOn)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                    )
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_flashlight),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (flashlightOn) "Apagar linterna" else "Encender linterna",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                if (!hasCameraPermission) {
                    Text(
                        text = "Se necesita permiso de cámara para controlar la linterna.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "Cómo funciona la orquestación con vasos",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "La linterna del teléfono se usa junto con vasos de colores para señalizar tu estado durante la clase.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            StepCard(
                stepNumber = 1,
                title = "Enciende la linterna",
                description = "Activa la linterna del teléfono y colócalo boca abajo sobre la mesa."
            )

            StepCard(
                stepNumber = 2,
                title = "Coloca el vaso verde",
                description = "El vaso verde sobre la linterna indica que estás trabajando y buscando ayuda colaborativa."
            )

            StepCard(
                stepNumber = 3,
                title = "Cambia al vaso rojo",
                description = "Si no logras resolver la duda, cambia al vaso rojo. La app detectará el cambio y gestionará la prioridad del turno."
            )
        }
    }
}

// ============================================
// Previews
// ============================================

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun IntegratedLanternScreenPreview() {
    MobileLanternTheme {
        IntegratedLanternScreen(
            flashlightOn = false,
            hasCameraPermission = true,
            onBackClick = {},
            onToggleFlashlight = {},
            onRequestPermission = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Linterna encendida")
@Composable
private fun IntegratedLanternScreenOnPreview() {
    MobileLanternTheme {
        IntegratedLanternScreen(
            flashlightOn = true,
            hasCameraPermission = true,
            onBackClick = {},
            onToggleFlashlight = {},
            onRequestPermission = {}
        )
    }
}
