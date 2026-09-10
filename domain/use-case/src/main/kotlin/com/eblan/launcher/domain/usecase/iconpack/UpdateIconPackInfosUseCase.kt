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
import com.eblan.launcher.domain.model.EblanIconPackInfo
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.EblanIconPackInfoRepository
import com.eblan.launcher.domain.usecase.util.updateIconPackInfos
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UpdateIconPackInfosUseCase @Inject constructor(
    private val launcherAppsWrapper: LauncherAppsWrapper,
    private val iconPackManager: IconPackManager,
    private val fileManager: FileManager,
    private val eblanIconPackInfoRepository: EblanIconPackInfoRepository,
    private val eblanApplicationInfoRepository: EblanApplicationInfoRepository,
    private val iconKeyGenerator: IconKeyGenerator,
    @param:Dispatcher(EblanDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(iconPackInfoPackageName: String) {
        withContext(ioDispatcher) {
            val eblanApplicationInfo =
                eblanApplicationInfoRepository.getEblanApplicationInfosByPackageName(
                    serialNumber = 0L,
                    packageName = iconPackInfoPackageName,
                ).firstOrNull()

            if (eblanApplicationInfo != null) {
                updateIconPackInfos(
                    iconPackInfoPackageName = iconPackInfoPackageName,
                    fileManager = fileManager,
                    iconPackManager = iconPackManager,
                    fastLauncherAppsActivityInfos = launcherAppsWrapper.getFastActivityList(),
                    iconKeyGenerator = iconKeyGenerator,
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
}
