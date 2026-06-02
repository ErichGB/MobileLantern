package com.mssde.mobilelantern.data.repository

import com.mssde.mobilelantern.data.mapper.toHistoryQuestion
import com.mssde.mobilelantern.data.model.HistoryQuestion
import com.mssde.mobilelantern.data.model.QuestionPhase
import com.mssde.mobilelantern.data.remote.QuestionService
import com.mssde.mobilelantern.domain.repository.AuthRepository
import com.mssde.mobilelantern.domain.repository.DrawerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DrawerRepositoryImpl @Inject constructor(
    private val questionService: QuestionService,
    private val authRepository: AuthRepository,
    private val localHistoryRepository: LocalHistoryRepository
) : DrawerRepository {

    override suspend fun getQuestions(): Result<List<HistoryQuestion>> {
        return try {
            val storedUserId = authRepository.getStoredUserId().firstOrNull() ?: 0
            
            // Primero intentar obtener datos locales
            val localQuestions = localHistoryRepository.getQuestionsSync(storedUserId)
            
            if (localQuestions.isNotEmpty()) {
                // Si hay datos locales, devolverlos
                Result.success(localQuestions)
            } else {
                // Si no hay datos locales, sincronizar desde servidor
                localHistoryRepository.syncQuestionsFromServer(storedUserId)
            }
        } catch (e: Exception) {
            android.util.Log.e("DrawerRepository", "Error obteniendo historial: ${e.message}")
            Result.failure(e)
        }
    }

    override fun getQuestionsFlow(): Flow<List<HistoryQuestion>> {
        return kotlinx.coroutines.flow.flow {
            val storedUserId = authRepository.getStoredUserId().firstOrNull() ?: 0
            localHistoryRepository.getQuestionsFlow(storedUserId).collect { questions ->
                emit(questions)
            }
        }
    }

    override fun filterQuestions(questions: List<HistoryQuestion>, query: String): List<HistoryQuestion> {
        if (query.isBlank()) return questions
        return questions.filter { 
            it.questionText.contains(query, ignoreCase = true) 
        }
    }
} 
