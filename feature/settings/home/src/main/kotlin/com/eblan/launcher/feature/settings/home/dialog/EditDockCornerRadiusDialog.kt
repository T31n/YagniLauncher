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
import com.eblan.launcher.feature.settings.home.R
import com.eblan.launcher.common.R as commonR

@Composable
internal fun EditDockCornerRadiusDialog(
    modifier: Modifier = Modifier,
    dockTopStartCornerRadius: Int,
    dockTopEndCornerRadius: Int,
    dockBottomStartCornerRadius: Int,
    dockBottomEndCornerRadius: Int,
    onDismissRequest: () -> Unit,
    onUpdateCornerRadius: (
        dockTopStartCornerRadius: Int,
        dockTopEndCornerRadius: Int,
        dockBottomStartCornerRadius: Int,
        dockBottomEndCornerRadius: Int,
    ) -> Unit,
) {
    var currentDockTopStartCornerRadius by remember { mutableStateOf("$dockTopStartCornerRadius") }
    var currentDockTopEndCornerRadius by remember { mutableStateOf("$dockTopEndCornerRadius") }
    var currentDockBottomStartCornerRadius by remember { mutableStateOf("$dockBottomStartCornerRadius") }
    var currentDockBottomEndCornerRadius by remember { mutableStateOf("$dockBottomEndCornerRadius") }

    var isErrorDockTopStartCornerRadius by remember { mutableStateOf(false) }
    var isErrorDockTopEndCornerRadius by remember { mutableStateOf(false) }
    var isErrorDockBottomStartCornerRadius by remember { mutableStateOf(false) }
    var isErrorDockBottomEndCornerRadius by remember { mutableStateOf(false) }

    EblanDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
    ) {
        Text(
            text = stringResource(R.string.dock_corner_radius),
            style = MaterialTheme.typography.titleLarge,
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TextField(
                value = currentDockTopStartCornerRadius,
                onValueChange = {
                    currentDockTopStartCornerRadius = it
                    isErrorDockTopStartCornerRadius = false
                },
                modifier = Modifier.weight(1f),
                label = { Text(text = stringResource(R.string.top_start)) },
                supportingText = if (isErrorDockTopStartCornerRadius) {
                    {
                        Text(text = stringResource(R.string.top_start_is_not_valid))
                    }
                } else {
                    null
                },
                isError = isErrorDockTopStartCornerRadius,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                ),
            )

            TextField(
                value = currentDockTopEndCornerRadius,
                onValueChange = {
                    currentDockTopEndCornerRadius = it
                    isErrorDockTopEndCornerRadius = false
                },
                modifier = Modifier.weight(1f),
                label = { Text(text = stringResource(R.string.top_end)) },
                supportingText = if (isErrorDockTopEndCornerRadius) {
                    {
                        Text(text = stringResource(R.string.top_end_is_not_valid))
                    }
                } else {
                    null
                },
                isError = isErrorDockTopEndCornerRadius,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                ),
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TextField(
                value = currentDockBottomStartCornerRadius,
                onValueChange = {
                    currentDockBottomStartCornerRadius = it
                    isErrorDockBottomStartCornerRadius = false
                },
                modifier = Modifier.weight(1f),
                label = { Text(text = stringResource(R.string.bottom_start)) },
                supportingText = if (isErrorDockBottomStartCornerRadius) {
                    {
                        Text(text = stringResource(R.string.bottom_start_is_not_valid))
                    }
                } else {
                    null
                },
                isError = isErrorDockBottomStartCornerRadius,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                ),
            )

            TextField(
                value = currentDockBottomEndCornerRadius,
                onValueChange = {
                    currentDockBottomEndCornerRadius = it
                    isErrorDockBottomEndCornerRadius = false
                },
                modifier = Modifier.weight(1f),
                label = { Text(text = stringResource(R.string.bottom_end)) },
                supportingText = if (isErrorDockBottomEndCornerRadius) {
                    {
                        Text(text = stringResource(R.string.bottom_end_is_not_valid))
                    }
                } else {
                    null
                },
                isError = isErrorDockBottomEndCornerRadius,
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
                    val newDockTopStartCornerRadius = currentDockTopStartCornerRadius.toIntOrNull()
                    val newDockTopEndCornerRadius = currentDockTopEndCornerRadius.toIntOrNull()
                    val newDockBottomStartCornerRadius =
                        currentDockBottomStartCornerRadius.toIntOrNull()
                    val newDockBottomEndCornerRadius =
                        currentDockBottomEndCornerRadius.toIntOrNull()

                    isErrorDockTopStartCornerRadius = newDockTopStartCornerRadius == null ||
                        newDockTopStartCornerRadius < 0
                    isErrorDockTopEndCornerRadius = newDockTopEndCornerRadius == null ||
                        newDockTopEndCornerRadius < 0
                    isErrorDockBottomStartCornerRadius = newDockBottomStartCornerRadius == null ||
                        newDockBottomStartCornerRadius < 0
                    isErrorDockBottomEndCornerRadius = newDockBottomEndCornerRadius == null ||
                        newDockBottomEndCornerRadius < 0

                    if (newDockTopStartCornerRadius != null && newDockTopStartCornerRadius >= 0 &&
                        newDockTopEndCornerRadius != null && newDockTopEndCornerRadius >= 0 &&
                        newDockBottomStartCornerRadius != null && newDockBottomStartCornerRadius >= 0 &&
                        newDockBottomEndCornerRadius != null && newDockBottomEndCornerRadius >= 0
                    ) {
                        onUpdateCornerRadius(
                            newDockTopStartCornerRadius,
                            newDockTopEndCornerRadius,
                            newDockBottomStartCornerRadius,
                            newDockBottomEndCornerRadius,
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
