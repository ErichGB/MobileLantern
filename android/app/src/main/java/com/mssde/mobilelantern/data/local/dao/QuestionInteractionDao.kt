package com.mssde.mobilelantern.data.local.dao

import androidx.room.*
import com.mssde.mobilelantern.data.local.entity.QuestionInteractionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionInteractionDao {
    
    @Query("SELECT * FROM question_interactions WHERE question_id = :questionId ORDER BY moment ASC")
    fun getInteractionsByQuestion(questionId: Int): Flow<List<QuestionInteractionEntity>>
    
    @Query("SELECT * FROM question_interactions WHERE question_id = :questionId ORDER BY moment ASC")
    suspend fun getInteractionsByQuestionSync(questionId: Int): List<QuestionInteractionEntity>
    
    @Query("SELECT * FROM question_interactions WHERE id = :interactionId")
    suspend fun getInteractionById(interactionId: Int): QuestionInteractionEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInteraction(interaction: QuestionInteractionEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInteractions(interactions: List<QuestionInteractionEntity>)
    
    @Update
    suspend fun updateInteraction(interaction: QuestionInteractionEntity)
    
    @Delete
    suspend fun deleteInteraction(interaction: QuestionInteractionEntity)
    
    @Query("DELETE FROM question_interactions WHERE question_id = :questionId")
    suspend fun deleteInteractionsByQuestion(questionId: Int)
    
    @Query("SELECT COUNT(*) FROM question_interactions WHERE question_id = :questionId")
    suspend fun getInteractionsCountByQuestion(questionId: Int): Int
    
    /**
     * Verifica si ya existen interacciones locales para una pregunta
     */
    @Query("SELECT EXISTS(SELECT 1 FROM question_interactions WHERE question_id = :questionId)")
    suspend fun hasInteractionsForQuestion(questionId: Int): Boolean
}
