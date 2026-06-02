package com.mssde.mobilelantern

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.mssde.mobilelantern.ui.components.AIResponseCard
import com.mssde.mobilelantern.ui.components.CollaborativeAnswerCard
import com.mssde.mobilelantern.ui.components.StandardQuestionCard
import com.mssde.mobilelantern.ui.components.drawer.DrawerWrapper
import com.mssde.mobilelantern.ui.theme.MobileLanternTheme
import com.mssde.mobilelantern.viewmodel.QuestionDetailViewModel
import dagger.hilt.android.AndroidEntryPoint
import dev.jeziellago.compose.markdowntext.MarkdownText
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class QuestionDetailActivity : ComponentActivity() {
    
    private val viewModel: QuestionDetailViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val questionId = intent.getIntExtra("questionId", -1)
        val questionText = intent.getStringExtra("questionText") ?: "Pregunta"
        
        if (questionId == -1) {
            Toast.makeText(this, "Pregunta no encontrada", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        // Cargar detalles de la pregunta
        viewModel.loadQuestionDetails(questionId)
        
        setContent {
            MobileLanternTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DrawerWrapper(
                        title = "Detalle de Pregunta",
                        showBackButton = true,
                        showConfirmDialog = false,
                        onBackClick = {
                            finish()
                        },
                        onReconnectBluetooth = {
                            startActivity(Intent(this@QuestionDetailActivity, LanternActivity::class.java))
                        },
                        onQuestionClick = { question ->
                            val intent = Intent(this@QuestionDetailActivity, QuestionDetailActivity::class.java).apply {
                                putExtra("questionId", question.id)
                                putExtra("questionText", question.questionText)
                            }
                            startActivity(intent)
                            finish()
                        }
                    ) { paddingValues ->
                        QuestionDetailContent(
                            viewModel = viewModel,
                            modifier = Modifier.padding(paddingValues)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuestionDetailContent(
    viewModel: QuestionDetailViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = uiState.isRefreshing)
    
    SwipeRefresh(
        state = swipeRefreshState,
        onRefresh = { viewModel.refreshQuestionDetails() },
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            if (uiState.interactions.isNotEmpty()) {
                val firstInteraction = uiState.interactions.first()
                val questionText = firstInteraction.question ?: firstInteraction.texto ?: ""
                
                StandardQuestionCard(
                    question = questionText,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    headerActions = {
                        InteractionHeaderActions(firstInteraction)
                    }
                )
            }
            
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Cargando interacciones...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                
                uiState.error != null -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Error",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = uiState.error!!,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.retry() }
                            ) {
                                Text("Reintentar")
                            }
                        }
                    }
                }
                
                uiState.interactions.isEmpty() -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No hay interacciones disponibles",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                else -> {
                    Text(
                        text = "Historial de Interacciones",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.interactions.drop(1)) { interaction ->
                            InteractionCard(interaction = interaction)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InteractionCard(
    interaction: com.mssde.mobilelantern.data.local.entity.QuestionInteractionEntity
) {
    val content = interaction.question ?: interaction.texto ?: ""
    
    when (interaction.rol) {
        "assistant" -> {
            AIResponseCard(
                response = content,
                modifier = Modifier.fillMaxWidth(),
                headerActions = {
                    InteractionHeaderActions(interaction)
                }
            )
        }
        "user" -> {
            CollaborativeAnswerCard(
                answer = content,
                modifier = Modifier.fillMaxWidth(),
                headerActions = {
                    InteractionHeaderActions(interaction)
                }
            )
        }
        else -> {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = interaction.rol?.replaceFirstChar { it.uppercase() } ?: "Desconocido",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = formatRelativeTime(interaction.moment),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    if (content.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        MarkdownText(
                            markdown = content,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RowScope.InteractionHeaderActions(
    interaction: com.mssde.mobilelantern.data.local.entity.QuestionInteractionEntity
) {
    Text(
        text = formatRelativeTime(interaction.moment),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

fun formatRelativeTime(dateString: String): String {
    return try {
        val formats = listOf(
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS", Locale.getDefault()),
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()),
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        )
        
        var date: Date? = null
        for (format in formats) {
            try {
                date = format.parse(dateString)
                break
            } catch (e: Exception) {
                continue
            }
        }
        
        if (date == null) return dateString
        
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis
        val diff = now - date.time
        
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateCalendar = Calendar.getInstance().apply { time = date }
        
        when {
            seconds < 60 -> "Hace un momento"
            minutes < 60 -> "Hace ${minutes} min"
            hours < 24 -> {
                if (hours == 1L) "Hace 1 hora" else "Hace ${hours} horas"
            }
            days == 1L -> "Ayer, ${timeFormat.format(date)}"
            days < 7 -> {
                val dayFormat = SimpleDateFormat("EEEE HH:mm", Locale("es", "ES"))
                dayFormat.format(date).replaceFirstChar { it.uppercase() }
            }
            days < 30 -> {
                val weeks = days / 7
                if (weeks == 1L) "Hace 1 semana" else "Hace ${weeks} semanas"
            }
            else -> {
                val dateFormat = SimpleDateFormat("d MMM yyyy", Locale("es", "ES"))
                dateFormat.format(date)
            }
        }
    } catch (e: Exception) {
        dateString
    }
}
