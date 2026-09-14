package com.eblan.launcher.domain.usecase.application

import com.eblan.launcher.domain.common.Dispatcher
import com.eblan.launcher.domain.common.EblanDispatchers
import com.eblan.launcher.domain.model.application.EblanApplicationInfoTag
import com.eblan.launcher.domain.repository.EblanApplicationInfoTagRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetEblanApplicationTagsUseCase @Inject constructor(
    private val eblanApplicationInfoTagRepository: EblanApplicationInfoTagRepository,
    @param:Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    operator fun invoke(): Flow<List<EblanApplicationInfoTag>> =
        eblanApplicationInfoTagRepository.eblanApplicationInfoTagsFlow.map { eblanApplicationInfoTags ->
            eblanApplicationInfoTags.sortedBy { it.index }
        }.flowOn(defaultDispatcher)
}