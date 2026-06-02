package com.mssde.mobilelantern.domain.usecase

import com.mssde.mobilelantern.data.repository.LocalHistoryRepository
import com.mssde.mobilelantern.domain.repository.AuthRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class RefreshHistoryUseCase @Inject constructor(
    private val localHistoryRepository: LocalHistoryRepository,
    private val authRepository: AuthRepository
) {
    
    suspend operator fun invoke(): Result<Unit> {
        return try {
            val userId = authRepository.getStoredUserId().firstOrNull() ?: 0
            localHistoryRepository.syncQuestionsFromServer(userId)
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("RefreshHistory", "Error: ${e.message}")
            Result.failure(e)
        }
    }
}

