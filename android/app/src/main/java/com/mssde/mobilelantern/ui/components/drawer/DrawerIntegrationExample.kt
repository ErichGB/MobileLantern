package com.mssde.mobilelantern.ui.components.drawer

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.LayoutDirection
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import com.mssde.mobilelantern.QuestionActivity
import com.mssde.mobilelantern.WelcomeActivity
import com.mssde.mobilelantern.data.model.HistoryQuestion
import com.mssde.mobilelantern.ui.components.CommonTopBar

/**
 * Componente reutilizable que envuelve cualquier contenido con el drawer de navegación
 * 
 * Uso:
 * ```
 * DrawerWrapper(
 *     title = "Mi Actividad",
 *     showBackButton = true,
 *     onBackClick = { finish() }
 * ) {
 *     // Tu contenido aquí
 *     MiContenidoComposable()
 * }
 * ```
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DrawerWrapper(
    title: String,
    showBackButton: Boolean = true,
    showConfirmDialog: Boolean = true,
    showMenuButton: Boolean = true,
    onBackClick: () -> Unit = {},
    onQuestionClick: ((HistoryQuestion) -> Unit)? = null,
    onReconnectBluetooth: () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    val context = LocalContext.current
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val drawerViewModel: DrawerViewModel = hiltViewModel()

    LaunchedEffect(drawerState.targetValue) {
        if (drawerState.targetValue == DrawerValue.Open) {
            keyboardController?.hide()
            focusManager.clearFocus()
        }
    }

    LaunchedEffect(drawerState.currentValue) {
        if (drawerState.currentValue == DrawerValue.Open) {
            keyboardController?.hide()
            focusManager.clearFocus()
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    MainDrawer(
                        drawerState = drawerState,
                        onNewQuestionClick = {
                            focusManager.clearFocus()
                            scope.launch { drawerState.close() }
                            navigateToNewQuestion(context)
                        },
                        onQuestionClick = { question ->
                            focusManager.clearFocus()
                            scope.launch { drawerState.close() }
                            onQuestionClick?.invoke(question) ?: navigateToQuestion(context, question)
                        },
                        onUserClick = {
                            focusManager.clearFocus()
                            scope.launch { drawerState.close() }
                            // TODO: Implementar navegación al perfil
                        },
                        onLogoutClick = {
                            focusManager.clearFocus()
                            scope.launch { drawerState.close() }
                            // Limpiar sesión y navegar a WelcomeActivity
                            drawerViewModel.logout()
                            performLogout(context)
                        }
                    )
                }
            }
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Scaffold(
                    topBar = {
                        CommonTopBar(
                            title = title,
                            onBackClick = onBackClick,
                            showBackButton = showBackButton,
                            showConfirmDialog = showConfirmDialog,
                            showMenuButton = showMenuButton,
                            onMenuClick = {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                scope.launch { drawerState.open() }
                            },
                            onReconnectBluetooth = onReconnectBluetooth
                        )
                    },
                    floatingActionButton = floatingActionButton
                ) { paddingValues ->
                    content(paddingValues)
                }
            }
        }
    }
}

/**
 * Función helper para navegar a nueva pregunta
 */
private fun navigateToNewQuestion(context: Context) {
    val intent = Intent(context, QuestionActivity::class.java)
    context.startActivity(intent)
}

/**
 * Función helper para navegar a una pregunta específica
 */
private fun navigateToQuestion(context: Context, question: HistoryQuestion) {
    val intent = Intent(context, com.mssde.mobilelantern.QuestionDetailActivity::class.java).apply {
        putExtra("questionId", question.id)
        putExtra("questionText", question.questionText)
    }
    context.startActivity(intent)
}

/**
 * Función helper para realizar el logout y navegar a WelcomeActivity
 * Cierra todas las actividades previas para empezar desde cero
 */
private fun performLogout(context: Context) {
    val intent = Intent(context, WelcomeActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    context.startActivity(intent)
}

/**
 * Extensión para Activities que facilita el uso del drawer
 * 
 * Ejemplo de uso en una Activity:
 * ```kotlin
 * @Composable
 * fun MyActivityContent() {
 *     DrawerScaffold(
 *         title = "Mi Actividad",
 *         showBackButton = true,
 *         onBackClick = { finish() }
 *     ) { paddingValues ->
 *         // Tu contenido aquí
 *         MyContent(modifier = Modifier.padding(paddingValues))
 *     }
 * }
 * ```
 */
@Composable
fun DrawerScaffold(
    title: String,
    showBackButton: Boolean = true,
    showConfirmDialog: Boolean = true,
    onBackClick: () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    DrawerWrapper(
        title = title,
        showBackButton = showBackButton,
        showConfirmDialog = showConfirmDialog,
        onBackClick = onBackClick,
        floatingActionButton = floatingActionButton
    ) { paddingValues ->
        content(paddingValues)
    }
} 