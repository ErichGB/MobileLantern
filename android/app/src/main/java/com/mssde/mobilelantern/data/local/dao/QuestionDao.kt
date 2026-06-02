package com.mssde.mobilelantern.data.local.dao

import androidx.room.*
import com.mssde.mobilelantern.data.local.entity.QuestionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionDao {
    
    @Query("SELECT * FROM questions WHERE id_user = :userId ORDER BY moment DESC")
    fun getQuestionsByUser(userId: Int): Flow<List<QuestionEntity>>
    
    @Query("SELECT * FROM questions WHERE id_user = :userId ORDER BY moment DESC")
    suspend fun getQuestionsByUserSync(userId: Int): List<QuestionEntity>
    
    /**
     * Obtiene todas las preguntas sin filtrar por usuario (para modo aula)
     */
    @Query("SELECT * FROM questions ORDER BY moment DESC")
    fun getAllQuestions(): Flow<List<QuestionEntity>>
    
    /**
     * Obtiene todas las preguntas síncronamente sin filtrar por usuario (para modo aula)
     */
    @Query("SELECT * FROM questions ORDER BY moment DESC")
    suspend fun getAllQuestionsSync(): List<QuestionEntity>
    
    @Query("SELECT * FROM questions WHERE id = :questionId")
    suspend fun getQuestionById(questionId: Int): QuestionEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: QuestionEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<QuestionEntity>)
    
    @Update
    suspend fun updateQuestion(question: QuestionEntity)
    
    @Delete
    suspend fun deleteQuestion(question: QuestionEntity)
    
    @Query("DELETE FROM questions WHERE id_user = :userId")
    suspend fun deleteQuestionsByUser(userId: Int)

    @Query("DELETE FROM questions")
    suspend fun deleteAllQuestions()
    
    @Query("SELECT COUNT(*) FROM questions WHERE id_user = :userId")
    suspend fun getQuestionsCountByUser(userId: Int): Int
}
