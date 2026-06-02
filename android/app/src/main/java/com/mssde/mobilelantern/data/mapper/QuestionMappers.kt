package com.mssde.mobilelantern.data.mapper

import com.mssde.mobilelantern.data.local.entity.QuestionEntity
import com.mssde.mobilelantern.data.local.entity.QuestionInteractionEntity
import com.mssde.mobilelantern.data.model.HistoryQuestion
import com.mssde.mobilelantern.data.model.QuestionPhase
import com.mssde.mobilelantern.data.remote.dto.QuestionDto
import com.mssde.mobilelantern.data.remote.dto.QuestionInteractionDto
import java.text.SimpleDateFormat
import java.util.*

/**
 * Mappers para conversión entre DTOs, Entidades y Modelos de dominio
 */

// DTO -> Entity
fun QuestionDto.toEntity(): QuestionEntity = QuestionEntity(
    id = id,
    idUser = idUser,
    moment = moment,
    question = question ?: "(Sin texto)",
    context = context,
    texto = texto,
    rol = rol,
    correct = correct,
    ia = ia,
    teacher = teacher
)

fun QuestionInteractionDto.toEntity(questionId: Int): QuestionInteractionEntity = QuestionInteractionEntity(
    id = 0, // Autoincrement
    questionId = questionId,
    idUser = idUser,
    moment = moment,
    question = question,
    context = context,
    texto = texto,
    rol = rol,
    correct = correct,
    ia = ia,
    teacher = teacher
)

// Entity -> Model
fun QuestionEntity.toHistoryQuestion(): HistoryQuestion {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val date = try {
        dateFormat.parse(moment) ?: Date()
    } catch (e: Exception) {
        Date()
    }
    
    return HistoryQuestion(
        id = id,
        questionText = question,
        timestamp = date,
        phase = when {
            teacher > 0 -> QuestionPhase.COMPLETED
            ia > 0 -> QuestionPhase.AI_RESPONSE
            correct > 0 -> QuestionPhase.ANSWERED
            else -> QuestionPhase.PENDING
        },
        isResolved = teacher > 0 || correct > 0
    )
}


