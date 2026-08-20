package dev.lucasnlm.antimine.common.level.logic

import dev.lucasnlm.antimine.core.models.Area
import dev.lucasnlm.antimine.preferences.models.Minefield
import java.util.Random

class MinefieldCreatorImpl(
    private val minefield: Minefield,
    private val seed: Long,
) : MinefieldCreator {
    override fun createEmpty(): List<Area> = minefield.createEmptyAreas()

    override suspend fun create(safeIndex: Int): List<Area> =
        minefield.createEmptyAreas().toMutableList().apply {
            filterNotNeighborsOf(safeIndex)
                .shuffled(Random(seed))
                .take(minefield.mines)
                .onEach {
                    it.neighborsIds.forEach { neighborId ->
                        val neighbor = this[neighborId]
                        this[neighborId] =
                            this[neighborId].copy(
                                minesAround = neighbor.minesAround + 1,
                            )
                    }
                }.onEach {
                    this[it.id] = this[it.id].copy(hasMine = true, minesAround = 0)
                }
        }
}
