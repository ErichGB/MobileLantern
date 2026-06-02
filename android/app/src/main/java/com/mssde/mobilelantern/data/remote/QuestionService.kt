package com.mssde.mobilelantern.data.remote

import com.mssde.mobilelantern.data.model.Answer
import com.mssde.mobilelantern.data.model.AnswerResponse
import com.mssde.mobilelantern.data.model.Answeria
import com.mssde.mobilelantern.data.model.AnsweriaResponse
import com.mssde.mobilelantern.data.model.AnswerTeacher
import com.mssde.mobilelantern.data.model.Question
import com.mssde.mobilelantern.data.model.QuestionResponse
import com.mssde.mobilelantern.data.remote.dto.QuestionDetailResponse
import com.mssde.mobilelantern.data.remote.dto.QuestionsResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface QuestionService {
    @POST("ia/question")
    suspend fun askQuestion(@Body question: Question): QuestionResponse

    @GET("ia/question/{id}")
    suspend fun getQuestionById(@Path("id") questionId: Int): QuestionDetailResponse

    @POST("ia/answer")
    suspend fun submitAnswer(@Body answer: Answer): List<AnswerResponse>

    @POST("ia/answeria")
    suspend fun submitAnsweria(@Body answeria: Answeria): AnsweriaResponse

    @POST("ia/answerteacher")
    suspend fun submitAnswerTeacher(@Body answerTeacher: AnswerTeacher): AnsweriaResponse

    // GET con query parameter
    @GET("ia/questions")
    suspend fun getQuestions(@Query("id_user") userId: Int): QuestionsResponse

    // GET preguntas por aula
    @GET("ia/questions/aula/{id}")
    suspend fun getQuestionsByAula(@Path("id") aulaId: Int): QuestionsResponse
} 
