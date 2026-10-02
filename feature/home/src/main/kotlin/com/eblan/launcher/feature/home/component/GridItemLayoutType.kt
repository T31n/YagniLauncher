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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

@Composable
internal fun TopIconBottomLabel(
    modifier: Modifier,
    horizontalAlignment: Alignment.Horizontal,
    verticalArrangement: Arrangement.Vertical,
    icon: @Composable () -> Unit,
    label: @Composable (Modifier) -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = verticalArrangement,
    ) {
        icon()
        label(Modifier)
    }
}

@Composable
internal fun TopLabelBottomIcon(
    modifier: Modifier,
    horizontalAlignment: Alignment.Horizontal,
    verticalArrangement: Arrangement.Vertical,
    icon: @Composable () -> Unit,
    label: @Composable (Modifier) -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = verticalArrangement,
    ) {
        label(Modifier)
        icon()
    }
}

@Composable
internal fun StartIconEndLabel(
    modifier: Modifier,
    horizontalArrangement: Arrangement.Horizontal,
    verticalAlignment: Alignment.Vertical,
    icon: @Composable () -> Unit,
    label: @Composable (Modifier) -> Unit,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = verticalAlignment,
    ) {
        icon()
        label(Modifier)
    }
}

@Composable
internal fun StartLabelEndIcon(
    modifier: Modifier,
    horizontalArrangement: Arrangement.Horizontal,
    verticalAlignment: Alignment.Vertical,
    icon: @Composable () -> Unit,
    label: @Composable (Modifier) -> Unit,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = verticalAlignment,
    ) {
        label(Modifier)
        icon()
    }
}

@Composable
internal fun IconOnly(
    modifier: Modifier = Modifier,
    horizontalAlignment: Alignment.Horizontal,
    verticalArrangement: Arrangement.Vertical,
    icon: @Composable () -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = verticalArrangement,
    ) {
        icon()
    }
}

@Composable
internal fun LabelOnly(
    modifier: Modifier = Modifier,
    iconSize: Dp,
    iconPadding: Dp,
    iconModifier: Modifier,
    label: @Composable (Modifier) -> Unit,
) {
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .size(iconSize)
                .padding(iconPadding),
        ) {
            label(
                Modifier
                    .matchParentSize()
                    .then(iconModifier),
            )
        }
    }
}
