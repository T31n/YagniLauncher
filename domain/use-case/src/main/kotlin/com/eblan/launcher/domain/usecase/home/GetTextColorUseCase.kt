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

import com.eblan.launcher.domain.common.Dispatcher
import com.eblan.launcher.domain.common.EblanDispatchers
import com.eblan.launcher.domain.framework.ResourcesWrapper
import com.eblan.launcher.domain.framework.WallpaperManagerWrapper
import com.eblan.launcher.domain.model.userdata.TextColor
import com.eblan.launcher.domain.model.userdata.Theme
import com.eblan.launcher.domain.repository.UserDataRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class GetTextColorUseCase @Inject constructor(
    private val userDataRepository: UserDataRepository,
    private val wallpaperManagerWrapper: WallpaperManagerWrapper,
    private val resourcesWrapper: ResourcesWrapper,
    @param:Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) {
    operator fun invoke(): Flow<TextColor> = combine(
        userDataRepository.userDataFlow,
        wallpaperManagerWrapper.getColorsChanged(),
    ) { userData, colorHints ->
        when (userData.homeSettings.gridItemSettings.textColor) {
            TextColor.System -> {
                getTextColorFromWallpaperColors(
                    theme = userData.generalSettings.theme,
                    colorHints = colorHints,
                )
            }

            else -> userData.homeSettings.gridItemSettings.textColor
        }
    }.flowOn(defaultDispatcher)

    private fun getTextColorFromWallpaperColors(
        theme: Theme,
        colorHints: Int?,
    ): TextColor = if (colorHints != null) {
        val hintSupportsDarkText = colorHints and wallpaperManagerWrapper.hintSupportsDarkText != 0

        if (hintSupportsDarkText) {
            TextColor.Dark
        } else {
            TextColor.Light
        }
    } else {
        getTextColorFromSystemTheme(theme = theme)
    }

    private fun getTextColorFromSystemTheme(theme: Theme): TextColor = when (theme) {
        Theme.System -> getTextColorFromSystemTheme(theme = resourcesWrapper.getSystemTheme())
        Theme.Light -> TextColor.Light
        Theme.Dark -> TextColor.Dark
    }
}
