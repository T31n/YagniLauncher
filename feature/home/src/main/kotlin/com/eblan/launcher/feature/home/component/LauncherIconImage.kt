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
package com.eblan.launcher.feature.home.component

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.rememberAsyncImagePainter
import coil3.compose.useExistingImageAsPlaceholder
import coil3.request.ImageRequest
import coil3.request.addLastModifiedToFileCacheKey
import coil3.request.crossfade
import coil3.size.Size
import com.eblan.launcher.ui.local.LocalImageSerializer

@OptIn(ExperimentalCoilApi::class)
@Composable
internal fun LauncherIconImage(
    iconPath: String?,
    modifier: Modifier = Modifier,
    size: Size = Size.ORIGINAL,
) {
    val context = LocalContext.current
    val imageSerializer = LocalImageSerializer.current

    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(iconPath)
            .addLastModifiedToFileCacheKey(true)
            .useExistingImageAsPlaceholder(true)
            .size(size)
            .crossfade(false)
            .build(),
    )

    LaunchedEffect(iconPath, painter, imageSerializer) {
        if (iconPath == null) return@LaunchedEffect

        imageSerializer.updatedPaths.collect { updatedPath ->
            if (updatedPath == iconPath) {
                painter.restart()
            }
        }
    }

    Image(
        painter = painter,
        contentDescription = null,
        modifier = modifier,
    )
}
