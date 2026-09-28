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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.eblan.launcher.designsystem.component.EblanDialog
import com.eblan.launcher.designsystem.component.EblanRadioButton
import com.eblan.launcher.common.R as commonR

@Composable
fun <T> RadioOptionsDialog(
    title: String,
    modifier: Modifier = Modifier,
    options: List<T>,
    selected: T,
    label: @Composable (T) -> String,
    onDismissRequest: () -> Unit,
    onUpdateClick: (T) -> Unit,
) {
    var selectedOption by remember { mutableStateOf(selected) }

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
            options.forEach { option ->
                EblanRadioButton(
                    selected = selectedOption == option,
                    text = label(option),
                    onClick = { selectedOption = option },
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
                    onUpdateClick(selectedOption)

                    onDismissRequest()
                },
            ) {
                Text(text = stringResource(commonR.string.update))
            }
        }
    }
}
