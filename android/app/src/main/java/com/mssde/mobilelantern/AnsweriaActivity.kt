package com.mssde.mobilelantern

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mssde.mobilelantern.ui.components.NewQuestionInputCard
import com.mssde.mobilelantern.ui.components.StandardQuestionCard
import com.mssde.mobilelantern.ui.components.CollaborativeAnswerCard
import com.mssde.mobilelantern.ui.components.AIResponseCard
import com.mssde.mobilelantern.ui.components.UserQuestionCard
import com.mssde.mobilelantern.ui.components.LoadingCard
import com.mssde.mobilelantern.ui.components.ConfirmationCard
import com.mssde.mobilelantern.ui.components.ErrorCard
import com.mssde.mobilelantern.ui.components.EmergencyMuteButton
import com.mssde.mobilelantern.ui.components.FlashIndicatorContent
import com.mssde.mobilelantern.hardware.FlashState
import com.mssde.mobilelantern.hardware.HardwareController
import com.mssde.mobilelantern.hardware.LedColor
import com.mssde.mobilelantern.hardware.TimerService
import com.mssde.mobilelantern.ui.components.TutorInterventionDialog
import com.mssde.mobilelantern.ui.components.PlaceGlassDialog
import com.mssde.mobilelantern.ui.components.GlassColor
import com.mssde.mobilelantern.ui.components.LanternType
import com.mssde.mobilelantern.ui.components.drawer.DrawerWrapper
import com.mssde.mobilelantern.ui.question.QuestionViewModel
import com.mssde.mobilelantern.ui.theme.MobileLanternTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

data class QAPair(
    val question: String,
    val answer: String,
    val isAIResponse: Boolean
)

@AndroidEntryPoint
class AnsweriaActivity : ComponentActivity() {
    @Inject
    lateinit var timerService: TimerService

    @Inject
    lateinit var hardwareController: HardwareController

    private val viewModel: QuestionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Obtener datos de la actividad anterior
        val question = intent.getStringExtra("question") ?: ""
        val answer = intent.getStringExtra("answer") ?: ""
        val questionId = intent.getIntExtra("questionId", 0)

        setContent {
            val uiState by viewModel.uiState.collectAsState()
            var showTutorDialog by remember { mutableStateOf(false) }
            var showAISuccessDialog by remember { mutableStateOf(false) }
            var showTutorSuccessDialog by remember { mutableStateOf(false) }

            // Efecto para hacer la petición automática a /ia/answeria cuando se inicia la actividad
            LaunchedEffect(Unit) {
                android.util.Log.d("AnsweriaActivity", "Iniciando petición automática a /ia/answeria")
                // Procesar primer intento con IA
                timerService.processAIAttempt(false)
                viewModel.submitAnsweria(
                    questionId = questionId,
                    answerText = answer,
                    isCorrect = false
                )
            }

            MobileLanternTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DrawerWrapper(
                        title = hardwareController.aiPhaseTitle(),
                        showBackButton = true,
                        onBackClick = {
                            startActivity(Intent(this@AnsweriaActivity, QuestionActivity::class.java))
                            finish()
                        },
                        onReconnectBluetooth = {
                            startActivity(Intent(this@AnsweriaActivity, LanternActivity::class.java))
                        },
                        onQuestionClick = { question ->
                            // Solo navegar si es una pregunta diferente a la actual
                            if (question.id != questionId) {
                                val intent = Intent(this@AnsweriaActivity, QuestionDetailActivity::class.java).apply {
                                    putExtra("questionId", question.id)
                                    putExtra("questionText", question.questionText)
                                }
                                startActivity(intent)
                            }
                            // Si es la misma pregunta, el drawer simplemente se cierra (comportamiento por defecto)
                        },
                        floatingActionButton = {
                            // Mostrar botón de tutor si hay respuesta de IA o si hay error
                            if (uiState.currentAnsweria != null || uiState.error != null) {
                                FloatingActionButton(
                                    onClick = { showTutorDialog = true },
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                    contentColor = MaterialTheme.colorScheme.onSecondary
                                ) {
                                    Text(
                                        text = "👨‍🏫",
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                }
                            }
                        }
                    ) { paddingValues ->
                        Box(modifier = Modifier.fillMaxSize()) {
                            AnsweriaContent(
                                initialQuestion = question,
                                initialAnswer = answer,
                                questionId = questionId,
                                onSubmitQuestion = { newQuestion ->
                                    // Procesar nuevo intento con IA
                                    timerService.processAIAttempt(false)
                                    viewModel.submitAnsweria(
                                        questionId = questionId,
                                        answerText = newQuestion,
                                        isCorrect = false
                                    )
                                },
                                onMarkCorrect = { answerText ->
                                    // Marcar respuesta de IA como correcta
                                    viewModel.submitAnsweria(
                                        questionId = questionId,
                                        answerText = answerText,
                                        isCorrect = true
                                    )
                                },
                                onBackClick = {
                                    // Procesar respuesta satisfactoria de IA
                                    timerService.processAIAttempt(true)
                                    // Mostrar diálogo de felicitación
                                    showAISuccessDialog = true
                                },
                                onRetry = {
                                    // Reintentar la petición inicial
                                    viewModel.clearError()
                                    timerService.processAIAttempt(false)
                                    viewModel.submitAnsweria(
                                        questionId = questionId,
                                        answerText = answer,
                                        isCorrect = false
                                    )
                                },
                                isLoading = uiState.isLoading,
                                error = uiState.error,
                                currentAnsweria = uiState.currentAnsweria,
                                modifier = Modifier.padding(paddingValues)
                            )
                            
                            // Botón de mute de emergencia (opuesto al FAB del tutor)
                            if (uiState.currentAnsweria != null || uiState.error != null) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(16.dp)
                                ) {
                                    EmergencyMuteButton()
                                }
                            }
                        }
                    }

                    // Mostrar diálogo de intervención del tutor
                    if (showTutorDialog) {
                        TutorInterventionDialog(
                            onDismiss = { showTutorDialog = false },
                            onConfirm = { interventionText ->
                                // Procesar intervención del tutor
                                timerService.processTutorIntervention()
                                viewModel.submitAnswerTeacher(questionId, interventionText)
                            },
                            isLoading = uiState.isLoading
                        )
                    }

                    // Efecto para manejar el resultado de la intervención del tutor
                    LaunchedEffect(uiState.currentAnswerTeacher) {
                        uiState.currentAnswerTeacher?.let {
                            android.util.Log.d("TutorIntervention", "Intervención guardada exitosamente: $it")
                            showTutorDialog = false
                            // Mostrar diálogo de felicitación
                            showTutorSuccessDialog = true
                        }
                    }

                    // Mostrar diálogo de éxito con IA
                    if (showAISuccessDialog) {
                        PlaceGlassDialog(
                            glassColor = GlassColor.AI_SUCCESS,
                            onConfirm = {
                                showAISuccessDialog = false
                                // Ir a QuestionActivity para iniciar un nuevo ciclo
                                startActivity(Intent(this@AnsweriaActivity, QuestionActivity::class.java))
                                finish()
                            }
                        )
                    }

                    // Mostrar diálogo de éxito con tutor
                    if (showTutorSuccessDialog) {
                        PlaceGlassDialog(
                            glassColor = GlassColor.TUTOR_SUCCESS,
                            onConfirm = {
                                showTutorSuccessDialog = false
                                // Ir a QuestionActivity para iniciar un nuevo ciclo
                                startActivity(Intent(this@AnsweriaActivity, QuestionActivity::class.java))
                                finish()
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // Si estamos saliendo de la actividad y hay una sesión activa, detener las alertas
        if (isFinishing && timerService.isSessionActive()) {
            android.util.Log.d("AnsweriaActivity", "Actividad finalizando, deteniendo alertas")
            timerService.endSession()
        }
    }
}

@Composable
fun AnsweriaContent(
    initialQuestion: String,
    initialAnswer: String,
    questionId: Int,
    onSubmitQuestion: (String) -> Unit,
    onMarkCorrect: (String) -> Unit,
    onBackClick: () -> Unit,
    onRetry: () -> Unit,
    isLoading: Boolean,
    error: String?,
    currentAnsweria: com.mssde.mobilelantern.data.model.AnsweriaResponse?,
    modifier: Modifier = Modifier
) {
    var showNewQuestionForm by remember { mutableStateOf(false) }
    var newQuestionText by remember { mutableStateOf("") }
    var qaHistory by remember { mutableStateOf(listOf(
        QAPair(initialQuestion, initialAnswer, false)
    )) }
    val scrollState = rememberScrollState()

    // Efecto para actualizar el historial cuando se recibe una respuesta de la IA
    LaunchedEffect(currentAnsweria) {
        currentAnsweria?.let { answeria ->
            // Solo agregar al historial si answerIA tiene valor (no es null)
            answeria.answerIA?.let { aiAnswer ->
                qaHistory = qaHistory + QAPair("", aiAnswer, true)
            }
        }
    }

    // Efecto para scroll automático cuando se muestra el formulario o los botones
    LaunchedEffect(showNewQuestionForm, qaHistory.lastOrNull()?.isAIResponse) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
            // Card de Pregunta Inicial (siempre se muestra)
            StandardQuestionCard(question = initialQuestion)

            // Card de Respuesta Colaborativa (siempre se muestra)
            CollaborativeAnswerCard(answer = initialAnswer)

            // Mostrar error si existe
            if (error != null && !isLoading) {
                ErrorCard(
                    title = "Error al consultar IA",
                    message = error,
                    onRetry = onRetry,
                    retryText = "Reintentar"
                )
            }

            // Indicador de carga mientras se hace la petición inicial a /ia/answeria
            if (isLoading && currentAnsweria == null) {
                LoadingCard(
                    title = "Consultando con la IA...",
                    message = "La IA está analizando tu respuesta y preparando una explicación detallada..."
                )
            }

            // Mostrar respuestas adicionales (IA o nuevas preguntas) solo si no hay error
            if (error == null) {
                qaHistory.drop(1).forEach { qaPair ->
                    if (qaPair.isAIResponse) {
                        AIResponseCard(
                            response = qaPair.answer
                        )
                    } else {
                        UserQuestionCard(
                            question = qaPair.question
                        )
                    }
                }
            }

            // Indicador de carga para preguntas subsiguientes (después del historial)
            if (isLoading && currentAnsweria != null) {
                LoadingCard(
                    title = "Procesando nueva pregunta...",
                    message = "La IA está procesando tu nueva pregunta..."
                )
            }

            // Mostrar formulario de nueva pregunta o respuesta de IA actual (solo si no hay error)
            if (showNewQuestionForm && error == null) {
                NewQuestionInputCard(
                    questionText = newQuestionText,
                    onQuestionChange = { newQuestionText = it },
                    onSubmit = {
                        onSubmitQuestion(newQuestionText)
                        // Agregar la nueva pregunta al historial
                        qaHistory = qaHistory + QAPair(newQuestionText, "", false)
                        newQuestionText = ""
                        showNewQuestionForm = false
                    },
                    isLoading = isLoading,
                    isValid = newQuestionText.isNotBlank()
                )
            }

        // Mostrar botones de confirmación solo para la última respuesta de IA (solo si no hay error)
        if (qaHistory.isNotEmpty() && qaHistory.last().isAIResponse && !showNewQuestionForm && error == null) {
            ConfirmationCard(
                question = "¿Has resuelto tu duda con la IA?",
                onConfirm = {
                    // Marcar respuesta de IA como correcta
                    currentAnsweria?.let { answeria ->
                        answeria.answerIA?.let { aiAnswer ->
                            onMarkCorrect(aiAnswer)
                        }
                    }
                    // Procesar respuesta satisfactoria de IA
                    // Nota: El TimerService será usado por la actividad padre
                    onBackClick()
                },
                onDeny = { showNewQuestionForm = true },
                infoText = "Si el profesor ha resuelto finalmente tu pregunta, pulsa el botón de la esquina inferior derecha para registrar su solución."
            )
        }
        
        // Espaciado al final para que el FAB no tape el contenido
        Spacer(modifier = Modifier.height(80.dp))
    }
}

// ============================================
// Previews
// ============================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PreviewPhaseTopBar(title: String, flashState: FlashState) {
    CenterAlignedTopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FlashIndicatorContent(
                    flashState = flashState,
                    lanternType = LanternType.NONE,
                    isBluetoothConnected = false
                )
                Text(
                    text = title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        navigationIcon = { },
        actions = { }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "Answeria AI phase + RED indicator")
@Composable
fun AnsweriaContentAIPhasePreview() {
    MobileLanternTheme {
        Scaffold(
            topBar = {
                PreviewPhaseTopBar(
                    title = "Tiempo de asistencia con IA",
                    flashState = FlashState.On(LedColor.RED)
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                AnsweriaContent(
                    initialQuestion = "No sé cómo hacer un diagrama de gantt",
                    initialAnswer = "Buscar en la documentación del curso",
                    questionId = 1,
                    onSubmitQuestion = {},
                    onMarkCorrect = {},
                    onBackClick = {},
                    onRetry = {},
                    isLoading = false,
                    error = null,
                    currentAnsweria = com.mssde.mobilelantern.data.model.AnsweriaResponse(
                        answerIA = "Un diagrama de Gantt organiza tareas en el tiempo.",
                        answer = emptyList()
                    )
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    EmergencyMuteButton()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "Answeria AI success dialog + OFF indicator")
@Composable
fun AnsweriaContentAISuccessPreview() {
    MobileLanternTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                topBar = {
                    PreviewPhaseTopBar(
                        title = "Tiempo de asistencia con IA",
                        flashState = FlashState.Off
                    )
                }
            ) { paddingValues ->
                AnsweriaContent(
                    initialQuestion = "No sé cómo hacer un diagrama de gantt",
                    initialAnswer = "Buscar en la documentación del curso",
                    questionId = 1,
                    onSubmitQuestion = {},
                    onMarkCorrect = {},
                    onBackClick = {},
                    onRetry = {},
                    isLoading = false,
                    error = null,
                    currentAnsweria = com.mssde.mobilelantern.data.model.AnsweriaResponse(
                        answerIA = "Un diagrama de Gantt organiza tareas en el tiempo.",
                        answer = emptyList()
                    ),
                    modifier = Modifier.padding(paddingValues)
                )
            }
            PlaceGlassDialog(
                glassColor = GlassColor.AI_SUCCESS,
                onConfirm = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AnsweriaContentInitialLoadingPreview() {
    MobileLanternTheme {
        AnsweriaContent(
            initialQuestion = "¿Cuál es la fórmula del área de un círculo?",
            initialAnswer = "El área es pi por radio al cuadrado",
            questionId = 1,
            onSubmitQuestion = {},
            onMarkCorrect = {},
            onBackClick = {},
            onRetry = {},
            isLoading = true,
            error = null,
            currentAnsweria = null
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AnsweriaContentWithAIResponsePreview() {
    MobileLanternTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            AnsweriaContent(
                initialQuestion = "¿Cómo se calcula el perímetro de un rectángulo?",
                initialAnswer = "Sumando todos los lados",
                questionId = 1,
                onSubmitQuestion = {},
                onMarkCorrect = {},
                onBackClick = {},
                onRetry = {},
                isLoading = false,
                error = null,
                currentAnsweria = com.mssde.mobilelantern.data.model.AnsweriaResponse(
                    answerIA = "# Explicación del perímetro\n\nEl perímetro de un rectángulo se calcula sumando la longitud de todos sus lados.\n\n## Fórmula\n\n**P = 2 × (base + altura)**\n\nDonde:\n- **base**: lado horizontal\n- **altura**: lado vertical\n\nPor ejemplo, si un rectángulo tiene base = 5 cm y altura = 3 cm:\n\nP = 2 × (5 + 3) = 2 × 8 = **16 cm**",
                    answer = emptyList()
                )
            )
            
            // Botón de mute visible en el preview
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                EmergencyMuteButton()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AnsweriaContentWithErrorPreview() {
    MobileLanternTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            AnsweriaContent(
                initialQuestion = "¿Qué es la fotosíntesis?",
                initialAnswer = "Es el proceso por el cual las plantas producen su alimento",
                questionId = 1,
                onSubmitQuestion = {},
                onMarkCorrect = {},
                onBackClick = {},
                onRetry = {},
                isLoading = false,
                error = "Error al conectar con el servidor de IA. Por favor, intenta nuevamente.",
                currentAnsweria = null
            )
            
            // Botón de mute visible en el preview (también aparece con error)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                EmergencyMuteButton()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AnsweriaContentWithMultipleQAPreview() {
    MobileLanternTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            AnsweriaContent(
                initialQuestion = "¿Cómo funciona el teorema de Pitágoras?",
                initialAnswer = "Se usa para calcular el lado de un triángulo",
                questionId = 1,
                onSubmitQuestion = {},
                onMarkCorrect = {},
                onBackClick = {},
                onRetry = {},
                isLoading = false,
                error = null,
                currentAnsweria = com.mssde.mobilelantern.data.model.AnsweriaResponse(
                    answerIA = "# Teorema de Pitágoras\n\n**a² + b² = c²**\n\nDonde:\n- **a** y **b**: catetos del triángulo rectángulo\n- **c**: hipotenusa (lado más largo)\n\nEste teorema se aplica solo en triángulos rectángulos (con un ángulo de 90°).",
                    answer = emptyList()
                )
            )
            
            // Botón de mute visible en el preview
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                EmergencyMuteButton()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TutorInterventionDialogPreview() {
    MobileLanternTheme {
        TutorInterventionDialog(
            onDismiss = {},
            onConfirm = {},
            isLoading = false
        )
    }
} 