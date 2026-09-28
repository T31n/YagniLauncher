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
internal fun EditVerticalGridDialog(
    modifier: Modifier = Modifier,
    appDrawerColumns: Int,
    appDrawerRowsHeight: Int,
    onDismissRequest: () -> Unit,
    onUpdateVerticalGrid: (
        appDrawerColumns: Int,
        appDrawerRowsHeight: Int,
    ) -> Unit,
) {
    var currentAppDrawerColumns by remember { mutableStateOf("$appDrawerColumns") }
    var currentAppDrawerRowsHeight by remember { mutableStateOf("$appDrawerRowsHeight") }

    var isErrorAppDrawerColumns by remember { mutableStateOf(false) }
    var isErrorAppDrawerRowsHeight by remember { mutableStateOf(false) }

    EblanDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
    ) {
        Text(
            text = stringResource(R.string.vertical_grid),
            style = MaterialTheme.typography.titleLarge,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TextField(
                value = currentAppDrawerColumns,
                onValueChange = {
                    currentAppDrawerColumns = it
                    isErrorAppDrawerColumns = false
                },
                modifier = Modifier.weight(1f),
                label = { Text(text = stringResource(commonR.string.columns)) },
                supportingText = if (isErrorAppDrawerColumns) {
                    {
                        Text(text = stringResource(commonR.string.columns_is_not_valid))
                    }
                } else {
                    null
                },
                isError = isErrorAppDrawerColumns,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                ),
            )

            TextField(
                value = currentAppDrawerRowsHeight,
                onValueChange = {
                    currentAppDrawerRowsHeight = it
                    isErrorAppDrawerRowsHeight = false
                },
                modifier = Modifier.weight(1f),
                label = { Text(text = stringResource(R.string.rows_height)) },
                supportingText = if (isErrorAppDrawerRowsHeight) {
                    {
                        Text(text = stringResource(R.string.rows_height_is_not_valid))
                    }
                } else {
                    null
                },
                isError = isErrorAppDrawerRowsHeight,
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
                    val newColumns = currentAppDrawerColumns.toIntOrNull()
                    val newRowsHeight = currentAppDrawerRowsHeight.toIntOrNull()

                    isErrorAppDrawerColumns = newColumns == null || newColumns <= 0
                    isErrorAppDrawerRowsHeight = newRowsHeight == null || newRowsHeight <= 0

                    if (newColumns != null &&
                        newRowsHeight != null &&
                        newColumns > 0 &&
                        newRowsHeight > 0
                    ) {
                        onUpdateVerticalGrid(
                            newColumns,
                            newRowsHeight,
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
