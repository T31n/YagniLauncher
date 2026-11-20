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
package com.eblan.launcher.domain.usecase.application

import com.eblan.launcher.domain.common.Dispatcher
import com.eblan.launcher.domain.common.EblanDispatchers
import com.eblan.launcher.domain.model.application.EblanApplicationInfo
import com.eblan.launcher.domain.model.application.GetEblanApplicationInfosByLabelAndTag
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfo
import com.eblan.launcher.domain.model.launcherapps.EblanUserPageKey
import com.eblan.launcher.domain.model.launcherapps.EblanUserType
import com.eblan.launcher.domain.model.userdata.AppDrawerType
import com.eblan.launcher.domain.platform.FileManager
import com.eblan.launcher.domain.platform.IconKeyGenerator
import com.eblan.launcher.domain.platform.JaroWinklerSimilarityWrapper
import com.eblan.launcher.domain.platform.LauncherAppsWrapper
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.FolderEblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import com.eblan.launcher.domain.usecase.util.getIconPackFilePaths
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.text.Normalizer
import javax.inject.Inject

/** Produces filtered app-drawer data from application, folder, user, and display settings. */
class GetEblanApplicationInfosByLabelAndTagUseCase @Inject constructor(
    private val eblanApplicationInfoRepository: EblanApplicationInfoRepository,
    private val launcherAppsWrapper: LauncherAppsWrapper,
    private val userDataRepository: UserDataRepository,
    private val fileManager: FileManager,
    private val iconKeyGenerator: IconKeyGenerator,
    private val jaroWinklerSimilarityWrapper: JaroWinklerSimilarityWrapper,
    private val folderEblanApplicationInfoRepository: FolderEblanApplicationInfoRepository,
    @param:Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    /**
     * Combines the query and selected tag with repository and user settings to build app-drawer data.
     *
     * @param labelFlow The flow of text entered in the app-drawer search.
     * @param eblanApplicationInfoTagIdFlow The flow of the selected tag ID, or `null` for no tag.
     * @return A flow of filtered application and folder data in the configured drawer layout.
     */
    operator fun invoke(
        labelFlow: Flow<String>,
        eblanApplicationInfoTagIdFlow: Flow<Long?>,
    ): Flow<GetEblanApplicationInfosByLabelAndTag> = combine(
        eblanApplicationInfoTagIdFlow,
        labelFlow,
        userDataRepository.userDataFlow,
        eblanApplicationInfoRepository.eblanApplicationInfosFlow,
        folderEblanApplicationInfoRepository.folderEblanApplicationInfosFlow,
    ) { tagId, label, userData, eblanApplicationInfos, folderEblanApplicationInfos ->
        val iconPackPackageName = userData.generalSettings.iconPackPackageName

        val eblanApplicationInfosByLabel = getEblanApplicationInfos(
            eblanApplicationInfos = eblanApplicationInfos,
            excludeTaggedApps = userData.appDrawerSettings.excludeTaggedApps,
            fuzzySearch = userData.appDrawerSettings.fuzzySearch,
            label = label,
            tagId = tagId,
        )

        val folderEblanApplicationInfosByLabel = getFolderEblanApplicationInfos(
            folderEblanApplicationInfos = folderEblanApplicationInfos,
            fuzzySearch = userData.appDrawerSettings.fuzzySearch,
            label = label,
            tagId = tagId,
        )

        val iconPackFilePaths = getIconPackFilePaths(
            iconPackPackageName = iconPackPackageName,
            componentNames = eblanApplicationInfos.map { currentEblanApplicationInfo ->
                currentEblanApplicationInfo.componentName
            },
            fileManager = fileManager,
            iconKeyGenerator = iconKeyGenerator,
        )

        when (userData.appDrawerSettings.appDrawerType) {
            AppDrawerType.Vertical ->
                getVerticalOrListEblanApplicationInfosByLabel(
                    eblanApplicationInfos = eblanApplicationInfosByLabel,
                    folderEblanApplicationInfos = folderEblanApplicationInfosByLabel,
                    iconPackFilePaths = iconPackFilePaths,
                )

            AppDrawerType.Horizontal ->
                getHorizontalEblanApplicationInfosByLabel(
                    horizontalAppDrawerColumns = userData.appDrawerSettings.horizontalAppDrawerColumns,
                    horizontalAppDrawerRows = userData.appDrawerSettings.horizontalAppDrawerRows,
                    eblanApplicationInfos = eblanApplicationInfosByLabel,
                    iconPackFilePaths = iconPackFilePaths,
                )
        }
    }.flowOn(defaultDispatcher)

    /**
     * Groups vertical or list drawer contents by user, adds folders to the personal profile, and
     * builds optional alphabetical scroll-bar entries.
     */
    private suspend fun getVerticalOrListEblanApplicationInfosByLabel(
        eblanApplicationInfos: List<EblanApplicationInfo>,
        folderEblanApplicationInfos: List<FolderEblanApplicationInfo>,
        iconPackFilePaths: Map<String, String?>,
    ): GetEblanApplicationInfosByLabelAndTag {
        val groupedEblanApplicationInfos = eblanApplicationInfos
            .groupBy { currentEblanApplicationInfo ->
                EblanUserPageKey(
                    eblanUser = launcherAppsWrapper.getUser(serialNumber = currentEblanApplicationInfo.serialNumber),
                    page = 0,
                )
            }
            .toSortedMap(
                nullsLast(
                    compareBy { currentEblanUserPageKey ->
                        currentEblanUserPageKey.eblanUser.serialNumber
                    },
                ),
            )

        val groupedEblanApplicationInfosWithFolders =
            if (
                folderEblanApplicationInfos.isNotEmpty() &&
                groupedEblanApplicationInfos.keys.none { currentEblanUserPageKey ->
                    currentEblanUserPageKey.eblanUser.eblanUserType == EblanUserType.Personal
                }
            ) {
                val personalEblanUserPageKey = EblanUserPageKey(
                    eblanUser = launcherAppsWrapper.getUser(serialNumber = 0L),
                    page = 0,
                )

                groupedEblanApplicationInfos + (personalEblanUserPageKey to emptyList())
            } else {
                groupedEblanApplicationInfos
            }

        val privateEblanUserPageKey =
            groupedEblanApplicationInfosWithFolders.keys.firstOrNull { currentEblanUserPageKey ->
                currentEblanUserPageKey.eblanUser.eblanUserType == EblanUserType.Private
            }

        return GetEblanApplicationInfosByLabelAndTag(
            eblanApplicationInfos = groupedEblanApplicationInfosWithFolders
                .filterKeys { currentEblanUserPageKey ->
                    currentEblanUserPageKey != privateEblanUserPageKey
                },
            privateEblanUser = privateEblanUserPageKey?.eblanUser,
            privateEblanApplicationInfos = groupedEblanApplicationInfos[privateEblanUserPageKey].orEmpty(),
            iconPackFilePaths = iconPackFilePaths,
            folderEblanApplicationInfos = folderEblanApplicationInfos,
        )
    }

    /** Groups horizontal drawer results by user and splits each user's applications into pages. */
    private suspend fun getHorizontalEblanApplicationInfosByLabel(
        horizontalAppDrawerColumns: Int,
        horizontalAppDrawerRows: Int,
        eblanApplicationInfos: List<EblanApplicationInfo>,
        iconPackFilePaths: Map<String, String?>,
    ): GetEblanApplicationInfosByLabelAndTag {
        val groupedEblanApplicationInfos = eblanApplicationInfos.groupBy { currentEblanApplicationInfo ->
            launcherAppsWrapper.getUser(serialNumber = currentEblanApplicationInfo.serialNumber)
        }.toSortedMap(
            nullsLast(
                compareBy { currentEblanUser ->
                    currentEblanUser.serialNumber
                },
            ),
        )
            .flatMap { (eblanUser, eblanApplicationInfos) ->
                eblanApplicationInfos.chunked(horizontalAppDrawerColumns * horizontalAppDrawerRows)
                    .mapIndexed { index, eblanApplicationInfos ->
                        EblanUserPageKey(
                            eblanUser = eblanUser,
                            page = index,
                        ) to eblanApplicationInfos
                    }
            }.toMap()

        return GetEblanApplicationInfosByLabelAndTag(
            eblanApplicationInfos = groupedEblanApplicationInfos,
            privateEblanUser = null,
            privateEblanApplicationInfos = emptyList(),
            iconPackFilePaths = iconPackFilePaths,
            folderEblanApplicationInfos = emptyList(),
        )
    }

    /**
     * Selects eligible applications by tag and search query, omitting hidden and folder-contained
     * applications.
     */
    private suspend fun getEblanApplicationInfos(
        eblanApplicationInfos: List<EblanApplicationInfo>,
        excludeTaggedApps: Boolean,
        fuzzySearch: Boolean,
        label: String,
        tagId: Long?,
    ): List<EblanApplicationInfo> {
        val eblanApplicationInfosByTag = when {
            tagId != null ->
                eblanApplicationInfoRepository.getEblanApplicationInfosByTagId(id = tagId)

            excludeTaggedApps ->
                eblanApplicationInfoRepository.getEblanApplicationInfosWithoutTag()

            else -> eblanApplicationInfos
        }.filterNot { currentEblanApplicationInfo ->
            currentEblanApplicationInfo.isHidden || currentEblanApplicationInfo.folderId != null
        }

        return getItemsByLabel(
            items = eblanApplicationInfosByTag,
            fuzzySearch = fuzzySearch,
            label = label,
            getLabel = { currentEblanApplicationInfo ->
                currentEblanApplicationInfo.customLabel ?: currentEblanApplicationInfo.label
            },
        )
    }

    /** Selects top-level folders for an untagged search and filters them by folder label. */
    private suspend fun getFolderEblanApplicationInfos(
        folderEblanApplicationInfos: List<FolderEblanApplicationInfo>,
        fuzzySearch: Boolean,
        label: String,
        tagId: Long?,
    ): List<FolderEblanApplicationInfo> {
        val folderEblanApplicationInfosByTag = if (tagId == null) {
            folderEblanApplicationInfos.filterNot { currentFolderEblanApplicationInfo ->
                currentFolderEblanApplicationInfo.folderId != null
            }
        } else {
            emptyList()
        }

        return getItemsByLabel(
            items = folderEblanApplicationInfosByTag,
            fuzzySearch = fuzzySearch,
            label = label,
            getLabel = { currentFolderEblanApplicationInfo ->
                currentFolderEblanApplicationInfo.label
            },
        ).sortedBy { currentFolderEblanApplicationInfo ->
            currentFolderEblanApplicationInfo.index
        }
    }

    /**
     * Returns substring matches followed by sufficiently similar fuzzy matches when enabled.
     *
     * @param items The items to search.
     * @param fuzzySearch Whether to include normalized Jaro-Winkler matches.
     * @param label The search query.
     * @param getLabel Extracts the searchable label from an item.
     * @return Matching items, with exact substring matches ordered before fuzzy matches.
     */
    private suspend fun <T> getItemsByLabel(
        items: List<T>,
        fuzzySearch: Boolean,
        label: String,
        getLabel: (T) -> String,
    ): List<T> {
        val itemsByLabel = items.filter { currentItem ->
            getLabel(currentItem).contains(
                other = label,
                ignoreCase = true,
            )
        }

        return if (fuzzySearch || itemsByLabel.isNotEmpty()) {
            val fuzzyMatches = if (fuzzySearch) {
                (items - itemsByLabel.toSet())
                    .map { currentItem ->
                        currentItem to jaroWinklerSimilarityWrapper.apply(
                            left = normalize(text = label),
                            right = normalize(text = getLabel(currentItem)),
                        )
                    }
                    .filter { (_, score) -> score >= 0.85 }
                    .sortedByDescending { (_, score) -> score }
                    .map { (item, _) -> item }
            } else {
                emptyList()
            }

            itemsByLabel.sortedBy { currentItem ->
                getLabel(currentItem).lowercase()
            } + fuzzyMatches
        } else {
            emptyList()
        }
    }

    /** Removes diacritics and lowercases text for accent-insensitive fuzzy comparison. */
    private suspend fun normalize(text: String): String = withContext(defaultDispatcher) {
        Normalizer.normalize(text, Normalizer.Form.NFD)
            .replace("\\p{M}+".toRegex(), "")
            .lowercase()
    }
}
