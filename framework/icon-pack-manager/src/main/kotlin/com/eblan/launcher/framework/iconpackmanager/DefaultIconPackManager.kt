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
package com.eblan.launcher.framework.iconpackmanager

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.XmlResourceParser
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.TypedValue
import com.eblan.launcher.common.AndroidImageSerializer
import com.eblan.launcher.domain.common.Dispatcher
import com.eblan.launcher.domain.common.EblanDispatchers
import com.eblan.launcher.domain.framework.IconPackManager
import com.eblan.launcher.domain.framework.WallpaperManagerWrapper
import com.eblan.launcher.domain.model.iconpackinfo.IconPackComponent
import com.eblan.launcher.domain.model.userdata.IconShape
import com.eblan.launcher.domain.model.userdata.IconTint
import com.eblan.launcher.domain.model.userdata.Theme
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import java.io.InputStream
import javax.inject.Inject

@SuppressLint("DiscouragedApi")
internal class DefaultIconPackManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val imageSerializer: AndroidImageSerializer,
    private val wallpaperManagerWrapper: WallpaperManagerWrapper,
    @param:Dispatcher(EblanDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : IconPackManager,
    AndroidIconPackManager {
    override suspend fun getIconPackComponents(packageName: String): List<IconPackComponent> = withContext(ioDispatcher) {
        try {
            val packageContext = context.createPackageContext(
                packageName,
                Context.CONTEXT_IGNORE_SECURITY,
            )

            val resources = packageContext.resources

            val xmlId = resources.getIdentifier(
                "appfilter",
                "xml",
                packageName,
            )

            val rawId = resources.getIdentifier(
                "appfilter",
                "raw",
                packageName,
            )

            val autoCloseable = when {
                xmlId != 0 -> resources.getXml(xmlId)
                rawId != 0 -> resources.openRawResource(rawId)
                else -> packageContext.assets.open("appfilter.xml")
            }

            autoCloseable.use {
                when (it) {
                    is XmlResourceParser -> parseXml(xmlPullParser = it)

                    is InputStream -> {
                        val xmlPullParser = XmlPullParserFactory.newInstance().newPullParser()

                        xmlPullParser.setInput(it.reader())

                        parseXml(xmlPullParser = xmlPullParser)
                    }

                    else -> emptyList()
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun createIconPackPath(
        packageName: String,
        drawableName: String,
        file: File,
        iconTint: IconTint,
        iconShape: IconShape,
        customIconTint: Int,
        fallbackIconTint: Boolean,
        theme: Theme,
    ): String? = withContext(ioDispatcher) {
        val packageContext =
            context.createPackageContext(packageName, Context.CONTEXT_IGNORE_SECURITY)

        val resources = packageContext.resources

        val resId = resources.getIdentifier(drawableName, "drawable", packageName)

        if (resId != 0) {
            val drawable = resources.getDrawable(
                resId,
                packageContext.theme,
            )

            val transformedDrawable = imageSerializer.getTintedAndShapedDrawable(
                drawable = drawable,
                iconTint = iconTint,
                customIconTint = when (iconTint) {
                    IconTint.System -> getSystemIconTintColor()
                    else -> customIconTint
                },
                fallbackIconTint = fallbackIconTint,
                theme = theme,
                iconShape = iconShape,
            ) ?: return@withContext null

            imageSerializer.createDrawablePath(
                drawable = transformedDrawable,
                file = file,
            )

            file.absolutePath
        } else {
            null
        }
    }

    private fun getSystemAccentColor(): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return context.getColor(android.R.color.system_accent1_600)
        }

        val value = TypedValue()

        return if (context.theme.resolveAttribute(
                android.R.attr.colorAccent,
                value,
                true,
            )
        ) {
            value.data
        } else {
            context.getColor(android.R.color.holo_blue_dark)
        }
    }

    private fun getSystemIconTintColor(): Int = wallpaperManagerWrapper.getSystemWallpaperColor() ?: getSystemAccentColor()

    override suspend fun loadDrawableFromIconPack(
        packageName: String,
        drawableName: String,
    ): Drawable? = withContext(ioDispatcher) {
        val packageContext =
            context.createPackageContext(packageName, Context.CONTEXT_IGNORE_SECURITY)

        val resources = packageContext.resources

        val resId = resources.getIdentifier(drawableName, "drawable", packageName)

        if (resId != 0) {
            resources.getDrawable(
                resId,
                packageContext.theme,
            )
        } else {
            null
        }
    }

    private suspend fun parseXml(xmlPullParser: XmlPullParser): List<IconPackComponent> {
        var eventType = xmlPullParser.eventType

        return buildList {
            while (currentCoroutineContext().isActive && eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && xmlPullParser.name == "item") {
                    val component = xmlPullParser.getAttributeValue(null, "component")

                    val drawable = xmlPullParser.getAttributeValue(null, "drawable")

                    if (component != null && drawable != null) {
                        add(
                            IconPackComponent(
                                componentName = component,
                                drawableName = drawable,
                            ),
                        )
                    }
                }

                eventType = xmlPullParser.next()
            }
        }
    }
}
