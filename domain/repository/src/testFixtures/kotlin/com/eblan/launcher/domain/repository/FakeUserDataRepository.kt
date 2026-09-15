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
package com.eblan.launcher.domain.repository

import com.eblan.launcher.domain.model.userdata.AppDrawerSettings
import com.eblan.launcher.domain.model.userdata.ExperimentalSettings
import com.eblan.launcher.domain.model.userdata.GeneralSettings
import com.eblan.launcher.domain.model.userdata.GestureSettings
import com.eblan.launcher.domain.model.userdata.HomeSettings
import com.eblan.launcher.domain.model.userdata.UserData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class FakeUserDataRepository(initialUserData: UserData) : UserDataRepository {
    private val _userDataFlow = MutableStateFlow(initialUserData)

    override val userDataFlow: Flow<UserData>
        get() = _userDataFlow

    override suspend fun updateHomeSettings(
        homeSettings: HomeSettings,
    ) {
        _userDataFlow.update { userData ->
            userData.copy(
                homeSettings = homeSettings,
            )
        }
    }

    override suspend fun updateAppDrawerSettings(
        appDrawerSettings: AppDrawerSettings,
    ) {
        _userDataFlow.update { userData ->
            userData.copy(
                appDrawerSettings = appDrawerSettings,
            )
        }
    }

    override suspend fun updateGeneralSettings(
        generalSettings: GeneralSettings,
    ) {
        _userDataFlow.update { userData ->
            userData.copy(
                generalSettings = generalSettings,
            )
        }
    }

    override suspend fun updateGestureSettings(
        gestureSettings: GestureSettings,
    ) {
        _userDataFlow.update { userData ->
            userData.copy(
                gestureSettings = gestureSettings,
            )
        }
    }

    override suspend fun updateExperimentalSettings(
        experimentalSettings: ExperimentalSettings,
    ) {
        _userDataFlow.update { userData ->
            userData.copy(
                experimentalSettings = experimentalSettings,
            )
        }
    }
}
