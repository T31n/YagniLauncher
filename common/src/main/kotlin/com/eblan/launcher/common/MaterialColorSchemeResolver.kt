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
package com.eblan.launcher.common

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.eblan.launcher.domain.model.userdata.ColorSource
import com.eblan.launcher.domain.model.userdata.Theme
import com.google.android.material.color.utilities.DynamicColor
import com.google.android.material.color.utilities.Hct
import com.google.android.material.color.utilities.MaterialDynamicColors
import com.google.android.material.color.utilities.SchemeTonalSpot

object MaterialColorSchemeResolver {
    fun getWallpaperColorScheme(
        context: Context,
        isDarkTheme: Boolean,
    ): ColorScheme? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (isDarkTheme) {
            getDynamicDarkColorScheme(context)
        } else {
            getDynamicLightColorScheme(context)
        }
    } else {
        null
    }

    @SuppressLint("RestrictedApi")
    fun getCustomColorScheme(
        seedColor: Int,
        isDarkTheme: Boolean,
    ): ColorScheme {
        val opaqueSeedColor = (seedColor and 0x00FFFFFF) or 0xFF000000.toInt()
        val sourceColor = Hct.fromInt(opaqueSeedColor)
        val dynamicScheme = SchemeTonalSpot(sourceColor, isDarkTheme, 0.0)
        val materialDynamicColors = MaterialDynamicColors()
        val getColor = { dynamicColor: DynamicColor ->
            Color(dynamicColor.getArgb(dynamicScheme))
        }

        return ColorScheme(
            primary = getColor(materialDynamicColors.primary()),
            onPrimary = getColor(materialDynamicColors.onPrimary()),
            primaryContainer = getColor(materialDynamicColors.primaryContainer()),
            onPrimaryContainer = getColor(materialDynamicColors.onPrimaryContainer()),
            inversePrimary = getColor(materialDynamicColors.inversePrimary()),
            secondary = getColor(materialDynamicColors.secondary()),
            onSecondary = getColor(materialDynamicColors.onSecondary()),
            secondaryContainer = getColor(materialDynamicColors.secondaryContainer()),
            onSecondaryContainer = getColor(materialDynamicColors.onSecondaryContainer()),
            tertiary = getColor(materialDynamicColors.tertiary()),
            onTertiary = getColor(materialDynamicColors.onTertiary()),
            tertiaryContainer = getColor(materialDynamicColors.tertiaryContainer()),
            onTertiaryContainer = getColor(materialDynamicColors.onTertiaryContainer()),
            background = getColor(materialDynamicColors.background()),
            onBackground = getColor(materialDynamicColors.onBackground()),
            surface = getColor(materialDynamicColors.surface()),
            onSurface = getColor(materialDynamicColors.onSurface()),
            surfaceVariant = getColor(materialDynamicColors.surfaceVariant()),
            onSurfaceVariant = getColor(materialDynamicColors.onSurfaceVariant()),
            surfaceTint = getColor(materialDynamicColors.surfaceTint()),
            inverseSurface = getColor(materialDynamicColors.inverseSurface()),
            inverseOnSurface = getColor(materialDynamicColors.inverseOnSurface()),
            error = getColor(materialDynamicColors.error()),
            onError = getColor(materialDynamicColors.onError()),
            errorContainer = getColor(materialDynamicColors.errorContainer()),
            onErrorContainer = getColor(materialDynamicColors.onErrorContainer()),
            outline = getColor(materialDynamicColors.outline()),
            outlineVariant = getColor(materialDynamicColors.outlineVariant()),
            scrim = getColor(materialDynamicColors.scrim()),
            surfaceBright = getColor(materialDynamicColors.surfaceBright()),
            surfaceDim = getColor(materialDynamicColors.surfaceDim()),
            surfaceContainer = getColor(materialDynamicColors.surfaceContainer()),
            surfaceContainerHigh = getColor(materialDynamicColors.surfaceContainerHigh()),
            surfaceContainerHighest = getColor(materialDynamicColors.surfaceContainerHighest()),
            surfaceContainerLow = getColor(materialDynamicColors.surfaceContainerLow()),
            surfaceContainerLowest = getColor(materialDynamicColors.surfaceContainerLowest()),
            primaryFixed = getColor(materialDynamicColors.primaryFixed()),
            primaryFixedDim = getColor(materialDynamicColors.primaryFixedDim()),
            onPrimaryFixed = getColor(materialDynamicColors.onPrimaryFixed()),
            onPrimaryFixedVariant = getColor(materialDynamicColors.onPrimaryFixedVariant()),
            secondaryFixed = getColor(materialDynamicColors.secondaryFixed()),
            secondaryFixedDim = getColor(materialDynamicColors.secondaryFixedDim()),
            onSecondaryFixed = getColor(materialDynamicColors.onSecondaryFixed()),
            onSecondaryFixedVariant = getColor(materialDynamicColors.onSecondaryFixedVariant()),
            tertiaryFixed = getColor(materialDynamicColors.tertiaryFixed()),
            tertiaryFixedDim = getColor(materialDynamicColors.tertiaryFixedDim()),
            onTertiaryFixed = getColor(materialDynamicColors.onTertiaryFixed()),
            onTertiaryFixedVariant = getColor(materialDynamicColors.onTertiaryFixedVariant()),
        )
    }

    @ColorInt
    fun getPrimaryColor(
        context: Context,
        colorSource: ColorSource,
        customSeedColor: Int,
        theme: Theme,
    ): Int {
        val isDarkTheme = when (theme) {
            Theme.System ->
                context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK ==
                    Configuration.UI_MODE_NIGHT_YES

            Theme.Light -> false

            Theme.Dark -> true
        }

        return when (colorSource) {
            ColorSource.None -> if (isDarkTheme) {
                DARK_THEME_PRIMARY_COLOR
            } else {
                LIGHT_THEME_PRIMARY_COLOR
            }

            ColorSource.Wallpaper -> getWallpaperColorScheme(
                context = context,
                isDarkTheme = isDarkTheme,
            )?.primary?.toArgb() ?: if (isDarkTheme) {
                DARK_THEME_PRIMARY_COLOR
            } else {
                LIGHT_THEME_PRIMARY_COLOR
            }

            ColorSource.Custom -> getCustomColorScheme(
                seedColor = customSeedColor,
                isDarkTheme = isDarkTheme,
            ).primary.toArgb()
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun getDynamicLightColorScheme(context: Context): ColorScheme = dynamicLightColorScheme(context)

    @RequiresApi(Build.VERSION_CODES.S)
    private fun getDynamicDarkColorScheme(context: Context): ColorScheme = dynamicDarkColorScheme(context)

    private const val LIGHT_THEME_PRIMARY_COLOR = 0xFF4C662B.toInt()
    private const val DARK_THEME_PRIMARY_COLOR = 0xFFB1D18A.toInt()
}
