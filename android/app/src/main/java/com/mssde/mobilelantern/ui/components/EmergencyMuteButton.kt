package com.mssde.mobilelantern.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import com.mssde.mobilelantern.hardware.HardwareController
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.delay

/**
 * EntryPoint para acceder a HardwareController desde Compose sin inyección de parámetros
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface EmergencyMuteEntryPoint {
    fun hardwareController(): HardwareController
}

/**
 * Botón de emergencia para silenciar las alertas acústicas.
 * Requiere 3 toques consecutivos para activarse (evita falsos gestos).
 * Se muestra como un icono pequeño y semitransparente.
 * 
 * @param hardwareController Opcional, solo para previews. Por defecto usa inyección de Hilt.
 */
@Composable
fun EmergencyMuteButton(
    hardwareController: HardwareController? = null
) {
    val context = LocalContext.current
    
    // Obtener HardwareController usando EntryPoint o el proporcionado
    val controller = remember(hardwareController) {
        hardwareController ?: try {
            val appContext = context.applicationContext
            EntryPointAccessors.fromApplication(
                appContext,
                EmergencyMuteEntryPoint::class.java
            ).hardwareController()
        } catch (e: IllegalStateException) {
            // En previews no hay aplicación Hilt, retornar null
            null
        }
    }
    
    // Si no hay controlador (previews), mostrar solo el icono sin funcionalidad
    if (controller == null) {
        PreviewEmergencyMuteButton()
        return
    }
    
    // Observar estado de mute
    val isMuted by controller.isSoundMuted.collectAsState()
    
    // Contador de toques (0-3)
    var tapCount by remember { mutableStateOf(0) }
    
    // Animación de escala basada en el contador de toques
    val targetScale = when (tapCount) {
        0 -> 1.0f
        1 -> 1.15f
        2 -> 1.3f
        else -> 1.0f
    }
    
    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = spring(
            dampingRatio = 0.5f,
            stiffness = 300f
        ),
        label = "muteButtonScale"
    )
    
    // Timer de reset: si pasan 2 segundos sin tocar, resetear contador
    LaunchedEffect(tapCount) {
        if (tapCount > 0 && tapCount < 3) {
            delay(2000)
            tapCount = 0
        }
    }
    
    // Icono según estado
    val icon = if (isMuted) "🔇" else "🔊"
    
    Box(
        modifier = Modifier
            .alpha(0.35f)
            .scale(scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null // Sin efecto de ripple
            ) {
                tapCount++
                
                // Al tercer toque, toggle mute y resetear contador
                if (tapCount >= 3) {
                    controller.toggleSoundMute()
                    tapCount = 0
                }
            }
    ) {
        Text(
            text = icon,
            fontSize = 28.sp
        )
    }
}

/**
 * Versión simplificada del botón para previews (sin funcionalidad)
 */
@Composable
private fun PreviewEmergencyMuteButton() {
    Box(
        modifier = Modifier.alpha(0.35f)
    ) {
        Text(
            text = "🔊",
            fontSize = 28.sp
        )
    }
}

