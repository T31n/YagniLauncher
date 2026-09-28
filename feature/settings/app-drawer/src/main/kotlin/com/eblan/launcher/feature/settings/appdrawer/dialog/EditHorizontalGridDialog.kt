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
import com.eblan.launcher.feature.settings.appdrawer.R
import com.eblan.launcher.common.R as commonR

@Composable
internal fun EditHorizontalGridDialog(
    modifier: Modifier = Modifier,
    horizontalAppDrawerColumns: Int,
    horizontalAppDrawerRows: Int,
    onDismissRequest: () -> Unit,
    onUpdateHorizontalGrid: (
        horizontalAppDrawerColumns: Int,
        horizontalAppDrawerRows: Int,
    ) -> Unit,
) {
    var currentHorizontalAppDrawerColumns by remember { mutableStateOf("$horizontalAppDrawerColumns") }
    var currentHorizontalAppDrawerRows by remember { mutableStateOf("$horizontalAppDrawerRows") }

    var isErrorHorizontalAppDrawerColumns by remember { mutableStateOf(false) }
    var isErrorHorizontalAppDrawerRows by remember { mutableStateOf(false) }

    EblanDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
    ) {
        Text(
            text = stringResource(R.string.horizontal_grid),
            style = MaterialTheme.typography.titleLarge,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TextField(
                value = currentHorizontalAppDrawerColumns,
                onValueChange = {
                    currentHorizontalAppDrawerColumns = it
                    isErrorHorizontalAppDrawerColumns = false
                },
                modifier = Modifier.weight(1f),
                label = { Text(text = stringResource(commonR.string.columns)) },
                supportingText = if (isErrorHorizontalAppDrawerColumns) {
                    {
                        Text(text = stringResource(commonR.string.columns_is_not_valid))
                    }
                } else {
                    null
                },
                isError = isErrorHorizontalAppDrawerColumns,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                ),
            )

            TextField(
                value = currentHorizontalAppDrawerRows,
                onValueChange = {
                    currentHorizontalAppDrawerRows = it
                    isErrorHorizontalAppDrawerRows = false
                },
                modifier = Modifier.weight(1f),
                label = { Text(text = stringResource(commonR.string.rows)) },
                supportingText = if (isErrorHorizontalAppDrawerRows) {
                    {
                        Text(text = stringResource(commonR.string.rows_is_not_valid))
                    }
                } else {
                    null
                },
                isError = isErrorHorizontalAppDrawerRows,
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
                    val newHorizontalAppDrawerColumns =
                        currentHorizontalAppDrawerColumns.toIntOrNull()
                    val newHorizontalAppDrawerRows = currentHorizontalAppDrawerRows.toIntOrNull()

                    isErrorHorizontalAppDrawerColumns =
                        newHorizontalAppDrawerColumns == null || newHorizontalAppDrawerColumns <= 2
                    isErrorHorizontalAppDrawerRows =
                        newHorizontalAppDrawerRows == null || newHorizontalAppDrawerRows <= 2

                    if (newHorizontalAppDrawerColumns != null && newHorizontalAppDrawerRows != null &&
                        newHorizontalAppDrawerColumns > 2 && newHorizontalAppDrawerRows > 2
                    ) {
                        onUpdateHorizontalGrid(
                            newHorizontalAppDrawerColumns,
                            newHorizontalAppDrawerRows,
                        )

                        onDismissRequest()
                    }
                },
            ) {
                Text(text = stringResource(commonR.string.update))
            }
        }
    }
}
