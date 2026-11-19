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
package com.eblan.launcher.activity.action

import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Canvas
import android.graphics.drawable.Icon
import android.os.Bundle
import android.util.TypedValue
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eblan.launcher.activity.main.MainActivity
import com.eblan.launcher.designsystem.theme.getDefaultColorScheme
import com.eblan.launcher.domain.common.Dispatcher
import com.eblan.launcher.domain.common.EblanDispatchers
import com.eblan.launcher.domain.model.userdata.EblanAction
import com.eblan.launcher.domain.model.userdata.GeneralSettings
import com.eblan.launcher.feature.action.ActionScreen
import com.eblan.launcher.model.ApplicationTheme
import com.eblan.launcher.platform.accessibilitymanager.AndroidAccessibilityManagerWrapper
import com.eblan.launcher.platform.colorscheme.MaterialColorSchemeResolver
import com.eblan.launcher.platform.wallpapermanager.AndroidWallpaperManagerWrapper
import com.eblan.launcher.ui.local.LocalAccessibilityManager
import com.eblan.launcher.ui.local.LocalMaterialColorSchemeResolver
import com.eblan.launcher.ui.local.LocalWallpaperManager
import com.eblan.launcher.ui.settings.getEblanActionTypeSubtitle
import com.eblan.launcher.util.handleEdgeToEdge
import com.eblan.launcher.util.produceApplicationThemeState
import com.eblan.launcher.util.produceColorSchemeState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import kotlin.uuid.Uuid

@AndroidEntryPoint
class ActionActivity : ComponentActivity() {
    private val viewModel: ActionActivityViewModel by viewModels()

    @Inject
    @Dispatcher(EblanDispatchers.Default)
    lateinit var defaultDispatcher: CoroutineDispatcher

    @Inject
    lateinit var androidAccessibilityManagerWrapper: AndroidAccessibilityManagerWrapper

    @Inject
    lateinit var materialColorSchemeResolver: MaterialColorSchemeResolver

    @Inject
    lateinit var androidWallpaperManagerWrapper: AndroidWallpaperManagerWrapper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CompositionLocalProvider(
                LocalAccessibilityManager provides androidAccessibilityManagerWrapper,
                LocalMaterialColorSchemeResolver provides materialColorSchemeResolver,
                LocalWallpaperManager provides androidWallpaperManagerWrapper,
            ) {
                val generalSettings: GeneralSettings? by viewModel.generalSettingsFlow.collectAsStateWithLifecycle(initialValue = null)

                val applicationTheme by produceApplicationThemeState(
                    theme = generalSettings?.theme,
                    systemBarsTheme = generalSettings?.systemBarsTheme,
                    colorSource = generalSettings?.colorSource,
                    customSeedColor = generalSettings?.customSeedColor,
                )

                val colorScheme by produceColorSchemeState(
                    theme = generalSettings?.theme,
                    colorSource = generalSettings?.colorSource,
                    customSeedColor = generalSettings?.customSeedColor,
                    colorSchemeSettings = generalSettings?.colorSchemeSettings,
                )

                MainContent(
                    applicationTheme = applicationTheme,
                    colorScheme = colorScheme,
                )
            }
        }
    }

    @Composable
    private fun MainContent(
        applicationTheme: ApplicationTheme?,
        colorScheme: ColorScheme?,
    ) {
        when {
            applicationTheme == null || colorScheme == null -> {
                SideEffect {
                    enableEdgeToEdge()
                }

                MaterialTheme(colorScheme = getDefaultColorScheme(isSystemInDarkTheme())) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        Box(modifier = Modifier.fillMaxSize())
                    }
                }
            }

            else -> {
                val colorSchemeResolver = LocalMaterialColorSchemeResolver.current

                SideEffect {
                    handleEdgeToEdge(
                        theme = applicationTheme.systemBarsTheme,
                        materialColorSchemeResolver = colorSchemeResolver,
                    )
                }

                MaterialTheme(colorScheme = colorScheme) {
                    Surface {
                        ActionScreen(
                            onFinish = ::finish,
                            onUpdateEblanAction = ::createShortcutResult,
                        )
                    }
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    private suspend fun createShortcutResult(
        resId: Int,
        eblanAction: EblanAction,
    ) {
        withContext(defaultDispatcher) {
            val json = Json.encodeToString(eblanAction)

            val shortcutId = Uuid.random().toHexString()

            val shortcutName =
                eblanAction.eblanActionType.getEblanActionTypeSubtitle(
                    context = this@ActionActivity,
                    componentName = eblanAction.componentName,
                )

            val bitmap = ContextCompat.getDrawable(
                applicationContext,
                resId,
            )?.let { drawable ->
                val typedValue = TypedValue()

                theme.resolveAttribute(
                    android.R.attr.colorPrimary,
                    typedValue,
                    true,
                )

                DrawableCompat.setTint(drawable, typedValue.data)

                createBitmap(
                    width = drawable.intrinsicWidth,
                    height = drawable.intrinsicHeight,
                ).also { bitmap ->
                    val canvas = Canvas(bitmap)

                    drawable.setBounds(
                        0,
                        0,
                        canvas.width,
                        canvas.height,
                    )

                    drawable.draw(canvas)
                }
            }

            val shortcutIntent = Intent(applicationContext, MainActivity::class.java).apply {
                action = EblanAction.ACTION
                putExtra(EblanAction.NAME, json)
            }

            val legacyExtras = Intent().apply {
                putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent)
                putExtra(Intent.EXTRA_SHORTCUT_NAME, shortcutName)
                putExtra(Intent.EXTRA_SHORTCUT_ICON, bitmap)
            }

            val shortcutManager = getSystemService(ShortcutManager::class.java)

            val resultIntent = if (shortcutManager.isRequestPinShortcutSupported) {
                val shortcut = ShortcutInfo.Builder(applicationContext, shortcutId)
                    .setShortLabel(shortcutName).setLongLabel(shortcutName).setIcon(
                        Icon.createWithResource(
                            applicationContext,
                            resId,
                        ),
                    ).setIntent(shortcutIntent).build()

                shortcutManager.createShortcutResultIntent(shortcut)?.apply {
                    putExtras(legacyExtras)
                }
            } else {
                legacyExtras
            }

            setResult(RESULT_OK, resultIntent)

            finish()
        }
    }
}
