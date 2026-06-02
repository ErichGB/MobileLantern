package com.mssde.mobilelantern.data.model

import com.google.gson.annotations.SerializedName

data class Question(
    val id: Int = 0,
    val question: String,
    val context: String,
    @SerializedName("id_user")
    val idUser: Int = 0
)

data class QuestionResponse(
    val id: Int,
    val question: String,
    val idUser: Int,
    val timestamp: String
)

data class Answer(
    @SerializedName("id_question")
    val idQuestion: Int,
    @SerializedName("id_user")
    val idUser: Int,
    val answer: String,
    val correct: Boolean
)

data class AnswerResponse(
    val id: Int,
    val idQuestion: Int,
    val idUser: Int,
    val answer: String,
    val correct: Boolean,
    val timestamp: String
)

data class Answeria(
    @SerializedName("id_question")
    val idQuestion: Int,
    @SerializedName("id_user")
    val idUser: Int,
    val answer: String,
    val correct: Boolean
)

data class AnsweriaResponse(
    val answerIA: String?,
    val answer: List<AnswerResponse>
)

data class AnswerTeacher(
    @SerializedName("id_question")
    val idQuestion: Int,
    @SerializedName("id_user")
    val idUser: Int,
    val answer: String,
    val correct: Boolean
)

data class AnswerTeacherResponse(
    val id: Int,
    val idQuestion: Int,
    val idUser: Int,
    val answer: String,
    val correct: Boolean,
    val timestamp: String
) 