package dev.lucasnlm.antimine.common.level.viewmodel

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.batodev.antimine.ImageHelper
import dev.lucasnlm.antimine.common.level.database.models.Save
import dev.lucasnlm.antimine.common.level.logic.GameController
import dev.lucasnlm.antimine.common.level.logic.allMinesFound
import dev.lucasnlm.antimine.common.level.logic.dismissMistake
import dev.lucasnlm.antimine.common.level.logic.getErrorTolerance
import dev.lucasnlm.antimine.common.level.logic.increaseErrorTolerance
import dev.lucasnlm.antimine.common.level.logic.increaseErrorToleranceByWrongMines
import dev.lucasnlm.antimine.common.level.logic.remainingMines
import dev.lucasnlm.antimine.core.models.Analytics
import dev.lucasnlm.antimine.core.models.Difficulty
import dev.lucasnlm.antimine.preferences.models.Minefield
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Starting/resuming/retrying a game, split out of [GameViewModel] - see its
 * class doc.
 */
suspend fun GameViewModel.startNewGame(
    context: Context,
    newDifficulty: Difficulty = gameState.difficulty,
): Minefield {
    clock.reset()
    initialized = false

    val minefield =
        minefieldRepository.fromDifficulty(
            newDifficulty,
            dimensionRepository,
            preferencesRepository,
        )

    withContext(Dispatchers.IO) {
        sendEvent(GameEvent.LoadingNewGame)

        val seed = minefieldRepository.randomSeed()
        prizeImage = ImageHelper.randomImage(context)
        this@startNewGame.context = context.applicationContext
        gameController =
            GameController(
                minefield = minefield,
                seed = seed,
                useSimonTatham = preferencesRepository.useSimonTathamAlgorithm(),
                onCreateUnsafeLevel = { onCreateUnsafeLevel() },
                saveId = null,
                prizeImage = prizeImage,
            )

        val newGameState =
            GameState(
                duration = 0L,
                seed = seed,
                difficulty = newDifficulty,
                minefield = minefield,
                mineCount = minefield.mines,
                field = gameController.field(),
                hints = tipRepository.getTotalTips(),
                isGameCompleted = false,
                isActive = true,
                hasMines = false,
                isCreatingGame = false,
                useHelp = preferencesRepository.useHelp(),
                isLoadingMap = true,
                showTutorial = preferencesRepository.showTutorialButton(),
            )

        sendEvent(GameEvent.NewGame(newGameState))

        initialized = true
        refreshUserPreferences()

        analyticsManager.sentEvent(
            Analytics.NewGame(
                minefield,
                newDifficulty,
                gameController.seed,
            ),
        )
    }

    return minefield
}

internal fun GameViewModel.baseGameStateFromSave(save: Save): GameState =
    GameState(
        saveId = save.uid.toLong(),
        duration = save.duration,
        seed = save.seed,
        difficulty = save.difficulty,
        minefield = save.minefield,
        mineCount = gameController.remainingMines(),
        field = gameController.field(),
        hints = tipRepository.getTotalTips(),
        isGameCompleted = false,
        isCreatingGame = false,
        isActive = false,
        hasMines = false,
        useHelp = preferencesRepository.useHelp(),
        isLoadingMap = true,
        showTutorial = preferencesRepository.showTutorialButton(),
    )

internal fun GameViewModel.resumeGameFromSave(save: Save): Minefield {
    clock.reset(save.duration)

    sendEvent(GameEvent.LoadingNewGame)

    gameController = GameController(save, preferencesRepository.useSimonTathamAlgorithm())
    initialized = true

    refreshUserPreferences()
    prizeImage = save.prizeImage
    gameController.prizeImage = prizeImage

    val newGameState =
        baseGameStateFromSave(save).copy(
            isGameCompleted = gameController.remainingMines() == 0,
            isActive = !gameController.allMinesFound(),
            hasMines = true,
        )

    sendEvent(GameEvent.NewGame(newGameState))

    if (newGameState.isActive && !newGameState.isGameCompleted && !newGameState.isLoadingMap) {
        runClock()
    }

    gameController.increaseErrorToleranceByWrongMines()

    analyticsManager.sentEvent(Analytics.ResumePreviousGame)
    return newGameState.minefield
}

internal fun GameViewModel.retryGameFromSave(save: Save) {
    clock.reset()

    sendEvent(GameEvent.LoadingNewGame)

    gameController =
        GameController(
            minefield = save.minefield,
            seed = save.seed,
            useSimonTatham = preferencesRepository.useSimonTathamAlgorithm(),
            saveId = save.uid,
            onCreateUnsafeLevel = ::onCreateUnsafeLevel,
            prizeImage = prizeImage,
        )
    initialized = true
    refreshUserPreferences()

    val newGameState =
        baseGameStateFromSave(save).copy(
            isActive = true,
        )

    sendEvent(GameEvent.NewGame(newGameState))

    analyticsManager.sentEvent(
        Analytics.RetryGame(
            newGameState.minefield,
            newGameState.difficulty,
            newGameState.seed,
            save.firstOpen.toInt(),
        ),
    )
}

suspend fun GameViewModel.loadGame(
    uid: Int,
    context: AppCompatActivity,
): Minefield =
    withContext(Dispatchers.IO) {
        val lastGame = savesRepository.loadFromId(uid)

        if (lastGame != null) {
            resumeGameFromSave(lastGame)
        } else {
            // Fail to load
            startNewGame(context)
        }
    }

internal suspend fun GameViewModel.onContinueFromGameOver() {
    if (initialized) {
        gameController.increaseErrorTolerance()
        gameController.dismissMistake()
        statsRepository.deleteLastStats()
        analyticsManager.sentEvent(
            Analytics.ContinueGameAfterGameOver(gameController.getErrorTolerance()),
        )
    }
}
