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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.addLastModifiedToFileCacheKey
import coil3.size.Size

@Composable
internal fun AsyncFilePathImage(
    filePath: String?,
    lastModifiedFilePaths: Map<String, Long>,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    size: Size? = Size.ORIGINAL,
) {
    val context = LocalContext.current

    val lastModified = filePath?.let(lastModifiedFilePaths::get)

    val model = remember(
        context,
        filePath,
        lastModified,
        size,
    ) {
        ImageRequest.Builder(context)
            .data(filePath)
            .apply {
                if (filePath != null && lastModified != null) {
                    memoryCacheKey("$filePath:$lastModified:${size ?: "auto"}")
                }

                size?.let(::size)
            }
            .addLastModifiedToFileCacheKey(enable = true)
            .build()
    }

    AsyncImage(
        model = model,
        contentDescription = null,
        modifier = modifier,
        contentScale = contentScale,
    )
}
