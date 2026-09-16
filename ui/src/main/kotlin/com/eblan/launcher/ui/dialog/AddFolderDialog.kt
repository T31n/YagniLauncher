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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.eblan.launcher.designsystem.component.EblanDialog
import com.eblan.launcher.domain.model.folder.FolderEblanApplicationInfo
import com.eblan.launcher.ui.R
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import com.eblan.launcher.common.R as commonR

@OptIn(ExperimentalUuidApi::class)
@Composable
fun AddFolderDialog(
    modifier: Modifier = Modifier,
    topLevelFolderEblanApplicationInfos: List<FolderEblanApplicationInfo>,
    onDismissRequest: () -> Unit,
    onAddFolderEblanApplicationInfo: (FolderEblanApplicationInfo) -> Unit,
) {
    val maxIndex = remember(key1 = topLevelFolderEblanApplicationInfos) {
        topLevelFolderEblanApplicationInfos.maxOfOrNull { it.index } ?: 0
    }

    var value by remember { mutableStateOf("") }

    var isError by remember { mutableStateOf(false) }

    EblanDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
    ) {
        Text(
            text = stringResource(R.string.add_folder),
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
                Text(text = stringResource(R.string.add_folder))
            },
            isError = isError,
            supportingText = if (isError) {
                {
                    Text(text = stringResource(R.string.folder_is_not_valid))
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
                        onAddFolderEblanApplicationInfo(
                            FolderEblanApplicationInfo(
                                id = Uuid.random().toHexString(),
                                icon = null,
                                label = value,
                                index = maxIndex + 1,
                                folderIndex = -1,
                                folderId = null,
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
