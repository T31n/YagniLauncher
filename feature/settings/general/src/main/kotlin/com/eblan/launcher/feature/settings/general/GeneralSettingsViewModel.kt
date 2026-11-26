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
package com.eblan.launcher.feature.settings.general

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eblan.launcher.domain.model.iconpackinfo.EblanIconPackInfo
import com.eblan.launcher.domain.model.userdata.GeneralSettings
import com.eblan.launcher.domain.repository.EblanIconPackInfoRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import com.eblan.launcher.domain.usecase.iconpack.UpdateIconPacksUseCase
import com.eblan.launcher.feature.settings.general.model.GeneralSettingsUiState
import com.eblan.launcher.feature.settings.general.model.ImportIconPackProgress
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class GeneralSettingsViewModel @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val eblanIconPackInfoRepository: EblanIconPackInfoRepository,
    private val updateIconPacksUseCase: UpdateIconPacksUseCase,
) : ViewModel() {
    val generalSettingsUiState = userDataRepository.userDataFlow.map { userData ->
        GeneralSettingsUiState.Success(generalSettings = userData.generalSettings)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = GeneralSettingsUiState.Loading,
    )

    val eblanIconPacks = eblanIconPackInfoRepository.eblanIconPacksFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    private var importIconPackJob: Job? = null

    private val _importIconPackProgress = MutableStateFlow<ImportIconPackProgress?>(null)
    val importIconPackProgress = _importIconPackProgress.asStateFlow()

    fun deleteIconPackInfo(eblanIconPackInfo: EblanIconPackInfo) {
        viewModelScope.launch {
            eblanIconPackInfoRepository.deleteEblanIconPackInfo(eblanIconPackInfo = eblanIconPackInfo)
        }
    }

    fun updateGeneralSettings(generalSettings: GeneralSettings) {
        viewModelScope.launch {
            userDataRepository.updateGeneralSettings(generalSettings = generalSettings)
        }
    }

    fun importIconPack(
        packageName: String,
        label: String,
    ) {
        importIconPackJob?.cancel()

        importIconPackJob = viewModelScope.launch {
            _importIconPackProgress.value = ImportIconPackProgress(label = label)

            updateIconPacksUseCase(iconPackPackageName = packageName) { current, total ->
                _importIconPackProgress.value = ImportIconPackProgress(
                    label = label,
                    current = current,
                    total = total,
                )
            }

            _importIconPackProgress.value = null
        }
    }

    fun cancelImportIconPack() {
        importIconPackJob?.cancel()
        _importIconPackProgress.value = null
    }
}
