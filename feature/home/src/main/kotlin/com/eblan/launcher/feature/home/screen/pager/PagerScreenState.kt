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
package com.eblan.launcher.feature.home.screen.pager

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps.PinItemRequest
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.toAndroidDragEvent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import com.eblan.launcher.common.AndroidImageSerializer
import com.eblan.launcher.domain.common.FileManager
import com.eblan.launcher.domain.common.IconKeyGenerator
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.EblanAction
import com.eblan.launcher.domain.model.EblanActionType
import com.eblan.launcher.domain.model.EblanApplicationInfoGroup
import com.eblan.launcher.domain.model.ExperimentalSettings
import com.eblan.launcher.domain.model.GestureSettings
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.HomeSettings
import com.eblan.launcher.domain.model.MoveGridItemResult
import com.eblan.launcher.domain.model.PinItemRequestType
import com.eblan.launcher.feature.home.model.Drag
import com.eblan.launcher.feature.home.model.GridItemSource
import com.eblan.launcher.feature.home.model.PageDirection
import com.eblan.launcher.feature.home.model.SharedElementKey
import com.eblan.launcher.feature.home.util.calculatePage
import com.eblan.launcher.feature.home.util.handleEblanAction
import com.eblan.launcher.framework.launcherapps.AndroidLauncherAppsWrapper
import com.eblan.launcher.framework.launcherapps.PinItemRequestWrapper
import com.eblan.launcher.framework.usermanager.AndroidUserManagerWrapper
import com.eblan.launcher.framework.wallpapermanager.AndroidWallpaperManagerWrapper
import com.eblan.launcher.ui.local.LocalFileManager
import com.eblan.launcher.ui.local.LocalIconKeyGenerator
import com.eblan.launcher.ui.local.LocalImageSerializer
import com.eblan.launcher.ui.local.LocalLauncherApps
import com.eblan.launcher.ui.local.LocalPinItemRequest
import com.eblan.launcher.ui.local.LocalUserManager
import com.eblan.launcher.ui.local.LocalWallpaperManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

/**
 * The [PagerScreen] is so huge that we have to do this
 * 2k LOC is unacceptable and this is only what we've got
 */
@OptIn(ExperimentalFoundationApi::class)
internal class PagerScreenState(
    density: Density,
    private val screenHeight: Int,
    private val fileManager: FileManager,
    private val androidImageSerializer: AndroidImageSerializer,
    private val androidLauncherAppsWrapper: AndroidLauncherAppsWrapper,
    private val scope: CoroutineScope,
    private val context: Context,
    private val androidUserManagerWrapper: AndroidUserManagerWrapper,
    private val pinItemRequestWrapper: PinItemRequestWrapper,
    private val gestureSettings: GestureSettings,
    private val homeSettings: HomeSettings,
    private val androidWallpaperManagerWrapper: AndroidWallpaperManagerWrapper,
    private val iconKeyGenerator: IconKeyGenerator,
    private val onGetPinGridItem: (PinItemRequestType) -> Unit,
    private val onResetPinGridItem: () -> Unit,
) {
    var hasDoubleTap by mutableStateOf(false)
        private set

    var eblanApplicationInfoGroup by mutableStateOf<EblanApplicationInfoGroup?>(null)
        private set

    var showGridItemPopup by mutableStateOf(false)
        private set

    var showSettingsPopup by mutableStateOf(false)
        private set

    var showFolderGridItemPopup by mutableStateOf(false)
        private set

    var isDragging by mutableStateOf(false)
        private set

    var isResizing by mutableStateOf(false)
        private set

    var settingsPopupIntOffset by mutableStateOf<IntOffset?>(null)
        private set

    var popupIntOffset by mutableStateOf<IntOffset?>(null)
        private set

    var popupIntSize by mutableStateOf<IntSize?>(null)
        private set

    var widgetGridItem by mutableStateOf<GridItem?>(null)
        private set

    var gridPageDirection by mutableStateOf<PageDirection?>(null)
        private set

    var dockPageDirection by mutableStateOf<PageDirection?>(null)
        private set

    var dragIntOffset by mutableStateOf(IntOffset.Zero)
        private set

    var overlayIntOffset by mutableStateOf<IntOffset?>(null)
        private set

    var overlayIntSize by mutableStateOf<IntSize?>(null)
        private set

    var overlayImageBitmap by mutableStateOf<ImageBitmap?>(null)
        private set

    var drag by mutableStateOf(Drag.None)
        private set

    var sharedElementKey by mutableStateOf<SharedElementKey?>(null)
        private set

    var associate by mutableStateOf<Associate?>(null)
        private set

    var isVisibleFolders by mutableStateOf(false)
        private set

    val swipeUpY = Animatable(screenHeight.toFloat())

    val swipeDownY = Animatable(screenHeight.toFloat())

    val target = object : DragAndDropTarget {
        override fun onStarted(event: DragAndDropEvent) {
            val offset = with(event.toAndroidDragEvent()) {
                IntOffset(x = x.roundToInt(), y = y.roundToInt())
            }

            drag = Drag.Start

            dragIntOffset = offset

            scope.launch {
                handlePinItemRequest(pinItemRequest = pinItemRequestWrapper.getPinItemRequest())
            }
        }

        override fun onEnded(event: DragAndDropEvent) {
            drag = Drag.End

            val pinItemRequest = pinItemRequestWrapper.getPinItemRequest()

            if (pinItemRequest != null) {
                onResetPinGridItem()

                pinItemRequestWrapper.updatePinItemRequest(null)
            }
        }

        override fun onMoved(event: DragAndDropEvent) {
            val offset = with(event.toAndroidDragEvent()) {
                IntOffset(x = x.roundToInt(), y = y.roundToInt())
            }

            drag = Drag.Dragging

            dragIntOffset = offset
        }

        override fun onDrop(event: DragAndDropEvent): Boolean = true
    }

    val applicationScreenSwipeY by derivedStateOf {
        if (swipeUpY.value < screenHeight.toFloat() &&
            gestureSettings.swipeUp.eblanActionType == EblanActionType.OpenAppDrawer
        ) {
            swipeUpY
        } else if (swipeDownY.value < screenHeight.toFloat() &&
            gestureSettings.swipeDown.eblanActionType == EblanActionType.OpenAppDrawer
        ) {
            swipeDownY
        } else {
            Animatable(screenHeight.toFloat())
        }
    }

    val isApplicationScreenVisible by derivedStateOf {
        applicationScreenSwipeY.value < screenHeight.toFloat()
    }

    val applicationScreenAlpha by derivedStateOf {
        ((screenHeight - applicationScreenSwipeY.value) / (screenHeight / 2)).coerceIn(0f, 1f)
    }

    val applicationScreenCornerSize by derivedStateOf {
        val progress = (applicationScreenSwipeY.value / screenHeight).coerceIn(0f, 1f)

        (20 * progress).dp
    }

    val pagerScreenAlpha by derivedStateOf {
        val threshold = screenHeight / 2

        ((applicationScreenSwipeY.value - threshold) / threshold).coerceIn(0f, 1f)
    }

    val widgetScreenSwipeY = Animatable(screenHeight.toFloat())

    val widgetScreenAlpha by derivedStateOf {
        ((screenHeight - widgetScreenSwipeY.value) / (screenHeight / 2)).coerceIn(0f, 1f)
    }

    val widgetScreenCornerSize by derivedStateOf {
        val progress = (widgetScreenSwipeY.value / screenHeight).coerceIn(0f, 1f)

        (20 * progress).dp
    }

    val shortcutConfigScreenSwipeY = Animatable(screenHeight.toFloat())

    val shortcutConfigScreenAlpha by derivedStateOf {
        ((screenHeight - shortcutConfigScreenSwipeY.value) / (screenHeight / 2)).coerceIn(0f, 1f)
    }

    val shortcutConfigScreenCornerSize by derivedStateOf {
        val progress = (shortcutConfigScreenSwipeY.value / screenHeight).coerceIn(0f, 1f)

        (20 * progress).dp
    }

    val appWidgetScreenSwipeY = Animatable(screenHeight.toFloat())

    var isCloseGridItemPopup by mutableStateOf(false)
        private set

    var isCloseFolderGridItemPopup by mutableStateOf(false)
        private set

    var showApplicationScreen by mutableStateOf(false)
        private set

    var showWidgetScreen by mutableStateOf(false)
        private set

    var showShortcutConfigScreen by mutableStateOf(false)
        private set

    var lastAppWidgetId by mutableIntStateOf(AppWidgetManager.INVALID_APPWIDGET_ID)
        private set

    val isAvailableSystemNavigation
        get() = applicationScreenSwipeY.value == screenHeight.toFloat() &&
            !showWidgetScreen &&
            !showShortcutConfigScreen &&
            !showGridItemPopup &&
            !showSettingsPopup &&
            !showFolderGridItemPopup &&
            eblanApplicationInfoGroup == null

    private val touchSlop = with(density) {
        50.dp.toPx()
    }

    private var accumulatedDragOffset by mutableStateOf(Offset.Zero)

    suspend fun handlePinGridItemEffect(
        pinGridItem: GridItem?,
        onUpdateGridItemSource: (GridItemSource) -> Unit,
        onUpdateIsVisibleOverlay: (Boolean) -> Unit,
        onUpdateMoveGridItemResult: (MoveGridItemResult) -> Unit,
    ) {
        if (pinGridItem == null) return

        val pinItemRequest = pinItemRequestWrapper.getPinItemRequest() ?: return

        if (isApplicationScreenVisible) {
            applicationScreenSwipeY.animateTo(
                targetValue = screenHeight.toFloat(),
                animationSpec = tween(
                    easing = FastOutSlowInEasing,
                ),
            )
        }

        onUpdateGridItemSource(
            GridItemSource.Pin(pinItemRequest = pinItemRequest),
        )

        onUpdateMoveGridItemResult(
            MoveGridItemResult(
                isSuccess = false,
                movingGridItem = pinGridItem,
                conflictingGridItem = null,
            ),
        )

        onUpdateIsVisibleOverlay(true)

        isDragging = true
    }

    fun handleHasDoubleTap() {
        if (!hasDoubleTap) return

        handleEblanAction(
            context = context,
            eblanAction = gestureSettings.doubleTap,
            launcherApps = androidLauncherAppsWrapper,
            onOpenAppDrawer = ::openApplicationScreen,
        )

        hasDoubleTap = false
    }

    fun dragStart(offset: Offset) {
        drag = Drag.Start

        dragIntOffset = offset.round()

        accumulatedDragOffset = Offset.Zero
    }

    fun drag(dragAmount: Offset) {
        accumulatedDragOffset += dragAmount

        if (accumulatedDragOffset.getDistance() >= touchSlop) {
            drag = Drag.Dragging
        }

        dragIntOffset += dragAmount.round()

        overlayIntOffset = overlayIntOffset?.plus(dragAmount.round())
    }

    fun updateOverlayBounds(
        intOffset: IntOffset,
        intSize: IntSize,
    ) {
        overlayIntOffset = intOffset

        overlayIntSize = intSize
    }

    fun resetOverlay() {
        overlayImageBitmap = null

        sharedElementKey = null

        overlayIntOffset = null

        overlayIntSize = null

        drag = Drag.None
    }

    fun updateHasDoubleTap(value: Boolean) {
        hasDoubleTap = value
    }

    fun showGridItemPopup(
        intOffset: IntOffset,
        intSize: IntSize,
    ) {
        popupIntOffset = intOffset

        popupIntSize = intSize

        showGridItemPopup = true
    }

    fun dismissGridItemPopup() {
        popupIntOffset = null

        popupIntSize = null

        showGridItemPopup = false

        isCloseGridItemPopup = false
    }

    fun showFolderGridItemPopup(
        intOffset: IntOffset,
        intSize: IntSize,
    ) {
        popupIntOffset = intOffset

        popupIntSize = intSize

        showFolderGridItemPopup = true
    }

    fun dismissFolderGridItemPopup() {
        popupIntOffset = null

        popupIntSize = null

        showFolderGridItemPopup = false

        isCloseFolderGridItemPopup = false
    }

    fun updateIsDragging(value: Boolean) {
        isDragging = value
    }

    fun updateIsResizing(value: Boolean) {
        isResizing = value
    }

    fun updateOverlayImageBitmap(value: ImageBitmap?) {
        overlayImageBitmap = value
    }

    fun updateDrag(value: Drag) {
        drag = value
    }

    fun updateSharedElementKey(value: SharedElementKey?) {
        sharedElementKey = value
    }

    fun verticalDragStart() {
        showApplicationScreen =
            gestureSettings.swipeUp.eblanActionType == EblanActionType.OpenAppDrawer ||
            gestureSettings.swipeDown.eblanActionType == EblanActionType.OpenAppDrawer
    }

    fun verticalDrag(dragAmount: Float) {
        scope.launch {
            swipeUpY.snapTo(swipeUpY.value + dragAmount)

            swipeDownY.snapTo(swipeDownY.value - dragAmount)
        }
    }

    fun verticalDragEnd() {
        val swipeThreshold = 100f

        suspend fun animateSwipeY(
            eblanAction: EblanAction,
            swipeY: Animatable<Float, AnimationVector1D>,
        ) {
            val targetValue = if (eblanAction.eblanActionType == EblanActionType.OpenAppDrawer &&
                swipeY.value < screenHeight - swipeThreshold
            ) {
                0f
            } else {
                screenHeight.toFloat()
            }

            swipeY.animateTo(
                targetValue = targetValue,
                animationSpec = tween(
                    easing = FastOutSlowInEasing,
                ),
            )
        }

        if (swipeUpY.value < screenHeight - swipeThreshold) {
            handleEblanAction(
                context = context,
                eblanAction = gestureSettings.swipeUp,
                launcherApps = androidLauncherAppsWrapper,
                onOpenAppDrawer = {},
            )
        }

        if (swipeDownY.value < screenHeight - swipeThreshold) {
            handleEblanAction(
                context = context,
                eblanAction = gestureSettings.swipeDown,
                launcherApps = androidLauncherAppsWrapper,
                onOpenAppDrawer = {},
            )
        }

        scope.launch {
            animateSwipeY(
                eblanAction = gestureSettings.swipeUp,
                swipeY = swipeUpY,
            )

            animateSwipeY(
                eblanAction = gestureSettings.swipeDown,
                swipeY = swipeDownY,
            )

            if (applicationScreenSwipeY.value == screenHeight.toFloat()) {
                showApplicationScreen = false
            }
        }
    }

    fun verticalDragCancel() {
        scope.launch {
            swipeUpY.animateTo(screenHeight.toFloat())

            swipeDownY.animateTo(screenHeight.toFloat())
        }
    }

    fun showSettingsPopup(offset: Offset) {
        settingsPopupIntOffset = offset.round()

        showSettingsPopup = true
    }

    fun dismissSettingsPopup() {
        settingsPopupIntOffset = null

        showSettingsPopup = false
    }

    fun openApplicationScreen() {
        scope.launch {
            showApplicationScreen = true

            applicationScreenSwipeY.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessLow,
                ),
            )
        }
    }

    fun dismissApplicationScreen() {
        scope.launch {
            applicationScreenSwipeY.animateTo(
                targetValue = screenHeight.toFloat(),
                animationSpec = tween(
                    easing = FastOutSlowInEasing,
                ),
            )

            showApplicationScreen = false
        }
    }

    fun verticalDragApplicationScreen(dragAmount: Float) {
        scope.launch {
            applicationScreenSwipeY.snapTo(
                (applicationScreenSwipeY.value + dragAmount).coerceIn(
                    0f,
                    screenHeight.toFloat(),
                ),
            )
        }
    }

    fun openWidgetScreen() {
        scope.launch {
            showWidgetScreen = true

            widgetScreenSwipeY.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    easing = FastOutSlowInEasing,
                ),
            )
        }
    }

    fun dismissWidgetScreen() {
        scope.launch {
            widgetScreenSwipeY.animateTo(
                targetValue = screenHeight.toFloat(),
                animationSpec = tween(
                    easing = FastOutSlowInEasing,
                ),
            )

            showWidgetScreen = false
        }
    }

    fun verticalDragWidgetScreen(dragAmount: Float) {
        scope.launch {
            widgetScreenSwipeY.snapTo(
                (widgetScreenSwipeY.value + dragAmount).coerceIn(
                    0f,
                    screenHeight.toFloat(),
                ),
            )
        }
    }

    fun verticalDragShortcutConfigScreen(dragAmount: Float) {
        scope.launch {
            shortcutConfigScreenSwipeY.snapTo(
                (shortcutConfigScreenSwipeY.value + dragAmount).coerceIn(
                    0f,
                    screenHeight.toFloat(),
                ),
            )
        }
    }

    fun openShortcutConfigScreen() {
        scope.launch {
            showShortcutConfigScreen = true

            shortcutConfigScreenSwipeY.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    easing = FastOutSlowInEasing,
                ),
            )
        }
    }

    fun dismissShortcutConfigScreen() {
        scope.launch {
            shortcutConfigScreenSwipeY.animateTo(
                targetValue = screenHeight.toFloat(),
                animationSpec = tween(
                    easing = FastOutSlowInEasing,
                ),
            )

            showShortcutConfigScreen = false
        }
    }

    fun dismissAppWidgetScreen() {
        scope.launch {
            appWidgetScreenSwipeY.animateTo(
                targetValue = screenHeight.toFloat(),
                animationSpec = tween(
                    easing = FastOutSlowInEasing,
                ),
            )

            eblanApplicationInfoGroup = null
        }
    }

    fun openAppWidgetScreen(value: EblanApplicationInfoGroup) {
        scope.launch {
            eblanApplicationInfoGroup = value

            appWidgetScreenSwipeY.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    easing = FastOutSlowInEasing,
                ),
            )
        }
    }

    fun verticalDragAppWidgetScreen(dragAmount: Float) {
        scope.launch {
            appWidgetScreenSwipeY.snapTo(
                (appWidgetScreenSwipeY.value + dragAmount).coerceIn(
                    0f,
                    screenHeight.toFloat(),
                ),
            )
        }
    }

    suspend fun handlePinItemRequest(pinItemRequest: PinItemRequest?) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O ||
            pinItemRequest == null
        ) {
            return
        }

        when (pinItemRequest.requestType) {
            PinItemRequest.REQUEST_TYPE_APPWIDGET -> {
                pinItemRequest.handleAppWidgetRequestType()
            }

            PinItemRequest.REQUEST_TYPE_SHORTCUT -> {
                pinItemRequest.handleRequestTypeShortcut()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun PinItemRequest.handleRequestTypeShortcut() {
        val shortcutInfo = shortcutInfo ?: return

        val serialNumber =
            androidUserManagerWrapper.getSerialNumberForUser(userHandle = shortcutInfo.userHandle)

        val icon = androidLauncherAppsWrapper.getShortcutBadgedIconDrawable(
            shortcutInfo = shortcutInfo,
            density = 0,
        )?.let {
            val directory = fileManager.getFilesDirectory(FileManager.SHORTCUTS_DIR)

            val file = File(
                directory,
                iconKeyGenerator.getShortcutIconKey(
                    serialNumber = serialNumber,
                    packageName = shortcutInfo.`package`,
                    id = shortcutInfo.id,
                ),
            )

            androidImageSerializer.createDrawablePath(
                drawable = it,
                file = file,
            )

            file.absolutePath
        }

        onGetPinGridItem(
            PinItemRequestType.ShortcutInfo(
                serialNumber = androidUserManagerWrapper.getSerialNumberForUser(
                    userHandle = shortcutInfo.userHandle,
                ),
                shortcutId = shortcutInfo.id,
                packageName = shortcutInfo.`package`,
                shortLabel = shortcutInfo.shortLabel.toString(),
                longLabel = shortcutInfo.longLabel.toString(),
                isEnabled = shortcutInfo.isEnabled,
                disabledMessage = shortcutInfo.disabledMessage?.toString(),
                icon = icon,
            ),
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun PinItemRequest.handleAppWidgetRequestType() {
        val appWidgetProviderInfo = getAppWidgetProviderInfo(context) ?: return

        val componentName = appWidgetProviderInfo.provider.flattenToString()

        val preview =
            appWidgetProviderInfo.loadPreviewImage(context, 0)?.let {
                val directory =
                    fileManager.getFilesDirectory(FileManager.WIDGETS_DIR)

                val file = File(
                    directory,
                    iconKeyGenerator.getHashedName(name = componentName),
                )

                androidImageSerializer.createDrawablePath(
                    drawable = it,
                    file = file,
                )

                file.absolutePath
            }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            onGetPinGridItem(
                PinItemRequestType.Widget(
                    appWidgetId = 0,
                    componentName = componentName,
                    packageName = appWidgetProviderInfo.provider.packageName,
                    serialNumber = androidUserManagerWrapper.getSerialNumberForUser(
                        userHandle = appWidgetProviderInfo.profile,
                    ),
                    configure = appWidgetProviderInfo.configure.flattenToString(),
                    minWidth = appWidgetProviderInfo.minWidth,
                    minHeight = appWidgetProviderInfo.minHeight,
                    resizeMode = appWidgetProviderInfo.resizeMode,
                    minResizeWidth = appWidgetProviderInfo.minResizeWidth,
                    minResizeHeight = appWidgetProviderInfo.minResizeHeight,
                    maxResizeWidth = appWidgetProviderInfo.maxResizeWidth,
                    maxResizeHeight = appWidgetProviderInfo.maxResizeHeight,
                    targetCellHeight = appWidgetProviderInfo.targetCellHeight,
                    targetCellWidth = appWidgetProviderInfo.targetCellWidth,
                    preview = preview,
                ),
            )
        } else {
            onGetPinGridItem(
                PinItemRequestType.Widget(
                    appWidgetId = 0,
                    componentName = appWidgetProviderInfo.provider.flattenToString(),
                    packageName = appWidgetProviderInfo.provider.packageName,
                    serialNumber = androidUserManagerWrapper.getSerialNumberForUser(
                        userHandle = appWidgetProviderInfo.profile,
                    ),
                    configure = appWidgetProviderInfo.configure.flattenToString(),
                    minWidth = appWidgetProviderInfo.minWidth,
                    minHeight = appWidgetProviderInfo.minHeight,
                    resizeMode = appWidgetProviderInfo.resizeMode,
                    minResizeWidth = appWidgetProviderInfo.minResizeWidth,
                    minResizeHeight = appWidgetProviderInfo.minResizeHeight,
                    maxResizeWidth = 0,
                    maxResizeHeight = 0,
                    targetCellHeight = 0,
                    targetCellWidth = 0,
                    preview = preview,
                ),
            )
        }
    }

    fun updateIsCloseGridItemPopup(value: Boolean) {
        isCloseGridItemPopup = value
    }

    fun updateIsCloseFolderGridItemPopup(value: Boolean) {
        isCloseFolderGridItemPopup = value
    }

    fun handleOnDragEndApplicationScreen() {
        scope.launch {
            if (applicationScreenSwipeY.value > 200f) {
                applicationScreenSwipeY.animateTo(
                    targetValue = screenHeight.toFloat(),
                    animationSpec = tween(easing = FastOutSlowInEasing),
                )

                showApplicationScreen = false
            } else {
                applicationScreenSwipeY.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessLow,
                    ),
                )
            }
        }
    }

    fun handleOnDragEndWidgetScreen() {
        scope.launch {
            if (widgetScreenSwipeY.value > 200f) {
                widgetScreenSwipeY.animateTo(
                    targetValue = screenHeight.toFloat(),
                    animationSpec = tween(easing = FastOutSlowInEasing),
                )

                showWidgetScreen = false
            } else {
                widgetScreenSwipeY.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessLow,
                    ),
                )
            }
        }
    }

    fun handleOnDragEndShortcutConfigScreen() {
        scope.launch {
            if (shortcutConfigScreenSwipeY.value > 200f) {
                shortcutConfigScreenSwipeY.animateTo(
                    targetValue = screenHeight.toFloat(),
                    animationSpec = tween(easing = FastOutSlowInEasing),
                )

                showShortcutConfigScreen = false
            } else {
                shortcutConfigScreenSwipeY.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessLow,
                    ),
                )
            }
        }
    }

    fun handleOnDragEndAppWidgetScreen() {
        scope.launch {
            if (appWidgetScreenSwipeY.value > 200f) {
                dismissAppWidgetScreen()
            } else {
                appWidgetScreenSwipeY.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(easing = FastOutSlowInEasing),
                )
            }
        }
    }

    suspend fun handleWallpaperScrollEffect(
        horizontalPagerState: PagerState,
        windowToken: IBinder,
    ) {
        if (!homeSettings.wallpaperScroll || homeSettings.pageCount <= 1) return

        var reverseXOffset: Float

        snapshotFlow { horizontalPagerState.currentPageOffsetFraction }.onStart {
            androidWallpaperManagerWrapper.setWallpaperOffsetSteps(
                xStep = 1f / (homeSettings.pageCount - 1),
                yStep = 1f,
            )
        }.collect { offsetFraction ->
            val page = calculatePage(
                index = horizontalPagerState.currentPage,
                infiniteScroll = homeSettings.infiniteScroll,
                pageCount = homeSettings.pageCount,
            )

            val scrollProgress = page + offsetFraction

            if (scrollProgress < 0f) {
                reverseXOffset = offsetFraction.absoluteValue

                androidWallpaperManagerWrapper.setWallpaperOffsets(
                    windowToken = windowToken,
                    xOffset = reverseXOffset,
                    yOffset = 0f,
                )
            } else if (scrollProgress > homeSettings.pageCount - 1) {
                reverseXOffset = 1f - offsetFraction

                androidWallpaperManagerWrapper.setWallpaperOffsets(
                    windowToken = windowToken,
                    xOffset = reverseXOffset,
                    yOffset = 0f,
                )
            } else {
                val xOffset = scrollProgress / (homeSettings.pageCount - 1)

                androidWallpaperManagerWrapper.setWallpaperOffsets(
                    windowToken = windowToken,
                    xOffset = xOffset,
                    yOffset = 0f,
                )
            }

            if (offsetFraction == 0f) {
                reverseXOffset = offsetFraction
            }
        }
    }

    fun handleSystemNavigation(
        dockGridHorizontalPagerState: PagerState,
        gridHorizontalPagerState: PagerState,
        windowToken: IBinder,
    ) {
        fun getInfiniteScrollInitialPage(
            currentPage: Int,
            initialPage: Int,
            pageCount: Int,
            center: Int = Int.MAX_VALUE / 2,
        ): Int {
            var diff = initialPage - Math.floorMod(currentPage - center, pageCount)

            val halfCount = pageCount / 2

            if (diff > halfCount) {
                diff -= pageCount
            } else if (diff < -halfCount) {
                diff += pageCount
            }

            return currentPage + diff
        }

        scope.launch {
            gridHorizontalPagerState.animateScrollToPage(
                if (homeSettings.infiniteScroll) {
                    getInfiniteScrollInitialPage(
                        currentPage = gridHorizontalPagerState.currentPage,
                        initialPage = homeSettings.initialPage,
                        pageCount = homeSettings.pageCount,
                    )
                } else {
                    homeSettings.initialPage
                },
            )
        }

        scope.launch {
            dockGridHorizontalPagerState.animateScrollToPage(
                if (homeSettings.dockInfiniteScroll) {
                    getInfiniteScrollInitialPage(
                        currentPage = dockGridHorizontalPagerState.currentPage,
                        initialPage = homeSettings.dockInitialPage,
                        pageCount = homeSettings.dockPageCount,
                    )
                } else {
                    homeSettings.dockInitialPage
                },
            )
        }

        if (homeSettings.wallpaperScroll && homeSettings.pageCount > 1) {
            val page = calculatePage(
                index = gridHorizontalPagerState.currentPage,
                infiniteScroll = homeSettings.infiniteScroll,
                pageCount = homeSettings.pageCount,
            )

            androidWallpaperManagerWrapper.setWallpaperOffsetSteps(
                xStep = 1f / (homeSettings.pageCount - 1),
                yStep = 1f,
            )

            androidWallpaperManagerWrapper.setWallpaperOffsets(
                windowToken = windowToken,
                xOffset = page.toFloat() / (homeSettings.pageCount - 1),
                yOffset = 0f,
            )
        }
    }

    fun handleEblanActionIntent(intent: Intent) {
        if (intent.action != EblanAction.ACTION || !isAvailableSystemNavigation) return

        val eblanAction = intent.getStringExtra(EblanAction.NAME)?.let {
            Json.decodeFromString<EblanAction>(it)
        } ?: return

        handleEblanAction(
            context = context,
            eblanAction = eblanAction,
            launcherApps = androidLauncherAppsWrapper,
            onOpenAppDrawer = ::openApplicationScreen,
        )
    }

    fun updateAssociate(value: Associate) {
        associate = value
    }

    fun updateLastAppWidgetId(value: Int) {
        lastAppWidgetId = value
    }

    fun updateWidgetGridItem(value: GridItem) {
        widgetGridItem = value
    }

    fun updateGridPageDirection(value: PageDirection?) {
        gridPageDirection = value
    }

    fun updateDockPageDirection(value: PageDirection?) {
        dockPageDirection = value
    }

    fun updateIsVisibleFolders(value: Boolean) {
        isVisibleFolders = value
    }
}

@Composable
internal fun rememberPagerScreenState(
    gestureSettings: GestureSettings,
    homeSettings: HomeSettings,
    screenHeight: Int,
    experimentalSettings: ExperimentalSettings,
    onGetPinGridItem: (PinItemRequestType) -> Unit,
    onResetPinGridItem: () -> Unit,
): PagerScreenState {
    val scope = rememberCoroutineScope()

    val context = LocalContext.current

    val androidLauncherAppsWrapper = LocalLauncherApps.current

    val androidWallpaperManagerWrapper = LocalWallpaperManager.current

    val density = LocalDensity.current

    val androidUserManagerWrapper = LocalUserManager.current

    val androidImageSerializer = LocalImageSerializer.current

    val fileManager = LocalFileManager.current

    val pinItemRequestWrapper = LocalPinItemRequest.current

    val iconKeyGenerator = LocalIconKeyGenerator.current

    return remember(
        screenHeight,
        gestureSettings,
        homeSettings,
        experimentalSettings,
    ) {
        PagerScreenState(
            density = density,
            screenHeight = screenHeight,
            fileManager = fileManager,
            androidImageSerializer = androidImageSerializer,
            androidLauncherAppsWrapper = androidLauncherAppsWrapper,
            scope = scope,
            context = context,
            androidUserManagerWrapper = androidUserManagerWrapper,
            pinItemRequestWrapper = pinItemRequestWrapper,
            gestureSettings = gestureSettings,
            homeSettings = homeSettings,
            androidWallpaperManagerWrapper = androidWallpaperManagerWrapper,
            iconKeyGenerator = iconKeyGenerator,
            onGetPinGridItem = onGetPinGridItem,
            onResetPinGridItem = onResetPinGridItem,
        )
    }
}
