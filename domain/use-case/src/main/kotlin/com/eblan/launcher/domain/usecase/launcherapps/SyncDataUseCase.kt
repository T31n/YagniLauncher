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

import com.eblan.launcher.domain.common.Dispatcher
import com.eblan.launcher.domain.common.EblanDispatchers
import com.eblan.launcher.domain.model.application.EblanApplicationInfo
import com.eblan.launcher.domain.model.application.SyncEblanApplicationInfo
import com.eblan.launcher.domain.model.grid.ApplicationInfoGridItem
import com.eblan.launcher.domain.model.grid.Associate
import com.eblan.launcher.domain.model.grid.GridItemSettings
import com.eblan.launcher.domain.model.iconpackinfo.EblanIconPackInfo
import com.eblan.launcher.domain.model.shortcutconfig.EblanShortcutConfig
import com.eblan.launcher.domain.model.userdata.ColorSchemeSettings
import com.eblan.launcher.domain.model.userdata.ColorSource
import com.eblan.launcher.domain.model.userdata.EblanAction
import com.eblan.launcher.domain.model.userdata.EblanActionType
import com.eblan.launcher.domain.model.userdata.ExperimentalSettings
import com.eblan.launcher.domain.model.userdata.GeneralSettings
import com.eblan.launcher.domain.model.userdata.IconShape
import com.eblan.launcher.domain.model.userdata.Theme
import com.eblan.launcher.domain.platform.AppWidgetManagerWrapper
import com.eblan.launcher.domain.platform.FileManager
import com.eblan.launcher.domain.platform.IconKeyGenerator
import com.eblan.launcher.domain.platform.LauncherAppsWrapper
import com.eblan.launcher.domain.platform.PackageManagerWrapper
import com.eblan.launcher.domain.repository.ApplicationInfoGridItemRepository
import com.eblan.launcher.domain.repository.EblanAppWidgetProviderInfoRepository
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.EblanIconPackInfoRepository
import com.eblan.launcher.domain.repository.EblanShortcutConfigRepository
import com.eblan.launcher.domain.repository.EblanShortcutInfoRepository
import com.eblan.launcher.domain.repository.FolderGridItemRepository
import com.eblan.launcher.domain.repository.GridRepository
import com.eblan.launcher.domain.repository.ShortcutConfigGridItemRepository
import com.eblan.launcher.domain.repository.ShortcutInfoGridItemRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import com.eblan.launcher.domain.repository.WidgetGridItemRepository
import com.eblan.launcher.domain.usecase.cache.CleanUpCacheUseCase
import com.eblan.launcher.domain.usecase.folder.CleanupFolderEblanApplicationInfosUseCase
import com.eblan.launcher.domain.usecase.folder.CleanupFolderGridItemsUseCase
import com.eblan.launcher.domain.usecase.iconpack.UpdateIconPacksUseCase
import com.eblan.launcher.domain.usecase.util.isTopLevel
import com.eblan.launcher.domain.usecase.util.toGridItems
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import kotlin.uuid.Uuid

/** Reconciles persisted application, widget, shortcut, and icon-pack data with installed packages. */
class SyncDataUseCase @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val eblanApplicationInfoRepository: EblanApplicationInfoRepository,
    private val launcherAppsWrapper: LauncherAppsWrapper,
    private val fileManager: FileManager,
    private val eblanAppWidgetProviderInfoRepository: EblanAppWidgetProviderInfoRepository,
    private val appWidgetManagerWrapper: AppWidgetManagerWrapper,
    private val packageManagerWrapper: PackageManagerWrapper,
    private val eblanShortcutInfoRepository: EblanShortcutInfoRepository,
    private val applicationInfoGridItemRepository: ApplicationInfoGridItemRepository,
    private val widgetGridItemRepository: WidgetGridItemRepository,
    private val shortcutInfoGridItemRepository: ShortcutInfoGridItemRepository,
    private val eblanShortcutConfigRepository: EblanShortcutConfigRepository,
    private val shortcutConfigGridItemRepository: ShortcutConfigGridItemRepository,
    private val iconKeyGenerator: IconKeyGenerator,
    private val folderGridItemRepository: FolderGridItemRepository,
    private val gridRepository: GridRepository,
    private val updateIconPacksUseCase: UpdateIconPacksUseCase,
    private val eblanIconPackInfoRepository: EblanIconPackInfoRepository,
    private val cleanupFolderGridItemsUseCase: CleanupFolderGridItemsUseCase,
    private val cleanupFolderEblanApplicationInfosUseCase: CleanupFolderEblanApplicationInfosUseCase,
    private val cleanUpCacheUseCase: CleanUpCacheUseCase,
    @param:Dispatcher(EblanDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) {
    /** Refreshes launcher data and icon-pack caches using the current user settings. */
    suspend operator fun invoke() {
        withContext(ioDispatcher) {
            val userData = userDataRepository.userDataFlow.first()

            updateEblanApplicationInfos(
                experimentalSettings = userData.experimentalSettings,
                addNewAppsToHomeScreen = userData.homeSettings.addNewAppsToHomeScreen,
                initialPage = userData.homeSettings.initialPage,
                gridItemSettings = userData.homeSettings.gridItemSettings,
                pageCount = userData.homeSettings.pageCount,
                columns = userData.homeSettings.columns,
                rows = userData.homeSettings.rows,
                dockGridItemSettings = userData.homeSettings.dockGridItemSettings,
                dockColumns = userData.homeSettings.dockColumns,
                maxFolderColumns = userData.folderSettings.maxFolderColumns,
                maxFolderRows = userData.folderSettings.maxFolderRows,
                iconTint = userData.generalSettings.iconTint,
                colorSource = userData.generalSettings.colorSource,
                customSeedColor = userData.generalSettings.customSeedColor,
                iconShape = userData.generalSettings.iconShape,
                theme = userData.generalSettings.theme,
                colorSchemeSettings = userData.generalSettings.colorSchemeSettings,
            )

            updateAppWidgetProviderInfos()

            updateEblanLauncherShortcutInfos()

            updateEblanIconPackInfos(generalSettings = userData.generalSettings)

            cleanupFolderGridItemsUseCase()

            if (userData.experimentalSettings.cleanCache) {
                cleanUpCacheUseCase()
            }
        }
    }

    /** Reconciles stored icon-pack entries with installed packages and refreshes active caches. */
    private suspend fun updateEblanIconPackInfos(generalSettings: GeneralSettings) {
        val personalApplicationInfosByPackageName =
            eblanApplicationInfoRepository.getEblanApplicationInfos()
                .filter { currentEblanApplicationInfo ->
                    currentEblanApplicationInfo.serialNumber == 0L
                }
                .associateBy { currentEblanApplicationInfo ->
                    currentEblanApplicationInfo.packageName
                }

        eblanIconPackInfoRepository.getEblanIconPacks().forEach { currentEblanIconPackInfo ->
            currentCoroutineContext().ensureActive()

            val eblanApplicationInfo =
                personalApplicationInfosByPackageName[currentEblanIconPackInfo.packageName]

            if (eblanApplicationInfo == null) {
                eblanIconPackInfoRepository.deleteEblanIconPackInfo(
                    eblanIconPackInfo = currentEblanIconPackInfo,
                )
            } else if (currentEblanIconPackInfo.packageName != generalSettings.iconPackPackageName) {
                val iconFile = File(
                    fileManager.getFilesDirectory(name = FileManager.ICONS_DIR),
                    iconKeyGenerator.getActivityIconKey(
                        serialNumber = 0L,
                        componentName = eblanApplicationInfo.componentName,
                    ),
                )

                val icon = packageManagerWrapper.getApplicationIconCache(
                    packageName = eblanApplicationInfo.packageName,
                    file = iconFile,
                    serialNumber = 0L,
                )

                eblanIconPackInfoRepository.upsertEblanIconPackInfo(
                    eblanIconPackInfo = EblanIconPackInfo(
                        packageName = eblanApplicationInfo.packageName,
                        icon = icon,
                        label = eblanApplicationInfo.label,
                    ),
                )
            }
        }

        val activeIconPackPackageName = generalSettings.iconPackPackageName

        if (activeIconPackPackageName.isNotEmpty() &&
            activeIconPackPackageName !in personalApplicationInfosByPackageName
        ) {
            userDataRepository.updateGeneralSettings(
                generalSettings = generalSettings.copy(iconPackPackageName = ""),
            )
        } else {
            updateIconPacksUseCase(
                iconPackPackageName = activeIconPackPackageName,
            )
        }
    }

    /** Synchronizes installed applications and their related shortcut-configuration records. */
    private suspend fun updateEblanApplicationInfos(
        experimentalSettings: ExperimentalSettings,
        addNewAppsToHomeScreen: Boolean,
        initialPage: Int,
        gridItemSettings: GridItemSettings,
        pageCount: Int,
        columns: Int,
        rows: Int,
        dockGridItemSettings: GridItemSettings,
        dockColumns: Int,
        maxFolderColumns: Int,
        maxFolderRows: Int,
        iconTint: Boolean,
        colorSource: ColorSource,
        customSeedColor: Int,
        iconShape: IconShape,
        theme: Theme,
        colorSchemeSettings: ColorSchemeSettings,
    ) {
        val newEblanShortcutConfigs = mutableSetOf<EblanShortcutConfig>()

        val newApplicationInfoGridItems = mutableListOf<ApplicationInfoGridItem>()

        val oldSyncEblanApplicationInfos =
            eblanApplicationInfoRepository.getEblanApplicationInfos().map { currentEblanApplicationInfo ->
                currentEblanApplicationInfo.toSyncEblanApplicationInfo()
            }

        val newSyncEblanApplicationInfos = buildList {
            launcherAppsWrapper.getActivityListWithCacheIcons(
                iconTint = iconTint,
                colorSource = colorSource,
                customSeedColor = customSeedColor,
                iconShape = iconShape,
                theme = theme,
                colorSchemeSettings = colorSchemeSettings,
            )
                .forEach { launcherAppsActivityInfo ->
                    currentCoroutineContext().ensureActive()

                    newEblanShortcutConfigs.addAll(
                        launcherAppsWrapper.getShortcutConfigActivityListWithCacheIcons(
                            serialNumber = launcherAppsActivityInfo.serialNumber,
                            packageName = launcherAppsActivityInfo.packageName,
                        ).map { currentShortcutConfigActivityInfo ->
                            currentCoroutineContext().ensureActive()

                            currentShortcutConfigActivityInfo.toEblanShortcutConfig(
                                fileManager = fileManager,
                                launcherAppsWrapper = launcherAppsWrapper,
                                packageManagerWrapper = packageManagerWrapper,
                                iconKeyGenerator = iconKeyGenerator,
                            )
                        },
                    )

                    add(launcherAppsActivityInfo.toSyncEblanApplicationInfo())
                }
        }

        addNewApplicationsToHomeScreen(
            addNewAppsToHomeScreen = addNewAppsToHomeScreen,
            initialPage = initialPage,
            gridItemSettings = gridItemSettings,
            pageCount = pageCount,
            columns = columns,
            rows = rows,
            firstLaunch = experimentalSettings.firstLaunch,
            newSyncEblanApplicationInfos = newSyncEblanApplicationInfos,
            oldSyncEblanApplicationInfos = oldSyncEblanApplicationInfos,
            applicationInfoGridItems = newApplicationInfoGridItems,
            maxFolderColumns = maxFolderColumns,
            maxFolderRows = maxFolderRows,
        )

        val oldDeleteEblanApplicationInfos = oldSyncEblanApplicationInfos
            .differenceByIdentity(newSyncEblanApplicationInfos) { currentSyncEblanApplicationInfo ->
                currentSyncEblanApplicationInfo.serialNumber to currentSyncEblanApplicationInfo.componentName
            }
            .map { currentSyncEblanApplicationInfo ->
                currentSyncEblanApplicationInfo.toDeleteEblanApplicationInfo()
            }

        eblanApplicationInfoRepository.deleteSyncEblanApplicationInfos(
            deleteEblanApplicationInfos = oldDeleteEblanApplicationInfos,
        )

        eblanApplicationInfoRepository.upsertSyncEblanApplicationInfos(
            syncEblanApplicationInfos = newSyncEblanApplicationInfos,
        )

        cleanupFolderEblanApplicationInfosUseCase()

        updateEblanShortcutConfigs(newEblanShortcutConfigs = newEblanShortcutConfigs)

        updateApplicationInfoGridItems(
            eblanApplicationInfos = eblanApplicationInfoRepository.getEblanApplicationInfos(),
            applicationInfoGridItemRepository = applicationInfoGridItemRepository,
        )

        insertApplicationInfoGridItems(
            eblanApplicationInfos = eblanApplicationInfoRepository.getEblanApplicationInfos(),
            experimentalSettings = experimentalSettings,
            columns = columns,
            dockColumns = dockColumns,
            gridItemSettings = gridItemSettings,
            dockGridItemSettings = dockGridItemSettings,
        )

        applicationInfoGridItemRepository.insertApplicationInfoGridItems(applicationInfoGridItems = newApplicationInfoGridItems)
    }

    /** Adds newly installed non-system applications to the home screen when configured to do so. */
    private suspend fun addNewApplicationsToHomeScreen(
        addNewAppsToHomeScreen: Boolean,
        initialPage: Int,
        gridItemSettings: GridItemSettings,
        pageCount: Int,
        columns: Int,
        rows: Int,
        firstLaunch: Boolean,
        newSyncEblanApplicationInfos: List<SyncEblanApplicationInfo>,
        oldSyncEblanApplicationInfos: List<SyncEblanApplicationInfo>,
        applicationInfoGridItems: MutableList<ApplicationInfoGridItem>,
        maxFolderColumns: Int,
        maxFolderRows: Int,
    ) {
        if (!addNewAppsToHomeScreen ||
            firstLaunch ||
            oldSyncEblanApplicationInfos.isEmpty()
        ) {
            return
        }

        val gridItems = gridRepository.getGridItems().toGridItems()
            .filter { currentGridItem ->
                currentGridItem.isTopLevel() && currentGridItem.associate == Associate.Grid
            }
            .toMutableList()

        val oldNonSystemApplications =
            oldSyncEblanApplicationInfos.filterNot { currentSyncEblanApplicationInfo ->
                currentCoroutineContext().ensureActive()

                packageManagerWrapper.isSystem(flags = currentSyncEblanApplicationInfo.flags)
            }

        val newNonSystemApplications =
            newSyncEblanApplicationInfos.filterNot { currentSyncEblanApplicationInfo ->
                currentCoroutineContext().ensureActive()

                packageManagerWrapper.isSystem(flags = currentSyncEblanApplicationInfo.flags)
            }

        val newAddNewEblanApplicationInfos = newNonSystemApplications
            .differenceByIdentity(oldNonSystemApplications) { currentSyncEblanApplicationInfo ->
                currentSyncEblanApplicationInfo.serialNumber to currentSyncEblanApplicationInfo.componentName
            }
            .map { currentSyncEblanApplicationInfo ->
                currentSyncEblanApplicationInfo.asAddNewEblanApplicationInfo()
            }

        newAddNewEblanApplicationInfos.forEach { currentAddNewEblanApplicationInfo ->
            currentCoroutineContext().ensureActive()

            addNewApplicationToHomeScreen(
                gridItems = gridItems,
                componentName = currentAddNewEblanApplicationInfo.componentName,
                packageName = currentAddNewEblanApplicationInfo.packageName,
                icon = currentAddNewEblanApplicationInfo.icon,
                label = currentAddNewEblanApplicationInfo.label,
                initialPage = initialPage,
                gridItemSettings = gridItemSettings,
                pageCount = pageCount,
                columns = columns,
                rows = rows,
                applicationInfoGridItems = applicationInfoGridItems,
                folderGridItemRepository = folderGridItemRepository,
                maxFolderColumns = maxFolderColumns,
                maxFolderRows = maxFolderRows,
            )
        }
    }

    /** Synchronizes installed widget providers and updates their persisted grid-item metadata. */
    private suspend fun updateAppWidgetProviderInfos() {
        if (!packageManagerWrapper.hasSystemFeatureAppWidgets) return

        val appWidgetManagerAppWidgetProviderInfos =
            appWidgetManagerWrapper.getInstalledProvidersForProfilesWithCacheIcons()

        val oldEblanAppWidgetProviderInfos =
            eblanAppWidgetProviderInfoRepository.getEblanAppWidgetProviderInfos()

        val newEblanAppWidgetProviderInfos =
            appWidgetManagerAppWidgetProviderInfos.map { currentAppWidgetManagerAppWidgetProviderInfo ->
                currentAppWidgetManagerAppWidgetProviderInfo.toEblanAppWidgetProviderInfo(
                    fileManager = fileManager,
                    launcherAppsWrapper = launcherAppsWrapper,
                    packageManagerWrapper = packageManagerWrapper,
                    iconKeyGenerator = iconKeyGenerator,
                )
            }

        val deleteEblanAppWidgetProviderInfos =
            oldEblanAppWidgetProviderInfos
                .differenceByIdentity(newEblanAppWidgetProviderInfos) { currentEblanAppWidgetProviderInfo ->
                    currentEblanAppWidgetProviderInfo.serialNumber to currentEblanAppWidgetProviderInfo.componentName
                }

        eblanAppWidgetProviderInfoRepository.deleteEblanAppWidgetProviderInfos(
            eblanAppWidgetProviderInfos = deleteEblanAppWidgetProviderInfos,
        )

        eblanAppWidgetProviderInfoRepository.upsertEblanAppWidgetProviderInfos(
            eblanAppWidgetProviderInfos = newEblanAppWidgetProviderInfos,
        )

        updateWidgetGridItems(
            eblanAppWidgetProviderInfos = eblanAppWidgetProviderInfoRepository.getEblanAppWidgetProviderInfos(),
            fileManager = fileManager,
            launcherAppsWrapper = launcherAppsWrapper,
            packageManagerWrapper = packageManagerWrapper,
            widgetGridItemRepository = widgetGridItemRepository,
            iconKeyGenerator = iconKeyGenerator,
        )
    }

    /** Synchronizes launcher shortcuts when shortcut-host permission is available. */
    private suspend fun updateEblanLauncherShortcutInfos() {
        if (!launcherAppsWrapper.hasShortcutHostPermission) return

        val launcherAppsShortcutInfos =
            launcherAppsWrapper.getShortcutsWithCacheIcons(shortcutQuery = null) ?: return

        val oldEblanShortcutInfos = eblanShortcutInfoRepository.getEblanShortcutInfos()

        val newEblanShortcutInfos = launcherAppsShortcutInfos.map { currentLauncherAppsShortcutInfo ->
            currentLauncherAppsShortcutInfo.toEblanShortcutInfo()
        }

        val deleteEblanShortcutInfos = oldEblanShortcutInfos
            .differenceByIdentity(newEblanShortcutInfos) { currentEblanShortcutInfo ->
                Triple(
                    currentEblanShortcutInfo.serialNumber,
                    currentEblanShortcutInfo.shortcutId,
                    currentEblanShortcutInfo.packageName,
                )
            }

        eblanShortcutInfoRepository.deleteEblanShortcutInfos(
            eblanShortcutInfos = deleteEblanShortcutInfos,
        )

        eblanShortcutInfoRepository.upsertEblanShortcutInfos(
            eblanShortcutInfos = newEblanShortcutInfos,
        )

        updateShortcutInfoGridItems(
            eblanShortcutInfos = eblanShortcutInfoRepository.getEblanShortcutInfos(),
            shortcutInfoGridItemRepository = shortcutInfoGridItemRepository,
            fileManager = fileManager,
            launcherAppsWrapper = launcherAppsWrapper,
            packageManagerWrapper = packageManagerWrapper,
            iconKeyGenerator = iconKeyGenerator,
        )
    }

    /** Reconciles shortcut-configuration records and the grid items that reference them. */
    private suspend fun updateEblanShortcutConfigs(
        newEblanShortcutConfigs: Set<EblanShortcutConfig>,
    ) {
        val oldEblanShortcutConfigs = eblanShortcutConfigRepository.getEblanShortcutConfigs()

        if (oldEblanShortcutConfigs.toSet() == newEblanShortcutConfigs) return

        val deleteEblanShortcutConfigs = oldEblanShortcutConfigs
            .differenceByIdentity(newEblanShortcutConfigs) { currentEblanShortcutConfig ->
                currentEblanShortcutConfig.serialNumber to currentEblanShortcutConfig.componentName
            }

        eblanShortcutConfigRepository.deleteEblanShortcutConfigs(
            eblanShortcutConfigs = deleteEblanShortcutConfigs,
        )

        eblanShortcutConfigRepository.upsertEblanShortcutConfigs(
            eblanShortcutConfigs = newEblanShortcutConfigs.toList(),
        )

        updateShortcutConfigGridItems(
            eblanShortcutConfigs = eblanShortcutConfigRepository.getEblanShortcutConfigs(),
            shortcutConfigGridItemRepository = shortcutConfigGridItemRepository,
            fileManager = fileManager,
            launcherAppsWrapper = launcherAppsWrapper,
            packageManagerWrapper = packageManagerWrapper,
            iconKeyGenerator = iconKeyGenerator,
        )
    }

    /** Seeds the home grid and dock with system applications during the first launch only. */
    private suspend fun insertApplicationInfoGridItems(
        eblanApplicationInfos: List<EblanApplicationInfo>,
        experimentalSettings: ExperimentalSettings,
        columns: Int,
        dockColumns: Int,
        gridItemSettings: GridItemSettings,
        dockGridItemSettings: GridItemSettings,
    ) {
        if (!experimentalSettings.firstLaunch) return

        val gridItems = gridRepository.getGridItems().toGridItems()
            .filter { currentGridItem ->
                currentGridItem.isTopLevel() && currentGridItem.associate == Associate.Grid
            }

        if (gridItems.isNotEmpty()) {
            userDataRepository.updateExperimentalSettings(
                experimentalSettings.copy(firstLaunch = false),
            )

            return
        }

        val eblanApplicationInfosBySystem = eblanApplicationInfos.filter { currentEblanApplicationInfo ->
            currentCoroutineContext().ensureActive()

            packageManagerWrapper.isSystem(flags = currentEblanApplicationInfo.flags)
        }

        val applicationInfoGridItems = mutableListOf<ApplicationInfoGridItem>()

        /** Creates a default grid record for one application at the specified grid position. */
        fun insertApplicationInfoGridItem(
            index: Int,
            eblanApplicationInfo: EblanApplicationInfo,
            columns: Int,
            associate: Associate,
        ) {
            val startColumn = index % columns

            val startRow = index / columns

            val eblanAction = EblanAction(
                eblanActionType = EblanActionType.None,
                serialNumber = 0L,
                componentName = "",
            )

            applicationInfoGridItems.add(
                ApplicationInfoGridItem(
                    id = Uuid.random().toHexString(),
                    page = 0,
                    startColumn = startColumn,
                    startRow = startRow,
                    columnSpan = 1,
                    rowSpan = 1,
                    associate = associate,
                    componentName = eblanApplicationInfo.componentName,
                    packageName = eblanApplicationInfo.packageName,
                    icon = eblanApplicationInfo.icon,
                    label = eblanApplicationInfo.label,
                    override = false,
                    serialNumber = eblanApplicationInfo.serialNumber,
                    customIcon = null,
                    customLabel = null,
                    gridItemSettings = when (associate) {
                        Associate.Dock -> dockGridItemSettings
                        Associate.Grid -> gridItemSettings
                    },
                    doubleTap = eblanAction,
                    swipeUp = eblanAction,
                    swipeDown = eblanAction,
                    index = -1,
                    folderId = null,
                ),
            )
        }

        eblanApplicationInfosBySystem.take(columns)
            .forEachIndexed { index, launcherAppsActivityInfo ->
                insertApplicationInfoGridItem(
                    index = index,
                    eblanApplicationInfo = launcherAppsActivityInfo,
                    columns = columns,
                    associate = Associate.Grid,
                )
            }

        eblanApplicationInfosBySystem.drop(columns)
            .take(dockColumns)
            .forEachIndexed { index, launcherAppsActivityInfo ->
                insertApplicationInfoGridItem(
                    index = index,
                    eblanApplicationInfo = launcherAppsActivityInfo,
                    columns = dockColumns,
                    associate = Associate.Dock,
                )
            }

        applicationInfoGridItemRepository.insertApplicationInfoGridItems(applicationInfoGridItems = applicationInfoGridItems)

        userDataRepository.updateExperimentalSettings(
            experimentalSettings = experimentalSettings.copy(
                firstLaunch = false,
            ),
        )
    }
}
