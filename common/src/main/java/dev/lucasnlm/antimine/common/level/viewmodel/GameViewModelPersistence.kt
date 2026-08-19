package dev.lucasnlm.antimine.common.level.viewmodel

import androidx.appcompat.app.AppCompatActivity
import dev.lucasnlm.antimine.common.level.database.models.FirstOpen
import dev.lucasnlm.antimine.common.level.logic.getSaveState
import dev.lucasnlm.antimine.common.level.logic.getStats
import dev.lucasnlm.antimine.common.level.logic.isGameOver
import dev.lucasnlm.antimine.common.level.logic.setCurrentSaveId
import dev.lucasnlm.antimine.common.level.logic.singleClick
import dev.lucasnlm.antimine.preferences.models.Minefield
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.withContext

/**
 * Save/load/retry/pause entry points, split out of [GameViewModel] - see its
 * class doc.
 */
suspend fun GameViewModel.retryGame(uid: Int, context: AppCompatActivity): Minefield =
    withContext(Dispatchers.IO) {
        val save = savesRepository.loadFromId(uid)

        if (save != null) {
            retryGameFromSave(save)

            withContext(Dispatchers.Main) {
                if (save.firstOpen is FirstOpen.Position) {
                    gameController
                        .singleClick(save.firstOpen.value)
                        .filterNotNull()
                        .collect { refreshField() }
                }
            }

            save.minefield
        } else {
            // Fail to load
            startNewGame(context)
        }
    }

suspend fun GameViewModel.loadLastGame(context: AppCompatActivity): Minefield =
    withContext(Dispatchers.IO) {
        val lastGame = savesRepository.fetchCurrentSave()

        if (lastGame != null) {
            resumeGameFromSave(lastGame)
        } else {
            // Fail to load
            startNewGame(context)
        }
    }

fun GameViewModel.pauseGame() {
    if (initialized) {
        if (gameController.hasMines()) {
            sendEvent(GameEvent.SetGameActivation(false))
        }
        clock.stop()
    }
}

suspend fun GameViewModel.saveGame() {
    if (!initialized || !gameController.hasMines()) {
        return
    }

    val id =
        savesRepository.saveGame(
            gameController.getSaveState(gameState.duration, gameState.difficulty),
        ) ?: return

    gameController.setCurrentSaveId(id.toInt())
    sendEvent(GameEvent.UpdateSave(id))
}

internal suspend fun GameViewModel.saveStats() {
    if (!initialized || !gameController.hasMines()) {
        return
    }

    val stats = gameController.getStats(gameState.duration) ?: return
    statsRepository.addStats(stats)
}

fun GameViewModel.resumeGame() {
    if (initialized && gameController.hasMines() && !gameController.isGameOver()) {
        sendEvent(GameEvent.SetGameActivation(true))
    }
}
