package com.mssde.mobilelantern.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import com.mssde.mobilelantern.domain.model.AuthCredentials
import com.mssde.mobilelantern.domain.model.QRCredentials
import com.mssde.mobilelantern.domain.model.UiState
import com.mssde.mobilelantern.domain.repository.AuthRepository
import com.mssde.mobilelantern.ui.UiTextConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Initial)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val gson = Gson()
    private val qrContentMutex = Mutex()

    fun resetState() {
        _uiState.value = UiState.Initial
    }

    fun login(
        userName: String = "alumno",
        password: String = "alumno",
        context: String = "Actua como un profesor de ciclos formativos de grado superior. No des la respuesta directamente, da pinceladas de cómo hacerlo, con ejemplos. No pongas mucho texto, solo los conceptos clave.",
        aula: String? = null
    ) {
        viewModelScope.launch {
            executeLogin(userName, password, context, aula)
        }
    }

    fun loginWithQRContent(content: String) {
        viewModelScope.launch {
            qrContentMutex.withLock {
                if (_uiState.value == UiState.Loading) return@withLock
                try {
                    val trimmed = content.trim()
                    if (trimmed.startsWith("mobilelantern://")) {
                        parseUrlAndApply(Uri.parse(trimmed))
                    } else {
                        parseJsonAndApply(trimmed)
                    }
                } catch (e: Exception) {
                    _uiState.value = UiState.Error(
                        e.message?.let { "Error al procesar el código QR: $it" }
                            ?: "Error al procesar el código QR"
                    )
                }
            }
        }
    }

    fun loginWithDeepLink(uri: Uri) {
        viewModelScope.launch {
            qrContentMutex.withLock {
                try {
                    parseUrlAndApply(uri)
                } catch (e: IllegalArgumentException) {
                    _uiState.value = UiState.Error("URL inválida: ${e.message}")
                } catch (e: Exception) {
                    _uiState.value = UiState.Error(
                        e.message ?: "Error al procesar el enlace"
                    )
                }
            }
        }
    }

    private suspend fun parseJsonAndApply(jsonContent: String) {
        try {
            val jsonObject = gson.fromJson(jsonContent, JsonObject::class.java)
            val credentials = gson.fromJson(jsonContent, QRCredentials::class.java)
            if (credentials.userName.isBlank() || credentials.password.isBlank()) {
                throw IllegalArgumentException("Credenciales vacías")
            }
            val mandatoryFields = setOf("userName", "password", "context")
            val extraFields = mutableMapOf<String, String>()
            jsonObject.entrySet().forEach { (key, value) ->
                if (key !in mandatoryFields && value.isJsonPrimitive && value.asJsonPrimitive.isString) {
                    extraFields[key] = value.asString
                }
            }
            processCredentials(credentials.userName, credentials.password, credentials.context, extraFields)
        } catch (e: JsonSyntaxException) {
            _uiState.value = UiState.Error("Código QR inválido: formato JSON incorrecto")
        } catch (e: IllegalArgumentException) {
            _uiState.value = UiState.Error("Código QR inválido: credenciales vacías")
        }
    }

    private suspend fun parseUrlAndApply(uri: Uri) {
        val userName = uri.getQueryParameter("userName")
        val password = uri.getQueryParameter("password")
        val contextParam = uri.getQueryParameter("context")
        if (userName.isNullOrBlank() || password.isNullOrBlank()) {
            throw IllegalArgumentException("Credenciales vacías en URL")
        }
        val mandatoryFields = setOf("userName", "password", "context")
        val extraFields = mutableMapOf<String, String>()
        uri.queryParameterNames.forEach { paramName ->
            if (paramName !in mandatoryFields) {
                uri.getQueryParameter(paramName)?.let { value ->
                    extraFields[paramName] = value
                }
            }
        }
        processCredentials(userName, password, contextParam ?: "", extraFields)
    }

    private suspend fun processCredentials(
        userName: String,
        password: String,
        context: String,
        extraFields: Map<String, String>
    ) {
        UiTextConfig.clear()
        extraFields.forEach { (key, value) ->
            UiTextConfig.set(key, value)
        }
        val aula = extraFields["aula"]
        executeLogin(userName, password, context, aula)
    }

    private suspend fun executeLogin(
        userName: String,
        password: String,
        context: String,
        aula: String?
    ) {
        _uiState.value = UiState.Loading
        try {
            val credentials = AuthCredentials(userName, password)
            authRepository.login(credentials)
                .onSuccess {
                    authRepository.saveContext(context)
                    authRepository.saveAula(aula)
                    _uiState.value = UiState.Success("Login exitoso")
                }
                .onFailure {
                    _uiState.value = UiState.Error(it.message ?: "Error en el login")
                }
        } catch (e: Exception) {
            _uiState.value = UiState.Error(e.message ?: "Error inesperado")
        }
    }
}
