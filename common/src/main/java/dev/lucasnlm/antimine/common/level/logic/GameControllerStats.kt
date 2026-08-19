package dev.lucasnlm.antimine.common.level.logic

import dev.lucasnlm.antimine.core.models.Area
import dev.lucasnlm.antimine.core.models.Score

/**
 * Read-only score/mine stats over the current field, split out of
 * [GameController] - see its class doc.
 */
fun GameController.getScore() =
    Score(
        mines().count { !it.mistake },
        getMinesCount(),
        field.count(),
    )

fun GameController.getMinesCount() = mines().count()

fun GameController.findExplodedMine() = mines().firstOrNull { it.mistake }

fun GameController.takeExplosionRadius(target: Area): List<Area> =
    mines().filter { it.isCovered && it.mark.isNone() }.sortedBy {
        val dx1 = (it.posX - target.posX)
        val dy1 = (it.posY - target.posY)
        dx1 * dx1 + dy1 * dy1
    }

fun GameController.hasAnyMineExploded(): Boolean = mines().firstOrNull { it.mistake } != null

internal fun GameController.explodedMinesCount(): Int = mines().count { !it.isCovered && it.hasMine }
