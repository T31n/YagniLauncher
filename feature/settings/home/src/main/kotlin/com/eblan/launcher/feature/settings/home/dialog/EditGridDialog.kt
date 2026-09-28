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
package com.eblan.launcher.feature.settings.home.dialog

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
import com.eblan.launcher.common.R as commonR

@Composable
internal fun EditGridDialog(
    modifier: Modifier = Modifier,
    columns: Int,
    rows: Int,
    onDismissRequest: () -> Unit,
    onUpdateGrid: (
        columns: Int,
        rows: Int,
    ) -> Unit,
) {
    var currentColumns by remember { mutableStateOf("$columns") }
    var currentRows by remember { mutableStateOf("$rows") }

    var isErrorColumns by remember { mutableStateOf(false) }
    var isErrorRows by remember { mutableStateOf(false) }

    EblanDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
    ) {
        Text(
            text = stringResource(commonR.string.grid),
            style = MaterialTheme.typography.titleLarge,
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TextField(
                value = currentColumns,
                onValueChange = {
                    currentColumns = it
                    isErrorColumns = false
                },
                modifier = Modifier.weight(1f),
                label = { Text(text = stringResource(commonR.string.columns)) },
                supportingText = if (isErrorColumns) {
                    {
                        Text(text = stringResource(commonR.string.columns_is_not_valid))
                    }
                } else {
                    null
                },
                isError = isErrorColumns,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                ),
            )

            TextField(
                value = currentRows,
                onValueChange = {
                    currentRows = it
                    isErrorRows = false
                },
                modifier = Modifier.weight(1f),
                label = { Text(text = stringResource(commonR.string.rows)) },
                supportingText = if (isErrorRows) {
                    {
                        Text(text = stringResource(commonR.string.rows_is_not_valid))
                    }
                } else {
                    null
                },
                isError = isErrorRows,
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
                    val newColumns = currentColumns.toIntOrNull()
                    val newRows = currentRows.toIntOrNull()

                    isErrorColumns = newColumns == null || newColumns <= 0
                    isErrorRows = newRows == null || newRows <= 0

                    if (newColumns != null &&
                        newRows != null &&
                        newColumns > 0 &&
                        newRows > 0
                    ) {
                        onUpdateGrid(newColumns, newRows)

                        onDismissRequest()
                    }
                },
            ) {
                Text(text = stringResource(commonR.string.update))
            }
        }
    }
}
