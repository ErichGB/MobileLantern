package com.mssde.mobilelantern.di

import com.mssde.mobilelantern.hardware.HardwareController
import com.mssde.mobilelantern.hardware.SessionManager
import com.mssde.mobilelantern.hardware.TimerService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HardwareModule {

    @Provides
    @Singleton
    fun provideSessionManager(): SessionManager {
        return SessionManager()
    }

    @Provides
    @Singleton
    fun provideTimerService(
        sessionManager: SessionManager,
        hardwareController: HardwareController
    ): TimerService {
        return TimerService(sessionManager, hardwareController)
    }
}