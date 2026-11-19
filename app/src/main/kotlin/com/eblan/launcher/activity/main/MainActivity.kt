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
package com.eblan.launcher.activity.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.eblan.launcher.activity.settings.SettingsActivity
import com.eblan.launcher.domain.platform.FileManager
import com.eblan.launcher.domain.platform.IconKeyGenerator
import com.eblan.launcher.model.ApplicationTheme
import com.eblan.launcher.navigation.MainNavHost
import com.eblan.launcher.platform.accessibilitymanager.AndroidAccessibilityManagerWrapper
import com.eblan.launcher.platform.colorscheme.MaterialColorSchemeResolver
import com.eblan.launcher.platform.iconpackmanager.AndroidIconPackManager
import com.eblan.launcher.platform.imageserializer.AndroidImageSerializer
import com.eblan.launcher.platform.launcherapps.AndroidLauncherAppsWrapper
import com.eblan.launcher.platform.launcherapps.PinItemRequestWrapper
import com.eblan.launcher.platform.packagemanager.AndroidPackageManagerWrapper
import com.eblan.launcher.platform.rolemanager.AndroidRoleManagerWrapper
import com.eblan.launcher.platform.settings.AndroidSettingsWrapper
import com.eblan.launcher.platform.statusbarmanager.AndroidStatusBarManagerWrapper
import com.eblan.launcher.platform.usermanager.AndroidUserManagerWrapper
import com.eblan.launcher.platform.wallpapermanager.AndroidWallpaperManagerWrapper
import com.eblan.launcher.platform.widgetmanager.AndroidAppWidgetHostWrapper
import com.eblan.launcher.platform.widgetmanager.AndroidAppWidgetManagerWrapper
import com.eblan.launcher.ui.local.LocalAccessibilityManager
import com.eblan.launcher.ui.local.LocalAppWidgetHost
import com.eblan.launcher.ui.local.LocalAppWidgetManager
import com.eblan.launcher.ui.local.LocalFileManager
import com.eblan.launcher.ui.local.LocalIconKeyGenerator
import com.eblan.launcher.ui.local.LocalIconPackManager
import com.eblan.launcher.ui.local.LocalImageSerializer
import com.eblan.launcher.ui.local.LocalLauncherApps
import com.eblan.launcher.ui.local.LocalMaterialColorSchemeResolver
import com.eblan.launcher.ui.local.LocalPackageManager
import com.eblan.launcher.ui.local.LocalPinItemRequest
import com.eblan.launcher.ui.local.LocalRoleManager
import com.eblan.launcher.ui.local.LocalSettings
import com.eblan.launcher.ui.local.LocalStatusBarManager
import com.eblan.launcher.ui.local.LocalUserManager
import com.eblan.launcher.ui.local.LocalWallpaperManager
import com.eblan.launcher.util.handleEdgeToEdge
import com.eblan.launcher.util.produceApplicationThemeState
import com.eblan.launcher.util.produceColorSchemeState
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var androidAppWidgetHostWrapper: AndroidAppWidgetHostWrapper

    @Inject
    lateinit var androidAppWidgetManagerWrapper: AndroidAppWidgetManagerWrapper

    @Inject
    lateinit var androidLauncherAppsWrapper: AndroidLauncherAppsWrapper

    @Inject
    lateinit var pinItemRequestWrapper: PinItemRequestWrapper

    @Inject
    lateinit var androidWallpaperManagerWrapper: AndroidWallpaperManagerWrapper

    @Inject
    lateinit var androidPackageManagerWrapper: AndroidPackageManagerWrapper

    @Inject
    lateinit var androidRoleManagerWrapper: AndroidRoleManagerWrapper

    @Inject
    lateinit var imageSerializer: AndroidImageSerializer

    @Inject
    lateinit var materialColorSchemeResolver: MaterialColorSchemeResolver

    @Inject
    lateinit var androidUserManagerWrapper: AndroidUserManagerWrapper

    @Inject
    lateinit var androidSettingsWrapper: AndroidSettingsWrapper

    @Inject
    lateinit var androidIconPackManager: AndroidIconPackManager

    @Inject
    lateinit var fileManager: FileManager

    @Inject
    lateinit var androidAccessibilityManagerWrapper: AndroidAccessibilityManagerWrapper

    @Inject
    lateinit var iconKeyGenerator: IconKeyGenerator

    @Inject
    lateinit var androidStatusBarManagerWrapper: AndroidStatusBarManagerWrapper

    private val viewModel: MainActivityViewModel by viewModels()

    private var configureResultCode by mutableStateOf<Int?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CompositionLocalProvider(
                LocalAppWidgetHost provides androidAppWidgetHostWrapper,
                LocalAppWidgetManager provides androidAppWidgetManagerWrapper,
                LocalLauncherApps provides androidLauncherAppsWrapper,
                LocalPinItemRequest provides pinItemRequestWrapper,
                LocalWallpaperManager provides androidWallpaperManagerWrapper,
                LocalPackageManager provides androidPackageManagerWrapper,
                LocalRoleManager provides androidRoleManagerWrapper,
                LocalImageSerializer provides imageSerializer,
                LocalMaterialColorSchemeResolver provides materialColorSchemeResolver,
                LocalUserManager provides androidUserManagerWrapper,
                LocalSettings provides androidSettingsWrapper,
                LocalIconPackManager provides androidIconPackManager,
                LocalFileManager provides fileManager,
                LocalAccessibilityManager provides androidAccessibilityManagerWrapper,
                LocalIconKeyGenerator provides iconKeyGenerator,
                LocalStatusBarManager provides androidStatusBarManagerWrapper,
            ) {
                val generalSettings by viewModel.generalSettingsFlow.collectAsStateWithLifecycle(
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
                )
            }
        }
    }

    @Suppress("DEPRECATION")
    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AndroidAppWidgetHostWrapper.CONFIGURE_REQUEST_CODE) {
            configureResultCode = resultCode
        }
    }

    @Composable
    private fun MainContent(
        applicationTheme: ApplicationTheme?,
        colorScheme: ColorScheme?,
    ) {
        when {
            applicationTheme == null || colorScheme == null -> enableEdgeToEdge()

            else -> {
                val colorSchemeResolver = LocalMaterialColorSchemeResolver.current
                val navController = rememberNavController()

                SideEffect {
                    handleEdgeToEdge(
                        theme = applicationTheme.systemBarsTheme,
                        materialColorSchemeResolver = colorSchemeResolver,
                    )
                }

                MaterialTheme(colorScheme = colorScheme) {
                    MainNavHost(
                        configureResultCode = configureResultCode,
                        navController = navController,
                        onResetConfigureResultCode = {
                            configureResultCode = null
                        },
                        onSettings = {
                            startActivity(Intent(this, SettingsActivity::class.java))
                        },
                    )
                }
            }
        }
    }
}
