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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.domain.model.aiorganizer.AiOrganizerDefaults
import com.eblan.launcher.domain.model.aiorganizer.ApplyAiOrganizationResult
import com.eblan.launcher.domain.model.userdata.AiOrganizerSettings
import com.eblan.launcher.domain.usecase.aiorganizer.AiOrganizerStatus
import com.eblan.launcher.feature.settings.aiorganizer.model.AiOrganizerSettingsUiState

@Composable
internal fun AiOrganizerSettingsRoute(
    modifier: Modifier = Modifier,
    viewModel: AiOrganizerSettingsViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit,
) {
    val aiOrganizerSettingsUiState by viewModel.aiOrganizerSettingsUiState.collectAsStateWithLifecycle()

    LifecycleResumeEffect(Unit) {
        viewModel.refreshStatus()

        onPauseOrDispose {}
    }

    AiOrganizerSettingsScreen(
        modifier = modifier,
        aiOrganizerSettingsUiState = aiOrganizerSettingsUiState,
        onNavigateUp = onNavigateUp,
        onSave = viewModel::updateAiOrganizerSettings,
        onApply = {
            viewModel.applyAiOrganization(restore = false)
        },
        onRestore = {
            viewModel.applyAiOrganization(restore = true)
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AiOrganizerSettingsScreen(
    modifier: Modifier = Modifier,
    aiOrganizerSettingsUiState: AiOrganizerSettingsUiState,
    onNavigateUp: () -> Unit,
    onSave: (AiOrganizerSettings) -> Unit,
    onApply: () -> Unit,
    onRestore: () -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.ai_organizer))
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
            if (aiOrganizerSettingsUiState is AiOrganizerSettingsUiState.Success) {
                Success(
                    aiOrganizerSettings = aiOrganizerSettingsUiState.aiOrganizerSettings,
                    status = aiOrganizerSettingsUiState.status,
                    applyResult = aiOrganizerSettingsUiState.applyResult,
                    onSave = onSave,
                    onApply = onApply,
                    onRestore = onRestore,
                )
            }
        }
    }
}

@Composable
private fun Success(
    modifier: Modifier = Modifier,
    aiOrganizerSettings: AiOrganizerSettings,
    status: AiOrganizerStatus,
    applyResult: ApplyAiOrganizationResult?,
    onSave: (AiOrganizerSettings) -> Unit,
    onApply: () -> Unit,
    onRestore: () -> Unit,
) {
    var endpoint by remember(aiOrganizerSettings) { mutableStateOf(aiOrganizerSettings.endpoint) }
    var apiKey by remember(aiOrganizerSettings) { mutableStateOf(aiOrganizerSettings.apiKey) }
    var model by remember(aiOrganizerSettings) { mutableStateOf(aiOrganizerSettings.model) }
    var systemInstructions by remember(aiOrganizerSettings) {
        mutableStateOf(aiOrganizerSettings.systemInstructions)
    }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .fillMaxSize()
            .padding(15.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(R.string.ai_organizer_data_disclosure),
            style = MaterialTheme.typography.bodyMedium,
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = endpoint,
            onValueChange = { endpoint = it },
            label = { Text(text = stringResource(R.string.ai_organizer_endpoint)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = apiKey,
            onValueChange = { apiKey = it },
            label = { Text(text = stringResource(R.string.ai_organizer_api_key)) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = model,
            onValueChange = { model = it },
            label = { Text(text = stringResource(R.string.ai_organizer_model)) },
            singleLine = true,
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = systemInstructions,
            onValueChange = { systemInstructions = it },
            label = { Text(text = stringResource(R.string.ai_organizer_system_instructions)) },
            supportingText = { Text(text = AiOrganizerDefaults.SYSTEM_INSTRUCTIONS) },
        )

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                onSave(
                    AiOrganizerSettings(
                        endpoint = endpoint.trim(),
                        apiKey = apiKey.trim(),
                        model = model.trim(),
                        systemInstructions = systemInstructions.trim(),
                    ),
                )
            },
        ) {
            Text(text = stringResource(R.string.ai_organizer_save))
        }

        Text(
            text = if (status.hasNewHome) {
                stringResource(R.string.ai_organizer_new_home_available)
            } else {
                stringResource(R.string.ai_organizer_no_new_home)
            },
            style = MaterialTheme.typography.bodyMedium,
        )

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = onApply,
            enabled = status.hasNewHome,
        ) {
            Text(text = stringResource(R.string.ai_organizer_apply))
        }

        Text(
            text = if (status.hasLastHome) {
                stringResource(R.string.ai_organizer_last_home_available)
            } else {
                stringResource(R.string.ai_organizer_no_last_home)
            },
            style = MaterialTheme.typography.bodyMedium,
        )

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = onRestore,
            enabled = status.hasLastHome,
        ) {
            Text(text = stringResource(R.string.ai_organizer_restore))
        }

        val applyErrorRes = applyResult?.errorMessageRes()
        if (applyErrorRes != null) {
            Text(
                text = stringResource(applyErrorRes),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

private fun ApplyAiOrganizationResult.errorMessageRes(): Int? = when (this) {
    ApplyAiOrganizationResult.APPLIED,
    ApplyAiOrganizationResult.RESTORED,
    -> null

    ApplyAiOrganizationResult.MISSING_FILE -> R.string.ai_organizer_apply_missing_file

    ApplyAiOrganizationResult.INVALID_FILE -> R.string.ai_organizer_apply_invalid_file

    ApplyAiOrganizationResult.FAILED -> R.string.ai_organizer_apply_failed
}
