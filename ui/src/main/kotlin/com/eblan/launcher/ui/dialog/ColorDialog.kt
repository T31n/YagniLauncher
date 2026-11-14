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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.eblan.launcher.common.R.string.cancel
import com.eblan.launcher.common.R.string.update
import com.eblan.launcher.designsystem.component.EblanDialog
import com.eblan.launcher.designsystem.component.EblanRadioButton
import com.eblan.launcher.domain.model.userdata.BackgroundColor
import com.eblan.launcher.domain.model.userdata.IconTint
import com.eblan.launcher.common.R as commonR

@Composable
fun BackgroundColorDialog(
    // Content
    title: String,
    // State
    backgroundColor: BackgroundColor,
    customBackgroundColor: Int,
    // Layout
    modifier: Modifier = Modifier,
    // Actions
    onDismissRequest: () -> Unit,
    onUpdateClick: (
        backgroundColor: BackgroundColor,
        customColor: Int,
    ) -> Unit,
) {
    var selectedColor by remember { mutableStateOf(value = backgroundColor) }
    var selectedCustomColor by remember { mutableIntStateOf(value = customBackgroundColor) }
    var showColorPickerDialog by remember { mutableStateOf(false) }

    EblanDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
        )

        Column(
            modifier = Modifier
                .selectableGroup()
                .fillMaxWidth(),
        ) {
            BackgroundColor.entries.forEach { entry ->
                EblanRadioButton(
                    selected = selectedColor == entry,
                    text = entry.getTitle(),
                    onClick = {
                        if (entry == BackgroundColor.Custom) {
                            showColorPickerDialog = true
                        } else {
                            selectedColor = entry
                        }
                    },
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(
                onClick = onDismissRequest,
            ) {
                Text(text = stringResource(cancel))
            }

            TextButton(
                onClick = {
                    onUpdateClick(
                        selectedColor,
                        selectedCustomColor,
                    )

                    onDismissRequest()
                },
            ) {
                Text(text = stringResource(update))
            }
        }
    }

    if (showColorPickerDialog) {
        ColorPickerDialog(
            // Content
            title = title,
            customColor = customBackgroundColor,
            // Callbacks
            onDismissRequest = {
                showColorPickerDialog = false
            },
            onSelectColor = {
                selectedColor = BackgroundColor.Custom
                selectedCustomColor = it
            },
        )
    }
}

@Composable
fun BackgroundColor.getTitle() = when (this) {
    BackgroundColor.System -> stringResource(commonR.string.system)
    BackgroundColor.Light -> stringResource(commonR.string.light)
    BackgroundColor.Dark -> stringResource(commonR.string.dark)
    BackgroundColor.Custom -> stringResource(commonR.string.custom)
}

@Composable
fun IconTint.getTitle() = when (this) {
    IconTint.None -> stringResource(commonR.string.none)
    IconTint.Custom -> stringResource(commonR.string.custom)
}
