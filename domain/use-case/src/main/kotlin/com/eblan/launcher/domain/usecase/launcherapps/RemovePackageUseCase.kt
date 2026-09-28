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
import com.eblan.launcher.domain.framework.PackageManagerWrapper
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
    private val packageManagerWrapper: PackageManagerWrapper,
    private val iconKeyGenerator: IconKeyGenerator,
    @param:Dispatcher(EblanDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(
        serialNumber: Long,
        packageName: String,
    ) {
        withContext(ioDispatcher) {
            deleteEblanApplicationInfoFiles(
                packageName = packageName,
                serialNumber = serialNumber,
            )

            deleteEblanAppWidgetProviderInfoFiles(packageName = packageName)

            deleteEblaShortcutInfoFiles(
                serialNumber = serialNumber,
                packageName = packageName,
            )

            deleteEblanShortcutConfigFiles(
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
        }
    }

    private suspend fun deleteEblanApplicationInfoFiles(
        packageName: String,
        serialNumber: Long,
    ) {
        val iconPackInfoPackageName =
            userDataRepository.userDataFlow.first().generalSettings.iconPackInfoPackageName

        val componentName = packageManagerWrapper.getComponentName(packageName = packageName)

        eblanApplicationInfoRepository.getEblanApplicationInfosByPackageName(
            serialNumber = serialNumber,
            packageName = packageName,
        ).forEach { eblanApplicationInfoByPackageName ->
            currentCoroutineContext().ensureActive()

            val icon = eblanApplicationInfoByPackageName.icon

            val hasNoIconReference = icon != null &&
                eblanApplicationInfoRepository.getEblanApplicationInfos()
                    .none {
                        it.icon == icon
                    }

            if (hasNoIconReference) {
                val iconFile = File(icon)

                if (iconFile.exists()) {
                    iconFile.delete()
                }

                deleteIconPackFile(
                    componentName = componentName,
                    iconPackInfoPackageName = iconPackInfoPackageName,
                )
            }
        }

        applicationInfoGridItemRepository.getApplicationInfoGridItemsByPackageName(
            serialNumber = serialNumber,
            packageName = packageName,
        ).forEach { applicationInfoGridItem ->
            currentCoroutineContext().ensureActive()

            applicationInfoGridItem.customIcon?.let {
                val customIconFile = File(it)

                if (customIconFile.exists()) {
                    customIconFile.delete()
                }
            }
        }
    }

    private suspend fun deleteIconPackFile(
        componentName: String?,
        iconPackInfoPackageName: String,
    ) {
        if (componentName == null) return

        val hasNoIconPackInfoReference = eblanApplicationInfoRepository.getEblanApplicationInfos()
            .none {
                it.componentName == componentName
            }

        if (hasNoIconPackInfoReference) {
            val iconPacksDirectory = File(
                fileManager.getFilesDirectory(FileManager.ICON_PACKS_DIR),
                iconPackInfoPackageName,
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

    private suspend fun deleteEblanAppWidgetProviderInfoFiles(packageName: String) {
        eblanAppWidgetProviderInfoRepository.getEblanAppWidgetProviderInfosByPackageName(
            packageName = packageName,
        ).forEach { eblanAppWidgetProviderInfo ->
            currentCoroutineContext().ensureActive()

            eblanAppWidgetProviderInfo.applicationIcon?.let {
                val iconFile = File(it)

                if (iconFile.exists()) {
                    iconFile.delete()
                }
            }

            eblanAppWidgetProviderInfo.preview?.let {
                val previewFile = File(it)

                if (previewFile.exists()) {
                    previewFile.delete()
                }
            }
        }
    }

    private suspend fun deleteEblaShortcutInfoFiles(
        serialNumber: Long,
        packageName: String,
    ) {
        eblanShortcutInfoRepository.getEblanShortcutInfosByPackageName(
            serialNumber = serialNumber,
            packageName = packageName,
        ).forEach { eblanShortcutInfoByPackageName ->
            currentCoroutineContext().ensureActive()

            val icon = eblanShortcutInfoByPackageName.icon

            val hasNoIconReference = icon != null &&
                eblanShortcutInfoRepository.getEblanShortcutInfos().none {
                    currentCoroutineContext().ensureActive()

                    it.icon == icon
                }

            if (hasNoIconReference) {
                val iconFile = File(icon)

                if (iconFile.exists()) {
                    iconFile.delete()
                }
            }
        }

        shortcutInfoGridItemRepository.getShortcutInfoGridItemsByPackageName(
            serialNumber = serialNumber,
            packageName = packageName,
        ).forEach { shortcutInfoGridItem ->
            currentCoroutineContext().ensureActive()

            shortcutInfoGridItem.customIcon?.let {
                val customIconFile = File(it)

                if (customIconFile.exists()) {
                    customIconFile.delete()
                }
            }
        }
    }

    private suspend fun deleteEblanShortcutConfigFiles(
        serialNumber: Long,
        packageName: String,
    ) {
        eblanShortcutConfigRepository.getEblanShortcutConfigsByPackageName(
            serialNumber = serialNumber,
            packageName = packageName,
        ).forEach { eblanShortcutConfigByPackageName ->
            currentCoroutineContext().ensureActive()

            val activityIcon = eblanShortcutConfigByPackageName.activityIcon

            val hasNoIconReference = activityIcon != null &&
                eblanShortcutConfigRepository.getEblanShortcutConfigs()
                    .none {
                        it.activityIcon == activityIcon
                    }

            if (hasNoIconReference) {
                val activityIconFile = File(activityIcon)

                if (activityIconFile.exists()) {
                    activityIconFile.delete()
                }

                eblanShortcutConfigByPackageName.applicationIcon?.let {
                    val applicationIconFile = File(it)

                    if (applicationIconFile.exists()) {
                        applicationIconFile.delete()
                    }
                }
            }
        }

        shortcutConfigGridItemRepository.getShortcutConfigGridItemsByPackageName(
            serialNumber = serialNumber,
            packageName = packageName,
        ).forEach { shortcutConfigGridItem ->
            currentCoroutineContext().ensureActive()

            shortcutConfigGridItem.shortcutIntentIcon?.let {
                val shortcutIntentIconFile = File(it)

                if (shortcutIntentIconFile.exists()) {
                    shortcutIntentIconFile.delete()
                }
            }
        }
    }
}
