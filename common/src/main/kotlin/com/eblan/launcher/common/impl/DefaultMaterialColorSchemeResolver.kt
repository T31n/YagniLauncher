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
package com.eblan.launcher.common.impl

import android.app.WallpaperColors
import android.app.WallpaperManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.annotation.ColorInt
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.eblan.launcher.common.MaterialColorSchemeResolver
import com.eblan.launcher.domain.model.userdata.ColorSource
import com.eblan.launcher.domain.model.userdata.Theme
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamicColorScheme
import com.materialkolor.dynamiccolor.ColorSpec
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal class DefaultMaterialColorSchemeResolver @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : MaterialColorSchemeResolver {
    override fun isDarkTheme(theme: Theme): Boolean {
        fun isSystemDarkTheme(): Boolean = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK ==
            Configuration.UI_MODE_NIGHT_YES

        fun getWallpaperDarkTheme(): Boolean? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            WallpaperManager.getInstance(context)
                .getWallpaperColors(WallpaperManager.FLAG_SYSTEM)
                ?.colorHints
                ?.let { colorHints ->
                    colorHints and WallpaperColors.HINT_SUPPORTS_DARK_THEME != 0
                }
        } else {
            null
        }

        return when (theme) {
            Theme.System -> isSystemDarkTheme()
            Theme.Light -> false
            Theme.Dark -> true
            Theme.Wallpaper -> getWallpaperDarkTheme() ?: isSystemDarkTheme()
        }
    }

    override fun getWallpaperColorScheme(isDarkTheme: Boolean): ColorScheme? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (isDarkTheme) {
            dynamicDarkColorScheme(context)
        } else {
            dynamicLightColorScheme(context)
        }
    } else {
        null
    }

    override fun getCustomColorScheme(
        seedColor: Int,
        isDarkTheme: Boolean,
    ): ColorScheme {
        val opaqueSeedColor = (seedColor and 0x00FFFFFF) or 0xFF000000.toInt()

        return dynamicColorScheme(
            seedColor = Color(opaqueSeedColor),
            isDark = isDarkTheme,
            style = PaletteStyle.TonalSpot,
            contrastLevel = 0.0,
            specVersion = ColorSpec.SpecVersion.SPEC_2021,
        )
    }

    @ColorInt
    override fun getPrimaryColor(
        colorSource: ColorSource,
        customSeedColor: Int,
        theme: Theme,
    ): Int {
        val isDarkTheme = isDarkTheme(theme)

        val colorScheme = getColorScheme(
            colorSource = colorSource,
            customSeedColor = customSeedColor,
            isDarkTheme = isDarkTheme,
        )

        return colorScheme?.primary?.toArgb() ?: if (isDarkTheme) {
            DARK_THEME_PRIMARY_COLOR
        } else {
            LIGHT_THEME_PRIMARY_COLOR
        }
    }

    @ColorInt
    override fun getSurfaceColor(
        colorSource: ColorSource,
        customSeedColor: Int,
        theme: Theme,
    ): Int {
        val isDarkTheme = isDarkTheme(theme)

        val colorScheme = getColorScheme(
            colorSource = colorSource,
            customSeedColor = customSeedColor,
            isDarkTheme = isDarkTheme,
        )

        return colorScheme?.surfaceContainerHighest?.toArgb() ?: if (isDarkTheme) {
            DARK_THEME_SURFACE_CONTAINER_HIGHEST_COLOR
        } else {
            LIGHT_THEME_SURFACE_CONTAINER_HIGHEST_COLOR
        }
    }

    private fun getColorScheme(
        colorSource: ColorSource,
        customSeedColor: Int,
        isDarkTheme: Boolean,
    ): ColorScheme? = when (colorSource) {
        ColorSource.None -> null

        ColorSource.Wallpaper -> getWallpaperColorScheme(isDarkTheme)

        ColorSource.Custom -> getCustomColorScheme(
            seedColor = customSeedColor,
            isDarkTheme = isDarkTheme,
        )
    }

    private companion object {
        const val LIGHT_THEME_PRIMARY_COLOR = 0xFF4C662B.toInt()
        const val DARK_THEME_PRIMARY_COLOR = 0xFFB1D18A.toInt()
        const val LIGHT_THEME_SURFACE_CONTAINER_HIGHEST_COLOR = 0xFFE2E3D8.toInt()
        const val DARK_THEME_SURFACE_CONTAINER_HIGHEST_COLOR = 0xFF33362E.toInt()
    }
}
