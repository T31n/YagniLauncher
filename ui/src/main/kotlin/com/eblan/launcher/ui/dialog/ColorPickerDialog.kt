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

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.eblan.launcher.designsystem.component.EblanDialog
import java.util.Locale
import kotlin.math.ceil
import com.eblan.launcher.common.R as commonR

private val colorPresets = listOf(
    Color(0xFFF44336),
    Color(0xFFE91E63),
    Color(0xFF9C27B0),
    Color(0xFF673AB7),
    Color(0xFF3F51B5),
    Color(0xFF2196F3),
    Color(0xFF03A9F4),
    Color(0xFF00BCD4),
    Color(0xFF009688),
    Color(0xFF4CAF50),
    Color(0xFF8BC34A),
    Color(0xFFCDDC39),
    Color(0xFFFFEB3B),
    Color(0xFFFFC107),
    Color(0xFFFF9800),
    Color(0xFFFF5722),
    Color(0xFF795548),
    Color(0xFF9E9E9E),
    Color(0xFF607D8B),
    Color.White,
    Color.Black,
)

@Composable
fun ColorPickerDialog(
    // Content
    title: String,
    customColor: Int,
    // Layout
    modifier: Modifier = Modifier,
    // Callbacks
    onDismissRequest: () -> Unit,
    onSelectColor: (Int) -> Unit,
) {
    val hsv = FloatArray(3).apply {
        android.graphics.Color.RGBToHSV(
            android.graphics.Color.red(customColor),
            android.graphics.Color.green(customColor),
            android.graphics.Color.blue(customColor),
            this,
        )
    }

    var hue by remember { mutableFloatStateOf(hsv[0]) }

    var saturation by remember { mutableFloatStateOf(hsv[1]) }

    var value by remember { mutableFloatStateOf(hsv[2]) }

    var alpha by remember { mutableFloatStateOf(Color(customColor).alpha) }

    val selectedColor = Color.hsv(hue, saturation, value)
        .copy(alpha = alpha)
        .toArgb()

    EblanDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
        )

        ColorPicker(
            hue = hue,
            saturation = saturation,
            value = value,
            alpha = alpha,
            modifier = Modifier.fillMaxWidth(),
            onSaturationSelected = {
                saturation = it
            },
            onValueSelected = {
                value = it
            },
            onHueSelected = {
                hue = it
            },
            onAlphaSelected = {
                alpha = it
            },
        )

        Spacer(Modifier.height(12.dp))

        ColorPresetRow(
            selectedColor = selectedColor,
            onSelectColor = { presetColor ->
                val presetHsv = FloatArray(3)
                android.graphics.Color.colorToHSV(presetColor, presetHsv)

                hue = presetHsv[0]
                saturation = presetHsv[1]
                value = presetHsv[2]
                alpha = 1f
            },
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

            Spacer(
                modifier = Modifier.width(5.dp),
            )

            TextButton(
                onClick = {
                    onSelectColor(
                        Color.hsv(hue, saturation, value)
                            .copy(alpha = alpha)
                            .toArgb(),
                    )

                    onDismissRequest()
                },
            ) {
                Text(text = stringResource(commonR.string.save))
            }
        }
    }
}

@Composable
private fun ColorPicker(
    // Color Values
    hue: Float,
    saturation: Float,
    value: Float,
    alpha: Float,
    // Modifier
    modifier: Modifier = Modifier,
    // Callbacks
    onSaturationSelected: (Float) -> Unit,
    onValueSelected: (Float) -> Unit,
    onHueSelected: (Float) -> Unit,
    onAlphaSelected: (Float) -> Unit,
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val saturationValueHeight = (maxHeight * 0.55f).coerceAtMost(260.dp)
        val barHeight = (maxHeight * 0.06f).coerceIn(12.dp, 24.dp)

        Column {
            SaturationValueCanvas(
                hue = hue,
                saturation = saturation,
                value = value,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(saturationValueHeight),
                onSaturationSelected = onSaturationSelected,
                onValueSelected = onValueSelected,
            )

            Spacer(Modifier.height(16.dp))

            HueCanvas(
                hue = hue,
                onHueSelected = onHueSelected,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(barHeight),
            )

            Spacer(Modifier.height(16.dp))

            AlphaCanvas(
                color = Color.hsv(hue, saturation, value),
                alpha = alpha,
                onAlphaSelected = onAlphaSelected,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(barHeight),
            )
        }
    }
}

@Composable
private fun SaturationValueCanvas(
    // Color Value
    hue: Float,
    saturation: Float,
    value: Float,
    // Modifier
    modifier: Modifier = Modifier,
    // Callbacks
    onSaturationSelected: (Float) -> Unit,
    onValueSelected: (Float) -> Unit,
) {
    Box(
        modifier = modifier.fillMaxWidth(),
    ) {
        Canvas(
            modifier = Modifier
                .pointerInput(key1 = Unit) {
                    detectTapGestures(
                        onTap = {
                            onSaturationSelected((it.x / size.width).coerceIn(0f, 1f))

                            onValueSelected(1f - (it.y / size.height).coerceIn(0f, 1f))
                        },
                    )
                }
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDrag = { change, _ ->
                            onSaturationSelected((change.position.x / size.width).coerceIn(0f, 1f))

                            onValueSelected(1f - (change.position.y / size.height).coerceIn(0f, 1f))
                        },
                    )
                }
                .matchParentSize()
                .clip(RoundedCornerShape(5.dp)),
        ) {
            drawRect(color = Color.hsv(hue, 1f, 1f))

            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color.White, Color.Transparent),
                ),
            )

            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.Black),
                ),
            )
        }

        Canvas(modifier = Modifier.matchParentSize()) {
            val indicatorX = saturation * size.width

            val indicatorY = (1f - value) * size.height

            drawCircle(
                color = Color.Black,
                radius = 7.dp.toPx(),
                center = Offset(indicatorX, indicatorY),
                style = Stroke(width = 2.dp.toPx()),
            )

            drawCircle(
                color = Color.White,
                radius = 5.dp.toPx(),
                center = Offset(indicatorX, indicatorY),
                style = Stroke(width = 2.dp.toPx()),
            )
        }
    }
}

@Composable
private fun HueCanvas(
    modifier: Modifier = Modifier,
    hue: Float,
    onHueSelected: (Float) -> Unit,
) {
    Canvas(
        modifier = modifier
            .pointerInput(key1 = Unit) {
                detectTapGestures(
                    onTap = {
                        onHueSelected((it.x / size.width).coerceIn(0f, 1f) * 360f)
                    },
                )
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onHorizontalDrag = { change, _ ->
                        onHueSelected((change.position.x / size.width).coerceIn(0f, 1f) * 360f)
                    },
                )
            }
            .fillMaxWidth()
            .clip(RoundedCornerShape(5.dp)),
    ) {
        val hueColors = listOf(
            Color.Red,
            Color.Yellow,
            Color.Green,
            Color.Cyan,
            Color.Blue,
            Color.Magenta,
            Color.Red,
        )

        drawRect(
            brush = Brush.horizontalGradient(hueColors),
            size = size,
        )

        drawSliderIndicator((hue / 360f) * size.width)
    }
}

@Composable
private fun AlphaCanvas(
    modifier: Modifier = Modifier,
    color: Color,
    alpha: Float,
    onAlphaSelected: (Float) -> Unit,
) {
    Canvas(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    onAlphaSelected((offset.x / size.width).coerceIn(0f, 1f))
                }
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures { change, _ ->
                    onAlphaSelected((change.position.x / size.width).coerceIn(0f, 1f))
                }
            }
            .fillMaxWidth()
            .clip(RoundedCornerShape(5.dp)),
    ) {
        val squareSize = size.height / 2

        val columns = ceil(size.width / squareSize).toInt()
        val rows = ceil(size.height / squareSize).toInt()

        for (column in 0 until columns) {
            for (row in 0 until rows) {
                val isBlack = (row + column) % 2 == 0

                drawRect(
                    color = if (isBlack) Color.Black.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.4f),
                    topLeft = Offset(
                        x = column * squareSize,
                        y = row * squareSize,
                    ),
                    size = Size(squareSize, squareSize),
                )
            }
        }

        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(color.copy(alpha = 0f), color),
            ),
        )

        drawSliderIndicator(alpha * size.width)
    }
}

@Composable
private fun ColorPresetRow(
    selectedColor: Int,
    onSelectColor: (Int) -> Unit,
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(colorPresets) { presetColor ->
            val presetColorArgb = presetColor.toArgb()
            val isSelected = selectedColor == presetColorArgb

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(presetColor)
                    .border(
                        width = if (isSelected) 3.dp else 1.dp,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outlineVariant
                        },
                        shape = CircleShape,
                    )
                    .selectable(
                        selected = isSelected,
                        role = Role.RadioButton,
                        onClick = {
                            onSelectColor(presetColorArgb)
                        },
                    )
                    .semantics {
                        contentDescription =
                            "#%06X".format(Locale.ROOT, 0xFFFFFF and presetColorArgb)
                    },
            )
        }
    }
}

private fun DrawScope.drawSliderIndicator(x: Float) {
    val indicatorSize = 5.dp.toPx()

    val indicatorX = x.coerceIn(0f, size.width - indicatorSize)

    drawRect(
        color = Color.White,
        topLeft = Offset(indicatorX, 0f),
        size = Size(indicatorSize, size.height),
    )

    drawRect(
        color = Color.Black.copy(alpha = 0.4f),
        topLeft = Offset(indicatorX, 0f),
        size = Size(indicatorSize, size.height),
        style = Stroke(width = Dp.Hairline.toPx()),
    )
}
