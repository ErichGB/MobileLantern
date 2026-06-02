package com.mssde.mobilelantern.hardware

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimerService @Inject constructor(
    private val sessionManager: SessionManager,
    private val hardwareController: HardwareController
) {
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private var timerJob: Job? = null
    private var lastAlertEmissionTime: Long = 0L
    
    /**
     * Inicia el temporizador para una sesión de pregunta
     */
    fun startTimer(questionId: Int, question: String) {
        stopTimer() // Detener cualquier timer anterior
        
        sessionManager.startQuestionSession(questionId, question)
        
        timerJob = coroutineScope.launch {
            val startTime = System.currentTimeMillis()
            
            while (isActive) {
                val currentTime = System.currentTimeMillis()
                val elapsedTime = currentTime - startTime
                
                sessionManager.updateElapsedTime(elapsedTime)
                
                val state = sessionManager.sessionState.value
                
                // Verificar alertas progresivas durante la fase de IA
                checkAIPhaseAlerts(state)
                
                // Log cada minuto para debugging
                if (elapsedTime % 60000 < 1000) {
                    val minutes = elapsedTime / 60000
                    android.util.Log.d("TimerService", "Session active for ${minutes} minutes, phase: ${state.currentPhase}, attempts: ${state.aiAttempts}")
                }
                
                delay(1000) // Actualizar cada segundo
            }
        }
    }
    
    /**
     * Verifica y activa alertas progresivas durante la fase de IA
     * - A los 3 minutos: inicia tintineo (alerta visual)
     * - A los 6 minutos: inicia alarma sonora (alerta acústica)
     * - La alarma sonora se repite con frecuencia creciente:
     *   - 1er minuto (6:00-7:00): cada 30 segundos
     *   - 2o minuto (7:00-8:00): cada 20 segundos
     *   - 3er minuto en adelante (8:00+): cada 10 segundos
     */
    private fun checkAIPhaseAlerts(state: SessionState) {
        if (state.currentPhase != SessionPhase.AI_ASSISTANCE_PHASE) {
            return
        }
        
        val aiPhaseElapsed = sessionManager.getAIPhaseElapsedTime()
        val threeMinutesMs = 3 * 60 * 1000L
        val sixMinutesMs = 6 * 60 * 1000L
        
        // A los 3 minutos: iniciar tintineo (alerta visual)
        if (aiPhaseElapsed >= threeMinutesMs && !state.isFlashingActive) {
            hardwareController.startFlashing()
            sessionManager.setFlashingActive(true)
            android.util.Log.d("TimerService", "Visual alert activated (flashing) at ${aiPhaseElapsed}ms")
        }
        
        // A los 6 minutos: iniciar alarma sonora (alerta acústica) con repeticiones progresivas
        if (aiPhaseElapsed >= sixMinutesMs) {
            if (!state.isAlertSoundActive) {
                // Primera activación de la alerta sonora
                hardwareController.emitAlertSound()
                sessionManager.setAlertSoundActive(true)
                sessionManager.setAlertSoundActivatedTime(System.currentTimeMillis())
                lastAlertEmissionTime = System.currentTimeMillis()
                android.util.Log.d("TimerService", "Sound alert activated at ${aiPhaseElapsed}ms")
            } else {
                // Repeticiones con frecuencia creciente
                val timeSinceActivation = sessionManager.getTimeSinceAlertActivated()
                val interval = when {
                    timeSinceActivation < 60_000 -> 30_000L  // 1er minuto: cada 30 segundos
                    timeSinceActivation < 120_000 -> 20_000L // 2o minuto: cada 20 segundos
                    else -> 10_000L                          // 3er minuto en adelante: cada 10 segundos
                }
                
                val timeSinceLastEmission = System.currentTimeMillis() - lastAlertEmissionTime
                if (timeSinceLastEmission >= interval) {
                    hardwareController.emitAlertSound()
                    lastAlertEmissionTime = System.currentTimeMillis()
                    val minutesSinceActivation = timeSinceActivation / 60_000
                    android.util.Log.d("TimerService", "Sound alert repeated at ${timeSinceActivation}ms since activation (minute ${minutesSinceActivation + 1}, interval: ${interval}ms)")
                }
            }
        }
    }
    
    /**
     * Marca el inicio de la fase colaborativa (vaso verde)
     */
    fun startCollaborativePhase() {
        hardwareController.turnOnWithColor(LedColor.GREEN)
        sessionManager.setFlashlightOn(true)
        android.util.Log.d("TimerService", "Collaborative phase started - LED GREEN")
    }
    
    /**
     * Procesa la respuesta colaborativa
     */
    fun processCollaborativeAnswer(isCorrect: Boolean) {
        sessionManager.setCollaborativeAnswerGiven(isCorrect)
        
        if (isCorrect) {
            endSession()
            android.util.Log.d("TimerService", "Collaborative answer correct - session ended")
        } else {
            hardwareController.turnOnWithColor(LedColor.RED)
            sessionManager.setFlashlightOn(true)
            android.util.Log.d("TimerService", "Collaborative answer incorrect - AI phase started with LED RED")
        }
    }
    
    /**
     * Procesa un intento con IA
     */
    fun processAIAttempt(isCorrect: Boolean) {
        sessionManager.incrementAIAttempts()
        
        if (isCorrect) {
            // Respuesta de IA satisfactoria: terminar sesión
            endSession()
            android.util.Log.d("TimerService", "AI answer satisfactory - session ended")
        } else {
            val attempts = sessionManager.sessionState.value.aiAttempts
            android.util.Log.d("TimerService", "AI attempt $attempts processed - continuing")
        }
    }
    
    /**
     * Marca la intervención del tutor
     */
    fun processTutorIntervention() {
        sessionManager.markTutorIntervention()
        endSession()
        android.util.Log.d("TimerService", "Tutor intervention - session ended")
    }
    
    /**
     * Detiene el temporizador
     */
    fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }
    
    /**
     * Termina la sesión actual
     */
    fun endSession() {
        stopTimer()
        hardwareController.turnOffFlashlight()
        sessionManager.endSession()
        lastAlertEmissionTime = 0L
        android.util.Log.d("TimerService", "Session ended - all hardware turned off")
    }
    
    /**
     * Obtiene el estado actual de la sesión
     */
    fun getCurrentSessionState(): SessionState {
        return sessionManager.sessionState.value
    }
    
    /**
     * Verifica si hay una sesión activa
     */
    fun isSessionActive(): Boolean {
        return sessionManager.sessionState.value.isActive
    }
}