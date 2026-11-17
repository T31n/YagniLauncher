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
package com.eblan.launcher.platform.jarowinklersimilarity

import com.eblan.launcher.domain.common.Dispatcher
import com.eblan.launcher.domain.common.EblanDispatchers
import com.eblan.launcher.domain.platform.JaroWinklerSimilarityWrapper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.apache.commons.text.similarity.JaroWinklerSimilarity
import javax.inject.Inject

internal class DefaultJaroWinklerSimilarityWrapper @Inject constructor(
    @param:Dispatcher(EblanDispatchers.Default) private val defaultDispatcher: CoroutineDispatcher,
) : JaroWinklerSimilarityWrapper {
    private val jaroWinklerSimilarity = JaroWinklerSimilarity()

    override suspend fun apply(
        left: CharSequence,
        right: CharSequence,
    ): Double = withContext(defaultDispatcher) {
        jaroWinklerSimilarity.apply(left, right)
    }
}
