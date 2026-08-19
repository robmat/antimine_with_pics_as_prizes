package dev.lucasnlm.antimine.common.level.logic

import dev.lucasnlm.antimine.core.models.Area
import dev.lucasnlm.antimine.core.models.Mark
import kotlin.math.absoluteValue

class MinefieldBulkActionsImpl(
    private val field: MutableList<Area>,
) : MinefieldBulkActions {
    companion object {
        const val NEAR_MINE_THRESHOLD = 5
    }

    override fun showAllMines() {
        field.filter { it.hasMine && it.mark != Mark.Flag }
            .forEach { field[it.id] = it.copy(isCovered = false) }
    }

    override fun showAllWrongFlags() {
        field.filter { !it.hasMine && it.mark.isNotNone() }
            .forEach { field[it.id] = it.copy(mistake = true) }
    }

    override fun flagAllMines() {
        field.filter { it.hasMine && it.isCovered }
            .forEach { field[it.id] = it.copy(mark = Mark.Flag) }
    }

    override fun revealAllEmptyAreas() {
        field.filterNot { it.hasMine }
            .forEach { field[it.id] = it.copy(isCovered = false) }
    }

    override fun dismissMistake() {
        field.filter { it.hasMine && it.mistake }
            .forEach { field[it.id] = it.copy(mistake = false) }
    }

    override fun revealRandomMineNearUncoveredArea(
        lastX: Int?,
        lastY: Int?,
    ): Int? {
        val unrevealedMines = field.filter { it.hasMine && it.mark.isNone() && !it.revealed && it.isCovered }
        val nearestTarget =
            if (lastX != null && lastY != null) {
                unrevealedMines.filter {
                    (lastX - it.posX).absoluteValue < NEAR_MINE_THRESHOLD &&
                        (lastY - it.posY).absoluteValue < NEAR_MINE_THRESHOLD
                }.shuffled().firstOrNull()
            } else {
                null
            }

        return when {
            nearestTarget != null -> {
                field[nearestTarget.id] = nearestTarget.copy(revealed = true)
                nearestTarget.id
            }
            else -> {
                unrevealedMines.shuffled().firstOrNull()?.run {
                    field[this.id] = this.copy(revealed = true)
                    this.id
                }
            }
        }
    }
}
