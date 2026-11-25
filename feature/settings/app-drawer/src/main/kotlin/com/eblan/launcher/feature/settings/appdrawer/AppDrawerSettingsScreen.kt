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
import com.eblan.launcher.designsystem.theme.EblanShapes
import com.eblan.launcher.designsystem.theme.EblanSpacing
import com.eblan.launcher.domain.model.application.EblanApplicationInfo
import com.eblan.launcher.domain.model.application.EblanApplicationInfoTag
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfo
import com.eblan.launcher.domain.model.userdata.AppDrawerSettings
import com.eblan.launcher.domain.model.userdata.AppDrawerType
import com.eblan.launcher.domain.model.userdata.BackgroundColor
import com.eblan.launcher.domain.model.userdata.SearchBarType
import com.eblan.launcher.feature.settings.appdrawer.dialog.EditHorizontalGridDialog
import com.eblan.launcher.feature.settings.appdrawer.dialog.EditVerticalGridDialog
import com.eblan.launcher.feature.settings.appdrawer.dialog.HiddenEblanApplicationInfosDialog
import com.eblan.launcher.feature.settings.appdrawer.dialog.ManageFoldersDialog
import com.eblan.launcher.feature.settings.appdrawer.dialog.ManageTagsDialog
import com.eblan.launcher.feature.settings.appdrawer.model.AppDrawerSettingsUiState
import com.eblan.launcher.ui.dialog.ColorPickerDialog
import com.eblan.launcher.ui.settings.GridItemSettings
import com.eblan.launcher.ui.settings.SettingsColumnItem
import com.eblan.launcher.ui.settings.SettingsDropdownItem
import com.eblan.launcher.ui.settings.SettingsSwitchItem
import com.eblan.launcher.ui.settings.getTitle
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
    var showBackgroundColorPickerDialog by remember { mutableStateOf(false) }
    var showManageTagsDialog by remember { mutableStateOf(false) }
    var showManageFoldersDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .fillMaxSize()
            .padding(EblanSpacing.large),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        SettingsColumnItem(
            // Content
            title = stringResource(R.string.manage_tags),
            subtitle = stringResource(R.string.sort_update_and_delete_tags),
            // Layout
            shape = EblanShapes.first,
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
            shape = EblanShapes.middle,
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
            label = { currentAppDrawerType ->
                currentAppDrawerType.getTitle()
            },
            // Layout
            shape = EblanShapes.middle,
            // Actions
            onSelectedChange = { currentAppDrawerType ->
                onUpdateAppDrawerSettings(
                    appDrawerSettings.copy(appDrawerType = currentAppDrawerType),
                )
            },
        )

        when (appDrawerSettings.appDrawerType) {
            AppDrawerType.Vertical -> SettingsColumnItem(
                // Content
                title = stringResource(commonR.string.grid),
                subtitle = "${appDrawerSettings.appDrawerColumns}x${appDrawerSettings.appDrawerRowsHeight}",
                // Layout
                shape = EblanShapes.middle,
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
                shape = EblanShapes.middle,
                // Actions
                onClick = {
                    showHorizontalGridDialog = true
                },
            )
        }

        SettingsDropdownItem(
            // Content
            title = stringResource(R.string.search_bar_type),
            options = SearchBarType.entries,
            selected = appDrawerSettings.searchBarType,
            label = { currentSearchBarType ->
                currentSearchBarType.getTitle()
            },
            // Layout
            shape = EblanShapes.middle,
            // Actions
            onSelectedChange = { currentSearchBarType ->
                onUpdateAppDrawerSettings(
                    appDrawerSettings.copy(searchBarType = currentSearchBarType),
                )
            },
        )

        SettingsDropdownItem(
            // Content
            title = stringResource(commonR.string.background_color),
            options = BackgroundColor.entries,
            selected = appDrawerSettings.backgroundColor,
            label = { currentBackgroundColor ->
                currentBackgroundColor.getTitle()
            },
            // Layout
            shape = EblanShapes.middle,
            // Actions
            onSelectedChange = { currentBackgroundColor ->
                if (currentBackgroundColor == BackgroundColor.Custom) {
                    showBackgroundColorPickerDialog = true
                } else {
                    onUpdateAppDrawerSettings(
                        appDrawerSettings.copy(backgroundColor = currentBackgroundColor),
                    )
                }
            },
        )

        SettingsColumnItem(
            // Content
            title = stringResource(R.string.hidden_applications),
            subtitle = stringResource(R.string.hide_selected_apps_from_the_app_drawer),
            // Layout
            shape = EblanShapes.middle,
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
            shape = EblanShapes.middle,
            // Callbacks
            onCheckedChange = { currentExcludeTaggedApps ->
                onUpdateAppDrawerSettings(
                    appDrawerSettings.copy(
                        excludeTaggedApps = currentExcludeTaggedApps,
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
            shape = EblanShapes.middle,
            // Callbacks
            onCheckedChange = { currentShowKeyboard ->
                onUpdateAppDrawerSettings(
                    appDrawerSettings.copy(
                        showKeyboard = currentShowKeyboard,
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
            shape = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                EblanShapes.last
            } else {
                EblanShapes.middle
            },
            // Callbacks
            onCheckedChange = { currentFuzzySearch ->
                onUpdateAppDrawerSettings(
                    appDrawerSettings.copy(
                        fuzzySearch = currentFuzzySearch,
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
                shape = EblanShapes.last,
                // Callbacks
                onCheckedChange = { currentBlurBehind ->
                    onUpdateAppDrawerSettings(
                        appDrawerSettings.copy(
                            blurBehind = currentBlurBehind,
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
            onUpdateGridItemSettings = { currentGridItemSettings ->
                onUpdateAppDrawerSettings(
                    appDrawerSettings.copy(
                        gridItemSettings = currentGridItemSettings,
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

    if (showBackgroundColorPickerDialog) {
        ColorPickerDialog(
            // Content
            title = stringResource(commonR.string.background_color),
            // State
            customColor = appDrawerSettings.customBackgroundColor,
            showAlphaSlider = true,
            // Actions
            onDismissRequest = {
                showBackgroundColorPickerDialog = false
            },
            onSelectColor = { currentCustomBackgroundColor ->
                onUpdateAppDrawerSettings(
                    appDrawerSettings.copy(
                        backgroundColor = BackgroundColor.Custom,
                        customBackgroundColor = currentCustomBackgroundColor,
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

@Composable
private fun SearchBarType.getTitle(): String = when (this) {
    SearchBarType.Top -> stringResource(R.string.top)
    SearchBarType.Floating -> stringResource(R.string.floating)
    SearchBarType.None -> stringResource(commonR.string.none)
}
