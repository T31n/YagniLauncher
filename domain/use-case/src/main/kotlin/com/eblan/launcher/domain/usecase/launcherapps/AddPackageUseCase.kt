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
import com.eblan.launcher.domain.common.IconKeyGenerator
import com.eblan.launcher.domain.framework.AppWidgetManagerWrapper
import com.eblan.launcher.domain.framework.FileManager
import com.eblan.launcher.domain.framework.IconPackManager
import com.eblan.launcher.domain.framework.LauncherAppsWrapper
import com.eblan.launcher.domain.framework.PackageManagerWrapper
import com.eblan.launcher.domain.model.ApplicationInfoGridItem
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.model.HomeSettings
import com.eblan.launcher.domain.model.LauncherAppsActivityInfo
import com.eblan.launcher.domain.repository.ApplicationInfoGridItemRepository
import com.eblan.launcher.domain.repository.EblanAppWidgetProviderInfoRepository
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.EblanShortcutConfigRepository
import com.eblan.launcher.domain.repository.EblanShortcutInfoRepository
import com.eblan.launcher.domain.repository.FolderGridItemRepository
import com.eblan.launcher.domain.repository.GridRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import com.eblan.launcher.domain.usecase.grid.isTopLevel
import com.eblan.launcher.domain.usecase.util.cacheIconPackFile
import com.eblan.launcher.domain.usecase.util.toGridItems
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi

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
    @param:Dispatcher(EblanDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(
        serialNumber: Long,
        packageName: String,
    ) {
        withContext(ioDispatcher) {
            val userData = userDataRepository.userDataFlow.first()

            val newApplicationInfoGridItems = mutableListOf<ApplicationInfoGridItem>()

            val launcherAppsActivityInfosByPackageName = launcherAppsWrapper.getActivityListWithCacheIcons(
                serialNumber = serialNumber,
                packageName = packageName,
            ).onEach {
                addEblanApplicationInfo(
                    homeSettings = userData.homeSettings,
                    serialNumber = it.serialNumber,
                    componentName = it.componentName,
                    packageName = it.packageName,
                    activityIcon = it.activityIcon,
                    activityLabel = it.activityLabel,
                    lastUpdateTime = it.lastUpdateTime,
                    flags = it.flags,
                    applicationInfoGridItems = newApplicationInfoGridItems,
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

            applicationInfoGridItemRepository.insertApplicationInfoGridItems(
                applicationInfoGridItems = newApplicationInfoGridItems,
            )

            addIconPackInfos(
                iconPackInfoPackageName = userData.generalSettings.iconPackInfoPackageName,
                launcherAppsActivityInfos = launcherAppsActivityInfosByPackageName,
            )
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    private suspend fun addEblanApplicationInfo(
        homeSettings: HomeSettings,
        serialNumber: Long,
        componentName: String,
        packageName: String,
        activityIcon: String?,
        activityLabel: String,
        lastUpdateTime: Long,
        flags: Int,
        applicationInfoGridItems: MutableList<ApplicationInfoGridItem>,
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
                index = -1,
                flags = flags,
                folderIndex = -1,
                folderId = null,
            ),
        )

        if (!homeSettings.addNewAppsToHomeScreen) return

        val gridItems = gridRepository.getGridItems().toGridItems()
            .filter {
                it.isTopLevel() && it.associate == Associate.Grid
            }
            .toMutableList()

        addNewApplicationToHomeScreen(
            gridItems = gridItems,
            componentName = componentName,
            packageName = packageName,
            icon = activityIcon,
            label = activityLabel,
            homeSettings = homeSettings,
            applicationInfoGridItems = applicationInfoGridItems,
            folderGridItemRepository = folderGridItemRepository,
        )
    }

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

    private suspend fun addIconPackInfos(
        iconPackInfoPackageName: String,
        launcherAppsActivityInfos: List<LauncherAppsActivityInfo>,
    ) {
        if (iconPackInfoPackageName.isEmpty()) return

        val iconPackInfoDirectory = File(
            fileManager.getFilesDirectory(name = FileManager.ICON_PACKS_DIR),
            iconPackInfoPackageName,
        ).apply { if (!exists()) mkdirs() }

        val appFilter =
            iconPackManager.getIconPackInfoComponents(packageName = iconPackInfoPackageName)

        launcherAppsActivityInfos.forEach {
            currentCoroutineContext().ensureActive()

            val file = File(
                iconPackInfoDirectory,
                iconKeyGenerator.getHashedName(name = it.componentName),
            )

            cacheIconPackFile(
                iconPackManager = iconPackManager,
                appFilter = appFilter,
                iconPackInfoPackageName = iconPackInfoPackageName,
                file = file,
                componentName = it.componentName,
            )
        }
    }
}
