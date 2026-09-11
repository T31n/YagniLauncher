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
package com.eblan.launcher.feature.action

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.domain.model.application.EblanApplicationInfo
import com.eblan.launcher.domain.model.userdata.EblanAction
import com.eblan.launcher.domain.model.userdata.EblanActionType
import com.eblan.launcher.feature.action.model.ActionUiState
import com.eblan.launcher.ui.dialog.SelectApplicationDialog
import com.eblan.launcher.ui.settings.getEblanActionTypeSubtitle
import kotlinx.coroutines.launch

@Composable
fun ActionScreen(
    modifier: Modifier = Modifier,
    viewModel: ActionViewModel = hiltViewModel(),
    onFinish: () -> Unit,
    onUpdateEblanAction: suspend (
        resId: Int,
        eblanAction: EblanAction,
    ) -> Unit,
) {
    val actionUiState by viewModel.actionUiState.collectAsStateWithLifecycle()

    ActionScreen(
        modifier = modifier,
        actionUiState = actionUiState,
        onFinish = onFinish,
        onUpdateEblanAction = onUpdateEblanAction,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ActionScreen(
    modifier: Modifier = Modifier,
    actionUiState: ActionUiState,
    onFinish: () -> Unit,
    onUpdateEblanAction: suspend (
        resId: Int,
        eblanAction: EblanAction,
    ) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.yagni_launcher_action))
                },
                navigationIcon = {
                    IconButton(onClick = onFinish) {
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
            if (actionUiState is ActionUiState.Success) {
                Success(
                    eblanApplicationInfos = actionUiState.eblanApplicationInfos,
                    onUpdateEblanAction = onUpdateEblanAction,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Success(
    modifier: Modifier = Modifier,
    eblanApplicationInfos: List<EblanApplicationInfo>,
    onUpdateEblanAction: suspend (
        resId: Int,
        eblanAction: EblanAction,
    ) -> Unit,
) {
    val context = LocalContext.current

    val scope = rememberCoroutineScope()

    var showSelectApplicationDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .fillMaxSize(),
    ) {
        EblanActionType.entries.forEach { eblanActionType ->
            ListItem(
                modifier = Modifier
                    .clickable {
                        scope.launch {
                            if (eblanActionType == EblanActionType.OpenApp) {
                                showSelectApplicationDialog = true
                            } else {
                                onUpdateEblanAction(
                                    eblanActionType.getResId(),
                                    EblanAction(
                                        eblanActionType = eblanActionType,
                                        serialNumber = 0L,
                                        componentName = "",
                                    ),
                                )
                            }
                        }
                    }
                    .fillMaxWidth()
                    .padding(10.dp),
                headlineContent = {
                    Text(
                        text = eblanActionType.getEblanActionTypeSubtitle(
                            context = context,
                            componentName = "",
                        ),
                    )
                },
                leadingContent = {
                    Icon(
                        imageVector = ImageVector.vectorResource(eblanActionType.getResId()),
                        contentDescription = null,
                    )
                },
            )
        }
    }

    if (showSelectApplicationDialog) {
        SelectApplicationDialog(
            eblanApplicationInfos = eblanApplicationInfos,
            onDismissRequest = {
                showSelectApplicationDialog = false
            },
            onClick = {
                showSelectApplicationDialog = false

                scope.launch {
                    onUpdateEblanAction(
                        R.drawable.adb_24px,
                        EblanAction(
                            eblanActionType = EblanActionType.OpenApp,
                            serialNumber = it.serialNumber,
                            componentName = it.componentName,
                        ),
                    )
                }
            },
        )
    }
}

private fun EblanActionType.getResId() = when (this) {
    EblanActionType.None -> R.drawable.adb_24px
    EblanActionType.OpenApp -> R.drawable.adb_24px
    EblanActionType.OpenAppDrawer -> R.drawable.outline_apps_24
    EblanActionType.OpenNotificationPanel -> R.drawable.notification_settings_24px
    EblanActionType.LockScreen -> R.drawable.lock_24px
    EblanActionType.OpenQuickSettings -> R.drawable.settings_24px
    EblanActionType.OpenRecents -> R.drawable.preview_24px
}
