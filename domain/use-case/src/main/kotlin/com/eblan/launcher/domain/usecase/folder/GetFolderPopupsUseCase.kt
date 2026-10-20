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
package com.eblan.launcher.domain.usecase.folder

import com.eblan.launcher.domain.common.Dispatcher
import com.eblan.launcher.domain.common.EblanDispatchers
import com.eblan.launcher.domain.model.folder.FolderPopup
import com.eblan.launcher.domain.repository.FolderGridItemRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

/** Exposes grid folders as preview popups arranged using the current folder settings. */
class GetFolderPopupsUseCase @Inject constructor(
    private val folderGridItemRepository: FolderGridItemRepository,
    private val userDataRepository: UserDataRepository,
    @param:Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    /**
     * Emits folder popups built from the latest folder data and maximum row and column settings.
     *
     * @return A flow of folder popups indexed by folder ID.
     */
    operator fun invoke(): Flow<Map<String, FolderPopup>> = combine(
        userDataRepository.userDataFlow,
        folderGridItemRepository.folderGridItemWrappersFlow,
    ) { userData, folderGridItemWrappers ->
        folderGridItemWrappers.asPreviewFolders(
            maxFolderColumns = userData.folderSettings.maxFolderColumns,
            maxFolderRows = userData.folderSettings.maxFolderRows,
        )
    }.flowOn(defaultDispatcher)
}
