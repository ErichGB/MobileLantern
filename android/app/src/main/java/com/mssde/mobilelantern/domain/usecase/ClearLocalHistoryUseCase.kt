package com.mssde.mobilelantern.domain.usecase

import com.mssde.mobilelantern.data.repository.LocalHistoryRepository
import com.mssde.mobilelantern.domain.repository.AuthRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class ClearLocalHistoryUseCase @Inject constructor(
    private val localHistoryRepository: LocalHistoryRepository,
    private val authRepository: AuthRepository
) {

    suspend operator fun invoke() {
        val userId = authRepository.getStoredUserId().firstOrNull() ?: 0
        val aula = authRepository.getStoredAula().firstOrNull()
        localHistoryRepository.clearLocalHistory(userId, aula)
    }
}
