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
import com.eblan.launcher.domain.framework.AiOrganizerOperationState
import com.eblan.launcher.domain.model.userdata.AiOrganizerSettings
import com.eblan.launcher.domain.repository.UserDataRepository
import com.eblan.launcher.feature.settings.aiorganizer.model.AiOrganizerSettingsUiState
import com.eblan.launcher.feature.settings.aiorganizer.model.AiOrganizerStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
internal class AiOrganizerSettingsViewModel @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val fileManager: FileManager,
    private val aiOrganizerOperationState: AiOrganizerOperationState,
    @param:Dispatcher(EblanDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val hasNewHomeStatus = aiOrganizerOperationState.operation
        .map { operation -> operation == null }
        .distinctUntilChanged()
        .filter { isIdle -> isIdle }
        .map {
            val directory = fileManager.getFilesRootDirectory()

            AiOrganizerStatus(
                hasNewHome = File(directory, FileManager.HOME_FILE_NAME).exists(),
            )
        }
        .flowOn(ioDispatcher)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AiOrganizerStatus(hasNewHome = false),
        )

    val aiOrganizerSettingsUiState = combine(
        userDataRepository.userDataFlow,
        hasNewHomeStatus,
        aiOrganizerOperationState.applyResult,
        aiOrganizerOperationState.operation.map { it != null }.distinctUntilChanged(),
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

    fun updateAiOrganizerSettings(aiOrganizerSettings: AiOrganizerSettings) {
        viewModelScope.launch {
            userDataRepository.updateAiOrganizerSettings(aiOrganizerSettings = aiOrganizerSettings)
        }
    }
}
