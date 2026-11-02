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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
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
import com.eblan.launcher.ui.settings.settingsFirstItemShape
import com.eblan.launcher.ui.settings.settingsLastItemShape
import com.eblan.launcher.ui.settings.settingsMiddleItemShape
import com.eblan.launcher.common.R as commonR

@Composable
internal fun SettingsPopup(
    // Content
    gridItems: List<GridItem>,
    // State
    hasSystemFeatureAppWidgets: Boolean,
    popupSettingsIntOffset: IntOffset?,
    // Actions
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

    val dismiss = {
        transitionState.targetState = false
    }

    LaunchedEffect(
        key1 = transitionState.targetState,
        key2 = transitionState.isIdle,
    ) {
        if (!transitionState.targetState && transitionState.isIdle) {
            onDismissRequest()
        }
    }

    BackHandler(enabled = transitionState.targetState) {
        dismiss()
    }

    HomeHandler(enabled = transitionState.targetState) {
        dismiss()
    }

    Popup(
        popupPositionProvider = SettingsPopupPositionProvider(
            x = popupSettingsIntOffset.x,
            y = popupSettingsIntOffset.y,
        ),
        onDismissRequest = dismiss,
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
                PopupMenuRow(
                    title = stringResource(commonR.string.settings),
                    imageVector = EblanLauncherIcons.Settings,
                    shape = settingsFirstItemShape,
                    onClick = {
                        onSettings()

                        dismiss()
                    },
                )

                PopupMenuRow(
                    title = stringResource(R.string.edit_pages),
                    imageVector = EblanLauncherIcons.Pages,
                    shape = settingsMiddleItemShape,
                    onClick = {
                        onEditPage(
                            gridItems,
                            Associate.Grid,
                        )

                        dismiss()
                    },
                )

                PopupMenuRow(
                    title = stringResource(R.string.edit_dock_pages),
                    imageVector = EblanLauncherIcons.Pages,
                    shape = settingsMiddleItemShape,
                    onClick = {
                        onEditPage(
                            gridItems,
                            Associate.Dock,
                        )

                        dismiss()
                    },
                )

                if (hasSystemFeatureAppWidgets) {
                    PopupMenuRow(
                        title = stringResource(R.string.widgets),
                        imageVector = EblanLauncherIcons.Widgets,
                        shape = settingsMiddleItemShape,
                        onClick = {
                            onWidgets()

                            dismiss()
                        },
                    )
                }

                PopupMenuRow(
                    title = stringResource(R.string.shortcuts),
                    imageVector = EblanLauncherIcons.Shortcut,
                    shape = settingsMiddleItemShape,
                    onClick = {
                        onShortcutConfigActivities()

                        dismiss()
                    },
                )

                PopupMenuRow(
                    title = stringResource(R.string.wallpaper),
                    imageVector = EblanLauncherIcons.Image,
                    shape = settingsLastItemShape,
                    onClick = {
                        val intent = Intent(ACTION_SET_WALLPAPER)

                        val chooser = createChooser(intent, "Set Wallpaper")

                        context.startActivity(chooser)

                        dismiss()
                    },
                )
            }
        }
    }
}

@Composable
private fun PopupMenuRow(
    // Content
    title: String,
    imageVector: ImageVector,
    // Layout
    shape: Shape,
    modifier: Modifier = Modifier,
    // Action
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max),
        shape = shape,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = null,
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
