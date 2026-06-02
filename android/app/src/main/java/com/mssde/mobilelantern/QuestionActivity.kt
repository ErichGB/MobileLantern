package com.mssde.mobilelantern

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mssde.mobilelantern.hardware.HardwareController
import com.mssde.mobilelantern.hardware.TimerService
import com.mssde.mobilelantern.ui.components.FlashlightWarningDialog
import com.mssde.mobilelantern.ui.components.PlaceGlassDialog
import com.mssde.mobilelantern.ui.components.GlassColor
import com.mssde.mobilelantern.ui.components.QuestionInputCard
import com.mssde.mobilelantern.ui.components.drawer.DrawerWrapper
import com.mssde.mobilelantern.ui.question.QuestionViewModel
import com.mssde.mobilelantern.ui.theme.MobileLanternTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.runtime.collectAsState
import javax.inject.Inject

@AndroidEntryPoint
class QuestionActivity : ComponentActivity() {
    @Inject
    lateinit var timerService: TimerService
    
    @Inject
    lateinit var hardwareController: HardwareController
    
    private var showPlaceGlassDialog by mutableStateOf(false)
    private var showConfirmNavigationDialog by mutableStateOf(false)
    private var showFlashlightWarning by mutableStateOf(false)
    private var hasCameraPermission by mutableStateOf(false)
    private var pendingNavigationQuestion: com.mssde.mobilelantern.data.model.HistoryQuestion? by mutableStateOf(null)
    private var questionText by mutableStateOf("")
    private var currentInputText by mutableStateOf("")
    private val viewModel: QuestionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        hasCameraPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        
        if (hasCameraPermission) {
            showFlashlightWarning = hardwareController.checkFlashlightState()
        }

        setContent {
            val uiState by viewModel.uiState.collectAsState()
            val systemFlashlightState by hardwareController.systemFlashlightState.collectAsState()
            val lanternDriverError by hardwareController.lanternDriverError.collectAsStateWithLifecycle()

            LaunchedEffect(systemFlashlightState, hasCameraPermission) {
                if (hasCameraPermission && hardwareController.shouldWarnSystemFlashlight()) {
                    showFlashlightWarning = systemFlashlightState
                } else {
                    showFlashlightWarning = false
                }
            }

            LaunchedEffect(lanternDriverError) {
                lanternDriverError?.let {
                    Toast.makeText(
                        this@QuestionActivity,
                        "Conexión con la linterna perdida. Reconecta desde el menú.",
                        Toast.LENGTH_LONG
                    ).show()
                    hardwareController.clearLanternDriverError()
                }
            }

            MobileLanternTheme {
                if (showFlashlightWarning && hasCameraPermission && systemFlashlightState && hardwareController.shouldWarnSystemFlashlight()) {
                    FlashlightWarningDialog(isFlashlightOn = systemFlashlightState)
                }
                
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DrawerWrapper(
                        title = "Registrar pregunta",
                        showBackButton = true,
                        onBackClick = {
                            startActivity(Intent(this, WelcomeActivity::class.java))
                            finish()
                        },
                        onReconnectBluetooth = {
                            startActivity(Intent(this, LanternActivity::class.java))
                        },
                        onQuestionClick = { question ->
                            // Si hay texto en el input, mostrar diálogo de confirmación
                            if (currentInputText.isNotBlank()) {
                                pendingNavigationQuestion = question
                                showConfirmNavigationDialog = true
                            } else {
                                // Navegar directamente
                                val intent = Intent(this@QuestionActivity, QuestionDetailActivity::class.java).apply {
                                    putExtra("questionId", question.id)
                                    putExtra("questionText", question.questionText)
                                }
                                startActivity(intent)
                            }
                        }
                    ) { paddingValues ->
                        QuestionContent(
                            onRegisterClick = { 
                                questionText = it
                                viewModel.askQuestion(it)
                            },
                            onInputChange = { currentInputText = it },
                            isLoading = uiState.isLoading,
                            error = uiState.error,
                            modifier = Modifier.padding(paddingValues)
                        )
                    }

                    if (showPlaceGlassDialog) {
                        PlaceGlassDialog(
                            glassColor = GlassColor.GREEN,
                            onConfirm = {
                                showPlaceGlassDialog = false
                                val questionId = uiState.currentQuestion?.id ?: 0
                                // Iniciar temporizador y fase colaborativa
                                timerService.startTimer(questionId, questionText)
                                timerService.startCollaborativePhase()
                                
                                val intent = Intent(this, AnswerActivity::class.java).apply {
                                    putExtra("question", questionText)
                                    putExtra("questionId", questionId)
                                }
                                startActivity(intent)
                                finish()
                            }
                        )
                    }

                    // Diálogo de confirmación antes de navegar
                    if (showConfirmNavigationDialog) {
                        AlertDialog(
                            onDismissRequest = { showConfirmNavigationDialog = false },
                            title = { Text("¿Perder los cambios?") },
                            text = { 
                                Text("Tienes texto sin guardar. ¿Deseas descartarlo y continuar?") 
                            },
                            confirmButton = {
                                TextButton(
                                    onClick = {
                                        showConfirmNavigationDialog = false
                                        pendingNavigationQuestion?.let { question ->
                                            val intent = Intent(this@QuestionActivity, QuestionDetailActivity::class.java).apply {
                                                putExtra("questionId", question.id)
                                                putExtra("questionText", question.questionText)
                                            }
                                            startActivity(intent)
                                        }
                                    }
                                ) {
                                    Text("Descartar")
                                }
                            },
                            dismissButton = {
                                TextButton(
                                    onClick = { showConfirmNavigationDialog = false }
                                ) {
                                    Text("Cancelar")
                                }
                            }
                        )
                    }

                    // Mostrar error si existe
                    uiState.error?.let { error ->
                        LaunchedEffect(error) {
                            SnackbarHostState().showSnackbar(
                                message = error,
                                duration = SnackbarDuration.Short
                            )
                            viewModel.clearError()
                        }
                    }

                    LaunchedEffect(uiState.currentQuestion) {
                        uiState.currentQuestion?.let {
                            if (!hardwareController.requiresPhysicalGlassChange) {
                                val questionId = uiState.currentQuestion?.id ?: 0
                                timerService.startTimer(questionId, questionText)
                                timerService.startCollaborativePhase()
                                val intent = Intent(this@QuestionActivity, AnswerActivity::class.java).apply {
                                    putExtra("question", questionText)
                                    putExtra("questionId", questionId)
                                }
                                startActivity(intent)
                                finish()
                            } else {
                                showPlaceGlassDialog = true
                            }
                        }
                    }
                }
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        hasCameraPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (hasCameraPermission && hardwareController.shouldWarnSystemFlashlight()) {
            showFlashlightWarning = hardwareController.checkFlashlightState()
        }
    }
}

@Composable
fun QuestionContent(
    onRegisterClick: (String) -> Unit,
    onInputChange: (String) -> Unit = {},
    isLoading: Boolean,
    error: String?,
    modifier: Modifier = Modifier
) {
    var questionText by remember { mutableStateOf("") }
    var isQuestionValid by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        QuestionInputCard(
            questionText = questionText,
            onQuestionChange = { 
                questionText = it
                isQuestionValid = it.isNotBlank()
                onInputChange(it)
            },
            onSubmit = { onRegisterClick(questionText) },
            isLoading = isLoading,
            isValid = isQuestionValid
        )
    }
}

// ============================================
// Previews
// ============================================

@Preview(showBackground = true)
@Composable
fun QuestionContentPreview() {
    MobileLanternTheme {
        QuestionContent(
            onRegisterClick = {},
            isLoading = false,
            error = null
        )
    }
}

@Preview(showBackground = true)
@Composable
fun QuestionContentWithTextPreview() {
    MobileLanternTheme {
        QuestionContent(
            onRegisterClick = {},
            isLoading = false,
            error = null
        )
    }
}

@Preview(showBackground = true)
@Composable
fun QuestionContentLoadingPreview() {
    MobileLanternTheme {
        QuestionContent(
            onRegisterClick = {},
            isLoading = true,
            error = null
        )
    }
}

@Preview(showBackground = true)
@Composable
fun QuestionContentWithErrorPreview() {
    MobileLanternTheme {
        QuestionContent(
            onRegisterClick = {},
            isLoading = false,
            error = "Error al registrar la pregunta. Por favor, intenta de nuevo."
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PlaceGreenGlassDialogPreview() {
    MobileLanternTheme {
        PlaceGlassDialog(
            glassColor = GlassColor.GREEN,
            onConfirm = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ConfirmNavigationDialogPreview() {
    MobileLanternTheme {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("¿Perder los cambios?") },
            text = { 
                Text("Tienes texto sin guardar. ¿Deseas descartarlo y continuar?") 
            },
            confirmButton = {
                TextButton(onClick = {}) {
                    Text("Descartar")
                }
            },
            dismissButton = {
                TextButton(onClick = {}) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Preview(showBackground = true, name = "Modal linterna encendida")
@Composable
fun QuestionActivityWithFlashlightWarningPreview() {
    MobileLanternTheme {
        FlashlightWarningDialog(isFlashlightOn = true)
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            QuestionContent(
                onRegisterClick = {},
                isLoading = false,
                error = null
            )
        }
    }
}