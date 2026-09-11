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

import androidx.datastore.core.DataMigration
import com.eblan.launcher.data.datastore.proto.UserDataProto
import com.eblan.launcher.data.datastore.proto.appdrawer.appDrawerSettingsProto
import com.eblan.launcher.data.datastore.proto.copy
import com.eblan.launcher.data.datastore.proto.home.homeSettingsProto

internal class DataStoreMigration : DataMigration<UserDataProto> {
    override suspend fun shouldMigrate(currentData: UserDataProto): Boolean = currentData.homeSettingsProto.dockPageCount == 0 ||
        currentData.appDrawerSettingsProto.horizontalAppDrawerColumns == 0 ||
        currentData.appDrawerSettingsProto.horizontalAppDrawerRows == 0 ||
        currentData.homeSettingsProto.folderCellWidth == 0 ||
        currentData.homeSettingsProto.folderCellHeight == 0 ||
        currentData.homeSettingsProto.maxFolderColumns == 0 ||
        currentData.homeSettingsProto.maxFolderRows == 0

    override suspend fun migrate(currentData: UserDataProto): UserDataProto = currentData.copy {
        homeSettingsProto {
            dockPageCount = 1
            folderCellWidth = 64
            folderCellHeight = 96
            maxFolderColumns = 5
            maxFolderRows = 4
        }

        appDrawerSettingsProto {
            horizontalAppDrawerColumns = 5
            horizontalAppDrawerRows = 5
        }
    }

    override suspend fun cleanUp() {}
}
