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
import com.eblan.launcher.domain.model.userdata.BackgroundColor
import com.eblan.launcher.domain.model.userdata.TextColor
import com.materialkolor.ktx.contrastRatio

internal fun getGridItemTextColor(
    // Grid Item Text Colors
    gridItemTextColor: TextColor,
    gridItemCustomTextColor: Int,
    // System Text Colors
    systemTextColor: TextColor,
    systemCustomTextColor: Int,
    // Default Color
    defaultColor: Color = Color.Unspecified,
): Color = when (gridItemTextColor) {
    TextColor.System -> getTextColor(
        // Text Colors
        textColor = systemTextColor,
        customTextColor = systemCustomTextColor,
        defaultColor = defaultColor,
    )

    TextColor.Light -> Color.White

    TextColor.Dark -> Color.Black

    TextColor.Custom -> Color(gridItemCustomTextColor)
}

internal fun getTextColor(
    // Text Colors
    textColor: TextColor,
    customTextColor: Int,
    defaultColor: Color = Color.Unspecified,
): Color = when (textColor) {
    TextColor.System -> defaultColor
    TextColor.Light -> Color.White
    TextColor.Dark -> Color.Black
    TextColor.Custom -> Color(customTextColor)
}

internal fun getTextColorFromBackgroundColor(
    // Background Color
    backgroundColor: BackgroundColor,
    customBackgroundColor: Int,
    // Text Color
    textColor: TextColor,
    customTextColor: Int,
    systemCustomTextColor: Int,
    systemTextColor: TextColor,
    // Default Color
    defaultColor: Color = Color.Unspecified,
): Color = when (backgroundColor) {
    BackgroundColor.System -> defaultColor

    BackgroundColor.Custom -> {
        val gridItemTextColor = getTextColor(
            // Text Colors
            textColor = systemTextColor,
            customTextColor = systemCustomTextColor,
            defaultColor = defaultColor,
        )

        val textColorByContrast = getTextColorByContrast(
            // Colors
            customBackgroundColor = Color(customBackgroundColor),
            systemTextColor = gridItemTextColor,
        )

        getTextColor(
            // Text Colors
            textColor = textColor,
            customTextColor = customTextColor,
            defaultColor = textColorByContrast,
        )
    }
}

internal fun getApplicationScreenTextColor(
    // Background Colors
    backgroundColor: BackgroundColor,
    customBackgroundColor: Int,
    // Text Colors
    systemTextColor: TextColor,
    systemCustomTextColor: Int,
    // Defaults
    defaultColor: Color = Color.Unspecified,
): Color = when (backgroundColor) {
    BackgroundColor.System -> defaultColor

    BackgroundColor.Custom -> {
        val gridItemTextColor = getTextColor(
            // Text Colors
            textColor = systemTextColor,
            customTextColor = systemCustomTextColor,
            defaultColor = defaultColor,
        )

        getTextColorByContrast(
            // Colors
            customBackgroundColor = Color(customBackgroundColor),
            systemTextColor = gridItemTextColor,
        )
    }
}

private fun getTextColorByContrast(
    // Colors
    customBackgroundColor: Color,
    systemTextColor: Color,
    // Thresholds
    alphaThreshold: Float = 0.5f,
): Color {
    if (customBackgroundColor.alpha < alphaThreshold) return systemTextColor

    return if (Color.Black.contrastRatio(customBackgroundColor) >=
        Color.White.contrastRatio(customBackgroundColor)
    ) {
        Color.Black
    } else {
        Color.White
    }
}
