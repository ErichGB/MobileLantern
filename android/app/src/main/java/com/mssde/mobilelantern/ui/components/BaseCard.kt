package com.mssde.mobilelantern.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mssde.mobilelantern.ui.theme.MobileLanternTheme

@Composable
fun BaseCard(
    title: String,
    emoji: String,
    modifier: Modifier = Modifier,
    subtitle: String = "",
    isExpandable: Boolean = false,
    initiallyExpanded: Boolean = false,
    onExpandToggle: ((Boolean) -> Unit)? = null,
    headerActions: @Composable RowScope.() -> Unit = {},
    content: @Composable ColumnScope.(isExpanded: Boolean) -> Unit
) {
    var isExpanded by remember { mutableStateOf(initiallyExpanded) }
    
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header con título, emoji y acciones (solo si hay título o emoji)
            if (title.isNotEmpty() || emoji.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (emoji.isNotEmpty()) {
                            Text(emoji, style = MaterialTheme.typography.titleLarge)
                        }
                        if (title.isNotEmpty()) {
                            Text(
                                title,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Acciones personalizadas del header
                        headerActions()
                        
                        // Botón de expandir/contraer solo si es expandible
                        if (isExpandable) {
                            IconButton(
                                onClick = { 
                                    isExpanded = !isExpanded
                                    onExpandToggle?.invoke(isExpanded)
                                }
                            ) {
                                Icon(
                                    imageVector = if (isExpanded) 
                                        Icons.Default.KeyboardArrowUp 
                                    else 
                                        Icons.Default.KeyboardArrowDown,
                                    contentDescription = if (isExpanded) 
                                        "Contraer" 
                                    else 
                                        "Expandir"
                                )
                            }
                        }
                    }
                }
                
                // Subtítulo (opcional)
                if (subtitle.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Espaciador después del header
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Contenido personalizable
            content(isExpanded)
        }
    }
}

// ============================================
// Previews
// ============================================

@Preview(showBackground = true)
@Composable
fun BaseCardPreview() {
    MobileLanternTheme {
        BaseCard(
            title = "Título de ejemplo",
            emoji = "📝"
        ) { _ ->
            Text("Este es el contenido de la tarjeta básica.")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BaseCardWithSubtitlePreview() {
    MobileLanternTheme {
        BaseCard(
            title = "Título con subtítulo",
            emoji = "💡",
            subtitle = "Este es un subtítulo que ayuda al usuario a comprender mejor."
        ) { _ ->
            Text("Contenido de la tarjeta con subtítulo explicativo.")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BaseCardExpandablePreview() {
    MobileLanternTheme {
        BaseCard(
            title = "Tarjeta expandible",
            emoji = "📂",
            subtitle = "Puedes expandir o contraer esta tarjeta",
            isExpandable = true,
            initiallyExpanded = false
        ) { isExpanded ->
            if (isExpanded) {
                Text("Contenido visible cuando está expandida.")
            } else {
                Text("Vista colapsada")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BaseCardWithActionsPreview() {
    MobileLanternTheme {
        BaseCard(
            title = "Tarjeta con acciones",
            emoji = "⚙️",
            subtitle = "Esta tarjeta tiene botones de acción en el header",
            headerActions = {
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Acción"
                    )
                }
            }
        ) { _ ->
            Text("Contenido de la tarjeta con acciones personalizadas.")
        }
    }
} 