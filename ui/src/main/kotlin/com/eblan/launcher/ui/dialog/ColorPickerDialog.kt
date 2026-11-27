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
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.eblan.launcher.designsystem.component.EblanDialog
import com.eblan.launcher.designsystem.theme.EblanSpacing
import java.util.Locale
import kotlin.math.ceil
import com.eblan.launcher.common.R as commonR

private const val HUE_SEGMENT_COUNT = 6

private val hueGradientColors = List(HUE_SEGMENT_COUNT + 1) { hueSegmentIndex ->
    Color.hsv(hueSegmentIndex * 360f / HUE_SEGMENT_COUNT, 1f, 1f)
}

private const val COLOR_PRESET_HUE_COUNT = 16
private const val COLOR_PRESET_NEUTRAL_COUNT = 5

private val colorPresets = buildList(COLOR_PRESET_HUE_COUNT + COLOR_PRESET_NEUTRAL_COUNT) {
    repeat(COLOR_PRESET_HUE_COUNT) { hueIndex ->
        add(Color.hsv(hueIndex * 360f / COLOR_PRESET_HUE_COUNT, 1f, 1f))
    }

    repeat(COLOR_PRESET_NEUTRAL_COUNT) { neutralIndex ->
        val neutralValue = neutralIndex / (COLOR_PRESET_NEUTRAL_COUNT - 1f)
        add(Color.hsv(0f, 0f, neutralValue))
    }
}

private val SATURATION_VALUE_HEIGHT = 200.dp
private val SLIDER_BAR_HEIGHT = 28.dp
private val PREVIEW_SWATCH_SIZE = 48.dp

@Composable
fun ColorPickerDialog(
    // Content
    title: String,
    customColor: Int,
    showAlphaSlider: Boolean,
    // Layout
    modifier: Modifier = Modifier,
    // Callbacks
    onDismissRequest: () -> Unit,
    onSelectColor: (Int) -> Unit,
) {
    val initialHsv = remember(customColor) {
        FloatArray(3).apply {
            android.graphics.Color.RGBToHSV(
                android.graphics.Color.red(customColor),
                android.graphics.Color.green(customColor),
                android.graphics.Color.blue(customColor),
                this,
            )
        }
    }

    val initialAlpha = remember(customColor, showAlphaSlider) {
        when {
            !showAlphaSlider -> 1f
            customColor == 0 -> 1f
            else -> Color(customColor).alpha
        }
    }

    var hue by remember(customColor) { mutableFloatStateOf(initialHsv[0]) }
    var saturation by remember(customColor) { mutableFloatStateOf(initialHsv[1]) }
    var value by remember(customColor) { mutableFloatStateOf(initialHsv[2]) }
    var alpha by remember(customColor, showAlphaSlider) { mutableFloatStateOf(initialAlpha) }

    val selectedAlpha = if (showAlphaSlider) alpha else 1f

    val selectedColor = Color.hsv(hue, saturation, value)
        .copy(alpha = selectedAlpha)
        .toArgb()

    var hexText by remember(customColor, showAlphaSlider) {
        mutableStateOf(formatHexColor(argb = selectedColor, includeAlpha = showAlphaSlider))
    }

    val isHexValid = isValidHexColor(hex = hexText, includeAlpha = showAlphaSlider)

    val focusManager = LocalFocusManager.current

    fun syncHexFromHsv(
        currentHue: Float = hue,
        currentSaturation: Float = saturation,
        currentValue: Float = value,
        currentAlpha: Float = alpha,
    ) {
        val updatedArgb = Color.hsv(currentHue, currentSaturation, currentValue)
            .copy(alpha = if (showAlphaSlider) currentAlpha else 1f)
            .toArgb()

        hexText = formatHexColor(argb = updatedArgb, includeAlpha = showAlphaSlider)
    }

    EblanDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(EblanSpacing.medium),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
            )

            ColorPreviewHexRow(
                selectedColor = selectedColor,
                hexText = hexText,
                isHexValid = isHexValid,
                showAlphaSlider = showAlphaSlider,
                onHexTextChange = { rawInput ->
                    val maxLength = if (showAlphaSlider) 8 else 6
                    val sanitized = rawInput
                        .removePrefix("#")
                        .filter { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }
                        .uppercase(Locale.ROOT)
                        .take(maxLength)

                    hexText = sanitized

                    parseHexColor(hex = sanitized, includeAlpha = showAlphaSlider)?.let { parsedArgb ->
                        val parsedHsv = FloatArray(3)
                        android.graphics.Color.colorToHSV(parsedArgb, parsedHsv)

                        hue = parsedHsv[0]
                        saturation = parsedHsv[1]
                        value = parsedHsv[2]
                        if (showAlphaSlider) {
                            alpha = Color(parsedArgb).alpha
                        }
                    }
                },
                onDone = {
                    focusManager.clearFocus()
                },
            )

            ColorPicker(
                hue = hue,
                saturation = saturation,
                value = value,
                alpha = selectedAlpha,
                showAlphaSlider = showAlphaSlider,
                modifier = Modifier.fillMaxWidth(),
                onSaturationValueSelected = { currentSaturation, currentValue ->
                    focusManager.clearFocus()
                    saturation = currentSaturation
                    value = currentValue
                    syncHexFromHsv(
                        currentSaturation = currentSaturation,
                        currentValue = currentValue,
                    )
                },
                onHueSelected = { currentHue ->
                    focusManager.clearFocus()
                    hue = currentHue
                    syncHexFromHsv(currentHue = currentHue)
                },
                onAlphaSelected = { currentAlpha ->
                    focusManager.clearFocus()
                    alpha = currentAlpha
                    syncHexFromHsv(currentAlpha = currentAlpha)
                },
            )

            ColorPresetRow(
                selectedColor = selectedColor,
                onSelectColor = { presetColor ->
                    focusManager.clearFocus()
                    val presetHsv = FloatArray(3)
                    android.graphics.Color.colorToHSV(presetColor, presetHsv)

                    hue = presetHsv[0]
                    saturation = presetHsv[1]
                    value = presetHsv[2]
                    alpha = 1f
                    syncHexFromHsv(
                        currentHue = presetHsv[0],
                        currentSaturation = presetHsv[1],
                        currentValue = presetHsv[2],
                        currentAlpha = 1f,
                    )
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
                        onSelectColor(selectedColor)

                        onDismissRequest()
                    },
                    enabled = isHexValid,
                ) {
                    Text(text = stringResource(commonR.string.save))
                }
            }
        }
    }
}

@Composable
private fun ColorPreviewHexRow(
    selectedColor: Int,
    hexText: String,
    isHexValid: Boolean,
    showAlphaSlider: Boolean,
    onHexTextChange: (String) -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(EblanSpacing.medium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Canvas(
            modifier = Modifier
                .size(PREVIEW_SWATCH_SIZE)
                .clip(MaterialTheme.shapes.small)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = MaterialTheme.shapes.small,
                ),
        ) {
            if (showAlphaSlider) {
                drawCheckerboard(size.height / 4f)
            }

            drawRect(color = Color(selectedColor))
        }

        OutlinedTextField(
            value = hexText,
            onValueChange = onHexTextChange,
            modifier = Modifier.weight(1f),
            prefix = {
                Text(text = "#")
            },
            placeholder = {
                Text(text = if (showAlphaSlider) "AARRGGBB" else "RRGGBB")
            },
            isError = !isHexValid,
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters,
                autoCorrectEnabled = false,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = { onDone() },
            ),
        )
    }
}

@Composable
private fun ColorPicker(
    // Color Values
    hue: Float,
    saturation: Float,
    value: Float,
    alpha: Float,
    showAlphaSlider: Boolean,
    // Modifier
    modifier: Modifier = Modifier,
    // Callbacks
    onSaturationValueSelected: (saturation: Float, value: Float) -> Unit,
    onHueSelected: (Float) -> Unit,
    onAlphaSelected: (Float) -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(EblanSpacing.medium),
    ) {
        SaturationValueCanvas(
            hue = hue,
            saturation = saturation,
            value = value,
            modifier = Modifier
                .fillMaxWidth()
                .height(SATURATION_VALUE_HEIGHT),
            onSaturationValueSelected = onSaturationValueSelected,
        )

        HueCanvas(
            hue = hue,
            onHueSelected = onHueSelected,
            modifier = Modifier
                .fillMaxWidth()
                .height(SLIDER_BAR_HEIGHT),
        )

        if (showAlphaSlider) {
            AlphaCanvas(
                color = Color.hsv(hue, saturation, value),
                alpha = alpha,
                onAlphaSelected = onAlphaSelected,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(SLIDER_BAR_HEIGHT),
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
    onSaturationValueSelected: (saturation: Float, value: Float) -> Unit,
) {
    val currentOnSaturationValueSelected by rememberUpdatedState(onSaturationValueSelected)

    Box(
        modifier = modifier.fillMaxWidth(),
    ) {
        Canvas(
            modifier = Modifier
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        down.consume()
                        currentOnSaturationValueSelected(
                            (down.position.x / size.width).coerceIn(0f, 1f),
                            1f - (down.position.y / size.height).coerceIn(0f, 1f),
                        )

                        drag(down.id) { change ->
                            change.consume()
                            currentOnSaturationValueSelected(
                                (change.position.x / size.width).coerceIn(0f, 1f),
                                1f - (change.position.y / size.height).coerceIn(0f, 1f),
                            )
                        }
                    }
                }
                .matchParentSize()
                .clip(MaterialTheme.shapes.small),
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
    val currentOnHueSelected by rememberUpdatedState(onHueSelected)

    Canvas(
        modifier = modifier
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    down.consume()
                    currentOnHueSelected((down.position.x / size.width).coerceIn(0f, 1f) * 360f)

                    drag(down.id) { change ->
                        change.consume()
                        currentOnHueSelected(
                            (change.position.x / size.width).coerceIn(0f, 1f) * 360f,
                        )
                    }
                }
            }
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small),
    ) {
        drawRect(
            brush = Brush.horizontalGradient(hueGradientColors),
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
    val currentOnAlphaSelected by rememberUpdatedState(onAlphaSelected)

    Canvas(
        modifier = modifier
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    down.consume()
                    currentOnAlphaSelected((down.position.x / size.width).coerceIn(0f, 1f))

                    drag(down.id) { change ->
                        change.consume()
                        currentOnAlphaSelected((change.position.x / size.width).coerceIn(0f, 1f))
                    }
                }
            }
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small),
    ) {
        drawCheckerboard(size.height / 2f)

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

private fun DrawScope.drawCheckerboard(squareSize: Float) {
    if (squareSize <= 0f) return

    val columns = ceil(size.width / squareSize).toInt()
    val rows = ceil(size.height / squareSize).toInt()

    for (column in 0 until columns) {
        for (row in 0 until rows) {
            val isDark = (row + column) % 2 == 0

            drawRect(
                color = if (isDark) {
                    Color.Black.copy(alpha = 0.2f)
                } else {
                    Color.White.copy(alpha = 0.8f)
                },
                topLeft = Offset(
                    x = column * squareSize,
                    y = row * squareSize,
                ),
                size = Size(squareSize, squareSize),
            )
        }
    }
}

private fun DrawScope.drawSliderIndicator(x: Float) {
    val indicatorWidth = 8.dp.toPx()
    val halfWidth = indicatorWidth / 2f
    val indicatorX = (x - halfWidth).coerceIn(0f, size.width - indicatorWidth)
    val cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())

    drawRoundRect(
        color = Color.White,
        topLeft = Offset(indicatorX, 0f),
        size = Size(indicatorWidth, size.height),
        cornerRadius = cornerRadius,
    )

    drawRoundRect(
        color = Color.Black.copy(alpha = 0.5f),
        topLeft = Offset(indicatorX, 0f),
        size = Size(indicatorWidth, size.height),
        cornerRadius = cornerRadius,
        style = Stroke(width = 1.5.dp.toPx()),
    )
}

private fun formatHexColor(
    argb: Int,
    includeAlpha: Boolean,
): String = if (includeAlpha) {
    "%08X".format(Locale.ROOT, argb)
} else {
    "%06X".format(Locale.ROOT, 0xFFFFFF and argb)
}

private fun isValidHexColor(
    hex: String,
    includeAlpha: Boolean,
): Boolean = hex.length == 6 || (includeAlpha && hex.length == 8)

private fun parseHexColor(
    hex: String,
    includeAlpha: Boolean,
): Int? {
    val parsed = hex.toLongOrNull(16) ?: return null

    return when {
        hex.length == 6 -> (0xFF000000L or parsed).toInt()
        includeAlpha && hex.length == 8 -> parsed.toInt()
        else -> null
    }
}
