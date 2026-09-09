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
import com.eblan.launcher.domain.model.EblanAction
import com.eblan.launcher.domain.model.EblanActionType
import com.eblan.launcher.domain.model.EblanApplicationInfo
import com.eblan.launcher.ui.R
import com.eblan.launcher.ui.dialog.EblanActionDialog
import com.eblan.launcher.ui.model.SettingsItem
import com.eblan.launcher.common.R as commonR

@Composable
fun EblanActionSettings(
    modifier: Modifier = Modifier,
    doubleTap: EblanAction,
    swipeUp: EblanAction,
    swipeDown: EblanAction,
    eblanApplicationInfos: List<EblanApplicationInfo>,
    onUpdateDoubleTap: (EblanAction) -> Unit,
    onUpdateSwipeUp: (EblanAction) -> Unit,
    onUpdateSwipeDown: (EblanAction) -> Unit,
) {
    val context = LocalContext.current

    var showDoubleTapDialog by remember { mutableStateOf(false) }

    var showSwipeUpDialog by remember { mutableStateOf(false) }

    var showSwipeDownDialog by remember { mutableStateOf(false) }

    val items = buildEblanActionSettingsItems(
        context = context,
        doubleTap = doubleTap,
        swipeUp = swipeUp,
        swipeDown = swipeDown,
        onDoubleTapClick = {
            showDoubleTapDialog = true
        },
        onSwipeUpClick = {
            showSwipeUpDialog = true
        },
        onSwipeDownClick = {
            showSwipeDownDialog = true
        },
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        SettingsItems(items = items)
    }

    if (showDoubleTapDialog) {
        EblanActionDialog(
            title = stringResource(R.string.double_tap),
            eblanAction = doubleTap,
            eblanApplicationInfos = eblanApplicationInfos,
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
            title = stringResource(R.string.swipe_up),
            eblanAction = swipeUp,
            eblanApplicationInfos = eblanApplicationInfos,
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
            title = stringResource(R.string.swipe_down),
            eblanAction = swipeDown,
            eblanApplicationInfos = eblanApplicationInfos,
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
private fun buildEblanActionSettingsItems(
    context: Context,
    doubleTap: EblanAction,
    swipeUp: EblanAction,
    swipeDown: EblanAction,
    onDoubleTapClick: () -> Unit,
    onSwipeUpClick: () -> Unit,
    onSwipeDownClick: () -> Unit,
): List<SettingsItem> = buildList {
    add(
        SettingsItem.Column(
            title = stringResource(R.string.double_tap),
            subtitle = doubleTap.eblanActionType.getEblanActionTypeSubtitle(
                context = context,
                componentName = doubleTap.componentName,
            ),
            onClick = onDoubleTapClick,
        ),
    )

    add(
        SettingsItem.Column(
            title = stringResource(R.string.swipe_up),
            subtitle = swipeUp.eblanActionType.getEblanActionTypeSubtitle(
                context = context,
                componentName = swipeUp.componentName,
            ),
            onClick = onSwipeUpClick,
        ),
    )

    add(
        SettingsItem.Column(
            title = stringResource(R.string.swipe_down),
            subtitle = swipeDown.eblanActionType.getEblanActionTypeSubtitle(
                context = context,
                componentName = swipeDown.componentName,
            ),
            onClick = onSwipeDownClick,
        ),
    )
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
}
