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
package com.eblan.launcher.domain.usecase.grid

import com.eblan.launcher.domain.common.Dispatcher
import com.eblan.launcher.domain.common.EblanDispatchers
import com.eblan.launcher.domain.model.FolderGridItemPopup
import com.eblan.launcher.domain.model.FolderPopupEntry
import com.eblan.launcher.domain.repository.FolderGridItemRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class GetFolderGridItemsByIdUseCase @Inject constructor(
    private val folderGridItemRepository: FolderGridItemRepository,
    private val userDataRepository: UserDataRepository,
    @param:Dispatcher(EblanDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) {
    operator fun invoke(
        folderPopupEntriesFlow: Flow<List<FolderPopupEntry>>,
    ): Flow<List<FolderGridItemPopup>> = combine(
        userDataRepository.userDataFlow,
        folderPopupEntriesFlow,
        folderGridItemRepository.folderGridItemWrappersFlow,
    ) { userData, folderPopupEntries, folderGridItemWrappers ->
        folderPopupEntries.mapNotNull { folderPopupEntry ->
            folderGridItemWrappers.firstOrNull {
                it.folderGridItem.id == folderPopupEntry.id
            }?.asFolderPopup(
                folderGridItemRepository = folderGridItemRepository,
                folderPopupEntry = folderPopupEntry,
                maxFolderColumns = userData.homeSettings.maxFolderColumns,
                maxFolderRows = userData.homeSettings.maxFolderRows,
            )
        }
    }.flowOn(ioDispatcher)
}
