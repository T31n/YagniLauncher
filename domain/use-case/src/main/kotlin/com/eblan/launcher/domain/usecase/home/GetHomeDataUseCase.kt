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
package com.eblan.launcher.domain.usecase.home

import com.eblan.launcher.domain.common.Dispatcher
import com.eblan.launcher.domain.common.EblanDispatchers
import com.eblan.launcher.domain.common.IconKeyGenerator
import com.eblan.launcher.domain.framework.FileManager
import com.eblan.launcher.domain.framework.LauncherAppsWrapper
import com.eblan.launcher.domain.framework.PackageManagerWrapper
import com.eblan.launcher.domain.grid.isGridItemSpanWithinBounds
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.HomeData
import com.eblan.launcher.domain.repository.GridRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import com.eblan.launcher.domain.usecase.util.getIconPackInfoFilePaths
import com.eblan.launcher.domain.usecase.util.isTopLevel
import com.eblan.launcher.domain.usecase.util.toGridItems
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class GetHomeDataUseCase @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val launcherAppsWrapper: LauncherAppsWrapper,
    private val packageManagerWrapper: PackageManagerWrapper,
    private val gridRepository: GridRepository,
    private val fileManager: FileManager,
    private val iconKeyGenerator: IconKeyGenerator,
    @param:Dispatcher(EblanDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) {
    operator fun invoke(): Flow<HomeData> = combine(
        userDataRepository.userDataFlow,
        gridRepository.gridItemsFlow,
    ) { userData, gridItems ->
        val currentGridItems = gridItems.toGridItems().filter { it.isTopLevel() }

        val gridItemsByPage = currentGridItems.filter {
            isGridItemSpanWithinBounds(
                gridItem = it,
                columns = userData.homeSettings.columns,
                rows = userData.homeSettings.rows,
            ) && it.associate == Associate.Grid
        }.groupBy { it.page }

        val dockGridItemsByPage = currentGridItems.filter {
            isGridItemSpanWithinBounds(
                gridItem = it,
                columns = userData.homeSettings.dockColumns,
                rows = userData.homeSettings.dockRows,
            ) && it.associate == Associate.Dock
        }.groupBy { it.page }

        val iconPackInfoFilePaths = getIconPackInfoFilePaths(
            iconPackInfoPackageName = userData.generalSettings.iconPackInfoPackageName,
            componentNames = gridItems.applicationInfoGridItems.map { it.componentName },
            fileManager = fileManager,
            iconKeyGenerator = iconKeyGenerator,
        )

        HomeData(
            userData = userData,
            gridItems = currentGridItems,
            gridItemsByPage = gridItemsByPage,
            dockGridItemsByPage = dockGridItemsByPage,
            hasShortcutHostPermission = launcherAppsWrapper.hasShortcutHostPermission,
            hasSystemFeatureAppWidgets = packageManagerWrapper.hasSystemFeatureAppWidgets,
            iconPackInfoFilePaths = iconPackInfoFilePaths,
        )
    }.flowOn(ioDispatcher)
}
