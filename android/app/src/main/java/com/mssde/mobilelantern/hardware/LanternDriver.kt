package com.mssde.mobilelantern.hardware

sealed class LanternDriverError {
    data object Disconnected : LanternDriverError()
}

interface LanternDriver {
    fun turnOn()
    fun turnOnWithColor(color: LedColor)
    fun setColor(color: LedColor)
    fun setEffect(effect: String)
    fun turnOff()
    fun startFlashing(frequencyMs: Long)
    fun stopFlashing()
    fun isFlashingActive(): Boolean
    val requiresPhysicalGlassChange: Boolean
}
