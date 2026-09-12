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
package com.eblan.launcher.feature.editfolderapplicationinfo

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
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfo
import com.eblan.launcher.domain.model.iconpackinfo.IconPackInfoComponent
import com.eblan.launcher.domain.model.iconpackinfo.PackageManagerIconPackInfo
import com.eblan.launcher.feature.editfolderapplicationinfo.dialog.EditFolderLabelDialog
import com.eblan.launcher.feature.editfolderapplicationinfo.model.EditFolderApplicationInfoUiState
import com.eblan.launcher.ui.dialog.AddFolderDialog
import com.eblan.launcher.ui.dialog.IconPackInfoFilesDialog
import com.eblan.launcher.ui.model.SettingsItem
import com.eblan.launcher.ui.settings.SettingsCategoryText
import com.eblan.launcher.ui.settings.SettingsItems
import com.eblan.launcher.common.R as commonR

@Composable
internal fun EditFolderApplicationInfoRoute(
    modifier: Modifier = Modifier,
    viewModel: EditFolderApplicationInfoViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit,
) {
    val editFolderApplicationInfoUiState by viewModel.editFolderApplicationInfoUiState.collectAsStateWithLifecycle()

    val packageManagerIconPackInfos by viewModel.packageManagerIconPackInfos.collectAsStateWithLifecycle()

    val iconPackInfoComponents by viewModel.iconPackInfoComponents.collectAsStateWithLifecycle()

    val folderEblanApplicationInfos by viewModel.folderEblanApplicationInfos.collectAsStateWithLifecycle()

    EditFolderApplicationInfoScreen(
        modifier = modifier,
        editFolderApplicationInfoUiState = editFolderApplicationInfoUiState,
        iconPackInfoComponents = iconPackInfoComponents,
        packageManagerIconPackInfos = packageManagerIconPackInfos,
        folderEblanApplicationInfos = folderEblanApplicationInfos,
        onNavigateUp = onNavigateUp,
        onUpdateIconPackInfoPackageName = viewModel::updateIconPackInfoPackageName,
        onResetFolderEblanApplicationInfoCustomIcon = viewModel::resetFolderEblanApplicationInfoCustomIcon,
        onUpdateFolderEblanApplicationInfoCustomIcon = viewModel::updateFolderEblanApplicationInfoCustomIcon,
        onResetIconPackInfoPackageName = viewModel::resetIconPackInfoPackageName,
        onSearchIconPackInfoComponent = viewModel::searchIconPackInfoComponent,
        onUpdateFolderEblanApplicationInfo = viewModel::updateFolderEblanApplicationInfo,
        onAddFolderEblanApplicationInfo = viewModel::addFolderEblanApplicationInfo,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EditFolderApplicationInfoScreen(
    modifier: Modifier = Modifier,
    editFolderApplicationInfoUiState: EditFolderApplicationInfoUiState,
    iconPackInfoComponents: List<IconPackInfoComponent>,
    packageManagerIconPackInfos: List<PackageManagerIconPackInfo>,
    folderEblanApplicationInfos: List<FolderEblanApplicationInfo>,
    onNavigateUp: () -> Unit,
    onUpdateIconPackInfoPackageName: (String) -> Unit,
    onResetFolderEblanApplicationInfoCustomIcon: (FolderEblanApplicationInfo) -> Unit,
    onUpdateFolderEblanApplicationInfoCustomIcon: (
        folderEblanApplicationInfo: FolderEblanApplicationInfo,
        uri: String,
    ) -> Unit,
    onResetIconPackInfoPackageName: () -> Unit,
    onSearchIconPackInfoComponent: (String) -> Unit,
    onUpdateFolderEblanApplicationInfo: (FolderEblanApplicationInfo) -> Unit,
    onAddFolderEblanApplicationInfo: (FolderEblanApplicationInfo) -> Unit,
) {
    if (editFolderApplicationInfoUiState is EditFolderApplicationInfoUiState.Success && editFolderApplicationInfoUiState.folderEblanApplicationInfo != null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(
                                commonR.string.edit,
                                editFolderApplicationInfoUiState.folderEblanApplicationInfo.label,
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
                    folderEblanApplicationInfo = editFolderApplicationInfoUiState.folderEblanApplicationInfo,
                    iconPackInfoComponents = iconPackInfoComponents,
                    packageManagerIconPackInfos = packageManagerIconPackInfos,
                    folderEblanApplicationInfos = folderEblanApplicationInfos,
                    onUpdateIconPackInfoPackageName = onUpdateIconPackInfoPackageName,
                    onResetFolderEblanApplicationInfoCustomIcon = onResetFolderEblanApplicationInfoCustomIcon,
                    onUpdateFolderEblanApplicationInfoCustomIcon = onUpdateFolderEblanApplicationInfoCustomIcon,
                    onResetIconPackInfoPackageName = onResetIconPackInfoPackageName,
                    onSearchIconPackInfoComponent = onSearchIconPackInfoComponent,
                    onUpdateFolderEblanApplicationInfo = onUpdateFolderEblanApplicationInfo,
                    onAddFolderEblanApplicationInfo = onAddFolderEblanApplicationInfo,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun Success(
    modifier: Modifier = Modifier,
    folderEblanApplicationInfo: FolderEblanApplicationInfo,
    iconPackInfoComponents: List<IconPackInfoComponent>,
    packageManagerIconPackInfos: List<PackageManagerIconPackInfo>,
    folderEblanApplicationInfos: List<FolderEblanApplicationInfo>,
    onUpdateIconPackInfoPackageName: (String) -> Unit,
    onResetFolderEblanApplicationInfoCustomIcon: (FolderEblanApplicationInfo) -> Unit,
    onUpdateFolderEblanApplicationInfoCustomIcon: (
        folderEblanApplicationInfo: FolderEblanApplicationInfo,
        uri: String,
    ) -> Unit,
    onResetIconPackInfoPackageName: () -> Unit,
    onSearchIconPackInfoComponent: (String) -> Unit,
    onUpdateFolderEblanApplicationInfo: (FolderEblanApplicationInfo) -> Unit,
    onAddFolderEblanApplicationInfo: (FolderEblanApplicationInfo) -> Unit,
) {
    var showCustomIconDialog by remember { mutableStateOf(false) }

    var showEditLabelDialog by remember { mutableStateOf(false) }

    var iconPackInfoPackageName by remember { mutableStateOf<String?>(null) }

    var iconPackInfoLabel by remember { mutableStateOf<String?>(null) }

    val items = buildList {
        add(
            SettingsItem.CustomIcon(
                customIcon = folderEblanApplicationInfo.icon,
                packageManagerIconPackInfos = packageManagerIconPackInfos,
                onUpdateIconPackInfoPackageName = { packageName, label ->
                    iconPackInfoPackageName = packageName
                    iconPackInfoLabel = label
                    showCustomIconDialog = true

                    onUpdateIconPackInfoPackageName(packageName)
                },
                onUpdateUri = {
                    onUpdateFolderEblanApplicationInfoCustomIcon(
                        folderEblanApplicationInfo,
                        it,
                    )
                },
                onResetCustomIcon = {
                    onResetFolderEblanApplicationInfoCustomIcon(
                        folderEblanApplicationInfo,
                    )
                },
            ),
        )

        add(
            SettingsItem.Column(
                title = stringResource(commonR.string.edit_label),
                subtitle = folderEblanApplicationInfo.label,
                onClick = {
                    showEditLabelDialog = true
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
        SettingsCategoryText(text = stringResource(commonR.string.folders))

        Folders(
            modifier = modifier,
            folderEblanApplicationInfo = folderEblanApplicationInfo,
            folderEblanApplicationInfos = folderEblanApplicationInfos,
            onUpdateFolderEblanApplicationInfo = onUpdateFolderEblanApplicationInfo,
            onAddFolderEblanApplicationInfo = onAddFolderEblanApplicationInfo,
        )

        SettingsItems(
            modifier = modifier,
            items = items,
        )

        if (showCustomIconDialog) {
            IconPackInfoFilesDialog(
                iconPackInfoComponents = iconPackInfoComponents,
                iconPackInfoPackageName = iconPackInfoPackageName,
                iconPackInfoLabel = iconPackInfoLabel,
                iconName = folderEblanApplicationInfo.id,
                onDismissRequest = {
                    onResetIconPackInfoPackageName()

                    showCustomIconDialog = false
                },
                onUpdateIcon = {
                    onUpdateFolderEblanApplicationInfo(
                        folderEblanApplicationInfo.copy(
                            icon = it,
                        ),
                    )
                },
                onSearchIconPackInfoComponent = onSearchIconPackInfoComponent,
            )
        }

        if (showEditLabelDialog) {
            EditFolderLabelDialog(
                folderEblanApplicationInfo = folderEblanApplicationInfo,
                onDismissRequest = {
                    showEditLabelDialog = false
                },
                onUpdateFolderEblanApplicationInfo = onUpdateFolderEblanApplicationInfo,
            )
        }
    }
}

@Composable
private fun Folders(
    modifier: Modifier = Modifier,
    folderEblanApplicationInfo: FolderEblanApplicationInfo,
    folderEblanApplicationInfos: List<FolderEblanApplicationInfo>,
    onUpdateFolderEblanApplicationInfo: (FolderEblanApplicationInfo) -> Unit,
    onAddFolderEblanApplicationInfo: (FolderEblanApplicationInfo) -> Unit,
) {
    var showAddFolderDialog by remember { mutableStateOf(false) }

    FlowRow(modifier = modifier.fillMaxWidth()) {
        folderEblanApplicationInfos.forEach {
            FolderEblanApplicationInfoItem(
                folderEblanApplicationInfo = it,
                currentFolderEblanApplicationInfo = folderEblanApplicationInfo,
                onUpdateFolderEblanApplicationInfo = onUpdateFolderEblanApplicationInfo,
            )
        }

        AddFolder(
            onClick = {
                showAddFolderDialog = true
            },
        )
    }

    if (showAddFolderDialog) {
        AddFolderDialog(
            onDismissRequest = {
                showAddFolderDialog = false
            },
            onAddFolderEblanApplicationInfo = onAddFolderEblanApplicationInfo,
        )
    }
}

@Composable
private fun FolderEblanApplicationInfoItem(
    modifier: Modifier = Modifier,
    folderEblanApplicationInfo: FolderEblanApplicationInfo,
    currentFolderEblanApplicationInfo: FolderEblanApplicationInfo,
    onUpdateFolderEblanApplicationInfo: (FolderEblanApplicationInfo) -> Unit,
) {
    Card(
        modifier = modifier.padding(5.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier
                .combinedClickable(
                    onClick = {
                        if (folderEblanApplicationInfo.id == currentFolderEblanApplicationInfo.folderId) {
                            onUpdateFolderEblanApplicationInfo(
                                currentFolderEblanApplicationInfo.copy(
                                    folderId = null,
                                ),
                            )
                        } else {
                            onUpdateFolderEblanApplicationInfo(
                                currentFolderEblanApplicationInfo.copy(
                                    folderId = folderEblanApplicationInfo.id,
                                ),
                            )
                        }
                    },
                )
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (folderEblanApplicationInfo.id == currentFolderEblanApplicationInfo.folderId) {
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
