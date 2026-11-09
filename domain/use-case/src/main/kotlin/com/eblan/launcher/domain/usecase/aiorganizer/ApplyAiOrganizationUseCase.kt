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
import com.eblan.launcher.domain.framework.AppWidgetHostWrapper
import com.eblan.launcher.domain.framework.LauncherAppsWrapper
import com.eblan.launcher.domain.model.aiorganizer.AiOrganizerFile
import com.eblan.launcher.domain.model.aiorganizer.ApplyAiOrganizationResult
import com.eblan.launcher.domain.model.application.EblanApplicationInfo
import com.eblan.launcher.domain.model.application.EblanApplicationInfoTag
import com.eblan.launcher.domain.model.application.EblanApplicationInfoTagCrossRef
import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.domain.model.userdata.HomeSettings
import com.eblan.launcher.domain.repository.AiOrganizerRepository
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.EblanApplicationInfoTagRepository
import com.eblan.launcher.domain.repository.GridRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import com.eblan.launcher.domain.usecase.util.deleteGridItemData
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Applies a generated organization, or restores the previous one.
 *
 * Applying snapshots the current organization to `last_home.json` before changing anything, then
 * deletes `new_home.json`. Restoring applies `last_home.json` and keeps it on disk.
 *
 * Writes to the home are not transactional across Room and DataStore, so a failed apply rolls back
 * the in-memory previous state on a best-effort basis. Applies and restores are serialized so a
 * second run cannot snapshot a partially applied home.
 */
class ApplyAiOrganizationUseCase @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val gridRepository: GridRepository,
    private val eblanApplicationInfoRepository: EblanApplicationInfoRepository,
    private val eblanApplicationInfoTagRepository: EblanApplicationInfoTagRepository,
    private val appWidgetHostWrapper: AppWidgetHostWrapper,
    private val launcherAppsWrapper: LauncherAppsWrapper,
    private val aiOrganizerRepository: AiOrganizerRepository,
    @param:Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    /**
     * @param restore When true, applies `last_home.json`. Otherwise applies `new_home.json`.
     * Returns the outcome, including why the file could not be applied.
     */
    suspend operator fun invoke(restore: Boolean): ApplyAiOrganizationResult = withContext(defaultDispatcher) {
        applyMutex.withLock {
            try {
                apply(restore)
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                ApplyAiOrganizationResult.FAILED
            }
        }
    }

    private suspend fun apply(restore: Boolean): ApplyAiOrganizationResult {
        val sourceFile = if (restore) AiOrganizerFile.LAST_HOME else AiOrganizerFile.NEW_HOME

        val content = aiOrganizerRepository.readFile(sourceFile)
            ?: return ApplyAiOrganizationResult.MISSING_FILE

        val userData = userDataRepository.userDataFlow.first()
        val homeSettings = userData.homeSettings

        val context = buildAiOrganizerHomeContext(
            homeSettings = homeSettings,
            gridItems = gridRepository.getGridItems(),
            apps = eblanApplicationInfoRepository.getEblanApplicationInfos(),
        )

        val (home, finalGridItems) = try {
            val parsed = parseAiOrganizerHome(content.decodeToString())
            val document = if (restore) parsed.withoutUnrecoverableItems(context) else parsed

            document to document.validate(context)
        } catch (_: IllegalArgumentException) {
            return ApplyAiOrganizationResult.INVALID_FILE
        }

        val previousTags = eblanApplicationInfoTagRepository.eblanApplicationInfoTagsFlow.first()
        val previousTagCrossRefs = getTagCrossRefs(previousTags)

        if (!restore) {
            val snapshot = context.toSnapshotDto(
                pageCount = homeSettings.pageCount,
                tags = previousTags,
                tagCrossRefs = previousTagCrossRefs,
            )

            aiOrganizerRepository.writeFile(
                file = AiOrganizerFile.LAST_HOME,
                content = aiOrganizerJson.encodeToString(snapshot).toByteArray(),
            )
        }

        val previousGridItems = context.gridItems
        val finalIds = finalGridItems.map { it.id }.toSet()
        val previousIds = previousGridItems.map { it.id }.toSet()
        val omittedGridItems = previousGridItems.filter { it.id !in finalIds }
        val createdGridItems = finalGridItems.filter { it.id !in previousIds }

        try {
            gridRepository.upsertGridItems(finalGridItems)

            omittedGridItems.forEach {
                deleteGridItemData(
                    gridItem = it,
                    folderPopupGridItems = emptyMap(),
                    appWidgetHostWrapper = appWidgetHostWrapper,
                    launcherAppsWrapper = launcherAppsWrapper,
                )
            }

            gridRepository.deleteGridItems(omittedGridItems)

            userDataRepository.updateHomeSettings(
                homeSettings.copy(pageCount = home.settings.pageCount),
            )

            val (tags, tagCrossRefs) = withHiddenAppTags(
                hiddenApps = context.apps.filter { it.isHidden },
                proposedTags = home.tags.map {
                    EblanApplicationInfoTag(id = it.id, name = it.name, index = it.index)
                },
                proposedTagCrossRefs = home.tagCrossRefs.map {
                    EblanApplicationInfoTagCrossRef(
                        componentName = it.componentName,
                        serialNumber = it.serialNumber,
                        id = it.id,
                    )
                },
                previousTags = previousTags,
                previousTagCrossRefs = previousTagCrossRefs,
            )

            eblanApplicationInfoTagRepository.replaceEblanApplicationInfoTags(
                eblanApplicationInfoTags = tags,
                eblanApplicationInfoTagCrossRefs = tagCrossRefs,
            )
        } catch (exception: Exception) {
            rollback(
                previousGridItems = previousGridItems,
                createdGridItems = createdGridItems,
                previousHomeSettings = homeSettings,
                previousTags = previousTags,
                previousTagCrossRefs = previousTagCrossRefs,
            )

            throw exception
        }

        if (!restore) {
            aiOrganizerRepository.deleteFile(AiOrganizerFile.NEW_HOME)
            return ApplyAiOrganizationResult.APPLIED
        }

        return ApplyAiOrganizationResult.RESTORED
    }

    private suspend fun getTagCrossRefs(
        tags: List<EblanApplicationInfoTag>,
    ): List<EblanApplicationInfoTagCrossRef> = tags.flatMap { tag ->
        eblanApplicationInfoRepository.getEblanApplicationInfosByTagId(tag.id).map {
            EblanApplicationInfoTagCrossRef(
                componentName = it.componentName,
                serialNumber = it.serialNumber,
                id = tag.id,
            )
        }
    }

    /**
     * The proposal omits hidden apps, so their existing tag memberships are carried over. A
     * preserved tag whose id collides with a proposed tag is given a fresh id, and its memberships
     * are rewritten to match.
     */
    private fun withHiddenAppTags(
        hiddenApps: List<EblanApplicationInfo>,
        proposedTags: List<EblanApplicationInfoTag>,
        proposedTagCrossRefs: List<EblanApplicationInfoTagCrossRef>,
        previousTags: List<EblanApplicationInfoTag>,
        previousTagCrossRefs: List<EblanApplicationInfoTagCrossRef>,
    ): Pair<List<EblanApplicationInfoTag>, List<EblanApplicationInfoTagCrossRef>> {
        val hiddenKeys = hiddenApps.map { it.componentName to it.serialNumber }.toSet()

        val preservedCrossRefs = previousTagCrossRefs.filter {
            (it.componentName to it.serialNumber) in hiddenKeys
        }

        if (preservedCrossRefs.isEmpty()) {
            return proposedTags to proposedTagCrossRefs
        }

        val proposedTagIds = proposedTags.map { it.id }.toSet()
        var nextId = (proposedTagIds + previousTags.map { it.id }).maxOrNull() ?: 0L
        val remappedIds = mutableMapOf<Long, Long>()

        val preservedTagIds = preservedCrossRefs.map { it.id }.toSet()

        val preservedTags = previousTags.filter { it.id in preservedTagIds }.map { tag ->
            if (tag.id in proposedTagIds) {
                nextId += 1
                remappedIds[tag.id] = nextId
                tag.copy(id = nextId)
            } else {
                tag
            }
        }

        val tags = proposedTags + preservedTags

        val crossRefs = proposedTagCrossRefs.filter {
            (it.componentName to it.serialNumber) !in hiddenKeys
        } + preservedCrossRefs.map {
            it.copy(id = remappedIds[it.id] ?: it.id)
        }

        return tags to crossRefs
    }

    /**
     * Best-effort restore of the state captured before a failed apply. Runs non-cancellable so a
     * cancelled apply still attempts to roll back. Each step is isolated so one failure does not
     * skip the rest.
     */
    private suspend fun rollback(
        previousGridItems: List<GridItem>,
        createdGridItems: List<GridItem>,
        previousHomeSettings: HomeSettings,
        previousTags: List<EblanApplicationInfoTag>,
        previousTagCrossRefs: List<EblanApplicationInfoTagCrossRef>,
    ) = withContext(NonCancellable) {
        if (createdGridItems.isNotEmpty()) {
            runCatching { gridRepository.deleteGridItems(createdGridItems) }
        }

        runCatching { gridRepository.upsertGridItems(previousGridItems) }

        runCatching { userDataRepository.updateHomeSettings(previousHomeSettings) }

        runCatching {
            eblanApplicationInfoTagRepository.replaceEblanApplicationInfoTags(
                eblanApplicationInfoTags = previousTags,
                eblanApplicationInfoTagCrossRefs = previousTagCrossRefs,
            )
        }
    }

    private companion object {
        /** Shared across instances so concurrent callers cannot interleave applies. */
        val applyMutex = Mutex()
    }
}
