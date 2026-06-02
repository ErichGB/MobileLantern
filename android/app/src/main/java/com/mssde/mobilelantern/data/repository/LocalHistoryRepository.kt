package com.mssde.mobilelantern.data.repository

import com.mssde.mobilelantern.data.local.dao.QuestionDao
import com.mssde.mobilelantern.data.local.dao.QuestionInteractionDao
import com.mssde.mobilelantern.data.local.entity.QuestionEntity
import com.mssde.mobilelantern.data.local.entity.QuestionInteractionEntity
import com.mssde.mobilelantern.data.mapper.toEntity
import com.mssde.mobilelantern.data.mapper.toHistoryQuestion
import com.mssde.mobilelantern.data.model.HistoryQuestion
import com.mssde.mobilelantern.data.remote.QuestionService
import com.mssde.mobilelantern.data.remote.dto.QuestionDto
import com.mssde.mobilelantern.data.remote.dto.QuestionInteractionDto
import com.mssde.mobilelantern.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalHistoryRepository @Inject constructor(
    private val questionDao: QuestionDao,
    private val questionInteractionDao: QuestionInteractionDao,
    private val questionService: QuestionService,
    private val authRepository: AuthRepository
) {
    
    /**
     * Obtiene preguntas locales como Flow para UI reactiva.
     * Si existe 'aula' persistida, obtiene todas las preguntas del aula.
     * De lo contrario, filtra por userId.
     */
    fun getQuestionsFlow(userId: Int): Flow<List<HistoryQuestion>> {
        return flow {
            val aula = authRepository.getStoredAula().firstOrNull()
            val aulaId = aula?.toIntOrNull()
            
            if (aulaId != null) {
                android.util.Log.d("LocalHistoryRepository", "getQuestionsFlow: Modo aula ($aulaId) - obteniendo todas las preguntas")
                questionDao.getAllQuestions()
                    .map { entities -> entities.map { it.toHistoryQuestion() } }
                    .collect { questions ->
                        emit(questions)
                    }
            } else {
                android.util.Log.d("LocalHistoryRepository", "getQuestionsFlow: Modo usuario - filtrando por userId=$userId")
                questionDao.getQuestionsByUser(userId)
                    .map { entities -> entities.map { it.toHistoryQuestion() } }
                    .collect { questions ->
                        emit(questions)
                    }
            }
        }
    }
    
    /**
     * Obtiene preguntas locales síncronamente.
     * Si existe 'aula' persistida, obtiene todas las preguntas del aula.
     * De lo contrario, filtra por userId.
     */
    suspend fun getQuestionsSync(userId: Int): List<HistoryQuestion> {
        val aula = authRepository.getStoredAula().firstOrNull()
        val aulaId = aula?.toIntOrNull()
        return if (aulaId != null) {
            android.util.Log.d("LocalHistoryRepository", "getQuestionsSync: Modo aula ($aulaId) - obteniendo todas las preguntas")
            questionDao.getAllQuestionsSync()
                .map { it.toHistoryQuestion() }
        } else {
            android.util.Log.d("LocalHistoryRepository", "getQuestionsSync: Modo usuario - filtrando por userId=$userId")
            questionDao.getQuestionsByUserSync(userId)
                .map { it.toHistoryQuestion() }
        }
    }
    
    /**
     * Sincroniza preguntas desde el servidor y las guarda localmente.
     * Si existe el campo 'aula' persistida, usa el endpoint /ia/questions/aula/{id},
     * de lo contrario usa el endpoint /ia/questions con el userId.
     */
    suspend fun syncQuestionsFromServer(userId: Int): Result<List<HistoryQuestion>> {
        return try {
            // Verificar si existe el campo 'aula' persistida
            val aula = authRepository.getStoredAula().firstOrNull()
            val aulaId = aula?.toIntOrNull()
            
            val questionsResponse = if (aulaId != null) {
                android.util.Log.d("LocalHistoryRepository", "Usando endpoint /ia/questions/aula/$aulaId")
                questionService.getQuestionsByAula(aulaId)
            } else {
                android.util.Log.d("LocalHistoryRepository", "Usando endpoint /ia/questions con userId=$userId")
                questionService.getQuestions(userId)
            }
            
            val entities = questionsResponse.map { it.toEntity() }
            
            // Guardar en base de datos local
            questionDao.insertQuestions(entities)
            
            // Retornar como modelos de dominio
            val historyQuestions = entities.map { it.toHistoryQuestion() }
            Result.success(historyQuestions)
        } catch (e: Exception) {
            android.util.Log.e("LocalHistoryRepository", "Error sincronizando preguntas: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Obtiene interacciones de una pregunta específica
     */
    fun getQuestionInteractionsFlow(questionId: Int): Flow<List<QuestionInteractionEntity>> {
        return questionInteractionDao.getInteractionsByQuestion(questionId)
    }
    
    /**
     * Obtiene interacciones de una pregunta específica síncronamente
     */
    suspend fun getQuestionInteractionsSync(questionId: Int): List<QuestionInteractionEntity> {
        return questionInteractionDao.getInteractionsByQuestionSync(questionId)
    }
    
    /**
     * Obtiene detalles de una pregunta específica
     * Primero verifica si existen localmente, si no hace fetch del servidor
     */
    suspend fun getQuestionDetails(questionId: Int): Result<List<QuestionInteractionEntity>> {
        return try {
            val hasLocalInteractions = questionInteractionDao.hasInteractionsForQuestion(questionId)
            
            if (hasLocalInteractions) {
                val localInteractions = questionInteractionDao.getInteractionsByQuestionSync(questionId)
                android.util.Log.d("LocalHistoryRepository", "Usando datos locales para pregunta $questionId: ${localInteractions.size} interacciones")
                Result.success(localInteractions)
            } else {
                android.util.Log.d("LocalHistoryRepository", "Obteniendo detalles desde servidor para pregunta $questionId")
                val serverResponse = questionService.getQuestionById(questionId)
                val entities = serverResponse.map { it.toEntity(questionId) }
                
                ensureQuestionExists(questionId, entities)
                
                questionInteractionDao.deleteInteractionsByQuestion(questionId)
                
                questionInteractionDao.insertInteractions(entities)
                
                android.util.Log.d("LocalHistoryRepository", "Guardadas ${entities.size} interacciones localmente para pregunta $questionId")
                Result.success(entities)
            }
        } catch (e: Exception) {
            android.util.Log.e("LocalHistoryRepository", "Error obteniendo detalles de pregunta $questionId: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Asegura que existe la pregunta principal en la tabla questions antes de insertar interacciones
     */
    private suspend fun ensureQuestionExists(questionId: Int, interactions: List<QuestionInteractionEntity>) {
        try {
            // Verificar si la pregunta ya existe
            val existingQuestion = questionDao.getQuestionById(questionId)
            
            if (existingQuestion == null) {
                // Crear pregunta principal basada en las interacciones
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
                android.util.Log.d("LocalHistoryRepository", "Pregunta principal $questionId creada: '$questionText'")
            }
        } catch (e: Exception) {
            android.util.Log.e("LocalHistoryRepository", "Error asegurando pregunta $questionId: ${e.message}")
            throw e
        }
    }
    
    /**
     * Guarda una nueva pregunta localmente
     */
    suspend fun saveQuestion(questionDto: QuestionDto) {
        try {
            val entity = questionDto.toEntity()
            questionDao.insertQuestion(entity)
            android.util.Log.d("LocalHistoryRepository", "Pregunta ${entity.id} guardada localmente")
        } catch (e: Exception) {
            android.util.Log.e("LocalHistoryRepository", "Error guardando pregunta: ${e.message}")
        }
    }
    
    /**
     * Guarda interacciones de una pregunta localmente
     */
    suspend fun saveQuestionInteractions(questionId: Int, interactions: List<QuestionInteractionDto>) {
        try {
            val entities = interactions.map { it.toEntity(questionId) }
            questionInteractionDao.deleteInteractionsByQuestion(questionId)
            questionInteractionDao.insertInteractions(entities)
            android.util.Log.d("LocalHistoryRepository", "Guardadas ${entities.size} interacciones para pregunta $questionId")
        } catch (e: Exception) {
            android.util.Log.e("LocalHistoryRepository", "Error guardando interacciones: ${e.message}")
        }
    }
    
    /**
     * Limpia el historial local de un usuario
     */
    suspend fun clearUserHistory(userId: Int) {
        try {
            questionDao.deleteQuestionsByUser(userId)
            android.util.Log.d("LocalHistoryRepository", "Historial local limpiado para usuario $userId")
        } catch (e: Exception) {
            android.util.Log.e("LocalHistoryRepository", "Error limpiando historial: ${e.message}")
        }
    }

    /**
     * Limpia el historial local considerando el modo activo.
     * Si existe aula persistida, se borra todo el historial (modo aula muestra todas las preguntas).
     * En caso contrario, se borran únicamente las preguntas del usuario indicado.
     * Las interacciones asociadas se eliminan en cascada por la FK.
     */
    suspend fun clearLocalHistory(userId: Int, aula: String?) {
        try {
            if (!aula.isNullOrBlank()) {
                questionDao.deleteAllQuestions()
                android.util.Log.d("LocalHistoryRepository", "Historial local limpiado por completo (modo aula=$aula)")
            } else {
                questionDao.deleteQuestionsByUser(userId)
                android.util.Log.d("LocalHistoryRepository", "Historial local limpiado para usuario $userId")
            }
        } catch (e: Exception) {
            android.util.Log.e("LocalHistoryRepository", "Error limpiando historial: ${e.message}")
        }
    }
}
