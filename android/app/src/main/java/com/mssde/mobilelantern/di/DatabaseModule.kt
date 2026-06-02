package com.mssde.mobilelantern.di

import android.content.Context
import androidx.room.Room
import com.mssde.mobilelantern.data.local.MobileLanternDatabase
import com.mssde.mobilelantern.data.local.dao.QuestionDao
import com.mssde.mobilelantern.data.local.dao.QuestionInteractionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideMobileLanternDatabase(@ApplicationContext context: Context): MobileLanternDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            MobileLanternDatabase::class.java,
            "mobile_lantern_database"
        )
        .fallbackToDestructiveMigration() // Para desarrollo, cambiar en producción
        .build()
    }
    
    @Provides
    fun provideQuestionDao(database: MobileLanternDatabase): QuestionDao {
        return database.questionDao()
    }
    
    @Provides
    fun provideQuestionInteractionDao(database: MobileLanternDatabase): QuestionInteractionDao {
        return database.questionInteractionDao()
    }
}
