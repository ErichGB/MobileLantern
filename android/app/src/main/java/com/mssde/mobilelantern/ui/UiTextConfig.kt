package com.mssde.mobilelantern.ui

/**
 * Singleton para gestionar textos personalizables de las tarjetas de entrada desde el código QR.
 * 
 * Campos abreviados soportados:
 * - QuestionInputCard: qt (title), qs (subtitle), qp (placeholder)
 * - AnswerInputCard: at (title), as (subtitle), ap (placeholder)
 * - NewQuestionInputCard: nqt (title), nqs (subtitle), nqp (placeholder)
 */
object UiTextConfig {
    private val textMap = mutableMapOf<String, String>()
    
    /**
     * Establece un valor para una clave específica.
     */
    fun set(key: String, value: String) {
        textMap[key] = value
    }
    
    /**
     * Obtiene el valor asociado a una clave, o null si no existe.
     */
    fun get(key: String): String? {
        return textMap[key]
    }
    
    /**
     * Limpia todos los valores almacenados.
     * Debe llamarse antes de procesar un nuevo código QR.
     */
    fun clear() {
        textMap.clear()
    }
}

