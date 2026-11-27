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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.eblan.launcher.designsystem.component.EblanDialog
import com.eblan.launcher.designsystem.theme.EblanSpacing
import kotlin.math.roundToInt
import com.eblan.launcher.common.R as commonR

@Composable
fun SliderDialog(
    // Content
    title: String,
    value: Int,
    valueRange: IntRange,
    // Layout
    modifier: Modifier = Modifier,
    step: Int = 1,
    valueLabel: (Int) -> String = Int::toString,
    // Callbacks
    onDismissRequest: () -> Unit,
    onUpdateValue: (Int) -> Unit,
) {
    var currentValue by remember(value, valueRange) {
        mutableIntStateOf(value.coerceIn(valueRange.first, valueRange.last))
    }

    EblanDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
            )

            Text(
                text = valueLabel(currentValue),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium,
            )
        }

        Slider(
            value = currentValue.toFloat(),
            onValueChange = { rawValue ->
                currentValue = snapToStep(
                    value = rawValue,
                    valueRange = valueRange,
                    step = step,
                )
            },
            modifier = Modifier.fillMaxWidth(),
            valueRange = valueRange.first.toFloat()..valueRange.last.toFloat(),
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
                    onUpdateValue(currentValue)

                    onDismissRequest()
                },
            ) {
                Text(text = stringResource(commonR.string.update))
            }
        }
    }
}

@Composable
fun LabeledSlider(
    // Content
    label: String,
    value: Int,
    valueRange: IntRange,
    // Layout
    modifier: Modifier = Modifier,
    step: Int = 1,
    valueLabel: (Int) -> String = Int::toString,
    // Callbacks
    onValueChange: (Int) -> Unit,
) {
    val clampedValue = value.coerceIn(valueRange.first, valueRange.last)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(EblanSpacing.extraSmall),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
            )

            Text(
                text = valueLabel(clampedValue),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleSmall,
            )
        }

        Slider(
            value = clampedValue.toFloat(),
            onValueChange = { rawValue ->
                onValueChange(
                    snapToStep(
                        value = rawValue,
                        valueRange = valueRange,
                        step = step,
                    ),
                )
            },
            modifier = Modifier.fillMaxWidth(),
            valueRange = valueRange.first.toFloat()..valueRange.last.toFloat(),
        )
    }
}

private fun snapToStep(
    value: Float,
    valueRange: IntRange,
    step: Int,
): Int {
    if (step <= 1) {
        return value.roundToInt().coerceIn(valueRange.first, valueRange.last)
    }

    val offset = ((value - valueRange.first) / step).roundToInt() * step

    return (valueRange.first + offset).coerceIn(valueRange.first, valueRange.last)
}
