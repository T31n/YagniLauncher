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
package com.eblan.launcher.feature.settings.experimental

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eblan.launcher.domain.model.userdata.ExperimentalSettings
import com.eblan.launcher.domain.repository.UserDataRepository
import com.eblan.launcher.feature.settings.experimental.model.ExperimentalSettingsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ExperimentalSettingsViewModel @Inject constructor(private val userDataRepository: UserDataRepository) : ViewModel() {
    val experimentalSettingsUiState = userDataRepository.userDataFlow.map { userData ->
        ExperimentalSettingsUiState.Success(experimentalSettings = userData.experimentalSettings)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ExperimentalSettingsUiState.Loading,
    )

    fun updateExperimentalSettings(experimentalSettings: ExperimentalSettings) {
        viewModelScope.launch {
            userDataRepository.updateExperimentalSettings(experimentalSettings = experimentalSettings)
        }
    }
}
