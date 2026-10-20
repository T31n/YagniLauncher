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

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarColors
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarState
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.eblan.launcher.designsystem.icon.EblanLauncherIcons
import com.eblan.launcher.domain.model.userdata.BackgroundColor
import com.eblan.launcher.domain.model.userdata.TextColor
import com.eblan.launcher.feature.home.util.getApplicationScreenTextColor
import kotlinx.coroutines.launch
import com.eblan.launcher.common.R as commonR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ApplicationSearchBar(
    // State
    searchBarState: SearchBarState,
    textFieldState: TextFieldState,
    focusRequester: FocusRequester,
    // Appearance
    backgroundColor: BackgroundColor,
    customBackgroundColor: Int,
    systemTextColor: TextColor,
    systemCustomTextColor: Int,
    // Layout
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()

    val searchBarColors =
        getSearchBarColors(
            backgroundColor = backgroundColor,
            customBackgroundColor = customBackgroundColor,
            systemTextColor = systemTextColor,
            systemCustomTextColor = systemCustomTextColor
        )

    SearchBar(
        state = searchBarState,
        modifier = modifier
            .fillMaxWidth()
            .padding(10.dp),
        colors = searchBarColors,
        inputField = {
            SearchBarDefaults.InputField(
                modifier = Modifier.focusRequester(focusRequester),
                textFieldState = textFieldState,
                searchBarState = searchBarState,
                colors = searchBarColors.inputFieldColors,
                leadingIcon = {
                    Icon(
                        imageVector = EblanLauncherIcons.Search,
                        contentDescription = null,
                    )
                },
                trailingIcon = if (textFieldState.text.isNotEmpty()) {
                    {
                        IconButton(
                            onClick = {
                                textFieldState.clearText()
                            },
                        ) {
                            Icon(
                                imageVector = EblanLauncherIcons.Close,
                                contentDescription = null,
                            )
                        }
                    }
                } else {
                    null
                },
                onSearch = { scope.launch { searchBarState.animateToCollapsed() } },
                placeholder = { Text(text = stringResource(commonR.string.search_applications)) },
            )
        },
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun getSearchBarColors(
    // Background Colors
    backgroundColor: BackgroundColor,
    customBackgroundColor: Int,
    // Text Colors
    systemTextColor: TextColor,
    systemCustomTextColor: Int
): SearchBarColors {
    val containerColor = when (backgroundColor) {
        BackgroundColor.System -> MaterialTheme.colorScheme.surfaceContainerHigh
        BackgroundColor.Light -> Color.White
        BackgroundColor.Dark -> Color.Black
        BackgroundColor.Custom -> Color(customBackgroundColor)
    }

    val contentColor = getApplicationScreenTextColor(
        backgroundColor = backgroundColor,
        customBackgroundColor = customBackgroundColor,
        systemTextColor = systemTextColor,
        systemCustomTextColor = systemCustomTextColor,
        defaultColor = MaterialTheme.colorScheme.onSurface
    )

    val searchBarColors = SearchBarDefaults.colors(
        containerColor = containerColor,
        dividerColor = contentColor.copy(alpha = 0.3f),
        inputFieldColors = TextFieldDefaults.colors(
            focusedTextColor = contentColor,
            unfocusedTextColor = contentColor,
            focusedPlaceholderColor = contentColor.copy(alpha = 0.6f),
            unfocusedPlaceholderColor = contentColor.copy(alpha = 0.6f),
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedLeadingIconColor = contentColor,
            unfocusedLeadingIconColor = contentColor,
            focusedTrailingIconColor = contentColor,
            unfocusedTrailingIconColor = contentColor,
            cursorColor = contentColor,
        ),
    )

    return searchBarColors
}
