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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.eblan.launcher.designsystem.component.EblanDialog
import com.eblan.launcher.feature.settings.appdrawer.R
import com.eblan.launcher.ui.dialog.LabeledSlider
import com.eblan.launcher.common.R as commonR

private val VERTICAL_COLUMNS_RANGE = 1..12
private val VERTICAL_ROWS_HEIGHT_RANGE = 48..200
private const val VERTICAL_ROWS_HEIGHT_STEP = 4

@Composable
internal fun EditVerticalGridDialog(
    // Grid Properties
    appDrawerColumns: Int,
    appDrawerRowsHeight: Int,
    // Modifier
    modifier: Modifier = Modifier,
    // Callbacks
    onDismissRequest: () -> Unit,
    onUpdateVerticalGrid: (
        appDrawerColumns: Int,
        appDrawerRowsHeight: Int,
    ) -> Unit,
) {
    var currentAppDrawerColumns by remember(appDrawerColumns) {
        mutableIntStateOf(
            appDrawerColumns.coerceIn(VERTICAL_COLUMNS_RANGE.first, VERTICAL_COLUMNS_RANGE.last),
        )
    }
    var currentAppDrawerRowsHeight by remember(appDrawerRowsHeight) {
        mutableIntStateOf(
            appDrawerRowsHeight.coerceIn(
                VERTICAL_ROWS_HEIGHT_RANGE.first,
                VERTICAL_ROWS_HEIGHT_RANGE.last,
            ),
        )
    }

    EblanDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
    ) {
        Text(
            text = stringResource(R.string.vertical_grid),
            style = MaterialTheme.typography.titleLarge,
        )

        LabeledSlider(
            label = stringResource(commonR.string.columns),
            value = currentAppDrawerColumns,
            valueRange = VERTICAL_COLUMNS_RANGE,
            onValueChange = { currentAppDrawerColumns = it },
        )

        LabeledSlider(
            label = stringResource(R.string.rows_height),
            value = currentAppDrawerRowsHeight,
            valueRange = VERTICAL_ROWS_HEIGHT_RANGE,
            step = VERTICAL_ROWS_HEIGHT_STEP,
            onValueChange = { currentAppDrawerRowsHeight = it },
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
                    onUpdateVerticalGrid(
                        currentAppDrawerColumns,
                        currentAppDrawerRowsHeight,
                    )

                    onDismissRequest()
                },
            ) {
                Text(text = stringResource(commonR.string.update))
            }
        }
    }
}
