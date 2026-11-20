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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfo
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfoGridItemData
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfoPopup
import com.eblan.launcher.domain.model.iconpackinfo.IconPackComponent
import com.eblan.launcher.domain.model.userdata.GeneralSettings
import com.eblan.launcher.feature.editfolderapplicationinfo.dialog.EditFolderLabelDialog
import com.eblan.launcher.feature.editfolderapplicationinfo.model.EditFolderApplicationInfoUiState
import com.eblan.launcher.ui.dialog.AddFolderDialog
import com.eblan.launcher.ui.dialog.IconPackInfoFilesDialog
import com.eblan.launcher.ui.settings.CustomIcon
import com.eblan.launcher.ui.settings.SettingsCategoryText
import com.eblan.launcher.ui.settings.SettingsColumnItem
import com.eblan.launcher.common.R as commonR

@Composable
internal fun EditFolderApplicationInfoRoute(
    modifier: Modifier = Modifier,
    viewModel: EditFolderApplicationInfoViewModel = hiltViewModel(),
    onNavigateUp: () -> Unit,
) {
    val editFolderApplicationInfoUiState by viewModel.editFolderApplicationInfoUiState.collectAsStateWithLifecycle()
    val iconPackInfoComponents by viewModel.iconPackInfoComponents.collectAsStateWithLifecycle()
    val folderEblanApplicationInfos by viewModel.folderEblanApplicationInfos.collectAsStateWithLifecycle()
    val folderEblanApplicationInfoPopups by viewModel.folderEblanApplicationInfoPopups.collectAsStateWithLifecycle()

    EditFolderApplicationInfoScreen(
        // State
        editFolderApplicationInfoUiState = editFolderApplicationInfoUiState,
        // Data
        iconPackComponents = iconPackInfoComponents,
        folderEblanApplicationInfos = folderEblanApplicationInfos,
        folderEblanApplicationInfoPopups = folderEblanApplicationInfoPopups,
        // Layout
        modifier = modifier,
        // Actions
        onNavigateUp = onNavigateUp,
        onUpdateIconPackPackageName = viewModel::updateIconPackPackageName,
        onUpdateFolderEblanApplicationInfoCustomIcon = viewModel::updateFolderEblanApplicationInfoCustomIcon,
        onUpdateFolderEblanApplicationInfoCustomIconByteArray = viewModel::updateFolderEblanApplicationInfoCustomIconByteArray,
        onResetIconPackPackageName = viewModel::resetIconPackPackageName,
        onSearchIconPackInfoComponent = viewModel::searchIconPackInfoComponent,
        onUpdateFolderEblanApplicationInfo = viewModel::updateFolderEblanApplicationInfo,
        onAddFolderEblanApplicationInfo = viewModel::addFolderEblanApplicationInfo,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EditFolderApplicationInfoScreen(
    // State
    editFolderApplicationInfoUiState: EditFolderApplicationInfoUiState,
    // Data
    iconPackComponents: List<IconPackComponent>,
    folderEblanApplicationInfos: List<FolderEblanApplicationInfo>,
    folderEblanApplicationInfoPopups: Map<String, FolderEblanApplicationInfoPopup>,
    // Layout
    modifier: Modifier = Modifier,
    // Actions
    onNavigateUp: () -> Unit,
    onUpdateIconPackPackageName: (String) -> Unit,
    onUpdateFolderEblanApplicationInfoCustomIcon: (
        folderEblanApplicationInfo: FolderEblanApplicationInfo,
        uri: String,
    ) -> Unit,
    onUpdateFolderEblanApplicationInfoCustomIconByteArray: (
        folderEblanApplicationInfo: FolderEblanApplicationInfo,
        byteArray: ByteArray,
    ) -> Unit,
    onResetIconPackPackageName: () -> Unit,
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
                    // Application Info
                    folderEblanApplicationInfo = editFolderApplicationInfoUiState.folderEblanApplicationInfo,
                    folderEblanApplicationInfos = folderEblanApplicationInfos,
                    topLevelFolderEblanApplicationInfos = folderEblanApplicationInfos,
                    folderEblanApplicationInfoPopups = folderEblanApplicationInfoPopups,
                    generalSettings = editFolderApplicationInfoUiState.generalSettings,
                    // Icon Packs
                    iconPackComponents = iconPackComponents,
                    // Callbacks
                    onUpdateIconPackPackageName = onUpdateIconPackPackageName,
                    onUpdateFolderEblanApplicationInfoCustomIcon = onUpdateFolderEblanApplicationInfoCustomIcon,
                    onUpdateFolderEblanApplicationInfoCustomIconByteArray = onUpdateFolderEblanApplicationInfoCustomIconByteArray,
                    onResetIconPackPackageName = onResetIconPackPackageName,
                    onSearchIconPackInfoComponent = onSearchIconPackInfoComponent,
                    onUpdateFolderEblanApplicationInfo = onUpdateFolderEblanApplicationInfo,
                    onAddFolderEblanApplicationInfo = onAddFolderEblanApplicationInfo,
                )
            }
        }
    }
}

@Composable
internal fun Success(
    // Application Info
    folderEblanApplicationInfo: FolderEblanApplicationInfo,
    folderEblanApplicationInfos: List<FolderEblanApplicationInfo>,
    topLevelFolderEblanApplicationInfos: List<FolderEblanApplicationInfo>,
    folderEblanApplicationInfoPopups: Map<String, FolderEblanApplicationInfoPopup>,
    generalSettings: GeneralSettings,
    // Icon Packs
    iconPackComponents: List<IconPackComponent>,
    // Modifier
    modifier: Modifier = Modifier,
    // Callbacks
    onUpdateIconPackPackageName: (String) -> Unit,
    onUpdateFolderEblanApplicationInfoCustomIcon: (
        folderEblanApplicationInfo: FolderEblanApplicationInfo,
        uri: String,
    ) -> Unit,
    onUpdateFolderEblanApplicationInfoCustomIconByteArray: (
        folderEblanApplicationInfo: FolderEblanApplicationInfo,
        byteArray: ByteArray,
    ) -> Unit,
    onResetIconPackPackageName: () -> Unit,
    onSearchIconPackInfoComponent: (String) -> Unit,
    onUpdateFolderEblanApplicationInfo: (FolderEblanApplicationInfo) -> Unit,
    onAddFolderEblanApplicationInfo: (FolderEblanApplicationInfo) -> Unit,
) {
    var showCustomIconDialog by remember { mutableStateOf(false) }
    var showEditLabelDialog by remember { mutableStateOf(false) }

    var iconPackPackageName by remember { mutableStateOf<String?>(null) }
    var iconPackInfoLabel by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .fillMaxSize()
            .padding(EblanSpacing.large),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        SettingsCategoryText(text = stringResource(commonR.string.folders))

        Folders(
            // Folder Data
            folderEblanApplicationInfo = folderEblanApplicationInfo,
            folderEblanApplicationInfos = folderEblanApplicationInfos,
            topLevelFolderEblanApplicationInfos = topLevelFolderEblanApplicationInfos,
            folderEblanApplicationInfoPopups = folderEblanApplicationInfoPopups,
            // Folder Callbacks
            onUpdateFolderEblanApplicationInfo = onUpdateFolderEblanApplicationInfo,
            onAddFolderEblanApplicationInfo = onAddFolderEblanApplicationInfo,
        )

        CustomIcon(
            customIcon = folderEblanApplicationInfo.icon,
            shape = EblanShapes.first,
            onUpdateIconPackPackageName = { packageName, label ->
                iconPackPackageName = packageName
                iconPackInfoLabel = label
                showCustomIconDialog = true

                onUpdateIconPackPackageName(packageName)
            },
            onUpdateUri = { currentIconUri ->
                onUpdateFolderEblanApplicationInfoCustomIcon(
                    folderEblanApplicationInfo,
                    currentIconUri,
                )
            },
            onResetCustomIcon = {
                onUpdateFolderEblanApplicationInfo(
                    folderEblanApplicationInfo.copy(icon = null),
                )
            },
        )

        SettingsColumnItem(
            // Content
            title = stringResource(commonR.string.edit_label),
            subtitle = folderEblanApplicationInfo.label,
            // Layout
            shape = EblanShapes.last,
            // Actions
            onClick = {
                showEditLabelDialog = true
            },
        )

        if (showCustomIconDialog) {
            IconPackInfoFilesDialog(
                // Icon Information
                iconPackComponents = iconPackComponents,
                iconPackPackageName = iconPackPackageName,
                iconPackInfoLabel = iconPackInfoLabel,
                generalSettings = generalSettings,
                // Callbacks
                onDismissRequest = {
                    onResetIconPackPackageName()

                    showCustomIconDialog = false
                },
                onUpdateIcon = { currentIconByteArray ->
                    onUpdateFolderEblanApplicationInfoCustomIconByteArray(
                        folderEblanApplicationInfo,
                        currentIconByteArray,
                    )
                },
                onSearchIconPackInfoComponent = onSearchIconPackInfoComponent,
            )
        }

        if (showEditLabelDialog) {
            EditFolderLabelDialog(
                // Data
                folderEblanApplicationInfo = folderEblanApplicationInfo,
                // Callbacks
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
    // Folder Data
    folderEblanApplicationInfo: FolderEblanApplicationInfo,
    folderEblanApplicationInfos: List<FolderEblanApplicationInfo>,
    topLevelFolderEblanApplicationInfos: List<FolderEblanApplicationInfo>,
    folderEblanApplicationInfoPopups: Map<String, FolderEblanApplicationInfoPopup>,
    // Modifier
    modifier: Modifier = Modifier,
    // Folder Callbacks
    onUpdateFolderEblanApplicationInfo: (FolderEblanApplicationInfo) -> Unit,
    onAddFolderEblanApplicationInfo: (FolderEblanApplicationInfo) -> Unit,
) {
    if (folderEblanApplicationInfos.size == 1 &&
        folderEblanApplicationInfos.single().folderId != null
    ) {
        return
    }

    var showAddFolderDialog by remember { mutableStateOf(false) }

    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        folderEblanApplicationInfos.forEach { currentFolderEblanApplicationInfo ->
            FolderEblanApplicationInfoItem(
                // Identifier and Label
                id = currentFolderEblanApplicationInfo.id,
                label = currentFolderEblanApplicationInfo.label,
                // Application Info
                folderEblanApplicationInfo = folderEblanApplicationInfo,
                folderEblanApplicationInfoPopups = folderEblanApplicationInfoPopups,
                // Callbacks
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
            // Data
            topLevelFolderEblanApplicationInfos = topLevelFolderEblanApplicationInfos,
            // Callbacks
            onDismissRequest = {
                showAddFolderDialog = false
            },
            onAddFolderEblanApplicationInfo = onAddFolderEblanApplicationInfo,
        )
    }
}

@Composable
private fun FolderEblanApplicationInfoItem(
    // Identifier and Label
    id: String,
    label: String,
    // Application Info
    folderEblanApplicationInfo: FolderEblanApplicationInfo,
    folderEblanApplicationInfoPopups: Map<String, FolderEblanApplicationInfoPopup>,
    // Modifier
    modifier: Modifier = Modifier,
    // Callbacks
    onUpdateFolderEblanApplicationInfo: (FolderEblanApplicationInfo) -> Unit,
) {
    val folderIndex = remember(
        key1 = folderEblanApplicationInfoPopups,
        key2 = id,
    ) {
        folderEblanApplicationInfoPopups[id]?.folderEblanApplicationInfoGridItems?.maxOfOrNull { currentFolderEblanApplicationInfoGridItem ->
            when (val data = currentFolderEblanApplicationInfoGridItem.data) {
                is FolderEblanApplicationInfoGridItemData.ApplicationInfo -> data.folderIndex + 1
                is FolderEblanApplicationInfoGridItemData.Folder -> data.folderIndex + 1
            }
        } ?: 0
    }

    val selected = id == folderEblanApplicationInfo.folderId

    FilterChip(
        modifier = modifier,
        selected = selected,
        onClick = {
            if (selected) {
                onUpdateFolderEblanApplicationInfo(
                    folderEblanApplicationInfo.copy(
                        folderId = null,
                    ),
                )
            } else {
                onUpdateFolderEblanApplicationInfo(
                    folderEblanApplicationInfo.copy(
                        folderIndex = folderIndex,
                        folderId = id,
                    ),
                )
            }
        },
        label = {
            Text(text = label)
        },
        leadingIcon = if (selected) {
            {
                Icon(
                    imageVector = EblanLauncherIcons.Done,
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize),
                )
            }
        } else {
            null
        },
    )
}

@Composable
private fun AddFolder(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    AssistChip(
        modifier = modifier,
        onClick = onClick,
        label = {
            Text(text = stringResource(commonR.string.add))
        },
        leadingIcon = {
            Icon(
                imageVector = EblanLauncherIcons.Add,
                contentDescription = null,
                modifier = Modifier.size(AssistChipDefaults.IconSize),
            )
        },
    )
}
