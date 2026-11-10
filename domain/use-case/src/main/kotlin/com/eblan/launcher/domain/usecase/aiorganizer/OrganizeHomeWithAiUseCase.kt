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
import com.eblan.launcher.domain.model.aiorganizer.OrganizeHomeWithAiResult
import com.eblan.launcher.domain.repository.AiOrganizerRepository
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.GridRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

/**
 * Sends the current home screen and app drawer to the configured AI endpoint and caches the
 * validated result as `new_home.json`. Nothing is applied until the user chooses to apply it.
 */
class OrganizeHomeWithAiUseCase @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val gridRepository: GridRepository,
    private val eblanApplicationInfoRepository: EblanApplicationInfoRepository,
    private val aiOrganizerRepository: AiOrganizerRepository,
    private val homeOperationLock: HomeOperationLock,
    private val fileManager: FileManager,
    @param:Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
    @param:Dispatcher(EblanDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) {
    /**
     * Returns [OrganizeHomeWithAiResult.Ready] once `new_home.json` is written. Other results describe why it was not.
     */
    suspend operator fun invoke(
        systemInstructions: String,
    ): OrganizeHomeWithAiResult = withContext(defaultDispatcher) {
        val userData = userDataRepository.userDataFlow.first()
        val settings = userData.aiOrganizerSettings

        if (settings.endpoint.isBlank()) return@withContext OrganizeHomeWithAiResult.EndpointNotSet
        if (settings.model.isBlank()) return@withContext OrganizeHomeWithAiResult.ModelNotSet

        val homeSettings = userData.homeSettings

        // The snapshot must not be read while Apply or Backup is rewriting the home.
        val context = homeOperationLock.runExclusively {
            buildAiOrganizerHomeContext(
                homeSettings = homeSettings,
                gridItems = gridRepository.getGridItems(),
                apps = eblanApplicationInfoRepository.getEblanApplicationInfos(),
            )
        }

        val requestJson = aiOrganizerJson.encodeToString(
            context.toRequestDto(pageCount = homeSettings.pageCount),
        )

        val response = try {
            aiOrganizerRepository.complete(
                settings = settings,
                systemPrompt = systemInstructions,
                userPrompt = requestJson,
            )
        } catch (exception: IOException) {
            return@withContext OrganizeHomeWithAiResult.RequestFailed(
                detail = exception.message ?: exception.javaClass.simpleName,
            )
        }

        val home = try {
            parseAiOrganizerHome(response).also { it.validate(context) }
        } catch (exception: Exception) {
            return@withContext OrganizeHomeWithAiResult.InvalidResponse(
                detail = exception.message ?: exception.javaClass.simpleName,
            )
        }

        val homeFileBytes = aiOrganizerJson.encodeToString(home).toByteArray()

        homeOperationLock.runExclusively {
            withContext(ioDispatcher) {
                writeHomeFile(
                    directory = fileManager.getFilesDirectory(FileManager.HOME_DIR),
                    name = AiOrganizerFile.NEW_HOME.fileName,
                    content = homeFileBytes,
                )
            }
        }

        OrganizeHomeWithAiResult.Ready
    }
}
