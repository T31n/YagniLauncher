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
package com.eblan.launcher.framework.resources

import android.content.Context
import android.content.res.Configuration
import com.eblan.launcher.domain.framework.ResourcesWrapper
import com.eblan.launcher.domain.model.userdata.Theme
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal class DefaultResourcesWrapper @Inject constructor(@param:ApplicationContext private val context: Context) : ResourcesWrapper {
    override fun getSystemTheme(): Theme = when (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
        Configuration.UI_MODE_NIGHT_YES -> Theme.Dark
        Configuration.UI_MODE_NIGHT_NO -> Theme.Light
        else -> Theme.Light
    }
}
