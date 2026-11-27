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

import com.eblan.launcher.domain.grid.findAvailableRegionByPage
import com.eblan.launcher.domain.model.application.AddNewEblanApplicationInfo
import com.eblan.launcher.domain.model.application.DeleteEblanApplicationInfo
import com.eblan.launcher.domain.model.application.EblanApplicationInfo
import com.eblan.launcher.domain.model.application.SyncEblanApplicationInfo
import com.eblan.launcher.domain.model.grid.ApplicationInfoGridItem
import com.eblan.launcher.domain.model.grid.Associate
import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.domain.model.grid.GridItemData
import com.eblan.launcher.domain.model.grid.GridItemSettings
import com.eblan.launcher.domain.model.grid.PartialApplicationInfoGridItem
import com.eblan.launcher.domain.model.grid.PartialShortcutConfigGridItem
import com.eblan.launcher.domain.model.grid.PartialShortcutInfoGridItem
import com.eblan.launcher.domain.model.grid.ShortcutConfigGridItem
import com.eblan.launcher.domain.model.grid.ShortcutInfoGridItem
import com.eblan.launcher.domain.model.grid.WidgetGridItem
import com.eblan.launcher.domain.model.launcherapps.LauncherAppsActivityInfo
import com.eblan.launcher.domain.model.launcherapps.LauncherAppsShortcutInfo
import com.eblan.launcher.domain.model.launcherapps.ShortcutConfigActivityInfo
import com.eblan.launcher.domain.model.shortcutconfig.EblanShortcutConfig
import com.eblan.launcher.domain.model.shortcutinfo.EblanShortcutInfo
import com.eblan.launcher.domain.model.userdata.EblanAction
import com.eblan.launcher.domain.model.userdata.EblanActionType
import com.eblan.launcher.domain.model.widget.AppWidgetManagerAppWidgetProviderInfo
import com.eblan.launcher.domain.model.widget.EblanAppWidgetProviderInfo
import com.eblan.launcher.domain.model.widget.PartialUpdateWidgetGridItem
import com.eblan.launcher.domain.platform.FileManager
import com.eblan.launcher.domain.platform.IconKeyGenerator
import com.eblan.launcher.domain.platform.LauncherAppsWrapper
import com.eblan.launcher.domain.platform.PackageManagerWrapper
import com.eblan.launcher.domain.repository.ApplicationInfoGridItemRepository
import com.eblan.launcher.domain.repository.FolderGridItemRepository
import com.eblan.launcher.domain.repository.ShortcutConfigGridItemRepository
import com.eblan.launcher.domain.repository.ShortcutInfoGridItemRepository
import com.eblan.launcher.domain.repository.WidgetGridItemRepository
import com.eblan.launcher.domain.usecase.folder.asPreviewFolders
import com.eblan.launcher.domain.usecase.util.getRecursiveFolderGridItems
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import java.io.File
import kotlin.uuid.Uuid

/** Updates non-overridden application grid items and removes items whose applications disappeared. */
internal suspend fun updateApplicationInfoGridItems(
    eblanApplicationInfos: List<EblanApplicationInfo>,
    applicationInfoGridItemRepository: ApplicationInfoGridItemRepository,
) {
    val partialApplicationInfoGridItems = mutableListOf<PartialApplicationInfoGridItem>()

    val deleteApplicationInfoGridItems = mutableListOf<ApplicationInfoGridItem>()

    val applicationInfoGridItems =
        applicationInfoGridItemRepository.getApplicationInfoGridItems()

    applicationInfoGridItems.filterNot { currentApplicationInfoGridItem ->
        currentApplicationInfoGridItem.override
    }.forEach { applicationInfoGridItem ->
        currentCoroutineContext().ensureActive()

        val eblanApplicationInfo = eblanApplicationInfos.find { currentEblanApplicationInfo ->
            currentEblanApplicationInfo.serialNumber == applicationInfoGridItem.serialNumber &&
                currentEblanApplicationInfo.componentName == applicationInfoGridItem.componentName
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

    applicationInfoGridItemRepository.updatePartialApplicationInfoGridItems(partialApplicationInfoGridItems)

    applicationInfoGridItemRepository.deleteApplicationInfoGridItems(deleteApplicationInfoGridItems)
}

/**
 * Updates non-overridden shortcut grid items and removes stale items when shortcut data is available.
 *
 * If [eblanShortcutInfos] is `null`, the repository is left unchanged.
 */
internal suspend fun updateShortcutInfoGridItems(
    eblanShortcutInfos: List<EblanShortcutInfo>?,
    shortcutInfoGridItemRepository: ShortcutInfoGridItemRepository,
    fileManager: FileManager,
    launcherAppsWrapper: LauncherAppsWrapper,
    packageManagerWrapper: PackageManagerWrapper,
    iconKeyGenerator: IconKeyGenerator,
) {
    val partialShortcutInfoGridItems = mutableListOf<PartialShortcutInfoGridItem>()

    val deleteShortcutInfoGridItems = mutableListOf<ShortcutInfoGridItem>()

    val shortcutInfoGridItems = shortcutInfoGridItemRepository.getShortcutInfoGridItems()

    if (eblanShortcutInfos != null) {
        shortcutInfoGridItems.filterNot { currentShortcutInfoGridItem ->
            currentShortcutInfoGridItem.override
        }.forEach { shortcutInfoGridItem ->
            currentCoroutineContext().ensureActive()

            val eblanShortcutInfo = eblanShortcutInfos.find { currentEblanShortcutInfo ->
                currentEblanShortcutInfo.serialNumber == shortcutInfoGridItem.serialNumber &&
                    currentEblanShortcutInfo.shortcutId == shortcutInfoGridItem.shortcutId
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
                            launcherAppsWrapper = launcherAppsWrapper,
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

        shortcutInfoGridItemRepository.updatePartialShortcutInfoGridItems(partialShortcutInfoGridItems)

        shortcutInfoGridItemRepository.deleteShortcutInfoGridItems(deleteShortcutInfoGridItems)
    }
}

/** Updates non-overridden shortcut-configuration grid items and removes stale entries. */
internal suspend fun updateShortcutConfigGridItems(
    eblanShortcutConfigs: List<EblanShortcutConfig>,
    shortcutConfigGridItemRepository: ShortcutConfigGridItemRepository,
    fileManager: FileManager,
    launcherAppsWrapper: LauncherAppsWrapper,
    packageManagerWrapper: PackageManagerWrapper,
    iconKeyGenerator: IconKeyGenerator,
) {
    val partialShortcutConfigGridItems = mutableListOf<PartialShortcutConfigGridItem>()

    val deleteShortcutConfigGridItems = mutableListOf<ShortcutConfigGridItem>()

    val shortcutConfigGridItems = shortcutConfigGridItemRepository.getShortcutConfigGridItems()

    shortcutConfigGridItems.filterNot { currentShortcutConfigGridItem ->
        currentShortcutConfigGridItem.override
    }.forEach { shortcutConfigGridItem ->
        currentCoroutineContext().ensureActive()

        val eblanShortcutConfig = eblanShortcutConfigs.find { currentEblanShortcutConfig ->
            currentEblanShortcutConfig.serialNumber == shortcutConfigGridItem.serialNumber &&
                currentEblanShortcutConfig.componentName == shortcutConfigGridItem.componentName
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
                        serialNumber = eblanShortcutConfig.serialNumber,
                    ),
                    applicationIcon = resolveApplicationIcon(
                        fileManager = fileManager,
                        launcherAppsWrapper = launcherAppsWrapper,
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

    shortcutConfigGridItemRepository.updatePartialShortcutConfigGridItems(partialShortcutConfigGridItems)

    shortcutConfigGridItemRepository.deleteShortcutConfigGridItems(deleteShortcutConfigGridItems)
}

/** Updates widget grid-item metadata or deletes stale items when widgets are supported. */
internal suspend fun updateWidgetGridItems(
    eblanAppWidgetProviderInfos: List<EblanAppWidgetProviderInfo>,
    fileManager: FileManager,
    launcherAppsWrapper: LauncherAppsWrapper,
    packageManagerWrapper: PackageManagerWrapper,
    widgetGridItemRepository: WidgetGridItemRepository,
    iconKeyGenerator: IconKeyGenerator,
) {
    if (!packageManagerWrapper.hasSystemFeatureAppWidgets) return

    val partialUpdateWidgetGridItems = mutableListOf<PartialUpdateWidgetGridItem>()

    val deleteWidgetGridItems = mutableListOf<WidgetGridItem>()

    val widgetGridItems = widgetGridItemRepository.getWidgetGridItems()

    widgetGridItems.forEach { widgetGridItem ->
        currentCoroutineContext().ensureActive()

        val eblanAppWidgetProviderInfo = eblanAppWidgetProviderInfos.find { currentEblanAppWidgetProviderInfo ->
            currentEblanAppWidgetProviderInfo.serialNumber == widgetGridItem.serialNumber &&
                currentEblanAppWidgetProviderInfo.componentName == widgetGridItem.componentName
        }

        if (eblanAppWidgetProviderInfo != null &&
            widgetGridItem.preview != eblanAppWidgetProviderInfo.preview
        ) {
            widgetGridItemRepository.updateWidgetGridItem(
                widgetGridItem.copy(
                    preview = eblanAppWidgetProviderInfo.preview,
                ),
            )
        }
    }

    widgetGridItems.filterNot { currentWidgetGridItem ->
        currentWidgetGridItem.override
    }.forEach { widgetGridItem ->
        currentCoroutineContext().ensureActive()

        val eblanAppWidgetProviderInfo =
            eblanAppWidgetProviderInfos.find { currentEblanAppWidgetProviderInfo ->
                currentEblanAppWidgetProviderInfo.serialNumber == widgetGridItem.serialNumber &&
                    currentEblanAppWidgetProviderInfo.componentName == widgetGridItem.componentName
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
                        launcherAppsWrapper = launcherAppsWrapper,
                        packageManagerWrapper = packageManagerWrapper,
                        iconKeyGenerator = iconKeyGenerator,
                        serialNumber = eblanAppWidgetProviderInfo.serialNumber,
                        packageName = eblanAppWidgetProviderInfo.packageName,
                    ),
                    label = packageManagerWrapper.getApplicationLabel(
                        packageName = eblanAppWidgetProviderInfo.packageName,
                        serialNumber = eblanAppWidgetProviderInfo.serialNumber,
                    ),
                ),
            )
        } else {
            deleteWidgetGridItems.add(widgetGridItem)
        }
    }

    widgetGridItemRepository.updatePartialWidgetGridItems(partialUpdateWidgetGridItems)

    widgetGridItemRepository.deleteWidgetGridItemsByPackageName(deleteWidgetGridItems)
}

/** Converts framework widget-provider metadata to the persisted domain model. */
internal suspend fun AppWidgetManagerAppWidgetProviderInfo.toEblanAppWidgetProviderInfo(
    fileManager: FileManager,
    launcherAppsWrapper: LauncherAppsWrapper,
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
        launcherAppsWrapper = launcherAppsWrapper,
        packageManagerWrapper = packageManagerWrapper,
        iconKeyGenerator = iconKeyGenerator,
        serialNumber = serialNumber,
        packageName = packageName,
    ),
    applicationLabel = packageManagerWrapper.getApplicationLabel(
        packageName = packageName,
        serialNumber = serialNumber,
    ),
    lastUpdateTime = lastUpdateTime,
    label = label,
    description = description,
)

/** Copies an application's stored metadata into the synchronization model. */
internal fun EblanApplicationInfo.toSyncEblanApplicationInfo() = SyncEblanApplicationInfo(
    serialNumber = serialNumber,
    componentName = componentName,
    packageName = packageName,
    icon = icon,
    label = label,
    lastUpdateTime = lastUpdateTime,
    flags = flags,
)

/** Copies launcher activity metadata into the synchronization model. */
internal fun LauncherAppsActivityInfo.toSyncEblanApplicationInfo() = SyncEblanApplicationInfo(
    serialNumber = serialNumber,
    componentName = componentName,
    packageName = packageName,
    icon = activityIcon,
    label = activityLabel,
    lastUpdateTime = lastUpdateTime,
    flags = flags,
)

/**
 * Returns receiver items whose identity key is absent from [other].
 *
 * @param other The items whose identity keys are excluded.
 * @param identity Extracts the comparison key from an item.
 * @return Items from the receiver whose keys are not present in [other].
 */
internal inline fun <T, K> Iterable<T>.differenceByIdentity(
    other: Iterable<T>,
    identity: (T) -> K,
): List<T> {
    val otherIdentities = other.mapTo(mutableSetOf(), identity)

    return filterNot { currentItem -> identity(currentItem) in otherIdentities }
}

/** Converts synchronization data into the smaller record used to delete an application. */
internal fun SyncEblanApplicationInfo.toDeleteEblanApplicationInfo() = DeleteEblanApplicationInfo(
    serialNumber = serialNumber,
    componentName = componentName,
    packageName = packageName,
    icon = icon,
)

/** Converts shortcut-configuration activity metadata to a repository model with cached app icon data. */
internal suspend fun ShortcutConfigActivityInfo.toEblanShortcutConfig(
    fileManager: FileManager,
    launcherAppsWrapper: LauncherAppsWrapper,
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
        launcherAppsWrapper = launcherAppsWrapper,
        packageManagerWrapper = packageManagerWrapper,
        iconKeyGenerator = iconKeyGenerator,
        serialNumber = serialNumber,
        packageName = packageName,
    ),
    applicationLabel = packageManagerWrapper.getApplicationLabel(
        packageName = packageName,
        serialNumber = serialNumber,
    ),
)

/** Converts a launcher shortcut into its repository model. */
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
    activityComponentName = activityComponentName,
    rank = rank,
)

/**
 * Adds an application to the home-screen grid if it is not already present at the top level or in
 * one of its folders.
 */
internal suspend fun addNewApplicationToHomeScreen(
    gridItems: MutableList<GridItem>,
    componentName: String,
    packageName: String,
    icon: String?,
    label: String,
    initialPage: Int,
    gridItemSettings: GridItemSettings,
    pageCount: Int,
    columns: Int,
    rows: Int,
    applicationInfoGridItems: MutableList<ApplicationInfoGridItem>,
    folderGridItemRepository: FolderGridItemRepository,
    maxFolderColumns: Int,
    maxFolderRows: Int,
) {
    val alreadyOnHome = gridItems.any { currentGridItem ->
        when (val data = currentGridItem.data) {
            is GridItemData.ApplicationInfo ->
                data.serialNumber == 0L &&
                    data.componentName == componentName

            is GridItemData.Folder -> {
                val previewFolderGridItems =
                    folderGridItemRepository.getFolderGridItemWrappers()
                        .asPreviewFolders(
                            maxFolderColumns = maxFolderColumns,
                            maxFolderRows = maxFolderRows,
                        )

                val folderGridItems = getRecursiveFolderGridItems(
                    folderPopupGridItems = previewFolderGridItems,
                    folderId = currentGridItem.id,
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
        page = initialPage,
        startColumn = 0,
        startRow = 0,
        columnSpan = 1,
        rowSpan = 1,
        data = data,
        associate = Associate.Grid,
        override = false,
        gridItemSettings = gridItemSettings,
        doubleTap = eblanAction,
        swipeUp = eblanAction,
        swipeDown = eblanAction,
    )

    val newGridItem = findAvailableRegionByPage(
        gridItems = gridItems,
        gridItem = gridItem,
        pageCount = pageCount,
        columns = columns,
        rows = rows,
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

/** Converts synchronization data into the model used when adding a new home-screen application. */
internal fun SyncEblanApplicationInfo.asAddNewEblanApplicationInfo(): AddNewEblanApplicationInfo = AddNewEblanApplicationInfo(
    serialNumber = serialNumber,
    componentName = componentName,
    packageName = packageName,
    icon = icon,
    label = label,
)

/** Resolves an application's cached icon path, falling back from its launch activity to its package. */
private suspend fun resolveApplicationIcon(
    fileManager: FileManager,
    launcherAppsWrapper: LauncherAppsWrapper,
    packageManagerWrapper: PackageManagerWrapper,
    iconKeyGenerator: IconKeyGenerator,
    serialNumber: Long,
    packageName: String,
): String? {
    val directory = fileManager.getFilesDirectory(FileManager.ICONS_DIR)

    val componentName =
        launcherAppsWrapper.getFastActivityList(
            packageName = packageName,
            serialNumber = serialNumber,
        ).firstOrNull()?.componentName

    /** Returns the cached icon path for the given activity or package component. */
    suspend fun getApplicationIcon(componentName: String): String? {
        val file = File(
            directory,
            iconKeyGenerator.getActivityIconKey(
                serialNumber = serialNumber,
                componentName = componentName,
            ),
        )

        return packageManagerWrapper.getApplicationIconCache(
            packageName = packageName,
            file = file,
            serialNumber = serialNumber,
        )
    }

    return if (componentName != null) {
        val iconFile = File(
            directory,
            iconKeyGenerator.getActivityIconKey(
                serialNumber = serialNumber,
                componentName = componentName,
            ),
        )

        if (iconFile.exists()) {
            iconFile.absolutePath
        } else {
            getApplicationIcon(packageName)
        }
    } else {
        getApplicationIcon(packageName)
    }
}
