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
import com.eblan.launcher.feature.settings.home.R
import com.eblan.launcher.ui.settings.LabeledSlider
import com.eblan.launcher.common.R as commonR

private val DOCK_COLUMNS_RANGE = 1..12
private val DOCK_ROWS_RANGE = 1..4

@Composable
internal fun EditDockGridDialog(
    // Grid
    dockColumns: Int,
    dockRows: Int,
    // Modifier
    modifier: Modifier = Modifier,
    // Callbacks
    onDismissRequest: () -> Unit,
    onUpdateDockGrid: (
        dockColumns: Int,
        dockRows: Int,
    ) -> Unit,
) {
    var currentDockColumns by remember(dockColumns) {
        mutableIntStateOf(dockColumns)
    }
    var currentDockRows by remember(dockRows) {
        mutableIntStateOf(dockRows)
    }

    EblanDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
    ) {
        Text(
            text = stringResource(R.string.dock_grid),
            style = MaterialTheme.typography.titleLarge,
        )

        LabeledSlider(
            label = stringResource(commonR.string.columns),
            value = currentDockColumns,
            valueRange = DOCK_COLUMNS_RANGE,
            onValueChange = { currentDockColumns = it },
        )

        LabeledSlider(
            label = stringResource(commonR.string.rows),
            value = currentDockRows,
            valueRange = DOCK_ROWS_RANGE,
            onValueChange = { currentDockRows = it },
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
                    onUpdateDockGrid(currentDockColumns, currentDockRows)

                    onDismissRequest()
                },
            ) {
                Text(text = stringResource(commonR.string.update))
            }
        }
    }
}
