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
package com.eblan.launcher.framework.statusbarmanager

import android.annotation.SuppressLint
import android.app.StatusBarManager
import android.content.Context
import androidx.annotation.RequiresApi
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal class DefaultStatusBarManagerWrapper @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : AndroidStatusBarManagerWrapper {
    @RequiresApi(29)
    val statusBarManager = context.getSystemService("statusbar") as StatusBarManager

    @SuppressLint("PrivateApi")
    @RequiresApi(29)
    override fun expandNotificationsPanel(): Any? = try {
        statusBarManager.javaClass
            .getDeclaredMethod("expandNotificationsPanel")
            .invoke(statusBarManager)
    } catch (e: ReflectiveOperationException) {
        e.printStackTrace()
    } catch (e: SecurityException) {
        e.printStackTrace()
    }
}
