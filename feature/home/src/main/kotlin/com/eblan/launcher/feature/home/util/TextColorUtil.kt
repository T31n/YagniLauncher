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
    gridItemCustomTextColor: Int,
    gridItemTextColor: TextColor,
    systemCustomTextColor: Int,
    systemTextColor: TextColor,
    defaultColor: Color = Color.Unspecified,
): Color = when (gridItemTextColor) {
    TextColor.System -> getTextColor(
        customTextColor = systemCustomTextColor,
        textColor = systemTextColor,
        defaultColor = defaultColor,
    )

    TextColor.Light -> Color.White

    TextColor.Dark -> Color.Black

    TextColor.Custom -> Color(gridItemCustomTextColor)
}

internal fun getTextColor(
    customTextColor: Int,
    textColor: TextColor,
    defaultColor: Color = Color.Unspecified,
): Color = when (textColor) {
    TextColor.System -> defaultColor
    TextColor.Light -> Color.White
    TextColor.Dark -> Color.Black
    TextColor.Custom -> Color(customTextColor)
}

internal fun getTextColorFromBackgroundColor(
    backgroundColor: BackgroundColor,
    customBackgroundColor: Int,
    textColor: TextColor,
    customTextColor: Int,
    systemCustomTextColor: Int,
    systemTextColor: TextColor,
    defaultColor: Color = Color.Unspecified,
): Color = when (backgroundColor) {
    BackgroundColor.System -> defaultColor

    BackgroundColor.Light -> Color.Black

    BackgroundColor.Dark -> Color.White

    BackgroundColor.Custom -> {
        val gridItemTextColor = getTextColor(
            customTextColor = systemCustomTextColor,
            textColor = systemTextColor,
            defaultColor = defaultColor,
        )

        val textColorByLuminance = getTextColorByLuminance(
            customBackgroundColor = Color(customBackgroundColor),
            systemTextColor = gridItemTextColor,
        )

        getTextColor(
            customTextColor = customTextColor,
            textColor = textColor,
            defaultColor = textColorByLuminance,
        )
    }
}

internal fun getApplicationScreenTextColor(
    backgroundColor: BackgroundColor,
    customBackgroundColor: Int,
    systemCustomTextColor: Int,
    systemTextColor: TextColor,
    defaultColor: Color = Color.Unspecified,
): Color = when (backgroundColor) {
    BackgroundColor.System -> defaultColor

    BackgroundColor.Light -> Color.Black

    BackgroundColor.Dark -> Color.White

    BackgroundColor.Custom -> {
        val gridItemTextColor = getTextColor(
            customTextColor = systemCustomTextColor,
            textColor = systemTextColor,
            defaultColor = defaultColor,
        )

        getTextColorByLuminance(
            customBackgroundColor = Color(customBackgroundColor),
            systemTextColor = gridItemTextColor,
        )
    }
}

private fun getTextColorByLuminance(
    customBackgroundColor: Color,
    systemTextColor: Color,
    alphaThreshold: Float = 0.5f,
    luminanceCrossover: Float = 0.5f,
): Color {
    if (customBackgroundColor.alpha < alphaThreshold) return systemTextColor

    return if (customBackgroundColor.luminance() >= luminanceCrossover) {
        Color.Black
    } else {
        Color.White
    }
}
