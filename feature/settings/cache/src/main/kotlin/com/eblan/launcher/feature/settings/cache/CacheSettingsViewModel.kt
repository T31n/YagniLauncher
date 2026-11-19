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
package com.eblan.launcher.feature.settings.cache

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eblan.launcher.domain.usecase.cache.CleanUpCacheUseCase
import com.eblan.launcher.domain.usecase.cache.GetCacheInfoUseCase
import com.eblan.launcher.feature.settings.cache.model.CacheSettingsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class CacheSettingsViewModel @Inject constructor(
    private val getCacheInfoUseCase: GetCacheInfoUseCase,
    private val cleanUpCacheUseCase: CleanUpCacheUseCase,
) : ViewModel() {
    private val _cacheSettingsUiState =
        MutableStateFlow<CacheSettingsUiState>(CacheSettingsUiState.Loading)

    val cacheSettingsUiState: StateFlow<CacheSettingsUiState> =
        _cacheSettingsUiState.asStateFlow()

    init {
        viewModelScope.launch {
            _cacheSettingsUiState.value =
                CacheSettingsUiState.Success(cacheInfo = getCacheInfoUseCase())
        }
    }

    fun cleanUpCache() {
        val currentState = _cacheSettingsUiState.value as? CacheSettingsUiState.Success

        if (currentState == null || currentState.isCleaningUp) return

        viewModelScope.launch {
            _cacheSettingsUiState.value = currentState.copy(isCleaningUp = true)

            val freedSize = cleanUpCacheUseCase()

            _cacheSettingsUiState.value = CacheSettingsUiState.Success(
                cacheInfo = getCacheInfoUseCase(),
                freedSize = freedSize,
            )
        }
    }
}
