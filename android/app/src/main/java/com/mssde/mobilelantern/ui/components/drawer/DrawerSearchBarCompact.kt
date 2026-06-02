package com.mssde.mobilelantern.ui.components.drawer

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import android.content.res.Configuration
import com.mssde.mobilelantern.ui.theme.MobileLanternTheme

/**
 * Barra de búsqueda compacta para usar en el header del drawer
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawerSearchBarCompact(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onDrawerClose: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        modifier = modifier.height(48.dp),
        placeholder = { 
            Text(
                "Buscar preguntas...",
                style = MaterialTheme.typography.bodyMedium
            ) 
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Buscar",
                modifier = Modifier.size(18.dp)
            )
        },
        textStyle = MaterialTheme.typography.bodyMedium,
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
    )
}

// ==============================
// PREVIEWS
// ==============================

@Preview(name = "Search Bar - Vacía", showBackground = true)
@Composable
private fun PreviewDrawerSearchBarCompactEmpty() {
    MaterialTheme {
        DrawerSearchBarCompact(
            searchQuery = "",
            onSearchQueryChange = {}
        )
    }
}

@Preview(name = "Search Bar - Con Texto", showBackground = true)
@Composable
private fun PreviewDrawerSearchBarCompactWithText() {
    MaterialTheme {
        DrawerSearchBarCompact(
            searchQuery = "oxígeno",
            onSearchQueryChange = {}
        )
    }
}

// Dark Mode Previews
@Preview(name = "Search Bar - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewDrawerSearchBarCompactDark() {
    MobileLanternTheme(darkTheme = true) {
        DrawerSearchBarCompact(
            searchQuery = "",
            onSearchQueryChange = {}
        )
    }
}

@Preview(name = "Search Bar Con Texto - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewDrawerSearchBarCompactWithTextDark() {
    MobileLanternTheme(darkTheme = true) {
        DrawerSearchBarCompact(
            searchQuery = "reacción química",
            onSearchQueryChange = {}
        )
    }
} 