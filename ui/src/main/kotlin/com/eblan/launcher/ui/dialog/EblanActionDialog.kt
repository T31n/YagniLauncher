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
package com.eblan.launcher.ui.dialog

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.eblan.launcher.designsystem.component.EblanDialog
import com.eblan.launcher.designsystem.component.EblanRadioButton
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.domain.model.application.EblanApplicationInfo
import com.eblan.launcher.domain.model.userdata.EblanAction
import com.eblan.launcher.domain.model.userdata.EblanActionType
import com.eblan.launcher.ui.R
import com.eblan.launcher.ui.settings.getEblanActionTypeSubtitle
import com.eblan.launcher.ui.settings.rememberIsAccessibilityServiceEnabled
import com.eblan.launcher.common.R as commonR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EblanActionDialog(
    modifier: Modifier = Modifier,
    title: String,
    eblanAction: EblanAction,
    eblanApplicationInfos: List<EblanApplicationInfo>,
    onSelectEblanAction: (EblanAction) -> Unit,
    onDismissRequest: () -> Unit,
) {
    val context = LocalContext.current

    var selectedEblanAction by remember { mutableStateOf(eblanAction) }

    var showSelectApplicationDialog by remember { mutableStateOf(false) }

    val isAccessibilityServiceEnabled by rememberIsAccessibilityServiceEnabled()

    EblanDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
        )

        AccessibilityServiceCard()

        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .weight(1f, fill = false)
                .selectableGroup()
                .fillMaxWidth(),
        ) {
            EblanActionType.entries.forEach { eblanActionType ->
                EblanRadioButton(
                    enabled = when (eblanActionType) {
                        EblanActionType.OpenNotificationPanel,
                        EblanActionType.LockScreen,
                        EblanActionType.OpenQuickSettings,
                        EblanActionType.OpenRecents,
                        -> isAccessibilityServiceEnabled

                        else -> true
                    },
                    selected = selectedEblanAction.eblanActionType == eblanActionType,
                    text = eblanActionType.getEblanActionTypeSubtitle(
                        context = context,
                        componentName = selectedEblanAction.componentName,
                    ),
                    onClick = {
                        if (eblanActionType == EblanActionType.OpenApp) {
                            showSelectApplicationDialog = true
                        } else {
                            selectedEblanAction = EblanAction(
                                eblanActionType = eblanActionType,
                                serialNumber = 0L,
                                componentName = "",
                            )
                        }
                    },
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(
                onClick = onDismissRequest,
            ) {
                Text(text = stringResource(commonR.string.cancel))
            }

            Spacer(
                modifier = Modifier.width(5.dp),
            )

            TextButton(
                onClick = {
                    onSelectEblanAction(selectedEblanAction)
                },
            ) {
                Text(text = stringResource(commonR.string.update))
            }
        }
    }

    if (showSelectApplicationDialog) {
        SelectApplicationDialog(
            eblanApplicationInfos = eblanApplicationInfos,
            onDismissRequest = {
                showSelectApplicationDialog = false
            },
            onClick = {
                selectedEblanAction = EblanAction(
                    eblanActionType = EblanActionType.OpenApp,
                    serialNumber = it.serialNumber,
                    componentName = it.componentName,
                )

                showSelectApplicationDialog = false
            },
        )
    }
}

@Composable
private fun AccessibilityServiceCard(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(10.dp),
        onClick = {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)

            context.startActivity(intent)
        },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(imageVector = EblanLauncherIcons.Info, contentDescription = null)

            Spacer(modifier = Modifier.width(5.dp))

            Text(
                modifier = Modifier,
                text = stringResource(R.string.enable_the_accessibility_service_permission_to_use_additional_actions),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
