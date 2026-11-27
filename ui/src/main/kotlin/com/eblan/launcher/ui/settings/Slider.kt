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
package com.eblan.launcher.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import com.eblan.launcher.designsystem.theme.EblanSpacing
import kotlin.math.roundToInt

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
    labelStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    // Callbacks
    onValueChange: (Int) -> Unit,
    onValueChangeFinished: (() -> Unit)? = null,
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
                style = labelStyle,
            )

            Text(
                text = valueLabel(value),
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
            steps = calculateSliderSteps(
                valueRange = valueRange,
                step = step,
            ),
            onValueChangeFinished = onValueChangeFinished,
        )
    }
}

private fun calculateSliderSteps(
    valueRange: IntRange,
    step: Int,
): Int {
    val safeStep = step.coerceAtLeast(1)
    val span = (valueRange.last - valueRange.first).coerceAtLeast(0)

    return ((span / safeStep) - 1).coerceAtLeast(0)
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
