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
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfoPopup
import com.eblan.launcher.domain.repository.FolderEblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

/** Exposes application folders as preview popups sized by the current folder settings. */
class GetFolderEblanApplicationInfoPopupsUseCase @Inject constructor(
    private val folderEblanApplicationInfoRepository: FolderEblanApplicationInfoRepository,
    private val userDataRepository: UserDataRepository,
    @param:Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    /**
     * Emits folder popups ordered by folder label and arranged using the configured maximum size.
     *
     * @return A flow of folder popups indexed by folder ID.
     */
    operator fun invoke(): Flow<Map<String, FolderEblanApplicationInfoPopup>> = combine(
        userDataRepository.userDataFlow,
        folderEblanApplicationInfoRepository.folderEblanApplicationInfoWrappersFlow,
    ) { userData, folderEblanApplicationInfoWrappers ->
        folderEblanApplicationInfoWrappers
            .sortedBy { it.folderEblanApplicationInfo.label }
            .asPreviewFolderEblanApplicationInfos(
                maxFolderColumns = userData.folderSettings.maxFolderColumns,
                maxFolderRows = userData.folderSettings.maxFolderRows,
            )
    }.flowOn(defaultDispatcher)
}
