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
package com.eblan.launcher.domain.usecase.page

import com.eblan.launcher.domain.common.Dispatcher
import com.eblan.launcher.domain.common.EblanDispatchers
import com.eblan.launcher.domain.grid.isGridItemSpanWithinBounds
import com.eblan.launcher.domain.model.PageItem
import com.eblan.launcher.domain.model.grid.Associate
import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.domain.repository.UserDataRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CachePageItemsUseCase @Inject constructor(
    private val userDataRepository: UserDataRepository,
    @param:Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(
        gridItems: List<GridItem>,
        associate: Associate,
    ): List<PageItem> = withContext(defaultDispatcher) {
        val userData = userDataRepository.userDataFlow.first()

        val columns = when (associate) {
            Associate.Grid -> userData.homeSettings.columns
            Associate.Dock -> userData.homeSettings.dockColumns
        }

        val rows = when (associate) {
            Associate.Grid -> userData.homeSettings.rows
            Associate.Dock -> userData.homeSettings.dockRows
        }

        val pageCount = when (associate) {
            Associate.Grid -> userData.homeSettings.pageCount
            Associate.Dock -> userData.homeSettings.dockPageCount
        }

        val gridItemsByPage = gridItems.filter {
            isGridItemSpanWithinBounds(
                gridItem = it,
                columns = columns,
                rows = rows,
            ) && it.associate == associate
        }.groupBy { it.page }

        (0 until pageCount).map {
            PageItem(
                id = it,
                gridItems = gridItemsByPage[it].orEmpty(),
            )
        }
    }
}
