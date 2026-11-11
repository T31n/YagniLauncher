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

/**
 * Serializes every grid read-compute-write sequence so that background sync and user edits
 * cannot observe or overwrite each other's partial state.
 *
 * Implementations must be reentrant: a block running under the lock may call other
 * lock-guarded grid functions (for example, repository methods) without deadlocking.
 */
interface GridLock {
    suspend fun <T> withLock(block: suspend () -> T): T
}
