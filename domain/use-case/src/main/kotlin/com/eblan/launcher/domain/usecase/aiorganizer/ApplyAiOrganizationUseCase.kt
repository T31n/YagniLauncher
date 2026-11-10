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
import com.eblan.launcher.domain.framework.AppWidgetHostWrapper
import com.eblan.launcher.domain.framework.LauncherAppsWrapper
import com.eblan.launcher.domain.model.aiorganizer.AiOrganizerFile
import com.eblan.launcher.domain.model.aiorganizer.ApplyAiOrganizationResult
import com.eblan.launcher.domain.model.application.EblanApplicationInfo
import com.eblan.launcher.domain.model.application.EblanApplicationInfoTag
import com.eblan.launcher.domain.model.application.EblanApplicationInfoTagCrossRef
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfo
import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.domain.model.userdata.HomeSettings
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.EblanApplicationInfoTagRepository
import com.eblan.launcher.domain.repository.FolderEblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.GridRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import com.eblan.launcher.domain.usecase.util.deleteGridItemData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Applies a generated organization, or restores the manual backup.
 *
 * Applying deletes `new_home.json` after the new organization is in place. Restoring applies
 * `home.json` and keeps it on disk. Backups are created separately by [BackupAiOrganizationUseCase].
 *
 * Writes to the home are not transactional across Room and DataStore, so a failed apply rolls back
 * the in-memory previous state on a best-effort basis. Applies and backups are serialized so a
 * backup cannot snapshot a partially applied home.
 */
class ApplyAiOrganizationUseCase @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val gridRepository: GridRepository,
    private val eblanApplicationInfoRepository: EblanApplicationInfoRepository,
    private val eblanApplicationInfoTagRepository: EblanApplicationInfoTagRepository,
    private val folderEblanApplicationInfoRepository: FolderEblanApplicationInfoRepository,
    private val appWidgetHostWrapper: AppWidgetHostWrapper,
    private val launcherAppsWrapper: LauncherAppsWrapper,
    private val homeOperationLock: HomeOperationLock,
    private val fileManager: FileManager,
    @param:Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    /**
     * @param restore When true, applies `home.json`. Otherwise applies `new_home.json`.
     * Returns the outcome, including why the file could not be applied.
     */
    suspend operator fun invoke(restore: Boolean): ApplyAiOrganizationResult = withContext(defaultDispatcher) {
        homeOperationLock.runExclusively {
            val homeDirectory = fileManager.getFilesDirectory(FileManager.HOME_DIR)
            val sourceFile = if (restore) AiOrganizerFile.HOME_BACKUP else AiOrganizerFile.NEW_HOME

            val content = fileManager.readFileBytes(
                directory = homeDirectory,
                name = sourceFile.fileName,
            ) ?: return@runExclusively ApplyAiOrganizationResult.MISSING_FILE

            val homeSettings = userDataRepository.userDataFlow.first().homeSettings

            val context = buildAiOrganizerHomeContext(
                homeSettings = homeSettings,
                gridItems = gridRepository.getGridItems(),
                apps = eblanApplicationInfoRepository.getEblanApplicationInfos(),
            )

            val (home, finalGridItems) = runCatching {
                val parsed = parseAiOrganizerHome(content.decodeToString())
                val document = (if (restore) parsed.withoutUnrecoverableItems(context) else parsed)
                    .withoutEmptyAppDrawerFolders()

                document to document.validate(context)
            }.getOrElse { return@runExclusively ApplyAiOrganizationResult.INVALID_FILE }

            val previousTags = eblanApplicationInfoTagRepository.eblanApplicationInfoTagsFlow.first()
            val previousTagCrossRefs = eblanApplicationInfoRepository.getTagCrossRefs(previousTags)
            val previousFolders = folderEblanApplicationInfoRepository.getFolderEblanApplicationInfos()

            replaceHome(
                home = home,
                finalGridItems = finalGridItems,
                context = context,
                previousHomeSettings = homeSettings,
                previousTags = previousTags,
                previousTagCrossRefs = previousTagCrossRefs,
                previousFolders = previousFolders,
                appDrawerPlan = planAppDrawerFolders(home, context, previousFolders),
            )

            if (restore) {
                ApplyAiOrganizationResult.RESTORED
            } else {
                File(homeDirectory, AiOrganizerFile.NEW_HOME.fileName).delete()

                ApplyAiOrganizationResult.APPLIED
            }
        }
    }

    /**
     * Writes the validated organization. If any step fails, the previous state is restored.
     */
    private suspend fun replaceHome(
        home: AiOrganizerHomeDto,
        finalGridItems: List<GridItem>,
        context: AiOrganizerHomeContext,
        previousHomeSettings: HomeSettings,
        previousTags: List<EblanApplicationInfoTag>,
        previousTagCrossRefs: List<EblanApplicationInfoTagCrossRef>,
        previousFolders: List<FolderEblanApplicationInfo>,
        appDrawerPlan: AppDrawerPlan,
    ) {
        val previousGridItems = context.gridItems
        val finalIds = finalGridItems.map { it.id }.toSet()
        val previousIds = previousGridItems.map { it.id }.toSet()
        val omittedGridItems = previousGridItems.filter { it.id !in finalIds }
        val createdGridItems = finalGridItems.filter { it.id !in previousIds }

        var applied = false

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
                previousHomeSettings.copy(pageCount = home.settings.pageCount),
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

            eblanApplicationInfoRepository.updateEblanApplicationInfos(appDrawerPlan.clearedApps)

            appDrawerPlan.removedFolders.forEach {
                folderEblanApplicationInfoRepository.deleteFolderEblanApplicationInfoById(it.id)
            }

            appDrawerPlan.newFolders.forEach {
                folderEblanApplicationInfoRepository.upsertFolderEblanApplicationInfo(it)
            }

            eblanApplicationInfoRepository.updateEblanApplicationInfos(appDrawerPlan.assignedApps)

            applied = true
        } finally {
            if (!applied) {
                rollback(
                    previousGridItems = previousGridItems,
                    createdGridItems = createdGridItems,
                    previousHomeSettings = previousHomeSettings,
                    previousTags = previousTags,
                    previousTagCrossRefs = previousTagCrossRefs,
                    previousApps = context.apps,
                    previousFolders = previousFolders,
                    createdFolders = appDrawerPlan.newFolders,
                )
            }
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
        previousApps: List<EblanApplicationInfo>,
        previousFolders: List<FolderEblanApplicationInfo>,
        createdFolders: List<FolderEblanApplicationInfo>,
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

        // Visible apps are unlinked first, so deleting the created folders does not cascade to them.
        runCatching {
            eblanApplicationInfoRepository.updateEblanApplicationInfos(
                previousApps.filter { !it.isHidden }.map { it.copy(folderId = null, folderIndex = -1) },
            )
        }

        createdFolders.forEach {
            runCatching { folderEblanApplicationInfoRepository.deleteFolderEblanApplicationInfoById(it.id) }
        }

        previousFolders.forEach {
            runCatching { folderEblanApplicationInfoRepository.upsertFolderEblanApplicationInfo(it) }
        }

        runCatching { eblanApplicationInfoRepository.updateEblanApplicationInfos(previousApps) }
    }
}

internal class AppDrawerPlan(
    val clearedApps: List<EblanApplicationInfo>,
    val removedFolders: List<FolderEblanApplicationInfo>,
    val newFolders: List<FolderEblanApplicationInfo>,
    val assignedApps: List<EblanApplicationInfo>,
)

/**
 * Plans the app drawer folder changes. Folders still used by hidden apps are kept, because hidden
 * apps are not part of the proposal.
 */
@OptIn(ExperimentalUuidApi::class)
internal fun planAppDrawerFolders(
    home: AiOrganizerHomeDto,
    context: AiOrganizerHomeContext,
    previousFolders: List<FolderEblanApplicationInfo>,
): AppDrawerPlan {
    val hiddenFolderIds = context.apps.filter { it.isHidden }.mapNotNull { it.folderId }.toSet()
    val keptMaxIndex = previousFolders.filter { it.id in hiddenFolderIds }.maxOfOrNull { it.index } ?: -1
    val newFolderIds = home.appDrawerFolders.associate { it.id to Uuid.random().toHexString() }
    val appsByKey = context.apps.associateBy { it.componentName to it.serialNumber }

    return AppDrawerPlan(
        clearedApps = context.apps
            .filter { !it.isHidden && it.folderId != null }
            .map { it.copy(folderId = null, folderIndex = -1) },
        removedFolders = previousFolders.filter { it.id !in hiddenFolderIds },
        newFolders = home.appDrawerFolders.mapIndexed { position, folder ->
            FolderEblanApplicationInfo(
                id = requireNotNull(newFolderIds[folder.id]),
                icon = null,
                label = folder.label,
                index = keptMaxIndex + 1 + position,
                folderIndex = -1,
                folderId = null,
            )
        },
        assignedApps = home.appDrawer
            .filter { it.folderId != null }
            .groupBy { requireNotNull(it.folderId) }
            .flatMap { (folderId, members) ->
                members.mapIndexed { position, app ->
                    requireNotNull(appsByKey[app.componentName to app.serialNumber]).copy(
                        folderId = requireNotNull(newFolderIds[folderId]),
                        folderIndex = position,
                    )
                }
            },
    )
}
