package com.mssde.mobilelantern.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp

@Composable
fun PlaceGlassDialog(
    glassColor: GlassColor,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { /* No permitir cerrar tocando fuera */ },
        title = { 
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(glassColor.emoji, style = MaterialTheme.typography.titleLarge)
                Text(glassColor.title)
            }
        },
        text = { 
            Text(glassColor.message)
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Aceptar")
            }
        }
    )
}

enum class GlassColor(
    val emoji: String,
    val title: String,
    val message: String
) {
    GREEN(
        emoji = "🟢",
        title = "Tiempo de colaboración",
        message = "Por favor coloca el vaso verde mientras buscas solución colaborativamente a tu duda, y que el profesor lo monitorice visualmente"
    ),
    RED(
        emoji = "🔴",
        title = "Tiempo de asistencia con IA",
        message = "Por favor coloca el vaso rojo mientras buscas mejorar la solución a tu duda utilizando nuestra IA, y que el profesor lo monitorice visualmente."
    ),
    CONGRATULATIONS(
        emoji = "🎉",
        title = "¡Enhorabuena!",
        message = "Has encontrado la respuesta a tus dudas de forma colaborativa, sin uso de la IA, y sin asistencia del profesor."
    ),
    AI_SUCCESS(
        emoji = "🤖✨",
        title = "¡Excelente!",
        message = "Has resuelto tu duda con la ayuda de la IA. ¡Sigue aprendiendo!"
    ),
    TUTOR_SUCCESS(
        emoji = "👨‍🏫✅",
        title = "¡Problema resuelto!",
        message = "El profesor ha intervenido y resuelto tu duda. ¡Continúa con tu aprendizaje!"
    )
} 