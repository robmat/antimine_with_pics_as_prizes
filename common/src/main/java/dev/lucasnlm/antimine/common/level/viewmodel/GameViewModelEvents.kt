package dev.lucasnlm.antimine.common.level.viewmodel

import dev.lucasnlm.antimine.common.level.logic.allMinesFound
import dev.lucasnlm.antimine.common.level.logic.isGameOver
import dev.lucasnlm.antimine.common.level.logic.isVictory
import dev.lucasnlm.antimine.common.level.logic.remainingMines
import kotlinx.coroutines.flow.FlowCollector

/**
 * [GameEvent] to [GameState] mapping, split out of [GameViewModel] - see its
 * class doc. Each handler takes the [FlowCollector] explicitly (rather than as
 * a receiver) since a receiver-extension can only be a class member.
 */
internal suspend fun GameViewModel.onGiveMoreTip(collector: FlowCollector<GameState>) {
    tipRepository.increaseTip(GameViewModel.TIP_INCREASE_AMOUNT)

    val newState =
        gameState.copy(
            hints = tipRepository.getTotalTips(),
        )

    collector.emit(newState)
}

internal suspend fun GameViewModel.onConsumeTip(collector: FlowCollector<GameState>) {
    if (tipRepository.removeTip()) {
        val newState =
            gameState.copy(
                field = gameController.field(),
                hints = tipRepository.getTotalTips(),
            )
        collector.emit(newState)
    }
}

internal suspend fun GameViewModel.onContinueGame(collector: FlowCollector<GameState>) {
    onContinueFromGameOver()
    runClock()
    val newState =
        gameState.copy(
            isActive = !gameController.allMinesFound(),
            isGameCompleted = gameController.remainingMines() == 0,
        )
    collector.emit(newState)
}

internal suspend fun GameViewModel.onEngineReady(collector: FlowCollector<GameState>) {
    collector.emit(gameState.copy(isLoadingMap = false))

    if (!gameState.isGameCompleted && gameState.hasMines && !gameState.isLoadingMap) {
        if (
            !gameController.isGameOver() &&
            !gameController.isVictory() &&
            gameController.remainingMines() > 1
        ) {
            runClock()
        }
    } else {
        stopClock()
    }
}

internal suspend fun GameViewModel.onLoadingNewGame(collector: FlowCollector<GameState>) {
    stopClock()
    collector.emit(gameState.copy(isLoadingMap = true, duration = 0L, isActive = false))
}

internal suspend fun GameViewModel.onUpdateMinefield(
    collector: FlowCollector<GameState>,
    event: GameEvent.UpdateMinefield,
) {
    val isVictory = gameController.isVictory()
    val isGameOver = gameController.isGameOver()
    val isComplete = isCompletedWithMistakes()
    val wasCompleted = gameState.isGameCompleted
    val hasMines = gameController.hasMines()

    var newState =
        gameState.copy(
            turn = gameState.turn + 1,
            field = event.field,
            mineCount = gameController.remainingMines(),
            isGameCompleted = isVictory || isGameOver || isComplete,
            hasMines = hasMines,
            isCreatingGame = false,
        )

    if (!wasCompleted && handleMinefieldCompletion(isVictory, isComplete, isGameOver)) {
        newState = newState.copy(field = gameController.field())
    }

    if (!wasCompleted && hasMines && !newState.isLoadingMap) {
        runClock()
    } else {
        stopClock()
    }

    collector.emit(newState)
}
