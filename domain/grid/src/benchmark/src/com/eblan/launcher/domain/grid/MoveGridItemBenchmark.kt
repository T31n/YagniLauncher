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
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Level
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Param
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.Warmup
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.TimeUnit
import kotlin.random.Random

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(2)
class MoveGridItemBenchmark {
    @Param("100", "500", "1000")
    var itemCount: Int = 10

    private val columns = 5

    // Worst case: every item takes the max span (2) and still only fits
    // one per row before wrapping — so itemCount rows is always enough
    // headroom, regardless of how itemCount scales across @Param values.
    private val rows: Int
        get() = itemCount + 1

    // Template list, built once per trial. Never passed directly to
    // resolveConflicts — always deep-copied per invocation, since the
    // function mutates its input and JMH benchmark methods run many
    // invocations per iteration.
    private lateinit var template: List<GridItem>
    private lateinit var movingGridItem: GridItem

    @Setup(Level.Trial)
    fun setUp() {
        val random = Random(seed = 42)

        val items = mutableListOf<GridItem>()
        var column = 0
        var row = 0

        repeat(itemCount) { index ->
            val columnSpan = random.nextInt(1, 3)
            val rowSpan = random.nextInt(1, 3)

            if (column + columnSpan > columns) {
                column = 0
                row += 1
            }

            val candidate = getGridItem(
                id = "item-$index",
                startColumn = column,
                startRow = row,
                columnSpan = columnSpan,
                rowSpan = rowSpan,
            )

            check(isGridItemSpanWithinBounds(candidate, columns, rows)) {
                "Generated out-of-bounds GridItem at index $index — increase `rows`"
            }

            items += candidate
            column += columnSpan
        }

        template = items
        movingGridItem = items.first().copy(
            startColumn = 0,
            startRow = 0,
        )
    }

    @Benchmark
    fun resolveConflictsRight(blackhole: Blackhole) = runBlocking {
        val gridItems = template.map { it.copy() }.toMutableList()

        val result = resolveConflicts(
            gridItems = gridItems,
            resolveDirection = ResolveDirection.Right,
            movingGridItem = movingGridItem,
            columns = columns,
            rows = rows,
        )

        blackhole.consume(result)
        blackhole.consume(gridItems)
    }

    @Benchmark
    fun resolveConflictsLeft(blackhole: Blackhole) = runBlocking {
        val gridItems = template.map { it.copy() }.toMutableList()

        val result = resolveConflicts(
            gridItems = gridItems,
            resolveDirection = ResolveDirection.Left,
            movingGridItem = movingGridItem,
            columns = columns,
            rows = rows,
        )

        blackhole.consume(result)
        blackhole.consume(gridItems)
    }

    private fun getGridItem(
        id: String,
        startColumn: Int,
        startRow: Int,
        columnSpan: Int,
        rowSpan: Int,
    ) = GridItem(
        id = id,
        page = 0,
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
