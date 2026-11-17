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
package com.eblan.launcher.activity.settings

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.eblan.launcher.activity.main.MainActivity
import com.eblan.launcher.designsystem.theme.getDefaultColorScheme
import com.eblan.launcher.domain.model.userdata.ApplicationTheme
import com.eblan.launcher.domain.model.userdata.GeneralSettings
import com.eblan.launcher.navigation.SettingsNavHost
import com.eblan.launcher.platform.accessibilitymanager.AndroidAccessibilityManagerWrapper
import com.eblan.launcher.platform.colorscheme.MaterialColorSchemeResolver
import com.eblan.launcher.platform.locale.LocaleWrapper
import com.eblan.launcher.platform.packagemanager.AndroidPackageManagerWrapper
import com.eblan.launcher.platform.rolemanager.AndroidRoleManagerWrapper
import com.eblan.launcher.platform.settings.AndroidSettingsWrapper
import com.eblan.launcher.platform.wallpapermanager.AndroidWallpaperManagerWrapper
import com.eblan.launcher.ui.local.LocalAccessibilityManager
import com.eblan.launcher.ui.local.LocalLocaleWrapper
import com.eblan.launcher.ui.local.LocalMaterialColorSchemeResolver
import com.eblan.launcher.ui.local.LocalPackageManager
import com.eblan.launcher.ui.local.LocalRoleManager
import com.eblan.launcher.ui.local.LocalSettings
import com.eblan.launcher.ui.local.LocalWallpaperManager
import com.eblan.launcher.util.handleEdgeToEdge
import com.eblan.launcher.util.produceApplicationThemeState
import com.eblan.launcher.util.produceColorSchemeState
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {
    private val viewModel: SettingsActivityViewModel by viewModels()

    @Inject
    lateinit var androidPackageManagerWrapper: AndroidPackageManagerWrapper

    @Inject
    lateinit var androidRoleManagerWrapper: AndroidRoleManagerWrapper

    @Inject
    lateinit var androidSettingsWrapper: AndroidSettingsWrapper

    @Inject
    lateinit var androidAccessibilityManagerWrapper: AndroidAccessibilityManagerWrapper

    @Inject
    lateinit var localeWrapper: LocaleWrapper

    @Inject
    lateinit var materialColorSchemeResolver: MaterialColorSchemeResolver

    @Inject
    lateinit var androidWallpaperManagerWrapper: AndroidWallpaperManagerWrapper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CompositionLocalProvider(
                LocalPackageManager provides androidPackageManagerWrapper,
                LocalRoleManager provides androidRoleManagerWrapper,
                LocalSettings provides androidSettingsWrapper,
                LocalAccessibilityManager provides androidAccessibilityManagerWrapper,
                LocalLocaleWrapper provides localeWrapper,
                LocalMaterialColorSchemeResolver provides materialColorSchemeResolver,
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

                val navController = rememberNavController()

                SideEffect {
                    handleEdgeToEdge(
                        theme = applicationTheme.systemBarsTheme,
                        materialColorSchemeResolver = colorSchemeResolver,
                    )
                }

                MaterialTheme(colorScheme = colorScheme) {
                    Surface {
                        SettingsNavHost(
                            navController = navController,
                            onFinish = {
                                startActivity(Intent(this, MainActivity::class.java))

                                finish()
                            },
                        )
                    }
                }
            }
        }
    }
}
