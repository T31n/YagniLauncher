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
package com.eblan.launcher.framework.rolemanager

import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal class DefaultRoleManagerWrapper @Inject constructor(@param:ApplicationContext private val context: Context) : AndroidRoleManagerWrapper {
    private val androidRoleManager = context.getSystemService(RoleManager::class.java) as RoleManager

    override fun isHomeRoleAvailable(): Boolean = androidRoleManager.isRoleAvailable(RoleManager.ROLE_HOME)

    override fun isHomeRoleHeld(): Boolean = isHomeRoleAvailable() &&
        androidRoleManager.isRoleHeld(RoleManager.ROLE_HOME)

    override fun createRequestHomeRoleIntent(): Intent = androidRoleManager.createRequestRoleIntent(RoleManager.ROLE_HOME)
}
