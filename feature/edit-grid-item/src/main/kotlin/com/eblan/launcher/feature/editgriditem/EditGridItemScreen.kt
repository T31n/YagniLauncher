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
package com.eblan.launcher.feature.editgriditem

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
import com.eblan.launcher.designsystem.component.VerticalSlideReveal
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.designsystem.theme.EblanShapes
import com.eblan.launcher.designsystem.theme.EblanSpacing
import com.eblan.launcher.domain.model.application.EblanApplicationInfo
import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.domain.model.grid.GridItemData
import com.eblan.launcher.domain.model.iconpackinfo.IconPackComponent
import com.eblan.launcher.domain.model.userdata.GeneralSettings
import com.eblan.launcher.feature.editgriditem.dialog.EditFolderLabelDialog
import com.eblan.launcher.feature.editgriditem.model.EditGridItemUiState
import com.eblan.launcher.ui.dialog.EditCustomLabelDialog
import com.eblan.launcher.ui.dialog.IconPackInfoFilesDialog
import com.eblan.launcher.ui.dialog.SelectApplicationsDialog
import com.eblan.launcher.ui.settings.CustomIcon
import com.eblan.launcher.ui.settings.EblanActionSettings
import com.eblan.launcher.ui.settings.GridItemSettings
import com.eblan.launcher.ui.settings.SettingsCategoryText
import com.eblan.launcher.ui.settings.SettingsColumnItem
import com.eblan.launcher.ui.settings.SettingsSwitchItem
import com.eblan.launcher.common.R as commonR

@Composable
internal fun EditGridItemRoute(
    modifier: Modifier = Modifier,
    viewModel: EditGridItemViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit,
) {
    val editUiState by viewModel.editGridItemUiState.collectAsStateWithLifecycle()
    val iconPackInfoComponents by viewModel.iconPackInfoComponents.collectAsStateWithLifecycle()
    val eblanApplicationInfos by viewModel.eblanApplicationInfos.collectAsStateWithLifecycle()

    EditGridItemScreen(
        // State and Data
        editGridItemUiState = editUiState,
        eblanApplicationInfos = eblanApplicationInfos,
        iconPackComponents = iconPackInfoComponents,
        modifier = modifier,
        // Actions
        onNavigateUp = onNavigateUp,
        onResetIconPackPackageName = viewModel::resetIconPackPackageName,
        onResetGridItemCustomIcon = viewModel::resetGridItemCustomIcon,
        onSearchIconPackInfoComponent = viewModel::searchIconPackInfoComponent,
        onUpdateGridItem = viewModel::updateGridItem,
        onUpdateIconPackPackageName = viewModel::updateIconPackPackageName,
        onUpdateGridItemCustomIcon = viewModel::updateGridItemCustomIcon,
        onUpdateGridItemCustomIconByteArray = viewModel::updateGridItemCustomIconByteArray,
        onAddApplicationInfosToFolder = viewModel::addApplicationInfosToFolder,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EditGridItemScreen(
    // State and Data
    editGridItemUiState: EditGridItemUiState,
    eblanApplicationInfos: List<EblanApplicationInfo>,
    iconPackComponents: List<IconPackComponent>,
    modifier: Modifier = Modifier,
    // Actions
    onNavigateUp: () -> Unit,
    onResetIconPackPackageName: () -> Unit,
    onResetGridItemCustomIcon: (GridItem) -> Unit,
    onSearchIconPackInfoComponent: (String) -> Unit,
    onUpdateGridItem: (GridItem) -> Unit,
    onUpdateIconPackPackageName: (String) -> Unit,
    onUpdateGridItemCustomIcon: (
        gridItem: GridItem,
        uri: String,
    ) -> Unit,
    onUpdateGridItemCustomIconByteArray: (
        gridItem: GridItem,
        byteArray: ByteArray,
    ) -> Unit,
    onAddApplicationInfosToFolder: (
        folderGridItem: GridItem,
        eblanApplicationInfos: List<EblanApplicationInfo>,
    ) -> Unit,
) {
    if (editGridItemUiState is EditGridItemUiState.Success && editGridItemUiState.gridItem != null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        val label = when (val data = editGridItemUiState.gridItem.data) {
                            is GridItemData.ApplicationInfo -> data.label
                            is GridItemData.ShortcutConfig -> data.activityLabel.toString()
                            is GridItemData.ShortcutInfo -> data.shortLabel
                            is GridItemData.Folder -> data.label
                            else -> error("Unsupported Grid Item")
                        }

                        Text(text = stringResource(commonR.string.edit, label))
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
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            ) {
                Success(
                    // Data
                    eblanApplicationInfos = eblanApplicationInfos,
                    gridItem = editGridItemUiState.gridItem,
                    generalSettings = editGridItemUiState.generalSettings,
                    iconPackComponents = iconPackComponents,
                    // Actions
                    onResetIconPackPackageName = onResetIconPackPackageName,
                    onSearchIconPackComponent = onSearchIconPackInfoComponent,
                    onUpdateGridItem = onUpdateGridItem,
                    onUpdateIconPackPackageName = onUpdateIconPackPackageName,
                    onResetGridItemCustomIcon = onResetGridItemCustomIcon,
                    onUpdateGridItemCustomIcon = onUpdateGridItemCustomIcon,
                    onUpdateGridItemCustomIconByteArray = onUpdateGridItemCustomIconByteArray,
                    onAddApplicationInfosToFolder = onAddApplicationInfosToFolder,
                )
            }
        }
    }
}

@Composable
private fun Success(
    // Data
    eblanApplicationInfos: List<EblanApplicationInfo>,
    gridItem: GridItem,
    generalSettings: GeneralSettings,
    iconPackComponents: List<IconPackComponent>,
    // Modifier
    modifier: Modifier = Modifier,
    // Actions
    onResetIconPackPackageName: () -> Unit,
    onSearchIconPackComponent: (String) -> Unit,
    onUpdateGridItem: (GridItem) -> Unit,
    onUpdateIconPackPackageName: (String) -> Unit,
    onResetGridItemCustomIcon: (GridItem) -> Unit,
    onUpdateGridItemCustomIcon: (
        gridItem: GridItem,
        uri: String,
    ) -> Unit,
    onUpdateGridItemCustomIconByteArray: (
        gridItem: GridItem,
        byteArray: ByteArray,
    ) -> Unit,
    onAddApplicationInfosToFolder: (
        folderGridItem: GridItem,
        eblanApplicationInfos: List<EblanApplicationInfo>,
    ) -> Unit,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .fillMaxSize()
            .padding(EblanSpacing.large),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        when (val data = gridItem.data) {
            is GridItemData.ApplicationInfo -> {
                EditApplicationInfo(
                    // Item Data
                    gridItem = gridItem,
                    data = data,
                    generalSettings = generalSettings,
                    // Icon Packs
                    iconPackComponents = iconPackComponents,
                    // Callbacks
                    onUpdateGridItem = onUpdateGridItem,
                    onResetGridItemCustomIcon = onResetGridItemCustomIcon,
                    onUpdateIconPackPackageName = onUpdateIconPackPackageName,
                    onResetIconPackPackageName = onResetIconPackPackageName,
                    onSearchIconPackComponent = onSearchIconPackComponent,
                    onUpdateGridItemCustomIcon = onUpdateGridItemCustomIcon,
                    onUpdateGridItemCustomIconByteArray = onUpdateGridItemCustomIconByteArray,
                )
            }

            is GridItemData.Folder -> {
                EditFolder(
                    // Grid Item
                    gridItem = gridItem,
                    data = data,
                    eblanApplicationInfos = eblanApplicationInfos,
                    generalSettings = generalSettings,
                    // Icon Packs
                    iconPackComponents = iconPackComponents,
                    // Actions
                    onUpdateGridItem = onUpdateGridItem,
                    onResetGridItemCustomIcon = onResetGridItemCustomIcon,
                    onResetIconPackPackageName = onResetIconPackPackageName,
                    onSearchIconPackInfoComponent = onSearchIconPackComponent,
                    onUpdateIconPackPackageName = onUpdateIconPackPackageName,
                    onUpdateGridItemCustomIcon = onUpdateGridItemCustomIcon,
                    onUpdateGridItemCustomIconByteArray = onUpdateGridItemCustomIconByteArray,
                    onAddApplicationInfosToFolder = onAddApplicationInfosToFolder,
                )
            }

            is GridItemData.ShortcutInfo -> {
                EditShortcutInfo(
                    // Shortcut Data
                    gridItem = gridItem,
                    data = data,
                    generalSettings = generalSettings,
                    // Icon Packs
                    iconPackComponents = iconPackComponents,
                    // Actions
                    onResetIconPackPackageName = onResetIconPackPackageName,
                    onSearchIconPackInfoComponent = onSearchIconPackComponent,
                    onUpdateGridItem = onUpdateGridItem,
                    onUpdateIconPackPackageName = onUpdateIconPackPackageName,
                    onResetGridItemCustomIcon = onResetGridItemCustomIcon,
                    onUpdateGridItemCustomIcon = onUpdateGridItemCustomIcon,
                    onUpdateGridItemCustomIconByteArray = onUpdateGridItemCustomIconByteArray,
                )
            }

            is GridItemData.ShortcutConfig -> {
                EditShortcutConfig(
                    // Grid Item
                    gridItem = gridItem,
                    data = data,
                    generalSettings = generalSettings,
                    // Icon Packs
                    iconPackComponents = iconPackComponents,
                    // Actions
                    onResetIconPackPackageName = onResetIconPackPackageName,
                    onSearchIconPackInfoComponent = onSearchIconPackComponent,
                    onUpdateGridItem = onUpdateGridItem,
                    onUpdateIconPackPackageName = onUpdateIconPackPackageName,
                    onResetGridItemCustomIcon = onResetGridItemCustomIcon,
                    onUpdateGridItemCustomIcon = onUpdateGridItemCustomIcon,
                    onUpdateGridItemCustomIconByteArray = onUpdateGridItemCustomIconByteArray,
                )
            }

            else -> Unit
        }

        VerticalSlideReveal(visible = gridItem.override) {
            GridItemSettings(
                // Grid item
                gridItemSettings = gridItem.gridItemSettings,
                // Content
                title = stringResource(com.eblan.launcher.ui.R.string.grid_item),
                // Actions
                onUpdateGridItemSettings = { currentGridItemSettings ->
                    onUpdateGridItem(gridItem.copy(gridItemSettings = currentGridItemSettings))
                },
            )
        }

        SettingsCategoryText(text = stringResource(R.string.grid_item_actions))

        EblanActionSettings(
            // Actions
            doubleTap = gridItem.doubleTap,
            swipeUp = gridItem.swipeUp,
            swipeDown = gridItem.swipeDown,
            // Application Info
            eblanApplicationInfos = eblanApplicationInfos,
            // Action Callbacks
            onUpdateDoubleTap = { currentDoubleTapEblanAction ->
                onUpdateGridItem(gridItem.copy(doubleTap = currentDoubleTapEblanAction))
            },
            onUpdateSwipeUp = { currentSwipeUpEblanAction ->
                onUpdateGridItem(gridItem.copy(swipeUp = currentSwipeUpEblanAction))
            },
            onUpdateSwipeDown = { currentSwipeDownEblanAction ->
                onUpdateGridItem(gridItem.copy(swipeDown = currentSwipeDownEblanAction))
            },
        )
    }
}

@Composable
private fun EditApplicationInfo(
    // Item Data
    gridItem: GridItem,
    data: GridItemData.ApplicationInfo,
    generalSettings: GeneralSettings,
    // Icon Packs
    iconPackComponents: List<IconPackComponent>,
    // Modifier
    modifier: Modifier = Modifier,
    // Callbacks
    onUpdateGridItem: (GridItem) -> Unit,
    onResetGridItemCustomIcon: (GridItem) -> Unit,
    onUpdateIconPackPackageName: (String) -> Unit,
    onResetIconPackPackageName: () -> Unit,
    onSearchIconPackComponent: (String) -> Unit,
    onUpdateGridItemCustomIcon: (
        gridItem: GridItem,
        uri: String,
    ) -> Unit,
    onUpdateGridItemCustomIconByteArray: (
        gridItem: GridItem,
        byteArray: ByteArray,
    ) -> Unit,
) {
    var showCustomLabelDialog by remember { mutableStateOf(false) }

    EditCustomIcon(
        gridItem = gridItem,
        customIcon = data.customIcon,
        generalSettings = generalSettings,
        iconPackComponents = iconPackComponents,
        modifier = modifier,
        onResetIconPackPackageName = onResetIconPackPackageName,
        onSearchIconPackInfoComponent = onSearchIconPackComponent,
        onUpdateIconPackPackageName = onUpdateIconPackPackageName,
        onResetGridItemCustomIcon = onResetGridItemCustomIcon,
        onUpdateGridItemCustomIcon = onUpdateGridItemCustomIcon,
        onUpdateGridItemCustomIconByteArray = onUpdateGridItemCustomIconByteArray,
    )

    SettingsColumnItem(
        // Content
        title = stringResource(commonR.string.custom_label),
        subtitle = data.customLabel ?: stringResource(commonR.string.none),
        // Layout
        shape = EblanShapes.middle,
        modifier = modifier,
        // Actions
        onClick = {
            showCustomLabelDialog = true
        },
    )

    EditOverrideSwitch(
        gridItem = gridItem,
        modifier = modifier,
        onUpdateGridItem = onUpdateGridItem,
    )

    if (showCustomLabelDialog) {
        EditCustomLabelDialog(
            // Content
            customLabel = data.customLabel,
            // Callbacks
            onDismissRequest = {
                showCustomLabelDialog = false
            },
            onUpdateCustomLabel = { currentCustomLabel ->
                val newData = data.copy(customLabel = currentCustomLabel)

                onUpdateGridItem(gridItem.copy(data = newData))
            },
        )
    }
}

@Composable
private fun EditFolder(
    // Grid Item
    gridItem: GridItem,
    data: GridItemData.Folder,
    eblanApplicationInfos: List<EblanApplicationInfo>,
    generalSettings: GeneralSettings,
    modifier: Modifier = Modifier,
    // Icon Packs
    iconPackComponents: List<IconPackComponent>,
    // Actions
    onUpdateGridItem: (GridItem) -> Unit,
    onResetGridItemCustomIcon: (GridItem) -> Unit,
    onResetIconPackPackageName: () -> Unit,
    onSearchIconPackInfoComponent: (String) -> Unit,
    onUpdateIconPackPackageName: (String) -> Unit,
    onUpdateGridItemCustomIcon: (
        gridItem: GridItem,
        uri: String,
    ) -> Unit,
    onUpdateGridItemCustomIconByteArray: (
        gridItem: GridItem,
        byteArray: ByteArray,
    ) -> Unit,
    onAddApplicationInfosToFolder: (
        folderGridItem: GridItem,
        eblanApplicationInfos: List<EblanApplicationInfo>,
    ) -> Unit,
) {
    var showEditLabelDialog by remember { mutableStateOf(false) }
    var showSelectApplicationsDialog by remember { mutableStateOf(false) }

    EditCustomIcon(
        gridItem = gridItem,
        customIcon = data.icon,
        generalSettings = generalSettings,
        iconPackComponents = iconPackComponents,
        modifier = modifier,
        onResetIconPackPackageName = onResetIconPackPackageName,
        onSearchIconPackInfoComponent = onSearchIconPackInfoComponent,
        onUpdateIconPackPackageName = onUpdateIconPackPackageName,
        onResetGridItemCustomIcon = onResetGridItemCustomIcon,
        onUpdateGridItemCustomIcon = onUpdateGridItemCustomIcon,
        onUpdateGridItemCustomIconByteArray = onUpdateGridItemCustomIconByteArray,
    )

    SettingsColumnItem(
        // Content
        title = stringResource(commonR.string.edit_label),
        subtitle = data.label,
        // Layout
        shape = EblanShapes.middle,
        modifier = modifier,
        // Actions
        onClick = {
            showEditLabelDialog = true
        },
    )

    SettingsColumnItem(
        // Content
        title = stringResource(commonR.string.add_applications),
        subtitle = stringResource(commonR.string.add_applications_to_folder),
        // Layout
        shape = EblanShapes.middle,
        modifier = modifier,
        // Actions
        onClick = {
            showSelectApplicationsDialog = true
        },
    )

    EditOverrideSwitch(
        gridItem = gridItem,
        modifier = modifier,
        onUpdateGridItem = onUpdateGridItem,
    )

    if (showEditLabelDialog) {
        EditFolderLabelDialog(
            // Item Data
            gridItem = gridItem,
            data = data,
            // Callbacks
            onDismissRequest = {
                showEditLabelDialog = false
            },
            onUpdateGridItem = onUpdateGridItem,
        )
    }

    if (showSelectApplicationsDialog) {
        SelectApplicationsDialog(
            // Data
            eblanApplicationInfos = eblanApplicationInfos,
            // Callbacks
            onDismissRequest = {
                showSelectApplicationsDialog = false
            },
            onAddApplications = { selectedEblanApplicationInfos ->
                onAddApplicationInfosToFolder(
                    gridItem,
                    selectedEblanApplicationInfos,
                )
            },
        )
    }
}

@Composable
private fun EditShortcutInfo(
    // Shortcut Data
    gridItem: GridItem,
    data: GridItemData.ShortcutInfo,
    generalSettings: GeneralSettings,
    // Icon Packs
    iconPackComponents: List<IconPackComponent>,
    // Layout
    modifier: Modifier = Modifier,
    // Actions
    onResetIconPackPackageName: () -> Unit,
    onSearchIconPackInfoComponent: (String) -> Unit,
    onUpdateGridItem: (GridItem) -> Unit,
    onUpdateIconPackPackageName: (String) -> Unit,
    onResetGridItemCustomIcon: (GridItem) -> Unit,
    onUpdateGridItemCustomIcon: (
        gridItem: GridItem,
        uri: String,
    ) -> Unit,
    onUpdateGridItemCustomIconByteArray: (
        gridItem: GridItem,
        byteArray: ByteArray,
    ) -> Unit,
) {
    var showCustomShortLabelDialog by remember { mutableStateOf(false) }

    EditCustomIcon(
        gridItem = gridItem,
        customIcon = data.customIcon,
        generalSettings = generalSettings,
        iconPackComponents = iconPackComponents,
        modifier = modifier,
        onResetIconPackPackageName = onResetIconPackPackageName,
        onSearchIconPackInfoComponent = onSearchIconPackInfoComponent,
        onUpdateIconPackPackageName = onUpdateIconPackPackageName,
        onResetGridItemCustomIcon = onResetGridItemCustomIcon,
        onUpdateGridItemCustomIcon = onUpdateGridItemCustomIcon,
        onUpdateGridItemCustomIconByteArray = onUpdateGridItemCustomIconByteArray,
    )

    SettingsColumnItem(
        // Content
        title = stringResource(R.string.custom_short_label),
        subtitle = data.customShortLabel ?: stringResource(commonR.string.none),
        // Layout
        shape = EblanShapes.middle,
        modifier = modifier,
        // Actions
        onClick = {
            showCustomShortLabelDialog = true
        },
    )

    EditOverrideSwitch(
        gridItem = gridItem,
        modifier = modifier,
        onUpdateGridItem = onUpdateGridItem,
    )

    if (showCustomShortLabelDialog) {
        EditCustomLabelDialog(
            // Content
            customLabel = data.customShortLabel,
            // Callbacks
            onDismissRequest = {
                showCustomShortLabelDialog = false
            },
            onUpdateCustomLabel = { currentCustomShortLabel ->
                val newData = data.copy(customShortLabel = currentCustomShortLabel)

                onUpdateGridItem(gridItem.copy(data = newData))
            },
        )
    }
}

@Composable
private fun EditShortcutConfig(
    // Grid Item
    gridItem: GridItem,
    data: GridItemData.ShortcutConfig,
    generalSettings: GeneralSettings,
    // Icon Packs
    iconPackComponents: List<IconPackComponent>,
    // Modifier
    modifier: Modifier = Modifier,
    // Actions
    onResetIconPackPackageName: () -> Unit,
    onSearchIconPackInfoComponent: (String) -> Unit,
    onUpdateGridItem: (GridItem) -> Unit,
    onUpdateIconPackPackageName: (String) -> Unit,
    onResetGridItemCustomIcon: (GridItem) -> Unit,
    onUpdateGridItemCustomIcon: (
        gridItem: GridItem,
        uri: String,
    ) -> Unit,
    onUpdateGridItemCustomIconByteArray: (
        gridItem: GridItem,
        byteArray: ByteArray,
    ) -> Unit,
) {
    var showCustomLabelDialog by remember { mutableStateOf(false) }

    EditCustomIcon(
        gridItem = gridItem,
        customIcon = data.customIcon,
        generalSettings = generalSettings,
        iconPackComponents = iconPackComponents,
        modifier = modifier,
        onResetIconPackPackageName = onResetIconPackPackageName,
        onSearchIconPackInfoComponent = onSearchIconPackInfoComponent,
        onUpdateIconPackPackageName = onUpdateIconPackPackageName,
        onResetGridItemCustomIcon = onResetGridItemCustomIcon,
        onUpdateGridItemCustomIcon = onUpdateGridItemCustomIcon,
        onUpdateGridItemCustomIconByteArray = onUpdateGridItemCustomIconByteArray,
    )

    SettingsColumnItem(
        // Content
        title = stringResource(commonR.string.custom_label),
        subtitle = data.customLabel ?: stringResource(commonR.string.none),
        // Layout
        shape = EblanShapes.middle,
        modifier = modifier,
        // Actions
        onClick = {
            showCustomLabelDialog = true
        },
    )

    EditOverrideSwitch(
        gridItem = gridItem,
        modifier = modifier,
        onUpdateGridItem = onUpdateGridItem,
    )

    if (showCustomLabelDialog) {
        EditCustomLabelDialog(
            // Content
            customLabel = data.customLabel,
            // Callbacks
            onDismissRequest = {
                showCustomLabelDialog = false
            },
            onUpdateCustomLabel = { currentCustomLabel ->
                val newData = data.copy(customLabel = currentCustomLabel)

                onUpdateGridItem(gridItem.copy(data = newData))
            },
        )
    }
}

@Composable
private fun EditCustomIcon(
    gridItem: GridItem,
    customIcon: String?,
    generalSettings: GeneralSettings,
    iconPackComponents: List<IconPackComponent>,
    modifier: Modifier = Modifier,
    onResetIconPackPackageName: () -> Unit,
    onSearchIconPackInfoComponent: (String) -> Unit,
    onUpdateIconPackPackageName: (String) -> Unit,
    onResetGridItemCustomIcon: (GridItem) -> Unit,
    onUpdateGridItemCustomIcon: (
        gridItem: GridItem,
        uri: String,
    ) -> Unit,
    onUpdateGridItemCustomIconByteArray: (
        gridItem: GridItem,
        byteArray: ByteArray,
    ) -> Unit,
) {
    var showCustomIconDialog by remember { mutableStateOf(false) }

    var iconPackPackageName by remember { mutableStateOf<String?>(null) }
    var iconPackLabel by remember { mutableStateOf<String?>(null) }

    CustomIcon(
        customIcon = customIcon,
        shape = EblanShapes.first,
        modifier = modifier,
        onUpdateIconPackPackageName = { packageName, label ->
            iconPackPackageName = packageName
            iconPackLabel = label
            showCustomIconDialog = true

            onUpdateIconPackPackageName(packageName)
        },
        onUpdateUri = { currentIconUri ->
            onUpdateGridItemCustomIcon(gridItem, currentIconUri)
        },
        onResetCustomIcon = {
            onResetGridItemCustomIcon(gridItem)
        },
    )

    if (showCustomIconDialog) {
        IconPackInfoFilesDialog(
            // Icon Information
            iconPackComponents = iconPackComponents,
            iconPackPackageName = iconPackPackageName,
            iconPackInfoLabel = iconPackLabel,
            generalSettings = generalSettings,
            // Callbacks
            onDismissRequest = {
                onResetIconPackPackageName()

                showCustomIconDialog = false
            },
            onUpdateIcon = { currentIconByteArray ->
                onUpdateGridItemCustomIconByteArray(
                    gridItem,
                    currentIconByteArray,
                )
            },
            onSearchIconPackInfoComponent = onSearchIconPackInfoComponent,
        )
    }
}

@Composable
private fun EditOverrideSwitch(
    gridItem: GridItem,
    modifier: Modifier = Modifier,
    onUpdateGridItem: (GridItem) -> Unit,
) {
    SettingsSwitchItem(
        // Content
        title = stringResource(R.string.override),
        subtitle = stringResource(R.string.override_the_grid_item_settings),
        // State
        checked = gridItem.override,
        // Appearance
        shape = EblanShapes.last,
        modifier = modifier,
        // Callbacks
        onCheckedChange = { currentOverride ->
            onUpdateGridItem(
                gridItem.copy(
                    override = currentOverride,
                ),
            )
        },
    )
}
