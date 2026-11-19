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
package com.eblan.launcher.platform.locale

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.eblan.launcher.domain.common.Dispatcher
import com.eblan.launcher.domain.common.EblanDispatchers
import com.eblan.launcher.platform.R
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import javax.inject.Inject

internal class DefaultLocaleWrapper @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:Dispatcher(EblanDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : LocaleWrapper {
    override fun getApplicationLanguageTag(): String = AppCompatDelegate.getApplicationLocales().toLanguageTags()

    override fun setApplicationLanguageTag(languageTag: String) {
        val applicationLocales = if (languageTag.isEmpty()) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(languageTag)
        }
        AppCompatDelegate.setApplicationLocales(applicationLocales)
    }

    override suspend fun getSupportedLanguageTags(): List<String> {
        return withContext(ioDispatcher) {
            buildList {
                context.resources.getXml(R.xml.locales_config).use { xmlResourceParser ->
                    while (xmlResourceParser.next() != XmlPullParser.END_DOCUMENT) {
                        if (xmlResourceParser.eventType == XmlPullParser.START_TAG && xmlResourceParser.name == "locale") {
                            add(
                                xmlResourceParser.getAttributeValue(
                                    "http://schemas.android.com/apk/res/android",
                                    "name",
                                ),
                            )
                        }
                    }
                }
            }
        }
    }
}
