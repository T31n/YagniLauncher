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
import com.eblan.launcher.domain.model.AppDrawerSettings
import com.eblan.launcher.domain.model.AppDrawerType
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.feature.settings.appdrawer.dialog.EditHorizontalGridDialog
import com.eblan.launcher.feature.settings.appdrawer.dialog.EditVerticalGridDialog
import com.eblan.launcher.feature.settings.appdrawer.dialog.HiddenEblanApplicationInfosDialog
import com.eblan.launcher.feature.settings.appdrawer.model.AppDrawerSettingsUiState
import com.eblan.launcher.ui.dialog.BackgroundColorDialog
import com.eblan.launcher.ui.dialog.RadioOptionsDialog
import com.eblan.launcher.ui.dialog.getBackgroundColorTitle
import com.eblan.launcher.ui.model.SettingsItem
import com.eblan.launcher.ui.settings.GridItemSettings
import com.eblan.launcher.ui.settings.SettingsItems
import com.eblan.launcher.common.R as commonR

@Composable
internal fun AppDrawerSettingsRoute(
    modifier: Modifier = Modifier,
    viewModel: AppDrawerSettingsViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit,
) {
    val appDrawerSettingsUiState by viewModel.appDrawerSettingsUiState.collectAsStateWithLifecycle()

    AppDrawerSettingsScreen(
        modifier = modifier,
        appDrawerSettingsUiState = appDrawerSettingsUiState,
        onNavigateUp = onNavigateUp,
        onUpdateAppDrawerSettings = viewModel::updateAppDrawerSettings,
        onUpdateEblanApplicationInfo = viewModel::updateEblanApplicationInfo,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AppDrawerSettingsScreen(
    modifier: Modifier = Modifier,
    appDrawerSettingsUiState: AppDrawerSettingsUiState,
    onNavigateUp: () -> Unit,
    onUpdateAppDrawerSettings: (AppDrawerSettings) -> Unit,
    onUpdateEblanApplicationInfo: (EblanApplicationInfo) -> Unit,
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
                    appDrawerSettings = appDrawerSettingsUiState.appDrawerSettings,
                    eblanApplicationInfos = appDrawerSettingsUiState.eblanApplicationInfos,
                    onUpdateAppDrawerSettings = onUpdateAppDrawerSettings,
                    onUpdateEblanApplicationInfo = onUpdateEblanApplicationInfo,
                )
            }
        }
    }
}

@Composable
private fun Success(
    modifier: Modifier = Modifier,
    appDrawerSettings: AppDrawerSettings,
    eblanApplicationInfos: List<EblanApplicationInfo>,
    onUpdateAppDrawerSettings: (AppDrawerSettings) -> Unit,
    onUpdateEblanApplicationInfo: (EblanApplicationInfo) -> Unit,
) {
    var showAppDrawerTypeDialog by remember { mutableStateOf(false) }

    var showVerticalGridDialog by remember { mutableStateOf(false) }

    var showHorizontalGridDialog by remember { mutableStateOf(false) }

    var showHiddenEblanApplicationInfosDialog by remember { mutableStateOf(false) }

    var showBackgroundColorDialog by remember { mutableStateOf(false) }

    val items = buildAppDrawerSettingsItems(
        appDrawerSettings = appDrawerSettings,
        onAppDrawerTypeClick = {
            showAppDrawerTypeDialog = true
        },
        onVerticalGridClick = {
            showVerticalGridDialog = true
        },
        onHorizontalGridClick = {
            showHorizontalGridDialog = true
        },
        onBackgroundColorClick = {
            showBackgroundColorDialog = true
        },
        onHiddenApplicationsClick = {
            showHiddenEblanApplicationInfosDialog = true
        },
        onUpdateAppDrawerSettings = onUpdateAppDrawerSettings,
    )

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .fillMaxSize()
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        SettingsItems(items = items)

        GridItemSettings(
            gridItemSettings = appDrawerSettings.gridItemSettings,
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
            title = stringResource(R.string.app_drawer_type),
            options = AppDrawerType.entries,
            selected = appDrawerSettings.appDrawerType,
            label = {
                it.name
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
            appDrawerColumns = appDrawerSettings.appDrawerColumns,
            appDrawerRowsHeight = appDrawerSettings.appDrawerRowsHeight,
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
            horizontalAppDrawerColumns = appDrawerSettings.horizontalAppDrawerColumns,
            horizontalAppDrawerRows = appDrawerSettings.horizontalAppDrawerRows,
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
            eblanApplicationInfos = eblanApplicationInfos,
            onDismissRequest = {
                showHiddenEblanApplicationInfosDialog = false
            },
            onUpdateEblanApplicationInfo = onUpdateEblanApplicationInfo,
        )
    }

    if (showBackgroundColorDialog) {
        BackgroundColorDialog(
            title = stringResource(commonR.string.background_color),
            backgroundColor = appDrawerSettings.backgroundColor,
            customBackgroundColor = appDrawerSettings.customBackgroundColor,
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
}

@Composable
private fun buildAppDrawerSettingsItems(
    appDrawerSettings: AppDrawerSettings,
    onAppDrawerTypeClick: () -> Unit,
    onVerticalGridClick: () -> Unit,
    onHorizontalGridClick: () -> Unit,
    onBackgroundColorClick: () -> Unit,
    onHiddenApplicationsClick: () -> Unit,
    onUpdateAppDrawerSettings: (AppDrawerSettings) -> Unit,
): List<SettingsItem> = buildList {
    add(
        SettingsItem.Column(
            title = stringResource(R.string.app_drawer_type),
            subtitle = appDrawerSettings.appDrawerType.name,
            onClick = onAppDrawerTypeClick,
        ),
    )

    when (appDrawerSettings.appDrawerType) {
        AppDrawerType.Vertical -> add(
            SettingsItem.Column(
                title = stringResource(commonR.string.grid),
                subtitle = "${appDrawerSettings.appDrawerColumns}x${appDrawerSettings.appDrawerRowsHeight}",
                onClick = onVerticalGridClick,
            ),
        )

        AppDrawerType.Horizontal -> add(
            SettingsItem.Column(
                title = stringResource(commonR.string.grid),
                subtitle = "${appDrawerSettings.horizontalAppDrawerColumns}x${appDrawerSettings.horizontalAppDrawerRows}",
                onClick = onHorizontalGridClick,
            ),
        )

        AppDrawerType.List -> Unit
    }

    add(
        SettingsItem.Column(
            title = stringResource(commonR.string.background_color),
            subtitle = appDrawerSettings.backgroundColor.getBackgroundColorTitle(),
            onClick = onBackgroundColorClick,
        ),
    )

    add(
        SettingsItem.Column(
            title = stringResource(R.string.hidden_applications),
            subtitle = stringResource(R.string.hide_selected_apps_from_the_app_drawer),
            onClick = onHiddenApplicationsClick,
        ),
    )

    add(
        SettingsItem.Switch(
            checked = appDrawerSettings.excludeTaggedApps,
            title = stringResource(R.string.exclude_tagged_apps),
            subtitle = stringResource(R.string.hide_apps_marked_with_selected_tags),
            onClick = {
                onUpdateAppDrawerSettings(
                    appDrawerSettings.copy(
                        excludeTaggedApps = !appDrawerSettings.excludeTaggedApps,
                    ),
                )
            },
            onCheckedChange = {
                onUpdateAppDrawerSettings(
                    appDrawerSettings.copy(
                        excludeTaggedApps = it,
                    ),
                )
            },
        ),
    )

    add(
        SettingsItem.Switch(
            checked = appDrawerSettings.showKeyboard,
            title = stringResource(R.string.show_keyboard),
            subtitle = stringResource(R.string.show_keyboard_when_app_drawer_opens),
            onClick = {
                onUpdateAppDrawerSettings(
                    appDrawerSettings.copy(
                        showKeyboard = !appDrawerSettings.showKeyboard,
                    ),
                )
            },
            onCheckedChange = {
                onUpdateAppDrawerSettings(
                    appDrawerSettings.copy(
                        showKeyboard = it,
                    ),
                )
            },
        ),
    )

    add(
        SettingsItem.Switch(
            checked = appDrawerSettings.fuzzySearch,
            title = stringResource(R.string.fuzzy_search),
            subtitle = stringResource(R.string.find_apps_even_with_typos_or_accented_characters),
            onClick = {
                onUpdateAppDrawerSettings(
                    appDrawerSettings.copy(
                        fuzzySearch = !appDrawerSettings.fuzzySearch,
                    ),
                )
            },
            onCheckedChange = {
                onUpdateAppDrawerSettings(
                    appDrawerSettings.copy(
                        fuzzySearch = it,
                    ),
                )
            },
        ),
    )

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        add(
            SettingsItem.Switch(
                checked = appDrawerSettings.blurBehind,
                title = stringResource(R.string.blur_behind),
                subtitle = stringResource(R.string.blurs_the_wallpaper_when_app_drawer_opens),
                onClick = {
                    onUpdateAppDrawerSettings(
                        appDrawerSettings.copy(
                            blurBehind = !appDrawerSettings.blurBehind,
                        ),
                    )
                },
                onCheckedChange = {
                    onUpdateAppDrawerSettings(
                        appDrawerSettings.copy(
                            blurBehind = it,
                        ),
                    )
                },
            ),
        )
    }
}
