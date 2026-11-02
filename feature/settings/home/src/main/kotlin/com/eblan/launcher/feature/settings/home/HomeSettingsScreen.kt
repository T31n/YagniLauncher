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
import com.eblan.launcher.domain.model.userdata.PagerTransitionEffect
import com.eblan.launcher.feature.settings.home.dialog.EditDockCornerRadiusDialog
import com.eblan.launcher.feature.settings.home.dialog.EditDockGridDialog
import com.eblan.launcher.feature.settings.home.dialog.EditDockHeightDialog
import com.eblan.launcher.feature.settings.home.dialog.EditDockPaddingDialog
import com.eblan.launcher.feature.settings.home.dialog.EditGridDialog
import com.eblan.launcher.feature.settings.home.model.HomeSettingsUiState
import com.eblan.launcher.ui.dialog.ColorPickerDialog
import com.eblan.launcher.ui.dialog.RadioOptionsDialog
import com.eblan.launcher.ui.settings.CustomBackgroundColor
import com.eblan.launcher.ui.settings.GridItemSettings
import com.eblan.launcher.ui.settings.SettingsCategoryText
import com.eblan.launcher.ui.settings.SettingsColumnItem
import com.eblan.launcher.ui.settings.SettingsSwitchItem
import com.eblan.launcher.ui.settings.settingsFirstItemShape
import com.eblan.launcher.ui.settings.settingsLastItemShape
import com.eblan.launcher.ui.settings.settingsMiddleItemShape
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
    var showDockCustomBackgroundColorDialog by remember { mutableStateOf(false) }
    var showDockPaddingDialog by remember { mutableStateOf(false) }
    var showDockCornerRadiusDialog by remember { mutableStateOf(false) }
    var showPagerTransitionEffectDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .fillMaxSize()
            .padding(15.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        SettingsColumnItem(
            title = stringResource(commonR.string.grid),
            subtitle = "${homeSettings.columns}x${homeSettings.rows}",
            shape = settingsFirstItemShape,
            onClick = {
                showGridDialog = true
            },
        )

        SettingsColumnItem(
            title = stringResource(R.string.pager_transition_effect),
            subtitle = homeSettings.pagerTransitionEffect.getTitle(),
            shape = settingsMiddleItemShape,
            onClick = {
                showPagerTransitionEffectDialog = true
            },
        )

        SettingsSwitchItem(
            title = stringResource(R.string.infinite_scrolling),
            subtitle = stringResource(R.string.seamless_loop_page_scroll),
            checked = homeSettings.infiniteScroll,
            shape = settingsMiddleItemShape,
            onCheckedChange = {
                onUpdateHomeSettings(
                    homeSettings.copy(infiniteScroll = it),
                )
            },
        )

        SettingsSwitchItem(
            title = stringResource(R.string.wallpaper_scrolling),
            subtitle = stringResource(R.string.scroll_wallpaper_across_pages),
            checked = homeSettings.wallpaperScroll,
            shape = settingsMiddleItemShape,
            onCheckedChange = {
                onUpdateHomeSettings(
                    homeSettings.copy(wallpaperScroll = it),
                )
            },
        )

        SettingsSwitchItem(
            title = stringResource(R.string.lock_screen_orientation),
            subtitle = stringResource(R.string.prevent_rotation_when_device_orientation_changes),
            checked = homeSettings.lockScreenOrientation,
            shape = settingsMiddleItemShape,
            onCheckedChange = {
                onUpdateHomeSettings(
                    homeSettings.copy(lockScreenOrientation = it),
                )
            },
        )

        SettingsSwitchItem(
            title = stringResource(R.string.add_new_apps),
            subtitle = stringResource(R.string.add_new_apps_to_home_screen),
            checked = homeSettings.addNewAppsToHomeScreen,
            shape = settingsMiddleItemShape,
            onCheckedChange = {
                onUpdateHomeSettings(
                    homeSettings.copy(addNewAppsToHomeScreen = it),
                )
            },
        )

        SettingsSwitchItem(
            title = stringResource(R.string.show_page_indicator),
            subtitle = stringResource(R.string.show_an_indicator_for_the_current_page),
            checked = homeSettings.showPageIndicator,
            shape = settingsLastItemShape,
            onCheckedChange = {
                onUpdateHomeSettings(
                    homeSettings.copy(showPageIndicator = it),
                )
            },
        )

        SettingsCategoryText(text = stringResource(R.string.dock))

        SettingsColumnItem(
            title = stringResource(R.string.dock_grid),
            subtitle = "${homeSettings.dockColumns}x${homeSettings.dockRows}",
            shape = settingsFirstItemShape,
            onClick = {
                showDockGridDialog = true
            },
        )

        SettingsColumnItem(
            title = stringResource(R.string.dock_height),
            subtitle = "${homeSettings.dockHeight}",
            shape = settingsMiddleItemShape,
            onClick = {
                showDockHeightDialog = true
            },
        )

        SettingsSwitchItem(
            title = stringResource(R.string.dock_infinite_scroll),
            subtitle = stringResource(R.string.seamless_loop_page_scroll),
            checked = homeSettings.dockInfiniteScroll,
            shape = settingsMiddleItemShape,
            onCheckedChange = {
                onUpdateHomeSettings(
                    homeSettings.copy(dockInfiniteScroll = it),
                )
            },
        )

        CustomBackgroundColor(
            title = stringResource(R.string.dock_background_color),
            customBackgroundColor = homeSettings.dockCustomBackgroundColor,
            shape = settingsMiddleItemShape,
            onClick = {
                showDockCustomBackgroundColorDialog = true
            },
        )

        SettingsColumnItem(
            title = stringResource(R.string.dock_padding),
            subtitle = "${homeSettings.dockPadding}",
            shape = settingsMiddleItemShape,
            onClick = {
                showDockPaddingDialog = true
            },
        )

        SettingsColumnItem(
            title = stringResource(R.string.dock_corner_radius),
            subtitle = stringResource(R.string.set_the_radius_for_each_dock_corner),
            shape = settingsMiddleItemShape,
            onClick = {
                showDockCornerRadiusDialog = true
            },
        )

        SettingsSwitchItem(
            title = stringResource(R.string.add_bottom_padding),
            subtitle = stringResource(R.string.add_the_bottom_padding_of_the_dock),
            checked = homeSettings.addDockBottomPadding,
            shape = settingsLastItemShape,
            onCheckedChange = {
                onUpdateHomeSettings(
                    homeSettings.copy(addDockBottomPadding = it),
                )
            },
        )

        GridItemSettings(
            // Grid item
            gridItemSettings = homeSettings.gridItemSettings,
            // Actions
            onUpdateGridItemSettings = {
                onUpdateHomeSettings(
                    homeSettings.copy(gridItemSettings = it),
                )
            },
        )
    }

    if (showGridDialog) {
        EditGridDialog(
            // Grid Dimensions
            columns = homeSettings.columns,
            rows = homeSettings.rows,
            // Callbacks
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

    if (showPagerTransitionEffectDialog) {
        RadioOptionsDialog(
            // Dialog Content
            title = stringResource(R.string.pager_transition_effect),
            options = PagerTransitionEffect.entries,
            selected = homeSettings.pagerTransitionEffect,
            // Callbacks
            label = { it.getTitle() },
            onDismissRequest = {
                showPagerTransitionEffectDialog = false
            },
            onUpdateClick = {
                onUpdateHomeSettings(homeSettings.copy(pagerTransitionEffect = it))
            },
        )
    }

    if (showDockGridDialog) {
        EditDockGridDialog(
            // Grid
            dockColumns = homeSettings.dockColumns,
            dockRows = homeSettings.dockRows,
            // Callbacks
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
            // Layout
            dockHeight = homeSettings.dockHeight,
            // Actions
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

    if (showDockCustomBackgroundColorDialog) {
        ColorPickerDialog(
            // Content
            title = stringResource(R.string.dock_background_color),
            customColor = homeSettings.dockCustomBackgroundColor,
            // Callbacks
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
            // Padding
            padding = homeSettings.dockPadding,
            // Callbacks
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
            // Corner Radii
            dockTopStartCornerRadius = homeSettings.dockTopStartCornerRadius,
            dockTopEndCornerRadius = homeSettings.dockTopEndCornerRadius,
            dockBottomStartCornerRadius = homeSettings.dockBottomStartCornerRadius,
            dockBottomEndCornerRadius = homeSettings.dockBottomEndCornerRadius,
            // Callbacks
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
private fun PagerTransitionEffect.getTitle(): String = when (this) {
    PagerTransitionEffect.None -> stringResource(R.string.transition_effect_none)
    PagerTransitionEffect.Fade -> stringResource(R.string.transition_effect_fade)
    PagerTransitionEffect.ZoomOut -> stringResource(R.string.transition_effect_zoom_out)
    PagerTransitionEffect.Depth -> stringResource(R.string.transition_effect_depth)
    PagerTransitionEffect.Cube -> stringResource(R.string.transition_effect_cube)
    PagerTransitionEffect.Flip -> stringResource(R.string.transition_effect_flip)
    PagerTransitionEffect.Parallax -> stringResource(R.string.transition_effect_parallax)
    PagerTransitionEffect.Stack -> stringResource(R.string.transition_effect_stack)
}
