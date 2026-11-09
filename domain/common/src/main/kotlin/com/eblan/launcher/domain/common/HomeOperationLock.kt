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
package com.eblan.launcher.domain.common

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Serializes operations that change home screen data. Must be provided as a singleton so all callers share one [Mutex].
 */
class HomeOperationLock {
    private val mutex = Mutex()

    private val busy = MutableStateFlow(false)

    /** True while a [runExclusively] block is running. */
    val isBusy: StateFlow<Boolean> = busy.asStateFlow()

    /** Runs [block] after any operation already running has finished. */
    suspend fun <T> runExclusively(block: suspend () -> T): T = mutex.withLock {
        busy.value = true

        try {
            block()
        } finally {
            busy.value = false
        }
    }
}
