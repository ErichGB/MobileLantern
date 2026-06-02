package com.mssde.mobilelantern.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad Room para preguntas del historial
 */
@Entity(tableName = "questions")
data class QuestionEntity(
    @PrimaryKey
    val id: Int,
    
    @ColumnInfo(name = "id_user")
    val idUser: Int,
    
    @ColumnInfo(name = "moment")
    val moment: String,
    
    @ColumnInfo(name = "question")
    val question: String,
    
    @ColumnInfo(name = "context")
    val context: String? = null,
    
    @ColumnInfo(name = "texto")
    val texto: String? = null,
    
    @ColumnInfo(name = "rol")
    val rol: String? = null,
    
    @ColumnInfo(name = "correct")
    val correct: Int = 0,
    
    @ColumnInfo(name = "ia")
    val ia: Int = 0,
    
    @ColumnInfo(name = "teacher")
    val teacher: Int = 0,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
