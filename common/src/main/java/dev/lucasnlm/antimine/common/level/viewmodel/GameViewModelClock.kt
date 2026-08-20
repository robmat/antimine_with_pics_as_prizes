package dev.lucasnlm.antimine.common.level.viewmodel

import dev.lucasnlm.antimine.common.level.logic.hasIsolatedAllMines
import dev.lucasnlm.antimine.common.level.logic.remainingMines
import dev.lucasnlm.antimine.common.level.logic.revealAllEmptyAreas
import dev.lucasnlm.antimine.common.level.logic.revealRandomMine
import dev.lucasnlm.antimine.common.level.logic.showAllMistakes
import dev.lucasnlm.antimine.common.level.logic.showWrongFlags

/**
 * Game clock and reveal-mines/hint entry points, split out of [GameViewModel]
 * - see its class doc.
 */
internal fun GameViewModel.runClock() {
    clock.run {
        if (isStopped) {
            start {
                sendEvent(GameEvent.UpdateTime(it))
            }
        }
    }
}

internal fun GameViewModel.stopClock() {
    clock.stop()
}

internal fun GameViewModel.showAllEmptyAreas() {
    gameController.revealAllEmptyAreas()
}

fun GameViewModel.revealRandomMine(consume: Boolean = true): Int? =
    if (initialized) {
        val result = gameController.revealRandomMine()

        if (result != null) {
            soundManager.playRevealBomb()

            if (consume) {
                sendEvent(GameEvent.ConsumeTip)
            }
        }
        result
    } else {
        null
    }

internal fun GameViewModel.explosionDelay() = if (preferencesRepository.useAnimations()) GameViewModel.EXPLOSION_DELAY else 0L

fun GameViewModel.hasUnknownMines(): Boolean = !gameController.hasIsolatedAllMines() && gameController.remainingMines() > 1

fun GameViewModel.revealMines() {
    if (initialized) {
        gameController.run {
            showWrongFlags()
            showAllMistakes()
            refreshField()
        }
    }
}
