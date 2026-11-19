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
import com.eblan.launcher.domain.model.application.DeleteEblanApplicationInfo
import com.eblan.launcher.domain.model.application.EblanApplicationInfo
import com.eblan.launcher.domain.model.grid.ApplicationInfoGridItem
import com.eblan.launcher.domain.model.grid.ShortcutConfigGridItem
import com.eblan.launcher.domain.model.grid.ShortcutInfoGridItem
import com.eblan.launcher.domain.model.shortcutconfig.EblanShortcutConfig
import com.eblan.launcher.domain.model.widget.EblanAppWidgetProviderInfo
import com.eblan.launcher.domain.platform.FileManager
import com.eblan.launcher.domain.platform.IconKeyGenerator
import com.eblan.launcher.domain.repository.ApplicationInfoGridItemRepository
import com.eblan.launcher.domain.repository.EblanAppWidgetProviderInfoRepository
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.EblanShortcutConfigRepository
import com.eblan.launcher.domain.repository.EblanShortcutInfoRepository
import com.eblan.launcher.domain.repository.FolderEblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.GridRepository
import com.eblan.launcher.domain.repository.ShortcutConfigGridItemRepository
import com.eblan.launcher.domain.repository.ShortcutInfoGridItemRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import com.eblan.launcher.domain.repository.WidgetGridItemRepository
import com.eblan.launcher.domain.usecase.util.deleteCustomIconsIfUnreferenced
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

/** Removes a package's stored components, grid items, and unreferenced icon files. */
class RemovePackageUseCase @Inject constructor(
    private val fileManager: FileManager,
    private val eblanApplicationInfoRepository: EblanApplicationInfoRepository,
    private val userDataRepository: UserDataRepository,
    private val eblanAppWidgetProviderInfoRepository: EblanAppWidgetProviderInfoRepository,
    private val applicationInfoGridItemRepository: ApplicationInfoGridItemRepository,
    private val widgetGridItemRepository: WidgetGridItemRepository,
    private val shortcutInfoGridItemRepository: ShortcutInfoGridItemRepository,
    private val eblanShortcutInfoRepository: EblanShortcutInfoRepository,
    private val eblanShortcutConfigRepository: EblanShortcutConfigRepository,
    private val shortcutConfigGridItemRepository: ShortcutConfigGridItemRepository,
    private val folderEblanApplicationInfoRepository: FolderEblanApplicationInfoRepository,
    private val gridRepository: GridRepository,
    private val iconKeyGenerator: IconKeyGenerator,
    @param:Dispatcher(EblanDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) {
    /**
     * Deletes package-related icon files and repository records for the specified profile.
     *
     * @param serialNumber The user profile where the package was installed.
     * @param packageName The package to remove.
     */
    suspend operator fun invoke(
        serialNumber: Long,
        packageName: String,
    ) {
        withContext(ioDispatcher) {
            val deleteEblanApplicationInfos =
                eblanApplicationInfoRepository.getEblanApplicationInfosByPackageName(
                    serialNumber = serialNumber,
                    packageName = packageName,
                )

            val deleteEblanAppWidgetProviderInfos =
                eblanAppWidgetProviderInfoRepository.getEblanAppWidgetProviderInfos()
                    .filter {
                        it.serialNumber == serialNumber && it.packageName == packageName
                    }

            val deleteEblanShortcutInfos =
                eblanShortcutInfoRepository.getEblanShortcutInfosByPackageName(
                    serialNumber = serialNumber,
                    packageName = packageName,
                )

            val deleteEblanShortcutConfigs =
                eblanShortcutConfigRepository.getEblanShortcutConfigsByPackageName(
                    serialNumber = serialNumber,
                    packageName = packageName,
                )

            val deleteApplicationInfoGridItems =
                applicationInfoGridItemRepository.getApplicationInfoGridItemsByPackageName(
                    serialNumber = serialNumber,
                    packageName = packageName,
                )

            val deleteShortcutInfoGridItems =
                shortcutInfoGridItemRepository.getShortcutInfoGridItemsByPackageName(
                    serialNumber = serialNumber,
                    packageName = packageName,
                )

            val deleteShortcutConfigGridItems =
                shortcutConfigGridItemRepository.getShortcutConfigGridItemsByPackageName(
                    serialNumber = serialNumber,
                    packageName = packageName,
                )

            eblanApplicationInfoRepository.deleteEblanApplicationInfoByPackageName(
                serialNumber = serialNumber,
                packageName = packageName,
            )

            eblanAppWidgetProviderInfoRepository.deleteEblanAppWidgetProviderInfos(
                eblanAppWidgetProviderInfos = deleteEblanAppWidgetProviderInfos,
            )

            eblanShortcutInfoRepository.deleteEblanShortcutInfos(
                serialNumber = serialNumber,
                packageName = packageName,
            )

            eblanShortcutConfigRepository.deleteEblanShortcutConfigByPackageName(
                serialNumber = serialNumber,
                packageName = packageName,
            )

            applicationInfoGridItemRepository.deleteApplicationInfoGridItem(
                serialNumber = serialNumber,
                packageName = packageName,
            )

            widgetGridItemRepository.deleteWidgetGridItemByPackageName(
                serialNumber = serialNumber,
                packageName = packageName,
            )

            shortcutInfoGridItemRepository.deleteShortcutInfoGridItemByPackageName(
                serialNumber = serialNumber,
                packageName = packageName,
            )

            shortcutConfigGridItemRepository.deleteShortcutConfigGridItemByPackageName(
                serialNumber = serialNumber,
                packageName = packageName,
            )

            val remainingEblanApplicationInfos =
                eblanApplicationInfoRepository.getEblanApplicationInfos()
            val remainingEblanAppWidgetProviderInfos =
                eblanAppWidgetProviderInfoRepository.getEblanAppWidgetProviderInfos()
            val remainingEblanShortcutInfos =
                eblanShortcutInfoRepository.getEblanShortcutInfos()
            val remainingEblanShortcutConfigs =
                eblanShortcutConfigRepository.getEblanShortcutConfigs()
            val remainingApplicationInfoGridItems =
                applicationInfoGridItemRepository.getApplicationInfoGridItems()
            val remainingWidgetGridItems =
                widgetGridItemRepository.getWidgetGridItems()
            val remainingShortcutConfigGridItems =
                shortcutConfigGridItemRepository.getShortcutConfigGridItems()
            val remainingShortcutInfoGridItems =
                shortcutInfoGridItemRepository.getShortcutInfoGridItems()

            deleteEblanApplicationInfoFiles(
                deleteEblanApplicationInfos = deleteEblanApplicationInfos,
                remainingEblanApplicationInfos = remainingEblanApplicationInfos,
                remainingEblanAppWidgetProviderInfos = remainingEblanAppWidgetProviderInfos,
                remainingEblanShortcutConfigs = remainingEblanShortcutConfigs,
                remainingApplicationInfoGridItems = remainingApplicationInfoGridItems,
                remainingWidgetGridItems = remainingWidgetGridItems,
                remainingShortcutConfigGridItems = remainingShortcutConfigGridItems,
                remainingShortcutInfoGridItems = remainingShortcutInfoGridItems,
            )

            deleteEblanAppWidgetProviderInfoIcons(
                eblanApplicationInfos = remainingEblanApplicationInfos,
                eblanAppWidgetProviderInfos = remainingEblanAppWidgetProviderInfos,
                eblanShortcutConfigs = remainingEblanShortcutConfigs,
                applicationInfoGridItems = remainingApplicationInfoGridItems,
                widgetGridItems = remainingWidgetGridItems,
                shortcutConfigGridItems = remainingShortcutConfigGridItems,
                shortcutInfoGridItems = remainingShortcutInfoGridItems,
                deleteEblanAppWidgetProviderInfos = deleteEblanAppWidgetProviderInfos,
            )

            deleteEblanShortInfoIcons(
                eblanShortcutInfos = remainingEblanShortcutInfos,
                deleteEblanShortcutInfos = deleteEblanShortcutInfos,
            )

            deleteEblanShortcutConfigIcons(
                eblanApplicationInfos = remainingEblanApplicationInfos,
                eblanAppWidgetProviderInfos = remainingEblanAppWidgetProviderInfos,
                eblanShortcutConfigs = remainingEblanShortcutConfigs,
                applicationInfoGridItems = remainingApplicationInfoGridItems,
                widgetGridItems = remainingWidgetGridItems,
                shortcutConfigGridItems = remainingShortcutConfigGridItems,
                shortcutInfoGridItems = remainingShortcutInfoGridItems,
                deleteEblanShortcutConfigs = deleteEblanShortcutConfigs,
            )

            val customIconCandidates = buildList {
                deleteEblanApplicationInfos.mapNotNullTo(this) { it.customIcon }
                deleteApplicationInfoGridItems.mapNotNullTo(this) { it.customIcon }
                deleteShortcutInfoGridItems.mapNotNullTo(this) { it.customIcon }
                deleteShortcutConfigGridItems.mapNotNullTo(this) { it.customIcon }
                deleteShortcutConfigGridItems.mapNotNullTo(this) { it.shortcutIntentIcon }
            }

            deleteCustomIconsIfUnreferenced(
                iconPaths = customIconCandidates,
                eblanApplicationInfoRepository = eblanApplicationInfoRepository,
                folderEblanApplicationInfoRepository = folderEblanApplicationInfoRepository,
                gridRepository = gridRepository,
            )
        }
    }

    /** Deletes unreferenced application icons and icon-pack files for removed activities. */
    private suspend fun deleteEblanApplicationInfoFiles(
        deleteEblanApplicationInfos: List<EblanApplicationInfo>,
        remainingEblanApplicationInfos: List<EblanApplicationInfo>,
        remainingEblanAppWidgetProviderInfos: List<EblanAppWidgetProviderInfo>,
        remainingEblanShortcutConfigs: List<EblanShortcutConfig>,
        remainingApplicationInfoGridItems: List<ApplicationInfoGridItem>,
        remainingWidgetGridItems: List<com.eblan.launcher.domain.model.grid.WidgetGridItem>,
        remainingShortcutConfigGridItems: List<ShortcutConfigGridItem>,
        remainingShortcutInfoGridItems: List<ShortcutInfoGridItem>,
    ) {
        val iconPackPackageName =
            userDataRepository.userDataFlow.first().generalSettings.iconPackPackageName

        deleteEblanApplicationInfoIcons(
            eblanApplicationInfos = remainingEblanApplicationInfos,
            eblanAppWidgetProviderInfos = remainingEblanAppWidgetProviderInfos,
            eblanShortcutConfigs = remainingEblanShortcutConfigs,
            applicationInfoGridItems = remainingApplicationInfoGridItems,
            widgetGridItems = remainingWidgetGridItems,
            shortcutConfigGridItems = remainingShortcutConfigGridItems,
            shortcutInfoGridItems = remainingShortcutInfoGridItems,
            oldDeleteEblanApplicationInfos = deleteEblanApplicationInfos.map {
                DeleteEblanApplicationInfo(
                    serialNumber = it.serialNumber,
                    componentName = it.componentName,
                    packageName = it.packageName,
                    icon = it.icon,
                )
            },
        )

        deleteEblanApplicationInfos.forEach { eblanApplicationInfo ->
            currentCoroutineContext().ensureActive()

            deleteIconPackFile(
                componentName = eblanApplicationInfo.componentName,
                iconPackPackageName = iconPackPackageName,
                remainingEblanApplicationInfos = remainingEblanApplicationInfos,
            )
        }
    }

    /** Deletes a cached icon-pack file when no remaining application references its component. */
    private suspend fun deleteIconPackFile(
        componentName: String,
        iconPackPackageName: String,
        remainingEblanApplicationInfos: List<EblanApplicationInfo>,
    ) {
        if (iconPackPackageName.isEmpty()) return

        val hasNoIconPackInfoReference = remainingEblanApplicationInfos.none {
            it.componentName == componentName
        }

        if (hasNoIconPackInfoReference) {
            val iconPacksDirectory = File(
                fileManager.getFilesDirectory(FileManager.ICON_PACKS_DIR),
                iconPackPackageName,
            )

            val iconPackFile = File(
                iconPacksDirectory,
                iconKeyGenerator.getHashedName(name = componentName),
            )

            if (iconPackFile.exists()) {
                iconPackFile.delete()
            }
        }
    }
}
