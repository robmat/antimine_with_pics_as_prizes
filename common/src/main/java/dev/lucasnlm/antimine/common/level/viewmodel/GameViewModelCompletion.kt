package dev.lucasnlm.antimine.common.level.viewmodel

import dev.lucasnlm.antimine.common.level.logic.hadMistakes
import dev.lucasnlm.antimine.common.level.logic.hasIsolatedAllMines

/**
 * End-of-turn completion handling (victory / mistakes / game-over dialogs),
 * split out of [GameViewModel] - see its class doc.
 */
internal fun GameViewModel.isCompletedWithMistakes(): Boolean =
    gameController.hadMistakes() && gameController.hasIsolatedAllMines()

private suspend fun GameViewModel.onVictoryCompletion() {
    onVictory(context)

    val totalMines = gameController.mines().count()
    val sideEffect =
        GameEvent.VictoryDialog(
            delayToShow = 1500L,
            totalMines = totalMines,
            rightMines = totalMines,
            timestamp = gameState.duration,
            receivedTips = calcRewardHints(),
        )
    postSideEffect(sideEffect)
}

private suspend fun GameViewModel.onCompleteWithMistakesCompletion() {
    onGameOver(false)
    val sideEffect =
        GameEvent.GameCompleteDialog(
            delayToShow = 0L,
            totalMines = gameController.mines().count(),
            rightMines = gameController.mines().count { it.mark.isNotNone() },
            timestamp = gameState.duration,
            receivedTips = calcRewardHints(),
            turn = gameState.turn,
        )
    postSideEffect(sideEffect)
}

private suspend fun GameViewModel.onGameOverCompletion() {
    onGameOver(true)
    val sideEffect =
        GameEvent.GameOverDialog(
            delayToShow = explosionDelay(),
            totalMines = gameController.mines().count(),
            rightMines = gameController.mines().count { it.mark.isNotNone() },
            timestamp = gameState.duration,
            receivedTips = 0,
            turn = gameState.turn,
        )
    postSideEffect(sideEffect)
}

internal suspend fun GameViewModel.handleMinefieldCompletion(
    isVictory: Boolean,
    isComplete: Boolean,
    isGameOver: Boolean,
): Boolean {
    when {
        isVictory && !gameController.hadMistakes() -> onVictoryCompletion()
        isComplete -> onCompleteWithMistakesCompletion()
        isGameOver -> onGameOverCompletion()
        else -> return false
    }
    return true
}
