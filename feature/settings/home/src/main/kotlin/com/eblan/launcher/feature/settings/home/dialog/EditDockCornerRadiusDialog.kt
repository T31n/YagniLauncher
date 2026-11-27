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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.eblan.launcher.designsystem.theme.EblanSpacing
import com.eblan.launcher.feature.settings.home.R
import com.eblan.launcher.ui.dialog.LabeledSlider
import com.eblan.launcher.common.R as commonR

private val CORNER_RADIUS_RANGE = 0..50

@Composable
internal fun EditDockCornerRadiusDialog(
    // Corner Radii
    dockTopStartCornerRadius: Int,
    dockTopEndCornerRadius: Int,
    dockBottomStartCornerRadius: Int,
    dockBottomEndCornerRadius: Int,
    // Modifiers
    modifier: Modifier = Modifier,
    // Callbacks
    onDismissRequest: () -> Unit,
    onUpdateCornerRadius: (
        dockTopStartCornerRadius: Int,
        dockTopEndCornerRadius: Int,
        dockBottomStartCornerRadius: Int,
        dockBottomEndCornerRadius: Int,
    ) -> Unit,
) {
    var currentDockTopStartCornerRadius by remember(dockTopStartCornerRadius) {
        mutableIntStateOf(
            dockTopStartCornerRadius.coerceIn(CORNER_RADIUS_RANGE.first, CORNER_RADIUS_RANGE.last),
        )
    }
    var currentDockTopEndCornerRadius by remember(dockTopEndCornerRadius) {
        mutableIntStateOf(
            dockTopEndCornerRadius.coerceIn(CORNER_RADIUS_RANGE.first, CORNER_RADIUS_RANGE.last),
        )
    }
    var currentDockBottomStartCornerRadius by remember(dockBottomStartCornerRadius) {
        mutableIntStateOf(
            dockBottomStartCornerRadius.coerceIn(
                CORNER_RADIUS_RANGE.first,
                CORNER_RADIUS_RANGE.last,
            ),
        )
    }
    var currentDockBottomEndCornerRadius by remember(dockBottomEndCornerRadius) {
        mutableIntStateOf(
            dockBottomEndCornerRadius.coerceIn(CORNER_RADIUS_RANGE.first, CORNER_RADIUS_RANGE.last),
        )
    }

    EblanDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
    ) {
        Text(
            text = stringResource(R.string.dock_corner_radius),
            style = MaterialTheme.typography.titleLarge,
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(weight = 1f, fill = false)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(EblanSpacing.small),
        ) {
            LabeledSlider(
                label = stringResource(R.string.top_start),
                value = currentDockTopStartCornerRadius,
                valueRange = CORNER_RADIUS_RANGE,
                onValueChange = { currentDockTopStartCornerRadius = it },
            )

            LabeledSlider(
                label = stringResource(R.string.top_end),
                value = currentDockTopEndCornerRadius,
                valueRange = CORNER_RADIUS_RANGE,
                onValueChange = { currentDockTopEndCornerRadius = it },
            )

            LabeledSlider(
                label = stringResource(R.string.bottom_start),
                value = currentDockBottomStartCornerRadius,
                valueRange = CORNER_RADIUS_RANGE,
                onValueChange = { currentDockBottomStartCornerRadius = it },
            )

            LabeledSlider(
                label = stringResource(R.string.bottom_end),
                value = currentDockBottomEndCornerRadius,
                valueRange = CORNER_RADIUS_RANGE,
                onValueChange = { currentDockBottomEndCornerRadius = it },
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
                    onUpdateCornerRadius(
                        currentDockTopStartCornerRadius,
                        currentDockTopEndCornerRadius,
                        currentDockBottomStartCornerRadius,
                        currentDockBottomEndCornerRadius,
                    )

                    onDismissRequest()
                },
            ) {
                Text(text = stringResource(commonR.string.update))
            }
        }
    }
}
