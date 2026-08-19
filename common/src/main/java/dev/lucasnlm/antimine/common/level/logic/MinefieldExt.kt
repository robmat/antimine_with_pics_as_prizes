package dev.lucasnlm.antimine.common.level.logic

import dev.lucasnlm.antimine.core.models.Area
import dev.lucasnlm.antimine.preferences.models.Minefield
import kotlin.math.absoluteValue
import kotlin.math.floor

fun List<Area>.withId(id: Int) = this.first { it.id == id }

/**
 * Builds an empty [Area] list matching this [Minefield]'s dimensions, with no
 * mines placed and neighbors already linked up.
 */
fun Minefield.createEmptyAreas(): List<Area> {
    val fieldLength = width * height

    val list =
        (0 until fieldLength).map { index ->
            val yPosition = floor((index / width).toDouble()).toInt()
            val xPosition = (index % width)
            Area(
                index,
                xPosition,
                yPosition,
                0,
                hasMine = false,
                neighborsIds = emptyList(),
            )
        }

    return list.map {
        it.copy(neighborsIds = list.filterNeighborsOf(it).map { area -> area.id })
    }
}

fun List<Area>.filterNeighborsOf(area: Area) =
    this.filter {
        ((it.posX - area.posX).absoluteValue <= 1 && (it.posY - area.posY).absoluteValue <= 1)
    }.filterNot { area.id == it.id }

fun List<Area>.filterNotNeighborsOf(area: Area) =
    this.filter {
        ((it.posX - area.posX).absoluteValue > 1 || (it.posY - area.posY).absoluteValue > 1)
    }

fun List<Area>.filterNeighborsOf(areaId: Int) = this.filterNeighborsOf(this.withId(areaId))

fun List<Area>.filterNotNeighborsOf(areaId: Int) = this.filterNotNeighborsOf(this.withId(areaId))
