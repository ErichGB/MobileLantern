package com.mssde.mobilelantern.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp

@Composable
fun InputCard(
    title: String,
    emoji: String,
    inputValue: String,
    onInputChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String = "",
    placeholder: String = "",
    submitButtonText: String = "Enviar",
    isLoading: Boolean = false,
    isSubmitEnabled: Boolean = true,
    minLines: Int = 3,
    maxLines: Int = 5,
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        capitalization = KeyboardCapitalization.Sentences,
        imeAction = ImeAction.Done
    ),
    headerActions: @Composable RowScope.() -> Unit = {},
    infoText: String = ""
) {
    BaseCard(
        title = title,
        emoji = emoji,
        modifier = modifier,
        subtitle = subtitle,
        headerActions = headerActions
    ) { _ ->
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = inputValue,
                onValueChange = onInputChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = if (placeholder.isNotEmpty()) { { Text(placeholder) } } else null,
                keyboardOptions = keyboardOptions,
                minLines = minLines,
                maxLines = maxLines,
                enabled = !isLoading
            )

            Button(
                onClick = onSubmit,
                modifier = Modifier.fillMaxWidth(),
                enabled = isSubmitEnabled && !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(submitButtonText)
                }
            }

            if (infoText.isNotEmpty()) {
                Text(
                    text = infoText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
} 