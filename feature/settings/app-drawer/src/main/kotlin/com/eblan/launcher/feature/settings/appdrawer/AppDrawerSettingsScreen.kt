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
package com.eblan.launcher.feature.settings.appdrawer

import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.domain.model.application.EblanApplicationInfo
import com.eblan.launcher.domain.model.application.EblanApplicationInfoTag
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfo
import com.eblan.launcher.domain.model.userdata.AppDrawerSettings
import com.eblan.launcher.domain.model.userdata.AppDrawerType
import com.eblan.launcher.feature.settings.appdrawer.dialog.EditHorizontalGridDialog
import com.eblan.launcher.feature.settings.appdrawer.dialog.EditVerticalGridDialog
import com.eblan.launcher.feature.settings.appdrawer.dialog.HiddenEblanApplicationInfosDialog
import com.eblan.launcher.feature.settings.appdrawer.dialog.ManageFoldersDialog
import com.eblan.launcher.feature.settings.appdrawer.dialog.ManageTagsDialog
import com.eblan.launcher.feature.settings.appdrawer.model.AppDrawerSettingsUiState
import com.eblan.launcher.ui.dialog.BackgroundColorDialog
import com.eblan.launcher.ui.dialog.getTitle
import com.eblan.launcher.ui.settings.GridItemSettings
import com.eblan.launcher.ui.settings.SettingsColumnItem
import com.eblan.launcher.ui.settings.SettingsDropdownItem
import com.eblan.launcher.ui.settings.SettingsSwitchItem
import com.eblan.launcher.ui.settings.settingsFirstItemShape
import com.eblan.launcher.ui.settings.settingsMiddleItemShape
import com.eblan.launcher.common.R as commonR

@Composable
internal fun AppDrawerSettingsRoute(
    modifier: Modifier = Modifier,
    viewModel: AppDrawerSettingsViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit,
) {
    val appDrawerSettingsUiState by viewModel.appDrawerSettingsUiState.collectAsStateWithLifecycle()
    val eblanApplicationInfoTags by viewModel.eblanApplicationInfoTags.collectAsStateWithLifecycle()
    val folderEblanApplicationInfos by viewModel.folderEblanApplicationInfos.collectAsStateWithLifecycle()

    AppDrawerSettingsScreen(
        // State
        appDrawerSettingsUiState = appDrawerSettingsUiState,
        eblanApplicationInfoTags = eblanApplicationInfoTags,
        folderEblanApplicationInfos = folderEblanApplicationInfos,
        modifier = modifier,
        // Actions
        onNavigateUp = onNavigateUp,
        onUpdateAppDrawerSettings = viewModel::updateAppDrawerSettings,
        onUpdateEblanApplicationInfo = viewModel::updateEblanApplicationInfo,
        onUpdateEblanApplicationInfoTags = viewModel::updateEblanApplicationInfoTags,
        onUpdateFolderEblanApplicationInfos = viewModel::updateFolderEblanApplicationInfos,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AppDrawerSettingsScreen(
    // State
    appDrawerSettingsUiState: AppDrawerSettingsUiState,
    eblanApplicationInfoTags: List<EblanApplicationInfoTag>,
    folderEblanApplicationInfos: List<FolderEblanApplicationInfo>,
    modifier: Modifier = Modifier,
    // Actions
    onNavigateUp: () -> Unit,
    onUpdateAppDrawerSettings: (AppDrawerSettings) -> Unit,
    onUpdateEblanApplicationInfo: (EblanApplicationInfo) -> Unit,
    onUpdateEblanApplicationInfoTags: (List<EblanApplicationInfoTag>) -> Unit,
    onUpdateFolderEblanApplicationInfos: (List<FolderEblanApplicationInfo>) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(commonR.string.app_drawer))
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
            if (appDrawerSettingsUiState is AppDrawerSettingsUiState.Success) {
                Success(
                    // Data
                    eblanApplicationInfos = appDrawerSettingsUiState.eblanApplicationInfos,
                    folderEblanApplicationInfos = folderEblanApplicationInfos,
                    eblanApplicationInfoTags = eblanApplicationInfoTags,
                    appDrawerSettings = appDrawerSettingsUiState.appDrawerSettings,
                    // Callbacks
                    onUpdateAppDrawerSettings = onUpdateAppDrawerSettings,
                    onUpdateEblanApplicationInfo = onUpdateEblanApplicationInfo,
                    onUpdateEblanApplicationInfoTags = onUpdateEblanApplicationInfoTags,
                    onUpdateFolderEblanApplicationInfos = onUpdateFolderEblanApplicationInfos,
                )
            }
        }
    }
}

@Composable
private fun Success(
    // Data
    eblanApplicationInfos: List<EblanApplicationInfo>,
    folderEblanApplicationInfos: List<FolderEblanApplicationInfo>,
    eblanApplicationInfoTags: List<EblanApplicationInfoTag>,
    appDrawerSettings: AppDrawerSettings,
    // Modifiers
    modifier: Modifier = Modifier,
    // Callbacks
    onUpdateAppDrawerSettings: (AppDrawerSettings) -> Unit,
    onUpdateEblanApplicationInfo: (EblanApplicationInfo) -> Unit,
    onUpdateEblanApplicationInfoTags: (List<EblanApplicationInfoTag>) -> Unit,
    onUpdateFolderEblanApplicationInfos: (List<FolderEblanApplicationInfo>) -> Unit,
) {
    var showVerticalGridDialog by remember { mutableStateOf(false) }
    var showHorizontalGridDialog by remember { mutableStateOf(false) }
    var showHiddenEblanApplicationInfosDialog by remember { mutableStateOf(false) }
    var showBackgroundColorDialog by remember { mutableStateOf(false) }
    var showManageTagsDialog by remember { mutableStateOf(false) }
    var showManageFoldersDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .fillMaxSize()
            .padding(15.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        SettingsColumnItem(
            // Content
            title = stringResource(R.string.manage_tags),
            subtitle = stringResource(R.string.sort_update_and_delete_tags),
            // Layout
            shape = settingsFirstItemShape,
            // Actions
            onClick = {
                showManageTagsDialog = true
            },
        )

        SettingsColumnItem(
            // Content
            title = stringResource(R.string.manage_folders),
            subtitle = stringResource(R.string.sort_update_and_delete_folders),
            // Layout
            shape = settingsMiddleItemShape,
            // Actions
            onClick = {
                showManageFoldersDialog = true
            },
        )

        SettingsDropdownItem(
            // Content
            title = stringResource(R.string.app_drawer_type),
            options = AppDrawerType.entries,
            selected = appDrawerSettings.appDrawerType,
            label = {
                it.getTitle()
            },
            // Layout
            shape = settingsMiddleItemShape,
            // Actions
            onSelectedChange = {
                onUpdateAppDrawerSettings(appDrawerSettings.copy(appDrawerType = it))
            },
        )

        when (appDrawerSettings.appDrawerType) {
            AppDrawerType.Vertical -> SettingsColumnItem(
                // Content
                title = stringResource(commonR.string.grid),
                subtitle = "${appDrawerSettings.appDrawerColumns}x${appDrawerSettings.appDrawerRowsHeight}",
                // Layout
                shape = settingsMiddleItemShape,
                // Actions
                onClick = {
                    showVerticalGridDialog = true
                },
            )

            AppDrawerType.Horizontal -> SettingsColumnItem(
                // Content
                title = stringResource(commonR.string.grid),
                subtitle = "${appDrawerSettings.horizontalAppDrawerColumns}x${appDrawerSettings.horizontalAppDrawerRows}",
                // Layout
                shape = settingsMiddleItemShape,
                // Actions
                onClick = {
                    showHorizontalGridDialog = true
                },
            )
        }

        SettingsColumnItem(
            // Content
            title = stringResource(commonR.string.background_color),
            subtitle = appDrawerSettings.backgroundColor.getTitle(),
            // Layout
            shape = settingsMiddleItemShape,
            // Actions
            onClick = {
                showBackgroundColorDialog = true
            },
        )

        SettingsColumnItem(
            // Content
            title = stringResource(R.string.hidden_applications),
            subtitle = stringResource(R.string.hide_selected_apps_from_the_app_drawer),
            // Layout
            shape = settingsMiddleItemShape,
            // Actions
            onClick = {
                showHiddenEblanApplicationInfosDialog = true
            },
        )

        SettingsSwitchItem(
            // Content
            title = stringResource(R.string.exclude_tagged_apps),
            subtitle = stringResource(R.string.hide_apps_marked_with_selected_tags),
            // State
            checked = appDrawerSettings.excludeTaggedApps,
            // Appearance
            shape = settingsMiddleItemShape,
            // Callbacks
            onCheckedChange = {
                onUpdateAppDrawerSettings(
                    appDrawerSettings.copy(
                        excludeTaggedApps = it,
                    ),
                )
            },
        )

        SettingsSwitchItem(
            // Content
            title = stringResource(R.string.show_keyboard),
            subtitle = stringResource(R.string.show_keyboard_when_app_drawer_opens),
            // State
            checked = appDrawerSettings.showKeyboard,
            // Appearance
            shape = settingsMiddleItemShape,
            // Callbacks
            onCheckedChange = {
                onUpdateAppDrawerSettings(
                    appDrawerSettings.copy(
                        showKeyboard = it,
                    ),
                )
            },
        )

        SettingsSwitchItem(
            // Content
            title = stringResource(R.string.fuzzy_search),
            subtitle = stringResource(R.string.find_apps_even_with_typos_or_accented_characters),
            // State
            checked = appDrawerSettings.fuzzySearch,
            // Appearance
            shape = settingsMiddleItemShape,
            // Callbacks
            onCheckedChange = {
                onUpdateAppDrawerSettings(
                    appDrawerSettings.copy(
                        fuzzySearch = it,
                    ),
                )
            },
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            SettingsSwitchItem(
                // Content
                title = stringResource(R.string.blur_behind),
                subtitle = stringResource(R.string.blurs_the_wallpaper_when_app_drawer_opens),
                // State
                checked = appDrawerSettings.blurBehind,
                // Appearance
                shape = settingsMiddleItemShape,
                // Callbacks
                onCheckedChange = {
                    onUpdateAppDrawerSettings(
                        appDrawerSettings.copy(
                            blurBehind = it,
                        ),
                    )
                },
            )
        }

        GridItemSettings(
            // Grid item
            gridItemSettings = appDrawerSettings.gridItemSettings,
            // Content
            title = stringResource(com.eblan.launcher.ui.R.string.grid_item),
            // Actions
            onUpdateGridItemSettings = {
                onUpdateAppDrawerSettings(
                    appDrawerSettings.copy(
                        gridItemSettings = it,
                    ),
                )
            },
        )
    }

    if (showVerticalGridDialog) {
        EditVerticalGridDialog(
            // Grid Properties
            appDrawerColumns = appDrawerSettings.appDrawerColumns,
            appDrawerRowsHeight = appDrawerSettings.appDrawerRowsHeight,
            // Callbacks
            onDismissRequest = {
                showVerticalGridDialog = false
            },
            onUpdateVerticalGrid = { appDrawerColumns, appDrawerRowsHeight ->
                onUpdateAppDrawerSettings(
                    appDrawerSettings.copy(
                        appDrawerColumns = appDrawerColumns,
                        appDrawerRowsHeight = appDrawerRowsHeight,
                    ),
                )
            },
        )
    }

    if (showHorizontalGridDialog) {
        EditHorizontalGridDialog(
            // Grid Dimensions
            horizontalAppDrawerColumns = appDrawerSettings.horizontalAppDrawerColumns,
            horizontalAppDrawerRows = appDrawerSettings.horizontalAppDrawerRows,
            // Callbacks
            onDismissRequest = {
                showHorizontalGridDialog = false
            },
            onUpdateHorizontalGrid = { horizontalAppDrawerColumns, horizontalAppDrawerRows ->
                onUpdateAppDrawerSettings(
                    appDrawerSettings.copy(
                        horizontalAppDrawerColumns = horizontalAppDrawerColumns,
                        horizontalAppDrawerRows = horizontalAppDrawerRows,
                    ),
                )
            },
        )
    }

    if (showHiddenEblanApplicationInfosDialog) {
        HiddenEblanApplicationInfosDialog(
            // Data
            eblanApplicationInfos = eblanApplicationInfos,
            // Callbacks
            onDismissRequest = {
                showHiddenEblanApplicationInfosDialog = false
            },
            onUpdateEblanApplicationInfo = onUpdateEblanApplicationInfo,
        )
    }

    if (showBackgroundColorDialog) {
        BackgroundColorDialog(
            // Content
            title = stringResource(commonR.string.background_color),
            // State
            backgroundColor = appDrawerSettings.backgroundColor,
            customBackgroundColor = appDrawerSettings.customBackgroundColor,
            // Actions
            onDismissRequest = {
                showBackgroundColorDialog = false
            },
            onUpdateClick = { backgroundColor, customColor ->
                onUpdateAppDrawerSettings(
                    appDrawerSettings.copy(
                        backgroundColor = backgroundColor,
                        customBackgroundColor = customColor,
                    ),
                )
            },
        )
    }

    if (showManageTagsDialog) {
        ManageTagsDialog(
            // Tags
            eblanApplicationInfoTags = eblanApplicationInfoTags,
            // Callbacks
            onDismissRequest = {
                showManageTagsDialog = false
            },
            onUpdateEblanApplicationInfoTags = onUpdateEblanApplicationInfoTags,
        )
    }

    if (showManageFoldersDialog) {
        ManageFoldersDialog(
            // Data
            folderEblanApplicationInfos = folderEblanApplicationInfos,
            // Callbacks
            onUpdateFolderEblanApplicationInfos = onUpdateFolderEblanApplicationInfos,
            onDismissRequest = {
                showManageFoldersDialog = false
            },
        )
    }
}

@Composable
private fun AppDrawerType.getTitle(): String = when (this) {
    AppDrawerType.Vertical -> stringResource(R.string.vertical)
    AppDrawerType.Horizontal -> stringResource(R.string.horizontal)
}
