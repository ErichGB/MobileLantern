package com.mssde.mobilelantern.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.mssde.mobilelantern.data.local.dao.QuestionDao
import com.mssde.mobilelantern.data.local.dao.QuestionInteractionDao
import com.mssde.mobilelantern.data.local.entity.QuestionEntity
import com.mssde.mobilelantern.data.local.entity.QuestionInteractionEntity

@Database(
    entities = [
        QuestionEntity::class,
        QuestionInteractionEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class MobileLanternDatabase : RoomDatabase() {
    
    abstract fun questionDao(): QuestionDao
    abstract fun questionInteractionDao(): QuestionInteractionDao
    
    companion object {
        @Volatile
        private var INSTANCE: MobileLanternDatabase? = null
        
        fun getDatabase(context: Context): MobileLanternDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MobileLanternDatabase::class.java,
                    "mobile_lantern_database"
                )
                .fallbackToDestructiveMigration() // Para desarrollo, cambiar en producción
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
