package com.mssde.mobilelantern.data.repository

import com.mssde.mobilelantern.data.model.Answer
import com.mssde.mobilelantern.data.model.AnswerResponse
import com.mssde.mobilelantern.data.model.Question
import com.mssde.mobilelantern.data.model.QuestionResponse
import com.mssde.mobilelantern.data.model.Answeria
import com.mssde.mobilelantern.data.model.AnsweriaResponse
import com.mssde.mobilelantern.data.model.AnswerTeacher
import com.mssde.mobilelantern.data.remote.dto.QuestionDetailResponse
import kotlinx.coroutines.flow.Flow

interface QuestionRepository {
    suspend fun askQuestion(question: Question): Result<QuestionResponse>
    suspend fun getQuestionById(questionId: Int): Result<QuestionDetailResponse>
    suspend fun submitAnswer(answer: Answer): Result<List<AnswerResponse>>
    suspend fun submitAnsweria(answeria: Answeria): Result<AnsweriaResponse>
    suspend fun submitAnswerTeacher(answerTeacher: AnswerTeacher): Result<AnsweriaResponse>
} 