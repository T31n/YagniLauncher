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
package com.eblan.launcher.feature.home.contract

import androidx.compose.runtime.Stable

@Stable
internal interface ApplicationScreenContract :
    AppDrawerContract,
    AppDrawerFolderContract,
    OverlayContract

@Stable
internal interface FolderApplicationScreenContract :
    AppDrawerContract,
    AppDrawerFolderContract,
    GridResetContract,
    OverlayContract

@Stable
internal interface FolderScreenContract :
    GridFolderContract,
    GridMoveResultContract,
    GridResetContract,
    OverlayContract

@Stable
internal interface WidgetScreenContract :
    WidgetCatalogContract,
    OverlayContract

@Stable
internal interface AppWidgetScreenContract :
    AppWidgetCatalogContract,
    OverlayContract

@Stable
internal interface ShortcutConfigScreenContract :
    ShortcutConfigCatalogContract,
    OverlayContract

@Stable
internal interface ResizeScreenContract :
    GridResizeContract,
    GridResetContract

@Stable
internal interface PagerScreenContract :
    LauncherSyncContract,
    OverlayContract,
    GridResetContract,
    GridMoveResultContract,
    GridMoveContract,
    GridResizeContract,
    GridFolderContract,
    AppDrawerContract,
    AppDrawerFolderContract,
    WidgetCatalogContract,
    AppWidgetCatalogContract,
    ShortcutConfigCatalogContract,
    ShortcutInfoCatalogContract,
    EditPageContract,
    ApplicationScreenContract,
    FolderApplicationScreenContract,
    FolderScreenContract,
    WidgetScreenContract,
    AppWidgetScreenContract,
    ShortcutConfigScreenContract,
    ResizeScreenContract
