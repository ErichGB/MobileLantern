package com.mssde.mobilelantern.ui.question

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mssde.mobilelantern.data.model.Answer
import com.mssde.mobilelantern.data.model.AnswerResponse
import com.mssde.mobilelantern.data.model.Answeria
import com.mssde.mobilelantern.data.model.AnsweriaResponse
import com.mssde.mobilelantern.data.model.AnswerTeacher
import com.mssde.mobilelantern.data.model.Question
import com.mssde.mobilelantern.data.model.QuestionResponse
import com.mssde.mobilelantern.data.repository.QuestionRepository
import com.mssde.mobilelantern.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuestionUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentQuestion: QuestionResponse? = null,
    val currentAnswer: AnswerResponse? = null,
    val currentAnsweria: AnsweriaResponse? = null,
    val currentAnswerTeacher: AnsweriaResponse? = null
)

@HiltViewModel
class QuestionViewModel @Inject constructor(
    private val questionRepository: QuestionRepository,
    private val authRepository: AuthRepository,
    private val localHistoryRepository: com.mssde.mobilelantern.data.repository.LocalHistoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuestionUiState())
    val uiState: StateFlow<QuestionUiState> = _uiState.asStateFlow()

    fun askQuestion(questionText: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val userId = authRepository.getStoredUserId().first() ?: 0
                val question = Question(
                    question = questionText,
                    context = authRepository.getStoredContext().first() ?: "",
                    idUser = userId
                )
                
                questionRepository.askQuestion(question)
                    .onSuccess { response ->
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                currentQuestion = response
                            )
                        }
                        
                        // Guardar la nueva pregunta en el historial local
                        saveQuestionToLocalHistory(response)
                    }
                    .onFailure { error ->
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = error.message ?: "Error al enviar la pregunta"
                            )
                        }
                    }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error al obtener el contexto"
                    )
                }
            }
        }
    }

    fun submitAnswer(questionId: Int, answerText: String, isCorrect: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val userId = authRepository.getStoredUserId().first() ?: 0
            val answer = Answer(
                idQuestion = questionId,
                idUser = userId,
                answer = answerText,
                correct = isCorrect
            )
            
            questionRepository.submitAnswer(answer)
                .onSuccess { response ->
                    // Extraer la respuesta más reciente del array
                    val latestAnswer = response.maxByOrNull { it.id }
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            currentAnswer = latestAnswer
                        )
                    }
                    
                    // Guardar interacciones en historial local
                    saveAnswerInteractionsToLocalHistory(questionId, response)
                }
                .onFailure { error ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Error al enviar la respuesta"
                        )
                    }
                }
        }
    }

    fun submitAnsweria(questionId: Int, answerText: String, isCorrect: Boolean) {
        android.util.Log.d("QuestionViewModel", "submitAnsweria llamado: questionId=$questionId, answerText=$answerText, isCorrect=$isCorrect")
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val userId = authRepository.getStoredUserId().first() ?: 0
            val answeria = Answeria(
                idQuestion = questionId,
                idUser = userId,
                answer = answerText,
                correct = isCorrect
            )
            
            android.util.Log.d("QuestionViewModel", "Enviando request a /ia/answeria con datos: $answeria")
            
            questionRepository.submitAnsweria(answeria)
                .onSuccess { response ->
                    android.util.Log.d("QuestionViewModel", "Respuesta de /ia/answeria recibida: $response")
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            currentAnsweria = response
                        )
                    }
                }
                .onFailure { error ->
                    android.util.Log.e("QuestionViewModel", "Error en /ia/answeria: ${error.message}", error)
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Error al enviar la respuesta a la IA"
                        )
                    }
                }
        }
    }

    fun submitAnswerTeacher(questionId: Int, interventionText: String) {
        android.util.Log.d("QuestionViewModel", "submitAnswerTeacher llamado: questionId=$questionId, interventionText=$interventionText")
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val userId = authRepository.getStoredUserId().first() ?: 0
            val answerTeacher = AnswerTeacher(
                idQuestion = questionId,
                idUser = userId,
                answer = interventionText,
                correct = true
            )
            
            android.util.Log.d("QuestionViewModel", "Enviando request a /ia/answerteacher con datos: $answerTeacher")
            
            questionRepository.submitAnswerTeacher(answerTeacher)
                .onSuccess { response ->
                    android.util.Log.d("QuestionViewModel", "Respuesta de /ia/answerteacher recibida: $response")
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            currentAnswerTeacher = response,
                            error = null  // Limpiar cualquier error previo
                        )
                    }
                }
                .onFailure { error ->
                    android.util.Log.e("QuestionViewModel", "Error en /ia/answerteacher: ${error.message}", error)
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Error al guardar la intervención del tutor: ${error.message}"
                        )
                    }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    /**
     * Guarda una nueva pregunta en el historial local para que aparezca en el drawer
     */
    private fun saveQuestionToLocalHistory(questionResponse: QuestionResponse) {
        viewModelScope.launch {
            try {
                // Convertir QuestionResponse a QuestionDto para guardarlo
                val questionDto = com.mssde.mobilelantern.data.remote.dto.QuestionDto(
                    id = questionResponse.id,
                    idUser = questionResponse.idUser,
                    moment = questionResponse.timestamp ?: getCurrentTimestamp(),
                    question = questionResponse.question,
                    context = null,
                    texto = null,
                    rol = null,
                    correct = 0,
                    ia = 0,
                    teacher = 0
                )
                
                // Guardar en historial local
                localHistoryRepository.saveQuestion(questionDto)
                android.util.Log.d("QuestionViewModel", "Pregunta ${questionResponse.id} guardada en historial local")
            } catch (e: Exception) {
                android.util.Log.e("QuestionViewModel", "Error guardando pregunta en historial local: ${e.message}")
            }
        }
    }
    
    /**
     * Guarda las interacciones de respuesta colaborativa en el historial local
     */
    private fun saveAnswerInteractionsToLocalHistory(questionId: Int, answers: List<AnswerResponse>) {
        viewModelScope.launch {
            try {
                // Convertir AnswerResponse a QuestionInteractionDto
                val interactions = answers.map { answer ->
                    com.mssde.mobilelantern.data.remote.dto.QuestionInteractionDto(
                        id = answer.id,
                        idUser = answer.idUser,
                        moment = answer.timestamp ?: getCurrentTimestamp(),
                        question = null,
                        context = null,
                        texto = answer.answer,
                        rol = "user", // Respuesta colaborativa del usuario
                        correct = if (answer.correct) 1 else 0,
                        ia = 0,
                        teacher = 0
                    )
                }
                
                localHistoryRepository.saveQuestionInteractions(questionId, interactions)
                android.util.Log.d("QuestionViewModel", "Guardadas ${interactions.size} interacciones de respuesta para pregunta $questionId")
            } catch (e: Exception) {
                android.util.Log.e("QuestionViewModel", "Error guardando interacciones de respuesta: ${e.message}")
            }
        }
    }
    
    /**
     * Genera un timestamp en el formato esperado por la API
     */
    private fun getCurrentTimestamp(): String {
        return java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
    }
} 