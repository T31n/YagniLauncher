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
import com.eblan.launcher.domain.common.Dispatcher
import com.eblan.launcher.domain.common.EblanDispatchers
import com.eblan.launcher.domain.common.FileManager
import com.eblan.launcher.domain.common.HomeOperationLock
import com.eblan.launcher.domain.model.aiorganizer.AiOrganizerFile
import com.eblan.launcher.domain.model.aiorganizer.ApplyAiOrganizationResult
import com.eblan.launcher.domain.model.userdata.AiOrganizerSettings
import com.eblan.launcher.domain.repository.UserDataRepository
import com.eblan.launcher.domain.usecase.aiorganizer.ApplyAiOrganizationUseCase
import com.eblan.launcher.feature.settings.aiorganizer.model.AiOrganizerSettingsUiState
import com.eblan.launcher.feature.settings.aiorganizer.model.AiOrganizerStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
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
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

internal enum class AiOrganizerSettingsMessage {
    SettingsSaved,
    SettingsSaveFailed,
}

@HiltViewModel
internal class AiOrganizerSettingsViewModel @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val applyAiOrganizationUseCase: ApplyAiOrganizationUseCase,
    private val fileManager: FileManager,
    private val homeOperationLock: HomeOperationLock,
    @param:Dispatcher(EblanDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val hasNewHomeStatus = MutableStateFlow(AiOrganizerStatus(hasNewHome = false))

    private val applyResult = MutableStateFlow<ApplyAiOrganizationResult?>(null)

    private val _messages = MutableSharedFlow<AiOrganizerSettingsMessage>(extraBufferCapacity = 1)

    val messages: SharedFlow<AiOrganizerSettingsMessage> = _messages.asSharedFlow()

    private var applyJob: Job? = null

    private val applyExceptionHandler = CoroutineExceptionHandler { _, _ ->
        applyResult.value = ApplyAiOrganizationResult.FAILED

        refreshStatus()
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
            hasNewHomeStatus.value = withContext(ioDispatcher) {
                val directory = fileManager.getFilesDirectory(FileManager.HOME_DIR)

                AiOrganizerStatus(
                    hasNewHome = File(directory, AiOrganizerFile.NEW_HOME.fileName).exists(),
                )
            }
        }
    }

    fun updateAiOrganizerSettings(aiOrganizerSettings: AiOrganizerSettings) {
        viewModelScope.launch {
            try {
                userDataRepository.updateAiOrganizerSettings(aiOrganizerSettings = aiOrganizerSettings)
            } catch (exception: Exception) {
                _messages.emit(AiOrganizerSettingsMessage.SettingsSaveFailed)

                return@launch
            }

            _messages.emit(AiOrganizerSettingsMessage.SettingsSaved)
        }
    }

    fun applyAiOrganization() {
        if (applyJob?.isActive == true) return

        applyJob = viewModelScope.launch(applyExceptionHandler) {
            applyResult.value = applyAiOrganizationUseCase()

            refreshStatus()
        }
    }
}
