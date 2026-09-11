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
package com.eblan.launcher.feature.settings.home

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
import com.eblan.launcher.domain.model.userdata.HomeSettings
import com.eblan.launcher.feature.settings.home.dialog.EditDockCornerRadiusDialog
import com.eblan.launcher.feature.settings.home.dialog.EditDockGridDialog
import com.eblan.launcher.feature.settings.home.dialog.EditDockHeightDialog
import com.eblan.launcher.feature.settings.home.dialog.EditDockPaddingDialog
import com.eblan.launcher.feature.settings.home.dialog.EditFolderCellDimensionDialog
import com.eblan.launcher.feature.settings.home.dialog.EditFolderMaxGridDialog
import com.eblan.launcher.feature.settings.home.dialog.EditGridDialog
import com.eblan.launcher.feature.settings.home.model.HomeSettingsUiState
import com.eblan.launcher.ui.dialog.BackgroundColorDialog
import com.eblan.launcher.ui.dialog.ColorPickerDialog
import com.eblan.launcher.ui.dialog.EditCornerRadiusDialog
import com.eblan.launcher.ui.dialog.getBackgroundColorTitle
import com.eblan.launcher.ui.model.SettingsItem
import com.eblan.launcher.ui.settings.GridItemSettings
import com.eblan.launcher.ui.settings.SettingsCategoryText
import com.eblan.launcher.ui.settings.SettingsItems
import com.eblan.launcher.common.R as commonR

@Composable
internal fun HomeSettingsRoute(
    modifier: Modifier = Modifier,
    viewModel: HomeSettingsViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit,
) {
    val homeSettingsUiState by viewModel.homeSettingsUiState.collectAsStateWithLifecycle()

    HomeSettingsScreen(
        modifier = modifier,
        homeSettingsUiState = homeSettingsUiState,
        onNavigateUp = onNavigateUp,
        onUpdateHomeSettings = viewModel::updateHomeSettings,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeSettingsScreen(
    modifier: Modifier = Modifier,
    homeSettingsUiState: HomeSettingsUiState,
    onNavigateUp: () -> Unit,
    onUpdateHomeSettings: (HomeSettings) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(commonR.string.home))
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
            if (homeSettingsUiState is HomeSettingsUiState.Success) {
                Success(
                    homeSettings = homeSettingsUiState.homeSettings,
                    onUpdateHomeSettings = onUpdateHomeSettings,
                )
            }
        }
    }
}

@Composable
private fun Success(
    modifier: Modifier = Modifier,
    homeSettings: HomeSettings,
    onUpdateHomeSettings: (HomeSettings) -> Unit,
) {
    var showGridDialog by remember { mutableStateOf(false) }

    var showDockGridDialog by remember { mutableStateOf(false) }

    var showDockHeightDialog by remember { mutableStateOf(false) }

    var showFolderCellDimensionDialog by remember { mutableStateOf(false) }

    var showFolderMaxGridDialog by remember { mutableStateOf(false) }

    var showFolderCornerRadiusGridDialog by remember { mutableStateOf(false) }

    var showFolderBackgroundColorDialog by remember { mutableStateOf(false) }

    var showDockCustomBackgroundColorDialog by remember { mutableStateOf(false) }

    var showDockPaddingDialog by remember { mutableStateOf(false) }

    var showDockCornerRadiusDialog by remember { mutableStateOf(false) }

    val homeSettingsItems = buildHomeSettingsItems(
        homeSettings = homeSettings,
        onGridClick = {
            showGridDialog = true
        },
        onUpdateHomeSettings = onUpdateHomeSettings,
    )

    val dockHomeSettingsItems = buildDockHomeSettingsItems(
        homeSettings = homeSettings,
        onDockGridClick = {
            showDockGridDialog = true
        },
        onDockHeightClick = {
            showDockHeightDialog = true
        },
        onUpdateHomeSettings = onUpdateHomeSettings,
        onDockCustomBackgroundColorClick = {
            showDockCustomBackgroundColorDialog = true
        },
        onDockPaddingClick = {
            showDockPaddingDialog = true
        },
        onDockCornerRadiusClick = {
            showDockCornerRadiusDialog = true
        },
    )

    val folderHomeSettingsItems = buildFolderHomeSettingsItems(
        homeSettings = homeSettings,
        onFolderCellDimensionClick = {
            showFolderCellDimensionDialog = true
        },
        onFolderMaxGridClick = {
            showFolderMaxGridDialog = true
        },
        onFolderCornerRadiusClick = {
            showFolderCornerRadiusGridDialog = true
        },
        onFolderBackgroundColorClick = {
            showFolderBackgroundColorDialog = true
        },
    )

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .fillMaxSize()
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        SettingsItems(items = homeSettingsItems)

        SettingsCategoryText(text = stringResource(R.string.dock))

        SettingsItems(items = dockHomeSettingsItems)

        SettingsCategoryText(text = stringResource(R.string.folder))

        SettingsItems(items = folderHomeSettingsItems)

        GridItemSettings(
            gridItemSettings = homeSettings.gridItemSettings,
            onUpdateGridItemSettings = {
                onUpdateHomeSettings(
                    homeSettings.copy(gridItemSettings = it),
                )
            },
        )
    }

    if (showGridDialog) {
        EditGridDialog(
            columns = homeSettings.columns,
            rows = homeSettings.rows,
            onDismissRequest = {
                showGridDialog = false
            },
            onUpdateGrid = { columns, rows ->
                onUpdateHomeSettings(
                    homeSettings.copy(
                        columns = columns,
                        rows = rows,
                    ),
                )
            },
        )
    }

    if (showDockGridDialog) {
        EditDockGridDialog(
            dockColumns = homeSettings.dockColumns,
            dockRows = homeSettings.dockRows,
            onDismissRequest = {
                showDockGridDialog = false
            },
            onUpdateDockGrid = { dockColumns, dockRows ->
                onUpdateHomeSettings(
                    homeSettings.copy(
                        dockColumns = dockColumns,
                        dockRows = dockRows,
                    ),
                )
            },
        )
    }

    if (showDockHeightDialog) {
        EditDockHeightDialog(
            dockHeight = homeSettings.dockHeight,
            onDismissRequest = {
                showDockHeightDialog = false
            },
            onUpdateDockHeight = {
                onUpdateHomeSettings(
                    homeSettings.copy(
                        dockHeight = it,
                    ),
                )

                showDockHeightDialog = false
            },
        )
    }

    if (showFolderCellDimensionDialog) {
        EditFolderCellDimensionDialog(
            folderCellWidth = homeSettings.folderCellWidth,
            folderCellHeight = homeSettings.folderCellHeight,
            onDismissRequest = {
                showFolderCellDimensionDialog = false
            },
            onUpdateFolderCellDimension = { folderCellWidth, folderCellHeight ->
                onUpdateHomeSettings(
                    homeSettings.copy(
                        folderCellWidth = folderCellWidth,
                        folderCellHeight = folderCellHeight,
                    ),
                )
            },
        )
    }

    if (showFolderMaxGridDialog) {
        EditFolderMaxGridDialog(
            maxFolderColumns = homeSettings.maxFolderColumns,
            maxFolderRows = homeSettings.maxFolderRows,
            onDismissRequest = {
                showFolderMaxGridDialog = false
            },
            onUpdateFolderMaxGrid = { maxFolderColumns, maxFolderRows ->
                onUpdateHomeSettings(
                    homeSettings.copy(
                        maxFolderColumns = maxFolderColumns,
                        maxFolderRows = maxFolderRows,
                    ),
                )
            },
        )
    }

    if (showFolderCornerRadiusGridDialog) {
        EditCornerRadiusDialog(
            cornerRadius = homeSettings.folderCornerRadius,
            onDismissRequest = {
                showFolderCornerRadiusGridDialog = false
            },
            onUpdateCornerRadius = {
                onUpdateHomeSettings(
                    homeSettings.copy(
                        folderCornerRadius = it,
                    ),
                )
            },
        )
    }

    if (showFolderBackgroundColorDialog) {
        BackgroundColorDialog(
            title = stringResource(commonR.string.background_color),
            backgroundColor = homeSettings.folderBackgroundColor,
            customBackgroundColor = homeSettings.customFolderBackgroundColor,
            onDismissRequest = {
                showFolderBackgroundColorDialog = false
            },
            onUpdateClick = { backgroundColor, customColor ->
                onUpdateHomeSettings(
                    homeSettings.copy(
                        folderBackgroundColor = backgroundColor,
                        customFolderBackgroundColor = customColor,
                    ),
                )
            },
        )
    }

    if (showDockCustomBackgroundColorDialog) {
        ColorPickerDialog(
            title = stringResource(R.string.dock_background_color),
            customColor = homeSettings.dockCustomBackgroundColor,
            onDismissRequest = {
                showDockCustomBackgroundColorDialog = false
            },
            onSelectColor = {
                onUpdateHomeSettings(homeSettings.copy(dockCustomBackgroundColor = it))
            },
        )
    }

    if (showDockPaddingDialog) {
        EditDockPaddingDialog(
            padding = homeSettings.dockPadding,
            onDismissRequest = {
                showDockPaddingDialog = false
            },
            onUpdatePadding = {
                onUpdateHomeSettings(
                    homeSettings.copy(
                        dockPadding = it,
                    ),
                )
            },
        )
    }

    if (showDockCornerRadiusDialog) {
        EditDockCornerRadiusDialog(
            dockTopStartCornerRadius = homeSettings.dockTopStartCornerRadius,
            dockTopEndCornerRadius = homeSettings.dockTopEndCornerRadius,
            dockBottomStartCornerRadius = homeSettings.dockBottomStartCornerRadius,
            dockBottomEndCornerRadius = homeSettings.dockBottomEndCornerRadius,
            onDismissRequest = {
                showDockCornerRadiusDialog = false
            },
            onUpdateCornerRadius = { dockTopStartCornerRadius, dockTopEndCornerRadius, dockBottomStartCornerRadius, dockBottomEndCornerRadius ->
                onUpdateHomeSettings(
                    homeSettings.copy(
                        dockTopStartCornerRadius = dockTopStartCornerRadius,
                        dockTopEndCornerRadius = dockTopEndCornerRadius,
                        dockBottomStartCornerRadius = dockBottomStartCornerRadius,
                        dockBottomEndCornerRadius = dockBottomEndCornerRadius,
                    ),
                )
            },

        )
    }
}

@Composable
private fun buildHomeSettingsItems(
    homeSettings: HomeSettings,
    onGridClick: () -> Unit,
    onUpdateHomeSettings: (HomeSettings) -> Unit,
): List<SettingsItem> = buildList {
    add(
        SettingsItem.Column(
            title = stringResource(commonR.string.grid),
            subtitle = "${homeSettings.columns}x${homeSettings.rows}",
            onClick = onGridClick,
        ),
    )

    add(
        SettingsItem.Switch(
            checked = homeSettings.infiniteScroll,
            title = stringResource(R.string.infinite_scrolling),
            subtitle = stringResource(R.string.seamless_loop_page_scroll),
            onClick = {
                onUpdateHomeSettings(
                    homeSettings.copy(infiniteScroll = !homeSettings.infiniteScroll),
                )
            },
            onCheckedChange = {
                onUpdateHomeSettings(
                    homeSettings.copy(infiniteScroll = it),
                )
            },
        ),
    )

    add(
        SettingsItem.Switch(
            checked = homeSettings.wallpaperScroll,
            title = stringResource(R.string.wallpaper_scrolling),
            subtitle = stringResource(R.string.scroll_wallpaper_across_pages),
            onClick = {
                onUpdateHomeSettings(
                    homeSettings.copy(wallpaperScroll = !homeSettings.wallpaperScroll),
                )
            },
            onCheckedChange = {
                onUpdateHomeSettings(
                    homeSettings.copy(wallpaperScroll = it),
                )
            },
        ),
    )

    add(
        SettingsItem.Switch(
            checked = homeSettings.lockScreenOrientation,
            title = stringResource(R.string.lock_screen_orientation),
            subtitle = stringResource(R.string.prevent_rotation_when_device_orientation_changes),
            onClick = {
                onUpdateHomeSettings(
                    homeSettings.copy(lockScreenOrientation = !homeSettings.lockScreenOrientation),
                )
            },
            onCheckedChange = {
                onUpdateHomeSettings(
                    homeSettings.copy(lockScreenOrientation = it),
                )
            },
        ),
    )

    add(
        SettingsItem.Switch(
            checked = homeSettings.addNewAppsToHomeScreen,
            title = stringResource(R.string.add_new_apps),
            subtitle = stringResource(R.string.add_new_apps_to_home_screen),
            onClick = {
                onUpdateHomeSettings(
                    homeSettings.copy(addNewAppsToHomeScreen = !homeSettings.addNewAppsToHomeScreen),
                )
            },
            onCheckedChange = {
                onUpdateHomeSettings(
                    homeSettings.copy(addNewAppsToHomeScreen = it),
                )
            },
        ),
    )

    add(
        SettingsItem.Switch(
            checked = homeSettings.showPageIndicator,
            title = stringResource(R.string.show_page_indicator),
            subtitle = stringResource(R.string.show_an_indicator_for_the_current_page),
            onClick = {
                onUpdateHomeSettings(
                    homeSettings.copy(showPageIndicator = !homeSettings.showPageIndicator),
                )
            },
            onCheckedChange = {
                onUpdateHomeSettings(
                    homeSettings.copy(showPageIndicator = it),
                )
            },
        ),
    )
}

@Composable
private fun buildDockHomeSettingsItems(
    homeSettings: HomeSettings,
    onDockGridClick: () -> Unit,
    onDockHeightClick: () -> Unit,
    onDockCustomBackgroundColorClick: () -> Unit,
    onDockPaddingClick: () -> Unit,
    onDockCornerRadiusClick: () -> Unit,
    onUpdateHomeSettings: (HomeSettings) -> Unit,
): List<SettingsItem> = buildList {
    add(
        SettingsItem.Column(
            title = stringResource(R.string.dock_grid),
            subtitle = "${homeSettings.dockColumns}x${homeSettings.dockRows}",
            onClick = onDockGridClick,
        ),
    )

    add(
        SettingsItem.Column(
            title = stringResource(R.string.dock_height),
            subtitle = "${homeSettings.dockHeight}",
            onClick = onDockHeightClick,
        ),
    )

    add(
        SettingsItem.Switch(
            checked = homeSettings.dockInfiniteScroll,
            title = stringResource(R.string.dock_infinite_scroll),
            subtitle = stringResource(R.string.seamless_loop_page_scroll),
            onClick = {
                onUpdateHomeSettings(
                    homeSettings.copy(dockInfiniteScroll = !homeSettings.dockInfiniteScroll),
                )
            },
            onCheckedChange = {
                onUpdateHomeSettings(
                    homeSettings.copy(dockInfiniteScroll = it),
                )
            },
        ),
    )

    add(
        SettingsItem.CustomBackgroundColor(
            title = stringResource(R.string.dock_background_color),
            customBackgroundColor = homeSettings.dockCustomBackgroundColor,
            onClick = onDockCustomBackgroundColorClick,
        ),
    )

    add(
        SettingsItem.Column(
            title = stringResource(R.string.dock_padding),
            subtitle = "${homeSettings.dockPadding}",
            onClick = onDockPaddingClick,
        ),
    )

    add(
        SettingsItem.Column(
            title = stringResource(R.string.dock_corner_radius),
            subtitle = stringResource(R.string.set_the_radius_for_each_dock_corner),
            onClick = onDockCornerRadiusClick,
        ),
    )
}

@Composable
private fun buildFolderHomeSettingsItems(
    homeSettings: HomeSettings,
    onFolderCellDimensionClick: () -> Unit,
    onFolderMaxGridClick: () -> Unit,
    onFolderCornerRadiusClick: () -> Unit,
    onFolderBackgroundColorClick: () -> Unit,
): List<SettingsItem> = buildList {
    add(
        SettingsItem.Column(
            title = stringResource(R.string.folder_cell_dimension),
            subtitle = "${homeSettings.folderCellWidth}x${homeSettings.folderCellHeight}",
            onClick = onFolderCellDimensionClick,
        ),
    )

    add(
        SettingsItem.Column(
            title = stringResource(R.string.folder_max_grid),
            subtitle = "${homeSettings.maxFolderColumns}x${homeSettings.maxFolderRows}",
            onClick = onFolderMaxGridClick,
        ),
    )

    add(
        SettingsItem.Column(
            title = stringResource(R.string.folder_corner_radius),
            subtitle = "${homeSettings.folderCornerRadius}",
            onClick = onFolderCornerRadiusClick,
        ),
    )

    add(
        SettingsItem.Column(
            title = stringResource(R.string.folder_background_color),
            subtitle = homeSettings.folderBackgroundColor.getBackgroundColorTitle(),
            onClick = onFolderBackgroundColorClick,
        ),
    )
}
