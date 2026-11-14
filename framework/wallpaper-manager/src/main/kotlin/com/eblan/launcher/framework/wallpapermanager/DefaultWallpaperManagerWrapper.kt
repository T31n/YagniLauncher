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
package com.eblan.launcher.framework.wallpapermanager

import android.app.WallpaperColors
import android.app.WallpaperManager
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import com.eblan.launcher.domain.framework.WallpaperManagerWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

internal class DefaultWallpaperManagerWrapper @Inject constructor(@param:ApplicationContext private val context: Context) :
    WallpaperManagerWrapper,
    AndroidWallpaperManagerWrapper {
    private val wallpaperManager = WallpaperManager.getInstance(context)

    override val hintSupportsDarkText = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        WallpaperColors.HINT_SUPPORTS_DARK_TEXT
    } else {
        0
    }

    override val hintSupportsDarkTheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        WallpaperColors.HINT_SUPPORTS_DARK_THEME
    } else {
        0
    }

    override fun getColorsChanged(): Flow<Int?> = callbackFlow {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            send(wallpaperManager.getWallpaperColors(WallpaperManager.FLAG_SYSTEM)?.colorHints)

            val callback = WallpaperManager.OnColorsChangedListener { wallpaperColors, which ->
                if (which and WallpaperManager.FLAG_SYSTEM != 0) {
                    trySend(wallpaperColors?.colorHints)
                }
            }

            wallpaperManager.addOnColorsChangedListener(
                callback,
                Handler(Looper.getMainLooper()),
            )

            awaitClose {
                wallpaperManager.removeOnColorsChangedListener(callback)
            }
        } else {
            send(null)

            awaitClose()
        }
    }

    override fun setWallpaperOffsetSteps(xStep: Float, yStep: Float) {
        wallpaperManager.setWallpaperOffsetSteps(xStep, yStep)
    }

    override fun setWallpaperOffsets(
        windowToken: android.os.IBinder,
        xOffset: Float,
        yOffset: Float,
    ) {
        wallpaperManager.setWallpaperOffsets(windowToken, xOffset, yOffset)
    }
}
