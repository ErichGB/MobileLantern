package com.mssde.mobilelantern.ui.components.drawer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.res.Configuration
import com.mssde.mobilelantern.data.model.HistoryQuestion
import com.mssde.mobilelantern.data.model.QuestionPhase
import com.mssde.mobilelantern.ui.theme.MobileLanternTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Item individual de pregunta en el historial del drawer
 */
@Composable
fun DrawerQuestionItem(
    question: HistoryQuestion,
    onClick: (HistoryQuestion) -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = SimpleDateFormat("dd MMM", Locale.getDefault())
    val phaseColor = Color(android.graphics.Color.parseColor(question.phase.color))
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick(question) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Icono de fase
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        color = phaseColor.copy(alpha = 0.1f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = question.phase.emoji,
                    fontSize = 16.sp
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Contenido de la pregunta
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = question.preview,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = question.phase.displayName,
                        style = MaterialTheme.typography.bodySmall,
                        color = phaseColor,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Text(
                        text = " • ${dateFormatter.format(question.timestamp)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (question.isResolved) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "✓",
                            color = Color(android.graphics.Color.parseColor("#4CAF50")),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

// ==============================
// PREVIEWS
// ==============================

private val previewQuestions = listOf(
    HistoryQuestion(
        id = 1,
        questionText = "¿Cómo calcular la masa molar de un compuesto químico?",
        phase = QuestionPhase.COMPLETED,
        timestamp = Date(System.currentTimeMillis() - 3600000),
        isResolved = true
    ),
    HistoryQuestion(
        id = 2,
        questionText = "¿Qué es la electronegatividad y cómo afecta a los enlaces químicos?",
        phase = QuestionPhase.AI_RESPONSE,
        timestamp = Date(System.currentTimeMillis() - 86400000),
        isResolved = false
    ),
    HistoryQuestion(
        id = 3,
        questionText = "Explicar el concepto de mol y número de Avogadro",
        phase = QuestionPhase.ANSWERED,
        timestamp = Date(System.currentTimeMillis() - 172800000),
        isResolved = false
    ),
    HistoryQuestion(
        id = 4,
        questionText = "¿Cuál es la diferencia entre una reacción exotérmica y endotérmica?",
        phase = QuestionPhase.PENDING,
        timestamp = Date(System.currentTimeMillis() - 259200000),
        isResolved = false
    )
)

@Preview(name = "Question Item - Completada", showBackground = true)
@Composable
private fun PreviewDrawerQuestionItemCompleted() {
    MaterialTheme {
        DrawerQuestionItem(
            question = previewQuestions[0],
            onClick = {}
        )
    }
}

@Preview(name = "Question Item - IA Response", showBackground = true)
@Composable
private fun PreviewDrawerQuestionItemAI() {
    MaterialTheme {
        DrawerQuestionItem(
            question = previewQuestions[1],
            onClick = {}
        )
    }
}

@Preview(name = "Question Item - Respondida", showBackground = true)
@Composable
private fun PreviewDrawerQuestionItemAnswered() {
    MaterialTheme {
        DrawerQuestionItem(
            question = previewQuestions[2],
            onClick = {}
        )
    }
}

@Preview(name = "Question Item - Pendiente", showBackground = true)
@Composable
private fun PreviewDrawerQuestionItemPending() {
    MaterialTheme {
        DrawerQuestionItem(
            question = previewQuestions[3],
            onClick = {}
        )
    }
}

@Preview(name = "Question Item - Todas las Fases", showBackground = true)
@Composable
private fun PreviewDrawerQuestionItemAllPhases() {
    MaterialTheme {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            previewQuestions.forEach { question ->
                DrawerQuestionItem(
                    question = question,
                    onClick = {}
                )
            }
        }
    }
}

@Preview(name = "Question Item - Pregunta Larga", showBackground = true)
@Composable
private fun PreviewDrawerQuestionItemLongText() {
    MaterialTheme {
        DrawerQuestionItem(
            question = HistoryQuestion(
                id = 5,
                questionText = "¿Cómo puedo calcular la cantidad de oxígeno necesaria para una reacción de combustión completa del metano considerando las condiciones estándar de temperatura y presión?",
                phase = QuestionPhase.AI_RESPONSE,
                timestamp = Date(System.currentTimeMillis() - 3600000),
                isResolved = false
            ),
            onClick = {}
        )
    }
}

// Dark Mode Previews
@Preview(name = "Question Item Completed - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewDrawerQuestionItemCompletedDark() {
    MobileLanternTheme(darkTheme = true) {
        DrawerQuestionItem(
            question = previewQuestions[0],
            onClick = {}
        )
    }
}

@Preview(name = "Question Items All Phases - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewDrawerQuestionItemAllPhasesDark() {
    MobileLanternTheme(darkTheme = true) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            previewQuestions.forEach { question ->
                DrawerQuestionItem(
                    question = question,
                    onClick = {}
                )
            }
        }
    }
}

@Preview(name = "Question Item Long Text - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewDrawerQuestionItemLongTextDark() {
    MobileLanternTheme(darkTheme = true) {
        DrawerQuestionItem(
            question = HistoryQuestion(
                id = 5,
                questionText = "¿Cómo puedo calcular la cantidad de oxígeno necesaria para una reacción de combustión completa del metano considerando las condiciones estándar de temperatura y presión?",
                phase = QuestionPhase.AI_RESPONSE,
                timestamp = Date(System.currentTimeMillis() - 3600000),
                isResolved = false
            ),
            onClick = {}
        )
    }
} 