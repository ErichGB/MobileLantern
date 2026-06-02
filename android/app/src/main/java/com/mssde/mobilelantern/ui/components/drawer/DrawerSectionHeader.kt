package com.mssde.mobilelantern.ui.components.drawer

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import android.content.res.Configuration
import com.mssde.mobilelantern.ui.theme.MobileLanternTheme

/**
 * Divisor con título para secciones del drawer
 */
@Composable
fun DrawerSectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
}

// ==============================
// PREVIEWS
// ==============================

@Preview(name = "Section Header - Hoy", showBackground = true)
@Composable
private fun PreviewDrawerSectionHeaderToday() {
    MaterialTheme {
        DrawerSectionHeader(title = "Hoy")
    }
}

@Preview(name = "Section Header - Esta semana", showBackground = true)
@Composable
private fun PreviewDrawerSectionHeaderThisWeek() {
    MaterialTheme {
        DrawerSectionHeader(title = "Esta semana")
    }
}

@Preview(name = "Section Header - Este mes", showBackground = true)
@Composable
private fun PreviewDrawerSectionHeaderThisMonth() {
    MaterialTheme {
        DrawerSectionHeader(title = "Este mes")
    }
}

@Preview(name = "Section Header - Múltiples", showBackground = true)
@Composable
private fun PreviewDrawerSectionHeaderMultiple() {
    MaterialTheme {
        androidx.compose.foundation.layout.Column {
            DrawerSectionHeader(title = "Hoy")
            DrawerSectionHeader(title = "Ayer")
            DrawerSectionHeader(title = "Esta semana")
            DrawerSectionHeader(title = "Semana pasada")
            DrawerSectionHeader(title = "Este mes")
            DrawerSectionHeader(title = "Mes pasado")
        }
    }
}

// Dark Mode Previews
@Preview(name = "Section Header - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewDrawerSectionHeaderDark() {
    MobileLanternTheme(darkTheme = true) {
        DrawerSectionHeader(title = "Hoy")
    }
}

@Preview(name = "Section Headers Multiple - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewDrawerSectionHeaderMultipleDark() {
    MobileLanternTheme(darkTheme = true) {
        androidx.compose.foundation.layout.Column {
            DrawerSectionHeader(title = "Hoy")
            DrawerSectionHeader(title = "Ayer")
            DrawerSectionHeader(title = "Esta semana")
            DrawerSectionHeader(title = "Semana pasada")
            DrawerSectionHeader(title = "Este mes")
            DrawerSectionHeader(title = "Mes pasado")
        }
    }
} 