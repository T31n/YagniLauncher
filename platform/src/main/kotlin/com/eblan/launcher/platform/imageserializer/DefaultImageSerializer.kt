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
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sign
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
                    val parentDir = file.parentFile?.apply { if (!exists()) mkdirs() }
                    val tempFile = File.createTempFile("tmp_${file.name}", ".png", parentDir)

                    try {
                        FileOutputStream(tempFile).use { currentFileOutputStream ->
                            bitmap.compress(
                                Bitmap.CompressFormat.PNG,
                                100,
                                currentFileOutputStream,
                            )
                        }

                        if (!tempFile.renameTo(file)) {
                            tempFile.delete()
                        }
                    } catch (e: Throwable) {
                        tempFile.delete()
                        throw e
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
            IconShape.Squircle -> squirclePath(bounds)
            IconShape.RoundedSquare -> roundedSquarePath(bounds)
            IconShape.Square -> squarePath(bounds)
            IconShape.TearDrop -> tearDropPath(bounds)
            IconShape.Leaf -> leafPath(bounds)
            IconShape.Arch -> archPath(bounds)
            IconShape.Hexagon -> hexagonPath(bounds)
            IconShape.Octagon -> octagonPath(bounds)
            IconShape.Pentagon -> pentagonPath(bounds)
            IconShape.Diamond -> diamondPath(bounds)
            IconShape.Clover -> cloverPath(bounds)
            IconShape.Cookie -> cookiePath(bounds)
            IconShape.Flower -> flowerPath(bounds)
            IconShape.Scallop -> scallopPath(bounds)
            IconShape.Shield -> shieldPath(bounds)
            IconShape.Heart -> heartPath(bounds)
            IconShape.Star -> starPath(bounds)
            IconShape.Cloud -> cloudPath(bounds)
            IconShape.Cat -> catPath(bounds)
            IconShape.Ghost -> ghostPath(bounds)
            IconShape.PacMan -> pacManPath(bounds)
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

    private fun squirclePath(bounds: RectF) = Path().apply {
        val centerX = bounds.centerX()
        val centerY = bounds.centerY()
        val radiusX = bounds.width() / 2f
        val radiusY = bounds.height() / 2f
        val steps = 72
        val exponent = 0.46
        for (step in 0 until steps) {
            val angle = (2.0 * PI * step) / steps
            val cosAngle = cos(angle)
            val sinAngle = sin(angle)
            val x = centerX + radiusX * (sign(cosAngle) * abs(cosAngle).pow(exponent)).toFloat()
            val y = centerY + radiusY * (sign(sinAngle) * abs(sinAngle).pow(exponent)).toFloat()
            if (step == 0) {
                moveTo(x, y)
            } else {
                lineTo(x, y)
            }
        }
        close()
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
        val size = minOf(bounds.width(), bounds.height())
        val largeRadius = size * 0.5f
        val smallRadius = size * 0.12f
        addRoundRect(
            bounds,
            floatArrayOf(
                largeRadius,
                largeRadius,
                largeRadius,
                largeRadius,
                smallRadius,
                smallRadius,
                largeRadius,
                largeRadius,
            ),
            Path.Direction.CW,
        )
    }

    private fun leafPath(bounds: RectF) = Path().apply {
        val size = minOf(bounds.width(), bounds.height())
        val largeRadius = size * 0.5f
        val smallRadius = size * 0.1f
        addRoundRect(
            bounds,
            floatArrayOf(
                smallRadius,
                smallRadius,
                largeRadius,
                largeRadius,
                smallRadius,
                smallRadius,
                largeRadius,
                largeRadius,
            ),
            Path.Direction.CW,
        )
    }

    private fun archPath(bounds: RectF) = Path().apply {
        val size = minOf(bounds.width(), bounds.height())
        val topRadius = size * 0.5f
        val bottomRadius = size * 0.14f
        addRoundRect(
            bounds,
            floatArrayOf(
                topRadius,
                topRadius,
                topRadius,
                topRadius,
                bottomRadius,
                bottomRadius,
                bottomRadius,
                bottomRadius,
            ),
            Path.Direction.CW,
        )
    }

    private fun hexagonPath(bounds: RectF): Path {
        val centerX = bounds.centerX()
        val centerY = bounds.centerY()
        val radius = minOf(bounds.width(), bounds.height()) / 2f
        val vertices = List(6) { index ->
            val angle = Math.toRadians((index * 60).toDouble())
            (centerX + radius * cos(angle).toFloat()) to (centerY + radius * sin(angle).toFloat())
        }
        return roundedPolygonPath(vertices = vertices, cornerFraction = 0.16f)
    }

    private fun octagonPath(bounds: RectF): Path {
        val centerX = bounds.centerX()
        val centerY = bounds.centerY()
        val radius = (minOf(bounds.width(), bounds.height()) / 2f) / cos(Math.toRadians(22.5)).toFloat()
        val vertices = List(8) { index ->
            val angle = Math.toRadians(22.5 + index * 45.0)
            (centerX + radius * cos(angle).toFloat()) to (centerY + radius * sin(angle).toFloat())
        }
        return roundedPolygonPath(vertices = vertices, cornerFraction = 0.18f)
    }

    private fun diamondPath(bounds: RectF): Path {
        val vertices = listOf(
            bounds.centerX() to bounds.top,
            bounds.right to bounds.centerY(),
            bounds.centerX() to bounds.bottom,
            bounds.left to bounds.centerY(),
        )
        return roundedPolygonPath(vertices = vertices, cornerFraction = 0.18f)
    }

    private fun pentagonPath(bounds: RectF): Path {
        val size = minOf(bounds.width(), bounds.height())
        val centerX = bounds.centerX()
        val centerY = bounds.centerY() + size * 0.045f
        val radius = size * 0.51f
        val vertices = List(5) { vertex ->
            val angle = Math.toRadians((vertex * 72 - 90).toDouble())
            (centerX + radius * cos(angle).toFloat()) to (centerY + radius * sin(angle).toFloat())
        }
        return roundedPolygonPath(vertices = vertices, cornerFraction = 0.16f)
    }

    private fun cloverPath(bounds: RectF) = Path().apply {
        val size = minOf(bounds.width(), bounds.height())
        val centerX = bounds.centerX()
        val centerY = bounds.centerY()
        val offset = size * 0.2f
        val lobeRadius = size * 0.3f
        addCircle(centerX - offset, centerY - offset, lobeRadius, Path.Direction.CW)
        addCircle(centerX + offset, centerY - offset, lobeRadius, Path.Direction.CW)
        addCircle(centerX + offset, centerY + offset, lobeRadius, Path.Direction.CW)
        addCircle(centerX - offset, centerY + offset, lobeRadius, Path.Direction.CW)
        addCircle(centerX, centerY, size * 0.28f, Path.Direction.CW)
    }

    private fun cookiePath(bounds: RectF): Path {
        val size = minOf(bounds.width(), bounds.height())
        val base = Path().apply {
            addOval(bounds, Path.Direction.CW)
        }
        val bite = Path().apply {
            addCircle(
                bounds.right - size * 0.14f,
                bounds.top + size * 0.14f,
                size * 0.2f,
                Path.Direction.CW,
            )
            addCircle(
                bounds.right - size * 0.04f,
                bounds.top + size * 0.26f,
                size * 0.12f,
                Path.Direction.CW,
            )
        }
        base.op(bite, Path.Op.DIFFERENCE)
        return base
    }

    private fun flowerPath(bounds: RectF) = Path().apply {
        val size = minOf(bounds.width(), bounds.height())
        val centerX = bounds.centerX()
        val centerY = bounds.centerY()
        val radius = size * 0.28f
        val orbitRadius = size * 0.22f
        for (petal in 0 until 5) {
            val angle = Math.toRadians((petal * 72 - 90).toDouble())
            val x = centerX + orbitRadius * cos(angle).toFloat()
            val y = centerY + orbitRadius * sin(angle).toFloat()
            addCircle(x, y, radius, Path.Direction.CW)
        }
        addCircle(centerX, centerY, size * 0.26f, Path.Direction.CW)
    }

    private fun scallopPath(bounds: RectF) = Path().apply {
        val size = minOf(bounds.width(), bounds.height())
        val centerX = bounds.centerX()
        val centerY = bounds.centerY()
        val baseRadius = size * 0.455f
        val waveAmplitude = size * 0.045f
        val lobes = 10
        val steps = 120
        for (step in 0 until steps) {
            val angle = (2.0 * PI * step) / steps
            val radius = baseRadius + waveAmplitude * cos(lobes * angle).toFloat()
            val x = centerX + radius * cos(angle).toFloat()
            val y = centerY + radius * sin(angle).toFloat()
            if (step == 0) {
                moveTo(x, y)
            } else {
                lineTo(x, y)
            }
        }
        close()
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

    private fun heartPath(bounds: RectF) = Path().apply {
        val centerX = bounds.centerX()
        val width = bounds.width()
        val height = bounds.height()
        moveTo(centerX, bounds.top + height * 0.22f)
        cubicTo(
            centerX + width * 0.08f,
            bounds.top,
            bounds.right,
            bounds.top,
            bounds.right,
            bounds.top + height * 0.34f,
        )
        cubicTo(
            bounds.right,
            bounds.top + height * 0.62f,
            centerX + width * 0.22f,
            bounds.bottom - height * 0.18f,
            centerX,
            bounds.bottom - height * 0.03f,
        )
        cubicTo(
            centerX - width * 0.22f,
            bounds.bottom - height * 0.18f,
            bounds.left,
            bounds.top + height * 0.62f,
            bounds.left,
            bounds.top + height * 0.34f,
        )
        cubicTo(
            bounds.left,
            bounds.top,
            centerX - width * 0.08f,
            bounds.top,
            centerX,
            bounds.top + height * 0.22f,
        )
        close()
    }

    private fun starPath(bounds: RectF): Path {
        val size = minOf(bounds.width(), bounds.height())
        val centerX = bounds.centerX()
        val centerY = bounds.centerY() + size * 0.035f
        val outerRadius = size * 0.51f
        val innerRadius = size * 0.33f
        val vertices = List(10) { index ->
            val angle = Math.toRadians((index * 36 - 90).toDouble())
            val radius = if (index % 2 == 0) outerRadius else innerRadius
            (centerX + radius * cos(angle).toFloat()) to (centerY + radius * sin(angle).toFloat())
        }
        return roundedPolygonPath(vertices = vertices, cornerFraction = 0.28f)
    }

    private fun cloudPath(bounds: RectF) = Path().apply {
        val size = minOf(bounds.width(), bounds.height())
        val centerX = bounds.centerX()
        val centerY = bounds.centerY()
        val squareLeft = centerX - size / 2f
        val squareRight = centerX + size / 2f
        val squareTop = centerY - size / 2f
        val squareBottom = centerY + size / 2f
        addCircle(centerX - size * 0.04f, squareTop + size * 0.31f, size * 0.26f, Path.Direction.CW)
        addCircle(centerX + size * 0.19f, squareTop + size * 0.37f, size * 0.21f, Path.Direction.CW)
        addCircle(squareRight - size * 0.22f, centerY + size * 0.1f, size * 0.22f, Path.Direction.CW)
        addCircle(centerX + size * 0.14f, squareBottom - size * 0.24f, size * 0.22f, Path.Direction.CW)
        addCircle(centerX - size * 0.14f, squareBottom - size * 0.24f, size * 0.22f, Path.Direction.CW)
        addCircle(squareLeft + size * 0.23f, centerY + size * 0.08f, size * 0.23f, Path.Direction.CW)
        addCircle(squareLeft + size * 0.26f, squareTop + size * 0.4f, size * 0.2f, Path.Direction.CW)
        addCircle(centerX, centerY + size * 0.04f, size * 0.3f, Path.Direction.CW)
    }

    private fun catPath(bounds: RectF) = Path().apply {
        val width = bounds.width()
        val height = bounds.height()
        val centerX = bounds.centerX()
        val centerY = bounds.centerY()

        // Left ear
        moveTo(bounds.left + width * 0.04f, centerY + height * 0.04f)
        lineTo(bounds.left + width * 0.12f, bounds.top + height * 0.11f)
        quadTo(
            bounds.left + width * 0.15f,
            bounds.top + height * 0.01f,
            bounds.left + width * 0.24f,
            bounds.top + height * 0.07f,
        )
        lineTo(centerX - width * 0.06f, bounds.top + height * 0.22f)
        close()

        // Right ear
        moveTo(centerX + width * 0.06f, bounds.top + height * 0.22f)
        lineTo(bounds.right - width * 0.24f, bounds.top + height * 0.07f)
        quadTo(
            bounds.right - width * 0.15f,
            bounds.top + height * 0.01f,
            bounds.right - width * 0.12f,
            bounds.top + height * 0.11f,
        )
        lineTo(bounds.right - width * 0.04f, centerY + height * 0.04f)
        close()

        // Plump head
        addOval(
            RectF(
                bounds.left + width * 0.02f,
                bounds.top + height * 0.16f,
                bounds.right - width * 0.02f,
                bounds.bottom,
            ),
            Path.Direction.CW,
        )
    }

    private fun ghostPath(bounds: RectF) = Path().apply {
        val width = bounds.width()
        val height = bounds.height()
        val left = bounds.left + width * 0.04f
        val right = bounds.right - width * 0.04f
        val bodyWidth = right - left
        val domeRect = RectF(left, bounds.top, right, bounds.top + height * 0.92f)
        val waveBaseY = bounds.bottom - height * 0.07f
        val wavePeakY = bounds.bottom - height * 0.18f
        val waveFootY = bounds.bottom
        val scallopWidth = bodyWidth / 3f

        moveTo(left, bounds.top + height * 0.46f)
        arcTo(domeRect, 180f, 180f, false)
        lineTo(right, waveBaseY)

        for (i in 0 until 3) {
            val segRight = right - i * scallopWidth
            val segMid = segRight - scallopWidth * 0.5f
            val segLeft = segRight - scallopWidth
            quadTo(segRight - scallopWidth * 0.25f, waveFootY, segMid, wavePeakY)
            quadTo(segLeft + scallopWidth * 0.25f, waveFootY, segLeft, waveBaseY)
        }

        close()
    }

    private fun pacManPath(bounds: RectF) = Path().apply {
        moveTo(bounds.centerX(), bounds.centerY())
        arcTo(bounds, 32f, 296f, false)
        close()
    }

    private fun roundedPolygonPath(
        vertices: List<Pair<Float, Float>>,
        cornerFraction: Float,
    ) = Path().apply {
        val count = vertices.size
        for (i in 0 until count) {
            val (prevX, prevY) = vertices[(i - 1 + count) % count]
            val (currX, currY) = vertices[i]
            val (nextX, nextY) = vertices[(i + 1) % count]

            val startX = currX + (prevX - currX) * cornerFraction
            val startY = currY + (prevY - currY) * cornerFraction
            val endX = currX + (nextX - currX) * cornerFraction
            val endY = currY + (nextY - currY) * cornerFraction

            if (i == 0) {
                moveTo(startX, startY)
            } else {
                lineTo(startX, startY)
            }
            quadTo(currX, currY, endX, endY)
        }
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
