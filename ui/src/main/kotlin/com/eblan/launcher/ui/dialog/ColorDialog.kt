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
import com.eblan.launcher.designsystem.component.EblanDialog
import com.eblan.launcher.designsystem.component.EblanRadioButton
import com.eblan.launcher.domain.model.userdata.BackgroundColor
import com.eblan.launcher.domain.model.userdata.TextColor
import com.eblan.launcher.ui.R
import com.eblan.launcher.common.R as commonR

@Composable
fun TextColorDialog(
    modifier: Modifier = Modifier,
    title: String,
    textColor: TextColor,
    customTextColor: Int,
    onDismissRequest: () -> Unit,
    onUpdateClick: (
        textColor: TextColor,
        customColor: Int,
    ) -> Unit,
) {
    ColorDialog(
        modifier = modifier,
        title = title,
        color = textColor,
        customColor = customTextColor,
        entries = TextColor.entries,
        customEntry = TextColor.Custom,
        getTitle = { it.getTextColorTitle() },
        onDismissRequest = onDismissRequest,
        onUpdateClick = onUpdateClick,
    )
}

@Composable
fun BackgroundColorDialog(
    modifier: Modifier = Modifier,
    title: String,
    backgroundColor: BackgroundColor,
    customBackgroundColor: Int,
    onDismissRequest: () -> Unit,
    onUpdateClick: (
        backgroundColor: BackgroundColor,
        customColor: Int,
    ) -> Unit,
) {
    ColorDialog(
        modifier = modifier,
        title = title,
        color = backgroundColor,
        customColor = customBackgroundColor,
        entries = BackgroundColor.entries,
        customEntry = BackgroundColor.Custom,
        getTitle = { it.getBackgroundColorTitle() },
        onDismissRequest = onDismissRequest,
        onUpdateClick = onUpdateClick,
    )
}

@Composable
fun TextColor.getTextColorTitle() = when (this) {
    TextColor.System -> stringResource(commonR.string.system)
    TextColor.Light -> stringResource(commonR.string.light)
    TextColor.Dark -> stringResource(commonR.string.dark)
    TextColor.Custom -> stringResource(R.string.custom)
}

@Composable
fun BackgroundColor.getBackgroundColorTitle() = when (this) {
    BackgroundColor.System -> stringResource(commonR.string.system)
    BackgroundColor.Light -> stringResource(commonR.string.light)
    BackgroundColor.Dark -> stringResource(commonR.string.dark)
    BackgroundColor.Custom -> stringResource(R.string.custom)
}

@Composable
private fun <T : Enum<T>> ColorDialog(
    modifier: Modifier = Modifier,
    title: String,
    color: T,
    customColor: Int,
    entries: List<T>,
    customEntry: T,
    getTitle: @Composable (T) -> String,
    onDismissRequest: () -> Unit,
    onUpdateClick: (color: T, customColor: Int) -> Unit,
) {
    var selectedColor by remember { mutableStateOf(color) }
    var selectedCustomColor by remember { mutableIntStateOf(customColor) }
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
            entries.forEach { entry ->
                EblanRadioButton(
                    selected = selectedColor == entry,
                    text = getTitle(entry),
                    onClick = {
                        if (entry == customEntry) {
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
                Text(text = stringResource(commonR.string.cancel))
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
                Text(text = stringResource(commonR.string.update))
            }
        }
    }

    if (showColorPickerDialog) {
        ColorPickerDialog(
            title = title,
            customColor = customColor,
            onDismissRequest = {
                showColorPickerDialog = false
            },
            onSelectColor = {
                selectedColor = customEntry
                selectedCustomColor = it
            },
        )
    }
}
