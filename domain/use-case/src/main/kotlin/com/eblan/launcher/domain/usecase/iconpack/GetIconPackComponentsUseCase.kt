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
import com.eblan.launcher.domain.model.iconpackinfo.IconPackComponent
import com.eblan.launcher.domain.platform.IconPackManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/** Loads and filters icon pack components for a changing icon pack package name and query. */
class GetIconPackComponentsUseCase @Inject constructor(
    private val iconPackManager: IconPackManager,
    @param:Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    /**
     * Emits deduplicated [IconPackComponent]s for the latest package name in [packageNameFlow],
     * filtered by case-insensitive component-name substring from [queryFlow].
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(
        packageNameFlow: Flow<String?>,
        queryFlow: Flow<String>,
    ): Flow<List<IconPackComponent>> = packageNameFlow
        .flatMapLatest { packageName ->
            if (packageName == null) {
                flowOf(emptyList())
            } else {
                val iconPackComponents =
                    iconPackManager.getIconPackComponents(packageName)
                        .distinctBy { iconPackComponent ->
                            iconPackComponent.drawableName
                        }

                queryFlow.map { query ->
                    if (query.isEmpty()) {
                        iconPackComponents
                    } else {
                        iconPackComponents.filter { iconPackComponent ->
                            iconPackComponent.componentName.contains(
                                other = query,
                                ignoreCase = true,
                            )
                        }
                    }
                }
            }
        }.flowOn(defaultDispatcher)
}
