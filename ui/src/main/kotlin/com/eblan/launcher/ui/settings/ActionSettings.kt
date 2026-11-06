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
package com.eblan.launcher.ui.settings

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.eblan.launcher.domain.model.application.EblanApplicationInfo
import com.eblan.launcher.domain.model.userdata.EblanAction
import com.eblan.launcher.domain.model.userdata.EblanActionType
import com.eblan.launcher.domain.model.userdata.GestureSettings
import com.eblan.launcher.ui.R
import com.eblan.launcher.ui.dialog.EblanActionDialog
import com.eblan.launcher.common.R as commonR

@Composable
fun GridItemActionSettings(
    // Modifier
    modifier: Modifier = Modifier,
    // Actions
    doubleTap: EblanAction,
    swipeUp: EblanAction,
    swipeDown: EblanAction,
    // Application Info
    eblanApplicationInfos: List<EblanApplicationInfo>,
    // Action Callbacks
    onUpdateDoubleTap: (EblanAction) -> Unit,
    onUpdateSwipeUp: (EblanAction) -> Unit,
    onUpdateSwipeDown: (EblanAction) -> Unit,
) {
    val context = LocalContext.current

    var showDoubleTapDialog by remember { mutableStateOf(false) }

    var showSwipeUpDialog by remember { mutableStateOf(false) }

    var showSwipeDownDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        SettingsColumnItem(
            // Content
            title = stringResource(R.string.double_tap),
            subtitle = doubleTap.eblanActionType.getEblanActionTypeSubtitle(
                context = context,
                componentName = doubleTap.componentName,
            ),
            // Layout
            shape = settingsFirstItemShape,
            // Actions
            onClick = {
                showDoubleTapDialog = true
            },
        )

        SettingsColumnItem(
            // Content
            title = stringResource(R.string.swipe_up),
            subtitle = swipeUp.eblanActionType.getEblanActionTypeSubtitle(
                context = context,
                componentName = swipeUp.componentName,
            ),
            // Layout
            shape = settingsMiddleItemShape,
            // Actions
            onClick = {
                showSwipeUpDialog = true
            },
        )

        SettingsColumnItem(
            // Content
            title = stringResource(R.string.swipe_down),
            subtitle = swipeDown.eblanActionType.getEblanActionTypeSubtitle(
                context = context,
                componentName = swipeDown.componentName,
            ),
            // Layout
            shape = settingsLastItemShape,
            // Actions
            onClick = {
                showSwipeDownDialog = true
            },
        )
    }

    if (showDoubleTapDialog) {
        EblanActionDialog(
            // Content
            title = stringResource(R.string.double_tap),
            eblanAction = doubleTap,
            eblanApplicationInfos = eblanApplicationInfos,
            // Callbacks
            onSelectEblanAction = {
                onUpdateDoubleTap(it)

                showDoubleTapDialog = false
            },
            onDismissRequest = {
                showDoubleTapDialog = false
            },
        )
    }

    if (showSwipeUpDialog) {
        EblanActionDialog(
            // Content
            title = stringResource(R.string.swipe_up),
            eblanAction = swipeUp,
            eblanApplicationInfos = eblanApplicationInfos,
            // Callbacks
            onSelectEblanAction = {
                onUpdateSwipeUp(it)

                showSwipeUpDialog = false
            },
            onDismissRequest = {
                showSwipeUpDialog = false
            },
        )
    }

    if (showSwipeDownDialog) {
        EblanActionDialog(
            // Content
            title = stringResource(R.string.swipe_down),
            eblanAction = swipeDown,
            eblanApplicationInfos = eblanApplicationInfos,
            // Callbacks
            onSelectEblanAction = {
                onUpdateSwipeDown(it)

                showSwipeDownDialog = false
            },
            onDismissRequest = {
                showSwipeDownDialog = false
            },
        )
    }
}

@Composable
fun GridActionSettings(
    // Modifier
    modifier: Modifier = Modifier,
    // Settings
    gestureSettings: GestureSettings,
    // Application Info
    eblanApplicationInfos: List<EblanApplicationInfo>,
    // Callbacks
    onUpdateGestureSettings: (GestureSettings) -> Unit,
) {
    val context = LocalContext.current

    var showDoubleTapDialog by remember { mutableStateOf(false) }

    var showSwipeUpDialog by remember { mutableStateOf(false) }

    var showSwipeDownDialog by remember { mutableStateOf(false) }

    var showSwipeLeftDialog by remember { mutableStateOf(false) }

    var showSwipeRightDialog by remember { mutableStateOf(false) }

    var showDoubleTapSwipeUpDialog by remember { mutableStateOf(false) }

    var showDoubleTapSwipeDownDialog by remember { mutableStateOf(false) }

    var showTwoFingerSwipeUpDialog by remember { mutableStateOf(false) }

    var showTwoFingerSwipeDownDialog by remember { mutableStateOf(false) }

    var showPinchInDialog by remember { mutableStateOf(false) }

    var showPinchOutDialog by remember { mutableStateOf(false) }

    var showRotateCounterClockwiseDialog by remember { mutableStateOf(false) }

    var showRotateClockwiseDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        SettingsColumnItem(
            // Content
            title = stringResource(R.string.double_tap),
            subtitle = gestureSettings.doubleTap.eblanActionType.getEblanActionTypeSubtitle(
                context = context,
                componentName = gestureSettings.doubleTap.componentName,
            ),
            // Layout
            shape = settingsFirstItemShape,
            // Actions
            onClick = {
                showDoubleTapDialog = true
            },
        )

        SettingsColumnItem(
            // Content
            title = stringResource(R.string.swipe_up),
            subtitle = gestureSettings.swipeUp.eblanActionType.getEblanActionTypeSubtitle(
                context = context,
                componentName = gestureSettings.swipeUp.componentName,
            ),
            // Layout
            shape = settingsMiddleItemShape,
            // Actions
            onClick = {
                showSwipeUpDialog = true
            },
        )

        SettingsColumnItem(
            // Content
            title = stringResource(R.string.swipe_down),
            subtitle = gestureSettings.swipeDown.eblanActionType.getEblanActionTypeSubtitle(
                context = context,
                componentName = gestureSettings.swipeDown.componentName,
            ),
            // Layout
            shape = settingsMiddleItemShape,
            // Actions
            onClick = {
                showSwipeDownDialog = true
            },
        )

        SettingsColumnItem(
            // Content
            title = stringResource(R.string.swipe_left),
            subtitle = gestureSettings.swipeLeft.eblanActionType.getEblanActionTypeSubtitle(
                context = context,
                componentName = gestureSettings.swipeLeft.componentName,
            ),
            // Layout
            shape = settingsMiddleItemShape,
            // Actions
            onClick = {
                showSwipeLeftDialog = true
            },
        )

        SettingsColumnItem(
            // Content
            title = stringResource(R.string.swipe_right),
            subtitle = gestureSettings.swipeRight.eblanActionType.getEblanActionTypeSubtitle(
                context = context,
                componentName = gestureSettings.swipeRight.componentName,
            ),
            // Layout
            shape = settingsMiddleItemShape,
            // Actions
            onClick = {
                showSwipeRightDialog = true
            },
        )

        SettingsColumnItem(
            // Content
            title = stringResource(R.string.double_tap_swipe_up),
            subtitle = gestureSettings.doubleTapSwipeUp.eblanActionType.getEblanActionTypeSubtitle(
                context = context,
                componentName = gestureSettings.doubleTapSwipeUp.componentName,
            ),
            // Layout
            shape = settingsMiddleItemShape,
            // Actions
            onClick = {
                showDoubleTapSwipeUpDialog = true
            },
        )

        SettingsColumnItem(
            // Content
            title = stringResource(R.string.double_tap_swipe_down),
            subtitle = gestureSettings.doubleTapSwipeDown.eblanActionType.getEblanActionTypeSubtitle(
                context = context,
                componentName = gestureSettings.doubleTapSwipeDown.componentName,
            ),
            // Layout
            shape = settingsMiddleItemShape,
            // Actions
            onClick = {
                showDoubleTapSwipeDownDialog = true
            },
        )

        SettingsColumnItem(
            // Content
            title = stringResource(R.string.two_finger_swipe_up),
            subtitle = gestureSettings.twoFingerSwipeUp.eblanActionType.getEblanActionTypeSubtitle(
                context = context,
                componentName = gestureSettings.twoFingerSwipeUp.componentName,
            ),
            // Layout
            shape = settingsMiddleItemShape,
            // Actions
            onClick = {
                showTwoFingerSwipeUpDialog = true
            },
        )

        SettingsColumnItem(
            // Content
            title = stringResource(R.string.two_finger_swipe_down),
            subtitle = gestureSettings.twoFingerSwipeDown.eblanActionType.getEblanActionTypeSubtitle(
                context = context,
                componentName = gestureSettings.twoFingerSwipeDown.componentName,
            ),
            // Layout
            shape = settingsMiddleItemShape,
            // Actions
            onClick = {
                showTwoFingerSwipeDownDialog = true
            },
        )

        SettingsColumnItem(
            // Content
            title = stringResource(R.string.pinch_in),
            subtitle = gestureSettings.pinchIn.eblanActionType.getEblanActionTypeSubtitle(
                context = context,
                componentName = gestureSettings.pinchIn.componentName,
            ),
            // Layout
            shape = settingsMiddleItemShape,
            // Actions
            onClick = {
                showPinchInDialog = true
            },
        )

        SettingsColumnItem(
            // Content
            title = stringResource(R.string.pinch_out),
            subtitle = gestureSettings.pinchOut.eblanActionType.getEblanActionTypeSubtitle(
                context = context,
                componentName = gestureSettings.pinchOut.componentName,
            ),
            // Layout
            shape = settingsMiddleItemShape,
            // Actions
            onClick = {
                showPinchOutDialog = true
            },
        )

        SettingsColumnItem(
            // Content
            title = stringResource(R.string.rotate_counter_clockwise),
            subtitle = gestureSettings.rotateCounterClockwise.eblanActionType.getEblanActionTypeSubtitle(
                context = context,
                componentName = gestureSettings.rotateCounterClockwise.componentName,
            ),
            // Layout
            shape = settingsMiddleItemShape,
            // Actions
            onClick = {
                showRotateCounterClockwiseDialog = true
            },
        )

        SettingsColumnItem(
            // Content
            title = stringResource(R.string.rotate_clockwise),
            subtitle = gestureSettings.rotateClockwise.eblanActionType.getEblanActionTypeSubtitle(
                context = context,
                componentName = gestureSettings.rotateClockwise.componentName,
            ),
            // Layout
            shape = settingsLastItemShape,
            // Actions
            onClick = {
                showRotateClockwiseDialog = true
            },
        )
    }

    if (showDoubleTapDialog) {
        EblanActionDialog(
            // Content
            title = stringResource(R.string.double_tap),
            eblanAction = gestureSettings.doubleTap,
            eblanApplicationInfos = eblanApplicationInfos,
            // Callbacks
            onSelectEblanAction = {
                onUpdateGestureSettings(gestureSettings.copy(doubleTap = it))

                showDoubleTapDialog = false
            },
            onDismissRequest = {
                showDoubleTapDialog = false
            },
        )
    }

    if (showSwipeUpDialog) {
        EblanActionDialog(
            // Content
            title = stringResource(R.string.swipe_up),
            eblanAction = gestureSettings.swipeUp,
            eblanApplicationInfos = eblanApplicationInfos,
            // Callbacks
            onSelectEblanAction = {
                onUpdateGestureSettings(gestureSettings.copy(swipeUp = it))

                showSwipeUpDialog = false
            },
            onDismissRequest = {
                showSwipeUpDialog = false
            },
        )
    }

    if (showSwipeDownDialog) {
        EblanActionDialog(
            // Content
            title = stringResource(R.string.swipe_down),
            eblanAction = gestureSettings.swipeDown,
            eblanApplicationInfos = eblanApplicationInfos,
            // Callbacks
            onSelectEblanAction = {
                onUpdateGestureSettings(gestureSettings.copy(swipeDown = it))

                showSwipeDownDialog = false
            },
            onDismissRequest = {
                showSwipeDownDialog = false
            },
        )
    }

    if (showSwipeLeftDialog) {
        EblanActionDialog(
            // Content
            title = stringResource(R.string.swipe_left),
            eblanAction = gestureSettings.swipeLeft,
            eblanApplicationInfos = eblanApplicationInfos,
            // Callbacks
            onSelectEblanAction = {
                onUpdateGestureSettings(gestureSettings.copy(swipeLeft = it))

                showSwipeLeftDialog = false
            },
            onDismissRequest = {
                showSwipeLeftDialog = false
            },
        )
    }

    if (showSwipeRightDialog) {
        EblanActionDialog(
            // Content
            title = stringResource(R.string.swipe_right),
            eblanAction = gestureSettings.swipeRight,
            eblanApplicationInfos = eblanApplicationInfos,
            // Callbacks
            onSelectEblanAction = {
                onUpdateGestureSettings(gestureSettings.copy(swipeRight = it))

                showSwipeRightDialog = false
            },
            onDismissRequest = {
                showSwipeRightDialog = false
            },
        )
    }

    if (showDoubleTapSwipeUpDialog) {
        EblanActionDialog(
            // Content
            title = stringResource(R.string.double_tap_swipe_up),
            eblanAction = gestureSettings.doubleTapSwipeUp,
            eblanApplicationInfos = eblanApplicationInfos,
            // Callbacks
            onSelectEblanAction = {
                onUpdateGestureSettings(gestureSettings.copy(doubleTapSwipeUp = it))

                showDoubleTapSwipeUpDialog = false
            },
            onDismissRequest = {
                showDoubleTapSwipeUpDialog = false
            },
        )
    }

    if (showDoubleTapSwipeDownDialog) {
        EblanActionDialog(
            // Content
            title = stringResource(R.string.double_tap_swipe_down),
            eblanAction = gestureSettings.doubleTapSwipeDown,
            eblanApplicationInfos = eblanApplicationInfos,
            // Callbacks
            onSelectEblanAction = {
                onUpdateGestureSettings(gestureSettings.copy(doubleTapSwipeDown = it))

                showDoubleTapSwipeDownDialog = false
            },
            onDismissRequest = {
                showDoubleTapSwipeDownDialog = false
            },
        )
    }

    if (showTwoFingerSwipeUpDialog) {
        EblanActionDialog(
            // Content
            title = stringResource(R.string.two_finger_swipe_up),
            eblanAction = gestureSettings.twoFingerSwipeUp,
            eblanApplicationInfos = eblanApplicationInfos,
            // Callbacks
            onSelectEblanAction = {
                onUpdateGestureSettings(gestureSettings.copy(twoFingerSwipeUp = it))

                showTwoFingerSwipeUpDialog = false
            },
            onDismissRequest = {
                showTwoFingerSwipeUpDialog = false
            },
        )
    }

    if (showTwoFingerSwipeDownDialog) {
        EblanActionDialog(
            // Content
            title = stringResource(R.string.two_finger_swipe_down),
            eblanAction = gestureSettings.twoFingerSwipeDown,
            eblanApplicationInfos = eblanApplicationInfos,
            // Callbacks
            onSelectEblanAction = {
                onUpdateGestureSettings(gestureSettings.copy(twoFingerSwipeDown = it))

                showTwoFingerSwipeDownDialog = false
            },
            onDismissRequest = {
                showTwoFingerSwipeDownDialog = false
            },
        )
    }

    if (showPinchInDialog) {
        EblanActionDialog(
            // Content
            title = stringResource(R.string.pinch_in),
            eblanAction = gestureSettings.pinchIn,
            eblanApplicationInfos = eblanApplicationInfos,
            // Callbacks
            onSelectEblanAction = {
                onUpdateGestureSettings(gestureSettings.copy(pinchIn = it))

                showPinchInDialog = false
            },
            onDismissRequest = {
                showPinchInDialog = false
            },
        )
    }

    if (showPinchOutDialog) {
        EblanActionDialog(
            // Content
            title = stringResource(R.string.pinch_out),
            eblanAction = gestureSettings.pinchOut,
            eblanApplicationInfos = eblanApplicationInfos,
            // Callbacks
            onSelectEblanAction = {
                onUpdateGestureSettings(gestureSettings.copy(pinchOut = it))

                showPinchOutDialog = false
            },
            onDismissRequest = {
                showPinchOutDialog = false
            },
        )
    }

    if (showRotateCounterClockwiseDialog) {
        EblanActionDialog(
            // Content
            title = stringResource(R.string.rotate_counter_clockwise),
            eblanAction = gestureSettings.rotateCounterClockwise,
            eblanApplicationInfos = eblanApplicationInfos,
            // Callbacks
            onSelectEblanAction = {
                onUpdateGestureSettings(gestureSettings.copy(rotateCounterClockwise = it))

                showRotateCounterClockwiseDialog = false
            },
            onDismissRequest = {
                showRotateCounterClockwiseDialog = false
            },
        )
    }

    if (showRotateClockwiseDialog) {
        EblanActionDialog(
            // Content
            title = stringResource(R.string.rotate_clockwise),
            eblanAction = gestureSettings.rotateClockwise,
            eblanApplicationInfos = eblanApplicationInfos,
            // Callbacks
            onSelectEblanAction = {
                onUpdateGestureSettings(gestureSettings.copy(rotateClockwise = it))

                showRotateClockwiseDialog = false
            },
            onDismissRequest = {
                showRotateClockwiseDialog = false
            },
        )
    }
}

fun EblanActionType.getEblanActionTypeSubtitle(
    context: Context,
    componentName: String,
): String = when (this) {
    EblanActionType.None -> context.getString(commonR.string.none)

    EblanActionType.OpenApp -> context.getString(
        R.string.open,
        componentName.ifBlank {
            context.getString(
                R.string.app,
            )
        },
    )

    EblanActionType.OpenAppDrawer -> context.getString(R.string.open_app_drawer)

    EblanActionType.OpenNotificationPanel -> context.getString(R.string.open_notification_panel)

    EblanActionType.LockScreen -> context.getString(R.string.lock_screen)

    EblanActionType.OpenQuickSettings -> context.getString(R.string.open_quick_settings)

    EblanActionType.OpenRecents -> context.getString(R.string.open_recents)

    EblanActionType.OpenNotificationPanelByReflection -> context.getString(R.string.open_notification_panel_by_reflection)
}
