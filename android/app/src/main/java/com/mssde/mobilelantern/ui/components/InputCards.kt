package com.mssde.mobilelantern.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mssde.mobilelantern.ui.theme.MobileLanternTheme
import com.mssde.mobilelantern.ui.UiTextConfig

// Input Card específico para registrar preguntas
@Composable
fun QuestionInputCard(
    questionText: String,
    onQuestionChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    isValid: Boolean = true,
    headerActions: @Composable RowScope.() -> Unit = {}
) {
    InputCard(
        title = UiTextConfig.get("qt") ?: "¿Tienes dudas sobre la tarea?",
        emoji = "🙋",
        inputValue = questionText,
        onInputChange = onQuestionChange,
        onSubmit = onSubmit,
        modifier = modifier,
        subtitle = UiTextConfig.get("qs") ?: "Antes de preguntar al profesor o tus compañeros…",
        placeholder = UiTextConfig.get("qp") ?: "Escribe aquí tu pregunta para que podamos ayudarte ordenadamente…",
        submitButtonText = "Registrar",
        isLoading = isLoading,
        isSubmitEnabled = isValid,
        headerActions = headerActions
    )
}

// Input Card específico para nueva pregunta en IA
@Composable
fun NewQuestionInputCard(
    questionText: String,
    onQuestionChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    isValid: Boolean = true,
    headerActions: @Composable RowScope.() -> Unit = {}
) {
    InputCard(
        title = UiTextConfig.get("nqt") ?: "¿Nueva pregunta a la IA?",
        emoji = "❓",
        inputValue = questionText,
        onInputChange = onQuestionChange,
        onSubmit = onSubmit,
        modifier = modifier,
        subtitle = UiTextConfig.get("nqs") ?: "",
        placeholder = UiTextConfig.get("nqp") ?: "Escribe tu pregunta o contexto adicional...",
        submitButtonText = "Enviar",
        isLoading = isLoading,
        isSubmitEnabled = isValid,
        headerActions = headerActions,
        infoText = "Si el profesor ha resuelto finalmente tu pregunta, pulsa el botón de la esquina inferior derecha para registrar su solución."
    )
}

// Input Card específico para respuestas colaborativas
@Composable
fun AnswerInputCard(
    answerText: String,
    onAnswerChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String = "Antes de preguntar al profesor, consulta a los compañeros alrededor de ti y escribe a qué conclusión has llegado sobre tu duda.",
    isLoading: Boolean = false,
    isValid: Boolean = true,
    isCorrect: Boolean = false,
    onCorrectChange: (Boolean) -> Unit = {},
    showCorrectToggle: Boolean = true,
    headerActions: @Composable RowScope.() -> Unit = {}
) {
    BaseCard(
        title = UiTextConfig.get("at") ?: "Conclusión colaborativa",
        emoji = "💡",
        modifier = modifier,
        subtitle = UiTextConfig.get("as") ?: subtitle,
        headerActions = headerActions
    ) { _ ->
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = answerText,
                onValueChange = onAnswerChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(UiTextConfig.get("ap") ?: "Escribe aquí tu respuesta...") },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done
                ),
                minLines = 3,
                maxLines = 5,
                enabled = !isLoading
            )

            if (showCorrectToggle) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Pregunta con Switch al lado
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "¿Crees que la respuesta colaborativa es suficientemente buena?",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f).padding(end = 8.dp)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (isCorrect) "Sí" else "No",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Switch(
                                checked = isCorrect,
                                onCheckedChange = onCorrectChange,
                                enabled = !isLoading
                            )
                        }
                    }
                    
                    // Mensaje informativo con icono
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Información",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Seleccionando NO, podrás profundizar más utilizando IA generativa.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Button(
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth(),
                enabled = isValid && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Registrar")
                }
            }
        }
    }
}

// ============================================
// Previews
// ============================================

@Preview(showBackground = true)
@Composable
fun QuestionInputCardPreview() {
    MobileLanternTheme {
        QuestionInputCard(
            questionText = "",
            onQuestionChange = {},
            onSubmit = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun QuestionInputCardWithTextPreview() {
    MobileLanternTheme {
        QuestionInputCard(
            questionText = "¿Cómo puedo resolver este problema de matemáticas?",
            onQuestionChange = {},
            onSubmit = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun QuestionInputCardLoadingPreview() {
    MobileLanternTheme {
        QuestionInputCard(
            questionText = "¿Cómo puedo resolver este problema de matemáticas?",
            onQuestionChange = {},
            onSubmit = {},
            isLoading = true
        )
    }
}

@Preview(showBackground = true)
@Composable
fun NewQuestionInputCardPreview() {
    MobileLanternTheme {
        NewQuestionInputCard(
            questionText = "",
            onQuestionChange = {},
            onSubmit = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun NewQuestionInputCardWithTextPreview() {
    MobileLanternTheme {
        NewQuestionInputCard(
            questionText = "¿Puedes explicarme más sobre este concepto?",
            onQuestionChange = {},
            onSubmit = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AnswerInputCardEmptyPreview() {
    MobileLanternTheme {
        AnswerInputCard(
            answerText = "",
            onAnswerChange = {},
            onSubmit = {},
            isCorrect = false,
            onCorrectChange = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AnswerInputCardWithTextPreview() {
    MobileLanternTheme {
        AnswerInputCard(
            answerText = "La respuesta es que debemos aplicar el teorema de Pitágoras para resolver este problema de triángulos.",
            onAnswerChange = {},
            onSubmit = {},
            isCorrect = false,
            onCorrectChange = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AnswerInputCardCorrectPreview() {
    MobileLanternTheme {
        AnswerInputCard(
            answerText = "La respuesta es que debemos aplicar el teorema de Pitágoras.",
            onAnswerChange = {},
            onSubmit = {},
            isCorrect = true,
            onCorrectChange = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AnswerInputCardLoadingPreview() {
    MobileLanternTheme {
        AnswerInputCard(
            answerText = "La respuesta es que debemos aplicar el teorema de Pitágoras.",
            onAnswerChange = {},
            onSubmit = {},
            isCorrect = true,
            onCorrectChange = {},
            isLoading = true
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AnswerInputCardWithoutTogglePreview() {
    MobileLanternTheme {
        AnswerInputCard(
            answerText = "Esta es una respuesta sin el toggle de correcta.",
            onAnswerChange = {},
            onSubmit = {},
            showCorrectToggle = false
        )
    }
} 