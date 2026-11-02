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
import com.eblan.launcher.domain.model.userdata.ScrollBarType
import com.eblan.launcher.domain.model.userdata.SearchBarPosition
import com.eblan.launcher.feature.settings.appdrawer.dialog.EditHorizontalGridDialog
import com.eblan.launcher.feature.settings.appdrawer.dialog.EditVerticalGridDialog
import com.eblan.launcher.feature.settings.appdrawer.dialog.HiddenEblanApplicationInfosDialog
import com.eblan.launcher.feature.settings.appdrawer.dialog.ManageFoldersDialog
import com.eblan.launcher.feature.settings.appdrawer.dialog.ManageTagsDialog
import com.eblan.launcher.feature.settings.appdrawer.model.AppDrawerSettingsUiState
import com.eblan.launcher.ui.dialog.BackgroundColorDialog
import com.eblan.launcher.ui.dialog.RadioOptionsDialog
import com.eblan.launcher.ui.dialog.getTitle
import com.eblan.launcher.ui.settings.GridItemSettings
import com.eblan.launcher.ui.settings.SettingsColumnItem
import com.eblan.launcher.ui.settings.SettingsSwitchItem
import com.eblan.launcher.ui.settings.settingsFirstItemShape
import com.eblan.launcher.ui.settings.settingsLastItemShape
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
    var showAppDrawerTypeDialog by remember { mutableStateOf(false) }
    var showVerticalGridDialog by remember { mutableStateOf(false) }
    var showHorizontalGridDialog by remember { mutableStateOf(false) }
    var showHiddenEblanApplicationInfosDialog by remember { mutableStateOf(false) }
    var showBackgroundColorDialog by remember { mutableStateOf(false) }
    var showManageTagsDialog by remember { mutableStateOf(false) }
    var showManageFoldersDialog by remember { mutableStateOf(false) }
    var showSearchBarPositionDialog by remember { mutableStateOf(false) }
    var showScrollBarTypeDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .fillMaxSize()
            .padding(15.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        SettingsColumnItem(
            title = stringResource(R.string.manage_tags),
            subtitle = stringResource(R.string.sort_update_and_delete_tags),
            shape = settingsFirstItemShape,
            onClick = {
                showManageTagsDialog = true
            },
        )

        SettingsColumnItem(
            title = stringResource(R.string.manage_folders),
            subtitle = stringResource(R.string.sort_update_and_delete_folders),
            shape = settingsMiddleItemShape,
            onClick = {
                showManageFoldersDialog = true
            },
        )

        SettingsColumnItem(
            title = stringResource(R.string.app_drawer_type),
            subtitle = appDrawerSettings.appDrawerType.getTitle(),
            shape = settingsMiddleItemShape,
            onClick = {
                showAppDrawerTypeDialog = true
            },
        )

        when (appDrawerSettings.appDrawerType) {
            AppDrawerType.Vertical -> SettingsColumnItem(
                title = stringResource(commonR.string.grid),
                subtitle = "${appDrawerSettings.appDrawerColumns}x${appDrawerSettings.appDrawerRowsHeight}",
                shape = settingsMiddleItemShape,
                onClick = {
                    showVerticalGridDialog = true
                },
            )

            AppDrawerType.Horizontal -> SettingsColumnItem(
                title = stringResource(commonR.string.grid),
                subtitle = "${appDrawerSettings.horizontalAppDrawerColumns}x${appDrawerSettings.horizontalAppDrawerRows}",
                shape = settingsMiddleItemShape,
                onClick = {
                    showHorizontalGridDialog = true
                },
            )
        }

        SettingsColumnItem(
            title = stringResource(commonR.string.background_color),
            subtitle = appDrawerSettings.backgroundColor.getTitle(),
            shape = settingsMiddleItemShape,
            onClick = {
                showBackgroundColorDialog = true
            },
        )

        SettingsColumnItem(
            title = stringResource(R.string.hidden_applications),
            subtitle = stringResource(R.string.hide_selected_apps_from_the_app_drawer),
            shape = settingsMiddleItemShape,
            onClick = {
                showHiddenEblanApplicationInfosDialog = true
            },
        )

        SettingsSwitchItem(
            title = stringResource(R.string.exclude_tagged_apps),
            subtitle = stringResource(R.string.hide_apps_marked_with_selected_tags),
            checked = appDrawerSettings.excludeTaggedApps,
            shape = settingsMiddleItemShape,
            onCheckedChange = {
                onUpdateAppDrawerSettings(
                    appDrawerSettings.copy(
                        excludeTaggedApps = it,
                    ),
                )
            },
        )

        SettingsSwitchItem(
            title = stringResource(R.string.show_keyboard),
            subtitle = stringResource(R.string.show_keyboard_when_app_drawer_opens),
            checked = appDrawerSettings.showKeyboard,
            shape = settingsMiddleItemShape,
            onCheckedChange = {
                onUpdateAppDrawerSettings(
                    appDrawerSettings.copy(
                        showKeyboard = it,
                    ),
                )
            },
        )

        SettingsSwitchItem(
            title = stringResource(R.string.fuzzy_search),
            subtitle = stringResource(R.string.find_apps_even_with_typos_or_accented_characters),
            checked = appDrawerSettings.fuzzySearch,
            shape = settingsMiddleItemShape,
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
                title = stringResource(R.string.blur_behind),
                subtitle = stringResource(R.string.blurs_the_wallpaper_when_app_drawer_opens),
                checked = appDrawerSettings.blurBehind,
                shape = settingsMiddleItemShape,
                onCheckedChange = {
                    onUpdateAppDrawerSettings(
                        appDrawerSettings.copy(
                            blurBehind = it,
                        ),
                    )
                },
            )
        }

        SettingsColumnItem(
            title = stringResource(R.string.search_bar_position),
            subtitle = appDrawerSettings.searchBarPosition.getTitle(),
            shape = settingsMiddleItemShape,
            onClick = {
                showSearchBarPositionDialog = true
            },
        )

        SettingsColumnItem(
            title = stringResource(R.string.scroll_bar_type),
            subtitle = appDrawerSettings.scrollBarType.getTitle(),
            shape = settingsLastItemShape,
            onClick = {
                showScrollBarTypeDialog = true
            },
        )

        GridItemSettings(
            // Grid item
            gridItemSettings = appDrawerSettings.gridItemSettings,
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

    if (showAppDrawerTypeDialog) {
        RadioOptionsDialog(
            // Dialog Content
            title = stringResource(R.string.app_drawer_type),
            options = AppDrawerType.entries,
            selected = appDrawerSettings.appDrawerType,
            // Callbacks
            label = {
                it.getTitle()
            },
            onDismissRequest = {
                showAppDrawerTypeDialog = false
            },
            onUpdateClick = {
                onUpdateAppDrawerSettings(appDrawerSettings.copy(appDrawerType = it))
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

    if (showSearchBarPositionDialog) {
        RadioOptionsDialog(
            // Dialog Content
            title = stringResource(R.string.search_bar_position),
            options = SearchBarPosition.entries,
            selected = appDrawerSettings.searchBarPosition,
            // Callbacks
            label = {
                it.getTitle()
            },
            onDismissRequest = {
                showSearchBarPositionDialog = false
            },
            onUpdateClick = {
                onUpdateAppDrawerSettings(appDrawerSettings.copy(searchBarPosition = it))
            },
        )
    }

    if (showScrollBarTypeDialog) {
        RadioOptionsDialog(
            // Dialog Content
            title = stringResource(R.string.scroll_bar_type),
            options = ScrollBarType.entries,
            selected = appDrawerSettings.scrollBarType,
            // Callbacks
            label = {
                it.getTitle()
            },
            onDismissRequest = {
                showScrollBarTypeDialog = false
            },
            onUpdateClick = {
                onUpdateAppDrawerSettings(appDrawerSettings.copy(scrollBarType = it))
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
private fun SearchBarPosition.getTitle(): String = when (this) {
    SearchBarPosition.Top -> stringResource(R.string.top)
    SearchBarPosition.Bottom -> stringResource(R.string.bottom)
    SearchBarPosition.None -> stringResource(commonR.string.none)
}

@Composable
private fun ScrollBarType.getTitle(): String = when (this) {
    ScrollBarType.ScrollBar -> stringResource(R.string.scroll_bar)
    ScrollBarType.Alphabetical -> stringResource(R.string.alphabetical)
    ScrollBarType.None -> stringResource(commonR.string.none)
}
