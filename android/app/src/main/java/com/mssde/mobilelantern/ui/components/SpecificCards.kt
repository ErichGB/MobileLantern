package com.mssde.mobilelantern.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mssde.mobilelantern.ui.theme.MobileLanternTheme
import dev.jeziellago.compose.markdowntext.MarkdownText

// Card específico para mostrar preguntas con configuración predeterminada y soporte Markdown
@Composable
fun StandardQuestionCard(
    question: String,
    modifier: Modifier = Modifier,
    isExpandable: Boolean = false,
    initiallyExpanded: Boolean = false,
    maxCollapsedLines: Int = 3,
    onExpandToggle: ((Boolean) -> Unit)? = null,
    headerActions: @Composable RowScope.() -> Unit = {}
) {
    BaseCard(
        title = "Pregunta",
        emoji = "🙋",
        modifier = modifier,
        isExpandable = isExpandable,
        initiallyExpanded = initiallyExpanded,
        onExpandToggle = onExpandToggle,
        headerActions = headerActions
    ) { isExpanded ->
        MarkdownText(
            markdown = question,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = if (isExpandable && !isExpanded) maxCollapsedLines else Int.MAX_VALUE
        )
    }
}

// Card específico para mostrar respuestas colaborativas con soporte Markdown
@Composable
fun CollaborativeAnswerCard(
    answer: String,
    modifier: Modifier = Modifier,
    isExpandable: Boolean = false,
    initiallyExpanded: Boolean = false,
    maxCollapsedLines: Int = 3,
    onExpandToggle: ((Boolean) -> Unit)? = null,
    headerActions: @Composable RowScope.() -> Unit = {}
) {
    BaseCard(
        title = "Respuesta colaborativa",
        emoji = "💡",
        modifier = modifier,
        isExpandable = isExpandable,
        initiallyExpanded = initiallyExpanded,
        onExpandToggle = onExpandToggle,
        headerActions = headerActions
    ) { isExpanded ->
        MarkdownText(
            markdown = answer,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = if (isExpandable && !isExpanded) maxCollapsedLines else Int.MAX_VALUE
        )
    }
}

// Card específico para mostrar respuestas de IA con formato Markdown
@Composable
fun AIResponseCard(
    response: String,
    modifier: Modifier = Modifier,
    isExpandable: Boolean = false,
    initiallyExpanded: Boolean = false,
    onExpandToggle: ((Boolean) -> Unit)? = null,
    headerActions: @Composable RowScope.() -> Unit = {}
) {
    BaseCard(
        title = "Respuesta de IA",
        emoji = "🤖",
        modifier = modifier,
        isExpandable = isExpandable,
        initiallyExpanded = initiallyExpanded,
        onExpandToggle = onExpandToggle,
        headerActions = headerActions
    ) { _ ->
        MarkdownText(
            markdown = response,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// Card específico para mostrar nuevas preguntas de usuario
@Composable
fun UserQuestionCard(
    question: String,
    modifier: Modifier = Modifier,
    isExpandable: Boolean = false,
    initiallyExpanded: Boolean = false,
    maxCollapsedLines: Int = 3,
    onExpandToggle: ((Boolean) -> Unit)? = null,
    headerActions: @Composable RowScope.() -> Unit = {}
) {
    BaseCard(
        title = "Nueva Pregunta",
        emoji = "❓",
        modifier = modifier,
        isExpandable = isExpandable,
        initiallyExpanded = initiallyExpanded,
        onExpandToggle = onExpandToggle,
        headerActions = headerActions
    ) { isExpanded ->
        Text(
            text = question,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = if (isExpandable && !isExpanded) maxCollapsedLines else Int.MAX_VALUE
        )
    }
}

// Card específico para estados de carga
@Composable
fun LoadingCard(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    emoji: String = "🤖"
) {
    BaseCard(
        title = title,
        emoji = emoji,
        modifier = modifier
    ) { _ ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Card específico para confirmaciones con botones
@Composable
fun ConfirmationCard(
    question: String,
    modifier: Modifier = Modifier,
    onConfirm: () -> Unit,
    onDeny: () -> Unit,
    confirmText: String = "Sí",
    denyText: String = "No",
    confirmButtonColor: ButtonColors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary
    ),
    denyButtonColor: ButtonColors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.error
    ),
    infoText: String = ""
) {
    BaseCard(
        title = "",
        emoji = "",
        modifier = modifier
    ) { _ ->
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = question,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = onConfirm,
                    colors = confirmButtonColor
                ) {
                    Text(confirmText)
                }

                Button(
                    onClick = onDeny,
                    colors = denyButtonColor
                ) {
                    Text(denyText)
                }
            }

            if (infoText.isNotEmpty()) {
                Text(
                    text = infoText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// Card específico para mostrar errores con opción de reintentar
@Composable
fun ErrorCard(
    title: String = "Error",
    message: String,
    modifier: Modifier = Modifier,
    emoji: String = "⚠️",
    onRetry: () -> Unit,
    retryText: String = "Reintentar"
) {
    BaseCard(
        title = title,
        emoji = emoji,
        modifier = modifier
    ) { _ ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.error
            )
            
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(retryText)
            }
        }
    }
}

// ============================================
// Previews
// ============================================

@Preview(showBackground = true)
@Composable
fun StandardQuestionCardPreview() {
    MobileLanternTheme {
        StandardQuestionCard(
            question = "¿Cuál es la fórmula para calcular el área de un círculo?"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun StandardQuestionCardMarkdownPreview() {
    MobileLanternTheme {
        StandardQuestionCard(
            question = "# Pregunta sobre Matemáticas\n\n¿Cómo se resuelve la ecuación **x² + 5x + 6 = 0**?\n\n- Opción A: Factorización\n- Opción B: Fórmula general\n- Opción C: Ambas"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun StandardQuestionCardExpandablePreview() {
    MobileLanternTheme {
        StandardQuestionCard(
            question = "Esta es una pregunta muy larga que necesita ser expandible. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris.",
            isExpandable = true,
            initiallyExpanded = false,
            maxCollapsedLines = 3
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CollaborativeAnswerCardPreview() {
    MobileLanternTheme {
        CollaborativeAnswerCard(
            answer = "El área de un círculo se calcula con la fórmula **A = πr²**, donde r es el radio del círculo."
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CollaborativeAnswerCardMarkdownPreview() {
    MobileLanternTheme {
        CollaborativeAnswerCard(
            answer = "# Respuesta Colaborativa\n\nPara resolver este problema:\n\n1. Identifica los datos\n2. Aplica la fórmula\n3. Calcula el resultado\n\n**Importante:** No olvides las unidades."
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AIResponseCardPreview() {
    MobileLanternTheme {
        AIResponseCard(
            response = "# Explicación de la IA\n\nEl **teorema de Pitágoras** establece que en un triángulo rectángulo:\n\n**a² + b² = c²**\n\nDonde:\n- **a** y **b** son los catetos\n- **c** es la hipotenusa\n\n## Ejemplo\n\nSi a = 3 y b = 4, entonces:\n\nc² = 3² + 4² = 9 + 16 = 25\n\nc = √25 = **5**"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AIResponseCardSimplePreview() {
    MobileLanternTheme {
        AIResponseCard(
            response = "La respuesta correcta es aplicar la fórmula del área del triángulo: **A = (base × altura) / 2**"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun UserQuestionCardPreview() {
    MobileLanternTheme {
        UserQuestionCard(
            question = "¿Podrías explicarme más sobre cómo se aplica este concepto en la vida real?"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun UserQuestionCardLongPreview() {
    MobileLanternTheme {
        UserQuestionCard(
            question = "No entiendo bien la diferencia entre estos dos conceptos. ¿Podrías darme un ejemplo práctico que me ayude a visualizar mejor cómo funciona esto en la práctica?",
            isExpandable = true,
            maxCollapsedLines = 2
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LoadingCardPreview() {
    MobileLanternTheme {
        LoadingCard(
            title = "Consultando con la IA...",
            message = "La IA está analizando tu respuesta y preparando una explicación detallada..."
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LoadingCardProcessingPreview() {
    MobileLanternTheme {
        LoadingCard(
            title = "Procesando nueva pregunta...",
            message = "Estamos trabajando en tu solicitud. Esto puede tomar unos segundos.",
            emoji = "⏳"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ConfirmationCardPreview() {
    MobileLanternTheme {
        ConfirmationCard(
            question = "¿Has resuelto tu duda con la IA?",
            onConfirm = {},
            onDeny = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ConfirmationCardCustomPreview() {
    MobileLanternTheme {
        ConfirmationCard(
            question = "¿Deseas continuar con esta acción?",
            onConfirm = {},
            onDeny = {},
            confirmText = "Continuar",
            denyText = "Cancelar"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ErrorCardPreview() {
    MobileLanternTheme {
        ErrorCard(
            title = "Error al consultar IA",
            message = "No se pudo conectar con el servidor. Por favor, verifica tu conexión a internet e intenta nuevamente.",
            onRetry = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ErrorCardNetworkPreview() {
    MobileLanternTheme {
        ErrorCard(
            title = "Error de conexión",
            message = "Tiempo de espera agotado. El servidor no responde.",
            onRetry = {},
            retryText = "Volver a intentar"
        )
    }
} 