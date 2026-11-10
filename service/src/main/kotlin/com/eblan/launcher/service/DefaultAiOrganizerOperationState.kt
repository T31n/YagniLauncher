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
package com.eblan.launcher.service

import com.eblan.launcher.domain.framework.AiOrganizerOperationState
import com.eblan.launcher.domain.model.aiorganizer.AiOrganizerOperation
import com.eblan.launcher.domain.model.aiorganizer.ApplyAiOrganizationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class DefaultAiOrganizerOperationState @Inject constructor() : AiOrganizerOperationState {
    private val mutableOperation = MutableStateFlow<AiOrganizerOperation?>(null)

    override val operation: StateFlow<AiOrganizerOperation?> = mutableOperation.asStateFlow()

    private val mutableApplyResult = MutableStateFlow<ApplyAiOrganizationResult?>(null)

    override val applyResult: StateFlow<ApplyAiOrganizationResult?> = mutableApplyResult.asStateFlow()

    override fun setOperation(operation: AiOrganizerOperation?) {
        mutableOperation.value = operation
    }

    override fun setApplyResult(applyResult: ApplyAiOrganizationResult?) {
        mutableApplyResult.value = applyResult
    }
}
