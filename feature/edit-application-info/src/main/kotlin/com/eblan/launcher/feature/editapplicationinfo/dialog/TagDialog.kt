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
package com.eblan.launcher.feature.editapplicationinfo.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import com.eblan.launcher.designsystem.component.EblanDialog
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.domain.model.EblanApplicationInfoTag
import com.eblan.launcher.domain.model.EblanApplicationInfoTagUi
import com.eblan.launcher.feature.editapplicationinfo.R
import com.eblan.launcher.common.R as commonR

@Composable
internal fun AddTagDialog(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    onAddEblanApplicationInfoTag: (EblanApplicationInfoTag) -> Unit,
) {
    var value by remember { mutableStateOf("") }

    var isError by remember { mutableStateOf(false) }

    EblanDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
    ) {
        Text(
            text = stringResource(R.string.add_tag),
            style = MaterialTheme.typography.titleLarge,
        )

        TextField(
            value = value,
            onValueChange = {
                value = it
                isError = false
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(text = stringResource(R.string.add_tag))
            },
            isError = isError,
            supportingText = if (isError) {
                {
                    Text(text = stringResource(R.string.tag_is_not_valid))
                }
            } else {
                null
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
                onClick = {
                    if (value.isNotBlank()) {
                        onAddEblanApplicationInfoTag(
                            EblanApplicationInfoTag(
                                name = value,
                            ),
                        )

                        onDismissRequest()
                    } else {
                        isError = true
                    }
                },
            ) {
                Text(text = stringResource(commonR.string.add))
            }
        }
    }
}

@Composable
internal fun UpdateTagDialog(
    modifier: Modifier = Modifier,
    eblanApplicationInfoTagUi: EblanApplicationInfoTagUi?,
    onDeleteEblanApplicationInfoTag: (EblanApplicationInfoTag) -> Unit,
    onDismissRequest: () -> Unit,
    onUpdateEblanApplicationInfoTag: (EblanApplicationInfoTag) -> Unit,
) {
    if (eblanApplicationInfoTagUi == null) return

    var value by remember { mutableStateOf(eblanApplicationInfoTagUi.name) }

    var isError by remember { mutableStateOf(false) }

    EblanDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.update_tag),
                style = MaterialTheme.typography.titleLarge,
            )

            IconButton(
                onClick = {
                    onDeleteEblanApplicationInfoTag(
                        EblanApplicationInfoTag(
                            id = eblanApplicationInfoTagUi.id,
                            name = eblanApplicationInfoTagUi.name,
                        ),
                    )

                    onDismissRequest()
                },
            ) {
                Icon(
                    imageVector = EblanLauncherIcons.Delete,
                    contentDescription = null,
                )
            }
        }

        TextField(
            value = value,
            onValueChange = {
                value = it
                isError = false
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(text = stringResource(R.string.update_tag))
            },
            supportingText = {
                if (isError) {
                    Text(text = stringResource(R.string.tag_is_not_valid))
                }
            },
            isError = isError,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
            ),
            singleLine = true,
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
                onClick = {
                    if (value.isNotBlank()) {
                        onUpdateEblanApplicationInfoTag(
                            EblanApplicationInfoTag(
                                id = eblanApplicationInfoTagUi.id,
                                name = value,
                            ),
                        )

                        onDismissRequest()
                    } else {
                        isError = true
                    }
                },
            ) {
                Text(text = stringResource(commonR.string.update))
            }
        }
    }
}
