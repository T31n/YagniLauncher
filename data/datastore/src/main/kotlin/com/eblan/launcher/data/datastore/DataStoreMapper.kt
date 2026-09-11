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

import com.eblan.launcher.data.datastore.proto.appdrawer.AppDrawerSettingsProto
import com.eblan.launcher.data.datastore.proto.appdrawer.AppDrawerTypeProto
import com.eblan.launcher.data.datastore.proto.experimental.ExperimentalSettingsProto
import com.eblan.launcher.data.datastore.proto.general.GeneralSettingsProto
import com.eblan.launcher.data.datastore.proto.general.ThemeProto
import com.eblan.launcher.data.datastore.proto.gesture.EblanActionProto
import com.eblan.launcher.data.datastore.proto.gesture.EblanActionTypeProto
import com.eblan.launcher.data.datastore.proto.gesture.GestureSettingsProto
import com.eblan.launcher.data.datastore.proto.home.GridItemSettingsProto
import com.eblan.launcher.data.datastore.proto.home.HomeSettingsProto
import com.eblan.launcher.data.datastore.proto.home.HorizontalAlignmentProto
import com.eblan.launcher.data.datastore.proto.home.TextColorProto
import com.eblan.launcher.data.datastore.proto.home.VerticalArrangementProto
import com.eblan.launcher.data.datastore.proto.model.BackgroundColorProto
import com.eblan.launcher.domain.model.AppDrawerSettings
import com.eblan.launcher.domain.model.AppDrawerType
import com.eblan.launcher.domain.model.BackgroundColor
import com.eblan.launcher.domain.model.EblanAction
import com.eblan.launcher.domain.model.EblanActionType
import com.eblan.launcher.domain.model.ExperimentalSettings
import com.eblan.launcher.domain.model.GeneralSettings
import com.eblan.launcher.domain.model.GestureSettings
import com.eblan.launcher.domain.model.GridItemSettings
import com.eblan.launcher.domain.model.HomeSettings
import com.eblan.launcher.domain.model.HorizontalAlignment
import com.eblan.launcher.domain.model.TextColor
import com.eblan.launcher.domain.model.Theme
import com.eblan.launcher.domain.model.VerticalArrangement

internal fun HomeSettingsProto.toHomeSettings(): HomeSettings = HomeSettings(
    columns = columns,
    rows = rows,
    pageCount = pageCount,
    infiniteScroll = infiniteScroll,
    dockColumns = dockColumns,
    dockRows = dockRows,
    dockHeight = dockHeight,
    initialPage = initialPage,
    wallpaperScroll = wallpaperScroll,
    gridItemSettings = gridItemSettingsProto.toGridItemSettings(),
    lockScreenOrientation = lockScreenOrientation,
    dockPageCount = dockPageCount,
    dockInfiniteScroll = dockInfiniteScroll,
    dockInitialPage = dockInitialPage,
    addNewAppsToHomeScreen = addNewAppsToHomeScreen,
    folderCellWidth = folderCellWidth,
    folderCellHeight = folderCellHeight,
    maxFolderColumns = maxFolderColumns,
    maxFolderRows = maxFolderRows,
    showPageIndicator = if (hasShowPageIndicator()) showPageIndicator else true,
    dockCustomBackgroundColor = dockCustomBackgroundColor,
    dockPadding = dockPadding,
    dockTopStartCornerRadius = dockTopStartCornerRadius,
    dockTopEndCornerRadius = dockTopEndCornerRadius,
    dockBottomStartCornerRadius = dockBottomStartCornerRadius,
    dockBottomEndCornerRadius = dockBottomEndCornerRadius,
    folderCornerRadius = if (hasFolderCornerRadius()) folderCornerRadius else 5,
    folderBackgroundColor = folderBackgroundColorProto.toBackgroundColor(),
    customFolderBackgroundColor = customFolderBackgroundColor,
)

internal fun AppDrawerSettingsProto.toAppDrawerSettings(): AppDrawerSettings = AppDrawerSettings(
    appDrawerColumns = appDrawerColumns,
    appDrawerRowsHeight = appDrawerRowsHeight,
    gridItemSettings = gridItemSettingsProto.toGridItemSettings(),
    backgroundColor = backgroundColorProto.toBackgroundColor(),
    customBackgroundColor = customBackgroundColor,
    appDrawerType = appDrawerTypeProto.toAppDrawerType(),
    horizontalAppDrawerColumns = horizontalAppDrawerColumns,
    horizontalAppDrawerRows = horizontalAppDrawerRows,
    excludeTaggedApps = excludeTaggedApps,
    showKeyboard = showKeyboard,
    fuzzySearch = fuzzySearch,
    blurBehind = blurBehind,
)

internal fun GridItemSettingsProto.toGridItemSettings(): GridItemSettings = GridItemSettings(
    iconSize = iconSize,
    textColor = textColorProto.toTextColor(),
    textSize = textSize,
    showLabel = showLabel,
    singleLineLabel = singleLineLabel,
    horizontalAlignment = horizontalAlignmentProto.toHorizontalAlignment(),
    verticalArrangement = verticalArrangementProto.toVerticalArrangement(),
    customTextColor = customTextColor,
    customBackgroundColor = customBackgroundColor,
    padding = padding,
    cornerRadius = cornerRadius,
)

internal fun GeneralSettingsProto.toGeneralSettings(): GeneralSettings = GeneralSettings(
    theme = themeProto.toTheme(),
    dynamicTheme = dynamicTheme,
    iconPackInfoPackageName = iconPackInfoPackageName,
)

internal fun GridItemSettings.toGridItemSettingsProto(): GridItemSettingsProto = GridItemSettingsProto.newBuilder().also { builder ->
    builder.iconSize = iconSize
    builder.textColorProto = textColor.toTextColorProto()
    builder.textSize = textSize
    builder.showLabel = showLabel
    builder.singleLineLabel = singleLineLabel
    builder.horizontalAlignmentProto = horizontalAlignment.toHorizontalAlignmentProto()
    builder.verticalArrangementProto = verticalArrangement.toVerticalArrangementProto()
    builder.customTextColor = customTextColor
    builder.customBackgroundColor = customBackgroundColor
    builder.padding = padding
    builder.cornerRadius = cornerRadius
}.build()

internal fun HomeSettings.toHomeSettingsProto(): HomeSettingsProto = HomeSettingsProto.newBuilder().also { builder ->
    builder.columns = columns
    builder.rows = rows
    builder.pageCount = pageCount
    builder.infiniteScroll = infiniteScroll
    builder.dockColumns = dockColumns
    builder.dockRows = dockRows
    builder.dockHeight = dockHeight
    builder.initialPage = initialPage
    builder.wallpaperScroll = wallpaperScroll
    builder.gridItemSettingsProto = gridItemSettings.toGridItemSettingsProto()
    builder.lockScreenOrientation = lockScreenOrientation
    builder.dockPageCount = dockPageCount
    builder.dockInfiniteScroll = dockInfiniteScroll
    builder.dockInitialPage = dockInitialPage
    builder.addNewAppsToHomeScreen = addNewAppsToHomeScreen
    builder.folderCellWidth = folderCellWidth
    builder.folderCellHeight = folderCellHeight
    builder.maxFolderColumns = maxFolderColumns
    builder.maxFolderRows = maxFolderRows
    builder.showPageIndicator = showPageIndicator
    builder.dockCustomBackgroundColor = dockCustomBackgroundColor
    builder.dockPadding = dockPadding
    builder.dockTopStartCornerRadius = dockTopStartCornerRadius
    builder.dockTopEndCornerRadius = dockTopEndCornerRadius
    builder.dockBottomStartCornerRadius = dockBottomStartCornerRadius
    builder.dockBottomEndCornerRadius = dockBottomEndCornerRadius
    builder.folderCornerRadius = folderCornerRadius
    builder.folderBackgroundColorProto = folderBackgroundColor.toBackgroundColorProto()
    builder.customFolderBackgroundColor = customFolderBackgroundColor
}.build()

internal fun AppDrawerSettings.toAppDrawerSettingsProto(): AppDrawerSettingsProto = AppDrawerSettingsProto.newBuilder().also { builder ->
    builder.appDrawerColumns = appDrawerColumns
    builder.appDrawerRowsHeight = appDrawerRowsHeight
    builder.gridItemSettingsProto = gridItemSettings.toGridItemSettingsProto()
    builder.backgroundColorProto = backgroundColor.toBackgroundColorProto()
    builder.customBackgroundColor = customBackgroundColor
    builder.appDrawerTypeProto = appDrawerType.toAppDrawerTypeProto()
    builder.horizontalAppDrawerColumns = horizontalAppDrawerColumns
    builder.horizontalAppDrawerRows = horizontalAppDrawerRows
    builder.excludeTaggedApps = excludeTaggedApps
    builder.showKeyboard = showKeyboard
    builder.fuzzySearch = fuzzySearch
    builder.blurBehind = blurBehind
}.build()

internal fun GeneralSettings.toGeneralSettingsProto(): GeneralSettingsProto = GeneralSettingsProto.newBuilder().also { builder ->
    builder.themeProto = theme.toThemeProto()
    builder.dynamicTheme = dynamicTheme
    builder.iconPackInfoPackageName = iconPackInfoPackageName
}.build()

internal fun GestureSettings.toGestureSettingsProto(): GestureSettingsProto = GestureSettingsProto.newBuilder().also { builder ->
    builder.doubleTapProto = doubleTap.toEblanActionProto()
    builder.swipeUpProto = swipeUp.toEblanActionProto()
    builder.swipeDownProto = swipeDown.toEblanActionProto()
}.build()

internal fun ExperimentalSettings.toExperimentalSettingsProto(): ExperimentalSettingsProto = ExperimentalSettingsProto.newBuilder().also { builder ->
    builder.syncData = syncData
    builder.firstLaunch = firstLaunch
    builder.lockMovement = lockMovement
    builder.gridItemAnimation = gridItemAnimation
}.build()

internal fun ExperimentalSettingsProto.toExperimentalSettings(): ExperimentalSettings = ExperimentalSettings(
    syncData = syncData,
    firstLaunch = firstLaunch,
    lockMovement = lockMovement,
    gridItemAnimation = if (hasGridItemAnimation()) gridItemAnimation else true,
)

internal fun EblanAction.toEblanActionProto(): EblanActionProto = EblanActionProto.newBuilder().also { builder ->
    builder.eblanActionTypeProto = eblanActionType.toEblanActionTypeProto()
    builder.serialNumber = serialNumber
    builder.componentName = componentName
}.build()

internal fun GestureSettingsProto.toGestureSettings(): GestureSettings = GestureSettings(
    doubleTap = doubleTapProto.toEblanAction(),
    swipeUp = swipeUpProto.toEblanAction(),
    swipeDown = swipeDownProto.toEblanAction(),
)

internal fun EblanActionProto.toEblanAction(): EblanAction = EblanAction(
    eblanActionType = eblanActionTypeProto.toEblanActionType(),
    serialNumber = serialNumber,
    componentName = componentName,
)

internal fun Theme.toThemeProto(): ThemeProto = when (this) {
    Theme.System -> ThemeProto.ThemeSystem
    Theme.Light -> ThemeProto.ThemeLight
    Theme.Dark -> ThemeProto.ThemeDark
}

private fun EblanActionType.toEblanActionTypeProto(): EblanActionTypeProto = when (this) {
    EblanActionType.None -> EblanActionTypeProto.None
    EblanActionType.OpenAppDrawer -> EblanActionTypeProto.OpenAppDrawer
    EblanActionType.OpenNotificationPanel -> EblanActionTypeProto.OpenNotificationPanel
    EblanActionType.OpenApp -> EblanActionTypeProto.OpenApp
    EblanActionType.LockScreen -> EblanActionTypeProto.LockScreen
    EblanActionType.OpenQuickSettings -> EblanActionTypeProto.OpenQuickSettings
    EblanActionType.OpenRecents -> EblanActionTypeProto.OpenRecents
}

private fun EblanActionTypeProto.toEblanActionType(): EblanActionType = when (this) {
    EblanActionTypeProto.None, EblanActionTypeProto.UNRECOGNIZED -> EblanActionType.None
    EblanActionTypeProto.OpenAppDrawer -> EblanActionType.OpenAppDrawer
    EblanActionTypeProto.OpenNotificationPanel -> EblanActionType.OpenNotificationPanel
    EblanActionTypeProto.OpenApp -> EblanActionType.OpenApp
    EblanActionTypeProto.LockScreen -> EblanActionType.LockScreen
    EblanActionTypeProto.OpenQuickSettings -> EblanActionType.OpenQuickSettings
    EblanActionTypeProto.OpenRecents -> EblanActionType.OpenRecents
}

private fun ThemeProto.toTheme(): Theme = when (this) {
    ThemeProto.ThemeSystem, ThemeProto.UNRECOGNIZED -> Theme.System
    ThemeProto.ThemeLight -> Theme.Light
    ThemeProto.ThemeDark -> Theme.Dark
}

private fun TextColor.toTextColorProto(): TextColorProto = when (this) {
    TextColor.System -> TextColorProto.TextColorSystem
    TextColor.Light -> TextColorProto.TextColorLight
    TextColor.Dark -> TextColorProto.TextColorDark
    TextColor.Custom -> TextColorProto.TextColorCustom
}

private fun BackgroundColor.toBackgroundColorProto(): BackgroundColorProto = when (this) {
    BackgroundColor.System -> BackgroundColorProto.BackgroundColorSystem
    BackgroundColor.Light -> BackgroundColorProto.BackgroundColorLight
    BackgroundColor.Dark -> BackgroundColorProto.BackgroundColorDark
    BackgroundColor.Custom -> BackgroundColorProto.BackgroundColorCustom
}

private fun TextColorProto.toTextColor(): TextColor = when (this) {
    TextColorProto.TextColorSystem, TextColorProto.UNRECOGNIZED -> TextColor.System
    TextColorProto.TextColorLight -> TextColor.Light
    TextColorProto.TextColorDark -> TextColor.Dark
    TextColorProto.TextColorCustom -> TextColor.Custom
}

private fun BackgroundColorProto.toBackgroundColor(): BackgroundColor = when (this) {
    BackgroundColorProto.BackgroundColorSystem, BackgroundColorProto.UNRECOGNIZED -> BackgroundColor.System
    BackgroundColorProto.BackgroundColorLight -> BackgroundColor.Light
    BackgroundColorProto.BackgroundColorDark -> BackgroundColor.Dark
    BackgroundColorProto.BackgroundColorCustom -> BackgroundColor.Custom
}

private fun HorizontalAlignment.toHorizontalAlignmentProto(): HorizontalAlignmentProto = when (this) {
    HorizontalAlignment.Start -> HorizontalAlignmentProto.Start
    HorizontalAlignment.CenterHorizontally -> HorizontalAlignmentProto.CenterHorizontally
    HorizontalAlignment.End -> HorizontalAlignmentProto.End
}

private fun HorizontalAlignmentProto.toHorizontalAlignment(): HorizontalAlignment = when (this) {
    HorizontalAlignmentProto.Start -> HorizontalAlignment.Start
    HorizontalAlignmentProto.CenterHorizontally, HorizontalAlignmentProto.UNRECOGNIZED -> HorizontalAlignment.CenterHorizontally
    HorizontalAlignmentProto.End -> HorizontalAlignment.End
}

private fun VerticalArrangement.toVerticalArrangementProto(): VerticalArrangementProto = when (this) {
    VerticalArrangement.Top -> VerticalArrangementProto.Top
    VerticalArrangement.Center -> VerticalArrangementProto.Center
    VerticalArrangement.Bottom -> VerticalArrangementProto.Bottom
}

private fun VerticalArrangementProto.toVerticalArrangement(): VerticalArrangement = when (this) {
    VerticalArrangementProto.Top -> VerticalArrangement.Top
    VerticalArrangementProto.Center, VerticalArrangementProto.UNRECOGNIZED -> VerticalArrangement.Center
    VerticalArrangementProto.Bottom -> VerticalArrangement.Bottom
}

private fun AppDrawerType.toAppDrawerTypeProto(): AppDrawerTypeProto = when (this) {
    AppDrawerType.Vertical -> AppDrawerTypeProto.Vertical
    AppDrawerType.Horizontal -> AppDrawerTypeProto.Horizontal
    AppDrawerType.List -> AppDrawerTypeProto.List
}

private fun AppDrawerTypeProto.toAppDrawerType(): AppDrawerType = when (this) {
    AppDrawerTypeProto.Vertical, AppDrawerTypeProto.UNRECOGNIZED -> AppDrawerType.Vertical
    AppDrawerTypeProto.Horizontal -> AppDrawerType.Horizontal
    AppDrawerTypeProto.List -> AppDrawerType.List
}
