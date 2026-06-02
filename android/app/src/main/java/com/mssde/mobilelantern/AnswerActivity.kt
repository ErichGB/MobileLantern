package com.mssde.mobilelantern

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
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
import com.mssde.mobilelantern.hardware.FlashState
import com.mssde.mobilelantern.hardware.LedColor
import com.mssde.mobilelantern.ui.components.FlashIndicatorContent
import com.mssde.mobilelantern.ui.components.LanternType
import com.mssde.mobilelantern.hardware.HardwareController
import com.mssde.mobilelantern.hardware.TimerService
import com.mssde.mobilelantern.ui.components.PlaceGlassDialog
import com.mssde.mobilelantern.ui.components.GlassColor
import com.mssde.mobilelantern.ui.components.StandardQuestionCard
import com.mssde.mobilelantern.ui.components.AnswerInputCard
import com.mssde.mobilelantern.ui.components.drawer.DrawerWrapper
import com.mssde.mobilelantern.ui.question.QuestionViewModel
import com.mssde.mobilelantern.ui.theme.MobileLanternTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import javax.inject.Inject

@AndroidEntryPoint
class AnswerActivity : ComponentActivity() {
    @Inject
    lateinit var timerService: TimerService

    @Inject
    lateinit var hardwareController: HardwareController

    private var preserveSessionOnStop = false
    private var showPlaceRedGlassDialog by mutableStateOf(false)
    private var showCongratulationsDialog by mutableStateOf(false)
    private val viewModel: QuestionViewModel by viewModels()
    private var originalAnswer by mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Simulamos que la pregunta viene de la actividad anterior
        val question = intent.getStringExtra("question") ?: ""
        val questionId = intent.getIntExtra("questionId", 0)

        setContent {
            val uiState by viewModel.uiState.collectAsState()
            val lanternDriverError by hardwareController.lanternDriverError.collectAsStateWithLifecycle()

            LaunchedEffect(lanternDriverError) {
                lanternDriverError?.let {
                    Toast.makeText(
                        this@AnswerActivity,
                        "Conexión con la linterna perdida. Reconecta desde el menú.",
                        Toast.LENGTH_LONG
                    ).show()
                    hardwareController.clearLanternDriverError()
                }
            }

            MobileLanternTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DrawerWrapper(
                        title = hardwareController.collaborativePhaseTitle(),
                        showBackButton = true,
                        onBackClick = {
                            val intent = Intent(this@AnswerActivity, QuestionActivity::class.java)
                            startActivity(intent)
                            finish()
                        },
                        onReconnectBluetooth = {
                            startActivity(Intent(this@AnswerActivity, LanternActivity::class.java))
                        },
                        onQuestionClick = { question ->
                            // Solo navegar si es una pregunta diferente a la actual
                            if (question.id != questionId) {
                                val intent = Intent(this@AnswerActivity, QuestionDetailActivity::class.java).apply {
                                    putExtra("questionId", question.id)
                                    putExtra("questionText", question.questionText)
                                }
                                startActivity(intent)
                            }
                            // Si es la misma pregunta, el drawer simplemente se cierra (comportamiento por defecto)
                        }
                    ) { paddingValues ->
                        AnswerContent(
                            question = question,
                            onRegisterClick = { answer, isCorrect ->
                                originalAnswer = answer // Guardar la respuesta original
                                viewModel.submitAnswer(
                                    questionId = questionId,
                                    answerText = answer,
                                    isCorrect = isCorrect
                                )
                            },
                            isLoading = uiState.isLoading,
                            error = uiState.error,
                            modifier = Modifier.padding(paddingValues)
                        )
                    }

                    if (showPlaceRedGlassDialog) {
                        PlaceGlassDialog(
                            glassColor = GlassColor.RED,
                            onConfirm = {
                                showPlaceRedGlassDialog = false
                                preserveSessionOnStop = true
                                val intent = Intent(this@AnswerActivity, AnsweriaActivity::class.java).apply {
                                    putExtra("question", question)
                                    putExtra("answer", originalAnswer)
                                    putExtra("questionId", questionId)
                                }
                                startActivity(intent)
                                finish()
                            }
                        )
                    }

                    if (showCongratulationsDialog) {
                        PlaceGlassDialog(
                            glassColor = GlassColor.CONGRATULATIONS,
                            onConfirm = {
                                showCongratulationsDialog = false
                                // Ir a QuestionActivity para iniciar un nuevo ciclo sin re-login
                                val intent = Intent(this@AnswerActivity, QuestionActivity::class.java)
                                startActivity(intent)
                                finish()
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

                    LaunchedEffect(uiState.currentAnswer) {
                        uiState.currentAnswer?.let { answer ->
                            android.util.Log.d("AnswerActivity", "Respuesta registrada: correct=${answer.correct}, idQuestion=${answer.idQuestion}")
                            timerService.processCollaborativeAnswer(answer.correct)

                            if (answer.correct) {
                                showCongratulationsDialog = true
                            } else if (!hardwareController.requiresPhysicalGlassChange) {
                                preserveSessionOnStop = true
                                val intent = Intent(this@AnswerActivity, AnsweriaActivity::class.java).apply {
                                    putExtra("question", question)
                                    putExtra("answer", originalAnswer)
                                    putExtra("questionId", questionId)
                                }
                                startActivity(intent)
                                finish()
                            } else {
                                showPlaceRedGlassDialog = true
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // Si estamos saliendo de la actividad y hay una sesión activa, detener las alertas
        if (isFinishing && timerService.isSessionActive() && !preserveSessionOnStop) {
            android.util.Log.d("AnswerActivity", "Actividad finalizando, deteniendo alertas")
            timerService.endSession()
        }
        preserveSessionOnStop = false
    }
}

@Composable
fun AnswerContent(
    question: String,
    onRegisterClick: (String, Boolean) -> Unit,
    isLoading: Boolean,
    error: String?,
    modifier: Modifier = Modifier
) {
    var answerText by remember { mutableStateOf("") }
    var isCorrect by remember { mutableStateOf(false) }
     var isAnswerValid by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Pregunta
        StandardQuestionCard(
            question = question,
            isExpandable = false
        )

        // Respuesta
        AnswerInputCard(
            answerText = answerText,
            onAnswerChange = { 
                answerText = it
                isAnswerValid = it.isNotBlank()
            },
            onSubmit = { onRegisterClick(answerText, isCorrect) },
            isLoading = isLoading,
            isValid = isAnswerValid,
            isCorrect = isCorrect,
            onCorrectChange = { isCorrect = it }
        )
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
@Preview(showBackground = true, name = "Answer collaborative phase + GREEN indicator")
@Composable
fun AnswerContentCollaborativePhasePreview() {
    MobileLanternTheme {
        Scaffold(
            topBar = {
                PreviewPhaseTopBar(
                    title = "Tiempo de colaboración",
                    flashState = FlashState.On(LedColor.GREEN)
                )
            }
        ) { paddingValues ->
            AnswerContent(
                question = "No sé cómo hacer un diagrama de gantt",
                onRegisterClick = { _, _ -> },
                isLoading = false,
                error = null,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "Answer place red glass + RED indicator")
@Composable
fun AnswerContentPlaceRedGlassDialogPreview() {
    MobileLanternTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                topBar = {
                    PreviewPhaseTopBar(
                        title = "Tiempo de colaboración",
                        flashState = FlashState.On(LedColor.RED)
                    )
                }
            ) { paddingValues ->
                AnswerContent(
                    question = "No sé cómo hacer un diagrama de gantt",
                    onRegisterClick = { _, _ -> },
                    isLoading = false,
                    error = null,
                    modifier = Modifier.padding(paddingValues)
                )
            }
            PlaceGlassDialog(
                glassColor = GlassColor.RED,
                onConfirm = {}
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AnswerContentPreview() {
    MobileLanternTheme {
        AnswerContent(
            question = "¿Cuál es la fórmula para calcular el área de un círculo?",
            onRegisterClick = { _, _ -> },
            isLoading = false,
            error = null
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AnswerContentWithTextPreview() {
    MobileLanternTheme {
        AnswerContent(
            question = "¿Cómo se calcula la derivada de una función exponencial?",
            onRegisterClick = { _, _ -> },
            isLoading = false,
            error = null
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AnswerContentLoadingPreview() {
    MobileLanternTheme {
        AnswerContent(
            question = "¿Qué es el teorema de Pitágoras?",
            onRegisterClick = { _, _ -> },
            isLoading = true,
            error = null
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AnswerContentWithErrorPreview() {
    MobileLanternTheme {
        AnswerContent(
            question = "¿Cómo funciona la fotosíntesis?",
            onRegisterClick = { _, _ -> },
            isLoading = false,
            error = "Error al registrar la respuesta. Por favor, intenta de nuevo."
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AnswerContentLongContentPreview() {
    MobileLanternTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            var answerText by remember { mutableStateOf("Después de consultar con varios compañeros y revisar los materiales del curso, hemos llegado a la conclusión de que este problema requiere un enfoque sistemático. Primero, debemos entender los principios fundamentales que subyacen a la cuestión planteada. Luego, aplicamos estos principios de manera ordenada, verificando cada paso del proceso para asegurarnos de que nuestra solución sea correcta y completa. También consideramos casos especiales y posibles excepciones que podrían surgir. Finalmente, validamos nuestra respuesta comparándola con ejemplos similares y verificando que cumple con todas las condiciones establecidas en el enunciado original.") }
            var isCorrect by remember { mutableStateOf(true) }
            var isAnswerValid by remember { mutableStateOf(true) }
            val scrollState = rememberScrollState()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StandardQuestionCard(
                    question = "# Problema Complejo de Análisis Matemático\n\n" +
                            "Considera la siguiente situación: tenemos una función **f(x)** definida en el intervalo [0, 2π] que es continua y diferenciable, y queremos determinar los puntos críticos, los intervalos de crecimiento y decrecimiento, así como los valores máximos y mínimos locales y absolutos.\n\n" +
                            "La función está dada por:\n\n" +
                            "**f(x) = sin(x) · cos(x) · e^(-x/2)**\n\n" +
                            "Para resolver este problema, necesitamos:\n\n" +
                            "1. Calcular la primera derivada f'(x) usando las reglas del producto y la cadena\n" +
                            "2. Encontrar los puntos críticos resolviendo f'(x) = 0\n" +
                            "3. Analizar el signo de f'(x) en cada intervalo entre puntos críticos\n" +
                            "4. Evaluar la función en los puntos críticos y en los extremos del intervalo\n" +
                            "5. Determinar si los extremos son máximos o mínimos locales o absolutos\n\n" +
                            "Además, debemos considerar aspectos teóricos importantes como:\n\n" +
                            "- El teorema del valor extremo que garantiza la existencia de máximos y mínimos absolutos\n" +
                            "- La relación entre la primera derivada y el comportamiento de la función\n" +
                            "- Las propiedades de las funciones trigonométricas y exponenciales involucradas\n" +
                            "- La posible necesidad de usar métodos numéricos si la ecuación f'(x) = 0 no tiene solución analítica\n\n" +
                            "¿Puedes explicar detalladamente cómo abordarías cada uno de estos pasos y qué dificultades técnicas podrías encontrar durante el proceso?",
                    isExpandable = false
                )

                AnswerInputCard(
                    answerText = answerText,
                    onAnswerChange = {
                        answerText = it
                        isAnswerValid = it.isNotBlank()
                    },
                    onSubmit = { },
                    isLoading = false,
                    isValid = isAnswerValid,
                    isCorrect = isCorrect,
                    onCorrectChange = { isCorrect = it }
                )
            }
        }
    }
}