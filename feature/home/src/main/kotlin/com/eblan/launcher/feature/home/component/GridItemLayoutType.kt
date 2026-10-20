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
package com.eblan.launcher.feature.home.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.eblan.launcher.domain.model.grid.GridItemSettings
import com.eblan.launcher.domain.model.grid.LayoutType

@Composable
internal fun GridItemLayoutType(
    // Settings
    gridItemSettings: GridItemSettings,
    // Alignment and arrangement
    horizontalAlignment: Alignment.Horizontal,
    verticalAlignment: Alignment.Vertical,
    horizontalArrangement: Arrangement.Horizontal,
    verticalArrangement: Arrangement.Vertical,
    // Modifier
    modifier: Modifier = Modifier,
    // Content
    iconContent: @Composable () -> Unit,
    labelContent: @Composable () -> Unit,
    labelOnlyContent: @Composable () -> Unit
) {
    when (gridItemSettings.layoutType) {
        LayoutType.TopIconBottomLabel -> {
            Column(
                modifier = modifier,
                horizontalAlignment = horizontalAlignment,
                verticalArrangement = verticalArrangement,
            ) {
                iconContent()
                labelContent()
            }
        }

        LayoutType.TopLabelBottomIcon -> {
            Column(
                modifier = modifier,
                horizontalAlignment = horizontalAlignment,
                verticalArrangement = verticalArrangement,
            ) {
                labelContent()
                iconContent()
            }
        }

        LayoutType.StartIconEndLabel -> {
            Row(
                modifier = modifier,
                horizontalArrangement = horizontalArrangement,
                verticalAlignment = verticalAlignment,
            ) {
                iconContent()
                labelContent()
            }
        }

        LayoutType.StartLabelEndIcon -> {
            Row(
                modifier = modifier,
                horizontalArrangement = horizontalArrangement,
                verticalAlignment = verticalAlignment,
            ) {
                labelContent()
                iconContent()
            }
        }

        LayoutType.IconOnly -> {
            Column(
                modifier = modifier,
                horizontalAlignment = horizontalAlignment,
                verticalArrangement = verticalArrangement,
            ) {
                iconContent()
            }
        }

        LayoutType.LabelOnly -> {
            Column(
                modifier = modifier,
                horizontalAlignment = horizontalAlignment,
                verticalArrangement = verticalArrangement,
            ) {
                labelOnlyContent()
            }
        }
    }
}
