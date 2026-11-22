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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.eblan.launcher.designsystem.component.EblanDialog
import com.eblan.launcher.domain.model.application.EblanApplicationInfo
import com.eblan.launcher.ui.R
import com.eblan.launcher.common.R as commonR

@Composable
fun SelectApplicationsDialog(
    // Data
    eblanApplicationInfos: List<EblanApplicationInfo>,
    // Layout
    modifier: Modifier = Modifier,
    // Callbacks
    onDismissRequest: () -> Unit,
    onAddApplications: (List<EblanApplicationInfo>) -> Unit,
) {
    var selectedEblanApplicationInfos by remember {
        mutableStateOf(emptySet<EblanApplicationInfo>())
    }

    EblanDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
    ) {
        Text(
            text = stringResource(R.string.select_applications),
            style = MaterialTheme.typography.titleLarge,
        )

        EblanApplicationInfosPage(
            modifier = Modifier.weight(
                weight = 1f,
                fill = false,
            ),
            eblanApplicationInfos = eblanApplicationInfos,
            selectedEblanApplicationInfos = selectedEblanApplicationInfos,
            onToggleApplicationInfo = { currentEblanApplicationInfo ->
                selectedEblanApplicationInfos =
                    if (currentEblanApplicationInfo in selectedEblanApplicationInfos) {
                        selectedEblanApplicationInfos - currentEblanApplicationInfo
                    } else {
                        selectedEblanApplicationInfos + currentEblanApplicationInfo
                    }
            },
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(
                onClick = onDismissRequest,
            ) {
                Text(text = stringResource(commonR.string.cancel))
            }

            TextButton(
                enabled = selectedEblanApplicationInfos.isNotEmpty(),
                onClick = {
                    onAddApplications(selectedEblanApplicationInfos.toList())

                    onDismissRequest()
                },
            ) {
                Text(text = stringResource(commonR.string.add))
            }
        }
    }
}

@Composable
private fun EblanApplicationInfosPage(
    modifier: Modifier = Modifier,
    eblanApplicationInfos: List<EblanApplicationInfo>,
    selectedEblanApplicationInfos: Set<EblanApplicationInfo>,
    onToggleApplicationInfo: (EblanApplicationInfo) -> Unit,
) {
    LazyColumn(modifier = modifier) {
        items(
            items = eblanApplicationInfos,
            key = { currentEblanApplicationInfo ->
                "${currentEblanApplicationInfo.componentName} ${currentEblanApplicationInfo.serialNumber}"
            },
        ) { eblanApplicationInfo ->
            val checked = eblanApplicationInfo in selectedEblanApplicationInfos

            ListItem(
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                headlineContent = { Text(text = eblanApplicationInfo.label) },
                leadingContent = {
                    AsyncImage(
                        model = eblanApplicationInfo.icon,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                    )
                },
                trailingContent = {
                    Checkbox(
                        checked = checked,
                        onCheckedChange = null,
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .toggleable(
                        value = checked,
                        role = Role.Checkbox,
                        onValueChange = {
                            onToggleApplicationInfo(eblanApplicationInfo)
                        },
                    ),
            )
        }
    }
}
