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
package com.eblan.launcher.feature.home.component

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.core.util.Consumer

@Composable
internal fun HomeHandler(
    enabled: Boolean = true,
    onHome: (Intent) -> Unit,
) {
    val activity = LocalActivity.current as ComponentActivity

    val currentOnHome by rememberUpdatedState(onHome)

    DisposableEffect(
        key1 = activity,
        key2 = enabled,
    ) {
        val listener = Consumer<Intent> { intent ->
            handleActionMainIntent(
                enabled = enabled,
                intent = intent,
                onHome = currentOnHome,
            )
        }

        activity.addOnNewIntentListener(listener)

        onDispose {
            activity.removeOnNewIntentListener(listener)
        }
    }
}

private fun handleActionMainIntent(
    enabled: Boolean,
    intent: Intent,
    onHome: (Intent) -> Unit,
) {
    if (!enabled) return

    if (intent.action != Intent.ACTION_MAIN && !intent.hasCategory(Intent.CATEGORY_HOME)) {
        return
    }

    if ((intent.flags and Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) != 0) {
        return
    }

    onHome(intent)
}
