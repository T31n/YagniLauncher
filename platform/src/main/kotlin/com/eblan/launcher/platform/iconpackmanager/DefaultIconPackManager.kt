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
package com.eblan.launcher.platform.iconpackmanager

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.XmlResourceParser
import android.graphics.drawable.Drawable
import androidx.core.content.res.ResourcesCompat
import com.eblan.launcher.domain.common.Dispatcher
import com.eblan.launcher.domain.common.EblanDispatchers
import com.eblan.launcher.domain.model.iconpackinfo.IconPackComponent
import com.eblan.launcher.domain.model.userdata.ColorSchemeSettings
import com.eblan.launcher.domain.model.userdata.ColorSource
import com.eblan.launcher.domain.model.userdata.IconShape
import com.eblan.launcher.domain.model.userdata.Theme
import com.eblan.launcher.domain.platform.IconPackManager
import com.eblan.launcher.platform.colorscheme.MaterialColorSchemeResolver
import com.eblan.launcher.platform.imageserializer.AndroidImageSerializer
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
    private val materialColorSchemeResolver: MaterialColorSchemeResolver,
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

            autoCloseable.use { currentAutoCloseable ->
                when (currentAutoCloseable) {
                    is XmlResourceParser -> parseXml(currentAutoCloseable)

                    is InputStream -> {
                        val xmlPullParser = XmlPullParserFactory.newInstance().newPullParser()

                        xmlPullParser.setInput(currentAutoCloseable.reader())

                        parseXml(xmlPullParser)
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
        iconTint: Boolean,
        colorSource: ColorSource,
        customSeedColor: Int,
        iconShape: IconShape,
        theme: Theme,
        colorSchemeSettings: ColorSchemeSettings,
    ): String? = withContext(ioDispatcher) {
        val packageContext = try {
            getThemedPackageContext(
                packageName = packageName,
                theme = theme,
            )
        } catch (_: PackageManager.NameNotFoundException) {
            return@withContext null
        }

        val resources = packageContext.resources

        val resId = resources.getIdentifier(drawableName, "drawable", packageName)

        if (resId != 0) {
            val drawable = ResourcesCompat.getDrawable(
                resources,
                resId,
                packageContext.theme,
            ) ?: return@withContext null

            val transformedDrawable = if (iconTint) {
                imageSerializer.getTintedAndShapedDrawable(
                    drawable = drawable,
                    colorSource = colorSource,
                    customSeedColor = customSeedColor,
                    theme = theme,
                    iconShape = iconShape,
                    colorSchemeSettings = colorSchemeSettings,
                )
            } else {
                imageSerializer.getShapedDrawable(
                    drawable = drawable,
                    iconShape = iconShape,
                )
            } ?: return@withContext null

            imageSerializer.createDrawablePath(
                drawable = transformedDrawable,
                file = file,
            )

            file.absolutePath
        } else {
            null
        }
    }

    override suspend fun loadDrawableFromIconPack(
        packageName: String,
        drawableName: String,
        iconTint: Boolean,
        colorSource: ColorSource,
        customSeedColor: Int,
        iconShape: IconShape,
        theme: Theme,
        colorSchemeSettings: ColorSchemeSettings,
    ): Drawable? = withContext(ioDispatcher) {
        val packageContext = try {
            getThemedPackageContext(
                packageName = packageName,
                theme = theme,
            )
        } catch (_: PackageManager.NameNotFoundException) {
            return@withContext null
        }

        val resources = packageContext.resources

        val resId = resources.getIdentifier(drawableName, "drawable", packageName)

        if (resId != 0) {
            val drawable = ResourcesCompat.getDrawable(
                resources,
                resId,
                packageContext.theme,
            ) ?: return@withContext null

            if (iconTint) {
                imageSerializer.getTintedAndShapedDrawable(
                    drawable = drawable,
                    colorSource = colorSource,
                    customSeedColor = customSeedColor,
                    theme = theme,
                    iconShape = iconShape,
                    colorSchemeSettings = colorSchemeSettings,
                )
            } else {
                imageSerializer.getShapedDrawable(
                    drawable = drawable,
                    iconShape = iconShape,
                )
            }
        } else {
            null
        }
    }

    private fun getThemedPackageContext(
        packageName: String,
        theme: Theme,
    ): Context {
        val packageContext = context.createPackageContext(
            packageName,
            Context.CONTEXT_IGNORE_SECURITY,
        )

        val isDarkTheme = materialColorSchemeResolver.isDarkTheme(theme)

        val configuration = Configuration(packageContext.resources.configuration).apply {
            uiMode = (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or
                if (isDarkTheme) {
                    Configuration.UI_MODE_NIGHT_YES
                } else {
                    Configuration.UI_MODE_NIGHT_NO
                }
        }

        return packageContext.createConfigurationContext(configuration)
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
