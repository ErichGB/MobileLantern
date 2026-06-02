package com.mssde.mobilelantern.data.model

import java.util.Date

// Enum para las diferentes fases donde se puede resolver una pregunta
enum class QuestionPhase(
    val displayName: String,
    val emoji: String,
    val color: String
) {
    PENDING("Pendiente", "🙋", "#4CAF50"),
    ANSWERED("Respuesta", "💡", "#FF9800"), 
    AI_RESPONSE("Consulta IA", "🤖", "#2196F3"),
    COMPLETED("Completada", "✅", "#8BC34A")
}

// Modelo para preguntas históricas en el drawer
data class HistoryQuestion(
    val id: Int,
    val questionText: String,
    val phase: QuestionPhase,
    val timestamp: Date,
    val isResolved: Boolean = false,
    val preview: String = questionText.take(60) + if (questionText.length > 60) "..." else ""
)

// Modelo para el usuario actual
data class CurrentUser(
    val id: Int,
    val name: String,
    val email: String,
    val avatar: String? = null,
    val questionsCount: Int = 0,
    val resolvedCount: Int = 0
)

// Modelo para estadísticas del usuario
data class UserStats(
    val totalQuestions: Int,
    val resolvedQuestions: Int,
    val pendingQuestions: Int,
    val successRate: Float = if (totalQuestions > 0) resolvedQuestions.toFloat() / totalQuestions else 0f
) 