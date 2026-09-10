/*
 *
 *   Copyright 2023 Einstein Blanco
 *
 *   Licensed under the GNU General Public License v3.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       https://www.gnu.org/licenses/gpl-3.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */
package com.eblan.launcher.domain.usecase.launcherapps

import com.eblan.launcher.domain.common.FileManager
import com.eblan.launcher.domain.common.IconKeyGenerator
import com.eblan.launcher.domain.framework.PackageManagerWrapper
import com.eblan.launcher.domain.grid.findAvailableRegionByPage
import com.eblan.launcher.domain.model.AddNewEblanApplicationInfo
import com.eblan.launcher.domain.model.AppWidgetManagerAppWidgetProviderInfo
import com.eblan.launcher.domain.model.ApplicationInfoGridItem
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.DeleteEblanAppWidgetProviderInfo
import com.eblan.launcher.domain.model.DeleteEblanApplicationInfo
import com.eblan.launcher.domain.model.DeleteEblanShortcutConfig
import com.eblan.launcher.domain.model.DeleteEblanShortcutInfo
import com.eblan.launcher.domain.model.EblanAction
import com.eblan.launcher.domain.model.EblanActionType
import com.eblan.launcher.domain.model.EblanAppWidgetProviderInfo
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.model.EblanShortcutConfig
import com.eblan.launcher.domain.model.EblanShortcutInfo
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData
import com.eblan.launcher.domain.model.HomeSettings
import com.eblan.launcher.domain.model.LauncherAppsActivityInfo
import com.eblan.launcher.domain.model.LauncherAppsShortcutInfo
import com.eblan.launcher.domain.model.PartialApplicationInfoGridItem
import com.eblan.launcher.domain.model.PartialShortcutConfigGridItem
import com.eblan.launcher.domain.model.PartialShortcutInfoGridItem
import com.eblan.launcher.domain.model.PartialUpdateWidgetGridItem
import com.eblan.launcher.domain.model.ShortcutConfigActivityInfo
import com.eblan.launcher.domain.model.ShortcutConfigGridItem
import com.eblan.launcher.domain.model.ShortcutInfoGridItem
import com.eblan.launcher.domain.model.SyncEblanApplicationInfo
import com.eblan.launcher.domain.model.WidgetGridItem
import com.eblan.launcher.domain.repository.ApplicationInfoGridItemRepository
import com.eblan.launcher.domain.repository.FolderGridItemRepository
import com.eblan.launcher.domain.repository.ShortcutConfigGridItemRepository
import com.eblan.launcher.domain.repository.ShortcutInfoGridItemRepository
import com.eblan.launcher.domain.repository.WidgetGridItemRepository
import com.eblan.launcher.domain.usecase.util.getFolderGridItemsById
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import java.io.File
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal suspend fun deleteEblanApplicationInfoIcons(
    eblanApplicationInfos: List<EblanApplicationInfo>,
    eblanAppWidgetProviderInfos: List<EblanAppWidgetProviderInfo>,
    oldDeleteEblanApplicationInfos: List<DeleteEblanApplicationInfo>,
) {
    oldDeleteEblanApplicationInfos.forEach { oldDeleteEblanApplicationInfo ->
        currentCoroutineContext().ensureActive()

        val icon = oldDeleteEblanApplicationInfo.icon

        val hasNoIconReference =
            icon != null && eblanApplicationInfos.none {
                it.icon == icon
            } && eblanAppWidgetProviderInfos.none {
                it.applicationIcon == icon
            }

        if (hasNoIconReference) {
            val iconFile = File(icon)

            if (iconFile.exists()) {
                iconFile.delete()
            }
        }
    }
}

internal suspend fun deleteEblanAppWidgetProviderInfoIcons(
    eblanApplicationInfos: List<EblanApplicationInfo>,
    eblanAppWidgetProviderInfos: List<EblanAppWidgetProviderInfo>,
    oldDeleteEblanAppWidgetProviderInfos: List<DeleteEblanAppWidgetProviderInfo>,
) {
    oldDeleteEblanAppWidgetProviderInfos.forEach { deleteEblanAppWidgetProviderInfo ->
        currentCoroutineContext().ensureActive()

        val applicationIcon = deleteEblanAppWidgetProviderInfo.applicationIcon

        val hasNoIconReference =
            applicationIcon != null && eblanAppWidgetProviderInfos.none {
                it.applicationIcon == applicationIcon
            } && eblanApplicationInfos.none {
                it.icon == applicationIcon
            }

        if (hasNoIconReference) {
            val iconFile = File(applicationIcon)

            if (iconFile.exists()) {
                iconFile.delete()
            }
        }

        deleteEblanAppWidgetProviderInfo.preview?.let {
            val previewFile = File(it)

            if (previewFile.exists()) {
                previewFile.delete()
            }
        }
    }
}

internal suspend fun deleteEblanShortInfoIcons(
    eblanShortcutInfos: List<EblanShortcutInfo>,
    oldDeleteEblanShortcutInfos: List<DeleteEblanShortcutInfo>,
) {
    oldDeleteEblanShortcutInfos.forEach { deleteEblanShortcutInfo ->
        currentCoroutineContext().ensureActive()

        val icon = deleteEblanShortcutInfo.icon

        val hasNoIconReference =
            icon != null && eblanShortcutInfos.none {
                it.icon == icon
            }

        if (hasNoIconReference) {
            val iconFile = File(icon)

            if (iconFile.exists()) {
                iconFile.delete()
            }
        }
    }
}

internal suspend fun deleteEblanShortcutConfigIcons(oldDeleteEblanShortcutConfigs: List<DeleteEblanShortcutConfig>) {
    oldDeleteEblanShortcutConfigs.forEach { deleteEblanShortcutConfig ->
        currentCoroutineContext().ensureActive()

        val activityIcon = deleteEblanShortcutConfig.activityIcon

        if (activityIcon != null) {
            val activityIconFile = File(activityIcon)

            if (activityIconFile.exists()) {
                activityIconFile.delete()
            }
        }
    }
}

internal suspend fun updateApplicationInfoGridItems(
    eblanApplicationInfos: List<EblanApplicationInfo>,
    applicationInfoGridItemRepository: ApplicationInfoGridItemRepository,
) {
    val partialApplicationInfoGridItems = mutableListOf<PartialApplicationInfoGridItem>()

    val deleteApplicationInfoGridItems = mutableListOf<ApplicationInfoGridItem>()

    val applicationInfoGridItems =
        applicationInfoGridItemRepository.getApplicationInfoGridItems()

    applicationInfoGridItems.filterNot {
        it.override
    }.forEach { applicationInfoGridItem ->
        currentCoroutineContext().ensureActive()

        val eblanApplicationInfo = eblanApplicationInfos.find {
            it.serialNumber == applicationInfoGridItem.serialNumber && it.componentName == applicationInfoGridItem.componentName
        }

        if (eblanApplicationInfo != null) {
            partialApplicationInfoGridItems.add(
                PartialApplicationInfoGridItem(
                    id = applicationInfoGridItem.id,
                    componentName = eblanApplicationInfo.componentName,
                    icon = eblanApplicationInfo.icon,
                    label = eblanApplicationInfo.label,
                ),
            )
        } else {
            deleteApplicationInfoGridItems.add(applicationInfoGridItem)
        }
    }

    applicationInfoGridItemRepository.updatePartialApplicationInfoGridItems(
        partialApplicationInfoGridItems = partialApplicationInfoGridItems,
    )

    applicationInfoGridItemRepository.deleteApplicationInfoGridItems(
        applicationInfoGridItems = deleteApplicationInfoGridItems,
    )
}

internal suspend fun updateShortcutInfoGridItems(
    eblanShortcutInfos: List<EblanShortcutInfo>?,
    shortcutInfoGridItemRepository: ShortcutInfoGridItemRepository,
    fileManager: FileManager,
    packageManagerWrapper: PackageManagerWrapper,
    iconKeyGenerator: IconKeyGenerator,
) {
    val partialShortcutInfoGridItems = mutableListOf<PartialShortcutInfoGridItem>()

    val deleteShortcutInfoGridItems = mutableListOf<ShortcutInfoGridItem>()

    val shortcutInfoGridItems = shortcutInfoGridItemRepository.getShortcutInfoGridItems()

    if (eblanShortcutInfos != null) {
        shortcutInfoGridItems.filterNot {
            it.override
        }.forEach { shortcutInfoGridItem ->
            currentCoroutineContext().ensureActive()

            val eblanShortcutInfo = eblanShortcutInfos.find {
                it.serialNumber == shortcutInfoGridItem.serialNumber && it.shortcutId == shortcutInfoGridItem.shortcutId
            }

            if (eblanShortcutInfo != null) {
                partialShortcutInfoGridItems.add(
                    PartialShortcutInfoGridItem(
                        id = shortcutInfoGridItem.id,
                        shortLabel = eblanShortcutInfo.shortLabel,
                        longLabel = eblanShortcutInfo.longLabel,
                        isEnabled = eblanShortcutInfo.isEnabled,
                        icon = eblanShortcutInfo.icon,
                        eblanApplicationInfoIcon = resolveApplicationIcon(
                            fileManager = fileManager,
                            packageManagerWrapper = packageManagerWrapper,
                            iconKeyGenerator = iconKeyGenerator,
                            serialNumber = eblanShortcutInfo.serialNumber,
                            packageName = eblanShortcutInfo.packageName,
                        ),
                    ),
                )
            } else {
                deleteShortcutInfoGridItems.add(shortcutInfoGridItem)
            }
        }

        shortcutInfoGridItemRepository.updatePartialShortcutInfoGridItems(
            partialShortcutInfoGridItems = partialShortcutInfoGridItems,
        )

        shortcutInfoGridItemRepository.deleteShortcutInfoGridItems(shortcutInfoGridItems = deleteShortcutInfoGridItems)
    }
}

internal suspend fun updateShortcutConfigGridItems(
    eblanShortcutConfigs: List<EblanShortcutConfig>,
    shortcutConfigGridItemRepository: ShortcutConfigGridItemRepository,
    fileManager: FileManager,
    packageManagerWrapper: PackageManagerWrapper,
    iconKeyGenerator: IconKeyGenerator,
) {
    val partialShortcutConfigGridItems = mutableListOf<PartialShortcutConfigGridItem>()

    val deleteShortcutConfigGridItems = mutableListOf<ShortcutConfigGridItem>()

    val shortcutConfigGridItems = shortcutConfigGridItemRepository.getShortcutConfigGridItems()

    shortcutConfigGridItems.filterNot {
        it.override
    }.forEach { shortcutConfigGridItem ->
        currentCoroutineContext().ensureActive()

        val eblanShortcutConfig = eblanShortcutConfigs.find {
            it.serialNumber == shortcutConfigGridItem.serialNumber && it.componentName == shortcutConfigGridItem.componentName
        }

        if (eblanShortcutConfig != null) {
            partialShortcutConfigGridItems.add(
                PartialShortcutConfigGridItem(
                    id = shortcutConfigGridItem.id,
                    componentName = eblanShortcutConfig.componentName,
                    activityLabel = eblanShortcutConfig.activityLabel,
                    activityIcon = eblanShortcutConfig.activityIcon,
                    applicationLabel = packageManagerWrapper.getApplicationLabel(
                        packageName = eblanShortcutConfig.packageName,
                    ),
                    applicationIcon = resolveApplicationIcon(
                        fileManager = fileManager,
                        packageManagerWrapper = packageManagerWrapper,
                        iconKeyGenerator = iconKeyGenerator,
                        serialNumber = eblanShortcutConfig.serialNumber,
                        packageName = eblanShortcutConfig.packageName,
                    ),
                ),
            )
        } else {
            deleteShortcutConfigGridItems.add(shortcutConfigGridItem)
        }
    }

    shortcutConfigGridItemRepository.updatePartialShortcutConfigGridItems(
        partialShortcutConfigGridItems = partialShortcutConfigGridItems,
    )

    shortcutConfigGridItemRepository.deleteShortcutConfigGridItems(
        shortcutConfigGridItems = deleteShortcutConfigGridItems,
    )
}

internal suspend fun updateWidgetGridItems(
    eblanAppWidgetProviderInfos: List<EblanAppWidgetProviderInfo>,
    fileManager: FileManager,
    packageManagerWrapper: PackageManagerWrapper,
    widgetGridItemRepository: WidgetGridItemRepository,
    iconKeyGenerator: IconKeyGenerator,
) {
    if (!packageManagerWrapper.hasSystemFeatureAppWidgets) return

    val partialUpdateWidgetGridItems = mutableListOf<PartialUpdateWidgetGridItem>()

    val deleteWidgetGridItems = mutableListOf<WidgetGridItem>()

    val widgetGridItems = widgetGridItemRepository.getWidgetGridItems()

    widgetGridItems.filterNot {
        it.override
    }.forEach { widgetGridItem ->
        currentCoroutineContext().ensureActive()

        val eblanAppWidgetProviderInfo =
            eblanAppWidgetProviderInfos.find {
                it.serialNumber == widgetGridItem.serialNumber && it.componentName == widgetGridItem.componentName
            }

        if (eblanAppWidgetProviderInfo != null) {
            partialUpdateWidgetGridItems.add(
                PartialUpdateWidgetGridItem(
                    id = widgetGridItem.id,
                    componentName = eblanAppWidgetProviderInfo.componentName,
                    configure = eblanAppWidgetProviderInfo.configure,
                    minWidth = eblanAppWidgetProviderInfo.minWidth,
                    minHeight = eblanAppWidgetProviderInfo.minHeight,
                    resizeMode = eblanAppWidgetProviderInfo.resizeMode,
                    minResizeWidth = eblanAppWidgetProviderInfo.minResizeWidth,
                    minResizeHeight = eblanAppWidgetProviderInfo.minResizeHeight,
                    maxResizeWidth = eblanAppWidgetProviderInfo.maxResizeWidth,
                    maxResizeHeight = eblanAppWidgetProviderInfo.maxResizeHeight,
                    targetCellHeight = eblanAppWidgetProviderInfo.targetCellHeight,
                    targetCellWidth = eblanAppWidgetProviderInfo.targetCellWidth,
                    icon = resolveApplicationIcon(
                        fileManager = fileManager,
                        packageManagerWrapper = packageManagerWrapper,
                        iconKeyGenerator = iconKeyGenerator,
                        serialNumber = eblanAppWidgetProviderInfo.serialNumber,
                        packageName = eblanAppWidgetProviderInfo.packageName,
                    ),
                    label = packageManagerWrapper.getApplicationLabel(
                        packageName = eblanAppWidgetProviderInfo.packageName,
                    ),
                ),
            )
        } else {
            deleteWidgetGridItems.add(widgetGridItem)
        }
    }

    widgetGridItemRepository.updatePartialWidgetGridItems(partialUpdateWidgetGridItems = partialUpdateWidgetGridItems)

    widgetGridItemRepository.deleteWidgetGridItemsByPackageName(widgetGridItems = deleteWidgetGridItems)
}

internal suspend fun AppWidgetManagerAppWidgetProviderInfo.toEblanAppWidgetProviderInfo(
    fileManager: FileManager,
    packageManagerWrapper: PackageManagerWrapper,
    iconKeyGenerator: IconKeyGenerator,
): EblanAppWidgetProviderInfo = EblanAppWidgetProviderInfo(
    componentName = componentName,
    serialNumber = serialNumber,
    configure = configure,
    packageName = packageName,
    targetCellWidth = targetCellWidth,
    targetCellHeight = targetCellHeight,
    minWidth = minWidth,
    minHeight = minHeight,
    resizeMode = resizeMode,
    minResizeWidth = minResizeWidth,
    minResizeHeight = minResizeHeight,
    maxResizeWidth = maxResizeWidth,
    maxResizeHeight = maxResizeHeight,
    preview = preview,
    applicationIcon = resolveApplicationIcon(
        fileManager = fileManager,
        packageManagerWrapper = packageManagerWrapper,
        iconKeyGenerator = iconKeyGenerator,
        serialNumber = serialNumber,
        packageName = packageName,
    ),
    applicationLabel = packageManagerWrapper.getApplicationLabel(
        packageName = packageName,
    ),
    lastUpdateTime = lastUpdateTime,
    label = label,
    description = description,
)

internal fun EblanApplicationInfo.toSyncEblanApplicationInfo() = SyncEblanApplicationInfo(
    serialNumber = serialNumber,
    componentName = componentName,
    packageName = packageName,
    icon = icon,
    label = label,
    lastUpdateTime = lastUpdateTime,
    flags = flags,
)

internal fun LauncherAppsActivityInfo.toSyncEblanApplicationInfo() = SyncEblanApplicationInfo(
    serialNumber = serialNumber,
    componentName = componentName,
    packageName = packageName,
    icon = activityIcon,
    label = activityLabel,
    lastUpdateTime = lastUpdateTime,
    flags = flags,
)

internal fun SyncEblanApplicationInfo.toDeleteEblanApplicationInfo() = DeleteEblanApplicationInfo(
    serialNumber = serialNumber,
    componentName = componentName,
    packageName = packageName,
    icon = icon,
)

internal suspend fun ShortcutConfigActivityInfo.toEblanShortcutConfig(
    fileManager: FileManager,
    packageManagerWrapper: PackageManagerWrapper,
    iconKeyGenerator: IconKeyGenerator,
): EblanShortcutConfig = EblanShortcutConfig(
    componentName = componentName,
    packageName = packageName,
    serialNumber = serialNumber,
    activityIcon = activityIcon,
    activityLabel = activityLabel,
    applicationIcon = resolveApplicationIcon(
        fileManager = fileManager,
        packageManagerWrapper = packageManagerWrapper,
        iconKeyGenerator = iconKeyGenerator,
        serialNumber = serialNumber,
        packageName = packageName,
    ),
    applicationLabel = packageManagerWrapper.getApplicationLabel(
        packageName = packageName,
    ),
)

internal fun EblanAppWidgetProviderInfo.toDeleteEblanAppWidgetProviderInfo(): DeleteEblanAppWidgetProviderInfo = DeleteEblanAppWidgetProviderInfo(
    componentName = componentName,
    serialNumber = serialNumber,
    packageName = packageName,
    preview = preview,
    applicationIcon = applicationIcon,
)

internal fun LauncherAppsShortcutInfo.toEblanShortcutInfo(): EblanShortcutInfo = EblanShortcutInfo(
    shortcutId = shortcutId,
    serialNumber = serialNumber,
    packageName = packageName,
    shortLabel = shortLabel,
    longLabel = longLabel,
    icon = icon,
    shortcutQueryFlag = shortcutQueryFlag,
    isEnabled = isEnabled,
    lastChangedTimestamp = lastChangedTimestamp,
)

internal fun EblanShortcutInfo.toDeleteEblanShortcutInfo(): DeleteEblanShortcutInfo = DeleteEblanShortcutInfo(
    serialNumber = serialNumber,
    shortcutId = shortcutId,
    packageName = packageName,
    icon = icon,
)

internal fun EblanShortcutConfig.toDeleteEblanShortcutConfig(): DeleteEblanShortcutConfig = DeleteEblanShortcutConfig(
    componentName = componentName,
    packageName = packageName,
    serialNumber = serialNumber,
    activityIcon = activityIcon,
)

@OptIn(ExperimentalUuidApi::class)
internal suspend fun addNewApplicationToHomeScreen(
    gridItems: MutableList<GridItem>,
    componentName: String,
    packageName: String,
    icon: String?,
    label: String,
    homeSettings: HomeSettings,
    applicationInfoGridItems: MutableList<ApplicationInfoGridItem>,
    folderGridItemRepository: FolderGridItemRepository,
) {
    val alreadyOnHome = gridItems.any {
        when (val data = it.data) {
            is GridItemData.ApplicationInfo ->
                data.serialNumber == 0L &&
                    data.componentName == componentName

            is GridItemData.Folder -> {
                val folderGridItems = getFolderGridItemsById(
                    folderGridItemRepository = folderGridItemRepository,
                    folderId = it.id,
                )

                folderGridItems.any { folderGridItem ->
                    when (val folderData = folderGridItem.data) {
                        is GridItemData.ApplicationInfo -> {
                            folderData.serialNumber == 0L &&
                                folderData.componentName == componentName
                        }

                        else -> false
                    }
                }
            }

            else -> false
        }
    }

    if (alreadyOnHome) return

    val eblanAction = EblanAction(
        eblanActionType = EblanActionType.None,
        serialNumber = 0L,
        componentName = "",
    )

    val data = GridItemData.ApplicationInfo(
        serialNumber = 0L,
        componentName = componentName,
        packageName = packageName,
        icon = icon,
        label = label,
        customIcon = null,
        customLabel = null,
        index = -1,
        folderId = null,
    )

    val gridItem = GridItem(
        id = Uuid.random().toHexString(),
        page = homeSettings.initialPage,
        startColumn = 0,
        startRow = 0,
        columnSpan = 1,
        rowSpan = 1,
        data = data,
        associate = Associate.Grid,
        override = false,
        gridItemSettings = homeSettings.gridItemSettings,
        doubleTap = eblanAction,
        swipeUp = eblanAction,
        swipeDown = eblanAction,
    )

    val newGridItem = findAvailableRegionByPage(
        gridItems = gridItems,
        gridItem = gridItem,
        pageCount = homeSettings.pageCount,
        columns = homeSettings.columns,
        rows = homeSettings.rows,
    )

    if (newGridItem != null) {
        gridItems.add(newGridItem)

        applicationInfoGridItems.add(
            ApplicationInfoGridItem(
                id = newGridItem.id,
                page = newGridItem.page,
                startColumn = newGridItem.startColumn,
                startRow = newGridItem.startRow,
                columnSpan = newGridItem.columnSpan,
                rowSpan = newGridItem.rowSpan,
                associate = newGridItem.associate,
                componentName = data.componentName,
                packageName = data.packageName,
                icon = data.icon,
                label = data.label,
                override = newGridItem.override,
                serialNumber = data.serialNumber,
                customIcon = data.customIcon,
                customLabel = data.customLabel,
                gridItemSettings = newGridItem.gridItemSettings,
                doubleTap = newGridItem.doubleTap,
                swipeUp = newGridItem.swipeUp,
                swipeDown = newGridItem.swipeDown,
                index = data.index,
                folderId = data.folderId,
            ),
        )
    }
}

internal fun SyncEblanApplicationInfo.asAddNewEblanApplicationInfo(): AddNewEblanApplicationInfo = AddNewEblanApplicationInfo(
    serialNumber = serialNumber,
    componentName = componentName,
    packageName = packageName,
    icon = icon,
    label = label,
)

private suspend fun resolveApplicationIcon(
    fileManager: FileManager,
    packageManagerWrapper: PackageManagerWrapper,
    iconKeyGenerator: IconKeyGenerator,
    serialNumber: Long,
    packageName: String,
): String? {
    val directory = fileManager.getFilesDirectory(FileManager.ICONS_DIR)

    val componentName = packageManagerWrapper.getComponentName(packageName = packageName)

    return if (componentName != null) {
        File(
            directory,
            iconKeyGenerator.getActivityIconKey(
                serialNumber = serialNumber,
                componentName = componentName,
            ),
        ).absolutePath
    } else {
        val file =
            File(
                directory,
                iconKeyGenerator.getActivityIconKey(
                    serialNumber = serialNumber,
                    componentName = packageName,
                ),
            )

        packageManagerWrapper.getApplicationIcon(packageName = packageName, file = file)
    }
}
