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
package com.eblan.launcher.feature.settings.folder

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.domain.model.userdata.FolderSettings
import com.eblan.launcher.feature.settings.folder.dialog.EditFolderCellDimensionDialog
import com.eblan.launcher.feature.settings.folder.dialog.EditFolderMaxGridDialog
import com.eblan.launcher.feature.settings.folder.model.FolderSettingsUiState
import com.eblan.launcher.ui.dialog.BackgroundColorDialog
import com.eblan.launcher.ui.dialog.ColorPickerDialog
import com.eblan.launcher.ui.dialog.EditBorderWidthDialog
import com.eblan.launcher.ui.dialog.EditCornerRadiusDialog
import com.eblan.launcher.ui.dialog.getTitle
import com.eblan.launcher.ui.settings.SettingsColumnItem
import com.eblan.launcher.ui.settings.SettingsItemCard
import com.eblan.launcher.ui.settings.settingsFirstItemShape
import com.eblan.launcher.ui.settings.settingsLastItemShape
import com.eblan.launcher.ui.settings.settingsMiddleItemShape
import com.eblan.launcher.common.R as commonR

@Composable
internal fun FolderSettingsRoute(
    modifier: Modifier = Modifier,
    viewModel: FolderSettingsViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit,
) {
    val folderSettingsUiState by viewModel.folderSettingsUiState.collectAsStateWithLifecycle()

    FolderSettingsScreen(
        modifier = modifier,
        folderSettingsUiState = folderSettingsUiState,
        onNavigateUp = onNavigateUp,
        onUpdateFolderSettings = viewModel::updateFolderSettings,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun FolderSettingsScreen(
    modifier: Modifier = Modifier,
    folderSettingsUiState: FolderSettingsUiState,
    onNavigateUp: () -> Unit,
    onUpdateFolderSettings: (FolderSettings) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(commonR.string.folder))
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
            if (folderSettingsUiState is FolderSettingsUiState.Success) {
                Success(
                    folderSettings = folderSettingsUiState.folderSettings,
                    onUpdateFolderSettings = onUpdateFolderSettings,
                )
            }
        }
    }
}

@Composable
private fun Success(
    modifier: Modifier = Modifier,
    folderSettings: FolderSettings,
    onUpdateFolderSettings: (FolderSettings) -> Unit,
) {
    var showFolderCellDimensionDialog by remember { mutableStateOf(false) }

    var showFolderMaxGridDialog by remember { mutableStateOf(false) }

    var showFolderCornerRadiusGridDialog by remember { mutableStateOf(false) }

    var showFolderBackgroundColorDialog by remember { mutableStateOf(false) }

    var showFolderBorderColorDialog by remember { mutableStateOf(false) }

    var showFolderBorderWidthDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .fillMaxSize()
            .padding(15.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        SettingsColumnItem(
            // Content
            title = stringResource(R.string.folder_cell_dimension),
            subtitle = "${folderSettings.folderCellWidth}x${folderSettings.folderCellHeight}",
            // Layout
            shape = settingsFirstItemShape,
            // Actions
            onClick = {
                showFolderCellDimensionDialog = true
            },
        )

        SettingsColumnItem(
            // Content
            title = stringResource(R.string.folder_max_grid),
            subtitle = "${folderSettings.maxFolderColumns}x${folderSettings.maxFolderRows}",
            // Layout
            shape = settingsMiddleItemShape,
            // Actions
            onClick = {
                showFolderMaxGridDialog = true
            },
        )

        SettingsColumnItem(
            // Content
            title = stringResource(R.string.folder_corner_radius),
            subtitle = "${folderSettings.folderCornerRadius}",
            // Layout
            shape = settingsMiddleItemShape,
            // Actions
            onClick = {
                showFolderCornerRadiusGridDialog = true
            },
        )

        SettingsColumnItem(
            // Content
            title = stringResource(R.string.folder_background_color),
            subtitle = folderSettings.folderBackgroundColor.getTitle(),
            // Layout
            shape = settingsMiddleItemShape,
            // Actions
            onClick = {
                showFolderBackgroundColorDialog = true
            },
        )

        SettingsColumnItem(
            // Content
            title = stringResource(R.string.folder_border_width),
            subtitle = "${folderSettings.borderWidth}",
            // Layout
            shape = settingsMiddleItemShape,
            // Actions
            onClick = {
                showFolderBorderWidthDialog = true
            },
        )

        CustomBorderColor(
            // Content
            title = stringResource(R.string.folder_border_color),
            // Appearance
            customBorderColor = folderSettings.customFolderBorderColor,
            // Layout
            shape = settingsLastItemShape,
            // Actions
            onClick = {
                showFolderBorderColorDialog = true
            },
        )
    }

    if (showFolderCellDimensionDialog) {
        EditFolderCellDimensionDialog(
            // Dimensions
            folderCellWidth = folderSettings.folderCellWidth,
            folderCellHeight = folderSettings.folderCellHeight,
            // Callbacks
            onDismissRequest = {
                showFolderCellDimensionDialog = false
            },
            onUpdateFolderCellDimension = { folderCellWidth, folderCellHeight ->
                onUpdateFolderSettings(
                    folderSettings.copy(
                        folderCellWidth = folderCellWidth,
                        folderCellHeight = folderCellHeight,
                    ),
                )
            },
        )
    }

    if (showFolderMaxGridDialog) {
        EditFolderMaxGridDialog(
            // Grid Dimensions
            maxFolderColumns = folderSettings.maxFolderColumns,
            maxFolderRows = folderSettings.maxFolderRows,
            // Actions
            onDismissRequest = {
                showFolderMaxGridDialog = false
            },
            onUpdateFolderMaxGrid = { maxFolderColumns, maxFolderRows ->
                onUpdateFolderSettings(
                    folderSettings.copy(
                        maxFolderColumns = maxFolderColumns,
                        maxFolderRows = maxFolderRows,
                    ),
                )
            },
        )
    }

    if (showFolderCornerRadiusGridDialog) {
        EditCornerRadiusDialog(
            // State
            cornerRadius = folderSettings.folderCornerRadius,
            // Callbacks
            onDismissRequest = {
                showFolderCornerRadiusGridDialog = false
            },
            onUpdateCornerRadius = {
                onUpdateFolderSettings(
                    folderSettings.copy(
                        folderCornerRadius = it,
                    ),
                )
            },
        )
    }

    if (showFolderBackgroundColorDialog) {
        BackgroundColorDialog(
            // Content
            title = stringResource(commonR.string.background_color),
            // State
            backgroundColor = folderSettings.folderBackgroundColor,
            customBackgroundColor = folderSettings.customFolderBackgroundColor,
            // Actions
            onDismissRequest = {
                showFolderBackgroundColorDialog = false
            },
            onUpdateClick = { backgroundColor, customColor ->
                onUpdateFolderSettings(
                    folderSettings.copy(
                        folderBackgroundColor = backgroundColor,
                        customFolderBackgroundColor = customColor,
                    ),
                )
            },
        )
    }

    if (showFolderBorderColorDialog) {
        ColorPickerDialog(
            // Content
            title = stringResource(R.string.folder_border_color),
            // State
            customColor = folderSettings.customFolderBorderColor,
            // Callbacks
            onDismissRequest = {
                showFolderBorderColorDialog = false
            },
            onSelectColor = {
                onUpdateFolderSettings(
                    folderSettings.copy(
                        customFolderBorderColor = it,
                    ),
                )
            },
        )
    }

    if (showFolderBorderWidthDialog) {
        EditBorderWidthDialog(
            // Content
            title = stringResource(R.string.folder_border_width),
            // State
            borderWidth = folderSettings.borderWidth,
            // Callbacks
            onDismissRequest = {
                showFolderBorderWidthDialog = false
            },
            onUpdateBorderWidth = {
                onUpdateFolderSettings(
                    folderSettings.copy(
                        borderWidth = it,
                    ),
                )
            },
        )
    }
}

@Composable
private fun CustomBorderColor(
    // Item Data
    title: String,
    // Appearance
    customBorderColor: Int,
    shape: Shape,
    // Layout
    modifier: Modifier = Modifier,
    // Actions
    onClick: () -> Unit,
) {
    SettingsItemCard(
        // Modifier and Layout
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        shape = shape,
        // Actions and Content
        onClick = onClick,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
        )

        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = Color(customBorderColor),
                    shape = CircleShape,
                ),
        )
    }
}
