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
package com.eblan.launcher.feature.editapplicationinfo

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.eblan.launcher.domain.common.Dispatcher
import com.eblan.launcher.domain.common.EblanDispatchers
import com.eblan.launcher.domain.model.application.EblanApplicationInfo
import com.eblan.launcher.domain.model.application.EblanApplicationInfoTag
import com.eblan.launcher.domain.model.application.EblanApplicationInfoTagCrossRef
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfo
import com.eblan.launcher.domain.model.iconpackinfo.IconPackComponent
import com.eblan.launcher.domain.platform.IconPackManager
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.EblanApplicationInfoTagCrossRefRepository
import com.eblan.launcher.domain.repository.EblanApplicationInfoTagRepository
import com.eblan.launcher.domain.repository.FolderEblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import com.eblan.launcher.domain.usecase.application.GetEblanApplicationInfosTagsUiUseCase
import com.eblan.launcher.domain.usecase.application.UpdateEblanApplicationInfoCustomIconUseCase
import com.eblan.launcher.domain.usecase.folder.GetFolderEblanApplicationInfoPopupsUseCase
import com.eblan.launcher.domain.usecase.folder.GetTopLevelFolderEblanApplicationInfosUseCase
import com.eblan.launcher.feature.editapplicationinfo.model.EditApplicationInfoUiState
import com.eblan.launcher.feature.editapplicationinfo.navigation.EditApplicationInfoRouteData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class EditApplicationInfoViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val eblanApplicationInfoRepository: EblanApplicationInfoRepository,
    private val iconPackManager: IconPackManager,
    getEblanApplicationInfosTagsUiUseCase: GetEblanApplicationInfosTagsUiUseCase,
    private val eblanApplicationInfoTagRepository: EblanApplicationInfoTagRepository,
    private val eblanApplicationInfoTagCrossRefRepository: EblanApplicationInfoTagCrossRefRepository,
    private val updateEblanApplicationInfoCustomIconUseCase: UpdateEblanApplicationInfoCustomIconUseCase,
    private val folderEblanApplicationInfoRepository: FolderEblanApplicationInfoRepository,
    userDataRepository: UserDataRepository,
    getFolderEblanApplicationInfoPopupsUseCase: GetFolderEblanApplicationInfoPopupsUseCase,
    getTopLevelFolderEblanApplicationInfosUseCase: GetTopLevelFolderEblanApplicationInfosUseCase,
    @param:Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val editApplicationInfoRouteData =
        savedStateHandle.toRoute<EditApplicationInfoRouteData>()

    private val _eblanApplicationInfo = MutableSharedFlow<EblanApplicationInfo?>(replay = 1)
    val editApplicationInfoUiState = combine(
        _eblanApplicationInfo,
        userDataRepository.userDataFlow,
    ) { eblanApplicationInfo, userData ->
        EditApplicationInfoUiState.Success(
            eblanApplicationInfo = eblanApplicationInfo,
            generalSettings = userData.generalSettings,
        )
    }.onStart {
        getApplicationInfo()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = EditApplicationInfoUiState.Loading,
    )

    private val _iconPackComponents = MutableStateFlow(emptyList<IconPackComponent>())
    val iconPackInfoComponents = _iconPackComponents.asStateFlow()

    private var iconPackComponentsJob: Job? = null

    private var lastIconPackComponents = emptyList<IconPackComponent>()

    val eblanApplicationInfoTagsUi = getEblanApplicationInfosTagsUiUseCase(
        serialNumber = editApplicationInfoRouteData.serialNumber,
        componentName = editApplicationInfoRouteData.componentName,
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val folderEblanApplicationInfos =
        folderEblanApplicationInfoRepository.folderEblanApplicationInfosFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    val folderEblanApplicationInfoPopups =
        getFolderEblanApplicationInfoPopupsUseCase().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyMap(),
        )

    val topLevelFolderEblanApplicationInfos =
        getTopLevelFolderEblanApplicationInfosUseCase().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    fun updateEblanApplicationInfo(eblanApplicationInfo: EblanApplicationInfo) {
        viewModelScope.launch {
            eblanApplicationInfoRepository.updateEblanApplicationInfo(eblanApplicationInfo = eblanApplicationInfo)

            getApplicationInfo()
        }
    }

    fun updateIconPackPackageName(packageName: String) {
        iconPackComponentsJob = viewModelScope.launch(defaultDispatcher) {
            _iconPackComponents.update {
                iconPackManager.getIconPackComponents(packageName = packageName)
                    .distinctBy { iconPackInfoComponent ->
                        iconPackInfoComponent.drawableName
                    }.also { iconPackInfoComponents ->
                        lastIconPackComponents = iconPackInfoComponents
                    }
            }
        }
    }

    fun resetIconPackPackageName() {
        iconPackComponentsJob?.cancel()

        _iconPackComponents.update {
            emptyList()
        }

        lastIconPackComponents = emptyList()
    }

    fun searchIconPackInfoComponent(component: String) {
        viewModelScope.launch(defaultDispatcher) {
            _iconPackComponents.update {
                lastIconPackComponents.filter { iconPackInfoComponent ->
                    iconPackInfoComponent.componentName.contains(
                        other = component,
                        ignoreCase = true,
                    )
                }
            }
        }
    }

    fun addEblanApplicationInfoTag(eblanApplicationInfoTag: EblanApplicationInfoTag) {
        viewModelScope.launch {
            eblanApplicationInfoTagRepository.insertEblanApplicationInfoTag(eblanApplicationInfoTag = eblanApplicationInfoTag)
        }
    }

    fun updateEblanApplicationInfoTag(eblanApplicationInfoTag: EblanApplicationInfoTag) {
        viewModelScope.launch {
            eblanApplicationInfoTagRepository.updateEblanApplicationInfoTag(eblanApplicationInfoTag = eblanApplicationInfoTag)
        }
    }

    fun deleteEblanApplicationInfoTag(eblanApplicationInfoTag: EblanApplicationInfoTag) {
        viewModelScope.launch {
            eblanApplicationInfoTagRepository.deleteEblanApplicationInfoTag(eblanApplicationInfoTag = eblanApplicationInfoTag)
        }
    }

    fun addEblanApplicationInfoTagCrossRef(id: Long) {
        viewModelScope.launch {
            eblanApplicationInfoTagCrossRefRepository.insertEblanApplicationInfoTagCrossRef(
                eblanApplicationInfoTagCrossRef = EblanApplicationInfoTagCrossRef(
                    componentName = editApplicationInfoRouteData.componentName,
                    serialNumber = editApplicationInfoRouteData.serialNumber,
                    id = id,
                ),
            )
        }
    }

    fun deleteEblanApplicationInfoTagCrossRef(id: Long) {
        viewModelScope.launch {
            eblanApplicationInfoTagCrossRefRepository.deleteEblanApplicationInfoTagCrossRef(
                componentName = editApplicationInfoRouteData.componentName,
                serialNumber = editApplicationInfoRouteData.serialNumber,
                tagId = id,
            )
        }
    }

    fun updateEblanApplicationInfoCustomIcon(
        eblanApplicationInfo: EblanApplicationInfo,
        uri: String,
    ) {
        viewModelScope.launch {
            updateEblanApplicationInfoCustomIconUseCase(
                eblanApplicationInfo = eblanApplicationInfo,
                uri = uri,
            )

            getApplicationInfo()
        }
    }

    fun updateEblanApplicationInfoCustomIconByteArray(
        eblanApplicationInfo: EblanApplicationInfo,
        byteArray: ByteArray,
    ) {
        viewModelScope.launch {
            updateEblanApplicationInfoCustomIconUseCase(
                eblanApplicationInfo = eblanApplicationInfo,
                byteArray = byteArray,
            )

            getApplicationInfo()
        }
    }

    fun addFolderEblanApplicationInfo(folderEblanApplicationInfo: FolderEblanApplicationInfo) {
        viewModelScope.launch {
            folderEblanApplicationInfoRepository.insertFolderEblanApplicationInfo(
                folderEblanApplicationInfo = folderEblanApplicationInfo,
            )
        }
    }

    private fun getApplicationInfo() {
        viewModelScope.launch {
            _eblanApplicationInfo.emit(
                eblanApplicationInfoRepository.getEblanApplicationInfoByComponentName(
                    serialNumber = editApplicationInfoRouteData.serialNumber,
                    componentName = editApplicationInfoRouteData.componentName,
                ),
            )
        }
    }
}
