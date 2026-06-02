package com.mssde.mobilelantern.domain.usecase

import com.mssde.mobilelantern.data.local.dao.QuestionDao
import com.mssde.mobilelantern.data.local.dao.QuestionInteractionDao
import com.mssde.mobilelantern.data.local.entity.QuestionEntity
import com.mssde.mobilelantern.data.mapper.toEntity
import com.mssde.mobilelantern.data.remote.QuestionService
import javax.inject.Inject

class RefreshQuestionDetailsUseCase @Inject constructor(
    private val questionService: QuestionService,
    private val questionInteractionDao: QuestionInteractionDao,
    private val questionDao: QuestionDao
) {
    
    suspend operator fun invoke(questionId: Int): Result<Unit> {
        return try {
            val serverResponse = questionService.getQuestionById(questionId)
            val entities = serverResponse.map { it.toEntity(questionId) }
            
            ensureQuestionExists(questionId, entities)
            
            questionInteractionDao.deleteInteractionsByQuestion(questionId)
            
            questionInteractionDao.insertInteractions(entities)
            
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("RefreshQuestionDetails", "Error: ${e.message}")
            Result.failure(e)
        }
    }
    
    private suspend fun ensureQuestionExists(questionId: Int, interactions: List<com.mssde.mobilelantern.data.local.entity.QuestionInteractionEntity>) {
        val existingQuestion = questionDao.getQuestionById(questionId)
        
        if (existingQuestion == null) {
            val userInteraction = interactions.find { it.rol == "user" && !it.texto.isNullOrBlank() }
            val questionText = userInteraction?.texto ?: "Pregunta sin texto"
            val moment = interactions.firstOrNull()?.moment ?: ""
            val idUser = interactions.firstOrNull()?.idUser ?: 0
            
            val questionEntity = QuestionEntity(
                id = questionId,
                idUser = idUser,
                moment = moment,
                question = questionText,
                context = null,
                texto = null,
                rol = null,
                correct = 0,
                ia = 0,
                teacher = 0,
                createdAt = System.currentTimeMillis()
            )
            
            questionDao.insertQuestion(questionEntity)
        }
    }
}

