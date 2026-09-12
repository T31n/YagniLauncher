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
package com.eblan.launcher.domain.grid

import com.eblan.launcher.domain.model.getEblanAction
import com.eblan.launcher.domain.model.getGridItemSettings
import com.eblan.launcher.domain.model.grid.Associate
import com.eblan.launcher.domain.model.grid.GridItem
import com.eblan.launcher.domain.model.grid.GridItemData
import com.eblan.launcher.domain.model.grid.ResolveDirection
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull

class GridItemConstraintsTest {
    @Nested
    inner class IsGridItemSpanWithinBounds {
        @Test
        fun `returns true when grid item is within bounds`() {
            val gridItem = getGridItem(
                startColumn = 1,
                startRow = 1,
                columnSpan = 2,
                rowSpan = 2,
            )

            assertTrue(
                isGridItemSpanWithinBounds(
                    gridItem = gridItem,
                    columns = 5,
                    rows = 4,
                ),
            )
        }

        @Test
        fun `returns true when grid item exactly reaches right boundary`() {
            val gridItem = getGridItem(
                startColumn = 3,
                startRow = 0,
                columnSpan = 2,
                rowSpan = 1,
            )

            assertTrue(
                isGridItemSpanWithinBounds(
                    gridItem = gridItem,
                    columns = 5,
                    rows = 4,
                ),
            )
        }

        @Test
        fun `returns true when grid item exactly reaches bottom boundary`() {
            val gridItem = getGridItem(
                startColumn = 0,
                startRow = 2,
                columnSpan = 1,
                rowSpan = 2,
            )

            assertTrue(
                isGridItemSpanWithinBounds(
                    gridItem = gridItem,
                    columns = 5,
                    rows = 4,
                ),
            )
        }

        @Test
        fun `returns false when start column is negative`() {
            val gridItem = getGridItem(
                startColumn = -1,
                startRow = 0,
                columnSpan = 1,
                rowSpan = 1,
            )

            assertFalse(
                isGridItemSpanWithinBounds(
                    gridItem = gridItem,
                    columns = 5,
                    rows = 4,
                ),
            )
        }

        @Test
        fun `returns false when start row is negative`() {
            val gridItem = getGridItem(
                startColumn = 0,
                startRow = -1,
                columnSpan = 1,
                rowSpan = 1,
            )

            assertFalse(
                isGridItemSpanWithinBounds(
                    gridItem = gridItem,
                    columns = 5,
                    rows = 4,
                ),
            )
        }

        @Test
        fun `returns false when column span exceeds right boundary`() {
            val gridItem = getGridItem(
                startColumn = 4,
                startRow = 0,
                columnSpan = 2,
                rowSpan = 1,
            )

            assertFalse(
                isGridItemSpanWithinBounds(
                    gridItem = gridItem,
                    columns = 5,
                    rows = 4,
                ),
            )
        }

        @Test
        fun `returns false when row span exceeds bottom boundary`() {
            val gridItem = getGridItem(
                startColumn = 0,
                startRow = 3,
                columnSpan = 1,
                rowSpan = 2,
            )

            assertFalse(
                isGridItemSpanWithinBounds(
                    gridItem = gridItem,
                    columns = 5,
                    rows = 4,
                ),
            )
        }

        @Test
        fun `returns false when grid item starts at column boundary`() {
            val gridItem = getGridItem(
                startColumn = 5,
                startRow = 0,
                columnSpan = 1,
                rowSpan = 1,
            )

            assertFalse(
                isGridItemSpanWithinBounds(
                    gridItem = gridItem,
                    columns = 5,
                    rows = 4,
                ),
            )
        }
    }

    @Nested
    inner class RectanglesOverlap {
        @Test
        fun `returns true when rectangles overlap`() {
            val moving = getGridItem(
                startColumn = 1,
                startRow = 1,
                columnSpan = 2,
                rowSpan = 2,
            )

            val other = getGridItem(
                startColumn = 2,
                startRow = 2,
                columnSpan = 2,
                rowSpan = 2,
            )

            assertTrue(
                rectanglesOverlap(
                    moving = moving,
                    other = other,
                ),
            )
        }

        @Test
        fun `returns false when rectangles are separated horizontally`() {
            val moving = getGridItem(
                startColumn = 0,
                startRow = 0,
                columnSpan = 1,
                rowSpan = 1,
            )

            val other = getGridItem(
                startColumn = 1,
                startRow = 0,
                columnSpan = 1,
                rowSpan = 1,
            )

            assertFalse(
                rectanglesOverlap(
                    moving = moving,
                    other = other,
                ),
            )
        }

        @Test
        fun `returns false when rectangles are separated vertically`() {
            val moving = getGridItem(
                startColumn = 0,
                startRow = 0,
                columnSpan = 1,
                rowSpan = 1,
            )

            val other = getGridItem(
                startColumn = 0,
                startRow = 1,
                columnSpan = 1,
                rowSpan = 1,
            )

            assertFalse(
                rectanglesOverlap(
                    moving = moving,
                    other = other,
                ),
            )
        }

        @Test
        fun `returns false when rectangles only touch at a corner`() {
            val moving = getGridItem(
                startColumn = 0,
                startRow = 0,
                columnSpan = 1,
                rowSpan = 1,
            )

            val other = getGridItem(
                startColumn = 1,
                startRow = 1,
                columnSpan = 1,
                rowSpan = 1,
            )

            assertFalse(
                rectanglesOverlap(
                    moving = moving,
                    other = other,
                ),
            )
        }

        @Test
        fun `returns true when moving rectangle is completely inside other rectangle`() {
            val moving = getGridItem(
                startColumn = 1,
                startRow = 1,
                columnSpan = 1,
                rowSpan = 1,
            )

            val other = getGridItem(
                startColumn = 0,
                startRow = 0,
                columnSpan = 3,
                rowSpan = 3,
            )

            assertTrue(
                rectanglesOverlap(
                    moving = moving,
                    other = other,
                ),
            )
        }

        @Test
        fun `returns true when other rectangle is completely inside moving rectangle`() {
            val moving = getGridItem(
                startColumn = 0,
                startRow = 0,
                columnSpan = 3,
                rowSpan = 3,
            )

            val other = getGridItem(
                startColumn = 1,
                startRow = 1,
                columnSpan = 1,
                rowSpan = 1,
            )

            assertTrue(
                rectanglesOverlap(
                    moving = moving,
                    other = other,
                ),
            )
        }

        @Test
        fun `returns same result regardless of rectangle order`() {
            val moving = getGridItem(
                startColumn = 1,
                startRow = 1,
                columnSpan = 2,
                rowSpan = 2,
            )

            val other = getGridItem(
                startColumn = 2,
                startRow = 2,
                columnSpan = 2,
                rowSpan = 2,
            )

            assertTrue(
                rectanglesOverlap(
                    moving = moving,
                    other = other,
                ),
            )

            assertTrue(
                rectanglesOverlap(
                    moving = other,
                    other = moving,
                ),
            )
        }
    }

    @Nested
    inner class GetResolveDirectionByX {
        @Test
        fun `returns right when x is in first third of grid item`() {
            val gridItem = getGridItem(
                startColumn = 0,
                startRow = 0,
                columnSpan = 3,
                rowSpan = 1,
            )

            assertEquals(
                ResolveDirection.Right,
                getResolveDirectionByX(
                    gridItem = gridItem,
                    x = 20,
                    columns = 5,
                    gridWidth = 500,
                ),
            )
        }

        @Test
        fun `returns center when x is in middle third of grid item`() {
            val gridItem = getGridItem(
                startColumn = 0,
                startRow = 0,
                columnSpan = 3,
                rowSpan = 1,
            )

            assertEquals(
                ResolveDirection.Center,
                getResolveDirectionByX(
                    gridItem = gridItem,
                    x = 150,
                    columns = 5,
                    gridWidth = 500,
                ),
            )
        }

        @Test
        fun `returns left when x is in last third of grid item`() {
            val gridItem = getGridItem(
                startColumn = 0,
                startRow = 0,
                columnSpan = 3,
                rowSpan = 1,
            )

            assertEquals(
                ResolveDirection.Left,
                getResolveDirectionByX(
                    gridItem = gridItem,
                    x = 280,
                    columns = 5,
                    gridWidth = 500,
                ),
            )
        }

        @Test
        fun `returns center when x is exactly at first third boundary`() {
            val gridItem = getGridItem(
                startColumn = 0,
                startRow = 0,
                columnSpan = 3,
                rowSpan = 1,
            )

            assertEquals(
                ResolveDirection.Center,
                getResolveDirectionByX(
                    gridItem = gridItem,
                    x = 100,
                    columns = 5,
                    gridWidth = 500,
                ),
            )
        }

        @Test
        fun `returns left when x is exactly at second third boundary`() {
            val gridItem = getGridItem(
                startColumn = 0,
                startRow = 0,
                columnSpan = 3,
                rowSpan = 1,
            )

            assertEquals(
                ResolveDirection.Left,
                getResolveDirectionByX(
                    gridItem = gridItem,
                    x = 200,
                    columns = 5,
                    gridWidth = 500,
                ),
            )
        }

        @Test
        fun `accounts for grid item starting column`() {
            val gridItem = getGridItem(
                startColumn = 2,
                startRow = 0,
                columnSpan = 2,
                rowSpan = 1,
            )

            assertEquals(
                ResolveDirection.Right,
                getResolveDirectionByX(
                    gridItem = gridItem,
                    x = 210,
                    columns = 5,
                    gridWidth = 500,
                ),
            )
        }
    }

    @Nested
    inner class GetGridItemByCoordinates {
        @Test
        fun `returns grid item when coordinates are inside its span`() {
            val gridItem = getGridItem(
                id = "target",
                startColumn = 1,
                startRow = 1,
                columnSpan = 2,
                rowSpan = 2,
            )

            val result = getGridItemByCoordinates(
                id = "moving",
                gridItems = listOf(gridItem),
                columns = 5,
                rows = 4,
                x = 150,
                y = 150,
                gridWidth = 500,
                gridHeight = 400,
            )

            assertEquals(gridItem, result)
        }

        @Test
        fun `returns null when coordinates are outside grid item span`() {
            val gridItem = getGridItem(
                id = "target",
                startColumn = 1,
                startRow = 1,
                columnSpan = 2,
                rowSpan = 2,
            )

            val result = getGridItemByCoordinates(
                id = "moving",
                gridItems = listOf(gridItem),
                columns = 5,
                rows = 4,
                x = 50,
                y = 50,
                gridWidth = 500,
                gridHeight = 400,
            )

            assertNull(result)
        }

        @Test
        fun `returns null when coordinates are inside item with same id`() {
            val gridItem = getGridItem(
                id = "target",
                startColumn = 1,
                startRow = 1,
                columnSpan = 2,
                rowSpan = 2,
            )

            val result = getGridItemByCoordinates(
                id = "target",
                gridItems = listOf(gridItem),
                columns = 5,
                rows = 4,
                x = 150,
                y = 150,
                gridWidth = 500,
                gridHeight = 400,
            )

            assertNull(result)
        }

        @Test
        fun `returns item when coordinates are inside second column of its span`() {
            val gridItem = getGridItem(
                id = "target",
                startColumn = 1,
                startRow = 1,
                columnSpan = 2,
                rowSpan = 1,
            )

            val result = getGridItemByCoordinates(
                id = "moving",
                gridItems = listOf(gridItem),
                columns = 5,
                rows = 4,
                x = 250,
                y = 150,
                gridWidth = 500,
                gridHeight = 400,
            )

            assertEquals(gridItem, result)
        }

        @Test
        fun `returns item when coordinates are inside second row of its span`() {
            val gridItem = getGridItem(
                id = "target",
                startColumn = 1,
                startRow = 1,
                columnSpan = 1,
                rowSpan = 2,
            )

            val result = getGridItemByCoordinates(
                id = "moving",
                gridItems = listOf(gridItem),
                columns = 5,
                rows = 4,
                x = 150,
                y = 250,
                gridWidth = 500,
                gridHeight = 400,
            )

            assertEquals(gridItem, result)
        }

        @Test
        fun `returns matching item when multiple grid items exist`() {
            val firstItem = getGridItem(
                id = "first",
                startColumn = 0,
                startRow = 0,
                columnSpan = 1,
                rowSpan = 1,
            )

            val secondItem = getGridItem(
                id = "second",
                startColumn = 2,
                startRow = 1,
                columnSpan = 2,
                rowSpan = 2,
            )

            val result = getGridItemByCoordinates(
                id = "moving",
                gridItems = listOf(firstItem, secondItem),
                columns = 5,
                rows = 4,
                x = 250,
                y = 150,
                gridWidth = 500,
                gridHeight = 400,
            )

            assertEquals(secondItem, result)
        }
    }

    @Nested
    inner class GetRelativeResolveDirection {
        @Test
        fun `returns right when moving item is left of other`() {
            val moving = getGridItem(
                startColumn = 0,
                startRow = 0,
                columnSpan = 1,
                rowSpan = 1,
            )

            val other = getGridItem(
                startColumn = 1,
                startRow = 0,
                columnSpan = 1,
                rowSpan = 1,
            )

            assertEquals(
                ResolveDirection.Right,
                getRelativeResolveDirection(
                    moving = moving,
                    other = other,
                ),
            )
        }

        @Test
        fun `returns left when moving item is right of other`() {
            val moving = getGridItem(
                startColumn = 2,
                startRow = 0,
                columnSpan = 1,
                rowSpan = 1,
            )

            val other = getGridItem(
                startColumn = 1,
                startRow = 0,
                columnSpan = 1,
                rowSpan = 1,
            )

            assertEquals(
                ResolveDirection.Left,
                getRelativeResolveDirection(
                    moving = moving,
                    other = other,
                ),
            )
        }

        @Test
        fun `returns left when moving item is below other`() {
            val moving = getGridItem(
                startColumn = 0,
                startRow = 1,
                columnSpan = 1,
                rowSpan = 1,
            )

            val other = getGridItem(
                startColumn = 0,
                startRow = 0,
                columnSpan = 1,
                rowSpan = 1,
            )

            assertEquals(
                ResolveDirection.Left,
                getRelativeResolveDirection(
                    moving = moving,
                    other = other,
                ),
            )
        }

        @Test
        fun `returns right when moving item is above other`() {
            val moving = getGridItem(
                startColumn = 0,
                startRow = 0,
                columnSpan = 1,
                rowSpan = 1,
            )

            val other = getGridItem(
                startColumn = 0,
                startRow = 1,
                columnSpan = 1,
                rowSpan = 1,
            )

            assertEquals(
                ResolveDirection.Right,
                getRelativeResolveDirection(
                    moving = moving,
                    other = other,
                ),
            )
        }

        @Test
        fun `returns null when items have the same starting position`() {
            val moving = getGridItem(
                startColumn = 1,
                startRow = 1,
                columnSpan = 1,
                rowSpan = 1,
            )

            val other = getGridItem(
                startColumn = 1,
                startRow = 1,
                columnSpan = 1,
                rowSpan = 1,
            )

            assertNull(
                getRelativeResolveDirection(
                    moving = moving,
                    other = other,
                ),
            )
        }

        @Test
        fun `prioritizes horizontal direction when both column and row differ`() {
            val moving = getGridItem(
                startColumn = 0,
                startRow = 1,
                columnSpan = 1,
                rowSpan = 1,
            )

            val other = getGridItem(
                startColumn = 1,
                startRow = 0,
                columnSpan = 1,
                rowSpan = 1,
            )

            assertEquals(
                ResolveDirection.Right,
                getRelativeResolveDirection(
                    moving = moving,
                    other = other,
                ),
            )
        }
    }

    @Nested
    inner class FindAvailableRegionByPage {
        @Test
        fun `returns first available region`() {
            val gridItem = getGridItem(
                startColumn = 0,
                startRow = 0,
                columnSpan = 1,
                rowSpan = 1,
            )

            val result = runBlocking {
                findAvailableRegionByPage(
                    gridItems = emptyList(),
                    gridItem = gridItem,
                    pageCount = 1,
                    columns = 5,
                    rows = 4,
                )
            }

            assertEquals(
                gridItem.copy(
                    page = 0,
                    startColumn = 0,
                    startRow = 0,
                ),
                result,
            )
        }

        @Test
        fun `skips occupied region`() {
            val gridItem = getGridItem(
                startColumn = 0,
                startRow = 0,
                columnSpan = 1,
                rowSpan = 1,
            )

            val occupiedItem = getGridItem(
                id = "occupied",
                startColumn = 0,
                startRow = 0,
                columnSpan = 1,
                rowSpan = 1,
            )

            val result = runBlocking {
                findAvailableRegionByPage(
                    gridItems = listOf(occupiedItem),
                    gridItem = gridItem,
                    pageCount = 1,
                    columns = 5,
                    rows = 4,
                )
            }

            assertEquals(
                gridItem.copy(
                    page = 0,
                    startColumn = 1,
                    startRow = 0,
                ),
                result,
            )
        }

        @Test
        fun `searches columns from left to right before moving to next row`() {
            val gridItem = getGridItem(
                startColumn = 0,
                startRow = 0,
                columnSpan = 1,
                rowSpan = 1,
            )

            val occupiedItems = listOf(
                getGridItem(
                    id = "occupied-1",
                    startColumn = 0,
                    startRow = 0,
                    columnSpan = 1,
                    rowSpan = 1,
                ),
                getGridItem(
                    id = "occupied-2",
                    startColumn = 1,
                    startRow = 0,
                    columnSpan = 1,
                    rowSpan = 1,
                ),
            )

            val result = runBlocking {
                findAvailableRegionByPage(
                    gridItems = occupiedItems,
                    gridItem = gridItem,
                    pageCount = 1,
                    columns = 5,
                    rows = 4,
                )
            }

            assertEquals(
                gridItem.copy(
                    page = 0,
                    startColumn = 2,
                    startRow = 0,
                ),
                result,
            )
        }

        @Test
        fun `moves to next row when current row has no available region`() {
            val gridItem = getGridItem(
                startColumn = 0,
                startRow = 0,
                columnSpan = 1,
                rowSpan = 1,
            )

            val occupiedItems = (0 until 5).map { column ->
                getGridItem(
                    id = "occupied-$column",
                    startColumn = column,
                    startRow = 0,
                    columnSpan = 1,
                    rowSpan = 1,
                )
            }

            val result = runBlocking {
                findAvailableRegionByPage(
                    gridItems = occupiedItems,
                    gridItem = gridItem,
                    pageCount = 1,
                    columns = 5,
                    rows = 4,
                )
            }

            assertEquals(
                gridItem.copy(
                    page = 0,
                    startColumn = 0,
                    startRow = 1,
                ),
                result,
            )
        }

        @Test
        fun `moves to next page when current page has no available region`() {
            val gridItem = getGridItem(
                startColumn = 0,
                startRow = 0,
                columnSpan = 1,
                rowSpan = 1,
            )

            val occupiedItems = buildList {
                repeat(4) { row ->
                    repeat(5) { column ->
                        add(
                            getGridItem(
                                id = "occupied-$row-$column",
                                page = 0,
                                startColumn = column,
                                startRow = row,
                                columnSpan = 1,
                                rowSpan = 1,
                            ),
                        )
                    }
                }
            }

            val result = runBlocking {
                findAvailableRegionByPage(
                    gridItems = occupiedItems,
                    gridItem = gridItem,
                    pageCount = 2,
                    columns = 5,
                    rows = 4,
                )
            }

            assertEquals(
                gridItem.copy(
                    page = 1,
                    startColumn = 0,
                    startRow = 0,
                ),
                result,
            )
        }

        @Test
        fun `returns null when all pages are full`() {
            val gridItem = getGridItem(
                startColumn = 0,
                startRow = 0,
                columnSpan = 1,
                rowSpan = 1,
            )

            val occupiedItems = buildList {
                repeat(2) { page ->
                    repeat(4) { row ->
                        repeat(5) { column ->
                            add(
                                getGridItem(
                                    id = "occupied-$page-$row-$column",
                                    page = page,
                                    startColumn = column,
                                    startRow = row,
                                    columnSpan = 1,
                                    rowSpan = 1,
                                ),
                            )
                        }
                    }
                }
            }

            val result = runBlocking {
                findAvailableRegionByPage(
                    gridItems = occupiedItems,
                    gridItem = gridItem,
                    pageCount = 2,
                    columns = 5,
                    rows = 4,
                )
            }

            assertNull(result)
        }

        @Test
        fun `finds region large enough for multi-cell grid item`() {
            val gridItem = getGridItem(
                startColumn = 0,
                startRow = 0,
                columnSpan = 2,
                rowSpan = 2,
            )

            val occupiedItem = getGridItem(
                id = "occupied",
                startColumn = 0,
                startRow = 0,
                columnSpan = 1,
                rowSpan = 1,
            )

            val result = runBlocking {
                findAvailableRegionByPage(
                    gridItems = listOf(occupiedItem),
                    gridItem = gridItem,
                    pageCount = 1,
                    columns = 5,
                    rows = 4,
                )
            }

            assertEquals(
                gridItem.copy(
                    page = 0,
                    startColumn = 1,
                    startRow = 0,
                ),
                result,
            )
        }

        @Test
        fun `ignores grid items on other pages`() {
            val gridItem = getGridItem(
                startColumn = 0,
                startRow = 0,
                columnSpan = 1,
                rowSpan = 1,
            )

            val occupiedItem = getGridItem(
                id = "occupied",
                page = 1,
                startColumn = 0,
                startRow = 0,
                columnSpan = 1,
                rowSpan = 1,
            )

            val result = runBlocking {
                findAvailableRegionByPage(
                    gridItems = listOf(occupiedItem),
                    gridItem = gridItem,
                    pageCount = 2,
                    columns = 5,
                    rows = 4,
                )
            }

            assertEquals(
                gridItem.copy(
                    page = 0,
                    startColumn = 0,
                    startRow = 0,
                ),
                result,
            )
        }
    }

    private fun getGridItem(
        id: String = "Test",
        page: Int = 0,
        startColumn: Int,
        startRow: Int,
        columnSpan: Int,
        rowSpan: Int,
    ) = GridItem(
        id = id,
        page = page,
        startColumn = startColumn,
        startRow = startRow,
        columnSpan = columnSpan,
        rowSpan = rowSpan,
        data = GridItemData.Folder(
            label = "Test",
            icon = null,
            index = 0,
            folderId = null,
        ),
        associate = Associate.Grid,
        override = false,
        gridItemSettings = getGridItemSettings(),
        doubleTap = getEblanAction(),
        swipeUp = getEblanAction(),
        swipeDown = getEblanAction(),
    )
}
