package com.mssde.mobilelantern.ui.components.drawer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import android.content.res.Configuration
import com.mssde.mobilelantern.data.model.CurrentUser
import com.mssde.mobilelantern.data.model.UserStats
import com.mssde.mobilelantern.ui.theme.MobileLanternTheme

/**
 * Sección de usuario fija sin card para el footer del drawer
 */
@Composable
fun DrawerUserSectionFixed(
    user: CurrentUser,
    stats: UserStats,
    onUserClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onUserClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Avatar",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
        
        // Información del usuario
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = user.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = user.email,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        // Total de preguntas a la derecha
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stats.totalQuestions.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Total",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

// ==============================
// PREVIEWS
// ==============================

private val previewUser = CurrentUser(
    id = 1,
    name = "Luis Fernández",
    email = "luis.fernandez@student.com",
    questionsCount = 42,
    resolvedCount = 35
)

private val previewStats = UserStats(
    totalQuestions = 42,
    resolvedQuestions = 35,
    pendingQuestions = 7
)

@Preview(name = "User Section - Normal", showBackground = true)
@Composable
private fun PreviewDrawerUserSectionFixed() {
    MaterialTheme {
        DrawerUserSectionFixed(
            user = previewUser,
            stats = previewStats,
            onUserClick = {}
        )
    }
}

@Preview(name = "User Section - Usuario Nuevo", showBackground = true)
@Composable
private fun PreviewDrawerUserSectionFixedNewUser() {
    MaterialTheme {
        DrawerUserSectionFixed(
            user = previewUser.copy(
                name = "Elena Ramírez",
                email = "elena.ramirez@student.com"
            ),
            stats = UserStats(
                totalQuestions = 1,
                resolvedQuestions = 0,
                pendingQuestions = 1
            ),
            onUserClick = {}
        )
    }
}

@Preview(name = "User Section - Usuario Activo", showBackground = true)
@Composable
private fun PreviewDrawerUserSectionFixedActiveUser() {
    MaterialTheme {
        DrawerUserSectionFixed(
            user = previewUser.copy(
                name = "Pedro Sánchez",
                email = "pedro.sanchez@student.com"
            ),
            stats = UserStats(
                totalQuestions = 285,
                resolvedQuestions = 251,
                pendingQuestions = 34
            ),
            onUserClick = {}
        )
    }
}

@Preview(name = "User Section - Email Largo", showBackground = true)
@Composable
private fun PreviewDrawerUserSectionFixedLongEmail() {
    MaterialTheme {
        DrawerUserSectionFixed(
            user = previewUser.copy(
                name = "María Isabel García López",
                email = "maria.isabel.garcia.lopez@universidad.student.com"
            ),
            stats = previewStats,
            onUserClick = {}
        )
    }
}

// Dark Mode Previews
@Preview(name = "User Section - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewDrawerUserSectionFixedDark() {
    MobileLanternTheme(darkTheme = true) {
        DrawerUserSectionFixed(
            user = previewUser,
            stats = previewStats,
            onUserClick = {}
        )
    }
}

@Preview(name = "User Section Active - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewDrawerUserSectionFixedActiveUserDark() {
    MobileLanternTheme(darkTheme = true) {
        DrawerUserSectionFixed(
            user = previewUser.copy(
                name = "Pedro Sánchez",
                email = "pedro.sanchez@student.com"
            ),
            stats = UserStats(
                totalQuestions = 285,
                resolvedQuestions = 251,
                pendingQuestions = 34
            ),
            onUserClick = {}
        )
    }
}

@Preview(name = "User Section Long Email - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewDrawerUserSectionFixedLongEmailDark() {
    MobileLanternTheme(darkTheme = true) {
        DrawerUserSectionFixed(
            user = previewUser.copy(
                name = "María Isabel García López",
                email = "maria.isabel.garcia.lopez@universidad.student.com"
            ),
            stats = previewStats,
            onUserClick = {}
        )
    }
} 