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
import com.eblan.launcher.domain.model.grid.ApplicationInfoGridItem
import com.eblan.launcher.domain.model.grid.ShortcutConfigGridItem
import com.eblan.launcher.domain.model.grid.ShortcutInfoGridItem
import com.eblan.launcher.domain.model.grid.WidgetGridItem
import com.eblan.launcher.domain.model.shortcutconfig.EblanShortcutConfig
import com.eblan.launcher.domain.model.shortcutinfo.EblanShortcutInfo
import com.eblan.launcher.domain.model.widget.EblanAppWidgetProviderInfo
import com.eblan.launcher.domain.platform.FileManager
import com.eblan.launcher.domain.platform.IconKeyGenerator
import com.eblan.launcher.domain.repository.ApplicationInfoGridItemRepository
import com.eblan.launcher.domain.repository.EblanAppWidgetProviderInfoRepository
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.EblanShortcutConfigRepository
import com.eblan.launcher.domain.repository.EblanShortcutInfoRepository
import com.eblan.launcher.domain.repository.ShortcutConfigGridItemRepository
import com.eblan.launcher.domain.repository.ShortcutInfoGridItemRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import com.eblan.launcher.domain.repository.WidgetGridItemRepository
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
            val eblanApplicationInfosByPackageName =
                eblanApplicationInfoRepository.getEblanApplicationInfosByPackageName(
                    serialNumber = serialNumber,
                    packageName = packageName,
                )

            val applicationInfoGridItemsByPackageName =
                applicationInfoGridItemRepository.getApplicationInfoGridItemsByPackageName(
                    serialNumber = serialNumber,
                    packageName = packageName,
                )

            val deleteEblanAppWidgetProviderInfos =
                eblanAppWidgetProviderInfoRepository.getEblanAppWidgetProviderInfos()
                    .filter {
                        it.serialNumber == serialNumber && it.packageName == packageName
                    }

            val eblanShortcutInfosByPackageName =
                eblanShortcutInfoRepository.getEblanShortcutInfosByPackageName(
                    serialNumber = serialNumber,
                    packageName = packageName,
                )

            val shortcutInfoGridItemsByPackageName =
                shortcutInfoGridItemRepository.getShortcutInfoGridItemsByPackageName(
                    serialNumber = serialNumber,
                    packageName = packageName,
                )

            val eblanShortcutConfigsByPackageName =
                eblanShortcutConfigRepository.getEblanShortcutConfigsByPackageName(
                    serialNumber = serialNumber,
                    packageName = packageName,
                )

            val shortcutConfigGridItemsByPackageName =
                shortcutConfigGridItemRepository.getShortcutConfigGridItemsByPackageName(
                    serialNumber = serialNumber,
                    packageName = packageName,
                )

            eblanApplicationInfoRepository.deleteEblanApplicationInfoByPackageName(
                serialNumber = serialNumber,
                packageName = packageName,
            )

            eblanAppWidgetProviderInfoRepository.deleteEblanAppWidgetProviderInfoByPackageName(
                packageName = packageName,
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

            val remainingEblanShortcutConfigs =
                eblanShortcutConfigRepository.getEblanShortcutConfigs()

            val remainingEblanShortcutInfos =
                eblanShortcutInfoRepository.getEblanShortcutInfos()

            val remainingApplicationInfoGridItems =
                applicationInfoGridItemRepository.getApplicationInfoGridItems()

            val remainingWidgetGridItems =
                widgetGridItemRepository.getWidgetGridItems()

            val remainingShortcutConfigGridItems =
                shortcutConfigGridItemRepository.getShortcutConfigGridItems()

            val remainingShortcutInfoGridItems =
                shortcutInfoGridItemRepository.getShortcutInfoGridItems()

            deleteEblanApplicationInfoFiles(
                eblanApplicationInfosByPackageName = eblanApplicationInfosByPackageName,
                applicationInfoGridItemsByPackageName = applicationInfoGridItemsByPackageName,
                remainingEblanApplicationInfos = remainingEblanApplicationInfos,
                remainingEblanAppWidgetProviderInfos = remainingEblanAppWidgetProviderInfos,
                remainingEblanShortcutConfigs = remainingEblanShortcutConfigs,
                remainingApplicationInfoGridItems = remainingApplicationInfoGridItems,
                remainingWidgetGridItems = remainingWidgetGridItems,
                remainingShortcutConfigGridItems = remainingShortcutConfigGridItems,
                remainingShortcutInfoGridItems = remainingShortcutInfoGridItems,
            )

            deleteEblanAppWidgetProviderInfoFiles(
                eblanApplicationInfos = remainingEblanApplicationInfos,
                eblanAppWidgetProviderInfos = remainingEblanAppWidgetProviderInfos,
                eblanShortcutConfigs = remainingEblanShortcutConfigs,
                applicationInfoGridItems = remainingApplicationInfoGridItems,
                widgetGridItems = remainingWidgetGridItems,
                shortcutConfigGridItems = remainingShortcutConfigGridItems,
                shortcutInfoGridItems = remainingShortcutInfoGridItems,
                deleteEblanAppWidgetProviderInfos = deleteEblanAppWidgetProviderInfos,
            )

            deleteEblanShortcutInfoFiles(
                eblanShortcutInfosByPackageName = eblanShortcutInfosByPackageName,
                shortcutInfoGridItemsByPackageName = shortcutInfoGridItemsByPackageName,
                remainingEblanApplicationInfos = remainingEblanApplicationInfos,
                remainingEblanAppWidgetProviderInfos = remainingEblanAppWidgetProviderInfos,
                remainingEblanShortcutConfigs = remainingEblanShortcutConfigs,
                remainingEblanShortcutInfos = remainingEblanShortcutInfos,
                remainingApplicationInfoGridItems = remainingApplicationInfoGridItems,
                remainingWidgetGridItems = remainingWidgetGridItems,
                remainingShortcutConfigGridItems = remainingShortcutConfigGridItems,
                remainingShortcutInfoGridItems = remainingShortcutInfoGridItems,
            )

            deleteEblanShortcutConfigFiles(
                eblanShortcutConfigsByPackageName = eblanShortcutConfigsByPackageName,
                shortcutConfigGridItemsByPackageName = shortcutConfigGridItemsByPackageName,
                remainingEblanApplicationInfos = remainingEblanApplicationInfos,
                remainingEblanAppWidgetProviderInfos = remainingEblanAppWidgetProviderInfos,
                remainingEblanShortcutConfigs = remainingEblanShortcutConfigs,
                remainingApplicationInfoGridItems = remainingApplicationInfoGridItems,
                remainingWidgetGridItems = remainingWidgetGridItems,
                remainingShortcutConfigGridItems = remainingShortcutConfigGridItems,
                remainingShortcutInfoGridItems = remainingShortcutInfoGridItems,
            )
        }
    }

    /** Deletes unreferenced application icons and custom icons for the package's grid items. */
    private suspend fun deleteEblanApplicationInfoFiles(
        eblanApplicationInfosByPackageName: List<EblanApplicationInfo>,
        applicationInfoGridItemsByPackageName: List<ApplicationInfoGridItem>,
        remainingEblanApplicationInfos: List<EblanApplicationInfo>,
        remainingEblanAppWidgetProviderInfos: List<EblanAppWidgetProviderInfo>,
        remainingEblanShortcutConfigs: List<EblanShortcutConfig>,
        remainingApplicationInfoGridItems: List<ApplicationInfoGridItem>,
        remainingWidgetGridItems: List<WidgetGridItem>,
        remainingShortcutConfigGridItems: List<ShortcutConfigGridItem>,
        remainingShortcutInfoGridItems: List<ShortcutInfoGridItem>,
    ) {
        val iconPackPackageName =
            userDataRepository.userDataFlow.first().generalSettings.iconPackPackageName

        eblanApplicationInfosByPackageName.forEach { eblanApplicationInfoByPackageName ->
            currentCoroutineContext().ensureActive()

            val icon = eblanApplicationInfoByPackageName.icon

            if (icon != null && !isIconReferenced(
                    path = icon,
                    remainingEblanApplicationInfos = remainingEblanApplicationInfos,
                    remainingEblanAppWidgetProviderInfos = remainingEblanAppWidgetProviderInfos,
                    remainingEblanShortcutConfigs = remainingEblanShortcutConfigs,
                    remainingApplicationInfoGridItems = remainingApplicationInfoGridItems,
                    remainingWidgetGridItems = remainingWidgetGridItems,
                    remainingShortcutConfigGridItems = remainingShortcutConfigGridItems,
                    remainingShortcutInfoGridItems = remainingShortcutInfoGridItems,
                )
            ) {
                deleteFileIfExists(path = icon)
            }

            deleteIconPackFile(
                componentName = eblanApplicationInfoByPackageName.componentName,
                iconPackPackageName = iconPackPackageName,
                remainingEblanApplicationInfos = remainingEblanApplicationInfos,
            )
        }

        applicationInfoGridItemsByPackageName.forEach { applicationInfoGridItem ->
            currentCoroutineContext().ensureActive()

            applicationInfoGridItem.customIcon?.let { customIcon ->
                if (remainingApplicationInfoGridItems.none { it.customIcon == customIcon }) {
                    deleteFileIfExists(path = customIcon)
                }
            }
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

    /** Deletes unreferenced shortcut icons and custom icons for the package's shortcut items. */
    private suspend fun deleteEblanShortcutInfoFiles(
        eblanShortcutInfosByPackageName: List<EblanShortcutInfo>,
        shortcutInfoGridItemsByPackageName: List<ShortcutInfoGridItem>,
        remainingEblanApplicationInfos: List<EblanApplicationInfo>,
        remainingEblanAppWidgetProviderInfos: List<EblanAppWidgetProviderInfo>,
        remainingEblanShortcutConfigs: List<EblanShortcutConfig>,
        remainingEblanShortcutInfos: List<EblanShortcutInfo>,
        remainingApplicationInfoGridItems: List<ApplicationInfoGridItem>,
        remainingWidgetGridItems: List<WidgetGridItem>,
        remainingShortcutConfigGridItems: List<ShortcutConfigGridItem>,
        remainingShortcutInfoGridItems: List<ShortcutInfoGridItem>,
    ) {
        eblanShortcutInfosByPackageName.forEach { eblanShortcutInfoByPackageName ->
            currentCoroutineContext().ensureActive()

            val icon = eblanShortcutInfoByPackageName.icon

            if (icon != null &&
                remainingEblanShortcutInfos.none {
                    currentCoroutineContext().ensureActive()

                    it.icon == icon
                } &&
                remainingShortcutInfoGridItems.none {
                    currentCoroutineContext().ensureActive()

                    it.icon == icon
                }
            ) {
                deleteFileIfExists(path = icon)
            }
        }

        shortcutInfoGridItemsByPackageName.forEach { shortcutInfoGridItem ->
            currentCoroutineContext().ensureActive()

            shortcutInfoGridItem.customIcon?.let { customIcon ->
                if (remainingShortcutInfoGridItems.none { it.customIcon == customIcon }) {
                    deleteFileIfExists(path = customIcon)
                }
            }

            shortcutInfoGridItem.eblanApplicationInfoIcon?.let { appIcon ->
                if (!isIconReferenced(
                        path = appIcon,
                        remainingEblanApplicationInfos = remainingEblanApplicationInfos,
                        remainingEblanAppWidgetProviderInfos = remainingEblanAppWidgetProviderInfos,
                        remainingEblanShortcutConfigs = remainingEblanShortcutConfigs,
                        remainingApplicationInfoGridItems = remainingApplicationInfoGridItems,
                        remainingWidgetGridItems = remainingWidgetGridItems,
                        remainingShortcutConfigGridItems = remainingShortcutConfigGridItems,
                        remainingShortcutInfoGridItems = remainingShortcutInfoGridItems,
                    )
                ) {
                    deleteFileIfExists(path = appIcon)
                }
            }
        }
    }

    /** Deletes unreferenced shortcut-configuration icons and shortcut-intent icon files. */
    private suspend fun deleteEblanShortcutConfigFiles(
        eblanShortcutConfigsByPackageName: List<EblanShortcutConfig>,
        shortcutConfigGridItemsByPackageName: List<ShortcutConfigGridItem>,
        remainingEblanApplicationInfos: List<EblanApplicationInfo>,
        remainingEblanAppWidgetProviderInfos: List<EblanAppWidgetProviderInfo>,
        remainingEblanShortcutConfigs: List<EblanShortcutConfig>,
        remainingApplicationInfoGridItems: List<ApplicationInfoGridItem>,
        remainingWidgetGridItems: List<WidgetGridItem>,
        remainingShortcutConfigGridItems: List<ShortcutConfigGridItem>,
        remainingShortcutInfoGridItems: List<ShortcutInfoGridItem>,
    ) {
        eblanShortcutConfigsByPackageName.forEach { eblanShortcutConfigByPackageName ->
            currentCoroutineContext().ensureActive()

            eblanShortcutConfigByPackageName.activityIcon?.let { activityIcon ->
                if (!isIconReferenced(
                        path = activityIcon,
                        remainingEblanApplicationInfos = remainingEblanApplicationInfos,
                        remainingEblanAppWidgetProviderInfos = remainingEblanAppWidgetProviderInfos,
                        remainingEblanShortcutConfigs = remainingEblanShortcutConfigs,
                        remainingApplicationInfoGridItems = remainingApplicationInfoGridItems,
                        remainingWidgetGridItems = remainingWidgetGridItems,
                        remainingShortcutConfigGridItems = remainingShortcutConfigGridItems,
                        remainingShortcutInfoGridItems = remainingShortcutInfoGridItems,
                    )
                ) {
                    deleteFileIfExists(path = activityIcon)
                }
            }

            eblanShortcutConfigByPackageName.applicationIcon?.let { applicationIcon ->
                if (!isIconReferenced(
                        path = applicationIcon,
                        remainingEblanApplicationInfos = remainingEblanApplicationInfos,
                        remainingEblanAppWidgetProviderInfos = remainingEblanAppWidgetProviderInfos,
                        remainingEblanShortcutConfigs = remainingEblanShortcutConfigs,
                        remainingApplicationInfoGridItems = remainingApplicationInfoGridItems,
                        remainingWidgetGridItems = remainingWidgetGridItems,
                        remainingShortcutConfigGridItems = remainingShortcutConfigGridItems,
                        remainingShortcutInfoGridItems = remainingShortcutInfoGridItems,
                    )
                ) {
                    deleteFileIfExists(path = applicationIcon)
                }
            }
        }

        shortcutConfigGridItemsByPackageName.forEach { shortcutConfigGridItem ->
            currentCoroutineContext().ensureActive()

            shortcutConfigGridItem.shortcutIntentIcon?.let { shortcutIntentIcon ->
                if (remainingShortcutConfigGridItems.none { it.shortcutIntentIcon == shortcutIntentIcon }) {
                    deleteFileIfExists(path = shortcutIntentIcon)
                }
            }

            shortcutConfigGridItem.customIcon?.let { customIcon ->
                if (remainingShortcutConfigGridItems.none { it.customIcon == customIcon }) {
                    deleteFileIfExists(path = customIcon)
                }
            }
        }
    }

    private fun isIconReferenced(
        path: String,
        remainingEblanApplicationInfos: List<EblanApplicationInfo>,
        remainingEblanAppWidgetProviderInfos: List<EblanAppWidgetProviderInfo>,
        remainingEblanShortcutConfigs: List<EblanShortcutConfig>,
        remainingApplicationInfoGridItems: List<ApplicationInfoGridItem>,
        remainingWidgetGridItems: List<WidgetGridItem>,
        remainingShortcutConfigGridItems: List<ShortcutConfigGridItem>,
        remainingShortcutInfoGridItems: List<ShortcutInfoGridItem>,
    ): Boolean = remainingEblanApplicationInfos.any {
        it.icon == path
    } ||
        remainingEblanAppWidgetProviderInfos.any {
            it.applicationIcon == path
        } ||
        remainingEblanShortcutConfigs.any {
            it.activityIcon == path || it.applicationIcon == path
        } ||
        remainingApplicationInfoGridItems.any {
            it.icon == path
        } ||
        remainingWidgetGridItems.any {
            it.icon == path
        } ||
        remainingShortcutConfigGridItems.any {
            it.activityIcon == path || it.applicationIcon == path
        } ||
        remainingShortcutInfoGridItems.any {
            it.eblanApplicationInfoIcon == path
        }

    private fun deleteFileIfExists(path: String) {
        val file = File(path)

        if (file.exists()) {
            file.delete()
        }
    }
}
