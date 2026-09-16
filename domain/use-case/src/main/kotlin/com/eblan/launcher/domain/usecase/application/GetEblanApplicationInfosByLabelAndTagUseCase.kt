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
import com.eblan.launcher.domain.common.FileManager
import com.eblan.launcher.domain.common.IconKeyGenerator
import com.eblan.launcher.domain.framework.JaroWinklerSimilarityWrapper
import com.eblan.launcher.domain.framework.LauncherAppsWrapper
import com.eblan.launcher.domain.model.application.EblanApplicationInfo
import com.eblan.launcher.domain.model.application.GetEblanApplicationInfosByLabelAndTag
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfo
import com.eblan.launcher.domain.model.launcherapps.EblanUserPageKey
import com.eblan.launcher.domain.model.launcherapps.EblanUserType
import com.eblan.launcher.domain.model.userdata.AppDrawerType
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.FolderEblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import com.eblan.launcher.domain.usecase.util.getIconPackInfoFilePaths
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.text.Normalizer
import javax.inject.Inject

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
    @OptIn(ExperimentalCoroutinesApi::class)
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
        val iconPackInfoPackageName = userData.generalSettings.iconPackInfoPackageName

        val eblanApplicationInfosByLabel = getEblanApplicationInfos(
            eblanApplicationInfos = eblanApplicationInfos,
            excludeTaggedApps = userData.appDrawerSettings.excludeTaggedApps,
            fuzzySearch = userData.appDrawerSettings.fuzzySearch,
            label = label,
            tagId = tagId,
        )

        val folderEblanApplicationInfosByLabel = if (tagId == null && label.isEmpty()) {
            folderEblanApplicationInfos.filterNot { it.folderId != null }
        } else {
            emptyList()
        }

        when (userData.appDrawerSettings.appDrawerType) {
            AppDrawerType.Vertical, AppDrawerType.List ->
                getVerticalOrListEblanApplicationInfosByLabel(
                    eblanApplicationInfos = eblanApplicationInfosByLabel,
                    folderEblanApplicationInfos = folderEblanApplicationInfosByLabel,
                    iconPackInfoPackageName = iconPackInfoPackageName,
                )

            AppDrawerType.Horizontal ->
                getHorizontalEblanApplicationInfosByLabel(
                    horizontalAppDrawerColumns = userData.appDrawerSettings.horizontalAppDrawerColumns,
                    horizontalAppDrawerRows = userData.appDrawerSettings.horizontalAppDrawerRows,
                    eblanApplicationInfos = eblanApplicationInfosByLabel,
                    iconPackInfoPackageName = iconPackInfoPackageName,
                )
        }
    }.flowOn(defaultDispatcher)

    private suspend fun getVerticalOrListEblanApplicationInfosByLabel(
        eblanApplicationInfos: List<EblanApplicationInfo>,
        folderEblanApplicationInfos: List<FolderEblanApplicationInfo>,
        iconPackInfoPackageName: String,
    ): GetEblanApplicationInfosByLabelAndTag {
        val groupedEblanApplicationInfos = eblanApplicationInfos.groupBy {
            EblanUserPageKey(
                eblanUser = launcherAppsWrapper.getUser(serialNumber = it.serialNumber),
                page = 0,
            )
        }.toSortedMap(nullsLast(compareBy { it.eblanUser.serialNumber }))

        val privateEblanUserPageKey = groupedEblanApplicationInfos.keys.firstOrNull {
            it.eblanUser.eblanUserType == EblanUserType.Private
        }

        val iconPackInfoFilePaths = getIconPackInfoFilePaths(
            iconPackInfoPackageName = iconPackInfoPackageName,
            componentNames = eblanApplicationInfos.map { it.componentName },
            fileManager = fileManager,
            iconKeyGenerator = iconKeyGenerator,
        )

        return GetEblanApplicationInfosByLabelAndTag(
            eblanApplicationInfos = groupedEblanApplicationInfos.filterKeys { it != privateEblanUserPageKey },
            privateEblanUser = privateEblanUserPageKey?.eblanUser,
            privateEblanApplicationInfos = groupedEblanApplicationInfos[privateEblanUserPageKey].orEmpty(),
            iconPackInfoFilePaths = iconPackInfoFilePaths,
            folderEblanApplicationInfos = folderEblanApplicationInfos,
        )
    }

    private suspend fun getHorizontalEblanApplicationInfosByLabel(
        horizontalAppDrawerColumns: Int,
        horizontalAppDrawerRows: Int,
        eblanApplicationInfos: List<EblanApplicationInfo>,
        iconPackInfoPackageName: String,
    ): GetEblanApplicationInfosByLabelAndTag {
        val groupedEblanApplicationInfos = eblanApplicationInfos.groupBy {
            launcherAppsWrapper.getUser(serialNumber = it.serialNumber)
        }.toSortedMap(nullsLast(compareBy { it.serialNumber }))
            .flatMap { (eblanUser, eblanApplicationInfos) ->
                eblanApplicationInfos.chunked(horizontalAppDrawerColumns * horizontalAppDrawerRows)
                    .mapIndexed { index, eblanApplicationInfos ->
                        EblanUserPageKey(
                            eblanUser = eblanUser,
                            page = index,
                        ) to eblanApplicationInfos
                    }
            }.toMap()

        val iconPackInfoFilePaths = getIconPackInfoFilePaths(
            iconPackInfoPackageName = iconPackInfoPackageName,
            componentNames = eblanApplicationInfos.map { it.componentName },
            fileManager = fileManager,
            iconKeyGenerator = iconKeyGenerator,
        )

        return GetEblanApplicationInfosByLabelAndTag(
            eblanApplicationInfos = groupedEblanApplicationInfos,
            privateEblanUser = null,
            privateEblanApplicationInfos = emptyList(),
            iconPackInfoFilePaths = iconPackInfoFilePaths,
            folderEblanApplicationInfos = emptyList(),
        )
    }

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
        }.filterNot { it.isHidden || it.folderId != null }

        val eblanApplicationInfosByLabel = eblanApplicationInfosByTag.filter {
            val currentLabel = it.customLabel ?: it.label

            currentLabel.contains(
                other = label,
                ignoreCase = true,
            )
        }

        return if (fuzzySearch || eblanApplicationInfosByLabel.isNotEmpty()) {
            val fuzzyMatches = if (fuzzySearch) {
                (eblanApplicationInfosByTag - eblanApplicationInfosByLabel.toSet())
                    .map {
                        val currentLabel = it.customLabel ?: it.label

                        it to jaroWinklerSimilarityWrapper.apply(
                            left = normalize(text = label),
                            right = normalize(text = currentLabel),
                        )
                    }
                    .filter { (_, score) -> score >= 0.85 }
                    .sortedByDescending { (_, score) -> score }
                    .map { (eblanApplicationInfo, _) -> eblanApplicationInfo }
            } else {
                emptyList()
            }

            eblanApplicationInfosByLabel.sortedBy {
                val currentLabel = it.customLabel ?: it.label

                currentLabel.lowercase()
            } + fuzzyMatches
        } else {
            emptyList()
        }
    }

    private suspend fun normalize(text: String): String = withContext(defaultDispatcher) {
        Normalizer.normalize(text, Normalizer.Form.NFD)
            .replace("\\p{M}+".toRegex(), "")
            .lowercase()
    }
}
