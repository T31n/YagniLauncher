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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eblan.launcher.designsystem.component.VerticalSlideReveal
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.domain.model.application.EblanApplicationInfo
import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.domain.model.grid.GridItemData
import com.eblan.launcher.domain.model.iconpackinfo.IconPackInfoComponent
import com.eblan.launcher.domain.model.iconpackinfo.PackageManagerIconPackInfo
import com.eblan.launcher.feature.editgriditem.dialog.EditFolderLabelDialog
import com.eblan.launcher.feature.editgriditem.model.EditGridItemUiState
import com.eblan.launcher.ui.dialog.EditCustomLabelDialog
import com.eblan.launcher.ui.dialog.IconPackInfoFilesDialog
import com.eblan.launcher.ui.model.SettingsItem
import com.eblan.launcher.ui.settings.EblanActionSettings
import com.eblan.launcher.ui.settings.GridItemSettings
import com.eblan.launcher.ui.settings.SettingsItems
import com.eblan.launcher.common.R as commonR

@Composable
internal fun EditGridItemRoute(
    modifier: Modifier = Modifier,
    viewModel: EditGridItemViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit,
) {
    val editUiState by viewModel.editGridItemUiState.collectAsStateWithLifecycle()

    val packageManagerIconPackInfos by viewModel.packageManagerIconPackInfos.collectAsStateWithLifecycle()

    val iconPackInfoComponents by viewModel.iconPackInfoComponents.collectAsStateWithLifecycle()

    val eblanApplicationInfos by viewModel.eblanApplicationInfos.collectAsStateWithLifecycle()

    EditGridItemScreen(
        modifier = modifier,
        eblanApplicationInfos = eblanApplicationInfos,
        editGridItemUiState = editUiState,
        iconPackInfoComponents = iconPackInfoComponents,
        packageManagerIconPackInfos = packageManagerIconPackInfos,
        onNavigateUp = onNavigateUp,
        onResetIconPackInfoPackageName = viewModel::resetIconPackInfoPackageName,
        onResetGridItemCustomIcon = viewModel::resetGridItemCustomIcon,
        onSearchIconPackInfoComponent = viewModel::searchIconPackInfoComponent,
        onUpdateGridItem = viewModel::updateGridItem,
        onUpdateIconPackInfoPackageName = viewModel::updateIconPackInfoPackageName,
        onUpdateGridItemCustomIcon = viewModel::updateGridItemCustomIcon,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EditGridItemScreen(
    modifier: Modifier = Modifier,
    eblanApplicationInfos: List<EblanApplicationInfo>,
    editGridItemUiState: EditGridItemUiState,
    iconPackInfoComponents: List<IconPackInfoComponent>,
    packageManagerIconPackInfos: List<PackageManagerIconPackInfo>,
    onNavigateUp: () -> Unit,
    onResetIconPackInfoPackageName: () -> Unit,
    onResetGridItemCustomIcon: (GridItem) -> Unit,
    onSearchIconPackInfoComponent: (String) -> Unit,
    onUpdateGridItem: (GridItem) -> Unit,
    onUpdateIconPackInfoPackageName: (String) -> Unit,
    onUpdateGridItemCustomIcon: (
        gridItem: GridItem,
        uri: String,
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
                    eblanApplicationInfos = eblanApplicationInfos,
                    gridItem = editGridItemUiState.gridItem,
                    iconPackInfoComponents = iconPackInfoComponents,
                    packageManagerIconPackInfos = packageManagerIconPackInfos,
                    onResetIconPackInfoPackageName = onResetIconPackInfoPackageName,
                    onSearchIconPackInfoComponent = onSearchIconPackInfoComponent,
                    onUpdateGridItem = onUpdateGridItem,
                    onUpdateIconPackInfoPackageName = onUpdateIconPackInfoPackageName,
                    onResetGridItemCustomIcon = onResetGridItemCustomIcon,
                    onUpdateGridItemCustomIcon = onUpdateGridItemCustomIcon,
                )
            }
        }
    }
}

@Composable
private fun Success(
    modifier: Modifier = Modifier,
    eblanApplicationInfos: List<EblanApplicationInfo>,
    gridItem: GridItem,
    iconPackInfoComponents: List<IconPackInfoComponent>,
    packageManagerIconPackInfos: List<PackageManagerIconPackInfo>,
    onResetIconPackInfoPackageName: () -> Unit,
    onSearchIconPackInfoComponent: (String) -> Unit,
    onUpdateGridItem: (GridItem) -> Unit,
    onUpdateIconPackInfoPackageName: (String) -> Unit,
    onResetGridItemCustomIcon: (GridItem) -> Unit,
    onUpdateGridItemCustomIcon: (
        gridItem: GridItem,
        uri: String,
    ) -> Unit,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .fillMaxSize()
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        when (val data = gridItem.data) {
            is GridItemData.ApplicationInfo -> {
                EditApplicationInfo(
                    data = data,
                    gridItem = gridItem,
                    iconPackInfoComponents = iconPackInfoComponents,
                    packageManagerIconPackInfos = packageManagerIconPackInfos,
                    onResetIconPackInfoPackageName = onResetIconPackInfoPackageName,
                    onSearchIconPackInfoComponent = onSearchIconPackInfoComponent,
                    onUpdateGridItem = onUpdateGridItem,
                    onUpdateIconPackInfoPackageName = onUpdateIconPackInfoPackageName,
                    onResetGridItemCustomIcon = onResetGridItemCustomIcon,
                    onUpdateGridItemCustomIcon = onUpdateGridItemCustomIcon,
                )
            }

            is GridItemData.Folder -> {
                EditFolder(
                    data = data,
                    gridItem = gridItem,
                    iconPackInfoComponents = iconPackInfoComponents,
                    packageManagerIconPackInfos = packageManagerIconPackInfos,
                    onResetIconPackInfoPackageName = onResetIconPackInfoPackageName,
                    onSearchIconPackInfoComponent = onSearchIconPackInfoComponent,
                    onUpdateGridItem = onUpdateGridItem,
                    onUpdateIconPackInfoPackageName = onUpdateIconPackInfoPackageName,
                    onResetGridItemCustomIcon = onResetGridItemCustomIcon,
                    onUpdateGridItemCustomIcon = onUpdateGridItemCustomIcon,
                )
            }

            is GridItemData.ShortcutInfo -> {
                EditShortcutInfo(
                    data = data,
                    gridItem = gridItem,
                    iconPackInfoComponents = iconPackInfoComponents,
                    packageManagerIconPackInfos = packageManagerIconPackInfos,
                    onResetIconPackInfoPackageName = onResetIconPackInfoPackageName,
                    onSearchIconPackInfoComponent = onSearchIconPackInfoComponent,
                    onUpdateGridItem = onUpdateGridItem,
                    onUpdateIconPackInfoPackageName = onUpdateIconPackInfoPackageName,
                    onResetGridItemCustomIcon = onResetGridItemCustomIcon,
                    onUpdateGridItemCustomIcon = onUpdateGridItemCustomIcon,
                )
            }

            is GridItemData.ShortcutConfig -> {
                EditShortcutConfig(
                    data = data,
                    gridItem = gridItem,
                    iconPackInfoComponents = iconPackInfoComponents,
                    packageManagerIconPackInfos = packageManagerIconPackInfos,
                    onResetIconPackInfoPackageName = onResetIconPackInfoPackageName,
                    onSearchIconPackInfoComponent = onSearchIconPackInfoComponent,
                    onUpdateGridItem = onUpdateGridItem,
                    onUpdateIconPackInfoPackageName = onUpdateIconPackInfoPackageName,
                    onResetGridItemCustomIcon = onResetGridItemCustomIcon,
                    onUpdateGridItemCustomIcon = onUpdateGridItemCustomIcon,
                )
            }

            else -> Unit
        }

        VerticalSlideReveal(visible = gridItem.override) {
            GridItemSettings(
                gridItemSettings = gridItem.gridItemSettings,
                onUpdateGridItemSettings = {
                    onUpdateGridItem(gridItem.copy(gridItemSettings = it))
                },
            )
        }

        Text(
            modifier = Modifier.padding(15.dp),
            text = stringResource(R.string.grid_item_actions),
            style = MaterialTheme.typography.bodySmall,
        )

        EblanActionSettings(
            doubleTap = gridItem.doubleTap,
            swipeUp = gridItem.swipeUp,
            swipeDown = gridItem.swipeDown,
            eblanApplicationInfos = eblanApplicationInfos,
            onUpdateDoubleTap = {
                onUpdateGridItem(gridItem.copy(doubleTap = it))
            },
            onUpdateSwipeUp = {
                onUpdateGridItem(gridItem.copy(swipeUp = it))
            },
            onUpdateSwipeDown = {
                onUpdateGridItem(gridItem.copy(swipeDown = it))
            },
        )
    }
}

@Composable
private fun EditApplicationInfo(
    modifier: Modifier = Modifier,
    data: GridItemData.ApplicationInfo,
    gridItem: GridItem,
    iconPackInfoComponents: List<IconPackInfoComponent>,
    packageManagerIconPackInfos: List<PackageManagerIconPackInfo>,
    onResetIconPackInfoPackageName: () -> Unit,
    onSearchIconPackInfoComponent: (String) -> Unit,
    onUpdateGridItem: (GridItem) -> Unit,
    onUpdateIconPackInfoPackageName: (String) -> Unit,
    onResetGridItemCustomIcon: (GridItem) -> Unit,
    onUpdateGridItemCustomIcon: (
        gridItem: GridItem,
        uri: String,
    ) -> Unit,
) {
    var showCustomIconDialog by remember { mutableStateOf(false) }

    var showCustomLabelDialog by remember { mutableStateOf(false) }

    var iconPackInfoPackageName by remember { mutableStateOf<String?>(null) }

    var iconPackInfoLabel by remember { mutableStateOf<String?>(null) }

    val items = buildList {
        add(
            SettingsItem.CustomIcon(
                customIcon = data.customIcon,
                packageManagerIconPackInfos = packageManagerIconPackInfos,
                onUpdateIconPackInfoPackageName = { packageName, label ->
                    iconPackInfoPackageName = packageName
                    iconPackInfoLabel = label
                    showCustomIconDialog = true

                    onUpdateIconPackInfoPackageName(packageName)
                },
                onUpdateUri = {
                    onUpdateGridItemCustomIcon(gridItem, it)
                },
                onResetCustomIcon = {
                    onResetGridItemCustomIcon(gridItem)
                },
            ),
        )

        add(
            SettingsItem.Column(
                title = stringResource(commonR.string.custom_label),
                subtitle = data.customLabel ?: stringResource(commonR.string.none),
                onClick = {
                    showCustomLabelDialog = true
                },
            ),
        )

        add(
            SettingsItem.Switch(
                checked = gridItem.override,
                title = stringResource(R.string.override),
                subtitle = stringResource(R.string.override_the_grid_item_settings),
                onClick = {
                    onUpdateGridItem(
                        gridItem.copy(
                            override = !gridItem.override,
                        ),
                    )
                },
                onCheckedChange = {
                    onUpdateGridItem(
                        gridItem.copy(
                            override = it,
                        ),
                    )
                },
            ),
        )
    }

    SettingsItems(
        modifier = modifier,
        items = items,
    )

    if (showCustomIconDialog) {
        IconPackInfoFilesDialog(
            iconPackInfoComponents = iconPackInfoComponents,
            iconPackInfoPackageName = iconPackInfoPackageName,
            iconPackInfoLabel = iconPackInfoLabel,
            iconName = gridItem.id,
            onDismissRequest = {
                onResetIconPackInfoPackageName()

                showCustomIconDialog = false
            },
            onUpdateIcon = {
                onUpdateGridItem(
                    getGridItem(
                        gridItem = gridItem,
                        customIcon = it,
                    ),
                )
            },
            onSearchIconPackInfoComponent = onSearchIconPackInfoComponent,
        )
    }

    if (showCustomLabelDialog) {
        EditCustomLabelDialog(
            customLabel = data.customLabel,
            onDismissRequest = {
                showCustomLabelDialog = false
            },
            onUpdateCustomLabel = {
                val newData = data.copy(customLabel = it)

                onUpdateGridItem(gridItem.copy(data = newData))
            },
        )
    }
}

@Composable
private fun EditFolder(
    modifier: Modifier = Modifier,
    data: GridItemData.Folder,
    gridItem: GridItem,
    iconPackInfoComponents: List<IconPackInfoComponent>,
    packageManagerIconPackInfos: List<PackageManagerIconPackInfo>,
    onResetIconPackInfoPackageName: () -> Unit,
    onSearchIconPackInfoComponent: (String) -> Unit,
    onUpdateGridItem: (GridItem) -> Unit,
    onUpdateIconPackInfoPackageName: (String) -> Unit,
    onResetGridItemCustomIcon: (GridItem) -> Unit,
    onUpdateGridItemCustomIcon: (
        gridItem: GridItem,
        uri: String,
    ) -> Unit,
) {
    var showCustomIconDialog by remember { mutableStateOf(false) }

    var showEditLabelDialog by remember { mutableStateOf(false) }

    var iconPackInfoPackageName by remember { mutableStateOf<String?>(null) }

    var iconPackInfoLabel by remember { mutableStateOf<String?>(null) }

    val items = buildList {
        add(
            SettingsItem.CustomIcon(
                customIcon = data.icon,
                packageManagerIconPackInfos = packageManagerIconPackInfos,
                onUpdateIconPackInfoPackageName = { packageName, label ->
                    iconPackInfoPackageName = packageName
                    iconPackInfoLabel = label
                    showCustomIconDialog = true

                    onUpdateIconPackInfoPackageName(packageName)
                },
                onUpdateUri = {
                    onUpdateGridItemCustomIcon(gridItem, it)
                },
                onResetCustomIcon = {
                    onResetGridItemCustomIcon(gridItem)
                },
            ),
        )

        add(
            SettingsItem.Column(
                title = stringResource(commonR.string.edit_label),
                subtitle = data.label,
                onClick = {
                    showEditLabelDialog = true
                },
            ),
        )

        add(
            SettingsItem.Switch(
                checked = gridItem.override,
                title = stringResource(R.string.override),
                subtitle = stringResource(R.string.override_the_grid_item_settings),
                onClick = {
                    onUpdateGridItem(
                        gridItem.copy(
                            override = !gridItem.override,
                        ),
                    )
                },
                onCheckedChange = {
                    onUpdateGridItem(
                        gridItem.copy(
                            override = it,
                        ),
                    )
                },
            ),
        )
    }

    SettingsItems(
        modifier = modifier,
        items = items,
    )

    if (showCustomIconDialog) {
        IconPackInfoFilesDialog(
            iconPackInfoComponents = iconPackInfoComponents,
            iconPackInfoPackageName = iconPackInfoPackageName,
            iconPackInfoLabel = iconPackInfoLabel,
            iconName = gridItem.id,
            onDismissRequest = {
                onResetIconPackInfoPackageName()

                showCustomIconDialog = false
            },
            onUpdateIcon = {
                onUpdateGridItem(
                    getGridItem(
                        gridItem = gridItem,
                        customIcon = it,
                    ),
                )
            },
            onSearchIconPackInfoComponent = onSearchIconPackInfoComponent,
        )
    }

    if (showEditLabelDialog) {
        EditFolderLabelDialog(
            gridItem = gridItem,
            data = data,
            onDismissRequest = {
                showEditLabelDialog = false
            },
            onUpdateGridItem = onUpdateGridItem,
        )
    }
}

@Composable
private fun EditShortcutInfo(
    modifier: Modifier = Modifier,
    data: GridItemData.ShortcutInfo,
    gridItem: GridItem,
    iconPackInfoComponents: List<IconPackInfoComponent>,
    packageManagerIconPackInfos: List<PackageManagerIconPackInfo>,
    onResetIconPackInfoPackageName: () -> Unit,
    onSearchIconPackInfoComponent: (String) -> Unit,
    onUpdateGridItem: (GridItem) -> Unit,
    onUpdateIconPackInfoPackageName: (String) -> Unit,
    onResetGridItemCustomIcon: (GridItem) -> Unit,
    onUpdateGridItemCustomIcon: (
        gridItem: GridItem,
        uri: String,
    ) -> Unit,
) {
    var showCustomIconDialog by remember { mutableStateOf(false) }

    var showCustomShortLabelDialog by remember { mutableStateOf(false) }

    var iconPackInfoPackageName by remember { mutableStateOf<String?>(null) }

    var iconPackInfoLabel by remember { mutableStateOf<String?>(null) }

    val items = buildList {
        add(
            SettingsItem.CustomIcon(
                customIcon = data.customIcon,
                packageManagerIconPackInfos = packageManagerIconPackInfos,
                onUpdateIconPackInfoPackageName = { packageName, label ->
                    iconPackInfoPackageName = packageName
                    iconPackInfoLabel = label
                    showCustomIconDialog = true

                    onUpdateIconPackInfoPackageName(packageName)
                },
                onUpdateUri = {
                    onUpdateGridItemCustomIcon(gridItem, it)
                },
                onResetCustomIcon = {
                    onResetGridItemCustomIcon(gridItem)
                },
            ),
        )

        add(
            SettingsItem.Column(
                title = stringResource(R.string.custom_short_label),
                subtitle = data.customShortLabel ?: stringResource(commonR.string.none),
                onClick = {
                    showCustomShortLabelDialog = true
                },
            ),
        )

        add(
            SettingsItem.Switch(
                checked = gridItem.override,
                title = stringResource(R.string.override),
                subtitle = stringResource(R.string.override_the_grid_item_settings),
                onClick = {
                    onUpdateGridItem(
                        gridItem.copy(
                            override = !gridItem.override,
                        ),
                    )
                },
                onCheckedChange = {
                    onUpdateGridItem(
                        gridItem.copy(
                            override = it,
                        ),
                    )
                },
            ),
        )
    }

    SettingsItems(
        modifier = modifier,
        items = items,
    )

    if (showCustomIconDialog) {
        IconPackInfoFilesDialog(
            iconPackInfoComponents = iconPackInfoComponents,
            iconPackInfoPackageName = iconPackInfoPackageName,
            iconPackInfoLabel = iconPackInfoLabel,
            iconName = gridItem.id,
            onDismissRequest = {
                onResetIconPackInfoPackageName()

                showCustomIconDialog = false
            },
            onUpdateIcon = {
                onUpdateGridItem(
                    getGridItem(
                        gridItem = gridItem,
                        customIcon = it,
                    ),
                )
            },
            onSearchIconPackInfoComponent = onSearchIconPackInfoComponent,
        )
    }

    if (showCustomShortLabelDialog) {
        EditCustomLabelDialog(
            customLabel = data.customShortLabel,
            onDismissRequest = {
                showCustomShortLabelDialog = false
            },
            onUpdateCustomLabel = {
                val newData = data.copy(customShortLabel = it)

                onUpdateGridItem(gridItem.copy(data = newData))
            },
        )
    }
}

@Composable
private fun EditShortcutConfig(
    modifier: Modifier = Modifier,
    data: GridItemData.ShortcutConfig,
    gridItem: GridItem,
    iconPackInfoComponents: List<IconPackInfoComponent>,
    packageManagerIconPackInfos: List<PackageManagerIconPackInfo>,
    onResetIconPackInfoPackageName: () -> Unit,
    onSearchIconPackInfoComponent: (String) -> Unit,
    onUpdateGridItem: (GridItem) -> Unit,
    onUpdateIconPackInfoPackageName: (String) -> Unit,
    onResetGridItemCustomIcon: (GridItem) -> Unit,
    onUpdateGridItemCustomIcon: (
        gridItem: GridItem,
        uri: String,
    ) -> Unit,
) {
    var showCustomIconDialog by remember { mutableStateOf(false) }

    var showCustomLabelDialog by remember { mutableStateOf(false) }

    var iconPackInfoPackageName by remember { mutableStateOf<String?>(null) }

    var iconPackInfoLabel by remember { mutableStateOf<String?>(null) }

    val items = buildList {
        add(
            SettingsItem.CustomIcon(
                customIcon = data.customIcon,
                packageManagerIconPackInfos = packageManagerIconPackInfos,
                onUpdateIconPackInfoPackageName = { packageName, label ->
                    iconPackInfoPackageName = packageName
                    iconPackInfoLabel = label
                    showCustomIconDialog = true

                    onUpdateIconPackInfoPackageName(packageName)
                },
                onUpdateUri = {
                    onUpdateGridItemCustomIcon(gridItem, it)
                },
                onResetCustomIcon = {
                    onResetGridItemCustomIcon(gridItem)
                },
            ),
        )

        add(
            SettingsItem.Column(
                title = stringResource(commonR.string.custom_label),
                subtitle = data.customLabel ?: stringResource(commonR.string.none),
                onClick = {
                    showCustomLabelDialog = true
                },
            ),
        )

        add(
            SettingsItem.Switch(
                checked = gridItem.override,
                title = stringResource(R.string.override),
                subtitle = stringResource(R.string.override_the_grid_item_settings),
                onClick = {
                    onUpdateGridItem(
                        gridItem.copy(
                            override = !gridItem.override,
                        ),
                    )
                },
                onCheckedChange = {
                    onUpdateGridItem(
                        gridItem.copy(
                            override = it,
                        ),
                    )
                },
            ),
        )
    }

    SettingsItems(
        modifier = modifier,
        items = items,
    )

    if (showCustomIconDialog) {
        IconPackInfoFilesDialog(
            iconPackInfoComponents = iconPackInfoComponents,
            iconPackInfoPackageName = iconPackInfoPackageName,
            iconPackInfoLabel = iconPackInfoLabel,
            iconName = gridItem.id,
            onDismissRequest = {
                onResetIconPackInfoPackageName()

                showCustomIconDialog = false
            },
            onUpdateIcon = {
                onUpdateGridItem(
                    getGridItem(
                        gridItem = gridItem,
                        customIcon = it,
                    ),
                )
            },
            onSearchIconPackInfoComponent = onSearchIconPackInfoComponent,
        )
    }

    if (showCustomLabelDialog) {
        EditCustomLabelDialog(
            customLabel = data.customLabel,
            onDismissRequest = {
                showCustomLabelDialog = false
            },
            onUpdateCustomLabel = {
                val newData = data.copy(customLabel = it)

                onUpdateGridItem(gridItem.copy(data = newData))
            },
        )
    }
}

private fun getGridItem(gridItem: GridItem, customIcon: String?): GridItem = when (val data = gridItem.data) {
    is GridItemData.ApplicationInfo -> gridItem.copy(data = data.copy(customIcon = customIcon))

    is GridItemData.Folder -> gridItem.copy(data = data.copy(icon = customIcon))

    is GridItemData.ShortcutConfig -> gridItem.copy(data = data.copy(customIcon = customIcon))

    is GridItemData.ShortcutInfo ->
        gridItem.copy(data = data.copy(customIcon = customIcon))

    else -> gridItem
}
