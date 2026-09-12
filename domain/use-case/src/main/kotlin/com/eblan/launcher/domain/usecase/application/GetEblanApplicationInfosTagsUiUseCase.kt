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
import com.eblan.launcher.domain.model.application.EblanApplicationInfoTagUi
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.EblanApplicationInfoTagRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class GetEblanApplicationInfosTagsUiUseCase @Inject constructor(
    private val eblanApplicationInfoRepository: EblanApplicationInfoRepository,
    private val eblanApplicationInfoTagRepository: EblanApplicationInfoTagRepository,
    @param:Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    operator fun invoke(
        serialNumber: Long,
        componentName: String,
    ): Flow<List<EblanApplicationInfoTagUi>> = combine(
        eblanApplicationInfoRepository.getEblanApplicationInfoTagsFlow(
            serialNumber = serialNumber,
            componentName = componentName,
        ),
        eblanApplicationInfoTagRepository.eblanApplicationInfoTagsFlow,
    ) { eblanApplicationInfoTagsByComponentName, eblanApplicationInfoTags ->
        eblanApplicationInfoTags.map {
            EblanApplicationInfoTagUi(
                id = it.id,
                name = it.name,
                selected = it in eblanApplicationInfoTagsByComponentName,
            )
        }.sortedBy {
            it.id
        }
    }.flowOn(defaultDispatcher)
}
