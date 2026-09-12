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
package com.eblan.launcher.feature.settings.appdrawer.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.eblan.launcher.designsystem.component.EblanDialog
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.domain.model.application.EblanApplicationInfo
import com.eblan.launcher.feature.settings.appdrawer.R
import com.eblan.launcher.common.R as commonR

@Composable
internal fun HiddenEblanApplicationInfosDialog(
    modifier: Modifier = Modifier,
    eblanApplicationInfos: List<EblanApplicationInfo>,
    onDismissRequest: () -> Unit,
    onUpdateEblanApplicationInfo: (EblanApplicationInfo) -> Unit,
) {
    EblanDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
    ) {
        Text(
            text = stringResource(R.string.hidden_applications),
            style = MaterialTheme.typography.titleLarge,
        )

        when {
            eblanApplicationInfos.isEmpty() -> {
                Text(
                    text = stringResource(R.string.no_hidden_applications),
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.weight(
                        weight = 1f,
                        fill = false,
                    ),
                ) {
                    items(eblanApplicationInfos) { eblanApplicationInfo ->
                        ListItem(
                            headlineContent = {
                                Text(text = eblanApplicationInfo.label)
                            },
                            leadingContent = {
                                AsyncImage(
                                    model = eblanApplicationInfo.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
                                )
                            },
                            trailingContent = {
                                IconButton(
                                    onClick = {
                                        onUpdateEblanApplicationInfo(
                                            eblanApplicationInfo.copy(
                                                isHidden = false,
                                            ),
                                        )
                                    },
                                ) {
                                    Icon(
                                        imageVector = EblanLauncherIcons.Delete,
                                        contentDescription = null,
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(
                onClick = onDismissRequest,
            ) {
                Text(text = stringResource(commonR.string.okay))
            }
        }
    }
}
