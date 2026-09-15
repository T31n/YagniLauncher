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
import com.eblan.launcher.domain.common.Dispatcher
import com.eblan.launcher.domain.common.EblanDispatchers
import com.eblan.launcher.domain.framework.IconPackManager
import com.eblan.launcher.domain.framework.PackageManagerWrapper
import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.domain.model.iconpackinfo.IconPackInfoComponent
import com.eblan.launcher.domain.model.iconpackinfo.PackageManagerIconPackInfo
import com.eblan.launcher.domain.repository.GridRepository
import com.eblan.launcher.domain.usecase.application.GetEblanApplicationInfosUseCase
import com.eblan.launcher.domain.usecase.grid.DeleteGridItemCustomIconUseCase
import com.eblan.launcher.domain.usecase.grid.GetGridItemByIdUseCase
import com.eblan.launcher.domain.usecase.grid.UpdateGridItemCustomIconUseCase
import com.eblan.launcher.feature.editgriditem.model.EditGridItemUiState
import com.eblan.launcher.feature.editgriditem.navigation.EditGridItemRouteData
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
internal class EditGridItemViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val iconPackManager: IconPackManager,
    packageManagerWrapper: PackageManagerWrapper,
    private val gridRepository: GridRepository,
    getEblanApplicationInfosUseCase: GetEblanApplicationInfosUseCase,
    private val updateGridItemCustomIconUseCase: UpdateGridItemCustomIconUseCase,
    private val deleteGridItemCustomIconUseCase: DeleteGridItemCustomIconUseCase,
    private val getGridItemByIdUseCase: GetGridItemByIdUseCase,
    @param:Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val editGridItemRouteData = savedStateHandle.toRoute<EditGridItemRouteData>()

    private val _editGridItemUiState =
        MutableStateFlow<EditGridItemUiState>(EditGridItemUiState.Loading)

    val editGridItemUiState = _editGridItemUiState.onStart {
        getGridItem()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = EditGridItemUiState.Loading,
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

    val eblanApplicationInfos = getEblanApplicationInfosUseCase().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    private val _iconPackInfoComponents = MutableStateFlow(emptyList<IconPackInfoComponent>())

    val iconPackInfoComponents = _iconPackInfoComponents.asStateFlow()

    private var iconPackInfoComponentsJob: Job? = null

    private var lastIconPackInfoComponents = emptyList<IconPackInfoComponent>()

    fun updateGridItem(gridItem: GridItem) {
        viewModelScope.launch {
            gridRepository.updateGridItem(gridItem = gridItem)

            getGridItem()
        }
    }

    fun resetGridItemCustomIcon(gridItem: GridItem) {
        viewModelScope.launch {
            deleteGridItemCustomIconUseCase(gridItem = gridItem)

            getGridItem()
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

    private fun getGridItem() {
        viewModelScope.launch {
            _editGridItemUiState.update {
                EditGridItemUiState.Success(
                    gridItem = getGridItemByIdUseCase(id = editGridItemRouteData.id),
                )
            }
        }
    }
}
