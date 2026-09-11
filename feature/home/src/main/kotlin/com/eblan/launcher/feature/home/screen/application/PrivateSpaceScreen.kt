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

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
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
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.addLastModifiedToFileCacheKey
import coil3.request.crossfade
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.domain.model.application.EblanApplicationInfo
import com.eblan.launcher.domain.model.launcherapps.EblanUser
import com.eblan.launcher.domain.model.userdata.AppDrawerSettings
import com.eblan.launcher.domain.model.userdata.BackgroundColor
import com.eblan.launcher.domain.model.userdata.TextColor
import com.eblan.launcher.feature.home.R
import com.eblan.launcher.feature.home.util.getApplicationScreenTextColor
import com.eblan.launcher.feature.home.util.getHorizontalAlignment
import com.eblan.launcher.feature.home.util.getTextColorFromBackgroundColor
import com.eblan.launcher.feature.home.util.getVerticalArrangement
import com.eblan.launcher.feature.home.util.handleOnPress
import com.eblan.launcher.ui.local.LocalLauncherApps
import com.eblan.launcher.ui.local.LocalUserManager
import com.eblan.launcher.ui.settings.rememberIsDefaultLauncher
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi

internal fun LazyGridScope.privateSpace(
    appDrawerSettings: AppDrawerSettings,
    isQuietModeEnabled: Boolean,
    paddingValues: PaddingValues,
    privateEblanApplicationInfos: List<EblanApplicationInfo>,
    privateEblanUser: EblanUser?,
    isVisibleOverlay: Boolean,
    backgroundColor: BackgroundColor,
    customBackgroundColor: Int,
    systemCustomTextColor: Int,
    systemTextColor: TextColor,
    iconPackInfoFilePaths: Map<String, String?>,
    animations: Boolean,
    onUpdateOverlayBounds: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onUpdatePopupMenu: (Boolean) -> Unit,
    onUpdateEblanApplicationInfo: (EblanApplicationInfo) -> Unit,
) {
    if (privateEblanUser == null || privateEblanUser.isPrivateSpaceEntryPointHidden) return

    stickyHeader {
        PrivateSpaceStickyHeader(
            serialNumber = privateEblanUser.serialNumber,
            isQuietModeEnabled = isQuietModeEnabled,
            backgroundColor = backgroundColor,
            customBackgroundColor = customBackgroundColor,
            systemCustomTextColor = systemCustomTextColor,
            systemTextColor = systemTextColor,

        )
    }

    if (!isQuietModeEnabled) {
        items(privateEblanApplicationInfos) { eblanApplicationInfo ->
            PrivateSpaceEblanApplicationInfoItem(
                appDrawerSettings = appDrawerSettings,
                eblanApplicationInfo = eblanApplicationInfo,
                paddingValues = paddingValues,
                isVisibleOverlay = isVisibleOverlay,
                systemTextColor = systemTextColor,
                systemCustomTextColor = systemCustomTextColor,
                iconPackInfoFilePaths = iconPackInfoFilePaths,
                animations = animations,
                onUpdateOverlayBounds = onUpdateOverlayBounds,
                onUpdatePopupMenu = onUpdatePopupMenu,
                onUpdateEblanApplicationInfo = onUpdateEblanApplicationInfo,
            )
        }
    }
}

@Composable
internal fun PrivateSpaceStickyHeader(
    modifier: Modifier = Modifier,
    serialNumber: Long,
    isQuietModeEnabled: Boolean,
    backgroundColor: BackgroundColor,
    customBackgroundColor: Int,
    systemCustomTextColor: Int,
    systemTextColor: TextColor,
) {
    val scope = rememberCoroutineScope()

    val userManager = LocalUserManager.current

    val launcherApps = LocalLauncherApps.current

    val userHandle =
        userManager.getUserForSerialNumber(serialNumber = serialNumber)

    val privateSpaceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
    ) {}

    val isDefaultLauncher by rememberIsDefaultLauncher()

    val contentColor = getApplicationScreenTextColor(
        backgroundColor = backgroundColor,
        customBackgroundColor = customBackgroundColor,
        systemCustomTextColor = systemCustomTextColor,
        systemTextColor = systemTextColor,
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P && isDefaultLauncher && userHandle != null) {
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

@OptIn(
    ExperimentalUuidApi::class,
    ExperimentalSharedTransitionApi::class,
    ExperimentalLayoutApi::class,
)
@Composable
internal fun PrivateSpaceEblanApplicationInfoItem(
    modifier: Modifier = Modifier,
    appDrawerSettings: AppDrawerSettings,
    eblanApplicationInfo: EblanApplicationInfo,
    paddingValues: PaddingValues,
    isVisibleOverlay: Boolean,
    systemCustomTextColor: Int,
    systemTextColor: TextColor,
    iconPackInfoFilePaths: Map<String, String?>,
    animations: Boolean,
    onUpdateOverlayBounds: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    onUpdatePopupMenu: (Boolean) -> Unit,
    onUpdateEblanApplicationInfo: (EblanApplicationInfo) -> Unit,
) {
    val context = LocalContext.current

    val density = LocalDensity.current

    val layoutDirection = LocalLayoutDirection.current

    val launcherApps = LocalLauncherApps.current

    val keyboardController = LocalSoftwareKeyboardController.current

    val scope = rememberCoroutineScope()

    val textColor = getTextColorFromBackgroundColor(
        backgroundColor = appDrawerSettings.backgroundColor,
        customBackgroundColor = appDrawerSettings.customBackgroundColor,
        textColor = appDrawerSettings.gridItemSettings.textColor,
        customTextColor = appDrawerSettings.gridItemSettings.customTextColor,
        systemTextColor = systemTextColor,
        systemCustomTextColor = systemCustomTextColor,
    )

    val appDrawerRowsHeight = appDrawerSettings.appDrawerRowsHeight.dp

    val maxLines = if (appDrawerSettings.gridItemSettings.singleLineLabel) 1 else Int.MAX_VALUE

    val icon = iconPackInfoFilePaths[eblanApplicationInfo.componentName]
        ?: eblanApplicationInfo.icon

    val horizontalAlignment =
        getHorizontalAlignment(horizontalAlignment = appDrawerSettings.gridItemSettings.horizontalAlignment)

    val verticalArrangement =
        getVerticalArrangement(verticalArrangement = appDrawerSettings.gridItemSettings.verticalArrangement)

    val leftPadding = with(density) {
        paddingValues.calculateLeftPadding(layoutDirection).roundToPx()
    }

    val topPadding = with(density) {
        paddingValues.calculateTopPadding().roundToPx()
    }

    var intOffset by remember { mutableStateOf(IntOffset.Zero) }

    var intSize by remember { mutableStateOf(IntSize.Zero) }

    val iconSizePx = with(density) {
        appDrawerSettings.gridItemSettings.iconSize.dp.roundToPx()
    }

    val scale = remember { Animatable(1f) }

    LaunchedEffect(
        key1 = isVisibleOverlay,
        key2 = animations,
    ) {
        if (isVisibleOverlay && animations) {
            scale.snapTo(targetValue = 1f)
        }
    }

    Column(
        modifier = modifier
            .height(appDrawerRowsHeight)
            .padding(appDrawerSettings.gridItemSettings.padding.dp)
            .background(
                color = Color(appDrawerSettings.gridItemSettings.customBackgroundColor),
                shape = RoundedCornerShape(
                    size = appDrawerSettings.gridItemSettings.cornerRadius.dp,
                ),
            )
            .pointerInput(key1 = isVisibleOverlay) {
                detectTapGestures(
                    onTap = if (!isVisibleOverlay) {
                        {
                            scope.launch {
                                handleOnTapEblanApplicationInfoItem(
                                    eblanApplicationInfo = eblanApplicationInfo,
                                    intOffset = intOffset,
                                    intSize = intSize,
                                    keyboardController = keyboardController,
                                    launcherApps = launcherApps,
                                    leftPadding = leftPadding,
                                    topPadding = topPadding,
                                )
                            }
                        }
                    } else {
                        null
                    },
                    onLongPress = if (!isVisibleOverlay) {
                        {
                            scope.launch {
                                handleOnLongPressPrivateSpaceEblanApplicationInfoItem(
                                    onUpdateEblanApplicationInfo = onUpdateEblanApplicationInfo,
                                    eblanApplicationInfo = eblanApplicationInfo,
                                    onUpdateOverlayBounds = onUpdateOverlayBounds,
                                    intOffset = intOffset,
                                    intSize = intSize,
                                    onUpdatePopupMenu = onUpdatePopupMenu,
                                    keyboardController = keyboardController,
                                )
                            }
                        }
                    } else {
                        null
                    },
                    onPress = {
                        handleOnPress(
                            animations = animations,
                            scale = scale,
                        )
                    },
                )
            },
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = verticalArrangement,
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(eblanApplicationInfo.customIcon ?: icon)
                .addLastModifiedToFileCacheKey(true)
                .size(iconSizePx)
                .crossfade(false)
                .build(),
            contentDescription = null,
            modifier = Modifier
                .size(appDrawerSettings.gridItemSettings.iconSize.dp)
                .onGloballyPositioned {
                    intOffset = it.positionInRoot().round()

                    intSize = it.size
                }
                .run {
                    if (animations) {
                        graphicsLayer {
                            scaleX = scale.value
                            scaleY = scale.value
                        }
                    } else {
                        this
                    }
                },
            placeholder = ColorPainter(Color.Transparent),
            error = ColorPainter(Color.Transparent),
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = eblanApplicationInfo.customLabel
                ?: eblanApplicationInfo.label,
            color = textColor,
            textAlign = TextAlign.Center,
            maxLines = maxLines,
            fontSize = appDrawerSettings.gridItemSettings.textSize.sp,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

internal fun handleOnLongPressPrivateSpaceEblanApplicationInfoItem(
    onUpdateEblanApplicationInfo: (EblanApplicationInfo) -> Unit,
    eblanApplicationInfo: EblanApplicationInfo,
    onUpdateOverlayBounds: (
        intOffset: IntOffset,
        intSize: IntSize,
    ) -> Unit,
    intOffset: IntOffset,
    intSize: IntSize,
    onUpdatePopupMenu: (Boolean) -> Unit,
    keyboardController: SoftwareKeyboardController?,
) {
    onUpdateEblanApplicationInfo(eblanApplicationInfo)

    onUpdateOverlayBounds(
        intOffset,
        intSize,
    )

    onUpdatePopupMenu(true)

    keyboardController?.hide()
}
