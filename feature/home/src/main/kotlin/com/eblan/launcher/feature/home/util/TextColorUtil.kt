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
package com.eblan.launcher.feature.home.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.eblan.launcher.domain.model.userdata.BackgroundColor
import com.eblan.launcher.domain.model.userdata.TextColor

internal fun getGridItemTextColor(
    // Grid Item Colors
    gridItemCustomTextColor: Int,
    gridItemTextColor: TextColor,
    // System Colors
    systemCustomTextColor: Int,
    systemTextColor: TextColor,
    // Defaults
    defaultColor: Color = Color.Unspecified
): Color = when (gridItemTextColor) {
    TextColor.System -> getTextColor(
        textColor = systemTextColor,
        customTextColor = systemCustomTextColor,
        defaultColor = defaultColor
    )

    TextColor.Light -> Color.White

    TextColor.Dark -> Color.Black

    TextColor.Custom -> Color(gridItemCustomTextColor)
}

internal fun getTextColor(
    // Colors
    textColor: TextColor,
    customTextColor: Int,
    defaultColor: Color = Color.Unspecified
): Color = when (textColor) {
    TextColor.System -> defaultColor
    TextColor.Light -> Color.White
    TextColor.Dark -> Color.Black
    TextColor.Custom -> Color(customTextColor)
}

internal fun getTextColorFromBackgroundColor(
    // Background
    backgroundColor: BackgroundColor,
    customBackgroundColor: Int,
    // Text Color
    textColor: TextColor,
    customTextColor: Int,
    // System Color
    systemCustomTextColor: Int,
    systemTextColor: TextColor,
    // Default
    defaultColor: Color = Color.Unspecified
): Color = when (backgroundColor) {
    BackgroundColor.System -> defaultColor

    BackgroundColor.Light -> Color.Black

    BackgroundColor.Dark -> Color.White

    BackgroundColor.Custom -> {
        val gridItemTextColor = getTextColor(
            textColor = systemTextColor,
            customTextColor = systemCustomTextColor,
            defaultColor = defaultColor
        )

        val textColorByLuminance = getTextColorByLuminance(
            customBackgroundColor = Color(customBackgroundColor),
            systemTextColor = gridItemTextColor
        )

        getTextColor(
            textColor = textColor,
            customTextColor = customTextColor,
            defaultColor = textColorByLuminance
        )
    }
}

internal fun getApplicationScreenTextColor(
    // Background
    backgroundColor: BackgroundColor,
    customBackgroundColor: Int,
    // System Text
    systemTextColor: TextColor,
    systemCustomTextColor: Int,
    // Default
    defaultColor: Color = Color.Unspecified
): Color = when (backgroundColor) {
    BackgroundColor.System -> defaultColor

    BackgroundColor.Light -> Color.Black

    BackgroundColor.Dark -> Color.White

    BackgroundColor.Custom -> {
        val gridItemTextColor = getTextColor(
            textColor = systemTextColor,
            customTextColor = systemCustomTextColor,
            defaultColor = defaultColor
        )

        getTextColorByLuminance(
            customBackgroundColor = Color(customBackgroundColor),
            systemTextColor = gridItemTextColor
        )
    }
}

private fun getTextColorByLuminance(
    // Colors
    customBackgroundColor: Color,
    systemTextColor: Color,
    // Thresholds
    alphaThreshold: Float = 0.5f,
    luminanceCrossover: Float = 0.5f
): Color {
    if (customBackgroundColor.alpha < alphaThreshold) return systemTextColor

    return if (customBackgroundColor.luminance() >= luminanceCrossover) {
        Color.Black
    } else {
        Color.White
    }
}
