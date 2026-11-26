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
package com.eblan.launcher.feature.settings.general.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.eblan.launcher.designsystem.component.EblanDialog
import com.eblan.launcher.designsystem.theme.EblanSpacing
import com.eblan.launcher.feature.settings.general.R
import com.eblan.launcher.ui.settings.produceIconPackInfosState
import com.eblan.launcher.common.R as commonR

@Composable
internal fun ImportIconPackInfoDialog(
    // Layout and Appearance
    modifier: Modifier = Modifier,
    // Actions
    onDismissRequest: () -> Unit,
    onUpdateIconPackInfo: (String) -> Unit,
) {
    val packageManagerIconPacks by produceIconPackInfosState()

    EblanDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
    ) {
        Text(
            text = stringResource(R.string.import_icon_pack),
            style = MaterialTheme.typography.titleLarge,
        )

        val currentIconPacks = packageManagerIconPacks

        when {
            currentIconPacks == null -> {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(EblanSpacing.medium),
                )
            }

            currentIconPacks.isEmpty() -> {
                Text(
                    text = stringResource(R.string.no_icon_packs),
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.weight(
                        weight = 1f,
                        fill = false,
                    ),
                ) {
                    items(currentIconPacks) { currentPackageManagerIconPack ->
                        ListItem(
                            colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                            headlineContent = {
                                Text(text = currentPackageManagerIconPack.label)
                            },
                            leadingContent = {
                                AsyncImage(
                                    model = currentPackageManagerIconPack.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
                                )
                            },
                            modifier = Modifier
                                .clickable {
                                    onUpdateIconPackInfo(currentPackageManagerIconPack.packageName)

                                    onDismissRequest()
                                }
                                .fillMaxWidth(),
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
                Text(text = stringResource(commonR.string.cancel))
            }
        }
    }
}
