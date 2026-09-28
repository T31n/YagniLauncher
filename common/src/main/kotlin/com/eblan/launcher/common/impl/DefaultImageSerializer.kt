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

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.core.graphics.createBitmap
import com.eblan.launcher.common.AndroidImageSerializer
import com.eblan.launcher.domain.common.Dispatcher
import com.eblan.launcher.domain.common.EblanDispatchers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

internal class DefaultImageSerializer @Inject constructor(
    @param:Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
    @param:Dispatcher(EblanDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : AndroidImageSerializer {
    override suspend fun createByteArray(drawable: Drawable): ByteArray? = withContext(defaultDispatcher) {
        ByteArrayOutputStream().use { stream ->
            drawable.toBitmap()?.compress(
                Bitmap.CompressFormat.PNG,
                100,
                stream,
            )

            stream.toByteArray()
        }
    }

    override suspend fun createByteArray(bitmap: Bitmap?): ByteArray? = ByteArrayOutputStream().use { stream ->
        withContext(defaultDispatcher) {
            bitmap?.compress(Bitmap.CompressFormat.PNG, 100, stream)

            stream.toByteArray()
        }
    }

    override suspend fun createDrawablePath(
        drawable: Drawable,
        file: File,
    ) {
        withContext(ioDispatcher) {
            drawable.toBitmap()?.let { bitmap ->
                val createNew = if (file.exists()) {
                    val oldBitmap = BitmapFactory.decodeFile(file.path)

                    oldBitmap == null || !bitmap.sameAs(oldBitmap)
                } else {
                    true
                }

                if (createNew) {
                    FileOutputStream(file).use {
                        bitmap.compress(
                            Bitmap.CompressFormat.PNG,
                            100,
                            it,
                        )
                    }
                }
            }
        }
    }

    private fun Drawable.toBitmap(): Bitmap? = if (this is BitmapDrawable) {
        bitmap
    } else {
        val width = if (bounds.isEmpty) {
            intrinsicWidth
        } else {
            bounds.width()
        }

        val height = if (bounds.isEmpty) {
            intrinsicHeight
        } else {
            bounds.height()
        }

        if (width > 0 && height > 0) {
            createBitmap(
                width = width,
                height = height,
                config = Bitmap.Config.ARGB_8888,
            ).apply {
                val canvas = Canvas(this)

                setBounds(0, 0, canvas.width, canvas.height)

                draw(canvas)
            }
        } else {
            null
        }
    }
}
