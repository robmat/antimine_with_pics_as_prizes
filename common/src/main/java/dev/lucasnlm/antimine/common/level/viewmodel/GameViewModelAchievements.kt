package dev.lucasnlm.antimine.common.level.viewmodel

import androidx.lifecycle.viewModelScope
import dev.lucasnlm.antimine.common.level.logic.almostAchievement
import dev.lucasnlm.antimine.common.level.logic.getActionsCount
import dev.lucasnlm.antimine.core.models.Difficulty
import dev.lucasnlm.external.Achievement
import dev.lucasnlm.external.Leaderboard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Play Games achievements/leaderboards, split out of [GameViewModel] - see
 * its class doc.
 */
internal fun GameViewModel.difficultyLeaderboard(difficulty: Difficulty): Leaderboard? =
    when (difficulty) {
        Difficulty.Beginner -> Leaderboard.BeginnerBestTime
        Difficulty.Intermediate -> Leaderboard.IntermediateBestTime
        Difficulty.Expert -> Leaderboard.ExpertBestTime
        Difficulty.Master -> Leaderboard.MasterBestTime
        Difficulty.Legend -> Leaderboard.LegendaryBestTime
        else -> null
    }

internal suspend fun GameViewModel.incrementFlagsAchievementIfAny() {
    val flaggedCount = gameState.field.count { it.mark.isFlag() }
    if (flaggedCount > 0) {
        withContext(Dispatchers.Main) {
            playGamesManager.incrementAchievement(Achievement.Flags, flaggedCount)
        }
    }
}

internal suspend fun GameViewModel.updateVictoryStepAchievements() {
    val victories = statsRepository.getAllStats(0).count { it.victory == 1 }
    if (victories > 0) {
        viewModelScope.launch(Dispatchers.Main) {
            playGamesManager.setAchievementSteps(Achievement.Beginner, victories)
        }

        viewModelScope.launch(Dispatchers.Main) {
            playGamesManager.setAchievementSteps(Achievement.Intermediate, victories)
        }

        viewModelScope.launch(Dispatchers.Main) {
            playGamesManager.setAchievementSteps(Achievement.Expert, victories)
        }
    }
}

internal suspend fun GameViewModel.submitBestTimeIfEligible(time: Long) {
    if (time > 1L && gameController.getActionsCount() > GameViewModel.MIN_ACTION_TO_REWARD) {
        val board = difficultyLeaderboard(gameState.difficulty)
        board?.let {
            playGamesManager.submitLeaderboard(it, time)
        }

        updateVictoryStepAchievements()
    }
}

internal suspend fun GameViewModel.checkVictoryAchievements() {
    incrementFlagsAchievementIfAny()
    submitBestTimeIfEligible(clock.time())
}

internal fun GameViewModel.checkGameOverAchievements() =
    with(gameController) {
        viewModelScope.launch {
            if (getActionsCount() < GameViewModel.MIN_ACTION_TO_NO_LUCK) {
                withContext(Dispatchers.Main) {
                    playGamesManager.unlockAchievement(Achievement.NoLuck)
                }
            }

            if (almostAchievement()) {
                withContext(Dispatchers.Main) {
                    playGamesManager.unlockAchievement(Achievement.Almost)
                }
            }

            gameState.field.count { it.mark.isFlag() }.also {
                if (it > 0) {
                    withContext(Dispatchers.Main) {
                        playGamesManager.incrementAchievement(Achievement.Flags, it)
                    }
                }
            }

            gameState.field.count { it.hasMine && it.mistake }.also {
                if (it > 0) {
                    withContext(Dispatchers.Main) {
                        playGamesManager.incrementAchievement(Achievement.Boom, it)
                    }
                }
            }
        }
    }

internal fun GameViewModel.onCreateUnsafeLevel() {
    postSideEffect(GameEvent.ShowNoGuessFailWarning)
}
