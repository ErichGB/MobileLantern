package com.mssde.mobilelantern.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Response del endpoint /ia/question/{id} - devuelve array de interacciones
 */
typealias QuestionDetailResponse = List<QuestionInteractionDto>

/**
 * DTO individual de una interacción de pregunta
 */
data class QuestionInteractionDto(
    val id: Int,
    @SerializedName("id_user")
    val idUser: Int,
    val moment: String,
    val question: String? = null,
    val context: String? = null,
    val texto: String? = null,
    val rol: String? = null,
    val correct: Int = 0,
    val ia: Int = 0,
    val teacher: Int = 0
) 