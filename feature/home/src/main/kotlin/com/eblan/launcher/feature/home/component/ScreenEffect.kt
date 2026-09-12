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
package com.eblan.launcher.feature.home.component

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.eblan.launcher.feature.home.model.Drag
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.onEach
import kotlin.time.Duration.Companion.milliseconds

@Composable
@OptIn(FlowPreview::class)
internal fun ScreenEffect(
    drag: Drag,
    isVisibleOverlay: Boolean,
    screenHeight: Int,
    swipeY: Float,
    textFieldState: TextFieldState,
    onDismiss: () -> Unit,
    onGetLabel: (String) -> Unit,
    onUpdateIsVisibleOverlay: (Boolean) -> Unit,
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(key1 = textFieldState) {
        snapshotFlow { textFieldState.text }.debounce(500L.milliseconds)
            .onEach {
                onGetLabel(it.toString())
            }.collect()
    }

    LaunchedEffect(key1 = swipeY) {
        if (swipeY == screenHeight.toFloat()) {
            keyboardController?.hide()
        }
    }

    LaunchedEffect(
        key1 = isVisibleOverlay,
        key2 = drag,
    ) {
        if (isVisibleOverlay && (drag == Drag.Cancel || drag == Drag.End)) {
            onUpdateIsVisibleOverlay(false)
        }
    }

    BackHandler(enabled = swipeY < screenHeight.toFloat()) {
        onDismiss()
    }

    HomeHandler(enabled = swipeY < screenHeight.toFloat()) {
        onDismiss()
    }
}

@Composable
internal fun rememberNestedScrollConnectionEffect(
    lazyListState: LazyListState,
    swipeY: Float,
    onVerticalDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
): OffsetNestedScrollConnection {
    val currentOnVerticalDrag by rememberUpdatedState(onVerticalDrag)
    val currentOnDragEnd by rememberUpdatedState(onDragEnd)

    val nestedScrollConnection = remember {
        OffsetNestedScrollConnection(
            onVerticalDrag = currentOnVerticalDrag,
            onDragEnd = currentOnDragEnd,
        )
    }

    LaunchedEffect(
        key1 = nestedScrollConnection,
        key2 = swipeY,
        key3 = lazyListState.canScrollBackward,
    ) {
        nestedScrollConnection.updateSwipeY(swipeY)
        nestedScrollConnection.updateCanScrollBackward(lazyListState.canScrollBackward)
    }

    return nestedScrollConnection
}
