package dev.lucasnlm.antimine.common.level.logic

import dev.lucasnlm.antimine.common.level.database.models.Save
import dev.lucasnlm.antimine.common.level.database.models.SaveStatus
import dev.lucasnlm.antimine.common.level.database.models.Stats
import dev.lucasnlm.antimine.core.models.Difficulty

/**
 * Save/stats persistence, split out of [GameController] - see its class doc.
 */
fun GameController.getSaveState(
    duration: Long,
    difficulty: Difficulty,
): Save {
    val saveStatus: SaveStatus =
        when {
            isVictory() -> SaveStatus.VICTORY
            isGameOver() -> SaveStatus.DEFEAT
            else -> SaveStatus.ON_GOING
        }
    return Save(
        saveId,
        seed = seed,
        startDate = startTime,
        duration = duration,
        minefield = minefield,
        difficulty = difficulty,
        firstOpen = firstOpen,
        status = saveStatus,
        field = field.toList(),
        actions = actions,
        prizeImage = prizeImage,
    )
}

fun GameController.getStats(duration: Long): Stats? {
    val gameStatus: SaveStatus =
        when {
            isVictory() -> SaveStatus.VICTORY
            isGameOver() -> SaveStatus.DEFEAT
            else -> SaveStatus.ON_GOING
        }
    return if (gameStatus == SaveStatus.ON_GOING) {
        null
    } else {
        Stats(
            0,
            duration,
            getMinesCount(),
            if (gameStatus == SaveStatus.VICTORY) 1 else 0,
            minefield.width,
            minefield.height,
            mines().count { !it.isCovered },
        )
    }
}

fun GameController.setCurrentSaveId(id: Int) {
    this.saveId = id.coerceAtLeast(0)
}

fun GameController.getActionsCount() = actions
