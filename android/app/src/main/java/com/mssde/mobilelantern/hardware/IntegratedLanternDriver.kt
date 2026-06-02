package com.mssde.mobilelantern.hardware

import android.content.Context
import android.hardware.camera2.CameraManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlin.math.max
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IntegratedLanternDriver @Inject constructor(
    @ApplicationContext private val context: Context
) : LanternDriver {

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    private var isFlashlightOn = false
    private var flashingJob: Job? = null

    override val requiresPhysicalGlassChange: Boolean = true

    override fun turnOn() {
        stopFlashingInternal()
        try {
            val id = cameraManager.cameraIdList.firstOrNull()
            id?.let {
                cameraManager.setTorchMode(it, true)
                isFlashlightOn = true
            }
        } catch (e: Exception) {
            android.util.Log.e("IntegratedLanternDriver", "Error turning on flashlight", e)
        }
    }

    override fun turnOnWithColor(color: LedColor) {
        stopFlashingInternal()
        try {
            val id = cameraManager.cameraIdList.firstOrNull()
            id?.let {
                cameraManager.setTorchMode(it, true)
                isFlashlightOn = true
            }
        } catch (e: Exception) {
            android.util.Log.e("IntegratedLanternDriver", "Error turnOnWithColor", e)
        }
    }

    override fun setColor(color: LedColor) {
    }

    override fun setEffect(effect: String) {
    }

    override fun turnOff() {
        try {
            flashingJob?.cancel()
            flashingJob = null
            val id = cameraManager.cameraIdList.firstOrNull()
            id?.let { cameraManager.setTorchMode(it, false) }
            isFlashlightOn = false
        } catch (e: Exception) {
            android.util.Log.e("IntegratedLanternDriver", "Error turning off flashlight", e)
        }
    }

    override fun startFlashing(frequencyMs: Long) {
        val effectiveFrequency = max(frequencyMs, MIN_FLASH_PERIOD_MS)
        stopFlashingInternal()
        flashingJob = coroutineScope.launch {
            while (true) {
                try {
                    val id = cameraManager.cameraIdList.firstOrNull()
                    id?.let {
                        cameraManager.setTorchMode(it, true)
                        delay(DEFAULT_FLASH_ON_DURATION_MS)
                        cameraManager.setTorchMode(it, false)
                        delay(max(effectiveFrequency - DEFAULT_FLASH_ON_DURATION_MS, 0L))
                    }
                } catch (e: Exception) {
                    android.util.Log.e("IntegratedLanternDriver", "Error during flashing", e)
                    break
                }
            }
        }
    }

    override fun stopFlashing() {
        flashingJob?.cancel()
        flashingJob = null
    }

    override fun isFlashingActive(): Boolean = flashingJob?.isActive == true

    private fun stopFlashingInternal() {
        flashingJob?.cancel()
        flashingJob = null
    }

    companion object {
        private const val DEFAULT_FLASH_ON_DURATION_MS = 100L
        private const val MIN_FLASH_PERIOD_MS = 200L
    }
}
