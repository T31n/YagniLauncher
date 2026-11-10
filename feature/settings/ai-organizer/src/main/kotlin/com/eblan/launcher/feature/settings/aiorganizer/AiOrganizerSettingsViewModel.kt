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
import com.eblan.launcher.domain.common.HomeOperationLock
import com.eblan.launcher.domain.model.aiorganizer.AiOrganizerStatus
import com.eblan.launcher.domain.model.aiorganizer.ApplyAiOrganizationResult
import com.eblan.launcher.domain.model.userdata.AiOrganizerSettings
import com.eblan.launcher.domain.repository.UserDataRepository
import com.eblan.launcher.domain.usecase.aiorganizer.ApplyAiOrganizationUseCase
import com.eblan.launcher.domain.usecase.aiorganizer.BackupAiOrganizationUseCase
import com.eblan.launcher.domain.usecase.aiorganizer.GetAiOrganizerStatusUseCase
import com.eblan.launcher.feature.settings.aiorganizer.model.AiOrganizerSettingsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

internal enum class AiOrganizerSettingsMessage {
    SettingsSaved,
    SettingsSaveFailed,
    BackupSaved,
    BackupFailed,
}

@HiltViewModel
internal class AiOrganizerSettingsViewModel @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val applyAiOrganizationUseCase: ApplyAiOrganizationUseCase,
    private val backupAiOrganizationUseCase: BackupAiOrganizationUseCase,
    private val getAiOrganizerStatusUseCase: GetAiOrganizerStatusUseCase,
    private val homeOperationLock: HomeOperationLock,
) : ViewModel() {
    private val hasNewHomeStatus = MutableStateFlow(AiOrganizerStatus(hasNewHome = false, hasBackup = false))

    private val applyResult = MutableStateFlow<ApplyAiOrganizationResult?>(null)

    private val _messages = MutableSharedFlow<AiOrganizerSettingsMessage>(extraBufferCapacity = 1)

    val messages: SharedFlow<AiOrganizerSettingsMessage> = _messages.asSharedFlow()

    private var applyJob: Job? = null

    private var backupJob: Job? = null

    private val applyExceptionHandler = CoroutineExceptionHandler { _, _ ->
        applyResult.value = ApplyAiOrganizationResult.FAILED

        refreshStatus()
    }

    private val backupExceptionHandler = CoroutineExceptionHandler { _, _ ->
        refreshStatus()

        _messages.tryEmit(AiOrganizerSettingsMessage.BackupFailed)
    }

    val aiOrganizerSettingsUiState = combine(
        userDataRepository.userDataFlow,
        hasNewHomeStatus,
        applyResult,
        homeOperationLock.isBusy,
    ) { userData, aiOrganizerStatus, applyResult, isBusy ->
        AiOrganizerSettingsUiState.Success(
            aiOrganizerSettings = userData.aiOrganizerSettings,
            status = aiOrganizerStatus,
            applyResult = applyResult,
            isBusy = isBusy,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AiOrganizerSettingsUiState.Loading,
    )

    init {
        refreshStatus()

        viewModelScope.launch {
            homeOperationLock.isBusy.collect { busy ->
                if (!busy) refreshStatus()
            }
        }
    }

    fun refreshStatus() {
        viewModelScope.launch {
            hasNewHomeStatus.value = getAiOrganizerStatusUseCase()
        }
    }

    fun updateAiOrganizerSettings(aiOrganizerSettings: AiOrganizerSettings) {
        viewModelScope.launch {
            runCatching {
                userDataRepository.updateAiOrganizerSettings(aiOrganizerSettings = aiOrganizerSettings)
            }.onSuccess {
                _messages.emit(AiOrganizerSettingsMessage.SettingsSaved)
            }.onFailure {
                _messages.emit(AiOrganizerSettingsMessage.SettingsSaveFailed)
            }
        }
    }

    fun applyAiOrganization(restore: Boolean) {
        if (applyJob?.isActive == true) return

        applyJob = viewModelScope.launch(applyExceptionHandler) {
            applyResult.value = applyAiOrganizationUseCase(restore = restore)

            refreshStatus()
        }
    }

    fun backupAiOrganization() {
        if (backupJob?.isActive == true) return

        backupJob = viewModelScope.launch(backupExceptionHandler) {
            backupAiOrganizationUseCase()

            refreshStatus()

            _messages.emit(AiOrganizerSettingsMessage.BackupSaved)
        }
    }
}
