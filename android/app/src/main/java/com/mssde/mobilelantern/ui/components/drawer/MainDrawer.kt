package com.mssde.mobilelantern.ui.components.drawer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import android.content.res.Configuration
import androidx.hilt.navigation.compose.hiltViewModel
import com.mssde.mobilelantern.ui.theme.MobileLanternTheme
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.mssde.mobilelantern.data.model.CurrentUser
import com.mssde.mobilelantern.data.model.HistoryQuestion
import com.mssde.mobilelantern.data.model.QuestionPhase
import com.mssde.mobilelantern.data.model.UserStats
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MainDrawer(
    onNewQuestionClick: () -> Unit,
    onQuestionClick: (HistoryQuestion) -> Unit,
    onUserClick: () -> Unit,
    onLogoutClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    drawerState: DrawerState? = null,
    viewModel: DrawerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(drawerState?.currentValue) {
        if (drawerState?.currentValue == DrawerValue.Open) {
            viewModel.onDrawerOpened()
        }
    }
    
    val listState = rememberLazyListState()
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = uiState.isLoading)
    
    ModalDrawerSheet(
        modifier = modifier.fillMaxHeight(),
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerContentColor = MaterialTheme.colorScheme.onSurface,
        windowInsets = WindowInsets(0.dp),
        drawerShape = RoundedCornerShape(0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            DrawerHeader(
                searchQuery = uiState.searchQuery,
                onSearchQueryChange = { query -> viewModel.updateSearchQuery(query) },
                onNewQuestionClick = onNewQuestionClick
            )
            
            SwipeRefresh(
                state = swipeRefreshState,
                onRefresh = { viewModel.refreshHistory() },
                modifier = Modifier.weight(1f)
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                    contentPadding = PaddingValues(bottom = 16.dp, top = 8.dp)
                ) {
                when {
                    uiState.isLoading -> {
                        item {
                            LoadingContent()
                        }
                    }
                    uiState.error != null -> {
                        item {
                            val errorMessage = uiState.error!!
                            ErrorContent(
                                error = errorMessage,
                                onRetry = { viewModel.refreshHistory() }
                            )
                        }
                    }
                    uiState.filteredQuestions.isEmpty() -> {
                        item {
                            EmptySearchResults(
                                searchQuery = uiState.searchQuery,
                                hasQuestions = uiState.questions.isNotEmpty()
                            )
                        }
                    }
                    else -> {
                        val groupedQuestions = groupQuestionsByTime(uiState.filteredQuestions)
                        groupedQuestions.forEach { (period, questions) ->
                            item(key = "header_$period") {
                                DrawerSectionHeader(title = period)
                            }
                            items(
                                items = questions,
                                key = { it.id }
                            ) { question ->
                                DrawerQuestionItem(
                                    question = question,
                                    onClick = { 
                                        onQuestionClick(question)
                                    }
                                )
                            }
                        }
                    }
                }
                }
            }
            
            DrawerFooter(
                user = uiState.currentUser,
                stats = uiState.userStats,
                onUserClick = onUserClick,
                onLogoutClick = onLogoutClick
            )
        }
    }
}



// Componente para mostrar cuando no hay resultados de búsqueda
@Composable
private fun EmptySearchResults(
    searchQuery: String,
    hasQuestions: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (searchQuery.isBlank() && !hasQuestions) "📝" else "🔍",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = when {
                searchQuery.isBlank() && !hasQuestions -> "No hay preguntas aún"
                searchQuery.isNotBlank() -> "No se encontraron preguntas"
                else -> "No hay preguntas que mostrar"
            },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = when {
                searchQuery.isBlank() && !hasQuestions -> "Haz tu primera pregunta para comenzar"
                searchQuery.isNotBlank() -> "Intenta con otros términos de búsqueda"
                else -> "Revisa tu conexión e intenta de nuevo"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

// Componente para mostrar loading
@Composable
private fun LoadingContent(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Cargando preguntas...",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}

// Componente para mostrar errores
@Composable
private fun ErrorContent(
    error: String,
    onRetry: () -> Unit,
    onTestApi: () -> Unit = {},
    onTestQuestions: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Detectar si es el error específico del AWS Load Balancer
    val isAwsError = error.contains("AWS_LOAD_BALANCER_RESTRICTION")
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isAwsError) "🔧" else "⚠️",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = if (isAwsError) "Historial No Disponible" else "Error al cargar",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = if (isAwsError) {
                "El historial de preguntas no está disponible temporalmente debido a una limitación técnica del servidor.\n\nNuestro equipo está trabajando en una solución."
            } else {
                error
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (isAwsError) {
            // Para el error de AWS, mostrar botones de prueba
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Primera fila: Verificar + Probar Questions
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = onRetry,
                        colors = ButtonDefaults.outlinedButtonColors(),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Verificar", fontSize = 12.sp)
                    }
                    
                    Button(
                        onClick = onTestQuestions,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Confirmar ELB", fontSize = 12.sp)
                    }
                }
                
                // Segunda fila: Probar Question/{id}
                Button(
                    onClick = onTestApi,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Test /question/31", fontSize = 12.sp)
                }
            }
        } else {
            // Para otros errores, botón normal
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Reintentar")
            }
        }
    }
}

private fun groupQuestionsByTime(questions: List<HistoryQuestion>): Map<String, List<HistoryQuestion>> {
    if (questions.isEmpty()) return emptyMap()
    
    val calendar = Calendar.getInstance()
    val now = calendar.time
    
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val todayStart = calendar.time
    
    calendar.add(Calendar.DAY_OF_MONTH, -1)
    val yesterdayStart = calendar.time
    
    calendar.time = now
    calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val thisWeekStart = calendar.time
    
    calendar.add(Calendar.WEEK_OF_YEAR, -1)
    val lastWeekStart = calendar.time
    
    calendar.time = now
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val thisMonthStart = calendar.time
    
    calendar.add(Calendar.MONTH, -1)
    val lastMonthStart = calendar.time
    
    val grouped = mutableMapOf<String, MutableList<HistoryQuestion>>()
    val monthFormat = SimpleDateFormat("MMMM", Locale("es", "ES"))
    val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())
    val currentYear = yearFormat.format(now).toInt()
    
    questions.forEach { question ->
        val key = when {
            question.timestamp >= todayStart -> "Hoy"
            question.timestamp >= yesterdayStart -> "Ayer"
            question.timestamp >= thisWeekStart -> "Esta semana"
            question.timestamp >= lastWeekStart -> "Semana pasada"
            question.timestamp >= thisMonthStart -> "Este mes"
            question.timestamp >= lastMonthStart -> "Mes pasado"
            else -> {
                val questionYear = yearFormat.format(question.timestamp).toInt()
                if (questionYear == currentYear) {
                    monthFormat.format(question.timestamp).replaceFirstChar { it.uppercase() }
                } else {
                    questionYear.toString()
                }
            }
        }
        
        grouped.getOrPut(key) { mutableListOf() }.add(question)
    }
    
    val order = listOf(
        "Hoy", "Ayer", "Esta semana", "Semana pasada", 
        "Este mes", "Mes pasado"
    )
    
    return grouped.entries
        .sortedWith(compareBy<Map.Entry<String, MutableList<HistoryQuestion>>> { entry ->
            val index = order.indexOf(entry.key)
            if (index >= 0) {
                index.toLong()
            } else {
                val firstQuestion = entry.value.firstOrNull()
                if (firstQuestion != null) {
                    Long.MAX_VALUE - firstQuestion.timestamp.time
                } else {
                    Long.MAX_VALUE
                }
            }
        })
        .associate { it.key to it.value.sortedByDescending { q -> q.timestamp } }
}

@Composable
fun DrawerContent(
    onNewQuestionClick: () -> Unit,
    onQuestionClick: (HistoryQuestion) -> Unit,
    onUserClick: () -> Unit,
    onLogoutClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    MainDrawer(
        onNewQuestionClick = onNewQuestionClick,
        onQuestionClick = onQuestionClick,
        onUserClick = onUserClick,
        onLogoutClick = onLogoutClick,
        modifier = modifier
    )
}

@Composable
private fun MainDrawerPreview(
    uiState: DrawerUiState,
    onNewQuestionClick: () -> Unit = {},
    onQuestionClick: (HistoryQuestion) -> Unit = {},
    onUserClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = uiState.isLoading)
    var searchQuery by remember { mutableStateOf(uiState.searchQuery) }
    
    ModalDrawerSheet(
        modifier = modifier.fillMaxHeight(),
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerContentColor = MaterialTheme.colorScheme.onSurface,
        windowInsets = WindowInsets(0.dp),
        drawerShape = RoundedCornerShape(0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            DrawerHeader(
                searchQuery = searchQuery,
                onSearchQueryChange = { query -> searchQuery = query },
                onNewQuestionClick = onNewQuestionClick
            )
            
            SwipeRefresh(
                state = swipeRefreshState,
                onRefresh = { },
                modifier = Modifier.weight(1f)
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                    contentPadding = PaddingValues(bottom = 16.dp, top = 8.dp)
                ) {
                when {
                    uiState.isLoading -> {
                        item {
                            LoadingContent()
                        }
                    }
                    uiState.error != null -> {
                        item {
                            val errorMessage = uiState.error!!
                            ErrorContent(
                                error = errorMessage,
                                onRetry = { },
                                onTestApi = { },
                                onTestQuestions = { }
                            )
                        }
                    }
                    uiState.filteredQuestions.isEmpty() -> {
                        item {
                            EmptySearchResults(
                                searchQuery = uiState.searchQuery,
                                hasQuestions = uiState.questions.isNotEmpty()
                            )
                        }
                    }
                    else -> {
                        val filtered = if (searchQuery.isBlank()) {
                            uiState.filteredQuestions
                        } else {
                            uiState.filteredQuestions.filter { 
                                it.questionText.contains(searchQuery, ignoreCase = true) 
                            }
                        }
                        val groupedQuestions = groupQuestionsByTime(filtered)
                        groupedQuestions.forEach { (period, questions) ->
                            item(key = "header_$period") {
                                DrawerSectionHeader(title = period)
                            }
                            items(
                                items = questions,
                                key = { it.id }
                            ) { question ->
                                DrawerQuestionItem(
                                    question = question,
                                    onClick = { 
                                        onQuestionClick(question)
                                    }
                                )
                            }
                        }
                    }
                }
                }
            }
            
            DrawerFooter(
                user = uiState.currentUser,
                stats = uiState.userStats,
                onUserClick = onUserClick,
                onLogoutClick = onLogoutClick
            )
        }
    }
}

// ==============================
// PREVIEWS
// ==============================

// Datos de muestra para los previews
private val sampleUser = CurrentUser(
    id = 1,
    name = "Juan Pérez",
    email = "juan.perez@example.com",
    questionsCount = 15,
    resolvedCount = 10
)

private val sampleStats = UserStats(
    totalQuestions = 15,
    resolvedQuestions = 10,
    pendingQuestions = 5
)

private val sampleQuestions = listOf(
    HistoryQuestion(
        id = 1,
        questionText = "¿Cómo puedo calcular la cantidad de oxígeno necesaria para una reacción de combustión completa?",
        phase = QuestionPhase.COMPLETED,
        timestamp = Date(System.currentTimeMillis() - 3600000),
        isResolved = true
    ),
    HistoryQuestion(
        id = 2,
        questionText = "¿Qué es el pH y cómo se calcula?",
        phase = QuestionPhase.AI_RESPONSE,
        timestamp = Date(System.currentTimeMillis() - 86400000),
        isResolved = false
    ),
    HistoryQuestion(
        id = 3,
        questionText = "Explicar el principio de Le Chatelier",
        phase = QuestionPhase.ANSWERED,
        timestamp = Date(System.currentTimeMillis() - 172800000),
        isResolved = false
    ),
    HistoryQuestion(
        id = 4,
        questionText = "¿Cuál es la diferencia entre enlace iónico y covalente?",
        phase = QuestionPhase.PENDING,
        timestamp = Date(System.currentTimeMillis() - 259200000),
        isResolved = false
    )
)

private val sampleQuestionsExtended = listOf(
    HistoryQuestion(
        id = 1,
        questionText = "¿Cómo puedo calcular la cantidad de oxígeno necesaria para una reacción de combustión completa?",
        phase = QuestionPhase.COMPLETED,
        timestamp = Date(System.currentTimeMillis() - 3600000),
        isResolved = true
    ),
    HistoryQuestion(
        id = 2,
        questionText = "¿Qué es el pH y cómo se calcula?",
        phase = QuestionPhase.AI_RESPONSE,
        timestamp = Date(System.currentTimeMillis() - 7200000),
        isResolved = false
    ),
    HistoryQuestion(
        id = 3,
        questionText = "Explicar el principio de Le Chatelier",
        phase = QuestionPhase.ANSWERED,
        timestamp = Date(System.currentTimeMillis() - 86400000),
        isResolved = false
    ),
    HistoryQuestion(
        id = 4,
        questionText = "¿Cuál es la diferencia entre enlace iónico y covalente?",
        phase = QuestionPhase.PENDING,
        timestamp = Date(System.currentTimeMillis() - 172800000),
        isResolved = false
    ),
    HistoryQuestion(
        id = 5,
        questionText = "¿Cómo funciona la destilación fraccionada?",
        phase = QuestionPhase.COMPLETED,
        timestamp = Date(System.currentTimeMillis() - 259200000),
        isResolved = true
    ),
    HistoryQuestion(
        id = 6,
        questionText = "Explicar la ley de conservación de la masa",
        phase = QuestionPhase.ANSWERED,
        timestamp = Date(System.currentTimeMillis() - 345600000),
        isResolved = false
    ),
    HistoryQuestion(
        id = 7,
        questionText = "¿Qué son los isómeros y cuántos tipos existen?",
        phase = QuestionPhase.AI_RESPONSE,
        timestamp = Date(System.currentTimeMillis() - 432000000),
        isResolved = false
    ),
    HistoryQuestion(
        id = 8,
        questionText = "Calcular el número de oxidación del azufre en H2SO4",
        phase = QuestionPhase.COMPLETED,
        timestamp = Date(System.currentTimeMillis() - 518400000),
        isResolved = true
    )
)

// Preview 1: LoadingContent
@Preview(name = "Loading State", showBackground = true)
@Composable
private fun PreviewLoadingContent() {
    MaterialTheme {
        LoadingContent()
    }
}

// Preview 2: EmptySearchResults - Sin preguntas iniciales
@Preview(name = "Empty State - Sin Preguntas", showBackground = true)
@Composable
private fun PreviewEmptySearchResultsNoQuestions() {
    MaterialTheme {
        EmptySearchResults(
            searchQuery = "",
            hasQuestions = false
        )
    }
}

// Preview 3: EmptySearchResults - No hay resultados de búsqueda
@Preview(name = "Empty State - Sin Resultados", showBackground = true)
@Composable
private fun PreviewEmptySearchResultsNoResults() {
    MaterialTheme {
        EmptySearchResults(
            searchQuery = "búsqueda sin resultados",
            hasQuestions = true
        )
    }
}

// Preview 4: ErrorContent - Error genérico
@Preview(name = "Error State - Genérico", showBackground = true)
@Composable
private fun PreviewErrorContentGeneric() {
    MaterialTheme {
        ErrorContent(
            error = "No se pudo conectar con el servidor. Verifica tu conexión a internet.",
            onRetry = {}
        )
    }
}

// Preview 5: ErrorContent - Error AWS
@Preview(name = "Error State - AWS", showBackground = true)
@Composable
private fun PreviewErrorContentAWS() {
    MaterialTheme {
        ErrorContent(
            error = "AWS_LOAD_BALANCER_RESTRICTION: El servicio no está disponible temporalmente.",
            onRetry = {},
            onTestApi = {},
            onTestQuestions = {}
        )
    }
}

// Preview 6: DrawerSectionHeader
@Preview(name = "Section Header", showBackground = true)
@Composable
private fun PreviewDrawerSectionHeader() {
    MaterialTheme {
        Column {
            DrawerSectionHeader(title = "Hoy")
            DrawerSectionHeader(title = "Esta semana")
            DrawerSectionHeader(title = "Este mes")
        }
    }
}

// Preview 7: DrawerQuestionItem - Resuelta
@Preview(name = "Question Item - Resuelta", showBackground = true)
@Composable
private fun PreviewDrawerQuestionItemResolved() {
    MaterialTheme {
        DrawerQuestionItem(
            question = sampleQuestions[0],
            onClick = {}
        )
    }
}

// Preview 8: DrawerQuestionItem - Pendiente
@Preview(name = "Question Item - Pendiente", showBackground = true)
@Composable
private fun PreviewDrawerQuestionItemPending() {
    MaterialTheme {
        DrawerQuestionItem(
            question = sampleQuestions[3],
            onClick = {}
        )
    }
}

// Preview 9: DrawerQuestionItem - Todas las fases
@Preview(name = "Question Items - Todas las Fases", showBackground = true)
@Composable
private fun PreviewDrawerQuestionItemAllPhases() {
    MaterialTheme {
        Column {
            sampleQuestions.forEach { question ->
                DrawerQuestionItem(
                    question = question,
                    onClick = {}
                )
            }
        }
    }
}

// Preview 10: DrawerHeader
@Preview(name = "Drawer Header", showBackground = true)
@Composable
private fun PreviewDrawerHeader() {
    MaterialTheme {
        DrawerHeader(
            searchQuery = "",
            onSearchQueryChange = {},
            onNewQuestionClick = {}
        )
    }
}

// Preview 11: DrawerHeader con búsqueda
@Preview(name = "Drawer Header - Con Búsqueda", showBackground = true)
@Composable
private fun PreviewDrawerHeaderWithSearch() {
    MaterialTheme {
        DrawerHeader(
            searchQuery = "oxígeno",
            onSearchQueryChange = {},
            onNewQuestionClick = {}
        )
    }
}

// Preview 12: DrawerFooter
@Preview(name = "Drawer Footer", showBackground = true)
@Composable
private fun PreviewDrawerFooter() {
    MaterialTheme {
        DrawerFooter(
            user = sampleUser,
            stats = sampleStats,
            onUserClick = {},
            onLogoutClick = {}
        )
    }
}

// Preview 13: DrawerFooter - Usuario con muchas preguntas
@Preview(name = "Drawer Footer - Usuario Activo", showBackground = true)
@Composable
private fun PreviewDrawerFooterActiveUser() {
    MaterialTheme {
        DrawerFooter(
            user = sampleUser.copy(questionsCount = 150, resolvedCount = 120),
            stats = sampleStats.copy(totalQuestions = 150, resolvedQuestions = 120, pendingQuestions = 30),
            onUserClick = {},
            onLogoutClick = {}
        )
    }
}

// ==============================
// DARK MODE PREVIEWS
// ==============================

// Preview Dark 1: LoadingContent
@Preview(name = "Loading State - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewLoadingContentDark() {
    MobileLanternTheme(darkTheme = true) {
        LoadingContent()
    }
}

// Preview Dark 2: EmptySearchResults
@Preview(name = "Empty State - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewEmptySearchResultsDark() {
    MobileLanternTheme(darkTheme = true) {
        EmptySearchResults(
            searchQuery = "",
            hasQuestions = false
        )
    }
}

// Preview Dark 3: ErrorContent - Error genérico
@Preview(name = "Error State - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewErrorContentDark() {
    MobileLanternTheme(darkTheme = true) {
        ErrorContent(
            error = "No se pudo conectar con el servidor. Verifica tu conexión a internet.",
            onRetry = {}
        )
    }
}

// Preview Dark 4: ErrorContent - Error AWS
@Preview(name = "Error AWS - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewErrorContentAWSDark() {
    MobileLanternTheme(darkTheme = true) {
        ErrorContent(
            error = "AWS_LOAD_BALANCER_RESTRICTION: El servicio no está disponible temporalmente.",
            onRetry = {},
            onTestApi = {},
            onTestQuestions = {}
        )
    }
}

// Preview Dark 5: DrawerSectionHeader
@Preview(name = "Section Headers - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewDrawerSectionHeaderDark() {
    MobileLanternTheme(darkTheme = true) {
        Column {
            DrawerSectionHeader(title = "Hoy")
            DrawerSectionHeader(title = "Esta semana")
            DrawerSectionHeader(title = "Este mes")
        }
    }
}

// Preview Dark 6: DrawerQuestionItem - Todas las fases
@Preview(name = "Question Items - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewDrawerQuestionItemAllPhasesDark() {
    MobileLanternTheme(darkTheme = true) {
        Column {
            sampleQuestions.forEach { question ->
                DrawerQuestionItem(
                    question = question,
                    onClick = {}
                )
            }
        }
    }
}

// Preview Dark 7: DrawerHeader
@Preview(name = "Drawer Header - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewDrawerHeaderDark() {
    MobileLanternTheme(darkTheme = true) {
        DrawerHeader(
            searchQuery = "",
            onSearchQueryChange = {},
            onNewQuestionClick = {}
        )
    }
}

// Preview Dark 8: DrawerHeader con búsqueda
@Preview(name = "Drawer Header Search - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewDrawerHeaderWithSearchDark() {
    MobileLanternTheme(darkTheme = true) {
        DrawerHeader(
            searchQuery = "oxígeno",
            onSearchQueryChange = {},
            onNewQuestionClick = {}
        )
    }
}

// Preview Dark 9: DrawerFooter
@Preview(name = "Drawer Footer - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewDrawerFooterDark() {
    MobileLanternTheme(darkTheme = true) {
        DrawerFooter(
            user = sampleUser,
            stats = sampleStats,
            onUserClick = {},
            onLogoutClick = {}
        )
    }
}

// ==============================
// PREVIEWS COMPLETOS DEL DRAWER
// ==============================

@Preview(name = "Main Drawer - Completo", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun PreviewMainDrawerComplete() {
    MobileLanternTheme {
        MainDrawerPreview(
            uiState = DrawerUiState(
                questions = sampleQuestionsExtended,
                filteredQuestions = sampleQuestionsExtended,
                searchQuery = "",
                isLoading = false,
                error = null,
                currentUser = sampleUser,
                userStats = sampleStats.copy(
                    totalQuestions = sampleQuestionsExtended.size,
                    resolvedQuestions = sampleQuestionsExtended.count { it.isResolved },
                    pendingQuestions = sampleQuestionsExtended.count { !it.isResolved }
                )
            )
        )
    }
}

@Preview(name = "Main Drawer - Con Búsqueda", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun PreviewMainDrawerWithSearch() {
    MobileLanternTheme {
        MainDrawerPreview(
            uiState = DrawerUiState(
                questions = sampleQuestionsExtended,
                filteredQuestions = sampleQuestionsExtended.filter { 
                    it.questionText.contains("oxígeno", ignoreCase = true) ||
                    it.questionText.contains("pH", ignoreCase = true)
                },
                searchQuery = "oxígeno",
                isLoading = false,
                error = null,
                currentUser = sampleUser,
                userStats = sampleStats
            )
        )
    }
}

@Preview(name = "Main Drawer - Loading", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun PreviewMainDrawerLoading() {
    MobileLanternTheme {
        MainDrawerPreview(
            uiState = DrawerUiState(
                questions = emptyList(),
                filteredQuestions = emptyList(),
                searchQuery = "",
                isLoading = true,
                error = null,
                currentUser = sampleUser,
                userStats = sampleStats
            )
        )
    }
}

@Preview(name = "Main Drawer - Error", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun PreviewMainDrawerError() {
    MobileLanternTheme {
        MainDrawerPreview(
            uiState = DrawerUiState(
                questions = emptyList(),
                filteredQuestions = emptyList(),
                searchQuery = "",
                isLoading = false,
                error = "No se pudo conectar con el servidor. Verifica tu conexión a internet.",
                currentUser = sampleUser,
                userStats = sampleStats
            )
        )
    }
}

@Preview(name = "Main Drawer - Error AWS", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun PreviewMainDrawerErrorAWS() {
    MobileLanternTheme {
        MainDrawerPreview(
            uiState = DrawerUiState(
                questions = emptyList(),
                filteredQuestions = emptyList(),
                searchQuery = "",
                isLoading = false,
                error = "AWS_LOAD_BALANCER_RESTRICTION: El servicio no está disponible temporalmente.",
                currentUser = sampleUser,
                userStats = sampleStats
            )
        )
    }
}

@Preview(name = "Main Drawer - Vacío", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun PreviewMainDrawerEmpty() {
    MobileLanternTheme {
        MainDrawerPreview(
            uiState = DrawerUiState(
                questions = emptyList(),
                filteredQuestions = emptyList(),
                searchQuery = "",
                isLoading = false,
                error = null,
                currentUser = sampleUser.copy(questionsCount = 0, resolvedCount = 0),
                userStats = UserStats(totalQuestions = 0, resolvedQuestions = 0, pendingQuestions = 0)
            )
        )
    }
}

@Preview(name = "Main Drawer - Usuario Activo", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun PreviewMainDrawerActiveUser() {
    MobileLanternTheme {
        MainDrawerPreview(
            uiState = DrawerUiState(
                questions = sampleQuestionsExtended,
                filteredQuestions = sampleQuestionsExtended,
                searchQuery = "",
                isLoading = false,
                error = null,
                currentUser = sampleUser.copy(
                    questionsCount = 150,
                    resolvedCount = 120
                ),
                userStats = UserStats(
                    totalQuestions = 150,
                    resolvedQuestions = 120,
                    pendingQuestions = 30
                )
            )
        )
    }
}

@Preview(name = "Main Drawer - Dark", showBackground = true, widthDp = 360, heightDp = 800, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewMainDrawerCompleteDark() {
    MobileLanternTheme(darkTheme = true) {
        MainDrawerPreview(
            uiState = DrawerUiState(
                questions = sampleQuestionsExtended,
                filteredQuestions = sampleQuestionsExtended,
                searchQuery = "",
                isLoading = false,
                error = null,
                currentUser = sampleUser,
                userStats = sampleStats.copy(
                    totalQuestions = sampleQuestionsExtended.size,
                    resolvedQuestions = sampleQuestionsExtended.count { it.isResolved },
                    pendingQuestions = sampleQuestionsExtended.count { !it.isResolved }
                )
            )
        )
    }
}

@Preview(name = "Main Drawer - Dark Loading", showBackground = true, widthDp = 360, heightDp = 800, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewMainDrawerLoadingDark() {
    MobileLanternTheme(darkTheme = true) {
        MainDrawerPreview(
            uiState = DrawerUiState(
                questions = emptyList(),
                filteredQuestions = emptyList(),
                searchQuery = "",
                isLoading = true,
                error = null,
                currentUser = sampleUser,
                userStats = sampleStats
            )
        )
    }
} 