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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.eblan.launcher.designsystem.component.EblanDialog
import com.eblan.launcher.domain.model.FolderEblanApplicationInfo
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import com.eblan.launcher.common.R as commonR

@OptIn(ExperimentalUuidApi::class)
@Composable
internal fun AddFolderDialog(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    onAddFolderEblanApplicationInfo: (FolderEblanApplicationInfo) -> Unit,
) {
    val scope = rememberCoroutineScope()

    var value by remember { mutableStateOf("") }

    var isError by remember { mutableStateOf(false) }

    EblanDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
    ) {
        Text(
            text = "Add Folder",
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
                Text(text = "Add Folder")
            },
            isError = isError,
            supportingText = if (isError) {
                {
                    Text(text = "Folder is not valid")
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
                        scope.launch {
                            onAddFolderEblanApplicationInfo(
                                FolderEblanApplicationInfo(
                                    id = Uuid.random().toHexString(),
                                    icon = null,
                                    label = value,
                                    folderIndex = -1,
                                    folderId = null,
                                ),
                            )

                            onDismissRequest()
                        }
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
