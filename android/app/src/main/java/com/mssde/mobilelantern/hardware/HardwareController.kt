package com.mssde.mobilelantern.hardware

import android.content.Context
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Handler
import android.os.Looper
import com.mssde.mobilelantern.data.local.LanternPreferenceManager
import com.mssde.mobilelantern.ui.components.LanternType
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

@Singleton
class HardwareController @Inject constructor(
    @ApplicationContext private val context: Context,
    private val lanternPreferenceManager: LanternPreferenceManager,
    private val integratedLanternDriver: IntegratedLanternDriver,
    private val externalLanternDriver: ExternalLanternDriver
) {
    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private val toneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, 100)
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    private var isFlashlightOn = false
    private var flashingFrequency = DEFAULT_FLASH_PERIOD_MS
    private var currentLedColor: LedColor = LedColor.WHITE

    private val _flashState = MutableStateFlow<FlashState>(FlashState.Off)
    val flashState: StateFlow<FlashState> = _flashState.asStateFlow()

    private val _isSoundMuted = MutableStateFlow(false)
    val isSoundMuted: StateFlow<Boolean> = _isSoundMuted.asStateFlow()

    private val _systemFlashlightState = MutableStateFlow(false)
    val systemFlashlightState: StateFlow<Boolean> = _systemFlashlightState.asStateFlow()

    private val _lanternDriverError = MutableStateFlow<LanternDriverError?>(null)
    val lanternDriverError: StateFlow<LanternDriverError?> = _lanternDriverError.asStateFlow()

    private var torchCallback: CameraManager.TorchCallback? = null
    private var cameraId: String? = null

    init {
        try {
            cameraId = cameraManager.cameraIdList.firstOrNull()
            registerTorchCallback()
        } catch (e: Exception) {
            android.util.Log.e("HardwareController", "Error initializing camera", e)
        }
        coroutineScope.launch {
            combine(
                lanternPreferenceManager.selectedLanternType,
                externalLanternDriver.driverError
            ) { type, err ->
                if (type == LanternType.EXTERNAL) err else null
            }.collect { _lanternDriverError.value = it }
        }
    }

    private fun registerTorchCallback() {
        torchCallback = object : CameraManager.TorchCallback() {
            override fun onTorchModeChanged(cameraId: String, enabled: Boolean) {
                if (cameraId == this@HardwareController.cameraId) {
                    _systemFlashlightState.value = enabled
                    android.util.Log.d("HardwareController", "Torch state changed: $enabled")
                }
            }

            override fun onTorchModeUnavailable(cameraId: String) {
                if (cameraId == this@HardwareController.cameraId) {
                    _systemFlashlightState.value = false
                    android.util.Log.d("HardwareController", "Torch mode unavailable")
                }
            }
        }
        cameraManager.registerTorchCallback(torchCallback!!, Handler(Looper.getMainLooper()))
        checkInitialFlashlightState()
    }

    private fun checkInitialFlashlightState() {
        try {
            cameraId?.let { id ->
                val characteristics = cameraManager.getCameraCharacteristics(id)
                val flashAvailable = characteristics.get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) ?: false
                if (!flashAvailable) {
                    _systemFlashlightState.value = false
                    android.util.Log.d("HardwareController", "Flash not available")
                    return
                }
                android.util.Log.d("HardwareController", "Flash available, callback registered. State will be updated on torch changes.")
            }
        } catch (e: Exception) {
            android.util.Log.e("HardwareController", "Error checking initial torch state", e)
            _systemFlashlightState.value = false
        }
    }

    fun checkFlashlightState(): Boolean {
        return _systemFlashlightState.value
    }

    fun shouldWarnSystemFlashlight(): Boolean {
        return lanternPreferenceManager.selectedLanternType.value == LanternType.INTEGRATED
    }

    fun collaborativePhaseTitle(): String = "Tiempo de colaboración"

    fun aiPhaseTitle(): String = "Tiempo de asistencia con IA"

    fun clearLanternDriverError() {
        externalLanternDriver.clearDriverError()
    }

    val requiresPhysicalGlassChange: Boolean
        get() = when (lanternPreferenceManager.selectedLanternType.value) {
            LanternType.EXTERNAL -> false
            LanternType.INTEGRATED, LanternType.NONE -> true
        }

    private fun resolveDriver(): LanternDriver {
        return when (lanternPreferenceManager.selectedLanternType.value) {
            LanternType.EXTERNAL -> externalLanternDriver
            LanternType.INTEGRATED, LanternType.NONE -> integratedLanternDriver
        }
    }

    fun turnOnFlashlight() {
        turnOnWithColor(LedColor.WHITE)
    }

    fun turnOnWithColor(color: LedColor) {
        try {
            resolveDriver().stopFlashing()
            currentLedColor = color
            resolveDriver().turnOnWithColor(color)
            isFlashlightOn = true
            _flashState.value = FlashState.On(color)
        } catch (e: Exception) {
            android.util.Log.e("HardwareController", "Error turning on flashlight", e)
        }
    }

    fun setLedColor(color: LedColor) {
        currentLedColor = color
        resolveDriver().setColor(color)
        val currentState = _flashState.value
        _flashState.value = when (currentState) {
            is FlashState.On -> currentState.copy(color = color)
            is FlashState.Flashing -> currentState.copy(color = color)
            FlashState.Off -> currentState
        }
    }

    fun setLedEffect(effect: String) {
        resolveDriver().setEffect(effect)
    }

    fun turnOffFlashlight() {
        try {
            resolveDriver().stopFlashing()
            resolveDriver().turnOff()
            isFlashlightOn = false
            currentLedColor = LedColor.WHITE
            _flashState.value = FlashState.Off
            android.util.Log.d("HardwareController", "turnOffFlashlight: estado reseteado")
        } catch (e: Exception) {
            android.util.Log.e("HardwareController", "Error turning off flashlight", e)
        }
    }

    fun startFlashing(frequencyMs: Long = 1000L) {
        val effectiveFrequency = max(frequencyMs, MIN_FLASH_PERIOD_MS)
        flashingFrequency = effectiveFrequency
        resolveDriver().stopFlashing()

        resolveDriver().startFlashing(effectiveFrequency)
        _flashState.value = FlashState.Flashing(
            periodMs = effectiveFrequency,
            onDurationMs = DEFAULT_FLASH_ON_DURATION_MS,
            color = currentLedColor
        )
        android.util.Log.d("HardwareController", "startFlashing: fase activa")
    }

    fun stopFlashing() {
        resolveDriver().stopFlashing()
        if (isFlashlightOn) {
            _flashState.value = FlashState.On(currentLedColor)
        } else {
            _flashState.value = FlashState.Off
        }
    }

    fun increaseFlashingFrequency() {
        val newFrequency = max((flashingFrequency * 0.7).toLong(), MIN_FLASH_PERIOD_MS)
        flashingFrequency = newFrequency
        if (integratedLanternDriver.isFlashingActive()) {
            startFlashing(newFrequency)
        }
    }

    fun emitAlertSound() {
        if (_isSoundMuted.value) {
            android.util.Log.d("HardwareController", "Alert sound muted")
            return
        }

        try {
            coroutineScope.launch {
                repeat(3) {
                    toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 500)
                    delay(600)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("HardwareController", "Error emitting alert sound", e)
        }
    }

    fun toggleSoundMute() {
        _isSoundMuted.value = !_isSoundMuted.value
        android.util.Log.d("HardwareController", "Sound muted: ${_isSoundMuted.value}")
    }

    fun release() {
        integratedLanternDriver.stopFlashing()
        integratedLanternDriver.turnOff()
        externalLanternDriver.stopFlashing()
        externalLanternDriver.turnOff()
        toneGenerator.release()
        _flashState.value = FlashState.Off
        torchCallback?.let {
            cameraManager.unregisterTorchCallback(it)
        }
        torchCallback = null
    }

    fun isFlashlightActive(): Boolean = when (flashState.value) {
        FlashState.Off -> false
        is FlashState.On, is FlashState.Flashing -> true
    }

    companion object {
        private const val DEFAULT_FLASH_ON_DURATION_MS = 100L
        private const val MIN_FLASH_PERIOD_MS = 200L
        private const val DEFAULT_FLASH_PERIOD_MS = 1000L
    }
}
