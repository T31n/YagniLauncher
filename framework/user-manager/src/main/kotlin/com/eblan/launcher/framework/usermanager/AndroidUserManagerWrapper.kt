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
package com.eblan.launcher.framework.usermanager

import android.os.Build
import android.os.UserHandle
import androidx.annotation.RequiresApi

interface AndroidUserManagerWrapper {
    fun getSerialNumberForUser(userHandle: UserHandle): Long

    fun getUserForSerialNumber(serialNumber: Long): UserHandle?

    fun isUserRunning(userHandle: UserHandle): Boolean

    fun isUserUnlocked(userHandle: UserHandle): Boolean

    fun isQuietModeEnabled(userHandle: UserHandle): Boolean

    @RequiresApi(Build.VERSION_CODES.P)
    suspend fun requestQuietModeEnabled(
        enableQuiteMode: Boolean,
        userHandle: UserHandle,
    ): Boolean
}
