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
package com.eblan.launcher.data.datastore

import androidx.datastore.core.DataStore
import com.eblan.launcher.data.datastore.proto.UserDataProto
import com.eblan.launcher.data.datastore.proto.copy
import com.eblan.launcher.domain.model.userdata.AppDrawerSettings
import com.eblan.launcher.domain.model.userdata.ExperimentalSettings
import com.eblan.launcher.domain.model.userdata.GeneralSettings
import com.eblan.launcher.domain.model.userdata.GestureSettings
import com.eblan.launcher.domain.model.userdata.HomeSettings
import com.eblan.launcher.domain.model.userdata.UserData
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserDataStore @Inject constructor(private val dataStore: DataStore<UserDataProto>) {
    val userDataFlow = dataStore.data.map {
        UserData(
            homeSettings = it.homeSettingsProto.toHomeSettings(),
            appDrawerSettings = it.appDrawerSettingsProto.toAppDrawerSettings(),
            gestureSettings = it.gestureSettingsProto.toGestureSettings(),
            generalSettings = it.generalSettingsProto.toGeneralSettings(),
            experimentalSettings = it.experimentalSettingsProto.toExperimentalSettings(),
        )
    }

    suspend fun updateGeneralSettings(generalSettings: GeneralSettings) {
        dataStore.updateData {
            it.copy {
                generalSettingsProto = generalSettings.toGeneralSettingsProto()
            }
        }
    }

    suspend fun updateHomeSettings(homeSettings: HomeSettings) {
        dataStore.updateData {
            it.copy {
                homeSettingsProto = homeSettings.toHomeSettingsProto()
            }
        }
    }

    suspend fun updateAppDrawerSettings(appDrawerSettings: AppDrawerSettings) {
        dataStore.updateData {
            it.copy {
                appDrawerSettingsProto = appDrawerSettings.toAppDrawerSettingsProto()
            }
        }
    }

    suspend fun updateGestureSettings(gestureSettings: GestureSettings) {
        dataStore.updateData {
            it.copy {
                gestureSettingsProto = gestureSettings.toGestureSettingsProto()
            }
        }
    }

    suspend fun updateExperimentalSettings(experimentalSettings: ExperimentalSettings) {
        dataStore.updateData {
            it.copy {
                experimentalSettingsProto = experimentalSettings.toExperimentalSettingsProto()
            }
        }
    }
}
