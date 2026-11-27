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
import com.eblan.launcher.data.datastore.proto.appdrawer.SearchBarTypeProto
import com.eblan.launcher.data.datastore.proto.experimental.ExperimentalSettingsProto
import com.eblan.launcher.data.datastore.proto.folder.FolderSettingsProto
import com.eblan.launcher.data.datastore.proto.general.ColorSchemeContrastPresetProto
import com.eblan.launcher.data.datastore.proto.general.ColorSchemePaletteStyleProto
import com.eblan.launcher.data.datastore.proto.general.ColorSourceProto
import com.eblan.launcher.data.datastore.proto.general.GeneralSettingsProto
import com.eblan.launcher.data.datastore.proto.general.IconShapeProto
import com.eblan.launcher.data.datastore.proto.general.ThemeProto
import com.eblan.launcher.data.datastore.proto.gesture.EblanActionProto
import com.eblan.launcher.data.datastore.proto.gesture.EblanActionTypeProto
import com.eblan.launcher.data.datastore.proto.gesture.GestureSettingsProto
import com.eblan.launcher.data.datastore.proto.home.GridItemSettingsProto
import com.eblan.launcher.data.datastore.proto.home.HomeSettingsProto
import com.eblan.launcher.data.datastore.proto.home.HorizontalAlignmentProto
import com.eblan.launcher.data.datastore.proto.home.HorizontalArrangementProto
import com.eblan.launcher.data.datastore.proto.home.LayoutTypeProto
import com.eblan.launcher.data.datastore.proto.home.PageIndicatorTypeProto
import com.eblan.launcher.data.datastore.proto.home.PagerTransitionEffectProto
import com.eblan.launcher.data.datastore.proto.home.ScreenOrientationProto
import com.eblan.launcher.data.datastore.proto.home.TextColorProto
import com.eblan.launcher.data.datastore.proto.home.VerticalAlignmentProto
import com.eblan.launcher.data.datastore.proto.home.VerticalArrangementProto
import com.eblan.launcher.data.datastore.proto.model.BackgroundColorProto
import com.eblan.launcher.domain.model.grid.GridItemSettings
import com.eblan.launcher.domain.model.grid.HorizontalAlignment
import com.eblan.launcher.domain.model.grid.HorizontalArrangement
import com.eblan.launcher.domain.model.grid.LayoutType
import com.eblan.launcher.domain.model.grid.VerticalAlignment
import com.eblan.launcher.domain.model.grid.VerticalArrangement
import com.eblan.launcher.domain.model.userdata.AppDrawerSettings
import com.eblan.launcher.domain.model.userdata.AppDrawerType
import com.eblan.launcher.domain.model.userdata.BackgroundColor
import com.eblan.launcher.domain.model.userdata.ColorSchemeContrastPreset
import com.eblan.launcher.domain.model.userdata.ColorSchemePaletteStyle
import com.eblan.launcher.domain.model.userdata.ColorSchemeSettings
import com.eblan.launcher.domain.model.userdata.ColorSource
import com.eblan.launcher.domain.model.userdata.EblanAction
import com.eblan.launcher.domain.model.userdata.EblanActionType
import com.eblan.launcher.domain.model.userdata.ExperimentalSettings
import com.eblan.launcher.domain.model.userdata.FolderSettings
import com.eblan.launcher.domain.model.userdata.GeneralSettings
import com.eblan.launcher.domain.model.userdata.GestureSettings
import com.eblan.launcher.domain.model.userdata.HomeSettings
import com.eblan.launcher.domain.model.userdata.IconShape
import com.eblan.launcher.domain.model.userdata.PageIndicatorType
import com.eblan.launcher.domain.model.userdata.PagerTransitionEffect
import com.eblan.launcher.domain.model.userdata.ScreenOrientation
import com.eblan.launcher.domain.model.userdata.SearchBarType
import com.eblan.launcher.domain.model.userdata.TextColor
import com.eblan.launcher.domain.model.userdata.Theme

internal fun HomeSettingsProto.toHomeSettings(): HomeSettings = HomeSettings(
    columns = columns,
    rows = rows,
    pageCount = pageCount,
    infiniteScroll = infiniteScroll,
    dockColumns = dockColumns,
    dockRows = dockRows,
    initialPage = initialPage,
    wallpaperScroll = wallpaperScroll,
    gridItemSettings = gridItemSettingsProto.toGridItemSettings(),
    screenOrientation = screenOrientationProto.toScreenOrientation(),
    dockPageCount = dockPageCount,
    dockInfiniteScroll = dockInfiniteScroll,
    dockInitialPage = dockInitialPage,
    addNewAppsToHomeScreen = addNewAppsToHomeScreen,
    pageIndicatorType = pageIndicatorTypeProto.toPageIndicatorType(),
    dockBackgroundColor = dockBackgroundColorProto.toBackgroundColor(),
    dockCustomBackgroundColor = dockCustomBackgroundColor,
    dockPadding = dockPadding,
    dockTopStartCornerRadius = dockTopStartCornerRadius,
    dockTopEndCornerRadius = dockTopEndCornerRadius,
    dockBottomStartCornerRadius = dockBottomStartCornerRadius,
    dockBottomEndCornerRadius = dockBottomEndCornerRadius,
    addDockBottomPadding = addDockBottomPadding,
    pagerTransitionEffect = pagerTransitionEffectProto.toPagerTransitionEffect(),
    showStatusBar = showStatusBar,
    showNavigationBar = showNavigationBar,
    enableDock = enableDock,
    dockGridItemSettings = dockGridItemSettingsProto.toGridItemSettings(),
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
    searchBarType = searchBarTypeProto.toSearchBarType(),
)

internal fun GridItemSettingsProto.toGridItemSettings(): GridItemSettings = GridItemSettings(
    iconSize = iconSize,
    textColor = textColorProto.toTextColor(),
    textSize = textSize,
    singleLineLabel = singleLineLabel,
    horizontalAlignment = horizontalAlignmentProto.toHorizontalAlignment(),
    verticalArrangement = verticalArrangementProto.toVerticalArrangement(),
    customTextColor = customTextColor,
    backgroundColor = backgroundColorProto.toBackgroundColor(),
    customBackgroundColor = customBackgroundColor,
    padding = padding,
    cornerRadius = cornerRadius,
    layoutType = layoutTypeProto.toLayoutType(),
    horizontalArrangement = horizontalArrangementProto.toHorizontalArrangement(),
    verticalAlignment = verticalAlignmentProto.toVerticalAlignment(),
    iconPadding = iconPadding,
    textPadding = textPadding,
)

internal fun GeneralSettingsProto.toGeneralSettings(): GeneralSettings = GeneralSettings(
    theme = themeProto.toTheme(),
    systemBarsTheme = systemBarsThemeProto.toTheme(),
    colorSource = colorSourceProto.toColorSource(),
    customSeedColor = customSeedColor,
    iconPackPackageName = iconPackPackageName,
    iconTint = iconTint,
    iconShape = iconShapeProto.toIconShape(),
    colorSchemeSettings = ColorSchemeSettings(
        paletteStyle = colorSchemePaletteStyleProto.toColorSchemePaletteStyle(),
        contrastPreset = colorSchemeContrastPresetProto.toColorSchemeContrastPreset(),
        amoledBlack = amoledBlack,
    ),
)

internal fun GridItemSettings.toGridItemSettingsProto(): GridItemSettingsProto = GridItemSettingsProto.newBuilder().also { builder ->
    builder.iconSize = iconSize
    builder.textColorProto = textColor.toTextColorProto()
    builder.textSize = textSize
    builder.singleLineLabel = singleLineLabel
    builder.horizontalAlignmentProto = horizontalAlignment.toHorizontalAlignmentProto()
    builder.verticalArrangementProto = verticalArrangement.toVerticalArrangementProto()
    builder.customTextColor = customTextColor
    builder.backgroundColorProto = backgroundColor.toBackgroundColorProto()
    builder.customBackgroundColor = customBackgroundColor
    builder.padding = padding
    builder.cornerRadius = cornerRadius
    builder.layoutTypeProto = layoutType.toLayoutTypeProto()
    builder.horizontalArrangementProto = horizontalArrangement.toHorizontalAlignmentProto()
    builder.verticalAlignmentProto = verticalAlignment.toVerticalArrangementProto()
    builder.iconPadding = iconPadding
    builder.textPadding = textPadding
}.build()

internal fun HomeSettings.toHomeSettingsProto(): HomeSettingsProto = HomeSettingsProto.newBuilder().also { builder ->
    builder.columns = columns
    builder.rows = rows
    builder.pageCount = pageCount
    builder.infiniteScroll = infiniteScroll
    builder.dockColumns = dockColumns
    builder.dockRows = dockRows
    builder.initialPage = initialPage
    builder.wallpaperScroll = wallpaperScroll
    builder.gridItemSettingsProto = gridItemSettings.toGridItemSettingsProto()
    builder.screenOrientationProto = screenOrientation.toScreenOrientationProto()
    builder.dockPageCount = dockPageCount
    builder.dockInfiniteScroll = dockInfiniteScroll
    builder.dockInitialPage = dockInitialPage
    builder.addNewAppsToHomeScreen = addNewAppsToHomeScreen
    builder.pageIndicatorTypeProto = pageIndicatorType.toPageIndicatorTypeProto()
    builder.dockBackgroundColorProto = dockBackgroundColor.toBackgroundColorProto()
    builder.dockCustomBackgroundColor = dockCustomBackgroundColor
    builder.dockPadding = dockPadding
    builder.dockTopStartCornerRadius = dockTopStartCornerRadius
    builder.dockTopEndCornerRadius = dockTopEndCornerRadius
    builder.dockBottomStartCornerRadius = dockBottomStartCornerRadius
    builder.dockBottomEndCornerRadius = dockBottomEndCornerRadius
    builder.addDockBottomPadding = addDockBottomPadding
    builder.pagerTransitionEffectProto = pagerTransitionEffect.toPagerTransitionEffectProto()
    builder.showStatusBar = showStatusBar
    builder.showNavigationBar = showNavigationBar
    builder.enableDock = enableDock
    builder.dockGridItemSettingsProto = dockGridItemSettings.toGridItemSettingsProto()
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
    builder.searchBarTypeProto = searchBarType.toSearchBarTypeProto()
}.build()

internal fun GeneralSettings.toGeneralSettingsProto(): GeneralSettingsProto = GeneralSettingsProto.newBuilder().also { builder ->
    builder.themeProto = theme.toThemeProto()
    builder.systemBarsThemeProto = systemBarsTheme.toThemeProto()
    builder.colorSourceProto = colorSource.toColorSourceProto()
    builder.customSeedColor = customSeedColor
    builder.iconPackPackageName = iconPackPackageName
    builder.iconTint = iconTint
    builder.iconShapeProto = iconShape.toIconShapeProto()
    builder.colorSchemePaletteStyleProto =
        colorSchemeSettings.paletteStyle.toColorSchemePaletteStyleProto()
    builder.colorSchemeContrastPresetProto =
        colorSchemeSettings.contrastPreset.toColorSchemeContrastPresetProto()
    builder.amoledBlack = colorSchemeSettings.amoledBlack
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
    builder.cleanCache = cleanCache
}.build()

internal fun ExperimentalSettingsProto.toExperimentalSettings(): ExperimentalSettings = ExperimentalSettings(
    syncData = syncData,
    firstLaunch = firstLaunch,
    lockMovement = lockMovement,
    gridItemAnimation = gridItemAnimation,
    cleanCache = cleanCache,
)

internal fun FolderSettingsProto.toFolderSettings(): FolderSettings = FolderSettings(
    folderCellWidth = folderCellWidth,
    folderCellHeight = folderCellHeight,
    maxFolderColumns = maxFolderColumns,
    maxFolderRows = maxFolderRows,
    folderCornerRadius = folderCornerRadius,
    folderBackgroundColor = folderBackgroundColorProto.toBackgroundColor(),
    customFolderBackgroundColor = customFolderBackgroundColor,
    folderBorderColor = folderBorderColorProto.toBackgroundColor(),
    customFolderBorderColor = customFolderBorderColor,
    borderWidth = borderWidth,
)

internal fun FolderSettings.toFolderSettingsProto(): FolderSettingsProto = FolderSettingsProto.newBuilder().also { builder ->
    builder.folderCellWidth = folderCellWidth
    builder.folderCellHeight = folderCellHeight
    builder.maxFolderColumns = maxFolderColumns
    builder.maxFolderRows = maxFolderRows
    builder.folderCornerRadius = folderCornerRadius
    builder.folderBackgroundColorProto = folderBackgroundColor.toBackgroundColorProto()
    builder.customFolderBackgroundColor = customFolderBackgroundColor
    builder.folderBorderColorProto = folderBorderColor.toBackgroundColorProto()
    builder.customFolderBorderColor = customFolderBorderColor
    builder.borderWidth = borderWidth
}.build()

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
    Theme.Wallpaper -> ThemeProto.ThemeWallpaper
}

private fun PagerTransitionEffect.toPagerTransitionEffectProto(): PagerTransitionEffectProto = when (this) {
    PagerTransitionEffect.None -> PagerTransitionEffectProto.PagerTransitionEffectNone
    PagerTransitionEffect.Fade -> PagerTransitionEffectProto.PagerTransitionEffectFade
    PagerTransitionEffect.ZoomOut -> PagerTransitionEffectProto.PagerTransitionEffectZoomOut
    PagerTransitionEffect.Depth -> PagerTransitionEffectProto.PagerTransitionEffectDepth
    PagerTransitionEffect.Cube -> PagerTransitionEffectProto.PagerTransitionEffectCube
    PagerTransitionEffect.Flip -> PagerTransitionEffectProto.PagerTransitionEffectFlip
    PagerTransitionEffect.Parallax -> PagerTransitionEffectProto.PagerTransitionEffectParallax
    PagerTransitionEffect.Stack -> PagerTransitionEffectProto.PagerTransitionEffectStack
    PagerTransitionEffect.CubeInside -> PagerTransitionEffectProto.PagerTransitionEffectCubeInside
    PagerTransitionEffect.Accordion -> PagerTransitionEffectProto.PagerTransitionEffectAccordion
    PagerTransitionEffect.Windmill -> PagerTransitionEffectProto.PagerTransitionEffectWindmill
    PagerTransitionEffect.Jelly -> PagerTransitionEffectProto.PagerTransitionEffectJelly
    PagerTransitionEffect.Hinge -> PagerTransitionEffectProto.PagerTransitionEffectHinge
    PagerTransitionEffect.Pinwheel -> PagerTransitionEffectProto.PagerTransitionEffectPinwheel
    PagerTransitionEffect.CardToss -> PagerTransitionEffectProto.PagerTransitionEffectCardToss
    PagerTransitionEffect.Crt -> PagerTransitionEffectProto.PagerTransitionEffectCrt
    PagerTransitionEffect.Random -> PagerTransitionEffectProto.PagerTransitionEffectRandom
}

private fun PagerTransitionEffectProto.toPagerTransitionEffect(): PagerTransitionEffect = when (this) {
    PagerTransitionEffectProto.PagerTransitionEffectNone,
    PagerTransitionEffectProto.UNRECOGNIZED,
    -> PagerTransitionEffect.None

    PagerTransitionEffectProto.PagerTransitionEffectFade -> PagerTransitionEffect.Fade

    PagerTransitionEffectProto.PagerTransitionEffectZoomOut -> PagerTransitionEffect.ZoomOut

    PagerTransitionEffectProto.PagerTransitionEffectDepth -> PagerTransitionEffect.Depth

    PagerTransitionEffectProto.PagerTransitionEffectCube -> PagerTransitionEffect.Cube

    PagerTransitionEffectProto.PagerTransitionEffectFlip -> PagerTransitionEffect.Flip

    PagerTransitionEffectProto.PagerTransitionEffectParallax -> PagerTransitionEffect.Parallax

    PagerTransitionEffectProto.PagerTransitionEffectStack -> PagerTransitionEffect.Stack

    PagerTransitionEffectProto.PagerTransitionEffectCubeInside -> PagerTransitionEffect.CubeInside

    PagerTransitionEffectProto.PagerTransitionEffectAccordion -> PagerTransitionEffect.Accordion

    PagerTransitionEffectProto.PagerTransitionEffectWindmill -> PagerTransitionEffect.Windmill

    PagerTransitionEffectProto.PagerTransitionEffectJelly -> PagerTransitionEffect.Jelly

    PagerTransitionEffectProto.PagerTransitionEffectHinge -> PagerTransitionEffect.Hinge

    PagerTransitionEffectProto.PagerTransitionEffectPinwheel -> PagerTransitionEffect.Pinwheel

    PagerTransitionEffectProto.PagerTransitionEffectCardToss -> PagerTransitionEffect.CardToss

    PagerTransitionEffectProto.PagerTransitionEffectCrt -> PagerTransitionEffect.Crt

    PagerTransitionEffectProto.PagerTransitionEffectRandom -> PagerTransitionEffect.Random
}

private fun PageIndicatorType.toPageIndicatorTypeProto(): PageIndicatorTypeProto = when (this) {
    PageIndicatorType.Dots -> PageIndicatorTypeProto.PageIndicatorTypeDots
    PageIndicatorType.Line -> PageIndicatorTypeProto.PageIndicatorTypeLine
    PageIndicatorType.Worm -> PageIndicatorTypeProto.PageIndicatorTypeWorm
    PageIndicatorType.JumpingDot -> PageIndicatorTypeProto.PageIndicatorTypeJumpingDot
    PageIndicatorType.Equalizer -> PageIndicatorTypeProto.PageIndicatorTypeEqualizer
    PageIndicatorType.Diamonds -> PageIndicatorTypeProto.PageIndicatorTypeDiamonds
    PageIndicatorType.Numbers -> PageIndicatorTypeProto.PageIndicatorTypeNumbers
    PageIndicatorType.PacMan -> PageIndicatorTypeProto.PageIndicatorTypePacMan
    PageIndicatorType.None -> PageIndicatorTypeProto.PageIndicatorTypeNone
}

private fun PageIndicatorTypeProto.toPageIndicatorType(): PageIndicatorType = when (this) {
    PageIndicatorTypeProto.PageIndicatorTypeDots,
    PageIndicatorTypeProto.UNRECOGNIZED,
    -> PageIndicatorType.Dots

    PageIndicatorTypeProto.PageIndicatorTypeLine -> PageIndicatorType.Line

    PageIndicatorTypeProto.PageIndicatorTypeWorm -> PageIndicatorType.Worm

    PageIndicatorTypeProto.PageIndicatorTypeJumpingDot -> PageIndicatorType.JumpingDot

    PageIndicatorTypeProto.PageIndicatorTypeEqualizer -> PageIndicatorType.Equalizer

    PageIndicatorTypeProto.PageIndicatorTypeDiamonds -> PageIndicatorType.Diamonds

    PageIndicatorTypeProto.PageIndicatorTypeNumbers -> PageIndicatorType.Numbers

    PageIndicatorTypeProto.PageIndicatorTypePacMan -> PageIndicatorType.PacMan

    PageIndicatorTypeProto.PageIndicatorTypeNone -> PageIndicatorType.None
}

private fun ScreenOrientation.toScreenOrientationProto(): ScreenOrientationProto = when (this) {
    ScreenOrientation.System -> ScreenOrientationProto.ScreenOrientationSystem
    ScreenOrientation.Portrait -> ScreenOrientationProto.ScreenOrientationPortrait
    ScreenOrientation.Landscape -> ScreenOrientationProto.ScreenOrientationLandscape
}

private fun ScreenOrientationProto.toScreenOrientation(): ScreenOrientation = when (this) {
    ScreenOrientationProto.ScreenOrientationSystem,
    ScreenOrientationProto.UNRECOGNIZED,
    -> ScreenOrientation.System

    ScreenOrientationProto.ScreenOrientationPortrait -> ScreenOrientation.Portrait

    ScreenOrientationProto.ScreenOrientationLandscape -> ScreenOrientation.Landscape
}

private fun EblanActionType.toEblanActionTypeProto(): EblanActionTypeProto = when (this) {
    EblanActionType.None -> EblanActionTypeProto.None
    EblanActionType.OpenAppDrawer -> EblanActionTypeProto.OpenAppDrawer
    EblanActionType.OpenNotificationPanel -> EblanActionTypeProto.OpenNotificationPanel
    EblanActionType.OpenApp -> EblanActionTypeProto.OpenApp
    EblanActionType.LockScreen -> EblanActionTypeProto.LockScreen
    EblanActionType.OpenQuickSettings -> EblanActionTypeProto.OpenQuickSettings
    EblanActionType.OpenRecents -> EblanActionTypeProto.OpenRecents
    EblanActionType.OpenNotificationPanelByReflection -> EblanActionTypeProto.OpenNotificationPanelByReflection
}

private fun EblanActionTypeProto.toEblanActionType(): EblanActionType = when (this) {
    EblanActionTypeProto.None, EblanActionTypeProto.UNRECOGNIZED -> EblanActionType.None
    EblanActionTypeProto.OpenAppDrawer -> EblanActionType.OpenAppDrawer
    EblanActionTypeProto.OpenNotificationPanel -> EblanActionType.OpenNotificationPanel
    EblanActionTypeProto.OpenApp -> EblanActionType.OpenApp
    EblanActionTypeProto.LockScreen -> EblanActionType.LockScreen
    EblanActionTypeProto.OpenQuickSettings -> EblanActionType.OpenQuickSettings
    EblanActionTypeProto.OpenRecents -> EblanActionType.OpenRecents
    EblanActionTypeProto.OpenNotificationPanelByReflection -> EblanActionType.OpenNotificationPanelByReflection
}

private fun ThemeProto.toTheme(): Theme = when (this) {
    ThemeProto.ThemeSystem, ThemeProto.UNRECOGNIZED -> Theme.System
    ThemeProto.ThemeLight -> Theme.Light
    ThemeProto.ThemeDark -> Theme.Dark
    ThemeProto.ThemeWallpaper -> Theme.Wallpaper
}

private fun TextColor.toTextColorProto(): TextColorProto = when (this) {
    TextColor.System -> TextColorProto.TextColorSystem
    TextColor.Light -> TextColorProto.TextColorLight
    TextColor.Dark -> TextColorProto.TextColorDark
    TextColor.Custom -> TextColorProto.TextColorCustom
}

private fun BackgroundColor.toBackgroundColorProto(): BackgroundColorProto = when (this) {
    BackgroundColor.System -> BackgroundColorProto.BackgroundColorSystem
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
    BackgroundColorProto.BackgroundColorCustom -> BackgroundColor.Custom
}

private fun HorizontalAlignment.toHorizontalAlignmentProto(): HorizontalAlignmentProto = when (this) {
    HorizontalAlignment.Start -> HorizontalAlignmentProto.HorizontalAlignmentStart
    HorizontalAlignment.CenterHorizontally -> HorizontalAlignmentProto.HorizontalAlignmentCenterHorizontally
    HorizontalAlignment.End -> HorizontalAlignmentProto.HorizontalAlignmentEnd
}

private fun HorizontalAlignmentProto.toHorizontalAlignment(): HorizontalAlignment = when (this) {
    HorizontalAlignmentProto.HorizontalAlignmentStart -> HorizontalAlignment.Start
    HorizontalAlignmentProto.HorizontalAlignmentCenterHorizontally, HorizontalAlignmentProto.UNRECOGNIZED -> HorizontalAlignment.CenterHorizontally
    HorizontalAlignmentProto.HorizontalAlignmentEnd -> HorizontalAlignment.End
}

private fun VerticalArrangement.toVerticalArrangementProto(): VerticalArrangementProto = when (this) {
    VerticalArrangement.Top -> VerticalArrangementProto.VerticalArrangementTop
    VerticalArrangement.Center -> VerticalArrangementProto.VerticalArrangementCenter
    VerticalArrangement.Bottom -> VerticalArrangementProto.VerticalArrangementBottom
}

private fun VerticalArrangementProto.toVerticalArrangement(): VerticalArrangement = when (this) {
    VerticalArrangementProto.VerticalArrangementTop -> VerticalArrangement.Top
    VerticalArrangementProto.VerticalArrangementCenter, VerticalArrangementProto.UNRECOGNIZED -> VerticalArrangement.Center
    VerticalArrangementProto.VerticalArrangementBottom -> VerticalArrangement.Bottom
}

private fun AppDrawerType.toAppDrawerTypeProto(): AppDrawerTypeProto = when (this) {
    AppDrawerType.Vertical -> AppDrawerTypeProto.Vertical
    AppDrawerType.Horizontal -> AppDrawerTypeProto.Horizontal
}

private fun AppDrawerTypeProto.toAppDrawerType(): AppDrawerType = when (this) {
    AppDrawerTypeProto.Vertical, AppDrawerTypeProto.UNRECOGNIZED -> AppDrawerType.Vertical
    AppDrawerTypeProto.Horizontal -> AppDrawerType.Horizontal
}

private fun SearchBarType.toSearchBarTypeProto(): SearchBarTypeProto = when (this) {
    SearchBarType.Top -> SearchBarTypeProto.SearchBarTypeTop
    SearchBarType.Floating -> SearchBarTypeProto.SearchBarTypeFloating
    SearchBarType.None -> SearchBarTypeProto.SearchBarTypeNone
}

private fun SearchBarTypeProto.toSearchBarType(): SearchBarType = when (this) {
    SearchBarTypeProto.SearchBarTypeTop, SearchBarTypeProto.UNRECOGNIZED -> SearchBarType.Top
    SearchBarTypeProto.SearchBarTypeFloating -> SearchBarType.Floating
    SearchBarTypeProto.SearchBarTypeNone -> SearchBarType.None
}

private fun LayoutType.toLayoutTypeProto(): LayoutTypeProto = when (this) {
    LayoutType.TopIconBottomLabel -> LayoutTypeProto.LayoutTypeTopIconBottomLabel
    LayoutType.TopLabelBottomIcon -> LayoutTypeProto.LayoutTypeTopLabelBottomIcon
    LayoutType.StartIconEndLabel -> LayoutTypeProto.LayoutTypeStartIconEndLabel
    LayoutType.StartLabelEndIcon -> LayoutTypeProto.LayoutTypeStartLabelEndIcon
    LayoutType.IconOnly -> LayoutTypeProto.LayoutTypeIconOnly
    LayoutType.LabelOnly -> LayoutTypeProto.LayoutTypeLabelOnly
}

private fun LayoutTypeProto.toLayoutType(): LayoutType = when (this) {
    LayoutTypeProto.LayoutTypeTopIconBottomLabel, LayoutTypeProto.UNRECOGNIZED -> LayoutType.TopIconBottomLabel
    LayoutTypeProto.LayoutTypeTopLabelBottomIcon -> LayoutType.TopLabelBottomIcon
    LayoutTypeProto.LayoutTypeStartIconEndLabel -> LayoutType.StartIconEndLabel
    LayoutTypeProto.LayoutTypeStartLabelEndIcon -> LayoutType.StartLabelEndIcon
    LayoutTypeProto.LayoutTypeIconOnly -> LayoutType.IconOnly
    LayoutTypeProto.LayoutTypeLabelOnly -> LayoutType.LabelOnly
}

private fun HorizontalArrangement.toHorizontalAlignmentProto(): HorizontalArrangementProto = when (this) {
    HorizontalArrangement.Start -> HorizontalArrangementProto.HorizontalArrangementStart
    HorizontalArrangement.Center -> HorizontalArrangementProto.HorizontalArrangementCenter
    HorizontalArrangement.End -> HorizontalArrangementProto.HorizontalArrangementEnd
}

private fun HorizontalArrangementProto.toHorizontalArrangement(): HorizontalArrangement = when (this) {
    HorizontalArrangementProto.HorizontalArrangementStart, HorizontalArrangementProto.UNRECOGNIZED -> HorizontalArrangement.Start
    HorizontalArrangementProto.HorizontalArrangementCenter -> HorizontalArrangement.Center
    HorizontalArrangementProto.HorizontalArrangementEnd -> HorizontalArrangement.End
}

private fun VerticalAlignment.toVerticalArrangementProto(): VerticalAlignmentProto = when (this) {
    VerticalAlignment.Top -> VerticalAlignmentProto.VerticalAlignmentTop
    VerticalAlignment.CenterVertically -> VerticalAlignmentProto.VerticalAlignmentCenterVertically
    VerticalAlignment.Bottom -> VerticalAlignmentProto.VerticalAlignmentBottom
}

private fun VerticalAlignmentProto.toVerticalAlignment(): VerticalAlignment = when (this) {
    VerticalAlignmentProto.VerticalAlignmentTop, VerticalAlignmentProto.UNRECOGNIZED -> VerticalAlignment.Top
    VerticalAlignmentProto.VerticalAlignmentCenterVertically -> VerticalAlignment.CenterVertically
    VerticalAlignmentProto.VerticalAlignmentBottom -> VerticalAlignment.Bottom
}

private fun ColorSourceProto.toColorSource(): ColorSource = when (this) {
    ColorSourceProto.ColorSourceNone, ColorSourceProto.UNRECOGNIZED -> ColorSource.None
    ColorSourceProto.ColorSourceWallpaper -> ColorSource.Wallpaper
    ColorSourceProto.ColorSourceCustom -> ColorSource.Custom
}

private fun ColorSchemePaletteStyleProto.toColorSchemePaletteStyle(): ColorSchemePaletteStyle = when (this) {
    ColorSchemePaletteStyleProto.ColorSchemePaletteStyleTonalSpot, ColorSchemePaletteStyleProto.UNRECOGNIZED -> ColorSchemePaletteStyle.TonalSpot
    ColorSchemePaletteStyleProto.ColorSchemePaletteStyleNeutral -> ColorSchemePaletteStyle.Neutral
    ColorSchemePaletteStyleProto.ColorSchemePaletteStyleVibrant -> ColorSchemePaletteStyle.Vibrant
    ColorSchemePaletteStyleProto.ColorSchemePaletteStyleExpressive -> ColorSchemePaletteStyle.Expressive
    ColorSchemePaletteStyleProto.ColorSchemePaletteStyleRainbow -> ColorSchemePaletteStyle.Rainbow
    ColorSchemePaletteStyleProto.ColorSchemePaletteStyleFruitSalad -> ColorSchemePaletteStyle.FruitSalad
    ColorSchemePaletteStyleProto.ColorSchemePaletteStyleMonochrome -> ColorSchemePaletteStyle.Monochrome
    ColorSchemePaletteStyleProto.ColorSchemePaletteStyleFidelity -> ColorSchemePaletteStyle.Fidelity
    ColorSchemePaletteStyleProto.ColorSchemePaletteStyleContent -> ColorSchemePaletteStyle.Content
}

private fun ColorSchemePaletteStyle.toColorSchemePaletteStyleProto(): ColorSchemePaletteStyleProto = when (this) {
    ColorSchemePaletteStyle.TonalSpot -> ColorSchemePaletteStyleProto.ColorSchemePaletteStyleTonalSpot
    ColorSchemePaletteStyle.Neutral -> ColorSchemePaletteStyleProto.ColorSchemePaletteStyleNeutral
    ColorSchemePaletteStyle.Vibrant -> ColorSchemePaletteStyleProto.ColorSchemePaletteStyleVibrant
    ColorSchemePaletteStyle.Expressive -> ColorSchemePaletteStyleProto.ColorSchemePaletteStyleExpressive
    ColorSchemePaletteStyle.Rainbow -> ColorSchemePaletteStyleProto.ColorSchemePaletteStyleRainbow
    ColorSchemePaletteStyle.FruitSalad -> ColorSchemePaletteStyleProto.ColorSchemePaletteStyleFruitSalad
    ColorSchemePaletteStyle.Monochrome -> ColorSchemePaletteStyleProto.ColorSchemePaletteStyleMonochrome
    ColorSchemePaletteStyle.Fidelity -> ColorSchemePaletteStyleProto.ColorSchemePaletteStyleFidelity
    ColorSchemePaletteStyle.Content -> ColorSchemePaletteStyleProto.ColorSchemePaletteStyleContent
}

private fun ColorSchemeContrastPresetProto.toColorSchemeContrastPreset(): ColorSchemeContrastPreset = when (this) {
    ColorSchemeContrastPresetProto.ColorSchemeContrastPresetStandard, ColorSchemeContrastPresetProto.UNRECOGNIZED -> ColorSchemeContrastPreset.Standard
    ColorSchemeContrastPresetProto.ColorSchemeContrastPresetReduced -> ColorSchemeContrastPreset.Reduced
    ColorSchemeContrastPresetProto.ColorSchemeContrastPresetMedium -> ColorSchemeContrastPreset.Medium
    ColorSchemeContrastPresetProto.ColorSchemeContrastPresetHigh -> ColorSchemeContrastPreset.High
}

private fun ColorSchemeContrastPreset.toColorSchemeContrastPresetProto(): ColorSchemeContrastPresetProto = when (this) {
    ColorSchemeContrastPreset.Standard -> ColorSchemeContrastPresetProto.ColorSchemeContrastPresetStandard
    ColorSchemeContrastPreset.Reduced -> ColorSchemeContrastPresetProto.ColorSchemeContrastPresetReduced
    ColorSchemeContrastPreset.Medium -> ColorSchemeContrastPresetProto.ColorSchemeContrastPresetMedium
    ColorSchemeContrastPreset.High -> ColorSchemeContrastPresetProto.ColorSchemeContrastPresetHigh
}

private fun ColorSource.toColorSourceProto(): ColorSourceProto = when (this) {
    ColorSource.None -> ColorSourceProto.ColorSourceNone
    ColorSource.Wallpaper -> ColorSourceProto.ColorSourceWallpaper
    ColorSource.Custom -> ColorSourceProto.ColorSourceCustom
}

private fun IconShapeProto.toIconShape(): IconShape = when (this) {
    IconShapeProto.IconShapeNone, IconShapeProto.UNRECOGNIZED -> IconShape.None
    IconShapeProto.IconShapeCircle -> IconShape.Circle
    IconShapeProto.IconShapeSquare -> IconShape.Square
    IconShapeProto.IconShapeRoundedSquare -> IconShape.RoundedSquare
    IconShapeProto.IconShapeTearDrop -> IconShape.TearDrop
    IconShapeProto.IconShapeHexagon -> IconShape.Hexagon
    IconShapeProto.IconShapeOctagon -> IconShape.Octagon
    IconShapeProto.IconShapeDiamond -> IconShape.Diamond
    IconShapeProto.IconShapePentagon -> IconShape.Pentagon
    IconShapeProto.IconShapeClover -> IconShape.Clover
    IconShapeProto.IconShapeCookie -> IconShape.Cookie
    IconShapeProto.IconShapeFlower -> IconShape.Flower
    IconShapeProto.IconShapeShield -> IconShape.Shield
    IconShapeProto.IconShapeSquircle -> IconShape.Squircle
    IconShapeProto.IconShapeLeaf -> IconShape.Leaf
    IconShapeProto.IconShapeArch -> IconShape.Arch
    IconShapeProto.IconShapeHeart -> IconShape.Heart
    IconShapeProto.IconShapeStar -> IconShape.Star
    IconShapeProto.IconShapeScallop -> IconShape.Scallop
    IconShapeProto.IconShapeCloud -> IconShape.Cloud
    IconShapeProto.IconShapeCat -> IconShape.Cat
    IconShapeProto.IconShapeGhost -> IconShape.Ghost
    IconShapeProto.IconShapePacMan -> IconShape.PacMan
}

private fun IconShape.toIconShapeProto(): IconShapeProto = when (this) {
    IconShape.None -> IconShapeProto.IconShapeNone
    IconShape.Circle -> IconShapeProto.IconShapeCircle
    IconShape.Square -> IconShapeProto.IconShapeSquare
    IconShape.RoundedSquare -> IconShapeProto.IconShapeRoundedSquare
    IconShape.TearDrop -> IconShapeProto.IconShapeTearDrop
    IconShape.Hexagon -> IconShapeProto.IconShapeHexagon
    IconShape.Octagon -> IconShapeProto.IconShapeOctagon
    IconShape.Diamond -> IconShapeProto.IconShapeDiamond
    IconShape.Pentagon -> IconShapeProto.IconShapePentagon
    IconShape.Clover -> IconShapeProto.IconShapeClover
    IconShape.Cookie -> IconShapeProto.IconShapeCookie
    IconShape.Flower -> IconShapeProto.IconShapeFlower
    IconShape.Shield -> IconShapeProto.IconShapeShield
    IconShape.Squircle -> IconShapeProto.IconShapeSquircle
    IconShape.Leaf -> IconShapeProto.IconShapeLeaf
    IconShape.Arch -> IconShapeProto.IconShapeArch
    IconShape.Heart -> IconShapeProto.IconShapeHeart
    IconShape.Star -> IconShapeProto.IconShapeStar
    IconShape.Scallop -> IconShapeProto.IconShapeScallop
    IconShape.Cloud -> IconShapeProto.IconShapeCloud
    IconShape.Cat -> IconShapeProto.IconShapeCat
    IconShape.Ghost -> IconShapeProto.IconShapeGhost
    IconShape.PacMan -> IconShapeProto.IconShapePacMan
}
