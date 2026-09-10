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
import com.eblan.launcher.domain.common.IconKeyGenerator
import com.eblan.launcher.domain.framework.FileManager
import com.eblan.launcher.domain.framework.JaroWinklerSimilarityWrapper
import com.eblan.launcher.domain.framework.LauncherAppsWrapper
import com.eblan.launcher.domain.model.AppDrawerType
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.model.EblanUserPageKey
import com.eblan.launcher.domain.model.EblanUserType
import com.eblan.launcher.domain.model.GetEblanApplicationInfosByLabelAndTag
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
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
    ) { tagId, label, userData, eblanApplicationInfos ->
        val iconPackInfoPackageName = userData.generalSettings.iconPackInfoPackageName

        val eblanApplicationInfosByLabel = getEblanApplicationInfos(
            label = label,
            fuzzySearch = userData.appDrawerSettings.fuzzySearch,
            excludeTaggedApps = userData.appDrawerSettings.excludeTaggedApps,
            tagId = tagId,
            eblanApplicationInfos = eblanApplicationInfos,
        )

        when (userData.appDrawerSettings.appDrawerType) {
            AppDrawerType.Vertical, AppDrawerType.List ->
                getVerticalOrListEblanApplicationInfosByLabel(
                    eblanApplicationInfos = eblanApplicationInfosByLabel,
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
        eblanApplicationInfos: MutableList<EblanApplicationInfo>,
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
            eblanApplicationInfoWithIconPackInfos = groupedEblanApplicationInfos.filterKeys { it != privateEblanUserPageKey },
            privateEblanUser = privateEblanUserPageKey?.eblanUser,
            privateEblanApplicationInfoWithIconPackInfos = groupedEblanApplicationInfos[privateEblanUserPageKey].orEmpty(),
            iconPackInfoFilePaths = iconPackInfoFilePaths,
        )
    }

    private suspend fun getHorizontalEblanApplicationInfosByLabel(
        horizontalAppDrawerColumns: Int,
        horizontalAppDrawerRows: Int,
        eblanApplicationInfos: MutableList<EblanApplicationInfo>,
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
            eblanApplicationInfoWithIconPackInfos = groupedEblanApplicationInfos,
            privateEblanUser = null,
            privateEblanApplicationInfoWithIconPackInfos = emptyList(),
            iconPackInfoFilePaths = iconPackInfoFilePaths,
        )
    }

    private suspend fun getEblanApplicationInfos(
        label: String,
        fuzzySearch: Boolean,
        excludeTaggedApps: Boolean,
        tagId: Long?,
        eblanApplicationInfos: List<EblanApplicationInfo>,
    ): MutableList<EblanApplicationInfo> {
        val eblanApplicationInfosByTag = when {
            tagId != null ->
                eblanApplicationInfoRepository.getEblanApplicationInfosByTagId(id = tagId)

            excludeTaggedApps ->
                eblanApplicationInfoRepository.getEblanApplicationInfosWithoutTag()

            else -> eblanApplicationInfos
        }.filterNot { it.isHidden }

        val eblanApplicationInfosByLabel = eblanApplicationInfosByTag.filter {
            val currentLabel = it.customLabel ?: it.label

            currentLabel.startsWith(
                prefix = label,
                ignoreCase = true,
            ) || currentLabel.contains(
                other = label,
                ignoreCase = true,
            )
        }

        val filterEblanApplicationInfos =
            if (fuzzySearch || eblanApplicationInfosByLabel.isNotEmpty()) {
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

        return filterEblanApplicationInfos.toMutableList()
    }

    private suspend fun normalize(text: String): String = withContext(defaultDispatcher) {
        Normalizer.normalize(text, Normalizer.Form.NFD)
            .replace("\\p{M}+".toRegex(), "")
            .lowercase()
    }
}
