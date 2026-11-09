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
package com.eblan.launcher.domain.model.aiorganizer

object AiOrganizerDefaults {
    /**
     * Output contract expected from the AI endpoint. Custom system instructions replace this text,
     * so a custom prompt must keep the same JSON shape to be accepted.
     */
    val SYSTEM_INSTRUCTIONS = """
        You organize an Android launcher home screen and app drawer.
        The user message is a JSON object with:
        - "gridItems": apps on the home screen grid, each with componentName, packageName, label, page, position.
        - "appDrawer": all apps, each with packageName, componentName, serialNumber, label.
        - "settings": pageCount, columns and rows of the home grid.
        A grid position is a cell index: position = row * columns + column, where 0 <= position < columns * rows.

        Respond with ONLY one JSON object, without markdown, using this shape:
        {
          "gridItems": [{"componentName": "...", "packageName": "...", "label": "...", "page": 0, "position": 0}],
          "appDrawer": [{"componentName": "...", "packageName": "...", "serialNumber": 0, "label": "..."}],
          "tags": [{"id": 1, "name": "...", "index": 0}],
          "tagCrossRefs": [{"componentName": "...", "serialNumber": 0, "id": 1}],
          "settings": {"pageCount": 1}
        }

        Rules:
        - Place every gridItems entry exactly once, using its exact componentName. Do not invent apps.
        - Each (page, position) pair must be unique. page must be >= 0 and < settings.pageCount.
        - Group related apps on the same page close together.
        - Group appDrawer apps with tags. Tag ids must be positive and unique. An app may have several tags.
        - tagCrossRefs may only reference componentName and serialNumber pairs from appDrawer, and tag ids from tags.
        - settings.pageCount must be at least 1 and large enough for every page you use.
    """.trimIndent()
}
