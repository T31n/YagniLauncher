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
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfo
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.FolderEblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.GridRepository
import com.eblan.launcher.domain.usecase.util.deleteCustomIconsIfUnreferenced
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

/** Removes a folder's saved icon file and clears its icon reference. */
class DeleteFolderEblanApplicationInfoCustomIconUseCase @Inject constructor(
    private val eblanApplicationInfoRepository: EblanApplicationInfoRepository,
    private val folderEblanApplicationInfoRepository: FolderEblanApplicationInfoRepository,
    private val gridRepository: GridRepository,
    @param:Dispatcher(EblanDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) {
    /**
     * Persists the folder without an icon and deletes the current folder icon file if unreferenced.
     *
     * @param folderEblanApplicationInfo The folder whose icon should be removed.
     */
    suspend operator fun invoke(folderEblanApplicationInfo: FolderEblanApplicationInfo) = withContext(ioDispatcher) {
        folderEblanApplicationInfoRepository.upsertFolderEblanApplicationInfo(
            folderEblanApplicationInfo = folderEblanApplicationInfo.copy(icon = null),
        )

        deleteCustomIconsIfUnreferenced(
            iconPaths = listOf(folderEblanApplicationInfo.icon),
            eblanApplicationInfoRepository = eblanApplicationInfoRepository,
            folderEblanApplicationInfoRepository = folderEblanApplicationInfoRepository,
            gridRepository = gridRepository,
        )
    }
}
