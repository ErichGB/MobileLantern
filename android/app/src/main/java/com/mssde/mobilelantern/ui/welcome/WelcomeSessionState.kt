package com.mssde.mobilelantern.ui.welcome

sealed interface WelcomeSessionState {
    data object NoCredentials : WelcomeSessionState
    data object Validating : WelcomeSessionState
    data object Ready : WelcomeSessionState
    data object Unauthenticated : WelcomeSessionState
    data object ValidationUnavailable : WelcomeSessionState
}

fun WelcomeSessionState.primaryButtonLabel(): String = when (this) {
    WelcomeSessionState.NoCredentials,
    WelcomeSessionState.Unauthenticated -> "Empezar"
    WelcomeSessionState.Validating -> "Comprobando…"
    WelcomeSessionState.Ready,
    WelcomeSessionState.ValidationUnavailable -> "Continuar"
}

fun WelcomeSessionState.primaryButtonEnabled(hasLanternSelected: Boolean): Boolean =
    hasLanternSelected && this != WelcomeSessionState.Validating
