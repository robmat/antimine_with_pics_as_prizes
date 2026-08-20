package dev.lucasnlm.antimine.common.level.logic

/**
 * Victory/game-over condition queries, split out of [GameController] - see
 * its class doc.
 */
fun GameController.hasFlaggedAllMines(): Boolean = rightFlags() == minefield.mines

fun GameController.hasIsolatedAllMines(): Boolean = field.count { area -> !area.hasMine && area.isCovered } == 0

fun GameController.rightFlags() = mines().count { it.mark.isFlag() }

fun GameController.isVictory(): Boolean = hasMines() && hasIsolatedAllMines() && !hasAnyMineExploded()

fun GameController.isGameOver(): Boolean = hasIsolatedAllMines() || (explodedMinesCount() > errorTolerance)

fun GameController.allMinesFound(): Boolean = mines().count { !it.isCovered || it.mark.isNotNone() } == mines().count()

fun GameController.remainingMines(): Int {
    val flagsCount = field.count { it.isCovered && it.mark.isFlag() }
    val minesCount = mines().count()
    val openMinesCount = mines().count { !it.isCovered }
    return (minesCount - flagsCount - openMinesCount)
}

fun GameController.almostAchievement(): Boolean = mines().count() - mines().count { it.isCovered && it.mark.isFlag() } == 1
