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
package com.eblan.launcher.common.impl

import android.content.Context
import android.os.Build
import android.util.TypedValue
import com.eblan.launcher.common.AndroidColor
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

internal class DefaultAndroidColor @Inject constructor(@param:ApplicationContext private val context: Context) : AndroidColor {
    override fun getColor(): Int {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return context.getColor(android.R.color.system_accent1_600)
        }

        val value = TypedValue()

        return if (context.theme.resolveAttribute(
                android.R.attr.colorAccent,
                value,
                true,
            )
        ) {
            value.data
        } else {
            context.getColor(android.R.color.holo_blue_dark)
        }
    }
}
