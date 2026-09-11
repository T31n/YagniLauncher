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
package com.eblan.launcher.common.impl

import android.util.Base64
import com.eblan.launcher.domain.common.Dispatcher
import com.eblan.launcher.domain.common.EblanDispatchers
import com.eblan.launcher.domain.common.IconKeyGenerator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import javax.inject.Inject

internal class DefaultIconKeyGenerator @Inject constructor(
    @param:Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) : IconKeyGenerator {
    override suspend fun getActivityIconKey(
        serialNumber: Long,
        componentName: String,
    ) = getHashedName("$serialNumber:$componentName")

    override suspend fun getShortcutIconKey(
        serialNumber: Long,
        packageName: String,
        id: String,
    ) = getHashedName("$serialNumber:$packageName:$id")

    override suspend fun getHashedName(name: String): String = withContext(defaultDispatcher) {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(name.toByteArray())

        Base64.encodeToString(
            digest.copyOfRange(0, 8),
            Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP,
        )
    }
}
