package com.mssde.mobilelantern.domain.usecase

import com.mssde.mobilelantern.data.model.HistoryQuestion
import com.mssde.mobilelantern.data.repository.LocalHistoryRepository
import com.mssde.mobilelantern.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ObserveHistoryUseCase @Inject constructor(
    private val localHistoryRepository: LocalHistoryRepository,
    private val authRepository: AuthRepository
) {
    
    operator fun invoke(): Flow<List<HistoryQuestion>> = flow {
        val userId = authRepository.getStoredUserId().firstOrNull() ?: 0
        localHistoryRepository.getQuestionsFlow(userId).collect { questions ->
            emit(questions)
        }
    }
}

