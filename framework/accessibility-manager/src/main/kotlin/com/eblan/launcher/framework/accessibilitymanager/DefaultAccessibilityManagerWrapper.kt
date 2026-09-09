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
package com.eblan.launcher.framework.accessibilitymanager

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.view.accessibility.AccessibilityManager
import com.eblan.launcher.domain.common.Dispatcher
import com.eblan.launcher.domain.common.EblanDispatchers
import com.eblan.launcher.service.EblanAccessibilityService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class DefaultAccessibilityManagerWrapper @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:Dispatcher(EblanDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) : AndroidAccessibilityManagerWrapper {
    private val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE)
        as AccessibilityManager

    override suspend fun isAccessibilityServiceEnabled(): Boolean = withContext(ioDispatcher) {
        accessibilityManager.isEnabled && accessibilityManager
            .getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
            .any {
                val resolvedInfo = it.resolveInfo.serviceInfo

                resolvedInfo.packageName == context.packageName &&
                    resolvedInfo.name == EblanAccessibilityService::class.java.name
            }
    }
}
