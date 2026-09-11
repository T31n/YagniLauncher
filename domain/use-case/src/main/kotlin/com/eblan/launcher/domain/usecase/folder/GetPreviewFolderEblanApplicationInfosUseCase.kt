package com.eblan.launcher.domain.usecase.folder

import com.eblan.launcher.domain.common.Dispatcher
import com.eblan.launcher.domain.common.EblanDispatchers
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.model.FolderEblanApplicationInfo
import com.eblan.launcher.domain.model.FolderEblanApplicationInfoGridItem
import com.eblan.launcher.domain.model.FolderEblanApplicationInfoGridItemData
import com.eblan.launcher.domain.model.FolderEblanApplicationInfoWrapper
import com.eblan.launcher.domain.model.PreviewFolderEblanApplicationInfo
import com.eblan.launcher.domain.repository.FolderEblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import com.eblan.launcher.domain.usecase.util.getGridDimension
import com.eblan.launcher.domain.usecase.util.getPreviewFolderGridItems
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class GetPreviewFolderEblanApplicationInfosUseCase @Inject constructor(
    private val folderEblanApplicationInfoRepository: FolderEblanApplicationInfoRepository,
    private val userDataRepository: UserDataRepository,
    @param:Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    operator fun invoke(): Flow<List<PreviewFolderEblanApplicationInfo>> = combine(
        userDataRepository.userDataFlow,
        folderEblanApplicationInfoRepository.folderEblanApplicationInfoWrappersFlow,
    ) { userData, folderEblanApplicationInfoWrappers ->
        folderEblanApplicationInfoWrappers.map {
            it.asFolderEblanApplicationInfoGridItem(
                maxFolderColumns = userData.homeSettings.maxFolderColumns,
                maxFolderRows = userData.homeSettings.maxFolderRows,
            )
        }
    }.flowOn(defaultDispatcher)

    private fun FolderEblanApplicationInfoWrapper.asFolderEblanApplicationInfoGridItem(
        maxFolderColumns: Int,
        maxFolderRows: Int,
    ): PreviewFolderEblanApplicationInfo {
        val folderEblanApplicationInfoGridItems = folderEblanApplicationInfos.map {
            it.asFolderEblanApplicationInfoGridItem()
        } + eblanApplicationInfos.map {
            it.asFolderEblanApplicationInfoGridItem()
        }

        val (columns, rows) = getGridDimension(
            count = folderEblanApplicationInfoGridItems.size,
            maxFolderColumns = maxFolderColumns,
            maxFolderRows = maxFolderRows,
        )

        val previewFolderGridItems = getPreviewFolderGridItems(
            columns = columns,
            rows = rows,
            folderGridItems = folderEblanApplicationInfoGridItems,
        )

        return PreviewFolderEblanApplicationInfo(
            folderEblanApplicationInfo = folderEblanApplicationInfo,
            previewFolderGridItems = previewFolderGridItems,
        )
    }

    private fun FolderEblanApplicationInfo.asFolderEblanApplicationInfoGridItem(): FolderEblanApplicationInfoGridItem =
        FolderEblanApplicationInfoGridItem(
            id = id,
            data = FolderEblanApplicationInfoGridItemData.Folder(
                icon = icon,
                label = label,
                folderIndex = folderIndex,
                folderId = folderId,
            ),
        )

    private fun EblanApplicationInfo.asFolderEblanApplicationInfoGridItem(): FolderEblanApplicationInfoGridItem =
        FolderEblanApplicationInfoGridItem(
            id = "$componentName $serialNumber",
            data = FolderEblanApplicationInfoGridItemData.ApplicationInfo(
                componentName = componentName,
                serialNumber = serialNumber,
                packageName = packageName,
                icon = icon,
                label = label,
                customIcon = customIcon,
                customLabel = customLabel,
                isHidden = isHidden,
                lastUpdateTime = lastUpdateTime,
                flags = flags,
                folderIndex = folderIndex,
                folderId = folderId,
            ),
        )
}