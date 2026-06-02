package com.mssde.mobilelantern.ui.components.drawer

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import android.content.res.Configuration
import com.mssde.mobilelantern.ui.theme.MobileLanternTheme

/**
 * Botón de nueva pregunta cuadrado para el header compacto del drawer
 */
@Composable
fun NewQuestionIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.size(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Nueva Pregunta",
            modifier = Modifier.size(24.dp)
        )
    }
}

// ==============================
// PREVIEWS
// ==============================

@Preview(name = "New Question Button", showBackground = true)
@Composable
private fun PreviewNewQuestionIconButton() {
    MaterialTheme {
        NewQuestionIconButton(onClick = {})
    }
}

// Dark Mode Preview
@Preview(name = "New Question Button - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewNewQuestionIconButtonDark() {
    MobileLanternTheme(darkTheme = true) {
        NewQuestionIconButton(onClick = {})
    }
} 