package com.mssde.mobilelantern.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuOpen
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.mssde.mobilelantern.data.local.LanternPreferenceManager
import com.mssde.mobilelantern.ui.components.bluetooth.BluetoothConnectionAlert
import com.mssde.mobilelantern.ui.theme.MobileLanternTheme
import com.mssde.mobilelantern.viewmodel.BluetoothViewModel
import androidx.compose.ui.unit.dp
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommonTopBar(
    title: String = "MobileLantern",
    onBackClick: () -> Unit,
    showBackButton: Boolean = true,
    showConfirmDialog: Boolean = true,
    confirmDialogTitle: String = "Confirmar salida",
    confirmDialogText: String = "¿Estás seguro de que quieres salir? Se perderá el progreso actual.",
    showMenuButton: Boolean = false,
    onMenuClick: () -> Unit = {},
    showFlashIndicator: Boolean = true,
    onReconnectBluetooth: () -> Unit = {},
    trailingActions: @Composable RowScope.() -> Unit = {}
) {
    var showDialog by remember { mutableStateOf(false) }
    var showConnectionLostAlert by remember { mutableStateOf(false) }

    if (showFlashIndicator && !LocalInspectionMode.current) {
        val context = LocalContext.current.applicationContext
        val entryPoint = remember(context) {
            EntryPointAccessors.fromApplication(context, CommonTopBarEntryPoint::class.java)
        }
        val lanternType by entryPoint.lanternPreferenceManager().selectedLanternType.collectAsState()
        val btState by entryPoint.bluetoothViewModel().connectionState.collectAsState()
        val lanternPrefManager = entryPoint.lanternPreferenceManager()

        if (lanternType == LanternType.EXTERNAL) {
            LaunchedEffect(btState.error) {
                if (btState.error == "Conexión perdida") {
                    showConnectionLostAlert = true
                }
            }

            if (showConnectionLostAlert) {
                BluetoothConnectionAlert(
                    deviceName = btState.deviceName,
                    onReconnect = {
                        showConnectionLostAlert = false
                        onReconnectBluetooth()
                    },
                    onSwitchToIntegrated = {
                        showConnectionLostAlert = false
                        lanternPrefManager.saveSelectedType(LanternType.INTEGRATED)
                    },
                    onDismiss = { showConnectionLostAlert = false }
                )
            }
        }
    }

    val titleContent: @Composable () -> Unit = {
        if (showFlashIndicator) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FlashIndicator()
                Text(
                    text = title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        } else {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }

    val onBackIconClick: () -> Unit = {
        if (showConfirmDialog) {
            showDialog = true
        } else {
            onBackClick()
        }
    }

    val navigationIconContent: @Composable () -> Unit = {
        if (showBackButton) {
            IconButton(onClick = onBackIconClick) {
                Icon(
                    imageVector = if (showConfirmDialog) Icons.Default.Close else Icons.Filled.ArrowBackIosNew,
                    contentDescription = if (showConfirmDialog) "Salir" else "Volver"
                )
            }
        }
    }

    val actionsContent: @Composable RowScope.() -> Unit = {
        trailingActions()
        if (showMenuButton) {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.MenuOpen,
                    contentDescription = "Menú"
                )
            }
        }
    }

    CenterAlignedTopAppBar(
        title = titleContent,
        navigationIcon = navigationIconContent,
        actions = actionsContent
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(confirmDialogTitle) },
            text = { Text(confirmDialogText) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        onBackClick()
                    }
                ) {
                    Text("Sí, salir")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDialog = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
internal interface CommonTopBarEntryPoint {
    fun lanternPreferenceManager(): LanternPreferenceManager
    fun bluetoothViewModel(): BluetoothViewModel
}

// ============================================
// Previews
// ============================================

@Preview(showBackground = true)
@Composable
fun CommonTopBarDefaultPreview() {
    MobileLanternTheme {
        CommonTopBar(
            title = "Salir del flujo",
            onBackClick = {},
            showBackButton = true
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CommonTopBarWithMenuPreview() {
    MobileLanternTheme {
        CommonTopBar(
            title = "Registrar Pregunta",
            onBackClick = {},
            showBackButton = true,
            showMenuButton = true,
            onMenuClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CommonTopBarMenuOnlyPreview() {
    MobileLanternTheme {
        CommonTopBar(
            title = "Historial",
            onBackClick = {},
            showBackButton = false,
            showMenuButton = true,
            onMenuClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CommonTopBarBackOnlyPreview() {
    MobileLanternTheme {
        CommonTopBar(
            title = "Detalle de Pregunta",
            onBackClick = {},
            showBackButton = true,
            showMenuButton = false
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CommonTopBarNoConfirmPreview() {
    MobileLanternTheme {
        CommonTopBar(
            title = "Volver",
            onBackClick = {},
            showBackButton = true,
            showConfirmDialog = false
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CommonTopBarCustomConfirmTextPreview() {
    MobileLanternTheme {
        CommonTopBar(
            title = "Pregunta",
            onBackClick = {},
            showBackButton = true,
            showConfirmDialog = true,
            confirmDialogTitle = "Salir de la pregunta",
            confirmDialogText = "Si sales ahora perderás lo que no hayas enviado."
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CommonTopBarLongTitlePreview() {
    MobileLanternTheme {
        CommonTopBar(
            title = "Título muy largo que debería cortarse con puntos suspensivos",
            onBackClick = {},
            showBackButton = true,
            showMenuButton = true,
            onMenuClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CommonTopBarWelcomeStylePreview() {
    MobileLanternTheme {
        CommonTopBar(
            title = "Mobile Lantern",
            onBackClick = {},
            showBackButton = false,
            showMenuButton = false,
            showFlashIndicator = false
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CommonTopBarConfirmDialogPreview() {
    MobileLanternTheme {
        var showDialog by remember { mutableStateOf(true) }
        
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Confirmar salida") },
                text = { Text("¿Estás seguro de que quieres salir? Se perderá el progreso actual.") },
                confirmButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Sí, salir")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}