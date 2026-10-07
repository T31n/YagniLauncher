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

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toDrawable
import com.eblan.launcher.common.AndroidImageSerializer
import com.eblan.launcher.domain.common.Dispatcher
import com.eblan.launcher.domain.common.EblanDispatchers
import com.eblan.launcher.domain.framework.ResourcesWrapper
import com.eblan.launcher.domain.model.userdata.IconShape
import com.eblan.launcher.domain.model.userdata.IconTint
import com.eblan.launcher.domain.model.userdata.Theme
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

internal class DefaultImageSerializer @Inject constructor(
    private val resourcesWrapper: ResourcesWrapper,
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

    override fun getShapedDrawable(
        drawable: Drawable,
        iconShape: IconShape,
    ): Drawable? {
        if (iconShape == IconShape.None) return drawable

        val bitmap = drawable.toBitmap() ?: return null
        val shapedBitmap = createBitmap(bitmap.width, bitmap.height)
        val canvas = Canvas(shapedBitmap)
        val path = Path()
        val bounds = RectF(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat())

        when (iconShape) {
            IconShape.Circle -> path.addCircle(
                bounds.centerX(),
                bounds.centerY(),
                minOf(bounds.width(), bounds.height()) / 2f,
                Path.Direction.CW,
            )

            IconShape.Square -> path.addRect(bounds, Path.Direction.CW)

            IconShape.RoundedSquare -> {
                val cornerRadius =
                    minOf(bounds.width(), bounds.height()) * ROUNDED_SQUARE_CORNER_RATIO
                path.addRoundRect(bounds, cornerRadius, cornerRadius, Path.Direction.CW)
            }
        }

        canvas.clipPath(path)
        canvas.drawBitmap(bitmap, 0f, 0f, null)
        return shapedBitmap.toDrawable(Resources.getSystem())
    }

    override fun getTintedAndShapedDrawable(
        drawable: Drawable,
        iconTint: IconTint,
        customIconTint: Int,
        fallbackIconTint: Boolean,
        theme: Theme,
        iconShape: IconShape,
    ): Drawable? {
        val tintedDrawable = when (iconTint) {
            IconTint.None -> drawable

            IconTint.System,
            IconTint.Custom,
            -> getTintedDrawable(
                drawable = drawable,
                customIconTint = customIconTint,
                fallbackIconTint = fallbackIconTint,
                theme = theme,
            ) ?: return null
        }

        return getShapedDrawable(
            drawable = tintedDrawable,
            iconShape = iconShape,
        )
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

    private fun getTintedDrawable(
        drawable: Drawable,
        customIconTint: Int,
        fallbackIconTint: Boolean,
        theme: Theme,
    ): Drawable? {
        val copy = drawable.constantState?.newDrawable()?.mutate() ?: drawable.mutate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            copy is AdaptiveIconDrawable
        ) {
            copy.monochrome?.let {
                return AdaptiveIconDrawable(
                    adaptiveIconBackgroundColor(
                        theme = theme,
                        customIconColor = customIconTint,
                    ).toDrawable(),
                    it.mutate().apply { setTint(customIconTint) },
                )
            }
        }

        return if (fallbackIconTint) {
            copy.tintedBitmap(customIconColor = customIconTint)
        } else {
            copy
        }
    }

    private fun Drawable.tintedBitmap(customIconColor: Int): Drawable? {
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

        return if (width > 0 && height > 0) {
            val source = createBitmap(width, height)

            setBounds(0, 0, width, height)

            draw(Canvas(source))

            val result = createBitmap(width, height)

            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                colorFilter = luminanceTintFilter(customIconColor)
            }

            Canvas(result).drawBitmap(source, 0f, 0f, paint)

            source.recycle()

            result.toDrawable(Resources.getSystem())
        } else {
            null
        }
    }

    private fun luminanceTintFilter(@ColorInt tint: Int): ColorFilter {
        val r = Color.red(tint) / 255f
        val g = Color.green(tint) / 255f
        val b = Color.blue(tint) / 255f
        // luminance = 0.213R + 0.715G + 0.072B, then scaled per channel by the tint
        return ColorMatrixColorFilter(
            floatArrayOf(
                0.213f * r, 0.715f * r, 0.072f * r, 0f, 0f,
                0.213f * g, 0.715f * g, 0.072f * g, 0f, 0f,
                0.213f * b, 0.715f * b, 0.072f * b, 0f, 0f,
                0f, 0f, 0f, 1f, 0f,
            ),
        )
    }

    private fun adaptiveIconBackgroundColor(
        theme: Theme,
        customIconColor: Int,
    ): Int {
        val isDarkTheme = when (theme) {
            Theme.System -> resourcesWrapper.isDarkTheme()
            Theme.Light -> false
            Theme.Dark -> true
        }

        val baseColor = if (ColorUtils.calculateLuminance(customIconColor) > DARK_COLOR_LUMINANCE_THRESHOLD) {
            Color.BLACK
        } else {
            Color.WHITE
        }
        var tintAmount =
            if (isDarkTheme) DARK_BACKGROUND_TINT_AMOUNT else LIGHT_BACKGROUND_TINT_AMOUNT

        while (tintAmount > 0f) {
            val backgroundColor = ColorUtils.blendARGB(
                baseColor,
                customIconColor,
                tintAmount,
            )

            if (ColorUtils.calculateContrast(customIconColor, backgroundColor) >= MIN_ICON_CONTRAST_RATIO) {
                return backgroundColor
            }

            tintAmount /= 2f
        }

        return baseColor
    }

    private companion object {
        const val ROUNDED_SQUARE_CORNER_RATIO = 0.2f
        const val LIGHT_BACKGROUND_TINT_AMOUNT = 0.16f
        const val DARK_BACKGROUND_TINT_AMOUNT = 0.36f
        const val DARK_COLOR_LUMINANCE_THRESHOLD = 0.179
        const val MIN_ICON_CONTRAST_RATIO = 3.0
    }
}
