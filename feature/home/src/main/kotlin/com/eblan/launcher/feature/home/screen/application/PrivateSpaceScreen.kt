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
package com.eblan.launcher.feature.home.screen.application

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.sp
import coil3.size.Size
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.domain.model.application.EblanApplicationInfo
import com.eblan.launcher.domain.model.grid.GridItemSettings
import com.eblan.launcher.domain.model.userdata.BackgroundColor
import com.eblan.launcher.domain.model.userdata.TextColor
import com.eblan.launcher.feature.home.R
import com.eblan.launcher.feature.home.component.GridItemLayoutType
import com.eblan.launcher.feature.home.component.LauncherIconImage
import com.eblan.launcher.feature.home.util.getApplicationScreenTextColor
import com.eblan.launcher.feature.home.util.getGridItemBackgroundColor
import com.eblan.launcher.feature.home.util.getGridItemTextColorFromBackgroundColor
import com.eblan.launcher.feature.home.util.getHorizontalAlignment
import com.eblan.launcher.feature.home.util.getHorizontalArrangement
import com.eblan.launcher.feature.home.util.getVerticalAlignment
import com.eblan.launcher.feature.home.util.getVerticalArrangement
import com.eblan.launcher.ui.local.LocalLauncherApps
import com.eblan.launcher.ui.local.LocalUserManager
import com.eblan.launcher.ui.settings.produceIsDefaultLauncherState
import kotlinx.coroutines.launch

internal fun LazyGridScope.privateSpace(
    // User and Application Data
    privateSerialNumber: Long?,
    isPrivateSpaceVisible: Boolean,
    privateEblanApplicationInfos: List<EblanApplicationInfo>,
    // App Drawer and Appearance Settings
    gridItemSettings: GridItemSettings,
    appDrawerRowsHeight: Int,
    iconPackFilePaths: Map<String, String?>,
    // Space State
    isQuietModeEnabled: Boolean,
    isVisibleOverlay: Boolean,
    // Layout and Colors
    leftPadding: Int,
    topPadding: Int,
    backgroundColor: BackgroundColor,
    customBackgroundColor: Int,
    systemTextColor: TextColor,
    systemCustomTextColor: Int,
    // Callbacks
    onDismiss: () -> Unit,
    onLongPressPrivateSpaceApplicationInfoItem: (
        eblanApplicationInfo: EblanApplicationInfo,
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
) {
    if (!isPrivateSpaceVisible || privateSerialNumber == null) {
        return
    }

    stickyHeader {
        PrivateSpaceStickyHeader(
            // Device State
            serialNumber = privateSerialNumber,
            isQuietModeEnabled = isQuietModeEnabled,
            // Appearance
            backgroundColor = backgroundColor,
            customBackgroundColor = customBackgroundColor,
            systemTextColor = systemTextColor,
            systemCustomTextColor = systemCustomTextColor,
        )
    }

    if (!isQuietModeEnabled) {
        items(privateEblanApplicationInfos) { eblanApplicationInfo ->
            PrivateSpaceEblanApplicationInfoItem(
                // Application Info
                eblanApplicationInfo = eblanApplicationInfo,
                // Settings and Styling
                gridItemSettings = gridItemSettings,
                appDrawerRowsHeight = appDrawerRowsHeight,
                iconPackFilePath = iconPackFilePaths[eblanApplicationInfo.componentName],
                isVisibleOverlay = isVisibleOverlay,
                backgroundColor = backgroundColor,
                customBackgroundColor = customBackgroundColor,
                systemTextColor = systemTextColor,
                systemCustomTextColor = systemCustomTextColor,
                leftPadding = leftPadding,
                topPadding = topPadding,
                // Actions
                onDismiss = onDismiss,
                onLongPressPrivateSpaceApplicationInfoItem = onLongPressPrivateSpaceApplicationInfoItem,
            )
        }
    }
}

@Composable
internal fun PrivateSpaceStickyHeader(
    // Device State
    serialNumber: Long,
    isQuietModeEnabled: Boolean,
    // Appearance
    backgroundColor: BackgroundColor,
    customBackgroundColor: Int,
    systemTextColor: TextColor,
    systemCustomTextColor: Int,
    // Layout
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()

    val userManager = LocalUserManager.current
    val launcherApps = LocalLauncherApps.current

    val userHandle =
        userManager.getUserForSerialNumber(serialNumber = serialNumber)

    val privateSpaceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
    ) {}

    val isDefaultLauncher by produceIsDefaultLauncherState()

    val contentColor = getApplicationScreenTextColor(
        // Background Colors
        backgroundColor = backgroundColor,
        customBackgroundColor = customBackgroundColor,
        // Text Colors
        systemTextColor = systemTextColor,
        systemCustomTextColor = systemCustomTextColor,
        // Defaults
        defaultColor = MaterialTheme.colorScheme.onSurface,
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.private_space),
            color = contentColor,
        )
        Row {
            launcherApps.getPrivateSpaceSettingsIntent()?.let { intentSender ->
                IconButton(
                    onClick = {
                        privateSpaceLauncher.launch(
                            IntentSenderRequest.Builder(intentSender).build(),
                        )
                    },
                ) {
                    Icon(
                        imageVector = EblanLauncherIcons.Settings,
                        contentDescription = null,
                        tint = contentColor,
                    )
                }
            }
            if (isDefaultLauncher && userHandle != null) {
                IconButton(
                    onClick = {
                        scope.launch {
                            userManager.requestQuietModeEnabled(
                                enableQuiteMode = !isQuietModeEnabled,
                                userHandle = userHandle,
                            )
                        }
                    },
                ) {
                    Icon(
                        imageVector = if (isQuietModeEnabled) {
                            EblanLauncherIcons.Lock
                        } else {
                            EblanLauncherIcons.LockOpen
                        },
                        contentDescription = null,
                        tint = contentColor,
                    )
                }
            }
        }
    }
}

@Composable
internal fun PrivateSpaceEblanApplicationInfoItem(
    // Application Info
    eblanApplicationInfo: EblanApplicationInfo,
    // Settings and Styling
    gridItemSettings: GridItemSettings,
    appDrawerRowsHeight: Int,
    iconPackFilePath: String?,
    isVisibleOverlay: Boolean,
    backgroundColor: BackgroundColor,
    customBackgroundColor: Int,
    systemTextColor: TextColor,
    systemCustomTextColor: Int,
    leftPadding: Int,
    topPadding: Int,
    modifier: Modifier = Modifier,
    // Actions
    onDismiss: () -> Unit,
    onLongPressPrivateSpaceApplicationInfoItem: (
        eblanApplicationInfo: EblanApplicationInfo,
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
) {
    val density = LocalDensity.current
    val launcherApps = LocalLauncherApps.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val scope = rememberCoroutineScope()

    val currentOnDismiss by rememberUpdatedState(onDismiss)

    val textColor = getGridItemTextColorFromBackgroundColor(
        // Container Background Color
        backgroundColor = backgroundColor,
        customBackgroundColor = customBackgroundColor,
        // Grid Item Colors
        gridItemTextColor = gridItemSettings.textColor,
        gridItemCustomTextColor = gridItemSettings.customTextColor,
        gridItemBackgroundColor = gridItemSettings.backgroundColor,
        gridItemCustomBackgroundColor = gridItemSettings.customBackgroundColor,
        // Text Color
        systemTextColor = systemTextColor,
        systemCustomTextColor = systemCustomTextColor,
    )

    val appDrawerRowsHeightDp = appDrawerRowsHeight.dp

    val maxLines = if (gridItemSettings.singleLineLabel) 1 else Int.MAX_VALUE

    val icon = iconPackFilePath ?: eblanApplicationInfo.icon

    val horizontalAlignment =
        getHorizontalAlignment(horizontalAlignment = gridItemSettings.horizontalAlignment)
    val verticalArrangement =
        getVerticalArrangement(verticalArrangement = gridItemSettings.verticalArrangement)
    val horizontalArrangement =
        getHorizontalArrangement(horizontalArrangement = gridItemSettings.horizontalArrangement)
    val verticalAlignment =
        getVerticalAlignment(verticalAlignment = gridItemSettings.verticalAlignment)

    var intOffset by remember { mutableStateOf(IntOffset.Zero) }
    var intSize by remember { mutableStateOf(IntSize.Zero) }

    val iconSizePx = with(density) {
        gridItemSettings.iconSize.dp.roundToPx()
    }

    val itemModifier = modifier
        .height(appDrawerRowsHeightDp)
        .padding(gridItemSettings.padding.dp)
        .background(
            color = getGridItemBackgroundColor(
                backgroundColor = gridItemSettings.backgroundColor,
                customBackgroundColor = gridItemSettings.customBackgroundColor,
            ),
            shape = RoundedCornerShape(size = gridItemSettings.cornerRadius.dp),
        )
        .pointerInput(key1 = isVisibleOverlay) {
            detectTapGestures(
                onTap = if (!isVisibleOverlay) {
                    {
                        scope.launch {
                            handleOnTapEblanApplicationInfoItem(
                                // Application Info
                                serialNumber = eblanApplicationInfo.serialNumber,
                                componentName = eblanApplicationInfo.componentName,
                                launcherApps = launcherApps,
                                // Position and Size
                                intOffset = intOffset,
                                intSize = intSize,
                                // Padding
                                leftPadding = leftPadding,
                                topPadding = topPadding,
                                // Controller
                                keyboardController = keyboardController,
                            )

                            currentOnDismiss()
                        }
                    }
                } else {
                    null
                },
                onLongPress = if (!isVisibleOverlay) {
                    {
                        scope.launch {
                            handleOnLongPressPrivateSpaceEblanApplicationInfoItem(
                                // Application Info
                                eblanApplicationInfo = eblanApplicationInfo,
                                // Position and Size
                                intOffset = intOffset,
                                intSize = intSize,
                                // Keyboard
                                keyboardController = keyboardController,
                                // Callbacks
                                onLongPressPrivateSpaceApplicationInfoItem = onLongPressPrivateSpaceApplicationInfoItem,
                            )
                        }
                    }
                } else {
                    null
                },
            )
        }

    val iconModifier = Modifier
        .size(gridItemSettings.iconSize.dp)
        .padding(gridItemSettings.iconPadding.dp)
        .onGloballyPositioned { currentLayoutCoordinates ->
            intOffset = currentLayoutCoordinates.positionInRoot().round()
            intSize = currentLayoutCoordinates.size
        }

    val iconContent: @Composable () -> Unit = {
        LauncherIconImage(
            iconPath = eblanApplicationInfo.customIcon ?: icon,
            size = Size(width = iconSizePx, height = iconSizePx),
            modifier = iconModifier,
        )
    }

    val labelContent: @Composable () -> Unit = {
        Text(
            modifier = Modifier.padding(gridItemSettings.textPadding.dp),
            text = eblanApplicationInfo.customLabel
                ?: eblanApplicationInfo.label,
            color = textColor,
            textAlign = TextAlign.Center,
            maxLines = maxLines,
            fontSize = gridItemSettings.textSize.sp,
            overflow = TextOverflow.Ellipsis,
        )
    }

    GridItemLayoutType(
        // Layout and Modifier
        layoutType = gridItemSettings.layoutType,
        modifier = itemModifier,
        // Alignment and Arrangement
        horizontalAlignment = horizontalAlignment,
        verticalAlignment = verticalAlignment,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement,
        // Content
        iconContent = iconContent,
        labelContent = labelContent,
        labelOnlyContent = labelContent,
    )
}

internal fun handleOnLongPressPrivateSpaceEblanApplicationInfoItem(
    // Application Info
    eblanApplicationInfo: EblanApplicationInfo,
    // Position and Size
    intOffset: IntOffset,
    intSize: IntSize,
    // Keyboard
    keyboardController: SoftwareKeyboardController?,
    // Callbacks
    onLongPressPrivateSpaceApplicationInfoItem: (
        eblanApplicationInfo: EblanApplicationInfo,
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
) {
    onLongPressPrivateSpaceApplicationInfoItem(
        eblanApplicationInfo,
        intOffset,
        intSize,
    )

    keyboardController?.hide()
}
