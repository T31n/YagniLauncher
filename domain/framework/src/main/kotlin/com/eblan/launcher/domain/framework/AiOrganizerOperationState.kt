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
package com.eblan.launcher.domain.framework

import com.eblan.launcher.domain.model.aiorganizer.AiOrganizerOperation
import com.eblan.launcher.domain.model.aiorganizer.ApplyAiOrganizationResult
import kotlinx.coroutines.flow.StateFlow

/**
 * Shared view of the AI organizer work running in the service. The service is the only writer, so
 * organize and apply never overlap.
 */
interface AiOrganizerOperationState {
    /** The operation currently running, or null when idle. */
    val operation: StateFlow<AiOrganizerOperation?>

    /** The outcome of the most recent apply, or null if none has finished since it started. */
    val applyResult: StateFlow<ApplyAiOrganizationResult?>

    fun setOperation(operation: AiOrganizerOperation?)

    fun setApplyResult(applyResult: ApplyAiOrganizationResult?)
}
