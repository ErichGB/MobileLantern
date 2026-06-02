package com.mssde.mobilelantern.ui.components.checkin

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.mssde.mobilelantern.domain.model.UiState
import com.mssde.mobilelantern.ui.theme.MobileLanternTheme

@Composable
fun CheckInSheetContent(
    hasCameraPermission: Boolean,
    authState: UiState,
    isProcessingQR: Boolean,
    onQRScanned: (String) -> Unit,
    onSecretLogin: () -> Unit
) {
    var tapCount by remember { mutableStateOf(0) }
    var lastTapTime by remember { mutableStateOf(0L) }

    fun handleSecretTap() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastTapTime > 3000) {
            tapCount = 1
        } else {
            tapCount++
        }
        lastTapTime = currentTime
        if (tapCount >= 7 && !isProcessingQR) {
            onSecretLogin()
            tapCount = 0
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Escanear QR",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { handleSecretTap() }
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Escanea el codigo QR en tu clase para comenzar",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { handleSecretTap() }
        )

        when (authState) {
            is UiState.Loading -> {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Iniciando sesion...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            is UiState.Error -> {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Error: ${authState.errorMessage}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            else -> {}
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 280.dp)
                .aspectRatio(1f)
                .clip(MaterialTheme.shapes.large)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.large)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (hasCameraPermission) {
                QRScannerView(
                    onQRScanned = onQRScanned,
                    isProcessingQR = isProcessingQR
                )
            } else {
                CircularProgressIndicator()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckInBottomSheet(
    authState: UiState,
    onDismiss: () -> Unit,
    onQRScanned: (String) -> Unit,
    onSecretLogin: () -> Unit
) {
    var hasCameraPermission by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val isProcessingQR = authState is UiState.Loading

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            Toast.makeText(context, "Permiso de camara requerido", Toast.LENGTH_SHORT).show()
            onDismiss()
        }
    }

    LaunchedEffect(Unit) {
        hasCameraPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        CheckInSheetContent(
            hasCameraPermission = hasCameraPermission,
            authState = authState,
            isProcessingQR = isProcessingQR,
            onQRScanned = onQRScanned,
            onSecretLogin = onSecretLogin
        )
    }
}

@Preview(showBackground = true, heightDp = 520, name = "Check-in sheet Initial")
@Composable
private fun CheckInSheetContentInitialPreview() {
    MobileLanternTheme {
        CheckInSheetContent(
            hasCameraPermission = false,
            authState = UiState.Initial,
            isProcessingQR = false,
            onQRScanned = {},
            onSecretLogin = {}
        )
    }
}

@Preview(showBackground = true, heightDp = 520, name = "Check-in sheet Loading")
@Composable
private fun CheckInSheetContentLoadingPreview() {
    MobileLanternTheme {
        CheckInSheetContent(
            hasCameraPermission = false,
            authState = UiState.Loading,
            isProcessingQR = true,
            onQRScanned = {},
            onSecretLogin = {}
        )
    }
}

@Preview(showBackground = true, heightDp = 520, name = "Check-in sheet Error")
@Composable
private fun CheckInSheetContentErrorPreview() {
    MobileLanternTheme {
        CheckInSheetContent(
            hasCameraPermission = false,
            authState = UiState.Error("Credenciales invalidas."),
            isProcessingQR = false,
            onQRScanned = {},
            onSecretLogin = {}
        )
    }
}
