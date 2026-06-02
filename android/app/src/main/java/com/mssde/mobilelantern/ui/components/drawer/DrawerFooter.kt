package com.mssde.mobilelantern.ui.components.drawer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import android.content.res.Configuration
import com.mssde.mobilelantern.data.model.CurrentUser
import com.mssde.mobilelantern.data.model.UserStats
import com.mssde.mobilelantern.ui.theme.MobileLanternTheme

/**
 * Footer fijo del drawer con información de usuario y botón de cerrar sesión
 */
@Composable
fun DrawerFooter(
    user: CurrentUser,
    stats: UserStats,
    onUserClick: () -> Unit,
    onLogoutClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp)
            .background(MaterialTheme.colorScheme.surface)
    ) {
        DrawerUserSectionFixed(
            user = user,
            stats = stats,
            onUserClick = onUserClick
        )
        
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
        
        // Botón de cerrar sesión
        TextButton(
            onClick = onLogoutClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = "Cerrar sesión",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Cerrar sesión",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

// ==============================
// PREVIEWS
// ==============================

private val previewUser = CurrentUser(
    id = 1,
    name = "María González",
    email = "maria.gonzalez@student.com",
    questionsCount = 25,
    resolvedCount = 20
)

private val previewStats = UserStats(
    totalQuestions = 25,
    resolvedQuestions = 20,
    pendingQuestions = 5
)

@Preview(name = "Drawer Footer - Usuario Normal", showBackground = true)
@Composable
private fun PreviewDrawerFooter() {
    MaterialTheme {
        DrawerFooter(
            user = previewUser,
            stats = previewStats,
            onUserClick = {},
            onLogoutClick = {}
        )
    }
}

@Preview(name = "Drawer Footer - Usuario Nuevo", showBackground = true)
@Composable
private fun PreviewDrawerFooterNewUser() {
    MaterialTheme {
        DrawerFooter(
            user = previewUser.copy(
                name = "Carlos Ruiz",
                email = "carlos.ruiz@student.com",
                questionsCount = 0,
                resolvedCount = 0
            ),
            stats = UserStats(
                totalQuestions = 0,
                resolvedQuestions = 0,
                pendingQuestions = 0
            ),
            onUserClick = {},
            onLogoutClick = {}
        )
    }
}

@Preview(name = "Drawer Footer - Usuario Activo", showBackground = true)
@Composable
private fun PreviewDrawerFooterActiveUser() {
    MaterialTheme {
        DrawerFooter(
            user = previewUser.copy(
                name = "Ana Martínez",
                email = "ana.martinez@student.com",
                questionsCount = 157,
                resolvedCount = 142
            ),
            stats = UserStats(
                totalQuestions = 157,
                resolvedQuestions = 142,
                pendingQuestions = 15
            ),
            onUserClick = {},
            onLogoutClick = {}
        )
    }
}

// Dark Mode Previews
@Preview(name = "Drawer Footer - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewDrawerFooterDark() {
    MobileLanternTheme(darkTheme = true) {
        DrawerFooter(
            user = previewUser,
            stats = previewStats,
            onUserClick = {},
            onLogoutClick = {}
        )
    }
}

@Preview(name = "Drawer Footer Active - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewDrawerFooterActiveUserDark() {
    MobileLanternTheme(darkTheme = true) {
        DrawerFooter(
            user = previewUser.copy(
                name = "Ana Martínez",
                email = "ana.martinez@student.com",
                questionsCount = 157,
                resolvedCount = 142
            ),
            stats = UserStats(
                totalQuestions = 157,
                resolvedQuestions = 142,
                pendingQuestions = 15
            ),
            onUserClick = {},
            onLogoutClick = {}
        )
    }
} 