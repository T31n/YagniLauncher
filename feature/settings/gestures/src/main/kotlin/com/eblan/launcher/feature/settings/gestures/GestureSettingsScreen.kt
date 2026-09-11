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
package com.eblan.launcher.feature.settings.gestures

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.domain.model.userdata.GestureSettings
import com.eblan.launcher.feature.settings.gestures.model.GesturesSettingsUiState
import com.eblan.launcher.ui.settings.EblanActionSettings
import com.eblan.launcher.common.R as commonR

@Composable
internal fun GestureSettingsRoute(
    modifier: Modifier = Modifier,
    viewModel: GestureSettingsViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit,
) {
    val gesturesSettingsUiState by viewModel.gesturesSettingsUiState.collectAsStateWithLifecycle()

    GestureSettingsScreen(
        modifier = modifier,
        gesturesSettingsUiState = gesturesSettingsUiState,
        onNavigateUp = onNavigateUp,
        onUpdateGestureSettings = viewModel::updateGestureSettings,
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun GestureSettingsScreen(
    modifier: Modifier = Modifier,
    gesturesSettingsUiState: GesturesSettingsUiState,
    onNavigateUp: () -> Unit,
    onUpdateGestureSettings: (GestureSettings) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(commonR.string.gestures))
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = EblanLauncherIcons.ArrowBack,
                            contentDescription = null,
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            if (gesturesSettingsUiState is GesturesSettingsUiState.Success) {
                EblanActionSettings(
                    modifier = Modifier.padding(10.dp),
                    doubleTap = gesturesSettingsUiState.gestureSettings.doubleTap,
                    swipeUp = gesturesSettingsUiState.gestureSettings.swipeUp,
                    swipeDown = gesturesSettingsUiState.gestureSettings.swipeDown,
                    eblanApplicationInfos = gesturesSettingsUiState.eblanApplicationInfos,
                    onUpdateDoubleTap = {
                        onUpdateGestureSettings(
                            gesturesSettingsUiState.gestureSettings.copy(
                                doubleTap = it,
                            ),
                        )
                    },
                    onUpdateSwipeUp = {
                        onUpdateGestureSettings(
                            gesturesSettingsUiState.gestureSettings.copy(
                                swipeUp = it,
                            ),
                        )
                    },
                    onUpdateSwipeDown = {
                        onUpdateGestureSettings(
                            gesturesSettingsUiState.gestureSettings.copy(
                                swipeDown = it,
                            ),
                        )
                    },
                )
            }
        }
    }
}
