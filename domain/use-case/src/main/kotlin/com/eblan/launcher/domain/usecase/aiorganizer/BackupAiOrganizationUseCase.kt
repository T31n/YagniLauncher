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
package com.eblan.launcher.domain.usecase.aiorganizer

import com.eblan.launcher.domain.common.Dispatcher
import com.eblan.launcher.domain.common.EblanDispatchers
import com.eblan.launcher.domain.common.FileManager
import com.eblan.launcher.domain.common.HomeOperationLock
import com.eblan.launcher.domain.model.aiorganizer.AiOrganizerFile
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.EblanApplicationInfoTagRepository
import com.eblan.launcher.domain.repository.FolderEblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.GridRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

/**
 * Saves the current home screen organization to `home.json`, replacing any earlier backup, so the
 * user can restore it later.
 */
class BackupAiOrganizationUseCase @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val gridRepository: GridRepository,
    private val eblanApplicationInfoRepository: EblanApplicationInfoRepository,
    private val eblanApplicationInfoTagRepository: EblanApplicationInfoTagRepository,
    private val folderEblanApplicationInfoRepository: FolderEblanApplicationInfoRepository,
    private val homeOperationLock: HomeOperationLock,
    private val fileManager: FileManager,
    @param:Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
    @param:Dispatcher(EblanDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke() = withContext(defaultDispatcher) {
        homeOperationLock.runExclusively {
            val homeSettings = userDataRepository.userDataFlow.first().homeSettings

            val context = buildAiOrganizerHomeContext(
                homeSettings = homeSettings,
                gridItems = gridRepository.getGridItems(),
                apps = eblanApplicationInfoRepository.getEblanApplicationInfos(),
            )

            val tags = eblanApplicationInfoTagRepository.eblanApplicationInfoTagsFlow.first()

            val homeSnapshotDto = context.toSnapshotDto(
                pageCount = homeSettings.pageCount,
                tags = tags,
                tagCrossRefs = eblanApplicationInfoRepository.getTagCrossRefs(tags),
                appDrawerFolders = folderEblanApplicationInfoRepository.getFolderEblanApplicationInfos(),
            )

            val homeSnapshotBytes = aiOrganizerJson.encodeToString(homeSnapshotDto).toByteArray()

            withContext(ioDispatcher) {
                writeHomeFile(
                    directory = fileManager.getFilesDirectory(FileManager.HOME_DIR),
                    name = AiOrganizerFile.HOME_BACKUP.fileName,
                    content = homeSnapshotBytes,
                )
            }
        }
    }
}
