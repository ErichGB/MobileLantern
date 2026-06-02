package com.mssde.mobilelantern.hardware

import com.mssde.mobilelantern.viewmodel.BluetoothViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExternalLanternDriver @Inject constructor(
    private val bluetoothViewModel: BluetoothViewModel
) : LanternDriver {

    private val _driverError = MutableStateFlow<LanternDriverError?>(null)
    val driverError: StateFlow<LanternDriverError?> = _driverError.asStateFlow()

    override val requiresPhysicalGlassChange: Boolean = false

    fun clearDriverError() {
        _driverError.value = null
    }

    override fun turnOn() {
        sendOrSetError("LED_ON")
    }

    override fun turnOnWithColor(color: LedColor) {
        sendOrSetError("COLOR:${color.r},${color.g},${color.b}")
    }

    override fun setColor(color: LedColor) {
        sendOrSetError("COLOR:${color.r},${color.g},${color.b}")
    }

    override fun setEffect(effect: String) {
        sendOrSetError("EFFECT:$effect")
    }

    override fun turnOff() {
        sendOrSetError("LED_OFF")
    }

    override fun startFlashing(frequencyMs: Long) {
        sendOrSetError("EFFECT:PULSE")
    }

    override fun stopFlashing() {
        sendOrSetError("EFFECT:NONE")
    }

    override fun isFlashingActive(): Boolean = false

    private fun sendOrSetError(command: String) {
        if (!bluetoothViewModel.connectionState.value.isConnected) {
            _driverError.value = LanternDriverError.Disconnected
            android.util.Log.d("ExternalLanternDriver", "No conectado, comando omitido: $command")
            return
        }
        _driverError.value = null
        bluetoothViewModel.sendCommand(command)
    }
}
