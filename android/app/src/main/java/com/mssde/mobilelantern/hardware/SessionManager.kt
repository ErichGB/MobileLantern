package com.mssde.mobilelantern.hardware

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class SessionState(
    val isActive: Boolean = false,
    val questionId: Int = 0,
    val question: String = "",
    val startTime: Long = 0L,
    val elapsedTimeMs: Long = 0L,
    val aiAttempts: Int = 0,
    val collaborativeAnswerGiven: Boolean = false,
    val isFlashlightOn: Boolean = false,
    val currentPhase: SessionPhase = SessionPhase.IDLE,
    val aiPhaseStartTime: Long = 0L, // Tiempo cuando inicia la fase de IA
    val isFlashingActive: Boolean = false, // Si el tintineo está activo
    val isAlertSoundActive: Boolean = false, // Si la alarma sonora está activa
    val alertSoundActivatedTime: Long = 0L // Tiempo cuando se activó la alerta sonora por primera vez
)

enum class SessionPhase {
    IDLE,                    // Sin sesión activa
    COLLABORATIVE_PHASE,     // Fase de resolución colaborativa (vaso verde)
    AI_ASSISTANCE_PHASE,     // Fase de asistencia con IA (vaso rojo)
    TUTOR_INTERVENTION       // Intervención del tutor
}

@Singleton
class SessionManager @Inject constructor() {
    private val _sessionState = MutableStateFlow(SessionState())
    val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()
    
    /**
     * Inicia una nueva sesión de pregunta
     */
    fun startQuestionSession(questionId: Int, question: String) {
        _sessionState.value = SessionState(
            isActive = true,
            questionId = questionId,
            question = question,
            startTime = System.currentTimeMillis(),
            currentPhase = SessionPhase.COLLABORATIVE_PHASE
        )
    }
    
    /**
     * Actualiza el tiempo transcurrido
     */
    fun updateElapsedTime(elapsedMs: Long) {
        _sessionState.value = _sessionState.value.copy(
            elapsedTimeMs = elapsedMs
        )
    }
    
    /**
     * Marca que se ha dado una respuesta colaborativa
     */
    fun setCollaborativeAnswerGiven(isCorrect: Boolean) {
        val currentState = _sessionState.value
        _sessionState.value = currentState.copy(
            collaborativeAnswerGiven = true,
            currentPhase = if (isCorrect) SessionPhase.IDLE else SessionPhase.AI_ASSISTANCE_PHASE,
            aiPhaseStartTime = if (!isCorrect) System.currentTimeMillis() else 0L
        )
    }
    
    /**
     * Incrementa el contador de intentos con IA
     */
    fun incrementAIAttempts() {
        val currentState = _sessionState.value
        _sessionState.value = currentState.copy(
            aiAttempts = currentState.aiAttempts + 1
        )
    }
    
    /**
     * Marca que se ha activado la linterna
     */
    fun setFlashlightOn(isOn: Boolean) {
        _sessionState.value = _sessionState.value.copy(
            isFlashlightOn = isOn
        )
    }
    
    /**
     * Marca intervención del tutor
     */
    fun markTutorIntervention() {
        _sessionState.value = _sessionState.value.copy(
            currentPhase = SessionPhase.TUTOR_INTERVENTION
        )
    }
    
    /**
     * Finaliza la sesión actual
     */
    fun endSession() {
        _sessionState.value = SessionState() // Reset a estado inicial
    }
    
    /**
     * Marca que el tintineo está activo
     */
    fun setFlashingActive(isActive: Boolean) {
        _sessionState.value = _sessionState.value.copy(
            isFlashingActive = isActive
        )
    }
    
    /**
     * Marca que la alarma sonora está activa
     */
    fun setAlertSoundActive(isActive: Boolean) {
        _sessionState.value = _sessionState.value.copy(
            isAlertSoundActive = isActive
        )
    }
    
    /**
     * Registra el momento cuando se activó la alerta sonora por primera vez
     */
    fun setAlertSoundActivatedTime(time: Long) {
        _sessionState.value = _sessionState.value.copy(
            alertSoundActivatedTime = time
        )
    }
    
    /**
     * Obtiene el tiempo transcurrido desde que se activó la alerta sonora por primera vez
     */
    fun getTimeSinceAlertActivated(): Long {
        val state = _sessionState.value
        return if (state.alertSoundActivatedTime > 0L) {
            System.currentTimeMillis() - state.alertSoundActivatedTime
        } else {
            0L
        }
    }
    
    /**
     * Obtiene el tiempo transcurrido desde el inicio de la fase de IA
     */
    fun getAIPhaseElapsedTime(): Long {
        val state = _sessionState.value
        return if (state.aiPhaseStartTime > 0L) {
            System.currentTimeMillis() - state.aiPhaseStartTime
        } else {
            0L
        }
    }
}