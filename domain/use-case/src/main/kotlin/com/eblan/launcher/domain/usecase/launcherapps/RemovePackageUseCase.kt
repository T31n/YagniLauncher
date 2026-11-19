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
import com.eblan.launcher.domain.repository.ApplicationInfoGridItemRepository
import com.eblan.launcher.domain.repository.EblanAppWidgetProviderInfoRepository
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.EblanShortcutConfigRepository
import com.eblan.launcher.domain.repository.EblanShortcutInfoRepository
import com.eblan.launcher.domain.repository.ShortcutConfigGridItemRepository
import com.eblan.launcher.domain.repository.ShortcutInfoGridItemRepository
import com.eblan.launcher.domain.repository.WidgetGridItemRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

/** Removes a package's stored components and grid items. */
class RemovePackageUseCase @Inject constructor(
    private val eblanApplicationInfoRepository: EblanApplicationInfoRepository,
    private val eblanAppWidgetProviderInfoRepository: EblanAppWidgetProviderInfoRepository,
    private val applicationInfoGridItemRepository: ApplicationInfoGridItemRepository,
    private val widgetGridItemRepository: WidgetGridItemRepository,
    private val shortcutInfoGridItemRepository: ShortcutInfoGridItemRepository,
    private val eblanShortcutInfoRepository: EblanShortcutInfoRepository,
    private val eblanShortcutConfigRepository: EblanShortcutConfigRepository,
    private val shortcutConfigGridItemRepository: ShortcutConfigGridItemRepository,
    @param:Dispatcher(EblanDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) {
    /**
     * Deletes package-related repository records for the specified profile.
     *
     * @param serialNumber The user profile where the package was installed.
     * @param packageName The package to remove.
     */
    suspend operator fun invoke(
        serialNumber: Long,
        packageName: String,
    ) {
        withContext(ioDispatcher) {
            eblanApplicationInfoRepository.deleteEblanApplicationInfoByPackageName(
                serialNumber = serialNumber,
                packageName = packageName,
            )

            eblanAppWidgetProviderInfoRepository.deleteEblanAppWidgetProviderInfoByPackageName(
                serialNumber = serialNumber,
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
}
