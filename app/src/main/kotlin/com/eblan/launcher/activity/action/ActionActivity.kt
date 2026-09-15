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
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.DrawableCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eblan.launcher.activity.main.MainActivity
import com.eblan.launcher.designsystem.theme.EblanLauncherTheme
import com.eblan.launcher.domain.common.Dispatcher
import com.eblan.launcher.domain.common.EblanDispatchers
import com.eblan.launcher.domain.model.userdata.EblanAction
import com.eblan.launcher.domain.model.userdata.Theme
import com.eblan.launcher.feature.action.ActionScreen
import com.eblan.launcher.framework.accessibilitymanager.AndroidAccessibilityManagerWrapper
import com.eblan.launcher.model.ActivityUiState
import com.eblan.launcher.ui.local.LocalAccessibilityManager
import com.eblan.launcher.ui.settings.getEblanActionTypeSubtitle
import com.eblan.launcher.util.handleEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@AndroidEntryPoint
class ActionActivity : ComponentActivity() {
    private val viewModel: ActionActivityViewModel by viewModels()

    @Inject
    @Dispatcher(EblanDispatchers.Default)
    lateinit var defaultDispatcher: CoroutineDispatcher

    @Inject
    lateinit var androidAccessibilityManagerWrapper: AndroidAccessibilityManagerWrapper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CompositionLocalProvider(
                LocalAccessibilityManager provides androidAccessibilityManagerWrapper,
            ) {
                val activityUiState by viewModel.activityUiState.collectAsStateWithLifecycle()

                when (val state = activityUiState) {
                    ActivityUiState.Loading -> {
                        SideEffect {
                            enableEdgeToEdge()
                        }

                        EblanLauncherTheme(
                            theme = Theme.System,
                            dynamicTheme = false,
                        ) {
                            Surface(modifier = Modifier.fillMaxSize()) {
                                Box(modifier = Modifier.fillMaxSize())
                            }
                        }
                    }

                    is ActivityUiState.Success -> {
                        SideEffect {
                            handleEdgeToEdge(theme = state.applicationTheme.theme)
                        }

                        EblanLauncherTheme(
                            theme = state.applicationTheme.theme,
                            dynamicTheme = state.applicationTheme.dynamicTheme,
                        ) {
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
        }
    }

    @Suppress("DEPRECATION")
    @OptIn(ExperimentalUuidApi::class)
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

            val resultIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val shortcutManager = getSystemService(ShortcutManager::class.java)

                if (shortcutManager.isRequestPinShortcutSupported) {
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
            } else {
                legacyExtras
            }

            setResult(RESULT_OK, resultIntent)

            finish()
        }
    }
}
