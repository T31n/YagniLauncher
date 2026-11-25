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
import com.eblan.launcher.domain.model.application.EblanApplicationInfo
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.FolderEblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.FolderGridItemTransaction
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

/** Appends selected applications to an application-drawer folder. */
class AddEblanApplicationInfosToFolderUseCase @Inject constructor(
    private val eblanApplicationInfoRepository: EblanApplicationInfoRepository,
    private val folderEblanApplicationInfoRepository: FolderEblanApplicationInfoRepository,
    private val folderGridItemTransaction: FolderGridItemTransaction,
    @param:Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    /**
     * Assigns sequential folder indices after the existing items in [folderId]
     * and persists the updated application records.
     *
     * @param folderId The identifier of the target application-drawer folder.
     * @param eblanApplicationInfos The applications to append to the folder.
     */
    suspend operator fun invoke(
        folderId: String,
        eblanApplicationInfos: List<EblanApplicationInfo>,
    ) = withContext(defaultDispatcher) {
        folderGridItemTransaction.withTransaction {
            val maxApplicationIndex = eblanApplicationInfoRepository.getEblanApplicationInfos()
                .asSequence()
                .filter { currentEblanApplicationInfo ->
                    currentEblanApplicationInfo.folderId == folderId
                }
                .maxOfOrNull { currentEblanApplicationInfo ->
                    currentEblanApplicationInfo.folderIndex + 1
                } ?: 0

            val maxFolderIndex =
                folderEblanApplicationInfoRepository.getFolderEblanApplicationInfos()
                    .asSequence()
                    .filter { currentFolderEblanApplicationInfo ->
                        currentFolderEblanApplicationInfo.folderId == folderId
                    }
                    .maxOfOrNull { currentFolderEblanApplicationInfo ->
                        currentFolderEblanApplicationInfo.folderIndex + 1
                    } ?: 0

            val startIndex = maxOf(maxApplicationIndex, maxFolderIndex)

            eblanApplicationInfoRepository.updateEblanApplicationInfos(
                eblanApplicationInfos = eblanApplicationInfos.mapIndexed { index, currentEblanApplicationInfo ->
                    currentEblanApplicationInfo.copy(
                        folderIndex = startIndex + index,
                        folderId = folderId,
                    )
                },
            )
        }
    }
}
