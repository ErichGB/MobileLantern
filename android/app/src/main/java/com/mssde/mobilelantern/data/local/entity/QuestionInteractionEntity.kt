package com.mssde.mobilelantern.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad Room para interacciones de una pregunta específica
 * Almacena el detalle completo obtenido de /ia/question/{id}
 */
@Entity(
    tableName = "question_interactions",
    foreignKeys = [
        ForeignKey(
            entity = QuestionEntity::class,
            parentColumns = ["id"],
            childColumns = ["question_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["question_id"])
    ]
)
data class QuestionInteractionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    
    @ColumnInfo(name = "question_id")
    val questionId: Int,
    
    @ColumnInfo(name = "id_user")
    val idUser: Int,
    
    @ColumnInfo(name = "moment")
    val moment: String,
    
    @ColumnInfo(name = "question")
    val question: String? = null,
    
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
    val createdAt: Long = System.currentTimeMillis()
)
