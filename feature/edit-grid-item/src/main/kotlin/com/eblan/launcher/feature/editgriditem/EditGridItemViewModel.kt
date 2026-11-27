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
package com.eblan.launcher.feature.editgriditem

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.eblan.launcher.domain.model.application.EblanApplicationInfo
import com.eblan.launcher.domain.model.grid.Associate
import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.domain.repository.GridRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import com.eblan.launcher.domain.usecase.application.GetEblanApplicationInfosUseCase
import com.eblan.launcher.domain.usecase.folder.AddApplicationInfosToFolderGridItemUseCase
import com.eblan.launcher.domain.usecase.grid.DeleteGridItemCustomIconUseCase
import com.eblan.launcher.domain.usecase.grid.GetGridItemByIdUseCase
import com.eblan.launcher.domain.usecase.grid.UpdateGridItemCustomIconUseCase
import com.eblan.launcher.domain.usecase.iconpack.GetIconPackComponentsUseCase
import com.eblan.launcher.feature.editgriditem.model.EditGridItemUiState
import com.eblan.launcher.feature.editgriditem.navigation.EditGridItemRouteData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class EditGridItemViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val gridRepository: GridRepository,
    userDataRepository: UserDataRepository,
    getEblanApplicationInfosUseCase: GetEblanApplicationInfosUseCase,
    private val updateGridItemCustomIconUseCase: UpdateGridItemCustomIconUseCase,
    private val deleteGridItemCustomIconUseCase: DeleteGridItemCustomIconUseCase,
    private val getGridItemByIdUseCase: GetGridItemByIdUseCase,
    private val addApplicationInfosToFolderGridItemUseCase: AddApplicationInfosToFolderGridItemUseCase,
    getIconPackComponentsUseCase: GetIconPackComponentsUseCase,
) : ViewModel() {
    private val editGridItemRouteData = savedStateHandle.toRoute<EditGridItemRouteData>()

    private val _gridItem = MutableSharedFlow<GridItem?>(replay = 1)

    val editGridItemUiState = combine(
        _gridItem,
        userDataRepository.userDataFlow,
    ) { gridItem, userData ->
        EditGridItemUiState.Success(
            gridItem = gridItem,
            generalSettings = userData.generalSettings,
        )
    }.onStart {
        getGridItem()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = EditGridItemUiState.Loading,
    )

    val eblanApplicationInfos = getEblanApplicationInfosUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
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

    fun updateGridItem(gridItem: GridItem) {
        viewModelScope.launch {
            gridRepository.updateGridItem(gridItem)

            getGridItem()
        }
    }

    fun resetGridItemCustomIcon(gridItem: GridItem) {
        viewModelScope.launch {
            deleteGridItemCustomIconUseCase(gridItem)

            getGridItem()
        }
    }

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

    fun updateGridItemCustomIcon(
        gridItem: GridItem,
        uri: String,
    ) {
        viewModelScope.launch {
            updateGridItemCustomIconUseCase(
                gridItem = gridItem,
                uri = uri,
            )

            getGridItem()
        }
    }

    fun updateGridItemCustomIconByteArray(
        gridItem: GridItem,
        byteArray: ByteArray,
    ) {
        viewModelScope.launch {
            updateGridItemCustomIconUseCase(
                gridItem = gridItem,
                byteArray = byteArray,
            )

            getGridItem()
        }
    }

    fun addApplicationInfosToFolder(
        folderId: String,
        page: Int,
        associate: Associate,
        eblanApplicationInfos: List<EblanApplicationInfo>,
    ) {
        viewModelScope.launch {
            addApplicationInfosToFolderGridItemUseCase(
                folderId = folderId,
                page = page,
                associate = associate,
                eblanApplicationInfos = eblanApplicationInfos,
            )
        }
    }

    private fun getGridItem() {
        viewModelScope.launch {
            _gridItem.emit(
                getGridItemByIdUseCase(editGridItemRouteData.id),
            )
        }
    }
}
