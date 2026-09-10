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
import com.eblan.launcher.domain.common.FileManager
import com.eblan.launcher.domain.common.IconKeyGenerator
import com.eblan.launcher.domain.framework.AppWidgetManagerWrapper
import com.eblan.launcher.domain.framework.IconPackManager
import com.eblan.launcher.domain.framework.LauncherAppsWrapper
import com.eblan.launcher.domain.framework.PackageManagerWrapper
import com.eblan.launcher.domain.model.ApplicationInfoGridItem
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.EblanAction
import com.eblan.launcher.domain.model.EblanActionType
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.model.EblanShortcutConfig
import com.eblan.launcher.domain.model.ExperimentalSettings
import com.eblan.launcher.domain.model.HomeSettings
import com.eblan.launcher.domain.model.SyncEblanApplicationInfo
import com.eblan.launcher.domain.repository.ApplicationInfoGridItemRepository
import com.eblan.launcher.domain.repository.EblanAppWidgetProviderInfoRepository
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.EblanShortcutConfigRepository
import com.eblan.launcher.domain.repository.EblanShortcutInfoRepository
import com.eblan.launcher.domain.repository.FolderGridItemRepository
import com.eblan.launcher.domain.repository.GridRepository
import com.eblan.launcher.domain.repository.ShortcutConfigGridItemRepository
import com.eblan.launcher.domain.repository.ShortcutInfoGridItemRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import com.eblan.launcher.domain.repository.WidgetGridItemRepository
import com.eblan.launcher.domain.usecase.util.isTopLevel
import com.eblan.launcher.domain.usecase.util.toGridItems
import com.eblan.launcher.domain.usecase.util.updateIconPackInfos
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

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
    private val iconPackManager: IconPackManager,
    private val shortcutConfigGridItemRepository: ShortcutConfigGridItemRepository,
    private val iconKeyGenerator: IconKeyGenerator,
    private val folderGridItemRepository: FolderGridItemRepository,
    private val gridRepository: GridRepository,
    @param:Dispatcher(EblanDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke() {
        withContext(ioDispatcher) {
            val userData = userDataRepository.userDataFlow.first()

            launch {
                updateEblanApplicationInfos(
                    experimentalSettings = userData.experimentalSettings,
                    homeSettings = userData.homeSettings,
                )
            }

            launch {
                updateAppWidgetProviderInfos()
            }

            launch {
                updateEblanLauncherShortcutInfos()
            }

            launch {
                updateIconPackInfos(
                    iconPackInfoPackageName = userData.generalSettings.iconPackInfoPackageName,
                    fileManager = fileManager,
                    iconPackManager = iconPackManager,
                    fastLauncherAppsActivityInfos = launcherAppsWrapper.getFastActivityList(),
                    iconKeyGenerator = iconKeyGenerator,
                )
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private suspend fun updateEblanApplicationInfos(
        experimentalSettings: ExperimentalSettings,
        homeSettings: HomeSettings,
    ) {
        val newEblanShortcutConfigs = mutableSetOf<EblanShortcutConfig>()

        val newApplicationInfoGridItems = mutableListOf<ApplicationInfoGridItem>()

        val oldSyncEblanApplicationInfos =
            eblanApplicationInfoRepository.getEblanApplicationInfos().map {
                it.toSyncEblanApplicationInfo()
            }

        val newSyncEblanApplicationInfos = buildList {
            launcherAppsWrapper.getActivityListWithCacheIcons().forEach { launcherAppsActivityInfo ->
                currentCoroutineContext().ensureActive()

                newEblanShortcutConfigs.addAll(
                    launcherAppsWrapper.getShortcutConfigActivityListWithCacheIcons(
                        serialNumber = launcherAppsActivityInfo.serialNumber,
                        packageName = launcherAppsActivityInfo.packageName,
                    ).map {
                        currentCoroutineContext().ensureActive()

                        it.toEblanShortcutConfig(
                            fileManager = fileManager,
                            packageManagerWrapper = packageManagerWrapper,
                            iconKeyGenerator = iconKeyGenerator,
                        )
                    },
                )

                add(launcherAppsActivityInfo.toSyncEblanApplicationInfo())
            }
        }

        addNewApplicationsToHomeScreen(
            homeSettings = homeSettings,
            experimentalSettings = experimentalSettings,
            newSyncEblanApplicationInfos = newSyncEblanApplicationInfos,
            oldSyncEblanApplicationInfos = oldSyncEblanApplicationInfos,
            applicationInfoGridItems = newApplicationInfoGridItems,
        )

        val newDeleteEblanApplicationInfos =
            newSyncEblanApplicationInfos.map {
                it.toDeleteEblanApplicationInfo()
            }.toSet()

        val oldDeleteEblanApplicationInfos =
            oldSyncEblanApplicationInfos.map {
                it.toDeleteEblanApplicationInfo()
            }.filterNot {
                it in newDeleteEblanApplicationInfos
            }

        eblanApplicationInfoRepository.upsertSyncEblanApplicationInfos(
            syncEblanApplicationInfos = newSyncEblanApplicationInfos,
        )

        eblanApplicationInfoRepository.deleteSyncEblanApplicationInfos(
            deleteEblanApplicationInfos = oldDeleteEblanApplicationInfos,
        )

        deleteEblanApplicationInfoIcons(
            eblanApplicationInfos = eblanApplicationInfoRepository.getEblanApplicationInfos(),
            eblanAppWidgetProviderInfos = eblanAppWidgetProviderInfoRepository.getEblanAppWidgetProviderInfos(),
            oldDeleteEblanApplicationInfos = oldDeleteEblanApplicationInfos,
        )

        updateEblanShortcutConfigs(newEblanShortcutConfigs = newEblanShortcutConfigs)

        updateApplicationInfoGridItems(
            eblanApplicationInfos = eblanApplicationInfoRepository.getEblanApplicationInfos(),
            applicationInfoGridItemRepository = applicationInfoGridItemRepository,
        )

        insertApplicationInfoGridItems(
            eblanApplicationInfos = eblanApplicationInfoRepository.getEblanApplicationInfos(),
            experimentalSettings = experimentalSettings,
            homeSettings = homeSettings,
        )

        applicationInfoGridItemRepository.insertApplicationInfoGridItems(applicationInfoGridItems = newApplicationInfoGridItems)
    }

    @OptIn(ExperimentalUuidApi::class)
    private suspend fun addNewApplicationsToHomeScreen(
        homeSettings: HomeSettings,
        experimentalSettings: ExperimentalSettings,
        newSyncEblanApplicationInfos: List<SyncEblanApplicationInfo>,
        oldSyncEblanApplicationInfos: List<SyncEblanApplicationInfo>,
        applicationInfoGridItems: MutableList<ApplicationInfoGridItem>,
    ) {
        if (!homeSettings.addNewAppsToHomeScreen || experimentalSettings.firstLaunch) return

        val gridItems = gridRepository.getGridItems().toGridItems()
            .filter {
                it.isTopLevel() && it.associate == Associate.Grid
            }
            .toMutableList()

        val oldAddNewEblanApplicationInfos =
            oldSyncEblanApplicationInfos.filterNot {
                currentCoroutineContext().ensureActive()

                packageManagerWrapper.isSystem(flags = it.flags)
            }.map {
                it.asAddNewEblanApplicationInfo()
            }

        val newAddNewEblanApplicationInfos =
            newSyncEblanApplicationInfos.filterNot {
                currentCoroutineContext().ensureActive()

                packageManagerWrapper.isSystem(flags = it.flags)
            }.map {
                it.asAddNewEblanApplicationInfo()
            }

        val addNewEblanApplicationInfos =
            newAddNewEblanApplicationInfos - oldAddNewEblanApplicationInfos.toSet()

        addNewEblanApplicationInfos.forEach {
            currentCoroutineContext().ensureActive()

            addNewApplicationToHomeScreen(
                gridItems = gridItems,
                componentName = it.componentName,
                packageName = it.packageName,
                icon = it.icon,
                label = it.label,
                homeSettings = homeSettings,
                applicationInfoGridItems = applicationInfoGridItems,
                folderGridItemRepository = folderGridItemRepository,
            )
        }
    }

    private suspend fun updateAppWidgetProviderInfos() {
        if (!packageManagerWrapper.hasSystemFeatureAppWidgets) return

        val appWidgetManagerAppWidgetProviderInfos = appWidgetManagerWrapper.getInstalledProvidersWithCacheIcons()

        val oldEblanAppWidgetProviderInfos =
            eblanAppWidgetProviderInfoRepository.getEblanAppWidgetProviderInfos()

        val newEblanAppWidgetProviderInfos =
            appWidgetManagerAppWidgetProviderInfos.map {
                it.toEblanAppWidgetProviderInfo(
                    fileManager = fileManager,
                    packageManagerWrapper = packageManagerWrapper,
                    iconKeyGenerator = iconKeyGenerator,
                )
            }

        val newDeleteEblanAppWidgetProviderInfos =
            newEblanAppWidgetProviderInfos.map {
                it.toDeleteEblanAppWidgetProviderInfo()
            }.toSet()

        val oldDeleteEblanAppWidgetProviderInfos =
            oldEblanAppWidgetProviderInfos.map {
                it.toDeleteEblanAppWidgetProviderInfo()
            }.filterNot {
                it in newDeleteEblanAppWidgetProviderInfos
            }

        eblanAppWidgetProviderInfoRepository.upsertEblanAppWidgetProviderInfos(
            eblanAppWidgetProviderInfos = newEblanAppWidgetProviderInfos,
        )

        eblanAppWidgetProviderInfoRepository.deleteEblanAppWidgetProviderInfos(
            deleteEblanAppWidgetProviderInfos = oldDeleteEblanAppWidgetProviderInfos,
        )

        deleteEblanAppWidgetProviderInfoIcons(
            eblanApplicationInfos = eblanApplicationInfoRepository.getEblanApplicationInfos(),
            eblanAppWidgetProviderInfos = eblanAppWidgetProviderInfoRepository.getEblanAppWidgetProviderInfos(),
            oldDeleteEblanAppWidgetProviderInfos = oldDeleteEblanAppWidgetProviderInfos,
        )

        updateWidgetGridItems(
            eblanAppWidgetProviderInfos = eblanAppWidgetProviderInfoRepository.getEblanAppWidgetProviderInfos(),
            fileManager = fileManager,
            packageManagerWrapper = packageManagerWrapper,
            widgetGridItemRepository = widgetGridItemRepository,
            iconKeyGenerator = iconKeyGenerator,
        )
    }

    private suspend fun updateEblanLauncherShortcutInfos() {
        if (!launcherAppsWrapper.hasShortcutHostPermission) return

        val launcherAppsShortcutInfos =
            launcherAppsWrapper.getShortcutsWithCacheIcons(shortcutQuery = null) ?: return

        val oldEblanShortcutInfos = eblanShortcutInfoRepository.getEblanShortcutInfos()

        val newEblanShortcutInfos = launcherAppsShortcutInfos.map {
            it.toEblanShortcutInfo()
        }

        val newDeleteEblanShortcutInfos = newEblanShortcutInfos.map {
            it.toDeleteEblanShortcutInfo()
        }.toSet()

        val oldDeleteEblanShortcutInfos = oldEblanShortcutInfos.map {
            it.toDeleteEblanShortcutInfo()
        }.filterNot {
            it in newDeleteEblanShortcutInfos
        }

        eblanShortcutInfoRepository.upsertEblanShortcutInfos(
            eblanShortcutInfos = newEblanShortcutInfos,
        )

        eblanShortcutInfoRepository.deleteEblanShortcutInfos(
            deleteEblanShortcutInfos = oldDeleteEblanShortcutInfos,
        )

        deleteEblanShortInfoIcons(
            eblanShortcutInfos = eblanShortcutInfoRepository.getEblanShortcutInfos(),
            oldDeleteEblanShortcutInfos = oldDeleteEblanShortcutInfos,
        )

        updateShortcutInfoGridItems(
            eblanShortcutInfos = eblanShortcutInfoRepository.getEblanShortcutInfos(),
            shortcutInfoGridItemRepository = shortcutInfoGridItemRepository,
            fileManager = fileManager,
            packageManagerWrapper = packageManagerWrapper,
            iconKeyGenerator = iconKeyGenerator,
        )
    }

    private suspend fun updateEblanShortcutConfigs(
        newEblanShortcutConfigs: Set<EblanShortcutConfig>,
    ) {
        val oldEblanShortcutConfigs = eblanShortcutConfigRepository.getEblanShortcutConfigs()

        if (oldEblanShortcutConfigs.toSet() == newEblanShortcutConfigs) return

        val newDeleteEblanShortcutConfigs = newEblanShortcutConfigs.map {
            it.toDeleteEblanShortcutConfig()
        }.toSet()

        val oldDeleteEblanShortcutConfigs = oldEblanShortcutConfigs.map {
            it.toDeleteEblanShortcutConfig()
        }.filterNot {
            it in newDeleteEblanShortcutConfigs
        }

        eblanShortcutConfigRepository.upsertEblanShortcutConfigs(
            eblanShortcutConfigs = newEblanShortcutConfigs.toList(),
        )

        eblanShortcutConfigRepository.deleteEblanShortcutConfigs(
            deleteEblanShortcutConfigs = oldDeleteEblanShortcutConfigs,
        )

        deleteEblanShortcutConfigIcons(oldDeleteEblanShortcutConfigs = oldDeleteEblanShortcutConfigs)

        updateShortcutConfigGridItems(
            eblanShortcutConfigs = eblanShortcutConfigRepository.getEblanShortcutConfigs(),
            shortcutConfigGridItemRepository = shortcutConfigGridItemRepository,
            fileManager = fileManager,
            packageManagerWrapper = packageManagerWrapper,
            iconKeyGenerator = iconKeyGenerator,
        )
    }

    @OptIn(ExperimentalUuidApi::class)
    private suspend fun insertApplicationInfoGridItems(
        eblanApplicationInfos: List<EblanApplicationInfo>,
        experimentalSettings: ExperimentalSettings,
        homeSettings: HomeSettings,
    ) {
        if (!experimentalSettings.firstLaunch) return

        val eblanApplicationInfosBySystem = eblanApplicationInfos.filter {
            currentCoroutineContext().ensureActive()

            packageManagerWrapper.isSystem(flags = it.flags)
        }

        val applicationInfoGridItems = mutableListOf<ApplicationInfoGridItem>()

        @OptIn(ExperimentalUuidApi::class)
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
                    gridItemSettings = homeSettings.gridItemSettings,
                    doubleTap = eblanAction,
                    swipeUp = eblanAction,
                    swipeDown = eblanAction,
                    index = -1,
                    folderId = null,
                ),
            )
        }

        eblanApplicationInfosBySystem.take(homeSettings.columns)
            .forEachIndexed { index, launcherAppsActivityInfo ->
                insertApplicationInfoGridItem(
                    index = index,
                    eblanApplicationInfo = launcherAppsActivityInfo,
                    columns = homeSettings.columns,
                    associate = Associate.Grid,
                )
            }

        eblanApplicationInfosBySystem.drop(homeSettings.columns)
            .take(homeSettings.dockColumns)
            .forEachIndexed { index, launcherAppsActivityInfo ->
                insertApplicationInfoGridItem(
                    index = index,
                    eblanApplicationInfo = launcherAppsActivityInfo,
                    columns = homeSettings.dockColumns,
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
