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
package com.eblan.launcher.util

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import com.eblan.launcher.designsystem.theme.getDefaultColorScheme
import com.eblan.launcher.domain.model.userdata.ColorSchemeSettings
import com.eblan.launcher.domain.model.userdata.ColorSource
import com.eblan.launcher.domain.model.userdata.Theme
import com.eblan.launcher.model.ApplicationTheme
import com.eblan.launcher.ui.local.LocalMaterialColorSchemeResolver
import com.eblan.launcher.ui.local.LocalWallpaperManager

@Composable
fun produceApplicationThemeState(
    theme: Theme?,
    systemBarsTheme: Theme?,
): State<ApplicationTheme?> {
    val wallpaperManager = LocalWallpaperManager.current

    return produceState(
        null,
        theme,
        systemBarsTheme,
        wallpaperManager,
    ) {
        if (theme == null || systemBarsTheme == null) {
            value = null
            return@produceState
        }

        wallpaperManager.getColorsChanged().collect { colorHints ->
            value = ApplicationTheme(
                theme = resolveTheme(
                    theme = theme,
                    colorHints = colorHints,
                    hintSupportsDarkTheme = wallpaperManager.hintSupportsDarkTheme,
                ),
                systemBarsTheme = resolveTheme(
                    theme = systemBarsTheme,
                    colorHints = colorHints,
                    hintSupportsDarkTheme = wallpaperManager.hintSupportsDarkTheme,
                ),
            )
        }
    }
}

@Composable
fun produceColorSchemeState(
    theme: Theme?,
    colorSource: ColorSource?,
    customSeedColor: Int?,
    colorSchemeSettings: ColorSchemeSettings?,
): State<ColorScheme?> {
    val wallpaperManager = LocalWallpaperManager.current
    val materialColorSchemeResolver = LocalMaterialColorSchemeResolver.current
    val isSystemDarkTheme = isSystemInDarkTheme()

    return produceState(
        null,
        theme,
        colorSource,
        customSeedColor,
        colorSchemeSettings,
        wallpaperManager,
        materialColorSchemeResolver,
        isSystemDarkTheme,
    ) {
        if (theme == null || colorSource == null || customSeedColor == null || colorSchemeSettings == null) {
            value = null
            return@produceState
        }

        wallpaperManager.getColorsChanged().collect { colorHints ->
            val resolvedTheme = resolveTheme(
                theme = theme,
                colorHints = colorHints,
                hintSupportsDarkTheme = wallpaperManager.hintSupportsDarkTheme,
            )

            val isDarkTheme = when (resolvedTheme) {
                Theme.System -> isSystemDarkTheme
                Theme.Light -> false
                Theme.Dark -> true
                Theme.Wallpaper -> error("Wallpaper theme must be resolved before choosing a color scheme")
            }

            val fallbackColorScheme = getDefaultColorScheme(isDarkTheme)

            value = when (colorSource) {
                ColorSource.None -> fallbackColorScheme

                ColorSource.Wallpaper -> materialColorSchemeResolver.getWallpaperColorScheme(
                    isDarkTheme,
                )
                    ?: fallbackColorScheme

                ColorSource.Custom -> materialColorSchemeResolver.getCustomColorScheme(
                    seedColor = customSeedColor,
                    isDarkTheme = isDarkTheme,
                    colorSchemeSettings = colorSchemeSettings,
                )
            }
        }
    }
}

private fun resolveTheme(
    theme: Theme,
    colorHints: Int?,
    hintSupportsDarkTheme: Int,
): Theme = when (theme) {
    Theme.System -> Theme.System

    Theme.Light -> Theme.Light

    Theme.Dark -> Theme.Dark

    Theme.Wallpaper -> colorHints?.let { wallpaperColorHints ->
        if (wallpaperColorHints and hintSupportsDarkTheme != 0) {
            Theme.Dark
        } else {
            Theme.Light
        }
    } ?: Theme.System
}
