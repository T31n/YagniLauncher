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
package com.eblan.launcher.domain.usecase.pin

import com.eblan.launcher.domain.common.Dispatcher
import com.eblan.launcher.domain.common.EblanDispatchers
import com.eblan.launcher.domain.common.FileManager
import com.eblan.launcher.domain.common.IconKeyGenerator
import com.eblan.launcher.domain.framework.PackageManagerWrapper
import com.eblan.launcher.domain.model.Associate
import com.eblan.launcher.domain.model.EblanAction
import com.eblan.launcher.domain.model.EblanActionType
import com.eblan.launcher.domain.model.GridItem
import com.eblan.launcher.domain.model.GridItemData.ShortcutInfo
import com.eblan.launcher.domain.model.GridItemData.Widget
import com.eblan.launcher.domain.model.PinItemRequestType
import com.eblan.launcher.domain.repository.UserDataRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class GetPinGridItemUseCase @Inject constructor(
    private val fileManager: FileManager,
    private val userDataRepository: UserDataRepository,
    private val packageManagerWrapper: PackageManagerWrapper,
    private val iconKeyGenerator: IconKeyGenerator,
    @param:Dispatcher(EblanDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) {
    @OptIn(ExperimentalUuidApi::class)
    suspend operator fun invoke(
        pinItemRequestType: PinItemRequestType,
    ): GridItem = withContext(ioDispatcher) {
        val homeSettings = userDataRepository.userDataFlow.first().homeSettings

        when (pinItemRequestType) {
            is PinItemRequestType.Widget -> {
                val eblanApplicationInfoIcon =
                    packageManagerWrapper.getComponentName(packageName = pinItemRequestType.packageName)
                        ?.let {
                            val directory = fileManager.getFilesDirectory(FileManager.ICONS_DIR)

                            val file = File(
                                directory,
                                iconKeyGenerator.getActivityIconKey(
                                    serialNumber = pinItemRequestType.serialNumber,
                                    componentName = it,
                                ),
                            )

                            file.absolutePath
                        }

                val data = Widget(
                    appWidgetId = 0,
                    componentName = pinItemRequestType.componentName,
                    packageName = pinItemRequestType.packageName,
                    serialNumber = pinItemRequestType.serialNumber,
                    configure = pinItemRequestType.configure,
                    minWidth = pinItemRequestType.minWidth,
                    minHeight = pinItemRequestType.minHeight,
                    resizeMode = pinItemRequestType.resizeMode,
                    minResizeWidth = pinItemRequestType.minResizeWidth,
                    minResizeHeight = pinItemRequestType.minResizeHeight,
                    maxResizeWidth = pinItemRequestType.maxResizeWidth,
                    maxResizeHeight = pinItemRequestType.maxResizeHeight,
                    targetCellHeight = pinItemRequestType.targetCellHeight,
                    targetCellWidth = pinItemRequestType.targetCellWidth,
                    preview = pinItemRequestType.preview,
                    label = packageManagerWrapper.getApplicationLabel(packageName = pinItemRequestType.packageName),
                    icon = eblanApplicationInfoIcon,
                )

                GridItem(
                    id = Uuid.random()
                        .toHexString(),
                    page = homeSettings.initialPage,
                    startColumn = 0,
                    startRow = 0,
                    columnSpan = 1,
                    rowSpan = 1,
                    data = data,
                    associate = Associate.Grid,
                    override = false,
                    gridItemSettings = homeSettings.gridItemSettings,
                    doubleTap = EblanAction(
                        eblanActionType = EblanActionType.None,
                        serialNumber = 0L,
                        componentName = "",
                    ),
                    swipeUp = EblanAction(
                        eblanActionType = EblanActionType.None,
                        serialNumber = 0L,
                        componentName = "",
                    ),
                    swipeDown = EblanAction(
                        eblanActionType = EblanActionType.None,
                        serialNumber = 0L,
                        componentName = "",
                    ),
                )
            }

            is PinItemRequestType.ShortcutInfo -> {
                val eblanApplicationInfoIcon =
                    packageManagerWrapper.getComponentName(packageName = pinItemRequestType.packageName)
                        ?.let {
                            val directory = fileManager.getFilesDirectory(FileManager.ICONS_DIR)

                            val file = File(
                                directory,
                                iconKeyGenerator.getActivityIconKey(
                                    serialNumber = pinItemRequestType.serialNumber,
                                    componentName = it,
                                ),
                            )

                            file.absolutePath
                        }

                val data = ShortcutInfo(
                    shortcutId = pinItemRequestType.shortcutId,
                    packageName = pinItemRequestType.packageName,
                    serialNumber = pinItemRequestType.serialNumber,
                    shortLabel = pinItemRequestType.shortLabel,
                    longLabel = pinItemRequestType.longLabel,
                    icon = pinItemRequestType.icon,
                    isEnabled = pinItemRequestType.isEnabled,
                    eblanApplicationInfoIcon = eblanApplicationInfoIcon,
                    customIcon = null,
                    customShortLabel = null,
                    index = -1,
                    folderId = null,
                )

                GridItem(
                    id = pinItemRequestType.shortcutId,
                    page = homeSettings.initialPage,
                    startColumn = 0,
                    startRow = 0,
                    columnSpan = 1,
                    rowSpan = 1,
                    data = data,
                    associate = Associate.Grid,
                    override = false,
                    gridItemSettings = homeSettings.gridItemSettings,
                    doubleTap = EblanAction(
                        eblanActionType = EblanActionType.None,
                        serialNumber = 0L,
                        componentName = "",
                    ),
                    swipeUp = EblanAction(
                        eblanActionType = EblanActionType.None,
                        serialNumber = 0L,
                        componentName = "",
                    ),
                    swipeDown = EblanAction(
                        eblanActionType = EblanActionType.None,
                        serialNumber = 0L,
                        componentName = "",
                    ),
                )
            }
        }
    }
}
