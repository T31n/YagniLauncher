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
package com.eblan.launcher.feature.settings.folder.dialog

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
import com.eblan.launcher.feature.settings.folder.R
import com.eblan.launcher.ui.dialog.LabeledSlider
import com.eblan.launcher.common.R as commonR

private val FOLDER_CELL_DIMENSION_RANGE = 40..160
private const val FOLDER_CELL_DIMENSION_STEP = 4

@Composable
internal fun EditFolderCellDimensionDialog(
    // Dimensions
    folderCellWidth: Int,
    folderCellHeight: Int,
    // Layout
    modifier: Modifier = Modifier,
    // Callbacks
    onDismissRequest: () -> Unit,
    onUpdateFolderCellDimension: (
        folderCellWidth: Int,
        folderCellHeight: Int,
    ) -> Unit,
) {
    var currentFolderCellWidth by remember(folderCellWidth) {
        mutableIntStateOf(
            folderCellWidth.coerceIn(
                FOLDER_CELL_DIMENSION_RANGE.first,
                FOLDER_CELL_DIMENSION_RANGE.last,
            ),
        )
    }
    var currentFolderCellHeight by remember(folderCellHeight) {
        mutableIntStateOf(
            folderCellHeight.coerceIn(
                FOLDER_CELL_DIMENSION_RANGE.first,
                FOLDER_CELL_DIMENSION_RANGE.last,
            ),
        )
    }

    EblanDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
    ) {
        Text(
            text = stringResource(R.string.folder_cell_dimension),
            style = MaterialTheme.typography.titleLarge,
        )

        LabeledSlider(
            label = stringResource(R.string.cell_width),
            value = currentFolderCellWidth,
            valueRange = FOLDER_CELL_DIMENSION_RANGE,
            step = FOLDER_CELL_DIMENSION_STEP,
            onValueChange = { currentFolderCellWidth = it },
        )

        LabeledSlider(
            label = stringResource(R.string.cell_height),
            value = currentFolderCellHeight,
            valueRange = FOLDER_CELL_DIMENSION_RANGE,
            step = FOLDER_CELL_DIMENSION_STEP,
            onValueChange = { currentFolderCellHeight = it },
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
                    onUpdateFolderCellDimension(currentFolderCellWidth, currentFolderCellHeight)

                    onDismissRequest()
                },
            ) {
                Text(text = stringResource(commonR.string.update))
            }
        }
    }
}
