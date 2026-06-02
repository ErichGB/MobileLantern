package com.mssde.mobilelantern.ui.components.bluetooth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mssde.mobilelantern.ui.theme.MobileLanternTheme
import com.mssde.mobilelantern.viewmodel.BluetoothHistoryItem
import com.mssde.mobilelantern.viewmodel.ConnectionStatus
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BluetoothHistoryCard(
    item: BluetoothHistoryItem,
    isFirst: Boolean = false,
    isLast: Boolean = false
) {
    val iconBackgroundColor = when (item.status) {
        ConnectionStatus.CONNECTED -> Color(0xFFE8F5E9)
        ConnectionStatus.DISCONNECTED -> Color(0xFFEEEEEE)
        ConnectionStatus.FAILED -> Color(0xFFFFEBEE)
        ConnectionStatus.PAIRING -> Color(0xFFE3F2FD)
        ConnectionStatus.PAIRED -> Color(0xFFE1F5FE)
    }
    
    val iconColor = when (item.status) {
        ConnectionStatus.CONNECTED -> Color(0xFF4CAF50)
        ConnectionStatus.DISCONNECTED -> Color(0xFF9E9E9E)
        ConnectionStatus.FAILED -> Color(0xFFF44336)
        ConnectionStatus.PAIRING -> Color(0xFF2196F3)
        ConnectionStatus.PAIRED -> Color(0xFF03A9F4)
    }
    
    val statusLabel = when (item.status) {
        ConnectionStatus.CONNECTED -> "CONECTADO"
        ConnectionStatus.DISCONNECTED -> "DESCONECTADO"
        ConnectionStatus.FAILED -> "ERROR"
        ConnectionStatus.PAIRING -> "EMPAREJANDO"
        ConnectionStatus.PAIRED -> "EMPAREJADO"
    }
    
    val statusLabelColor = when (item.status) {
        ConnectionStatus.CONNECTED -> Color(0xFF4CAF50)
        ConnectionStatus.DISCONNECTED -> Color(0xFF9E9E9E)
        ConnectionStatus.FAILED -> Color(0xFFF44336)
        ConnectionStatus.PAIRING -> Color(0xFF2196F3)
        ConnectionStatus.PAIRED -> Color(0xFF03A9F4)
    }

    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier.width(48.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            if (!isFirst) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(24.dp)
                        .align(Alignment.TopCenter)
                        .background(Color(0xFFE0E0E0))
                )
            }

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(85.dp)
                        .align(Alignment.BottomCenter)
                        .offset(y = (36).dp)
                        .background(Color(0xFFE0E0E0))
                )
            }
            
            Box(
                modifier = Modifier
                    .padding(top = if (isFirst) 0.dp else 24.dp)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconBackgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (item.status) {
                        ConnectionStatus.CONNECTED -> Icons.Default.CheckCircle
                        ConnectionStatus.DISCONNECTED -> Icons.Default.Close
                        ConnectionStatus.FAILED -> Icons.Default.Warning
                        ConnectionStatus.PAIRING -> Icons.Default.Refresh
                        ConnectionStatus.PAIRED -> Icons.Default.CheckCircle
                    },
                    contentDescription = item.status.name,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp, top = if (isFirst) 0.dp else 24.dp, bottom = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                color = statusLabelColor.copy(alpha = 0.12f),
                shape = MaterialTheme.shapes.extraSmall,
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                Text(
                    text = statusLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = statusLabelColor,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                )
            }
            
            Text(
                text = item.deviceName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = formatTimestamp(item.timestamp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm · dd MMM, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

@Preview(showBackground = true)
@Composable
fun PreviewBluetoothHistoryCardConnected() {
    MobileLanternTheme {
        BluetoothHistoryCard(
            item = BluetoothHistoryItem(
                deviceName = "ESP32_LANTERN_01",
                timestamp = System.currentTimeMillis(),
                status = ConnectionStatus.CONNECTED
            ),
            isFirst = true,
            isLast = false
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewBluetoothHistoryCardPairing() {
    MobileLanternTheme {
        BluetoothHistoryCard(
            item = BluetoothHistoryItem(
                deviceName = "ESP32_LANTERN_02",
                timestamp = System.currentTimeMillis() - 60000,
                status = ConnectionStatus.PAIRING
            ),
            isFirst = false,
            isLast = false
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewBluetoothHistoryCardPaired() {
    MobileLanternTheme {
        BluetoothHistoryCard(
            item = BluetoothHistoryItem(
                deviceName = "ESP32_LANTERN_03",
                timestamp = System.currentTimeMillis() - 120000,
                status = ConnectionStatus.PAIRED
            ),
            isFirst = false,
            isLast = false
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewBluetoothHistoryCardDisconnected() {
    MobileLanternTheme {
        BluetoothHistoryCard(
            item = BluetoothHistoryItem(
                deviceName = "ESP32_OLD_DEVICE",
                timestamp = System.currentTimeMillis() - 3600000,
                status = ConnectionStatus.DISCONNECTED
            ),
            isFirst = false,
            isLast = false
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewBluetoothHistoryCardFailed() {
    MobileLanternTheme {
        BluetoothHistoryCard(
            item = BluetoothHistoryItem(
                deviceName = "ESP32_UNKNOWN",
                timestamp = System.currentTimeMillis() - 7200000,
                status = ConnectionStatus.FAILED
            ),
            isFirst = false,
            isLast = true
        )
    }
}

