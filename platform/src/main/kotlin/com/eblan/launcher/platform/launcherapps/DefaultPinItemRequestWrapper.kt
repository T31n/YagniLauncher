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
package com.eblan.launcher.platform.launcherapps

import android.content.pm.LauncherApps
import javax.inject.Inject

internal class DefaultPinItemRequestWrapper @Inject constructor() : PinItemRequestWrapper {
    private var pinItemRequest: LauncherApps.PinItemRequest? = null

    override fun updatePinItemRequest(pinItemRequest: LauncherApps.PinItemRequest?) {
        this.pinItemRequest = pinItemRequest
    }

    override fun getPinItemRequest(): LauncherApps.PinItemRequest? = pinItemRequest
}
