package com.mssde.mobilelantern.di

import com.mssde.mobilelantern.data.repository.AuthRepositoryImpl
import com.mssde.mobilelantern.data.repository.DrawerRepositoryImpl
import com.mssde.mobilelantern.data.repository.LocalHistoryRepository
import com.mssde.mobilelantern.data.repository.QuestionRepository
import com.mssde.mobilelantern.data.repository.QuestionRepositoryImpl
import com.mssde.mobilelantern.domain.repository.AuthRepository
import com.mssde.mobilelantern.domain.repository.DrawerRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindQuestionRepository(
        questionRepositoryImpl: QuestionRepositoryImpl
    ): QuestionRepository

    @Binds
    @Singleton
    abstract fun bindDrawerRepository(
        drawerRepositoryImpl: DrawerRepositoryImpl
    ): DrawerRepository
} 