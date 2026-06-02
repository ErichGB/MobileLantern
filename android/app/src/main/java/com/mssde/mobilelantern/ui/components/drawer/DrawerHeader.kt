package com.mssde.mobilelantern.ui.components.drawer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import android.content.res.Configuration
import com.mssde.mobilelantern.ui.theme.MobileLanternTheme

/**
 * Header fijo del drawer con búsqueda y botón nueva pregunta compactos
 */
@Composable
fun DrawerHeader(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onNewQuestionClick: () -> Unit,
    onDrawerStateChange: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp)
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Barra de búsqueda compacta
            DrawerSearchBarCompact(
                searchQuery = searchQuery,
                onSearchQueryChange = onSearchQueryChange,
                onDrawerClose = {
                    focusManager.clearFocus()
                },
                modifier = Modifier.weight(1f)
            )
            
            // Botón nueva pregunta como icono
            NewQuestionIconButton(
                onClick = {
                    focusManager.clearFocus()
                    onNewQuestionClick()
                }
            )
        }
    }
}

// ==============================
// PREVIEWS
// ==============================

@Preview(name = "Drawer Header - Sin Búsqueda", showBackground = true)
@Composable
private fun PreviewDrawerHeaderEmpty() {
    MaterialTheme {
        DrawerHeader(
            searchQuery = "",
            onSearchQueryChange = {},
            onNewQuestionClick = {}
        )
    }
}

@Preview(name = "Drawer Header - Con Búsqueda", showBackground = true)
@Composable
private fun PreviewDrawerHeaderWithSearch() {
    MaterialTheme {
        DrawerHeader(
            searchQuery = "reacción química",
            onSearchQueryChange = {},
            onNewQuestionClick = {}
        )
    }
}

// Dark Mode Previews
@Preview(name = "Drawer Header - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewDrawerHeaderDark() {
    MobileLanternTheme(darkTheme = true) {
        DrawerHeader(
            searchQuery = "",
            onSearchQueryChange = {},
            onNewQuestionClick = {}
        )
    }
}

@Preview(name = "Drawer Header Search - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewDrawerHeaderWithSearchDark() {
    MobileLanternTheme(darkTheme = true) {
        DrawerHeader(
            searchQuery = "reacción química",
            onSearchQueryChange = {},
            onNewQuestionClick = {}
        )
    }
} 