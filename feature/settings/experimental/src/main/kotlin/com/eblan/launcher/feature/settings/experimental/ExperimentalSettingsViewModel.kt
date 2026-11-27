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
import com.eblan.launcher.domain.model.cache.CacheInfo
import com.eblan.launcher.domain.model.userdata.ExperimentalSettings
import com.eblan.launcher.domain.repository.UserDataRepository
import com.eblan.launcher.domain.usecase.cache.CleanUpCacheUseCase
import com.eblan.launcher.domain.usecase.cache.GetCacheInfoUseCase
import com.eblan.launcher.feature.settings.experimental.model.ExperimentalSettingsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class ExperimentalSettingsViewModel @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val getCacheInfoUseCase: GetCacheInfoUseCase,
    private val cleanUpCacheUseCase: CleanUpCacheUseCase,
) : ViewModel() {
    private val _cacheInfo = MutableStateFlow(
        CacheInfo(
            totalSize = 0L,
            reclaimableSize = 0L,
        ),
    )

    private val _isCleaningUp = MutableStateFlow(false)

    val experimentalSettingsUiState = combine(
        userDataRepository.userDataFlow,
        _cacheInfo,
        _isCleaningUp,
    ) { userData, cacheInfo, isCleaningUp ->
        ExperimentalSettingsUiState.Success(
            experimentalSettings = userData.experimentalSettings,
            cacheInfo = cacheInfo,
            isCleaningUp = isCleaningUp,
        )
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

    fun refreshCacheInfo() {
        if (_isCleaningUp.value) return

        viewModelScope.launch {
            val cacheInfo = getCacheInfoUseCase()

            _cacheInfo.update { cacheInfo }
        }
    }

    fun cleanUpCache() {
        if (_isCleaningUp.value) return

        _isCleaningUp.update { true }

        viewModelScope.launch {
            cleanUpCacheUseCase()

            val cacheInfo = getCacheInfoUseCase()

            _cacheInfo.update { cacheInfo }

            _isCleaningUp.update { false }
        }
    }
}
