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
package com.eblan.launcher.activity.pin

import android.content.Intent
import android.content.pm.LauncherApps.PinItemRequest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eblan.launcher.common.AndroidImageSerializer
import com.eblan.launcher.common.MaterialColorSchemeResolver
import com.eblan.launcher.domain.common.FileManager
import com.eblan.launcher.domain.common.IconKeyGenerator
import com.eblan.launcher.domain.model.userdata.ApplicationTheme
import com.eblan.launcher.domain.model.userdata.GeneralSettings
import com.eblan.launcher.feature.pin.PinScreen
import com.eblan.launcher.framework.launcherapps.AndroidLauncherAppsWrapper
import com.eblan.launcher.framework.launcherapps.PinItemRequestWrapper
import com.eblan.launcher.framework.usermanager.AndroidUserManagerWrapper
import com.eblan.launcher.framework.wallpapermanager.AndroidWallpaperManagerWrapper
import com.eblan.launcher.framework.widgetmanager.AndroidAppWidgetHostWrapper
import com.eblan.launcher.framework.widgetmanager.AndroidAppWidgetManagerWrapper
import com.eblan.launcher.ui.local.LocalAppWidgetHost
import com.eblan.launcher.ui.local.LocalAppWidgetManager
import com.eblan.launcher.ui.local.LocalFileManager
import com.eblan.launcher.ui.local.LocalIconKeyGenerator
import com.eblan.launcher.ui.local.LocalImageSerializer
import com.eblan.launcher.ui.local.LocalLauncherApps
import com.eblan.launcher.ui.local.LocalMaterialColorSchemeResolver
import com.eblan.launcher.ui.local.LocalPinItemRequest
import com.eblan.launcher.ui.local.LocalUserManager
import com.eblan.launcher.ui.local.LocalWallpaperManager
import com.eblan.launcher.util.handleEdgeToEdge
import com.eblan.launcher.util.produceApplicationThemeState
import com.eblan.launcher.util.produceColorSchemeState
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PinActivity : ComponentActivity() {

    @Inject
    lateinit var androidAppWidgetHostWrapper: AndroidAppWidgetHostWrapper

    @Inject
    lateinit var androidAppWidgetManagerWrapper: AndroidAppWidgetManagerWrapper

    @Inject
    lateinit var androidLauncherAppsWrapper: AndroidLauncherAppsWrapper

    @Inject
    lateinit var pinItemRequestWrapper: PinItemRequestWrapper

    @Inject
    lateinit var imageSerializer: AndroidImageSerializer

    @Inject
    lateinit var materialColorSchemeResolver: MaterialColorSchemeResolver

    @Inject
    lateinit var androidUserManagerWrapper: AndroidUserManagerWrapper

    @Inject
    lateinit var androidWallpaperManagerWrapper: AndroidWallpaperManagerWrapper

    @Inject
    lateinit var fileManager: FileManager

    @Inject
    lateinit var iconKeyGenerator: IconKeyGenerator

    private val viewModel: PinActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()

        super.onCreate(savedInstanceState)

        val homeIntent = Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_HOME)
            .setPackage(packageName)

        val pinItemRequest = androidLauncherAppsWrapper.getPinItemRequest(intent = intent)

        setContent {
            CompositionLocalProvider(
                LocalAppWidgetHost provides androidAppWidgetHostWrapper,
                LocalAppWidgetManager provides androidAppWidgetManagerWrapper,
                LocalPinItemRequest provides pinItemRequestWrapper,
                LocalLauncherApps provides androidLauncherAppsWrapper,
                LocalImageSerializer provides imageSerializer,
                LocalMaterialColorSchemeResolver provides materialColorSchemeResolver,
                LocalUserManager provides androidUserManagerWrapper,
                LocalFileManager provides fileManager,
                LocalIconKeyGenerator provides iconKeyGenerator,
                LocalWallpaperManager provides androidWallpaperManagerWrapper,
            ) {
                val generalSettings: GeneralSettings? by viewModel.generalSettingsFlow.collectAsStateWithLifecycle(
                    initialValue = null,
                )

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
                    pinItemRequest = pinItemRequest,
                    homeIntent = homeIntent,
                )
            }
        }
    }

    @Composable
    private fun MainContent(
        applicationTheme: ApplicationTheme?,
        colorScheme: ColorScheme?,
        pinItemRequest: PinItemRequest,
        homeIntent: Intent,
    ) {
        when {
            applicationTheme == null || colorScheme == null -> {}

            else -> {
                val colorSchemeResolver = LocalMaterialColorSchemeResolver.current
                SideEffect {
                    handleEdgeToEdge(
                        theme = applicationTheme.systemBarsTheme,
                        materialColorSchemeResolver = colorSchemeResolver,
                    )
                }

                MaterialTheme(colorScheme = colorScheme) {
                    PinScreen(
                        // Data and State
                        pinItemRequest = pinItemRequest,
                        // Callbacks
                        onDragStart = {
                            startActivity(homeIntent)
                            finish()
                        },
                        onFinish = ::finish,
                    )
                }
            }
        }
    }
}
