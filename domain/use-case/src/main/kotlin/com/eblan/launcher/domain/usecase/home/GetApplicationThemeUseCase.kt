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
package com.eblan.launcher.domain.usecase.home

import com.eblan.launcher.domain.framework.ResourcesWrapper
import com.eblan.launcher.domain.framework.WallpaperManagerWrapper
import com.eblan.launcher.domain.model.userdata.ApplicationTheme
import com.eblan.launcher.domain.model.userdata.ColorSource
import com.eblan.launcher.domain.model.userdata.Theme
import com.eblan.launcher.domain.repository.UserDataRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/** Resolves the launcher theme from the user's preference and system wallpaper hints. */
class GetApplicationThemeUseCase @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val wallpaperManagerWrapper: WallpaperManagerWrapper,
    private val resourcesWrapper: ResourcesWrapper,
) {
    /** Emits the selected theme and colors, updating when wallpaper color hints change. */
    operator fun invoke(): Flow<ApplicationTheme> = combine(
        userDataRepository.userDataFlow,
        wallpaperManagerWrapper.getColorsChanged(),
    ) { userData, colorHints ->
        when (userData.generalSettings.theme) {
            Theme.System -> {
                getApplicationThemeByColorHints(
                    colorHints = colorHints,
                    colorSource = userData.generalSettings.colorSource,
                    customSeedColor = userData.generalSettings.customSeedColor,
                    systemBarsTheme = userData.generalSettings.systemBarsTheme,
                )
            }

            Theme.Light, Theme.Dark -> {
                ApplicationTheme(
                    theme = userData.generalSettings.theme,
                    systemBarsTheme = userData.generalSettings.systemBarsTheme,
                    colorSource = userData.generalSettings.colorSource,
                    customSeedColor = userData.generalSettings.customSeedColor,
                )
            }
        }
    }

    /** Resolves system mode from wallpaper hints, falling back to the current system theme. */
    private fun getApplicationThemeByColorHints(
        colorHints: Int?,
        colorSource: ColorSource,
        customSeedColor: Int,
        systemBarsTheme: Theme,
    ): ApplicationTheme = if (colorHints != null) {
        val hintSupportsDarkTheme =
            colorHints and wallpaperManagerWrapper.hintSupportsDarkTheme != 0

        if (hintSupportsDarkTheme) {
            ApplicationTheme(
                theme = Theme.Dark,
                systemBarsTheme = systemBarsTheme,
                colorSource = colorSource,
                customSeedColor = customSeedColor,
            )
        } else {
            ApplicationTheme(
                theme = Theme.Light,
                systemBarsTheme = systemBarsTheme,
                colorSource = colorSource,
                customSeedColor = customSeedColor,
            )
        }
    } else {
        ApplicationTheme(
            theme = if (resourcesWrapper.isDarkTheme()) {
                Theme.Dark
            } else {
                Theme.Light
            },
            systemBarsTheme = systemBarsTheme,
            colorSource = colorSource,
            customSeedColor = customSeedColor,
        )
    }
}
