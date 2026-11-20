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
import com.eblan.launcher.domain.model.application.EblanApplicationInfoGroup
import com.eblan.launcher.domain.model.launcherapps.EblanUser
import com.eblan.launcher.domain.model.widget.EblanAppWidgetProviderInfo
import com.eblan.launcher.domain.platform.LauncherAppsWrapper
import com.eblan.launcher.domain.repository.EblanAppWidgetProviderInfoRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

/** Exposes widget providers whose application labels match a changing search query. */
class GetEblanAppWidgetProviderInfosByLabelUseCase @Inject constructor(
    private val eblanAppWidgetProviderInfoRepository: EblanAppWidgetProviderInfoRepository,
    private val launcherAppsWrapper: LauncherAppsWrapper,
    @param:Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    /**
     * Filters providers by a case-insensitive label substring, sorts them by user and label, and
     * groups them by user and application.
     *
     * @param labelFlow The flow of query strings to apply.
     * @return A flow of provider lists grouped by user and application information.
     */
    operator fun invoke(labelFlow: Flow<String>): Flow<Map<EblanUser, Map<EblanApplicationInfoGroup, List<EblanAppWidgetProviderInfo>>>> = combine(
        eblanAppWidgetProviderInfoRepository.eblanAppWidgetProviderInfosFlow,
        labelFlow,
    ) { eblanAppWidgetProviderInfos, label ->
        eblanAppWidgetProviderInfos.filter { currentEblanAppWidgetProviderInfo ->
            currentEblanAppWidgetProviderInfo.applicationLabel?.contains(
                other = label,
                ignoreCase = true,
            ) == true
        }.sortedWith(
            compareBy(
                { currentEblanAppWidgetProviderInfo -> currentEblanAppWidgetProviderInfo.serialNumber },
                { currentEblanAppWidgetProviderInfo -> currentEblanAppWidgetProviderInfo.applicationLabel?.lowercase() },
            ),
        ).groupBy { currentEblanAppWidgetProviderInfo ->
            launcherAppsWrapper.getUser(serialNumber = currentEblanAppWidgetProviderInfo.serialNumber)
        }.mapValues { entry ->
            entry.value.groupBy { currentEblanAppWidgetProviderInfo ->
                EblanApplicationInfoGroup(
                    serialNumber = currentEblanAppWidgetProviderInfo.serialNumber,
                    packageName = currentEblanAppWidgetProviderInfo.packageName,
                    icon = currentEblanAppWidgetProviderInfo.applicationIcon,
                    label = currentEblanAppWidgetProviderInfo.applicationLabel,
                )
            }
        }
    }.flowOn(defaultDispatcher)
}
