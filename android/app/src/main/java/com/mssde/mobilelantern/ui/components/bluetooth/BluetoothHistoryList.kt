package com.mssde.mobilelantern.ui.components.bluetooth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mssde.mobilelantern.ui.theme.MobileLanternTheme
import com.mssde.mobilelantern.viewmodel.BluetoothHistoryItem
import com.mssde.mobilelantern.viewmodel.ConnectionStatus

@Composable
fun BluetoothHistoryList(
    history: List<BluetoothHistoryItem>,
    modifier: Modifier = Modifier
) {
    if (history.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No hay historial de conexiones",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            itemsIndexed(history) { index, item ->
                BluetoothHistoryCard(
                    item = item,
                    isFirst = index == 0,
                    isLast = index == history.lastIndex
                )
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 600)
@Composable
fun PreviewBluetoothHistoryListFull() {
    MobileLanternTheme {
        BluetoothHistoryList(
            history = listOf(
                BluetoothHistoryItem("ESP32_LANTERN_01", System.currentTimeMillis(), ConnectionStatus.CONNECTED),
                BluetoothHistoryItem("ESP32_LANTERN_02", System.currentTimeMillis() - 60000, ConnectionStatus.PAIRING),
                BluetoothHistoryItem("ESP32_LANTERN_02", System.currentTimeMillis() - 120000, ConnectionStatus.PAIRED),
                BluetoothHistoryItem("ESP32_OLD_DEVICE", System.currentTimeMillis() - 3600000, ConnectionStatus.DISCONNECTED),
                BluetoothHistoryItem("ESP32_UNKNOWN", System.currentTimeMillis() - 7200000, ConnectionStatus.FAILED)
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewBluetoothHistoryListEmpty() {
    MobileLanternTheme {
        BluetoothHistoryList(history = emptyList())
    }
}

