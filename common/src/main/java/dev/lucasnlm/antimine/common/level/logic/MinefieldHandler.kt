package dev.lucasnlm.antimine.common.level.logic

import dev.lucasnlm.antimine.core.models.Area
import dev.lucasnlm.antimine.core.models.Mark

class MinefieldHandler(
    private val field: MutableList<Area>,
    private val useQuestionMark: Boolean,
    private val individualActions: Boolean,
) : MinefieldBulkActions by MinefieldBulkActionsImpl(field) {
    fun removeMarkAt(index: Int) {
        field.getOrNull(index)?.let {
            field[it.id] = it.copy(mark = Mark.PurposefulNone)
        }
    }

    fun toggleMarkAt(
        index: Int,
        mark: Mark,
    ) {
        field.getOrNull(index)?.let {
            field[it.id] =
                if (it.mark.isNone()) {
                    it.copy(mark = mark)
                } else {
                    it.copy(mark = Mark.None)
                }
        }
    }

    fun switchMarkAt(index: Int) {
        field.getOrNull(index)?.let {
            if (it.isCovered) {
                field[index] =
                    it.copy(
                        mark =
                        when (it.mark) {
                            Mark.PurposefulNone, Mark.None -> Mark.Flag
                            Mark.Flag -> if (useQuestionMark && !individualActions) Mark.Question else Mark.None
                            Mark.Question -> Mark.None
                        },
                    )
            }
        }
    }

    fun openAt(
        index: Int,
        passive: Boolean,
        openNeighbors: Boolean = true,
    ) {
        field.getOrNull(index)?.run {
            if (isCovered) {
                field[index] =
                    copy(
                        isCovered = false,
                        mark = Mark.None,
                        mistake = (!passive && hasMine) || (!hasMine && mark.isFlag()),
                    )

                if (!hasMine && minesAround == 0 && openNeighbors) {
                    neighborsIds
                        .map { field[it] }
                        .filter { it.isCovered }
                        .onEach {
                            openAt(it.id, openNeighbors = true, passive = true)
                        }.count()
                }
            }
        }
    }

    private fun openUnflaggedCoveredNeighbors(neighbors: List<Area>) {
        neighbors
            .filter { it.isCovered && it.mark.isNone() }
            .forEach { openAt(it.id, passive = false, openNeighbors = true) }
    }

    private fun flagCoveredNeighborsIfAllAreMines(neighbors: List<Area>) {
        val coveredNeighbors = neighbors.filter { it.isCovered }
        val minesAmongNeighbors = neighbors.count { it.hasMine && it.isCovered }
        if (coveredNeighbors.count() == minesAmongNeighbors) {
            coveredNeighbors.filter {
                it.mark.isNone()
            }.forEach {
                switchMarkAt(it.id)
            }
        }
    }

    fun openOrFlagNeighborsOf(index: Int) {
        field.getOrNull(index)?.run {
            if (!isCovered) {
                val neighbors = neighborsIds.map { field[it] }
                val flaggedCount = neighbors.count { it.mark.isFlag() || (!it.isCovered && it.hasMine) }
                if (flaggedCount >= minesAround) {
                    openUnflaggedCoveredNeighbors(neighbors)
                } else {
                    flagCoveredNeighborsIfAllAreMines(neighbors)
                }
            }
        }
    }

    fun openNeighborsOf(index: Int) {
        field.getOrNull(index)?.run {
            if (!isCovered) {
                val neighbors = neighborsIds.map { field[it] }
                neighbors
                    .filter { it.isCovered && it.mark.isNone() }
                    .forEach { openAt(it.id, passive = false, openNeighbors = true) }
            }
        }
    }

    fun result(): List<Area> = field.toList()
}
