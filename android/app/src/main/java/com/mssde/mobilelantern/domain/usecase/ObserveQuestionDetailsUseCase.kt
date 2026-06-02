package com.mssde.mobilelantern.domain.usecase

import com.mssde.mobilelantern.data.local.entity.QuestionInteractionEntity
import com.mssde.mobilelantern.data.repository.LocalHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObserveQuestionDetailsUseCase @Inject constructor(
    private val localHistoryRepository: LocalHistoryRepository
) {
    
    operator fun invoke(questionId: Int): Flow<List<QuestionInteractionEntity>> {
        return localHistoryRepository.getQuestionInteractionsFlow(questionId)
            .map { interactions ->
                if (interactions.isNotEmpty() && interactions.first().rol == "system") {
                    interactions.drop(1)
                } else {
                    interactions
                }
            }
    }
}

