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
import com.eblan.launcher.domain.model.application.EblanApplicationInfo
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfo
import com.eblan.launcher.domain.repository.FolderEblanApplicationInfoRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import com.eblan.launcher.domain.usecase.application.GetEblanApplicationInfosUseCase
import com.eblan.launcher.domain.usecase.folder.AddEblanApplicationInfosToFolderUseCase
import com.eblan.launcher.domain.usecase.folder.GetFolderEblanApplicationInfoPopupsUseCase
import com.eblan.launcher.domain.usecase.folder.UpdateFolderEblanApplicationInfoCustomIconUseCase
import com.eblan.launcher.domain.usecase.iconpack.GetIconPackComponentsUseCase
import com.eblan.launcher.feature.editfolderapplicationinfo.model.EditFolderApplicationInfoUiState
import com.eblan.launcher.feature.editfolderapplicationinfo.navigation.EditFolderApplicationInfoRouteData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class EditFolderApplicationInfoViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val folderEblanApplicationInfoRepository: FolderEblanApplicationInfoRepository,
    userDataRepository: UserDataRepository,
    private val updateFolderEblanApplicationInfoCustomIconUseCase: UpdateFolderEblanApplicationInfoCustomIconUseCase,
    private val addEblanApplicationInfosToFolderUseCase: AddEblanApplicationInfosToFolderUseCase,
    getEblanApplicationInfosUseCase: GetEblanApplicationInfosUseCase,
    getFolderEblanApplicationInfoPopupsUseCase: GetFolderEblanApplicationInfoPopupsUseCase,
    getIconPackComponentsUseCase: GetIconPackComponentsUseCase,
) : ViewModel() {
    private val editFolderApplicationInfoRouteData =
        savedStateHandle.toRoute<EditFolderApplicationInfoRouteData>()

    private val _folderEblanApplicationInfo = MutableSharedFlow<FolderEblanApplicationInfo?>(replay = 1)
    val editFolderApplicationInfoUiState = combine(
        _folderEblanApplicationInfo,
        userDataRepository.userDataFlow,
    ) { folderEblanApplicationInfo, userData ->
        EditFolderApplicationInfoUiState.Success(
            folderEblanApplicationInfo = folderEblanApplicationInfo,
            generalSettings = userData.generalSettings,
        )
    }.onStart {
        getFolderApplicationInfo()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = EditFolderApplicationInfoUiState.Loading,
    )

    private val _iconPackPackageName = MutableStateFlow<String?>(null)
    private val _iconPackComponentQuery = MutableStateFlow("")

    val iconPackInfoComponents = getIconPackComponentsUseCase(
        packageNameFlow = _iconPackPackageName,
        queryFlow = _iconPackComponentQuery,
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val folderEblanApplicationInfos =
        folderEblanApplicationInfoRepository.folderEblanApplicationInfosFlow
            .map { folderEblanApplicationInfos ->
                folderEblanApplicationInfos.filterNot { currentFolderEblanApplicationInfo ->
                    currentFolderEblanApplicationInfo.id == editFolderApplicationInfoRouteData.id
                }
            }
            .stateIn(
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

    val eblanApplicationInfos = getEblanApplicationInfosUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    fun updateIconPackPackageName(packageName: String) {
        _iconPackComponentQuery.update { "" }
        _iconPackPackageName.update { packageName }
    }

    fun resetIconPackPackageName() {
        _iconPackPackageName.update { null }
        _iconPackComponentQuery.update { "" }
    }

    fun searchIconPackInfoComponent(component: String) {
        _iconPackComponentQuery.update { component }
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

    fun updateFolderEblanApplicationInfoCustomIconByteArray(
        folderEblanApplicationInfo: FolderEblanApplicationInfo,
        byteArray: ByteArray,
    ) {
        viewModelScope.launch {
            updateFolderEblanApplicationInfoCustomIconUseCase(
                folderEblanApplicationInfo = folderEblanApplicationInfo,
                byteArray = byteArray,
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

    fun addEblanApplicationInfosToFolder(
        folderEblanApplicationInfo: FolderEblanApplicationInfo,
        eblanApplicationInfos: List<EblanApplicationInfo>,
    ) {
        viewModelScope.launch {
            addEblanApplicationInfosToFolderUseCase(
                folderEblanApplicationInfo = folderEblanApplicationInfo,
                eblanApplicationInfos = eblanApplicationInfos,
            )
        }
    }

    private fun getFolderApplicationInfo() {
        viewModelScope.launch {
            _folderEblanApplicationInfo.emit(
                folderEblanApplicationInfoRepository.getFolderEblanApplicationInfoById(
                    id = editFolderApplicationInfoRouteData.id,
                ),
            )
        }
    }
}
