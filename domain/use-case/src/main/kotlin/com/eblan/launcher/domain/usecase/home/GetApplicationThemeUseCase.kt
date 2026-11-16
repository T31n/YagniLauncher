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
import com.eblan.launcher.domain.model.userdata.Theme
import com.eblan.launcher.domain.repository.UserDataRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/** Resolves the launcher and system-bar themes from user preferences, device mode, and wallpaper hints. */
class GetApplicationThemeUseCase @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val wallpaperManagerWrapper: WallpaperManagerWrapper,
    private val resourcesWrapper: ResourcesWrapper,
) {
    /** Emits resolved themes and colors, updating when wallpaper color hints change. */
    operator fun invoke(): Flow<ApplicationTheme> = combine(
        userDataRepository.userDataFlow,
        wallpaperManagerWrapper.getColorsChanged(),
    ) { userData, colorHints ->
        ApplicationTheme(
            theme = resolveTheme(
                theme = userData.generalSettings.theme,
                colorHints = colorHints,
            ),
            systemBarsTheme = resolveTheme(
                theme = userData.generalSettings.systemBarsTheme,
                colorHints = colorHints,
            ),
            colorSource = userData.generalSettings.colorSource,
            customSeedColor = userData.generalSettings.customSeedColor,
        )
    }

    private fun resolveTheme(
        theme: Theme,
        colorHints: Int?,
    ): Theme = when (theme) {
        Theme.System -> getSystemTheme()

        Theme.Light -> Theme.Light

        Theme.Dark -> Theme.Dark

        Theme.Wallpaper -> colorHints?.let { wallpaperColorHints ->
            if (wallpaperColorHints and wallpaperManagerWrapper.hintSupportsDarkTheme != 0) {
                Theme.Dark
            } else {
                Theme.Light
            }
        } ?: getSystemTheme()
    }

    private fun getSystemTheme(): Theme = if (resourcesWrapper.isDarkTheme()) {
        Theme.Dark
    } else {
        Theme.Light
    }
}
