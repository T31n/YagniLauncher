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
package com.eblan.launcher.platform.imageserializer

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toDrawable
import com.eblan.launcher.domain.common.Dispatcher
import com.eblan.launcher.domain.common.EblanDispatchers
import com.eblan.launcher.domain.model.userdata.ColorSchemeSettings
import com.eblan.launcher.domain.model.userdata.ColorSource
import com.eblan.launcher.domain.model.userdata.IconShape
import com.eblan.launcher.domain.model.userdata.Theme
import com.eblan.launcher.platform.colorscheme.MaterialColorSchemeResolver
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import kotlin.math.cos
import kotlin.math.sin

internal class DefaultImageSerializer @Inject constructor(
    private val materialColorSchemeResolver: MaterialColorSchemeResolver,
    @param:Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
    @param:Dispatcher(EblanDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : AndroidImageSerializer {
    override suspend fun createByteArray(drawable: Drawable): ByteArray? = withContext(defaultDispatcher) {
        val bitmap = drawable.toBitmap() ?: return@withContext null

        ByteArrayOutputStream().use { stream ->
            bitmap.compress(
                Bitmap.CompressFormat.PNG,
                100,
                stream,
            )

            stream.toByteArray()
        }
    }

    override suspend fun createByteArray(bitmap: Bitmap?): ByteArray? = withContext(defaultDispatcher) {
        if (bitmap == null) return@withContext null

        ByteArrayOutputStream().use { stream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)

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
                    FileOutputStream(file).use { currentFileOutputStream ->
                        bitmap.compress(
                            Bitmap.CompressFormat.PNG,
                            100,
                            currentFileOutputStream,
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

        val bitmap = drawable.toBitmap(unmaskAdaptiveIcon = true) ?: return null
        val shapedBitmap = createBitmap(bitmap.width, bitmap.height)
        val canvas = Canvas(shapedBitmap)
        val bounds = RectF(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat())
        val path = when (iconShape) {
            IconShape.Circle -> circlePath(bounds)
            IconShape.Square -> squarePath(bounds)
            IconShape.RoundedSquare -> roundedSquarePath(bounds)
            IconShape.TearDrop -> tearDropPath(bounds)
            IconShape.Hexagon -> hexagonPath(bounds)
            IconShape.Octagon -> octagonPath(bounds)
            IconShape.Diamond -> diamondPath(bounds)
            IconShape.Pentagon -> pentagonPath(bounds)
            IconShape.Clover -> cloverPath(bounds)
            IconShape.Cookie -> cookiePath(bounds)
            IconShape.Flower -> flowerPath(bounds)
            IconShape.Shield -> shieldPath(bounds)
            IconShape.None -> return drawable
        }

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        }
        canvas.drawPath(path, paint)
        return shapedBitmap.toDrawable(Resources.getSystem())
    }

    private fun circlePath(bounds: RectF) = Path().apply {
        addCircle(
            bounds.centerX(),
            bounds.centerY(),
            minOf(bounds.width(), bounds.height()) / 2f,
            Path.Direction.CW,
        )
    }

    private fun squarePath(bounds: RectF) = Path().apply {
        addRect(bounds, Path.Direction.CW)
    }

    private fun roundedSquarePath(bounds: RectF) = Path().apply {
        val cornerRadius =
            minOf(bounds.width(), bounds.height()) * ROUNDED_SQUARE_CORNER_RATIO
        addRoundRect(bounds, cornerRadius, cornerRadius, Path.Direction.CW)
    }

    private fun tearDropPath(bounds: RectF) = Path().apply {
        val centerX = bounds.centerX()
        val centerY = bounds.centerY()
        moveTo(centerX, bounds.top)
        cubicTo(
            centerX + bounds.width() * 0.03f,
            bounds.top + bounds.height() * 0.24f,
            bounds.right,
            centerY - bounds.height() * 0.1f,
            bounds.right,
            centerY + bounds.height() * 0.08f,
        )
        cubicTo(
            bounds.right,
            bounds.bottom - bounds.height() * 0.08f,
            centerX + bounds.width() * 0.25f,
            bounds.bottom,
            centerX,
            bounds.bottom,
        )
        cubicTo(
            centerX - bounds.width() * 0.25f,
            bounds.bottom,
            bounds.left,
            bounds.bottom - bounds.height() * 0.08f,
            bounds.left,
            centerY + bounds.height() * 0.08f,
        )
        cubicTo(
            bounds.left,
            centerY - bounds.height() * 0.1f,
            centerX - bounds.width() * 0.03f,
            bounds.top + bounds.height() * 0.24f,
            centerX,
            bounds.top,
        )
        close()
    }

    private fun hexagonPath(bounds: RectF) = Path().apply {
        moveTo(bounds.left + bounds.width() * 0.25f, bounds.top)
        lineTo(bounds.right - bounds.width() * 0.25f, bounds.top)
        lineTo(bounds.right, bounds.centerY())
        lineTo(bounds.right - bounds.width() * 0.25f, bounds.bottom)
        lineTo(bounds.left + bounds.width() * 0.25f, bounds.bottom)
        lineTo(bounds.left, bounds.centerY())
        close()
    }

    private fun octagonPath(bounds: RectF) = Path().apply {
        val corner = minOf(bounds.width(), bounds.height()) * 0.29f
        moveTo(bounds.left + corner, bounds.top)
        lineTo(bounds.right - corner, bounds.top)
        lineTo(bounds.right, bounds.top + corner)
        lineTo(bounds.right, bounds.bottom - corner)
        lineTo(bounds.right - corner, bounds.bottom)
        lineTo(bounds.left + corner, bounds.bottom)
        lineTo(bounds.left, bounds.bottom - corner)
        lineTo(bounds.left, bounds.top + corner)
        close()
    }

    private fun diamondPath(bounds: RectF) = Path().apply {
        moveTo(bounds.centerX(), bounds.top)
        lineTo(bounds.right, bounds.centerY())
        lineTo(bounds.centerX(), bounds.bottom)
        lineTo(bounds.left, bounds.centerY())
        close()
    }

    private fun pentagonPath(bounds: RectF) = Path().apply {
        val centerX = bounds.centerX()
        val centerY = bounds.centerY()
        val radiusX = bounds.width() / 2f
        val radiusY = bounds.height() / 2f
        for (vertex in 0 until 5) {
            val angle = Math.toRadians((vertex * 72 - 90).toDouble())
            val x = centerX + radiusX * cos(angle).toFloat()
            val y = centerY + radiusY * sin(angle).toFloat()
            if (vertex == 0) {
                moveTo(x, y)
            } else {
                lineTo(x, y)
            }
        }
        close()
    }

    private fun cloverPath(bounds: RectF) = Path().apply {
        val centerX = bounds.centerX()
        val centerY = bounds.centerY()
        val quarterWidth = bounds.width() * 0.25f
        val quarterHeight = bounds.height() * 0.25f
        moveTo(centerX, bounds.top)
        cubicTo(
            centerX - quarterWidth * 0.08f,
            bounds.top + quarterHeight * 0.72f,
            bounds.left + quarterWidth * 0.72f,
            bounds.top + quarterHeight * 0.08f,
            bounds.left + quarterWidth * 0.72f,
            bounds.top + quarterHeight * 0.72f,
        )
        cubicTo(
            bounds.left + quarterWidth * 0.08f,
            bounds.top + quarterHeight * 0.72f,
            bounds.left + quarterWidth * 0.08f,
            centerY - quarterHeight * 0.08f,
            centerX,
            centerY,
        )
        cubicTo(
            bounds.left + quarterWidth * 0.08f,
            centerY + quarterHeight * 0.08f,
            bounds.left + quarterWidth * 0.08f,
            bounds.bottom - quarterHeight * 0.72f,
            bounds.left + quarterWidth * 0.72f,
            bounds.bottom - quarterHeight * 0.72f,
        )
        cubicTo(
            bounds.left + quarterWidth * 0.72f,
            bounds.bottom - quarterHeight * 0.08f,
            centerX - quarterWidth * 0.08f,
            bounds.bottom - quarterHeight * 0.08f,
            centerX,
            bounds.bottom,
        )
        cubicTo(
            centerX + quarterWidth * 0.08f,
            bounds.bottom - quarterHeight * 0.08f,
            bounds.right - quarterWidth * 0.72f,
            bounds.bottom - quarterHeight * 0.08f,
            bounds.right - quarterWidth * 0.72f,
            bounds.bottom - quarterHeight * 0.72f,
        )
        cubicTo(
            bounds.right - quarterWidth * 0.08f,
            bounds.bottom - quarterHeight * 0.72f,
            bounds.right - quarterWidth * 0.08f,
            centerY + quarterHeight * 0.08f,
            centerX,
            centerY,
        )
        cubicTo(
            bounds.right - quarterWidth * 0.08f,
            centerY - quarterHeight * 0.08f,
            bounds.right - quarterWidth * 0.08f,
            bounds.top + quarterHeight * 0.72f,
            bounds.right - quarterWidth * 0.72f,
            bounds.top + quarterHeight * 0.72f,
        )
        cubicTo(
            bounds.right - quarterWidth * 0.72f,
            bounds.top + quarterHeight * 0.08f,
            centerX + quarterWidth * 0.08f,
            bounds.top + quarterHeight * 0.72f,
            centerX,
            bounds.top,
        )
        close()
    }

    private fun cookiePath(bounds: RectF) = Path().apply {
        fillType = Path.FillType.EVEN_ODD
        addOval(bounds, Path.Direction.CW)
        addCircle(
            bounds.right - bounds.width() * 0.12f,
            bounds.top + bounds.height() * 0.12f,
            minOf(bounds.width(), bounds.height()) * 0.23f,
            Path.Direction.CW,
        )
    }

    private fun flowerPath(bounds: RectF) = Path().apply {
        val centerX = bounds.centerX()
        val centerY = bounds.centerY()
        val radius = minOf(bounds.width(), bounds.height()) * 0.28f
        val orbitRadius = minOf(bounds.width(), bounds.height()) * 0.22f
        for (petal in 0 until 5) {
            val angle = Math.toRadians((petal * 72 - 90).toDouble())
            val x = centerX + orbitRadius * cos(angle).toFloat()
            val y = centerY + orbitRadius * sin(angle).toFloat()
            addCircle(x, y, radius, Path.Direction.CW)
        }
    }

    private fun shieldPath(bounds: RectF) = Path().apply {
        val shoulder = bounds.height() * 0.12f
        moveTo(bounds.left, bounds.top + shoulder)
        quadTo(bounds.left, bounds.top, bounds.left + shoulder, bounds.top)
        lineTo(bounds.right - shoulder, bounds.top)
        quadTo(bounds.right, bounds.top, bounds.right, bounds.top + shoulder)
        lineTo(bounds.right, bounds.centerY())
        cubicTo(
            bounds.right,
            bounds.bottom - bounds.height() * 0.2f,
            bounds.centerX() + bounds.width() * 0.18f,
            bounds.bottom - bounds.height() * 0.04f,
            bounds.centerX(),
            bounds.bottom,
        )
        cubicTo(
            bounds.centerX() - bounds.width() * 0.18f,
            bounds.bottom - bounds.height() * 0.04f,
            bounds.left,
            bounds.bottom - bounds.height() * 0.2f,
            bounds.left,
            bounds.centerY(),
        )
        close()
    }

    override fun getTintedAndShapedDrawable(
        drawable: Drawable,
        colorSource: ColorSource,
        customSeedColor: Int,
        theme: Theme,
        iconShape: IconShape,
        colorSchemeSettings: ColorSchemeSettings,
    ): Drawable? {
        val tintedDrawable = getTintedDrawable(
            drawable = drawable,
            colorSource = colorSource,
            customSeedColor = customSeedColor,
            theme = theme,
            colorSchemeSettings = colorSchemeSettings,
        )

        return getShapedDrawable(
            drawable = tintedDrawable,
            iconShape = iconShape,
        )
    }

    private fun Drawable.toBitmap(unmaskAdaptiveIcon: Boolean = false): Bitmap? = if (this is BitmapDrawable) {
        bitmap
    } else {
        val width = if (bounds.isEmpty) intrinsicWidth else bounds.width()
        val height = if (bounds.isEmpty) intrinsicHeight else bounds.height()

        if (width <= 0 || height <= 0) {
            null
        } else if (unmaskAdaptiveIcon && this is AdaptiveIconDrawable) {
            val insetX = (width * AdaptiveIconDrawable.getExtraInsetFraction()).toInt()
            val insetY = (height * AdaptiveIconDrawable.getExtraInsetFraction()).toInt()
            createBitmap(
                width = width,
                height = height,
                config = Bitmap.Config.ARGB_8888,
            ).apply {
                val canvas = Canvas(this)
                listOfNotNull(background, foreground).forEach { layer ->
                    val oldBounds = Rect(layer.bounds)
                    layer.setBounds(-insetX, -insetY, width + insetX, height + insetY)
                    layer.draw(canvas)
                    layer.bounds = oldBounds
                }
            }
        } else {
            createBitmap(
                width = width,
                height = height,
                config = Bitmap.Config.ARGB_8888,
            ).apply {
                val canvas = Canvas(this)
                val oldBounds = Rect(bounds)
                setBounds(0, 0, canvas.width, canvas.height)
                draw(canvas)
                bounds = oldBounds
            }
        }
    }

    private fun getTintedDrawable(
        drawable: Drawable,
        colorSource: ColorSource,
        customSeedColor: Int,
        theme: Theme,
        colorSchemeSettings: ColorSchemeSettings,
    ): Drawable {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            drawable !is AdaptiveIconDrawable ||
            drawable.monochrome == null
        ) {
            return drawable
        }

        val copy = (drawable.constantState?.newDrawable()?.mutate() ?: drawable.mutate())
            as? AdaptiveIconDrawable ?: return drawable
        val monochromeDrawable = copy.monochrome ?: return drawable

        val primaryColor = materialColorSchemeResolver.getPrimaryColor(
            colorSource = colorSource,
            customSeedColor = customSeedColor,
            theme = theme,
            colorSchemeSettings = colorSchemeSettings,
        )

        val surfaceColor = materialColorSchemeResolver.getSurfaceColor(
            colorSource = colorSource,
            customSeedColor = customSeedColor,
            theme = theme,
            colorSchemeSettings = colorSchemeSettings,
        )

        return AdaptiveIconDrawable(
            surfaceColor.toDrawable(),
            monochromeDrawable.mutate().apply { setTint(primaryColor) },
        )
    }

    private companion object {
        const val ROUNDED_SQUARE_CORNER_RATIO = 0.2f
    }
}
