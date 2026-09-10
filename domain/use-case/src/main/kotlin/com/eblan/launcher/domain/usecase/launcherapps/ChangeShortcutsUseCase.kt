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
import com.eblan.launcher.domain.framework.LauncherAppsWrapper
import com.eblan.launcher.domain.framework.PackageManagerWrapper
import com.eblan.launcher.domain.repository.EblanShortcutInfoRepository
import com.eblan.launcher.domain.repository.ShortcutInfoGridItemRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ChangeShortcutsUseCase @Inject constructor(
    private val eblanShortcutInfoRepository: EblanShortcutInfoRepository,
    private val launcherAppsWrapper: LauncherAppsWrapper,
    private val shortcutInfoGridItemRepository: ShortcutInfoGridItemRepository,
    private val fileManager: FileManager,
    private val packageManagerWrapper: PackageManagerWrapper,
    private val iconKeyGenerator: IconKeyGenerator,
    @param:Dispatcher(EblanDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(
        serialNumber: Long,
        packageName: String,
    ) {
        if (!launcherAppsWrapper.hasShortcutHostPermission) {
            return
        }

        withContext(ioDispatcher) {
            val launcherAppsShortcutInfos = launcherAppsWrapper.getShortcutsByPackageNameWithCacheIcons(
                serialNumber = serialNumber,
                packageName = packageName,
            ) ?: return@withContext

            val oldEblanShortcutInfos = eblanShortcutInfoRepository.getEblanShortcutInfos(
                serialNumber = serialNumber,
                packageName = packageName,
            )

            val newEblanShortcutInfos = launcherAppsShortcutInfos.map {
                it.toEblanShortcutInfo()
            }

            val newDeleteEblanShortcutInfos = newEblanShortcutInfos.map {
                it.toDeleteEblanShortcutInfo()
            }.toSet()

            val oldDeleteEblanShortcutInfos = oldEblanShortcutInfos.map {
                it.toDeleteEblanShortcutInfo()
            }.filterNot {
                it in newDeleteEblanShortcutInfos
            }

            eblanShortcutInfoRepository.upsertEblanShortcutInfos(
                eblanShortcutInfos = newEblanShortcutInfos,
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
    }
}
