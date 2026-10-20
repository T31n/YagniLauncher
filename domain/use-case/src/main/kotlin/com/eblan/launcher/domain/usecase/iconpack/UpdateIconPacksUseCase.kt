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
import com.eblan.launcher.domain.common.FileManager
import com.eblan.launcher.domain.common.IconKeyGenerator
import com.eblan.launcher.domain.framework.IconPackManager
import com.eblan.launcher.domain.framework.LauncherAppsWrapper
import com.eblan.launcher.domain.model.iconpackinfo.EblanIconPackInfo
import com.eblan.launcher.domain.model.launcherapps.FastLauncherAppsActivityInfo
import com.eblan.launcher.domain.model.userdata.GeneralSettings
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
                    generalSettings = generalSettings,
                )

                eblanIconPackInfoRepository.upsertEblanIconPackInfo(
                    eblanIconPackInfo = EblanIconPackInfo(
                        packageName = eblanApplicationInfo.packageName,
                        icon = eblanApplicationInfo.icon,
                        label = eblanApplicationInfo.label,
                    ),
                )
            }
        }
    }

    /** Generates cache files for installed activities and removes stale cached files. */
    private suspend fun updateIconPackInfos(
        iconPackPackageName: String,
        fileManager: FileManager,
        iconPackManager: IconPackManager,
        fastLauncherAppsActivityInfos: List<FastLauncherAppsActivityInfo>,
        iconKeyGenerator: IconKeyGenerator,
        generalSettings: GeneralSettings,
    ) {
        if (iconPackPackageName.isEmpty()) return

        val iconPackInfoDirectory = File(
            fileManager.getFilesDirectory(name = FileManager.ICON_PACKS_DIR),
            iconPackPackageName,
        ).apply { if (!exists()) mkdirs() }

        val appFilter = iconPackManager.getIconPackComponents(packageName = iconPackPackageName)

        val installedComponentHashedNames = buildSet {
            fastLauncherAppsActivityInfos.forEach {
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

                add(iconKeyGenerator.getHashedName(name = it.componentName))
            }
        }

        iconPackInfoDirectory.listFiles()
            ?.filter {
                currentCoroutineContext().ensureActive()

                it.isFile && it.name !in installedComponentHashedNames
            }
            ?.forEach {
                currentCoroutineContext().ensureActive()

                it.delete()
            }
    }
}
