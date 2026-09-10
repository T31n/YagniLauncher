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
package com.eblan.launcher.feature.editapplicationinfo

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eblan.launcher.common.R.string.custom_label
import com.eblan.launcher.common.R.string.none
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.domain.model.EblanApplicationInfoTag
import com.eblan.launcher.domain.model.EblanApplicationInfoTagUi
import com.eblan.launcher.domain.model.FolderEblanApplicationInfo
import com.eblan.launcher.domain.model.IconPackInfoComponent
import com.eblan.launcher.domain.model.PackageManagerIconPackInfo
import com.eblan.launcher.feature.editapplicationinfo.R.string.hide_from_drawer
import com.eblan.launcher.feature.editapplicationinfo.R.string.view_hidden_apps_in_app_drawer_settings
import com.eblan.launcher.feature.editapplicationinfo.dialog.AddFolderDialog
import com.eblan.launcher.feature.editapplicationinfo.dialog.AddTagDialog
import com.eblan.launcher.feature.editapplicationinfo.dialog.UpdateTagDialog
import com.eblan.launcher.feature.editapplicationinfo.model.EditApplicationInfoUiState
import com.eblan.launcher.ui.dialog.EditCustomLabelDialog
import com.eblan.launcher.ui.dialog.IconPackInfoFilesDialog
import com.eblan.launcher.ui.model.SettingsItem.Column
import com.eblan.launcher.ui.model.SettingsItem.CustomIcon
import com.eblan.launcher.ui.model.SettingsItem.Switch
import com.eblan.launcher.ui.settings.SettingsCategoryText
import com.eblan.launcher.ui.settings.SettingsItems
import com.eblan.launcher.common.R as commonR

@Composable
internal fun EditApplicationInfoRoute(
    modifier: Modifier = Modifier,
    viewModel: EditApplicationInfoViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit,
) {
    val editApplicationInfoUiState by viewModel.editApplicationInfoUiState.collectAsStateWithLifecycle()

    val packageManagerIconPackInfos by viewModel.packageManagerIconPackInfos.collectAsStateWithLifecycle()

    val iconPackInfoComponents by viewModel.iconPackInfoComponents.collectAsStateWithLifecycle()

    val eblanApplicationInfoTagsUi by viewModel.eblanApplicationInfoTagsUi.collectAsStateWithLifecycle()

    val folderEblanApplicationInfos by viewModel.folderEblanApplicationInfos.collectAsStateWithLifecycle()

    EditApplicationInfoScreen(
        modifier = modifier,
        eblanApplicationInfoTagsUi = eblanApplicationInfoTagsUi,
        editApplicationInfoUiState = editApplicationInfoUiState,
        iconPackInfoComponents = iconPackInfoComponents,
        packageManagerIconPackInfos = packageManagerIconPackInfos,
        folderEblanApplicationInfos = folderEblanApplicationInfos,
        onAddEblanApplicationInfoCrossRef = viewModel::addEblanApplicationInfoTagCrossRef,
        onAddEblanApplicationInfoTag = viewModel::addEblanApplicationInfoTag,
        onDeleteEblanApplicationInfoCrossRef = viewModel::deleteEblanApplicationInfoTagCrossRef,
        onDeleteEblanApplicationInfoTag = viewModel::deleteEblanApplicationInfoTag,
        onNavigateUp = onNavigateUp,
        onResetIconPackInfoPackageName = viewModel::resetIconPackInfoPackageName,
        onResetEblanApplicationInfoCustomIcon = viewModel::resetEblanApplicationInfoCustomIcon,
        onSearchIconPackInfoComponent = viewModel::searchIconPackInfoComponent,
        onUpdateEblanApplicationInfo = viewModel::updateEblanApplicationInfo,
        onUpdateEblanApplicationInfoTag = viewModel::updateEblanApplicationInfoTag,
        onUpdateIconPackInfoPackageName = viewModel::updateIconPackInfoPackageName,
        onUpdateEblanApplicationInfoCustomIcon = viewModel::updateEblanApplicationInfoCustomIcon,
        onAddFolderEblanApplicationInfo = viewModel::addFolderEblanApplicationInfo,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EditApplicationInfoScreen(
    modifier: Modifier = Modifier,
    eblanApplicationInfoTagsUi: List<EblanApplicationInfoTagUi>,
    editApplicationInfoUiState: EditApplicationInfoUiState,
    iconPackInfoComponents: List<IconPackInfoComponent>,
    packageManagerIconPackInfos: List<PackageManagerIconPackInfo>,
    folderEblanApplicationInfos: List<FolderEblanApplicationInfo>,
    onAddEblanApplicationInfoCrossRef: (Long) -> Unit,
    onAddEblanApplicationInfoTag: (EblanApplicationInfoTag) -> Unit,
    onDeleteEblanApplicationInfoCrossRef: (Long) -> Unit,
    onDeleteEblanApplicationInfoTag: (EblanApplicationInfoTag) -> Unit,
    onNavigateUp: () -> Unit,
    onResetIconPackInfoPackageName: () -> Unit,
    onSearchIconPackInfoComponent: (String) -> Unit,
    onUpdateEblanApplicationInfo: (EblanApplicationInfo) -> Unit,
    onUpdateEblanApplicationInfoTag: (EblanApplicationInfoTag) -> Unit,
    onUpdateIconPackInfoPackageName: (String) -> Unit,
    onResetEblanApplicationInfoCustomIcon: (EblanApplicationInfo) -> Unit,
    onUpdateEblanApplicationInfoCustomIcon: (
        eblanApplicationInfo: EblanApplicationInfo,
        uri: String,
    ) -> Unit,
    onAddFolderEblanApplicationInfo: (FolderEblanApplicationInfo) -> Unit,
) {
    if (editApplicationInfoUiState is EditApplicationInfoUiState.Success && editApplicationInfoUiState.eblanApplicationInfo != null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(
                                commonR.string.edit,
                                editApplicationInfoUiState.eblanApplicationInfo.label,
                            ),
                        )
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
                    eblanApplicationInfo = editApplicationInfoUiState.eblanApplicationInfo,
                    eblanApplicationInfoTagsUi = eblanApplicationInfoTagsUi,
                    iconPackInfoComponents = iconPackInfoComponents,
                    packageManagerIconPackInfos = packageManagerIconPackInfos,
                    folderEblanApplicationInfos = folderEblanApplicationInfos,
                    onAddEblanApplicationInfoCrossRef = onAddEblanApplicationInfoCrossRef,
                    onAddEblanApplicationInfoTag = onAddEblanApplicationInfoTag,
                    onDeleteEblanApplicationInfoCrossRef = onDeleteEblanApplicationInfoCrossRef,
                    onDeleteEblanApplicationInfoTag = onDeleteEblanApplicationInfoTag,
                    onResetIconPackInfoPackageName = onResetIconPackInfoPackageName,
                    onSearchIconPackInfoComponent = onSearchIconPackInfoComponent,
                    onUpdateEblanApplicationInfo = onUpdateEblanApplicationInfo,
                    onUpdateEblanApplicationInfoTag = onUpdateEblanApplicationInfoTag,
                    onUpdateIconPackInfoPackageName = onUpdateIconPackInfoPackageName,
                    onResetEblanApplicationInfoCustomIcon = onResetEblanApplicationInfoCustomIcon,
                    onUpdateEblanApplicationInfoCustomIcon = onUpdateEblanApplicationInfoCustomIcon,
                    onAddFolderEblanApplicationInfo = onAddFolderEblanApplicationInfo,
                )
            }
        }
    }
}

@Composable
private fun Success(
    modifier: Modifier = Modifier,
    eblanApplicationInfo: EblanApplicationInfo,
    eblanApplicationInfoTagsUi: List<EblanApplicationInfoTagUi>,
    iconPackInfoComponents: List<IconPackInfoComponent>,
    packageManagerIconPackInfos: List<PackageManagerIconPackInfo>,
    folderEblanApplicationInfos: List<FolderEblanApplicationInfo>,
    onAddEblanApplicationInfoCrossRef: (Long) -> Unit,
    onAddEblanApplicationInfoTag: (EblanApplicationInfoTag) -> Unit,
    onDeleteEblanApplicationInfoCrossRef: (Long) -> Unit,
    onDeleteEblanApplicationInfoTag: (EblanApplicationInfoTag) -> Unit,
    onResetIconPackInfoPackageName: () -> Unit,
    onSearchIconPackInfoComponent: (String) -> Unit,
    onUpdateEblanApplicationInfo: (EblanApplicationInfo) -> Unit,
    onUpdateEblanApplicationInfoTag: (EblanApplicationInfoTag) -> Unit,
    onUpdateIconPackInfoPackageName: (String) -> Unit,
    onResetEblanApplicationInfoCustomIcon: (EblanApplicationInfo) -> Unit,
    onUpdateEblanApplicationInfoCustomIcon: (
        eblanApplicationInfo: EblanApplicationInfo,
        uri: String,
    ) -> Unit,
    onAddFolderEblanApplicationInfo: (FolderEblanApplicationInfo) -> Unit,
) {
    var showCustomIconDialog by remember { mutableStateOf(false) }

    var showCustomLabelDialog by remember { mutableStateOf(false) }

    var iconPackInfoPackageName by remember { mutableStateOf<String?>(null) }

    var iconPackInfoLabel by remember { mutableStateOf<String?>(null) }

    val items = buildList {
        add(
            CustomIcon(
                customIcon = eblanApplicationInfo.customIcon,
                packageManagerIconPackInfos = packageManagerIconPackInfos,
                onUpdateIconPackInfoPackageName = { packageName, label ->
                    iconPackInfoPackageName = packageName
                    iconPackInfoLabel = label
                    showCustomIconDialog = true
                    onUpdateIconPackInfoPackageName(packageName)
                },
                onUpdateUri = {
                    onUpdateEblanApplicationInfoCustomIcon(eblanApplicationInfo, it)
                },
                onResetCustomIcon = {
                    onResetEblanApplicationInfoCustomIcon(eblanApplicationInfo)
                },
            ),
        )

        add(
            Column(
                title = stringResource(custom_label),
                subtitle = eblanApplicationInfo.customLabel
                    ?: stringResource(none),
                onClick = {
                    showCustomLabelDialog = true
                },
            ),
        )

        add(
            Switch(
                checked = eblanApplicationInfo.isHidden,
                title = stringResource(hide_from_drawer),
                subtitle = stringResource(view_hidden_apps_in_app_drawer_settings),
                onClick = {
                    onUpdateEblanApplicationInfo(
                        eblanApplicationInfo.copy(
                            isHidden = !eblanApplicationInfo.isHidden,
                        ),
                    )
                },
                onCheckedChange = {
                    onUpdateEblanApplicationInfo(
                        eblanApplicationInfo.copy(
                            isHidden = it,
                        ),
                    )
                },
            ),
        )
    }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .fillMaxSize()
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        SettingsCategoryText(text = "Tags")

        Tags(
            eblanApplicationInfoTagsUi = eblanApplicationInfoTagsUi,
            onAddEblanApplicationInfoCrossRef = onAddEblanApplicationInfoCrossRef,
            onAddEblanApplicationInfoTag = onAddEblanApplicationInfoTag,
            onDeleteEblanApplicationInfoCrossRef = onDeleteEblanApplicationInfoCrossRef,
            onDeleteEblanApplicationInfoTag = onDeleteEblanApplicationInfoTag,
            onUpdateEblanApplicationInfoTag = onUpdateEblanApplicationInfoTag,
        )

        SettingsCategoryText(text = "Folders")

        Folders(
            eblanApplicationInfo = eblanApplicationInfo,
            folderEblanApplicationInfos = folderEblanApplicationInfos,
            onUpdateEblanApplicationInfo = onUpdateEblanApplicationInfo,
            onAddFolderEblanApplicationInfo = onAddFolderEblanApplicationInfo,
        )

        SettingsItems(items = items)
    }

    if (showCustomIconDialog) {
        IconPackInfoFilesDialog(
            iconPackInfoComponents = iconPackInfoComponents,
            iconPackInfoPackageName = iconPackInfoPackageName,
            iconPackInfoLabel = iconPackInfoLabel,
            iconName = eblanApplicationInfo.packageName,
            onDismissRequest = {
                onResetIconPackInfoPackageName()

                showCustomIconDialog = false
            },
            onUpdateIcon = {
                onUpdateEblanApplicationInfo(eblanApplicationInfo.copy(customIcon = it))
            },
            onSearchIconPackInfoComponent = onSearchIconPackInfoComponent,
        )
    }

    if (showCustomLabelDialog) {
        EditCustomLabelDialog(
            customLabel = eblanApplicationInfo.customLabel,
            onDismissRequest = {
                showCustomLabelDialog = false
            },
            onUpdateCustomLabel = {
                onUpdateEblanApplicationInfo(eblanApplicationInfo.copy(customLabel = it))
            },
        )
    }
}

@Composable
private fun Tags(
    modifier: Modifier = Modifier,
    eblanApplicationInfoTagsUi: List<EblanApplicationInfoTagUi>,
    onAddEblanApplicationInfoCrossRef: (Long) -> Unit,
    onAddEblanApplicationInfoTag: (EblanApplicationInfoTag) -> Unit,
    onDeleteEblanApplicationInfoCrossRef: (Long) -> Unit,
    onDeleteEblanApplicationInfoTag: (EblanApplicationInfoTag) -> Unit,
    onUpdateEblanApplicationInfoTag: (EblanApplicationInfoTag) -> Unit,
) {
    var showAddTagDialog by remember { mutableStateOf(false) }

    var showUpdateTagDialog by remember { mutableStateOf(false) }

    var selectedEblanApplicationInfoTagUi by remember {
        mutableStateOf<EblanApplicationInfoTagUi?>(null)
    }

    FlowRow(modifier = modifier.fillMaxWidth()) {
        eblanApplicationInfoTagsUi.forEach { eblanApplicationInfoTagUi ->
            EblanApplicationInfoTagItem(
                eblanApplicationInfoTagUi = eblanApplicationInfoTagUi,
                onAddEblanApplicationInfoCrossRef = onAddEblanApplicationInfoCrossRef,
                onDeleteEblanApplicationInfoCrossRef = onDeleteEblanApplicationInfoCrossRef,
                onShowUpdateTagDialog = {
                    showUpdateTagDialog = true

                    selectedEblanApplicationInfoTagUi = it
                },
            )
        }

        AddTag(
            onClick = {
                showAddTagDialog = true
            },
        )
    }

    if (showAddTagDialog) {
        AddTagDialog(
            onDismissRequest = {
                showAddTagDialog = false
            },
            onAddEblanApplicationInfoTag = onAddEblanApplicationInfoTag,
        )
    }

    if (showUpdateTagDialog) {
        UpdateTagDialog(
            eblanApplicationInfoTagUi = selectedEblanApplicationInfoTagUi,
            onDeleteEblanApplicationInfoTag = onDeleteEblanApplicationInfoTag,
            onDismissRequest = {
                showUpdateTagDialog = false
            },
            onUpdateEblanApplicationInfoTag = onUpdateEblanApplicationInfoTag,
        )
    }
}

@Composable
private fun EblanApplicationInfoTagItem(
    modifier: Modifier = Modifier,
    eblanApplicationInfoTagUi: EblanApplicationInfoTagUi,
    onAddEblanApplicationInfoCrossRef: (Long) -> Unit,
    onDeleteEblanApplicationInfoCrossRef: (Long) -> Unit,
    onShowUpdateTagDialog: (EblanApplicationInfoTagUi) -> Unit,
) {
    Card(
        modifier = modifier.padding(5.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier
                .combinedClickable(
                    onClick = {
                        if (eblanApplicationInfoTagUi.selected) {
                            onDeleteEblanApplicationInfoCrossRef(eblanApplicationInfoTagUi.id)
                        } else {
                            onAddEblanApplicationInfoCrossRef(eblanApplicationInfoTagUi.id)
                        }
                    },
                    onLongClick = {
                        onShowUpdateTagDialog(eblanApplicationInfoTagUi)
                    },
                )
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (eblanApplicationInfoTagUi.selected) {
                Icon(
                    imageVector = EblanLauncherIcons.Done,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
            }

            Text(
                text = eblanApplicationInfoTagUi.name,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun AddTag(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier.padding(5.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier
                .combinedClickable(onClick = onClick)
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = EblanLauncherIcons.Add,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )

            Text(
                text = stringResource(commonR.string.add),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun Folders(
    modifier: Modifier = Modifier,
    eblanApplicationInfo: EblanApplicationInfo,
    folderEblanApplicationInfos: List<FolderEblanApplicationInfo>,
    onUpdateEblanApplicationInfo: (EblanApplicationInfo) -> Unit,
    onAddFolderEblanApplicationInfo: (FolderEblanApplicationInfo) -> Unit,
) {
    var showAddTagDialog by remember { mutableStateOf(false) }

    FlowRow(modifier = modifier.fillMaxWidth()) {
        folderEblanApplicationInfos.forEach { folderEblanApplicationInfo ->
            FolderEblanApplicationInfoItem(
                folderEblanApplicationInfo = folderEblanApplicationInfo,
                eblanApplicationInfo = eblanApplicationInfo,
                onUpdateEblanApplicationInfo = onUpdateEblanApplicationInfo,
            )
        }

        AddFolder(
            onClick = {
                showAddTagDialog = true
            },
        )
    }

    if (showAddTagDialog) {
        AddFolderDialog(
            onDismissRequest = {
                showAddTagDialog = false
            },
            onAddFolderEblanApplicationInfo = onAddFolderEblanApplicationInfo,
        )
    }
}

@Composable
private fun FolderEblanApplicationInfoItem(
    modifier: Modifier = Modifier,
    folderEblanApplicationInfo: FolderEblanApplicationInfo,
    eblanApplicationInfo: EblanApplicationInfo,
    onUpdateEblanApplicationInfo: (EblanApplicationInfo) -> Unit,
) {
    Card(
        modifier = modifier.padding(5.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier
                .combinedClickable(
                    onClick = {
                        if (folderEblanApplicationInfo.folderId == eblanApplicationInfo.folderId) {
                            onUpdateEblanApplicationInfo(eblanApplicationInfo.copy(folderId = null))
                        } else {
                            onUpdateEblanApplicationInfo(eblanApplicationInfo.copy(folderId = folderEblanApplicationInfo.folderId))
                        }
                    },
                )
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (folderEblanApplicationInfo.folderId == eblanApplicationInfo.folderId) {
                Icon(
                    imageVector = EblanLauncherIcons.Done,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
            }

            Text(
                text = folderEblanApplicationInfo.label,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun AddFolder(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier.padding(5.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier
                .combinedClickable(
                    onClick = onClick,
                )
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = EblanLauncherIcons.Add,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )

            Text(
                text = stringResource(commonR.string.add),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
