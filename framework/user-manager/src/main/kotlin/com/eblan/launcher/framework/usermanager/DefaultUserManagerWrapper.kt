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

import android.content.Context
import android.content.Context.USER_SERVICE
import android.os.Build
import android.os.UserHandle
import android.os.UserManager
import androidx.annotation.RequiresApi
import com.eblan.launcher.domain.common.Dispatcher
import com.eblan.launcher.domain.common.EblanDispatchers
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class DefaultUserManagerWrapper @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:Dispatcher(EblanDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : AndroidUserManagerWrapper {
    private val userManager = context.getSystemService(USER_SERVICE) as UserManager

    override fun getSerialNumberForUser(userHandle: UserHandle): Long = userManager.getSerialNumberForUser(userHandle)

    override fun getUserForSerialNumber(serialNumber: Long): UserHandle? = userManager.getUserForSerialNumber(serialNumber)

    override fun isUserRunning(userHandle: UserHandle): Boolean = userManager.isUserRunning(userHandle)

    override fun isUserUnlocked(userHandle: UserHandle): Boolean = userManager.isUserUnlocked(userHandle)

    override fun isQuietModeEnabled(userHandle: UserHandle): Boolean = userManager.isQuietModeEnabled(userHandle)

    @RequiresApi(Build.VERSION_CODES.P)
    override suspend fun requestQuietModeEnabled(
        enableQuiteMode: Boolean,
        userHandle: UserHandle,
    ): Boolean = withContext(ioDispatcher) {
        userManager.requestQuietModeEnabled(
            enableQuiteMode,
            userHandle,
        )
    }
}
