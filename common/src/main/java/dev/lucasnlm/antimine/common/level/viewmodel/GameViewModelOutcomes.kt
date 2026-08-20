package dev.lucasnlm.antimine.common.level.viewmodel

import android.content.Context
import android.util.Log
import com.batodev.antimine.SettingsHelper
import dev.lucasnlm.antimine.common.level.logic.flagAllMines
import dev.lucasnlm.antimine.common.level.logic.getScore
import dev.lucasnlm.antimine.common.level.logic.hasIsolatedAllMines
import dev.lucasnlm.antimine.common.level.logic.revealAllEmptyAreas
import dev.lucasnlm.antimine.common.level.logic.runNumberDimmerToAllMines
import dev.lucasnlm.antimine.common.level.logic.showWrongFlags
import dev.lucasnlm.antimine.core.models.Analytics
import dev.lucasnlm.antimine.core.models.Difficulty
import dev.lucasnlm.external.Achievement
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Score reporting and victory/game-over side effects, split out of
 * [GameViewModel] - see its class doc.
 */
internal fun GameViewModel.getScore() = gameController.getScore()

internal suspend fun GameViewModel.onGameOver(useGameOverFeedback: Boolean) {
    stopClock()
    analyticsManager.sentEvent(Analytics.GameOver(clock.time(), getScore()))

    gameController.run {
        if (useGameOverFeedback) {
            if (preferencesRepository.useHapticFeedback()) {
                hapticFeedbackManager.explosionFeedback()
            }

            soundManager.playBombExplosion()
            soundManager.pauseMusic()
        }

        if (hasIsolatedAllMines()) {
            revealAllEmptyAreas()
        }

        refreshField()
        updateGameState()
    }

    if (gameState.difficulty == Difficulty.Standard || gameState.difficulty == Difficulty.FixedSize) {
        preferencesRepository.decrementProgressiveValue()
    }

    saveStats()
    saveGame()
    checkGameOverAchievements()
}

internal fun GameViewModel.addNewTip(amount: Int) {
    tipRepository.increaseTip(amount.coerceAtLeast(0))
}

fun GameViewModel.getTips(): Int = tipRepository.getTotalTips()

internal suspend fun GameViewModel.onVictory(context: Context) {
    withContext(Dispatchers.IO) {
        val settingsHelper = SettingsHelper(context)
        val preferences = settingsHelper.preferences
        if (!preferences.uncoveredPics.contains(prizeImage)) {
            preferences.uncoveredPics.add(prizeImage)
            settingsHelper.savePreferences()
            Log.d(GameViewModel::class.java.simpleName, "Won prize: $prizeImage")
        }
    }
    analyticsManager.sentEvent(
        Analytics.Victory(
            clock.time(),
            getScore(),
            gameState.difficulty,
        ),
    )

    stopClock()

    gameController.run {
        showAllEmptyAreas()
        flagAllMines()
        showWrongFlags()

        if (preferencesRepository.dimNumbers()) {
            runNumberDimmerToAllMines()
        }
    }

    if (gameState.difficulty == Difficulty.Standard || gameState.difficulty == Difficulty.FixedSize) {
        preferencesRepository.incrementProgressiveValue()
    }

    if (clock.time() < GameViewModel.THIRTY_SECONDS_ACHIEVEMENT) {
        withContext(Dispatchers.Main) {
            playGamesManager.unlockAchievement(Achievement.ThirtySeconds)
        }
    }

    checkVictoryAchievements()
    saveGame()
    saveStats()

    soundManager.playWin()

    val rewardedHints = calcRewardHints()
    if (rewardedHints > 0) {
        addNewTip(rewardedHints)
    }
}

internal fun GameViewModel.calcRewardHints(): Int =
    if (clock.time() > GameViewModel.MIN_REWARD_GAME_SECONDS && preferencesRepository.isPremiumEnabled()) {
        val rewardedHints =
            if (isCompletedWithMistakes()) {
                (gameState.minefield.mines * GameViewModel.REWARD_RATIO_WITH_MISTAKES)
            } else {
                (gameState.minefield.mines * GameViewModel.REWARD_RATIO_WITHOUT_MISTAKES)
            }

        rewardedHints.toInt().coerceAtLeast(1)
    } else {
        0
    }
