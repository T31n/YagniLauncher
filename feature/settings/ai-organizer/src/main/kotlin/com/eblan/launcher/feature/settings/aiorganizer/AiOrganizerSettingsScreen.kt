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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.eblan.launcher.domain.model.aiorganizer.AiOrganizerStatus
import com.eblan.launcher.domain.model.aiorganizer.ApplyAiOrganizationResult
import com.eblan.launcher.domain.model.userdata.AiOrganizerSettings
import com.eblan.launcher.feature.settings.aiorganizer.model.AiOrganizerSettingsUiState
import kotlinx.coroutines.flow.Flow
import com.eblan.launcher.common.R as commonR

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
        messages = viewModel.messages,
        onNavigateUp = onNavigateUp,
        onSave = viewModel::updateAiOrganizerSettings,
        onApply = {
            viewModel.applyAiOrganization(restore = false)
        },
        onRestore = {
            viewModel.applyAiOrganization(restore = true)
        },
        onBackup = viewModel::backupAiOrganization,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AiOrganizerSettingsScreen(
    modifier: Modifier = Modifier,
    aiOrganizerSettingsUiState: AiOrganizerSettingsUiState,
    messages: Flow<AiOrganizerSettingsMessage>,
    onNavigateUp: () -> Unit,
    onSave: (AiOrganizerSettings) -> Unit,
    onApply: () -> Unit,
    onRestore: () -> Unit,
    onBackup: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    val backupSavedMessage = stringResource(R.string.ai_organizer_backup_saved)
    val backupFailedMessage = stringResource(R.string.ai_organizer_backup_failed)
    val settingsSavedMessage = stringResource(R.string.ai_organizer_settings_saved)
    val settingsSaveFailedMessage = stringResource(R.string.ai_organizer_settings_save_failed)

    LaunchedEffect(Unit) {
        messages.collect { message ->
            snackbarHostState.currentSnackbarData?.dismiss()

            snackbarHostState.showSnackbar(
                message = when (message) {
                    AiOrganizerSettingsMessage.SettingsSaved -> settingsSavedMessage
                    AiOrganizerSettingsMessage.SettingsSaveFailed -> settingsSaveFailedMessage
                    AiOrganizerSettingsMessage.BackupSaved -> backupSavedMessage
                    AiOrganizerSettingsMessage.BackupFailed -> backupFailedMessage
                },
            )
        }
    }

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
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
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
                    isBusy = aiOrganizerSettingsUiState.isBusy,
                    onSave = onSave,
                    onApply = onApply,
                    onRestore = onRestore,
                    onBackup = onBackup,
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
    isBusy: Boolean,
    onSave: (AiOrganizerSettings) -> Unit,
    onApply: () -> Unit,
    onRestore: () -> Unit,
    onBackup: () -> Unit,
) {
    var endpoint by remember(key1 = aiOrganizerSettings) { mutableStateOf(aiOrganizerSettings.endpoint) }
    var apiKey by remember(key1 = aiOrganizerSettings) { mutableStateOf(aiOrganizerSettings.apiKey) }
    var model by remember(key1 = aiOrganizerSettings) { mutableStateOf(aiOrganizerSettings.model) }
    var systemInstructions by remember(key1 = aiOrganizerSettings) { mutableStateOf(aiOrganizerSettings.systemInstructions) }

    val hasChanges = endpoint.trim() != aiOrganizerSettings.endpoint ||
        apiKey.trim() != aiOrganizerSettings.apiKey ||
        model.trim() != aiOrganizerSettings.model ||
        systemInstructions.trim() != aiOrganizerSettings.systemInstructions

    // System instructions may be blank because the default instructions are used instead.
    val canSave = hasChanges &&
        endpoint.isNotBlank() &&
        apiKey.isNotBlank() &&
        model.isNotBlank()

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
            supportingText = if (endpoint.trim().startsWith("http://", ignoreCase = true)) {
                { Text(text = stringResource(R.string.ai_organizer_cleartext_warning)) }
            } else {
                null
            },
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
            supportingText = { Text(text = stringResource(commonR.string.ai_organizer_default_system_instructions)) },
        )

        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = canSave,
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

        if (!status.hasBackup) {
            Text(
                text = stringResource(R.string.ai_organizer_backup_required),
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = onApply,
            enabled = !isBusy && status.hasNewHome && status.hasBackup,
        ) {
            Text(text = stringResource(R.string.ai_organizer_apply))
        }

        Text(
            text = if (status.hasBackup) {
                stringResource(R.string.ai_organizer_backup_available)
            } else {
                stringResource(R.string.ai_organizer_no_backup)
            },
            style = MaterialTheme.typography.bodyMedium,
        )

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = onBackup,
            enabled = !isBusy,
        ) {
            Text(text = stringResource(R.string.ai_organizer_backup))
        }

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = onRestore,
            enabled = !isBusy && status.hasBackup,
        ) {
            Text(text = stringResource(R.string.ai_organizer_restore))
        }

        val applyMessageRes = applyResult?.messageRes()
        if (applyMessageRes != null) {
            Text(
                text = stringResource(applyMessageRes),
                color = if (applyResult == ApplyAiOrganizationResult.RESTORED ||
                    applyResult == ApplyAiOrganizationResult.APPLIED
                ) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.error
                },
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

private fun ApplyAiOrganizationResult.messageRes(): Int? = when (this) {
    ApplyAiOrganizationResult.APPLIED -> R.string.ai_organizer_applied
    ApplyAiOrganizationResult.RESTORED -> R.string.ai_organizer_restored
    ApplyAiOrganizationResult.MISSING_FILE -> R.string.ai_organizer_apply_missing_file
    ApplyAiOrganizationResult.INVALID_FILE -> R.string.ai_organizer_apply_invalid_file
    ApplyAiOrganizationResult.FAILED -> R.string.ai_organizer_apply_failed
}
