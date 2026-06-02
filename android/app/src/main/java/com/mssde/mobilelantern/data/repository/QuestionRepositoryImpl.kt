package com.mssde.mobilelantern.data.repository

import com.mssde.mobilelantern.data.model.Answer
import com.mssde.mobilelantern.data.model.AnswerResponse
import com.mssde.mobilelantern.data.model.Answeria
import com.mssde.mobilelantern.data.model.AnsweriaResponse
import com.mssde.mobilelantern.data.model.AnswerTeacher
import com.mssde.mobilelantern.data.model.Question
import com.mssde.mobilelantern.data.model.QuestionResponse
import com.mssde.mobilelantern.data.remote.QuestionService
import com.mssde.mobilelantern.data.remote.dto.QuestionDetailResponse
import javax.inject.Inject

class QuestionRepositoryImpl @Inject constructor(
    private val questionService: QuestionService
) : QuestionRepository {

    override suspend fun askQuestion(question: Question): Result<QuestionResponse> {
        return try {
            val response = questionService.askQuestion(question)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getQuestionById(questionId: Int): Result<QuestionDetailResponse> {
        return try {
            // Funcionalidad simplificada - se completará más tarde

            Result.failure(Exception("Funcionalidad pendiente de implementar"))
        } catch (e: Exception) {
            android.util.Log.e("QuestionRepository", "Error obteniendo pregunta $questionId: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun submitAnswer(answer: Answer): Result<List<AnswerResponse>> {
        return try {
            val response = questionService.submitAnswer(answer)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun submitAnsweria(answeria: Answeria): Result<AnsweriaResponse> {
        return try {
            val response = questionService.submitAnsweria(answeria)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun submitAnswerTeacher(answerTeacher: AnswerTeacher): Result<AnsweriaResponse> {
        return try {
            val response = questionService.submitAnswerTeacher(answerTeacher)
            Result.success(response)
        } catch (e: Exception) {
            android.util.Log.e("QuestionRepository", "Error enviando intervención del tutor: ${e.message}")
            Result.failure(e)
        }
    }
} 
