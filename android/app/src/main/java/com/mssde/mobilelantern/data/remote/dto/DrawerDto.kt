package com.mssde.mobilelantern.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Response del endpoint /ia/questions - devuelve array directo
 */
typealias QuestionsResponse = List<QuestionDto>

/**
 * DTO individual de una pregunta desde la API (historial)
 */
data class QuestionDto(
    val id: Int,
    val question: String? = null,
    @SerializedName("id_user")
    val idUser: Int,
    val moment: String, // API usa "moment" no "timestamp"
    val context: String? = null,
    val texto: String? = null,
    val rol: String? = null,
    val correct: Int = 0,
    val ia: Int = 0,
    val teacher: Int = 0
) 
