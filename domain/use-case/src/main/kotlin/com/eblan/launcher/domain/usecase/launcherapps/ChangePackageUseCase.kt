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
import com.eblan.launcher.domain.model.shortcutconfig.EblanShortcutConfig
import com.eblan.launcher.domain.model.shortcutinfo.EblanShortcutInfo
import com.eblan.launcher.domain.model.userdata.ColorSchemeSettings
import com.eblan.launcher.domain.model.userdata.ColorSource
import com.eblan.launcher.domain.model.userdata.IconShape
import com.eblan.launcher.domain.model.userdata.Theme
import com.eblan.launcher.domain.model.widget.EblanAppWidgetProviderInfo
import com.eblan.launcher.domain.platform.AppWidgetManagerWrapper
import com.eblan.launcher.domain.platform.FileManager
import com.eblan.launcher.domain.platform.IconKeyGenerator
import com.eblan.launcher.domain.platform.LauncherAppsWrapper
import com.eblan.launcher.domain.platform.PackageManagerWrapper
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
import javax.inject.Inject

/** Refreshes application, widget, shortcut, and shortcut-configuration data after a package change. */
class ChangePackageUseCase @Inject constructor(
    private val packageManagerWrapper: PackageManagerWrapper,
    private val eblanApplicationInfoRepository: EblanApplicationInfoRepository,
    private val applicationInfoGridItemRepository: ApplicationInfoGridItemRepository,
    private val launcherAppsWrapper: LauncherAppsWrapper,
    private val eblanAppWidgetProviderInfoRepository: EblanAppWidgetProviderInfoRepository,
    private val appWidgetManagerWrapper: AppWidgetManagerWrapper,
    private val eblanShortcutInfoRepository: EblanShortcutInfoRepository,
    private val shortcutInfoGridItemRepository: ShortcutInfoGridItemRepository,
    private val shortcutConfigGridItemRepository: ShortcutConfigGridItemRepository,
    private val eblanShortcutConfigRepository: EblanShortcutConfigRepository,
    private val fileManager: FileManager,
    private val widgetGridItemRepository: WidgetGridItemRepository,
    private val iconKeyGenerator: IconKeyGenerator,
    private val userDataRepository: UserDataRepository,
    @param:Dispatcher(EblanDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) {
    /**
     * Refreshes the installed components associated with a package for the specified user profile.
     *
     * @param serialNumber The user profile where the package is installed.
     * @param packageName The package whose components changed.
     */
    suspend operator fun invoke(
        serialNumber: Long,
        packageName: String,
    ) {
        withContext(ioDispatcher) {
            val userData = userDataRepository.userDataFlow.first()

            val (obsoleteEblanApplicationInfoIcons, obsoleteEblanShortcutConfigIcons) =
                updateEblanApplicationInfo(
                    packageName = packageName,
                    serialNumber = serialNumber,
                    iconTint = userData.generalSettings.iconTint,
                    colorSource = userData.generalSettings.colorSource,
                    customSeedColor = userData.generalSettings.customSeedColor,
                    iconShape = userData.generalSettings.iconShape,
                    theme = userData.generalSettings.theme,
                    colorSchemeSettings = userData.generalSettings.colorSchemeSettings,
                )

            val obsoleteEblanAppWidgetProviderInfoFiles =
                updateEblanAppWidgetProviderInfo(
                    serialNumber = serialNumber,
                    packageName = packageName,
                )

            val obsoleteEblanShortcutInfoIcons =
                updateEblanShortcutInfo(
                    serialNumber = serialNumber,
                    packageName = packageName,
                )

            val eblanApplicationInfos = eblanApplicationInfoRepository.getEblanApplicationInfos()
            val eblanAppWidgetProviderInfos =
                eblanAppWidgetProviderInfoRepository.getEblanAppWidgetProviderInfos()
            val eblanShortcutInfos = eblanShortcutInfoRepository.getEblanShortcutInfos()
            val eblanShortcutConfigs = eblanShortcutConfigRepository.getEblanShortcutConfigs()
            val applicationInfoGridItems =
                applicationInfoGridItemRepository.getApplicationInfoGridItems()
            val widgetGridItems = widgetGridItemRepository.getWidgetGridItems()
            val shortcutConfigGridItems =
                shortcutConfigGridItemRepository.getShortcutConfigGridItems()
            val shortcutInfoGridItems = shortcutInfoGridItemRepository.getShortcutInfoGridItems()

            deleteEblanApplicationInfoIcons(
                eblanApplicationInfos = eblanApplicationInfos,
                eblanAppWidgetProviderInfos = eblanAppWidgetProviderInfos,
                eblanShortcutConfigs = eblanShortcutConfigs,
                applicationInfoGridItems = applicationInfoGridItems,
                widgetGridItems = widgetGridItems,
                shortcutConfigGridItems = shortcutConfigGridItems,
                shortcutInfoGridItems = shortcutInfoGridItems,
                oldDeleteEblanApplicationInfos = obsoleteEblanApplicationInfoIcons,
            )

            deleteEblanShortcutConfigIcons(
                eblanApplicationInfos = eblanApplicationInfos,
                eblanAppWidgetProviderInfos = eblanAppWidgetProviderInfos,
                eblanShortcutConfigs = eblanShortcutConfigs,
                applicationInfoGridItems = applicationInfoGridItems,
                widgetGridItems = widgetGridItems,
                shortcutConfigGridItems = shortcutConfigGridItems,
                shortcutInfoGridItems = shortcutInfoGridItems,
                deleteEblanShortcutConfigs = obsoleteEblanShortcutConfigIcons,
            )

            deleteEblanAppWidgetProviderInfoIcons(
                eblanApplicationInfos = eblanApplicationInfos,
                eblanAppWidgetProviderInfos = eblanAppWidgetProviderInfos,
                eblanShortcutConfigs = eblanShortcutConfigs,
                applicationInfoGridItems = applicationInfoGridItems,
                widgetGridItems = widgetGridItems,
                shortcutConfigGridItems = shortcutConfigGridItems,
                shortcutInfoGridItems = shortcutInfoGridItems,
                deleteEblanAppWidgetProviderInfos = obsoleteEblanAppWidgetProviderInfoFiles,
            )

            deleteEblanShortInfoIcons(
                eblanShortcutInfos = eblanShortcutInfos,
                shortcutInfoGridItems = shortcutInfoGridItems,
                deleteEblanShortcutInfos = obsoleteEblanShortcutInfoIcons,
            )
        }
    }

    /** Reconciles application activities and shortcut-configuration records with the package manager. */
    private suspend fun updateEblanApplicationInfo(
        packageName: String,
        serialNumber: Long,
        iconTint: Boolean,
        colorSource: ColorSource,
        customSeedColor: Int,
        iconShape: IconShape,
        theme: Theme,
        colorSchemeSettings: ColorSchemeSettings,
    ): Pair<List<DeleteEblanApplicationInfo>, List<EblanShortcutConfig>> {
        val launcherAppsActivityInfosByPackageName = launcherAppsWrapper.getActivityListWithCacheIcons(
            serialNumber = serialNumber,
            packageName = packageName,
            iconTint = iconTint,
            colorSource = colorSource,
            customSeedColor = customSeedColor,
            iconShape = iconShape,
            theme = theme,
            colorSchemeSettings = colorSchemeSettings,
        )

        val newEblanShortcutConfigs = mutableListOf<EblanShortcutConfig>()

        val oldSyncEblanApplicationInfosByPackageName =
            eblanApplicationInfoRepository.getEblanApplicationInfosByPackageName(
                serialNumber = serialNumber,
                packageName = packageName,
            ).map {
                it.toSyncEblanApplicationInfo()
            }

        val newSyncEblanApplicationInfosByPackageName = buildList {
            launcherAppsActivityInfosByPackageName.forEach { launcherAppsActivityInfo ->
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

        val oldDeleteEblanApplicationInfos = oldSyncEblanApplicationInfosByPackageName
            .differenceByIdentity(newSyncEblanApplicationInfosByPackageName) {
                it.serialNumber to it.componentName
            }
            .map {
                it.toDeleteEblanApplicationInfo()
            }

        val obsoleteEblanApplicationInfoIcons = oldSyncEblanApplicationInfosByPackageName
            .filter { old ->
                old.icon != null &&
                    newSyncEblanApplicationInfosByPackageName.none { new ->
                        new.serialNumber == old.serialNumber &&
                            new.componentName == old.componentName &&
                            new.icon == old.icon
                    }
            }
            .map {
                it.toDeleteEblanApplicationInfo()
            }

        eblanApplicationInfoRepository.upsertSyncEblanApplicationInfos(
            syncEblanApplicationInfos = newSyncEblanApplicationInfosByPackageName,
        )

        eblanApplicationInfoRepository.deleteSyncEblanApplicationInfos(
            deleteEblanApplicationInfos = oldDeleteEblanApplicationInfos,
        )

        updateApplicationInfoGridItems(
            eblanApplicationInfos = eblanApplicationInfoRepository.getEblanApplicationInfos(),
            applicationInfoGridItemRepository = applicationInfoGridItemRepository,
        )

        val obsoleteEblanShortcutConfigIcons = updateEblanShortcutConfigs(
            serialNumber = serialNumber,
            packageName = packageName,
            newEblanShortcutConfigs = newEblanShortcutConfigs,
        )

        return obsoleteEblanApplicationInfoIcons to obsoleteEblanShortcutConfigIcons
    }

    /** Reconciles widget-provider metadata and updates or removes related grid-item data. */
    private suspend fun updateEblanAppWidgetProviderInfo(
        serialNumber: Long,
        packageName: String,
    ): List<EblanAppWidgetProviderInfo> {
        if (!packageManagerWrapper.hasSystemFeatureAppWidgets) return emptyList()

        val appWidgetManagerAppWidgetProviderInfosByPackageName =
            appWidgetManagerWrapper.getInstalledProvidersForProfilesWithCacheIcons()
                .filter {
                    it.serialNumber == serialNumber && it.packageName == packageName
                }

        val oldEblanAppWidgetProviderInfosByPackageName =
            eblanAppWidgetProviderInfoRepository.getEblanAppWidgetProviderInfos()
                .filter {
                    it.serialNumber == serialNumber && it.packageName == packageName
                }

        val newEblanAppWidgetProviderInfosByPackageName =
            appWidgetManagerAppWidgetProviderInfosByPackageName.map {
                currentCoroutineContext().ensureActive()

                it.toEblanAppWidgetProviderInfo(
                    fileManager = fileManager,
                    packageManagerWrapper = packageManagerWrapper,
                    iconKeyGenerator = iconKeyGenerator,
                )
            }

        val deleteEblanAppWidgetProviderInfos =
            oldEblanAppWidgetProviderInfosByPackageName
                .differenceByIdentity(newEblanAppWidgetProviderInfosByPackageName) {
                    it.serialNumber to it.componentName
                }

        val obsoleteEblanAppWidgetProviderInfoPreviews =
            oldEblanAppWidgetProviderInfosByPackageName.filter { oldEblanAppWidgetProviderInfo ->
                newEblanAppWidgetProviderInfosByPackageName.none { newEblanAppWidgetProviderInfo ->
                    newEblanAppWidgetProviderInfo.serialNumber == oldEblanAppWidgetProviderInfo.serialNumber &&
                        newEblanAppWidgetProviderInfo.componentName == oldEblanAppWidgetProviderInfo.componentName &&
                        newEblanAppWidgetProviderInfo.preview == oldEblanAppWidgetProviderInfo.preview &&
                        newEblanAppWidgetProviderInfo.applicationIcon == oldEblanAppWidgetProviderInfo.applicationIcon
                }
            }

        eblanAppWidgetProviderInfoRepository.deleteEblanAppWidgetProviderInfos(
            eblanAppWidgetProviderInfos = deleteEblanAppWidgetProviderInfos,
        )

        eblanAppWidgetProviderInfoRepository.upsertEblanAppWidgetProviderInfos(
            eblanAppWidgetProviderInfos = newEblanAppWidgetProviderInfosByPackageName,
        )

        updateWidgetGridItems(
            eblanAppWidgetProviderInfos = eblanAppWidgetProviderInfoRepository.getEblanAppWidgetProviderInfos(),
            fileManager = fileManager,
            packageManagerWrapper = packageManagerWrapper,
            widgetGridItemRepository = widgetGridItemRepository,
            iconKeyGenerator = iconKeyGenerator,
        )

        return obsoleteEblanAppWidgetProviderInfoPreviews
    }

    /** Reconciles launcher shortcuts and updates or removes their grid-item data. */
    private suspend fun updateEblanShortcutInfo(
        serialNumber: Long,
        packageName: String,
    ): List<EblanShortcutInfo> {
        if (!launcherAppsWrapper.hasShortcutHostPermission) return emptyList()

        val launcherAppsShortcutInfosByPackageName = launcherAppsWrapper.getShortcutsByPackageNameWithCacheIcons(
            serialNumber = serialNumber,
            packageName = packageName,
        ) ?: return emptyList()

        val oldEblanShortcutInfosByPackageName =
            eblanShortcutInfoRepository.getEblanShortcutInfosByPackageName(
                serialNumber = serialNumber,
                packageName = packageName,
            )

        val newEblanShortcutInfosByPackageName =
            launcherAppsShortcutInfosByPackageName.map {
                it.toEblanShortcutInfo()
            }

        val deleteEblanShortcutInfos =
            oldEblanShortcutInfosByPackageName
                .differenceByIdentity(newEblanShortcutInfosByPackageName) {
                    Triple(it.serialNumber, it.shortcutId, it.packageName)
                }

        val obsoleteEblanShortcutInfoIcons = oldEblanShortcutInfosByPackageName.filter { old ->
            old.icon != null &&
                newEblanShortcutInfosByPackageName.none { new ->
                    new.serialNumber == old.serialNumber &&
                        new.shortcutId == old.shortcutId &&
                        new.packageName == old.packageName &&
                        new.icon == old.icon
                }
        }

        eblanShortcutInfoRepository.upsertEblanShortcutInfos(
            eblanShortcutInfos = newEblanShortcutInfosByPackageName,
        )

        eblanShortcutInfoRepository.deleteEblanShortcutInfos(
            eblanShortcutInfos = deleteEblanShortcutInfos,
        )

        updateShortcutInfoGridItems(
            eblanShortcutInfos = eblanShortcutInfoRepository.getEblanShortcutInfos(),
            shortcutInfoGridItemRepository = shortcutInfoGridItemRepository,
            fileManager = fileManager,
            packageManagerWrapper = packageManagerWrapper,
            iconKeyGenerator = iconKeyGenerator,
        )

        return obsoleteEblanShortcutInfoIcons
    }

    /** Reconciles shortcut-configuration activities and their grid-item data for this package. */
    private suspend fun updateEblanShortcutConfigs(
        serialNumber: Long,
        packageName: String,
        newEblanShortcutConfigs: List<EblanShortcutConfig>,
    ): List<EblanShortcutConfig> {
        val oldEblanShortcutConfigsByPackageName =
            eblanShortcutConfigRepository.getEblanShortcutConfigsByPackageName(
                serialNumber = serialNumber,
                packageName = packageName,
            )

        val deleteEblanShortcutConfigs = oldEblanShortcutConfigsByPackageName
            .differenceByIdentity(newEblanShortcutConfigs) {
                it.serialNumber to it.componentName
            }

        val obsoleteEblanShortcutConfigIcons = oldEblanShortcutConfigsByPackageName.filter { old ->
            newEblanShortcutConfigs.none { new ->
                new.serialNumber == old.serialNumber &&
                    new.componentName == old.componentName &&
                    new.activityIcon == old.activityIcon &&
                    new.applicationIcon == old.applicationIcon
            }
        }

        eblanShortcutConfigRepository.upsertEblanShortcutConfigs(
            eblanShortcutConfigs = newEblanShortcutConfigs,
        )

        eblanShortcutConfigRepository.deleteEblanShortcutConfigs(
            eblanShortcutConfigs = deleteEblanShortcutConfigs,
        )

        updateShortcutConfigGridItems(
            eblanShortcutConfigs = eblanShortcutConfigRepository.getEblanShortcutConfigs(),
            shortcutConfigGridItemRepository = shortcutConfigGridItemRepository,
            fileManager = fileManager,
            packageManagerWrapper = packageManagerWrapper,
            iconKeyGenerator = iconKeyGenerator,
        )

        return obsoleteEblanShortcutConfigIcons
    }
}
