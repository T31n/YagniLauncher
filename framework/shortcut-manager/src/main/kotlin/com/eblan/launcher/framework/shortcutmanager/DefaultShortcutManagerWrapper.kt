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
package com.eblan.launcher.framework.shortcutmanager

import android.content.Context
import android.content.pm.ShortcutManager
import android.os.Build
import com.eblan.launcher.domain.framework.ShortcutManagerWrapper
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal class DefaultShortcutManagerWrapper @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : ShortcutManagerWrapper {
    override fun getMaxShortcutCountPerActivity(): Int {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) {
            return 0
        }

        return checkNotNull(context.getSystemService(ShortcutManager::class.java)) {
            "ShortcutManager system service is unavailable"
        }.maxShortcutCountPerActivity
    }
}
