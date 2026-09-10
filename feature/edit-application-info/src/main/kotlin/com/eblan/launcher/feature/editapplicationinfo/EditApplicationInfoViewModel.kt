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
import com.eblan.launcher.domain.framework.IconPackManager
import com.eblan.launcher.domain.framework.PackageManagerWrapper
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.model.EblanApplicationInfoTag
import com.eblan.launcher.domain.model.EblanApplicationInfoTagCrossRef
import com.eblan.launcher.domain.model.IconPackInfoComponent
import com.eblan.launcher.domain.model.PackageManagerIconPackInfo
import com.eblan.launcher.domain.repository.EblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.EblanApplicationInfoTagCrossRefRepository
import com.eblan.launcher.domain.repository.EblanApplicationInfoTagRepository
import com.eblan.launcher.domain.usecase.application.DeleteEblanApplicationInfoCustomIconUseCase
import com.eblan.launcher.domain.usecase.application.GetEblanApplicationInfosTagsUiUseCase
import com.eblan.launcher.domain.usecase.application.UpdateEblanApplicationInfoCustomIconUseCase
import com.eblan.launcher.feature.editapplicationinfo.model.EditApplicationInfoUiState
import com.eblan.launcher.feature.editapplicationinfo.navigation.EditApplicationInfoRouteData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class EditApplicationInfoViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val eblanApplicationInfoRepository: EblanApplicationInfoRepository,
    packageManagerWrapper: PackageManagerWrapper,
    private val iconPackManager: IconPackManager,
    getEblanApplicationInfosTagsUiUseCase: GetEblanApplicationInfosTagsUiUseCase,
    private val eblanApplicationInfoTagRepository: EblanApplicationInfoTagRepository,
    private val eblanApplicationInfoTagCrossRefRepository: EblanApplicationInfoTagCrossRefRepository,
    private val updateEblanApplicationInfoCustomIconUseCase: UpdateEblanApplicationInfoCustomIconUseCase,
    private val deleteEblanApplicationInfoCustomIconUseCase: DeleteEblanApplicationInfoCustomIconUseCase,
    @param:Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val editApplicationInfoRouteData =
        savedStateHandle.toRoute<EditApplicationInfoRouteData>()

    private val _editApplicationInfoUiState =
        MutableStateFlow<EditApplicationInfoUiState>(EditApplicationInfoUiState.Loading)

    val editApplicationInfoUiState = _editApplicationInfoUiState.onStart {
        getApplicationInfo()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = EditApplicationInfoUiState.Loading,
    )

    private val _packageManagerIconPackInfos =
        MutableStateFlow(emptyList<PackageManagerIconPackInfo>())

    val packageManagerIconPackInfos = _packageManagerIconPackInfos.onStart {
        _packageManagerIconPackInfos.update {
            packageManagerWrapper.getIconPackInfos()
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    private val _iconPackInfoComponents = MutableStateFlow(emptyList<IconPackInfoComponent>())

    val iconPackInfoComponents = _iconPackInfoComponents.asStateFlow()

    private var iconPackInfoComponentsJob: Job? = null

    private var lastIconPackInfoComponents = emptyList<IconPackInfoComponent>()

    val eblanApplicationInfoTagsUi = getEblanApplicationInfosTagsUiUseCase(
        serialNumber = editApplicationInfoRouteData.serialNumber,
        componentName = editApplicationInfoRouteData.componentName,
    ).stateIn(
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

    fun updateIconPackInfoPackageName(packageName: String) {
        iconPackInfoComponentsJob = viewModelScope.launch(defaultDispatcher) {
            _iconPackInfoComponents.update {
                iconPackManager.getIconPackInfoComponents(packageName = packageName)
                    .distinctBy { iconPackInfoComponent ->
                        iconPackInfoComponent.drawableName
                    }.also { iconPackInfoComponents ->
                        lastIconPackInfoComponents = iconPackInfoComponents
                    }
            }
        }
    }

    fun resetIconPackInfoPackageName() {
        iconPackInfoComponentsJob?.cancel()

        _iconPackInfoComponents.update {
            emptyList()
        }

        lastIconPackInfoComponents = emptyList()
    }

    fun resetEblanApplicationInfoCustomIcon(eblanApplicationInfo: EblanApplicationInfo) {
        viewModelScope.launch {
            deleteEblanApplicationInfoCustomIconUseCase(eblanApplicationInfo = eblanApplicationInfo)

            getApplicationInfo()
        }
    }

    fun searchIconPackInfoComponent(component: String) {
        viewModelScope.launch(defaultDispatcher) {
            _iconPackInfoComponents.update {
                lastIconPackInfoComponents.filter { iconPackInfoComponent ->
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

    private fun getApplicationInfo() {
        viewModelScope.launch {
            _editApplicationInfoUiState.update {
                EditApplicationInfoUiState.Success(
                    eblanApplicationInfo = eblanApplicationInfoRepository.getEblanApplicationInfoByComponentName(
                        serialNumber = editApplicationInfoRouteData.serialNumber,
                        componentName = editApplicationInfoRouteData.componentName,
                    ),
                )
            }
        }
    }
}
