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
import com.eblan.launcher.designsystem.component.EblanDialog
import com.eblan.launcher.feature.settings.home.R
import com.eblan.launcher.common.R as commonR

@Composable
internal fun EditDockHeightDialog(
    modifier: Modifier = Modifier,
    dockHeight: Int,
    onDismissRequest: () -> Unit,
    onUpdateDockHeight: (Int) -> Unit,
) {
    var currentDockHeight by remember { mutableStateOf("$dockHeight") }

    var isErrorDockHeight by remember { mutableStateOf(false) }

    EblanDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
    ) {
        Text(
            text = stringResource(R.string.dock_height),
            style = MaterialTheme.typography.titleLarge,
        )

        TextField(
            value = currentDockHeight,
            onValueChange = {
                currentDockHeight = it
                isErrorDockHeight = false
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(text = stringResource(R.string.dock_height))
            },
            isError = isErrorDockHeight,
            supportingText = if (isErrorDockHeight) {
                {
                    Text(text = stringResource(R.string.dock_height_is_not_valid))
                }
            } else {
                null
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
            ),
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
                    val newDockHeight = currentDockHeight.toIntOrNull()

                    isErrorDockHeight = newDockHeight == null || newDockHeight < 0

                    if (newDockHeight != null && newDockHeight >= 0) {
                        onUpdateDockHeight(newDockHeight)
                    }
                },
            ) {
                Text(text = stringResource(commonR.string.update))
            }
        }
    }
}
