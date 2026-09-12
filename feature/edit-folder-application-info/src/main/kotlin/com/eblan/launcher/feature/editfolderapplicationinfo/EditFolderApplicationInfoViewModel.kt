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
package com.eblan.launcher.feature.editfolderapplicationinfo

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.eblan.launcher.domain.common.Dispatcher
import com.eblan.launcher.domain.common.EblanDispatchers
import com.eblan.launcher.domain.framework.IconPackManager
import com.eblan.launcher.domain.framework.PackageManagerWrapper
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfo
import com.eblan.launcher.domain.model.iconpackinfo.IconPackInfoComponent
import com.eblan.launcher.domain.model.iconpackinfo.PackageManagerIconPackInfo
import com.eblan.launcher.domain.repository.FolderEblanApplicationInfoRepository
import com.eblan.launcher.domain.usecase.folder.DeleteFolderEblanApplicationInfoCustomIconUseCase
import com.eblan.launcher.domain.usecase.folder.GetFolderEblanApplicationInfosByIdUseCase
import com.eblan.launcher.domain.usecase.folder.UpdateFolderEblanApplicationInfoCustomIconUseCase
import com.eblan.launcher.feature.editfolderapplicationinfo.model.EditFolderApplicationInfoUiState
import com.eblan.launcher.feature.editfolderapplicationinfo.navigation.EditFolderApplicationInfoRouteData
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
internal class EditFolderApplicationInfoViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    packageManagerWrapper: PackageManagerWrapper,
    private val iconPackManager: IconPackManager,
    private val folderEblanApplicationInfoRepository: FolderEblanApplicationInfoRepository,
    private val deleteFolderEblanApplicationInfoCustomIconUseCase: DeleteFolderEblanApplicationInfoCustomIconUseCase,
    private val updateFolderEblanApplicationInfoCustomIconUseCase: UpdateFolderEblanApplicationInfoCustomIconUseCase,
    getFolderEblanApplicationInfosByIdUseCase: GetFolderEblanApplicationInfosByIdUseCase,
    @param:Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val editFolderApplicationInfoRouteData =
        savedStateHandle.toRoute<EditFolderApplicationInfoRouteData>()

    private val _editFolderApplicationInfoUiState =
        MutableStateFlow<EditFolderApplicationInfoUiState>(EditFolderApplicationInfoUiState.Loading)
    val editFolderApplicationInfoUiState = _editFolderApplicationInfoUiState.onStart {
        getFolderApplicationInfo()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = EditFolderApplicationInfoUiState.Loading,
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

    val folderEblanApplicationInfos =
        getFolderEblanApplicationInfosByIdUseCase(id = editFolderApplicationInfoRouteData.id).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

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

    fun resetFolderEblanApplicationInfoCustomIcon(folderEblanApplicationInfo: FolderEblanApplicationInfo) {
        viewModelScope.launch {
            deleteFolderEblanApplicationInfoCustomIconUseCase(folderEblanApplicationInfo = folderEblanApplicationInfo)

            getFolderApplicationInfo()
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

    fun updateFolderEblanApplicationInfoCustomIcon(
        folderEblanApplicationInfo: FolderEblanApplicationInfo,
        uri: String,
    ) {
        viewModelScope.launch {
            updateFolderEblanApplicationInfoCustomIconUseCase(
                folderEblanApplicationInfo = folderEblanApplicationInfo,
                uri = uri,
            )

            getFolderApplicationInfo()
        }
    }

    fun addFolderEblanApplicationInfo(folderEblanApplicationInfo: FolderEblanApplicationInfo) {
        viewModelScope.launch {
            folderEblanApplicationInfoRepository.insertFolderEblanApplicationInfo(
                folderEblanApplicationInfo = folderEblanApplicationInfo,
            )
        }
    }

    fun updateFolderEblanApplicationInfo(folderEblanApplicationInfo: FolderEblanApplicationInfo) {
        viewModelScope.launch {
            folderEblanApplicationInfoRepository.updateFolderEblanApplicationInfo(
                folderEblanApplicationInfo = folderEblanApplicationInfo,
            )

            getFolderApplicationInfo()
        }
    }

    private fun getFolderApplicationInfo() {
        viewModelScope.launch {
            _editFolderApplicationInfoUiState.update {
                EditFolderApplicationInfoUiState.Success(
                    folderEblanApplicationInfo =
                    folderEblanApplicationInfoRepository.getFolderEblanApplicationInfoById(id = editFolderApplicationInfoRouteData.id),
                )
            }
        }
    }
}
