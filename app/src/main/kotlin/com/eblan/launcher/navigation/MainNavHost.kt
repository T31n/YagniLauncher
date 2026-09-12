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
package com.eblan.launcher.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.eblan.launcher.feature.editapplicationinfo.navigation.editApplicationScreen
import com.eblan.launcher.feature.editapplicationinfo.navigation.navigateToEditApplicationScreen
import com.eblan.launcher.feature.editfolderapplicationinfo.navigation.editFolderApplicationScreen
import com.eblan.launcher.feature.editfolderapplicationinfo.navigation.navigateToEditFolderApplicationScreen
import com.eblan.launcher.feature.editgriditem.navigation.editGridItemScreen
import com.eblan.launcher.feature.editgriditem.navigation.navigateToEditGridItemScreen
import com.eblan.launcher.feature.home.navigation.HomeRouteData
import com.eblan.launcher.feature.home.navigation.homeScreen

@Composable
fun MainNavHost(
    configureResultCode: Int?,
    navController: NavHostController,
    onResetConfigureResultCode: () -> Unit,
    onSettings: () -> Unit,
) {
    NavHost(
        navController = navController,
        startDestination = HomeRouteData::class,
    ) {
        homeScreen(
            configureResultCode = configureResultCode,
            onEditApplicationInfo = navController::navigateToEditApplicationScreen,
            onEditFolderApplicationInfo = navController::navigateToEditFolderApplicationScreen,
            onEditGridItem = navController::navigateToEditGridItemScreen,
            onResetConfigureResultCode = onResetConfigureResultCode,
            onSettings = onSettings,
        )

        editGridItemScreen(onNavigateUp = navController::navigateUp)

        editApplicationScreen(onNavigateUp = navController::navigateUp)

        editFolderApplicationScreen(onNavigateUp = navController::navigateUp)
    }
}
