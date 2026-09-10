package com.eblan.launcher.domain.usecase.application

import com.eblan.launcher.domain.common.Dispatcher
import com.eblan.launcher.domain.common.EblanDispatchers
import com.eblan.launcher.domain.repository.FolderEblanApplicationInfoRepository
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

class GetFolderEblanApplicationInfosUseCase @Inject constructor(
    private val folderEblanApplicationInfoRepository: FolderEblanApplicationInfoRepository,
    @param:Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    operator fun invoke(){

    }
}