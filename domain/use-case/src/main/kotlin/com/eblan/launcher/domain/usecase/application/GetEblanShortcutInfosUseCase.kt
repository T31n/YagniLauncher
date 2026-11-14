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
package com.eblan.launcher.domain.usecase.application

import com.eblan.launcher.domain.common.Dispatcher
import com.eblan.launcher.domain.common.EblanDispatchers
import com.eblan.launcher.domain.framework.ShortcutManagerWrapper
import com.eblan.launcher.domain.model.launcherapps.ShortcutQueryFlag
import com.eblan.launcher.domain.model.shortcutinfo.EblanShortcutInfo
import com.eblan.launcher.domain.model.shortcutinfo.EblanShortcutInfoByGroup
import com.eblan.launcher.domain.repository.EblanShortcutInfoRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/** Provides launcher shortcuts grouped by their user and owning package. */
class GetEblanShortcutInfosUseCase @Inject constructor(
    private val eblanShortcutInfoRepository: EblanShortcutInfoRepository,
    private val shortcutManagerWrapper: ShortcutManagerWrapper,
    @param:Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    /**
     * Emits shortcuts grouped by user and package, ordered by shortcut type and rank, and limited to the device
     * maximum. Pinned-only shortcuts are excluded.
     *
     * @return A flow of shortcut groups and their shortcut lists.
     */
    operator fun invoke(): Flow<Map<EblanShortcutInfoByGroup, List<EblanShortcutInfo>>> = eblanShortcutInfoRepository.eblanShortcutInfosFlow.map { eblanShortcutInfos ->
        val maxShortcutCountPerApp = shortcutManagerWrapper.getMaxShortcutCountPerActivity()
        println(maxShortcutCountPerApp)
        eblanShortcutInfos
            .filter {
                it.shortcutQueryFlag != ShortcutQueryFlag.Pinned
            }
            .groupBy { eblanShortcutInfo ->
                EblanShortcutInfoByGroup(
                    serialNumber = eblanShortcutInfo.serialNumber,
                    packageName = eblanShortcutInfo.packageName,
                )
            }
            .mapValues { (_, groupedEblanShortcutInfos) ->
                val shortcutOrder = compareBy<EblanShortcutInfo> {
                    it.shortcutQueryFlag != ShortcutQueryFlag.Manifest
                }.thenBy {
                    it.rank
                }

                groupedEblanShortcutInfos
                    .sortedWith(shortcutOrder)
                    .take(maxShortcutCountPerApp)
            }
    }.flowOn(defaultDispatcher)
}
