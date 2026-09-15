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
import com.eblan.launcher.domain.framework.LauncherAppsWrapper
import com.eblan.launcher.domain.framework.PackageManagerWrapper
import com.eblan.launcher.domain.model.shortcutconfig.EblanShortcutConfig
import com.eblan.launcher.domain.repository.ApplicationInfoGridItemRepository
import com.eblan.launcher.domain.repository.EblanAppWidgetProviderInfoRepository
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.EblanShortcutConfigRepository
import com.eblan.launcher.domain.repository.EblanShortcutInfoRepository
import com.eblan.launcher.domain.repository.ShortcutConfigGridItemRepository
import com.eblan.launcher.domain.repository.ShortcutInfoGridItemRepository
import com.eblan.launcher.domain.repository.WidgetGridItemRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import javax.inject.Inject

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
    @param:Dispatcher(EblanDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(
        serialNumber: Long,
        packageName: String,
    ) {
        withContext(ioDispatcher) {
            updateEblanApplicationInfo(
                packageName = packageName,
                serialNumber = serialNumber,
            )

            updateEblanAppWidgetProviderInfo(
                serialNumber = serialNumber,
                packageName = packageName,
            )

            updateEblanShortcutInfo(
                serialNumber = serialNumber,
                packageName = packageName,
            )
        }
    }

    private suspend fun updateEblanApplicationInfo(
        packageName: String,
        serialNumber: Long,
    ) {
        val launcherAppsActivityInfosByPackageName = launcherAppsWrapper.getActivityListWithCacheIcons(
            serialNumber = serialNumber,
            packageName = packageName,
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

        val newDeleteEblanApplicationInfos =
            newSyncEblanApplicationInfosByPackageName.map {
                it.toDeleteEblanApplicationInfo()
            }.toSet()

        val oldDeleteEblanApplicationInfos =
            oldSyncEblanApplicationInfosByPackageName.map {
                it.toDeleteEblanApplicationInfo()
            }
                .filterNot { it in newDeleteEblanApplicationInfos }

        eblanApplicationInfoRepository.upsertSyncEblanApplicationInfos(
            syncEblanApplicationInfos = newSyncEblanApplicationInfosByPackageName,
        )

        eblanApplicationInfoRepository.deleteSyncEblanApplicationInfos(
            deleteEblanApplicationInfos = oldDeleteEblanApplicationInfos,
        )

        deleteEblanApplicationInfoIcons(
            eblanApplicationInfos = eblanApplicationInfoRepository.getEblanApplicationInfos(),
            eblanAppWidgetProviderInfos = eblanAppWidgetProviderInfoRepository.getEblanAppWidgetProviderInfos(),
            oldDeleteEblanApplicationInfos = oldDeleteEblanApplicationInfos,
        )

        updateApplicationInfoGridItems(
            eblanApplicationInfos = eblanApplicationInfoRepository.getEblanApplicationInfos(),
            applicationInfoGridItemRepository = applicationInfoGridItemRepository,
        )

        updateEblanShortcutConfigs(
            serialNumber = serialNumber,
            packageName = packageName,
            newEblanShortcutConfigs = newEblanShortcutConfigs,
        )
    }

    private suspend fun updateEblanAppWidgetProviderInfo(
        serialNumber: Long,
        packageName: String,
    ) {
        if (!packageManagerWrapper.hasSystemFeatureAppWidgets) return

        val appWidgetManagerAppWidgetProviderInfosByPackageName =
            appWidgetManagerWrapper.getInstalledProvidersWithCacheIcons()
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

        val newDeleteEblanAppWidgetProviderInfos =
            newEblanAppWidgetProviderInfosByPackageName.map {
                it.toDeleteEblanAppWidgetProviderInfo()
            }.toSet()

        val oldDeleteEblanAppWidgetProviderInfos =
            oldEblanAppWidgetProviderInfosByPackageName.map {
                it.toDeleteEblanAppWidgetProviderInfo()
            }.filterNot {
                it in newDeleteEblanAppWidgetProviderInfos
            }

        eblanAppWidgetProviderInfoRepository.upsertEblanAppWidgetProviderInfos(
            eblanAppWidgetProviderInfos = newEblanAppWidgetProviderInfosByPackageName,
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

    private suspend fun updateEblanShortcutInfo(
        serialNumber: Long,
        packageName: String,
    ) {
        if (!launcherAppsWrapper.hasShortcutHostPermission) return

        val launcherAppsShortcutInfosByPackageName = launcherAppsWrapper.getShortcutsByPackageNameWithCacheIcons(
            serialNumber = serialNumber,
            packageName = packageName,
        ) ?: return

        val oldEblanShortcutInfosByPackageName =
            eblanShortcutInfoRepository.getEblanShortcutInfosByPackageName(
                serialNumber = serialNumber,
                packageName = packageName,
            )

        val newEblanShortcutInfosByPackageName =
            launcherAppsShortcutInfosByPackageName.map {
                it.toEblanShortcutInfo()
            }

        val newDeleteEblanShortcutInfos =
            newEblanShortcutInfosByPackageName.map {
                it.toDeleteEblanShortcutInfo()
            }.toSet()

        val oldDeleteEblanShortcutInfos =
            oldEblanShortcutInfosByPackageName.map {
                it.toDeleteEblanShortcutInfo()
            }.filterNot {
                it in newDeleteEblanShortcutInfos
            }

        eblanShortcutInfoRepository.upsertEblanShortcutInfos(
            eblanShortcutInfos = newEblanShortcutInfosByPackageName,
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
        serialNumber: Long,
        packageName: String,
        newEblanShortcutConfigs: List<EblanShortcutConfig>,
    ) {
        val oldEblanShortcutConfigsByPackageName =
            eblanShortcutConfigRepository.getEblanShortcutConfigsByPackageName(
                serialNumber = serialNumber,
                packageName = packageName,
            )

        val newDeleteEblanShortcutConfigs = newEblanShortcutConfigs.map {
            it.toDeleteEblanShortcutConfig()
        }.toSet()

        val oldDeleteEblanShortcutConfigs =
            oldEblanShortcutConfigsByPackageName.map {
                it.toDeleteEblanShortcutConfig()
            }.filterNot {
                it in newDeleteEblanShortcutConfigs
            }

        eblanShortcutConfigRepository.upsertEblanShortcutConfigs(
            eblanShortcutConfigs = newEblanShortcutConfigs,
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
}
