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
package com.eblan.launcher.domain.usecase.aiorganizer

import com.eblan.launcher.domain.model.aiorganizer.AiOrganizerFile
import com.eblan.launcher.domain.repository.AiOrganizerRepository
import javax.inject.Inject

/** Reports which generated organization files are available to apply or restore. */
class GetAiOrganizerStatusUseCase @Inject constructor(
    private val aiOrganizerRepository: AiOrganizerRepository,
) {
    suspend operator fun invoke(): AiOrganizerStatus = AiOrganizerStatus(
        hasNewHome = aiOrganizerRepository.fileExists(AiOrganizerFile.NEW_HOME),
        hasLastHome = aiOrganizerRepository.fileExists(AiOrganizerFile.LAST_HOME),
    )
}

data class AiOrganizerStatus(
    val hasNewHome: Boolean,
    val hasLastHome: Boolean,
)
