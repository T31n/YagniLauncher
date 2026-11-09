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
package com.eblan.launcher.feature.settings.aiorganizer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eblan.launcher.domain.model.aiorganizer.AiOrganizerFile
import com.eblan.launcher.domain.model.aiorganizer.AiOrganizerStatus
import com.eblan.launcher.domain.model.aiorganizer.ApplyAiOrganizationResult
import com.eblan.launcher.domain.model.userdata.AiOrganizerSettings
import com.eblan.launcher.domain.repository.AiOrganizerRepository
import com.eblan.launcher.domain.repository.UserDataRepository
import com.eblan.launcher.domain.usecase.aiorganizer.ApplyAiOrganizationUseCase
import com.eblan.launcher.feature.settings.aiorganizer.model.AiOrganizerSettingsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class AiOrganizerSettingsViewModel @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val aiOrganizerRepository: AiOrganizerRepository,
    private val applyAiOrganizationUseCase: ApplyAiOrganizationUseCase,
) : ViewModel() {
    private val status = MutableStateFlow(AiOrganizerStatus(hasNewHome = false, hasLastHome = false))

    private val applyResult = MutableStateFlow<ApplyAiOrganizationResult?>(null)

    private var applyJob: Job? = null

    val aiOrganizerSettingsUiState = combine(
        userDataRepository.userDataFlow,
        status,
        applyResult,
    ) { userData, status, applyResult ->
        AiOrganizerSettingsUiState.Success(
            aiOrganizerSettings = userData.aiOrganizerSettings,
            status = status,
            applyResult = applyResult,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AiOrganizerSettingsUiState.Loading,
    )

    init {
        refreshStatus()
    }

    fun refreshStatus() {
        viewModelScope.launch {
            status.value = AiOrganizerStatus(
                hasNewHome = aiOrganizerRepository.fileExists(AiOrganizerFile.NEW_HOME),
                hasLastHome = aiOrganizerRepository.fileExists(AiOrganizerFile.LAST_HOME),
            )
        }
    }

    fun updateAiOrganizerSettings(aiOrganizerSettings: AiOrganizerSettings) {
        viewModelScope.launch {
            userDataRepository.updateAiOrganizerSettings(aiOrganizerSettings = aiOrganizerSettings)
        }
    }

    fun applyAiOrganization(restore: Boolean) {
        if (applyJob?.isActive == true) return

        applyJob = viewModelScope.launch {
            applyResult.value = applyAiOrganizationUseCase(restore = restore)

            refreshStatus()
        }
    }
}
