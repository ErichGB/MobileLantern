package com.mssde.mobilelantern.domain.repository

import com.mssde.mobilelantern.data.model.HistoryQuestion
import kotlinx.coroutines.flow.Flow

interface DrawerRepository {
    /**
     * Obtiene la lista de preguntas del usuario desde la API
     */
    suspend fun getQuestions(): Result<List<HistoryQuestion>>
    
    /**
     * Obtiene las preguntas como Flow para actualizaciones reactivas
     */
    fun getQuestionsFlow(): Flow<List<HistoryQuestion>>
    
    /**
     * Filtra preguntas por texto de búsqueda
     */
    fun filterQuestions(questions: List<HistoryQuestion>, query: String): List<HistoryQuestion>
} 