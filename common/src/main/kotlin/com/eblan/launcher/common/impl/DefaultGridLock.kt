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

import com.eblan.launcher.domain.common.GridLock
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

/**
 * [Mutex] is not reentrant, so a use case holding the lock that calls a repository method
 * which also takes the lock would deadlock. The context element marks that the current
 * coroutine already holds the lock, letting nested calls pass through.
 */
internal class DefaultGridLock @Inject constructor() : GridLock {
    private val mutex = Mutex()

    override suspend fun <T> withLock(block: suspend () -> T): T {
        if (currentCoroutineContext()[HeldGridWriteLockKey] != null) return block()

        return mutex.withLock {
            withContext(HeldGridWriteLockElement()) { block() }
        }
    }
}

private object HeldGridWriteLockKey : CoroutineContext.Key<HeldGridWriteLockElement>

private class HeldGridWriteLockElement : AbstractCoroutineContextElement(HeldGridWriteLockKey)
