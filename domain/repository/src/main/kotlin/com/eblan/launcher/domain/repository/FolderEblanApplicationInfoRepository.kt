package com.eblan.launcher.domain.repository

import com.eblan.launcher.domain.model.FolderEblanApplicationInfo
import com.eblan.launcher.domain.model.FolderEblanApplicationInfoWrapper
import kotlinx.coroutines.flow.Flow

interface FolderEblanApplicationInfoRepository {
    val folderEblanApplicationInfoWrappersFlow : Flow<List<FolderEblanApplicationInfoWrapper>>

    fun getFolderEblanApplicationInfos(): List<FolderEblanApplicationInfo>

    suspend fun getFolderEblanApplicationInfoWrapper(id: String): FolderEblanApplicationInfoWrapper?
}