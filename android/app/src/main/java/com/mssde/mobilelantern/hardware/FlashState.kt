package com.mssde.mobilelantern.hardware

enum class LedColor(val r: Int, val g: Int, val b: Int) {
    WHITE(255, 255, 255),
    GREEN(0, 255, 0),
    RED(255, 0, 0)
}

sealed interface FlashState {
    data object Off : FlashState
    data class On(val color: LedColor = LedColor.WHITE) : FlashState
    data class Flashing(
        val periodMs: Long,
        val onDurationMs: Long,
        val color: LedColor = LedColor.WHITE
    ) : FlashState
}

