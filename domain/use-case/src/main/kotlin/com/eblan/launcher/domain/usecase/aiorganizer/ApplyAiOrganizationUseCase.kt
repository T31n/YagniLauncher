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
import com.eblan.launcher.domain.model.aiorganizer.AiOrganizerFile
import com.eblan.launcher.domain.model.aiorganizer.ApplyAiOrganizationResult
import com.eblan.launcher.domain.model.application.EblanApplicationInfoTag
import com.eblan.launcher.domain.model.application.EblanApplicationInfoTagCrossRef
import com.eblan.launcher.domain.repository.AiOrganizerRepository
import com.eblan.launcher.domain.repository.ApplicationInfoGridItemRepository
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.EblanApplicationInfoTagRepository
import com.eblan.launcher.domain.repository.GridRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Applies a generated organization, or restores the previous one.
 *
 * Applying snapshots the current organization to `last_home.json` before changing anything, then
 * deletes `new_home.json`. Restoring applies `last_home.json` and keeps it on disk.
 */
class ApplyAiOrganizationUseCase @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val gridRepository: GridRepository,
    private val eblanApplicationInfoRepository: EblanApplicationInfoRepository,
    private val eblanApplicationInfoTagRepository: EblanApplicationInfoTagRepository,
    private val applicationInfoGridItemRepository: ApplicationInfoGridItemRepository,
    private val aiOrganizerRepository: AiOrganizerRepository,
    @param:Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    /**
     * @param restore When true, applies `last_home.json`. Otherwise applies `new_home.json`.
     * Returns the outcome, including why the file could not be applied.
     */
    suspend operator fun invoke(restore: Boolean): ApplyAiOrganizationResult = withContext(defaultDispatcher) {
        try {
            apply(restore)
        } catch (exception: CancellationException) {
            throw exception
        } catch (_: Exception) {
            ApplyAiOrganizationResult.FAILED
        }
    }

    private suspend fun apply(restore: Boolean): ApplyAiOrganizationResult {
        val sourceFile = if (restore) AiOrganizerFile.LAST_HOME else AiOrganizerFile.NEW_HOME

        val content = aiOrganizerRepository.readFile(sourceFile)
            ?: return ApplyAiOrganizationResult.MISSING_FILE

        val userData = userDataRepository.userDataFlow.first()
        val homeSettings = userData.homeSettings

        val context = buildAiOrganizerHomeContext(
            columns = homeSettings.columns,
            rows = homeSettings.rows,
            gridItems = gridRepository.getGridItems(),
            apps = eblanApplicationInfoRepository.getEblanApplicationInfos(),
        )

        val home = try {
            parseAiOrganizerHome(content.decodeToString()).also { it.validate(context) }
        } catch (_: IllegalArgumentException) {
            return ApplyAiOrganizationResult.INVALID_FILE
        }

        if (!restore) {
            val tags = eblanApplicationInfoTagRepository.eblanApplicationInfoTagsFlow.first()

            val tagCrossRefs = tags.flatMap { tag ->
                eblanApplicationInfoRepository.getEblanApplicationInfosByTagId(tag.id).map {
                    EblanApplicationInfoTagCrossRef(
                        componentName = it.componentName,
                        serialNumber = it.serialNumber,
                        id = tag.id,
                    )
                }
            }

            val snapshot = context.toSnapshotDto(
                pageCount = homeSettings.pageCount,
                tags = tags,
                tagCrossRefs = tagCrossRefs,
            )

            aiOrganizerRepository.writeFile(
                file = AiOrganizerFile.LAST_HOME,
                content = aiOrganizerJson.encodeToString(snapshot).toByteArray(),
            )
        }

        val organizableItemsByComponentName = context.organizableItems.associateBy { it.componentName }

        home.gridItems.forEach { placed ->
            val item = organizableItemsByComponentName.getValue(placed.componentName)

            applicationInfoGridItemRepository.updateApplicationInfoGridItem(
                item.copy(
                    page = placed.page,
                    startColumn = placed.position % homeSettings.columns,
                    startRow = placed.position / homeSettings.columns,
                ),
            )
        }

        userDataRepository.updateHomeSettings(
            homeSettings.copy(pageCount = home.settings.pageCount),
        )

        eblanApplicationInfoTagRepository.replaceEblanApplicationInfoTags(
            eblanApplicationInfoTags = home.tags.map {
                EblanApplicationInfoTag(id = it.id, name = it.name, index = it.index)
            },
            eblanApplicationInfoTagCrossRefs = home.tagCrossRefs.map {
                EblanApplicationInfoTagCrossRef(
                    componentName = it.componentName,
                    serialNumber = it.serialNumber,
                    id = it.id,
                )
            },
        )

        if (!restore) {
            aiOrganizerRepository.deleteFile(AiOrganizerFile.NEW_HOME)
            return ApplyAiOrganizationResult.APPLIED
        }

        return ApplyAiOrganizationResult.RESTORED
    }
}
