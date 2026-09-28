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
package com.eblan.launcher.feature.settings.folder.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.eblan.launcher.designsystem.component.EblanDialog
import com.eblan.launcher.feature.settings.folder.R
import com.eblan.launcher.common.R as commonR

@Composable
internal fun EditFolderCellDimensionDialog(
    modifier: Modifier = Modifier,
    folderCellWidth: Int,
    folderCellHeight: Int,
    onDismissRequest: () -> Unit,
    onUpdateFolderCellDimension: (
        folderCellWidth: Int,
        folderCellHeight: Int,
    ) -> Unit,
) {
    var currentFolderCellWidth by remember { mutableStateOf("$folderCellWidth") }
    var currentFolderCellHeight by remember { mutableStateOf("$folderCellHeight") }

    var isErrorFolderCellWidth by remember { mutableStateOf(false) }
    var isErrorFolderCellHeight by remember { mutableStateOf(false) }

    EblanDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
    ) {
        Text(
            text = stringResource(R.string.folder_cell_dimension),
            style = MaterialTheme.typography.titleLarge,
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TextField(
                value = currentFolderCellWidth,
                onValueChange = {
                    currentFolderCellWidth = it
                    isErrorFolderCellWidth = false
                },
                modifier = Modifier.weight(1f),
                label = { Text(text = stringResource(R.string.cell_width)) },
                supportingText = if (isErrorFolderCellWidth) {
                    {
                        Text(text = stringResource(R.string.cell_width_is_not_valid))
                    }
                } else {
                    null
                },
                isError = isErrorFolderCellWidth,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                ),
            )

            TextField(
                value = currentFolderCellHeight,
                onValueChange = {
                    currentFolderCellHeight = it
                    isErrorFolderCellHeight = false
                },
                modifier = Modifier.weight(1f),
                label = { Text(text = stringResource(R.string.cell_height)) },
                supportingText = if (isErrorFolderCellHeight) {
                    {
                        Text(text = stringResource(R.string.cell_height_is_not_valid))
                    }
                } else {
                    null
                },
                isError = isErrorFolderCellHeight,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                ),
            )
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

            TextButton(
                onClick = {
                    val newWidth = currentFolderCellWidth.toIntOrNull()
                    val newHeight = currentFolderCellHeight.toIntOrNull()

                    isErrorFolderCellWidth = newWidth == null || newWidth <= 0
                    isErrorFolderCellHeight = newHeight == null || newHeight <= 0

                    if (newWidth != null &&
                        newHeight != null &&
                        newWidth > 0 &&
                        newHeight > 0
                    ) {
                        onUpdateFolderCellDimension(newWidth, newHeight)

                        onDismissRequest()
                    }
                },
            ) {
                Text(text = stringResource(commonR.string.update))
            }
        }
    }
}
