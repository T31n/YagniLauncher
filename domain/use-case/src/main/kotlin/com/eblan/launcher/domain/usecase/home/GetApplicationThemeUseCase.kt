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

class GetApplicationThemeUseCase @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val wallpaperManagerWrapper: WallpaperManagerWrapper,
    private val resourcesWrapper: ResourcesWrapper,
) {
    operator fun invoke(): Flow<ApplicationTheme> = combine(
        userDataRepository.userDataFlow,
        wallpaperManagerWrapper.getColorsChanged(),
    ) { userData, colorHints ->
        when (userData.generalSettings.theme) {
            Theme.System -> {
                getApplicationThemeByColorHints(
                    colorHints = colorHints,
                    dynamicTheme = userData.generalSettings.dynamicTheme,
                )
            }

            Theme.Light, Theme.Dark -> {
                ApplicationTheme(
                    theme = userData.generalSettings.theme,
                    dynamicTheme = userData.generalSettings.dynamicTheme,
                )
            }
        }
    }

    private fun getApplicationThemeByColorHints(
        colorHints: Int?,
        dynamicTheme: Boolean,
    ): ApplicationTheme = if (colorHints != null) {
        val hintSupportsDarkTheme =
            colorHints and wallpaperManagerWrapper.hintSupportsDarkTheme != 0

        if (hintSupportsDarkTheme) {
            ApplicationTheme(
                theme = Theme.Dark,
                dynamicTheme = dynamicTheme,
            )
        } else {
            ApplicationTheme(
                theme = Theme.Light,
                dynamicTheme = dynamicTheme,
            )
        }
    } else {
        ApplicationTheme(
            theme = resourcesWrapper.getSystemTheme(),
            dynamicTheme = dynamicTheme,
        )
    }
}
