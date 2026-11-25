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
package com.eblan.launcher.domain.usecase.iconpack

import com.eblan.launcher.domain.common.Dispatcher
import com.eblan.launcher.domain.common.EblanDispatchers
import com.eblan.launcher.domain.model.iconpackinfo.EblanIconPackInfo
import com.eblan.launcher.domain.model.launcherapps.FastLauncherAppsActivityInfo
import com.eblan.launcher.domain.model.userdata.ColorSchemeSettings
import com.eblan.launcher.domain.model.userdata.ColorSource
import com.eblan.launcher.domain.model.userdata.IconShape
import com.eblan.launcher.domain.model.userdata.Theme
import com.eblan.launcher.domain.platform.FileManager
import com.eblan.launcher.domain.platform.IconKeyGenerator
import com.eblan.launcher.domain.platform.IconPackManager
import com.eblan.launcher.domain.platform.LauncherAppsWrapper
import com.eblan.launcher.domain.platform.PackageManagerWrapper
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.EblanIconPackInfoRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import com.eblan.launcher.domain.usecase.util.cacheIconPackFile
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

/** Refreshes cached icons for an installed icon pack and stores its launcher-visible metadata. */
class UpdateIconPacksUseCase @Inject constructor(
    private val launcherAppsWrapper: LauncherAppsWrapper,
    private val iconPackManager: IconPackManager,
    private val fileManager: FileManager,
    private val eblanIconPackInfoRepository: EblanIconPackInfoRepository,
    private val eblanApplicationInfoRepository: EblanApplicationInfoRepository,
    private val userDataRepository: UserDataRepository,
    private val iconKeyGenerator: IconKeyGenerator,
    private val packageManagerWrapper: PackageManagerWrapper,
    @param:Dispatcher(EblanDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) {
    /**
     * Updates icon-pack files and metadata when the package is installed for the personal user.
     *
     * @param iconPackPackageName The package name of the icon pack to refresh.
     */
    suspend operator fun invoke(iconPackPackageName: String) {
        withContext(ioDispatcher) {
            val generalSettings = userDataRepository.userDataFlow.first().generalSettings

            val eblanApplicationInfo =
                eblanApplicationInfoRepository.getEblanApplicationInfosByPackageName(
                    serialNumber = 0L,
                    packageName = iconPackPackageName,
                ).firstOrNull()

            if (eblanApplicationInfo != null) {
                updateIconPackInfos(
                    iconPackPackageName = iconPackPackageName,
                    fileManager = fileManager,
                    iconPackManager = iconPackManager,
                    fastLauncherAppsActivityInfos = launcherAppsWrapper.getFastActivityList(),
                    iconKeyGenerator = iconKeyGenerator,
                    iconTint = generalSettings.iconTint,
                    colorSource = generalSettings.colorSource,
                    customSeedColor = generalSettings.customSeedColor,
                    iconShape = generalSettings.iconShape,
                    theme = generalSettings.theme,
                    colorSchemeSettings = generalSettings.colorSchemeSettings,
                )

                val iconFile = File(
                    fileManager.getFilesDirectory(name = FileManager.ICONS_DIR),
                    iconKeyGenerator.getActivityIconKey(
                        serialNumber = 0L,
                        componentName = eblanApplicationInfo.componentName,
                    ),
                )

                val icon = packageManagerWrapper.getApplicationIconCache(
                    packageName = iconPackPackageName,
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
    }

    /** Generates cache files for installed activities. */
    private suspend fun updateIconPackInfos(
        iconPackPackageName: String,
        fileManager: FileManager,
        iconPackManager: IconPackManager,
        fastLauncherAppsActivityInfos: List<FastLauncherAppsActivityInfo>,
        iconKeyGenerator: IconKeyGenerator,
        iconTint: Boolean,
        colorSource: ColorSource,
        customSeedColor: Int,
        iconShape: IconShape,
        theme: Theme,
        colorSchemeSettings: ColorSchemeSettings,
    ) {
        if (iconPackPackageName.isEmpty()) return

        val iconPackInfoDirectory = File(
            fileManager.getFilesDirectory(name = FileManager.ICON_PACKS_DIR),
            iconPackPackageName,
        ).apply { if (!exists()) mkdirs() }

        val appFilter = iconPackManager.getIconPackComponents(packageName = iconPackPackageName)

        fastLauncherAppsActivityInfos.forEach { currentFastLauncherAppsActivityInfo ->
            currentCoroutineContext().ensureActive()

            val file = File(
                iconPackInfoDirectory,
                iconKeyGenerator.getHashedName(name = currentFastLauncherAppsActivityInfo.componentName),
            )

            cacheIconPackFile(
                iconPackManager = iconPackManager,
                appFilter = appFilter,
                iconPackPackageName = iconPackPackageName,
                file = file,
                componentName = currentFastLauncherAppsActivityInfo.componentName,
                iconTint = iconTint,
                colorSource = colorSource,
                customSeedColor = customSeedColor,
                iconShape = iconShape,
                theme = theme,
                colorSchemeSettings = colorSchemeSettings,
            )
        }
    }
}
