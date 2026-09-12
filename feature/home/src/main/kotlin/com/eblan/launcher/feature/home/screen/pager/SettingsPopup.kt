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

import android.content.Intent
import android.content.Intent.ACTION_SET_WALLPAPER
import android.content.Intent.createChooser
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.domain.model.grid.Associate
import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.feature.home.R
import com.eblan.launcher.feature.home.component.HomeHandler
import com.eblan.launcher.feature.home.model.SettingsMenuItem
import com.eblan.launcher.ui.settings.settingsItemShape
import com.eblan.launcher.common.R as commonR

@Composable
internal fun SettingsPopup(
    gridItems: List<GridItem>,
    hasSystemFeatureAppWidgets: Boolean,
    popupSettingsIntOffset: IntOffset?,
    onDismissRequest: () -> Unit,
    onEditPage: (
        gridItems: List<GridItem>,
        associate: Associate,
    ) -> Unit,
    onSettings: () -> Unit,
    onShortcutConfigActivities: () -> Unit,
    onWidgets: () -> Unit,
) {
    requireNotNull(popupSettingsIntOffset)

    val context = LocalContext.current

    val transitionState = remember {
        MutableTransitionState(false).apply {
            targetState = true
        }
    }

    val items = buildSettingsMenuItems(
        hasSystemFeatureAppWidgets = hasSystemFeatureAppWidgets,
        onSettingsClick = {
            onSettings()

            transitionState.targetState = false
        },
        onEditPageClick = {
            onEditPage(
                gridItems,
                Associate.Grid,
            )

            transitionState.targetState = false
        },
        onEditDockPageClick = {
            onEditPage(
                gridItems,
                Associate.Dock,
            )

            transitionState.targetState = false
        },
        onWidgetsClick = {
            onWidgets()

            transitionState.targetState = false
        },
        onShortcutConfigActivitiesClick = {
            onShortcutConfigActivities()

            transitionState.targetState = false
        },
        onWallpaperClick = {
            val intent = Intent(ACTION_SET_WALLPAPER)

            val chooser = createChooser(intent, "Set Wallpaper")

            context.startActivity(chooser)

            transitionState.targetState = false
        },
    )

    LaunchedEffect(
        key1 = transitionState.targetState,
        key2 = transitionState.isIdle,
    ) {
        if (!transitionState.targetState && transitionState.isIdle) {
            onDismissRequest()
        }
    }

    BackHandler(enabled = transitionState.targetState) {
        transitionState.targetState = false
    }

    HomeHandler(enabled = transitionState.targetState) {
        transitionState.targetState = false
    }

    Popup(
        popupPositionProvider = SettingsPopupPositionProvider(
            x = popupSettingsIntOffset.x,
            y = popupSettingsIntOffset.y,
        ),
        onDismissRequest = {
            transitionState.targetState = false
        },
    ) {
        AnimatedVisibility(
            visibleState = transitionState,
            enter = fadeIn(tween()) + scaleIn(initialScale = 0.8f, animationSpec = tween()),
            exit = fadeOut(tween()) + scaleOut(targetScale = 0.8f, animationSpec = tween()),
        ) {
            Column(
                modifier = Modifier.width(IntrinsicSize.Max),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                items.forEachIndexed { index, settingsMenuItem ->
                    PopupMenuRow(
                        index = index,
                        size = items.size,
                        imageVector = settingsMenuItem.imageVector,
                        title = settingsMenuItem.title,
                        onClick = settingsMenuItem.onClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun PopupMenuRow(
    modifier: Modifier = Modifier,
    index: Int,
    size: Int,
    imageVector: ImageVector,
    title: String,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max),
        shape = settingsItemShape(
            index = index,
            size = size,
        ),
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = null,
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(text = title)
        }
    }
}

@Composable
private fun buildSettingsMenuItems(
    hasSystemFeatureAppWidgets: Boolean,
    onSettingsClick: () -> Unit,
    onEditPageClick: () -> Unit,
    onEditDockPageClick: () -> Unit,
    onWidgetsClick: () -> Unit,
    onShortcutConfigActivitiesClick: () -> Unit,
    onWallpaperClick: () -> Unit,
): List<SettingsMenuItem> = buildList {
    add(
        SettingsMenuItem(
            imageVector = EblanLauncherIcons.Settings,
            title = stringResource(commonR.string.settings),
            onClick = onSettingsClick,
        ),
    )

    add(
        SettingsMenuItem(
            imageVector = EblanLauncherIcons.Pages,
            title = stringResource(R.string.edit_pages),
            onClick = onEditPageClick,
        ),
    )

    add(
        SettingsMenuItem(
            imageVector = EblanLauncherIcons.Pages,
            title = stringResource(R.string.edit_dock_pages),
            onClick = onEditDockPageClick,
        ),
    )

    if (hasSystemFeatureAppWidgets) {
        add(
            SettingsMenuItem(
                imageVector = EblanLauncherIcons.Widgets,
                title = stringResource(R.string.widgets),
                onClick = onWidgetsClick,
            ),
        )
    }

    add(
        SettingsMenuItem(
            imageVector = EblanLauncherIcons.Shortcut,
            title = stringResource(R.string.shortcuts),
            onClick = onShortcutConfigActivitiesClick,
        ),
    )

    add(
        SettingsMenuItem(
            imageVector = EblanLauncherIcons.Image,
            title = stringResource(R.string.wallpaper),
            onClick = onWallpaperClick,
        ),
    )
}
