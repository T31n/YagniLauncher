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
import com.eblan.launcher.domain.common.GridLock
import com.eblan.launcher.domain.common.IconKeyGenerator
import com.eblan.launcher.domain.framework.AppWidgetManagerWrapper
import com.eblan.launcher.domain.framework.IconPackManager
import com.eblan.launcher.domain.framework.LauncherAppsWrapper
import com.eblan.launcher.domain.framework.PackageManagerWrapper
import com.eblan.launcher.domain.model.application.EblanApplicationInfo
import com.eblan.launcher.domain.model.grid.ApplicationInfoGridItem
import com.eblan.launcher.domain.model.grid.Associate
import com.eblan.launcher.domain.model.launcherapps.LauncherAppsActivityInfo
import com.eblan.launcher.domain.model.userdata.GeneralSettings
import com.eblan.launcher.domain.repository.ApplicationInfoGridItemRepository
import com.eblan.launcher.domain.repository.EblanAppWidgetProviderInfoRepository
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.EblanShortcutConfigRepository
import com.eblan.launcher.domain.repository.EblanShortcutInfoRepository
import com.eblan.launcher.domain.repository.FolderGridItemRepository
import com.eblan.launcher.domain.repository.GridRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import com.eblan.launcher.domain.usecase.util.cacheIconPackFile
import com.eblan.launcher.domain.usecase.util.isTopLevel
import com.eblan.launcher.domain.usecase.util.toGridItems
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

/** Imports a newly installed package's activities, widgets, shortcuts, and launchable entries. */
class AddPackageUseCase @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val packageManagerWrapper: PackageManagerWrapper,
    private val eblanApplicationInfoRepository: EblanApplicationInfoRepository,
    private val appWidgetManagerWrapper: AppWidgetManagerWrapper,
    private val eblanAppWidgetProviderInfoRepository: EblanAppWidgetProviderInfoRepository,
    private val eblanShortcutInfoRepository: EblanShortcutInfoRepository,
    private val launcherAppsWrapper: LauncherAppsWrapper,
    private val eblanShortcutConfigRepository: EblanShortcutConfigRepository,
    private val fileManager: FileManager,
    private val iconPackManager: IconPackManager,
    private val iconKeyGenerator: IconKeyGenerator,
    private val applicationInfoGridItemRepository: ApplicationInfoGridItemRepository,
    private val folderGridItemRepository: FolderGridItemRepository,
    private val gridRepository: GridRepository,
    private val gridLock: GridLock,
    @param:Dispatcher(EblanDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) {
    /**
     * Loads package components, stores their metadata, and optionally adds activities to the home screen.
     *
     * @param serialNumber The user profile where the package is installed.
     * @param packageName The package to import.
     */
    suspend operator fun invoke(
        serialNumber: Long,
        packageName: String,
    ) {
        withContext(ioDispatcher) {
            val userData = userDataRepository.userDataFlow.first()

            val launcherAppsActivityInfosByPackageName = launcherAppsWrapper.getActivityListWithCacheIcons(
                serialNumber = serialNumber,
                packageName = packageName,
                iconTint = userData.generalSettings.iconTint,
                iconShape = userData.generalSettings.iconShape,
                customIconTint = userData.generalSettings.customIconTint,
                fallbackIconTint = userData.generalSettings.fallbackIconTint,
                theme = userData.generalSettings.theme,
            ).onEach {
                upsertEblanApplicationInfo(
                    serialNumber = it.serialNumber,
                    componentName = it.componentName,
                    packageName = it.packageName,
                    activityIcon = it.activityIcon,
                    activityLabel = it.activityLabel,
                    lastUpdateTime = it.lastUpdateTime,
                    flags = it.flags,
                )
            }

            addEblanAppWidgetProviderInfos(
                serialNumber = serialNumber,
                packageName = packageName,
            )

            addEblanShortcutInfos(
                serialNumber = serialNumber,
                packageName = packageName,
            )

            addEblanShortcutConfigs(
                serialNumber = serialNumber,
                packageName = packageName,
            )

            gridLock.withLock {
                val newApplicationInfoGridItems = mutableListOf<ApplicationInfoGridItem>()

                if (userData.homeSettings.addNewAppsToHomeScreen) {
                    val gridItems = gridRepository.getGridItems().toGridItems()
                        .filter {
                            it.isTopLevel() && it.associate == Associate.Grid
                        }
                        .toMutableList()

                    launcherAppsActivityInfosByPackageName.forEach {
                        currentCoroutineContext().ensureActive()

                        addNewApplicationToHomeScreen(
                            gridItems = gridItems,
                            componentName = it.componentName,
                            packageName = it.packageName,
                            icon = it.activityIcon,
                            label = it.activityLabel,
                            homeSettings = userData.homeSettings,
                            applicationInfoGridItems = newApplicationInfoGridItems,
                            folderGridItemRepository = folderGridItemRepository,
                            folderSettings = userData.folderSettings,
                        )
                    }
                }

                applicationInfoGridItemRepository.insertApplicationInfoGridItems(
                    applicationInfoGridItems = newApplicationInfoGridItems,
                )
            }

            addIconPackInfos(
                iconPackPackageName = userData.generalSettings.iconPackPackageName,
                launcherAppsActivityInfos = launcherAppsActivityInfosByPackageName,
                generalSettings = userData.generalSettings,
            )
        }
    }

    /** Stores an activity's application record without touching the grid. */
    private suspend fun upsertEblanApplicationInfo(
        serialNumber: Long,
        componentName: String,
        packageName: String,
        activityIcon: String?,
        activityLabel: String,
        lastUpdateTime: Long,
        flags: Int,
    ) {
        eblanApplicationInfoRepository.upsertEblanApplicationInfo(
            eblanApplicationInfo = EblanApplicationInfo(
                componentName = componentName,
                serialNumber = serialNumber,
                packageName = packageName,
                icon = activityIcon,
                label = activityLabel,
                customIcon = null,
                customLabel = null,
                isHidden = false,
                lastUpdateTime = lastUpdateTime,
                flags = flags,
                folderIndex = -1,
                folderId = null,
            ),
        )
    }

    /** Imports widget-provider metadata and caches the provider's associated icons. */
    private suspend fun addEblanAppWidgetProviderInfos(
        serialNumber: Long,
        packageName: String,
    ) {
        val eblanAppWidgetProviderInfos = appWidgetManagerWrapper.getInstalledProvidersWithCacheIcons()
            .filter {
                it.serialNumber == serialNumber &&
                    it.packageName == packageName
            }.map {
                currentCoroutineContext().ensureActive()

                it.toEblanAppWidgetProviderInfo(
                    fileManager = fileManager,
                    packageManagerWrapper = packageManagerWrapper,
                    iconKeyGenerator = iconKeyGenerator,
                )
            }

        eblanAppWidgetProviderInfoRepository.upsertEblanAppWidgetProviderInfos(
            eblanAppWidgetProviderInfos = eblanAppWidgetProviderInfos,
        )
    }

    /** Imports launcher shortcuts for the specified package when shortcut data is available. */
    private suspend fun addEblanShortcutInfos(
        serialNumber: Long,
        packageName: String,
    ) {
        val eblanShortcutInfos =
            launcherAppsWrapper.getShortcutsByPackageNameWithCacheIcons(
                serialNumber = serialNumber,
                packageName = packageName,
            )?.map {
                it.toEblanShortcutInfo()
            }

        if (eblanShortcutInfos != null) {
            eblanShortcutInfoRepository.upsertEblanShortcutInfos(
                eblanShortcutInfos = eblanShortcutInfos,
            )
        }
    }

    /** Imports shortcut-configuration activities and their cached icon metadata. */
    private suspend fun addEblanShortcutConfigs(
        serialNumber: Long,
        packageName: String,
    ) {
        val eblanShortcutConfigs = launcherAppsWrapper.getShortcutConfigActivityListWithCacheIcons(
            serialNumber = serialNumber,
            packageName = packageName,
        ).map {
            currentCoroutineContext().ensureActive()

            it.toEblanShortcutConfig(
                fileManager = fileManager,
                packageManagerWrapper = packageManagerWrapper,
                iconKeyGenerator = iconKeyGenerator,
            )
        }

        eblanShortcutConfigRepository.upsertEblanShortcutConfigs(
            eblanShortcutConfigs = eblanShortcutConfigs,
        )
    }

    /** Caches icon-pack images for the activities discovered in the newly added package. */
    private suspend fun addIconPackInfos(
        iconPackPackageName: String,
        launcherAppsActivityInfos: List<LauncherAppsActivityInfo>,
        generalSettings: GeneralSettings,
    ) {
        if (iconPackPackageName.isEmpty()) return

        val iconPackInfoDirectory = File(
            fileManager.getFilesDirectory(name = FileManager.ICON_PACKS_DIR),
            iconPackPackageName,
        ).apply { if (!exists()) mkdirs() }

        val appFilter =
            iconPackManager.getIconPackComponents(packageName = iconPackPackageName)

        launcherAppsActivityInfos.forEach {
            currentCoroutineContext().ensureActive()

            val file = File(
                iconPackInfoDirectory,
                iconKeyGenerator.getHashedName(name = it.componentName),
            )

            cacheIconPackFile(
                iconPackManager = iconPackManager,
                appFilter = appFilter,
                iconPackPackageName = iconPackPackageName,
                file = file,
                componentName = it.componentName,
                generalSettings = generalSettings,
            )
        }
    }
}
