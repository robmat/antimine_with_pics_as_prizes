package com.batodev.antimine

import androidx.lifecycle.lifecycleScope
import com.batodev.antimine.gameover.GameOverDialogFragment
import com.batodev.antimine.gameover.WinGameDialogFragment
import com.batodev.antimine.gameover.model.CommonDialogState
import com.batodev.antimine.gameover.model.GameResult
import com.google.android.material.snackbar.Snackbar
import dev.lucasnlm.antimine.common.level.viewmodel.GameEvent
import dev.lucasnlm.antimine.common.level.viewmodel.hasUnknownMines
import dev.lucasnlm.antimine.core.dpToPx
import dev.lucasnlm.antimine.preferences.models.ControlStyle
import dev.lucasnlm.antimine.ui.ext.showWarning
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit
import dev.lucasnlm.antimine.i18n.R as i18n

/**
 * Handles every one-off event from
 * [dev.lucasnlm.antimine.common.level.viewmodel.GameViewModel.observeSideEffects] -
 * split out of [GameActivity], whose combined bindViewModel() was 223 lines
 * at cyclomatic complexity 41.
 */
class GameEventDialogs(private val activity: GameActivity) {
    suspend fun handle(event: GameEvent) =
        with(activity) {
            when (event) {
                is GameEvent.ShowNoGuessFailWarning -> showNoGuessFailWarning()
                is GameEvent.ShowNewGameDialog -> showNewGameDialog()
                is GameEvent.VictoryDialog -> showVictoryDialog(event)
                is GameEvent.GameOverDialog -> showGameOverDialog(event)
                is GameEvent.GameCompleteDialog -> showGameCompleteDialog(event)
                else -> {
                    // Empty
                }
            }
        }

    private fun GameActivity.showNoGuessFailWarning() {
        warning =
            showWarning(i18n.string.no_guess_fail_warning).apply {
                setAction(i18n.string.ok) {
                    warning?.dismiss()
                }
                show()
            }
    }

    private fun GameActivity.showNewGameDialog() {
        lifecycleScope.launch {
            GameOverDialogFragment.newInstance(
                CommonDialogState(
                    gameResult = GameResult.Completed,
                    showContinueButton = gameViewModel.hasUnknownMines(),
                    rightMines = 0,
                    totalMines = 0,
                    time = gameViewModel.singleState().duration,
                    received = 0,
                    turn = -1,
                ),
            ).run {
                showAllowingStateLoss(supportFragmentManager, WinGameDialogFragment.TAG)
            }
        }
    }

    private suspend fun GameActivity.showVictoryDialog(event: GameEvent.VictoryDialog) {
        if (preferencesRepository.showWindowsWhenFinishGame()) {
            withContext(Dispatchers.Main) { showKonfettiView() }

            lifecycleScope.launch {
                delay(event.delayToShow)

                gameAudioManager.pauseMusic()

                WinGameDialogFragment.newInstance(
                    CommonDialogState(
                        gameResult = GameResult.Victory,
                        showContinueButton = false,
                        rightMines = event.rightMines,
                        totalMines = event.totalMines,
                        time = event.timestamp,
                        received = event.receivedTips,
                        turn = -1,
                    ),
                ).run {
                    showAllowingStateLoss(supportFragmentManager, WinGameDialogFragment.TAG)

                    dialog?.setOnDismissListener {
                        if (!this@GameEventDialogs.activity.isFinishing) {
                            reviewWrapper.startInAppReview(this@GameEventDialogs.activity)
                        }
                    }
                }
            }
        } else {
            withContext(Dispatchers.Main) { showKonfettiView() }
            gameAudioManager.pauseMusic()
            showEndGameToast(GameResult.Victory)
        }
    }

    private fun GameActivity.showGameOverDialog(event: GameEvent.GameOverDialog) {
        if (preferencesRepository.showWindowsWhenFinishGame()) {
            lifecycleScope.launch {
                delay(event.delayToShow)
                GameOverDialogFragment.newInstance(
                    CommonDialogState(
                        gameResult = GameResult.GameOver,
                        showContinueButton = gameViewModel.hasUnknownMines(),
                        rightMines = event.rightMines,
                        totalMines = event.totalMines,
                        time = event.timestamp,
                        received = event.receivedTips,
                        turn = event.turn,
                    ),
                ).run {
                    showAllowingStateLoss(supportFragmentManager, WinGameDialogFragment.TAG)
                }
            }
        } else {
            showEndGameToast(GameResult.GameOver)
        }
    }

    private fun GameActivity.showGameCompleteDialog(event: GameEvent.GameCompleteDialog) {
        if (preferencesRepository.showWindowsWhenFinishGame()) {
            lifecycleScope.launch {
                delay(event.delayToShow)
                GameOverDialogFragment.newInstance(
                    CommonDialogState(
                        gameResult = GameResult.Completed,
                        showContinueButton = false,
                        rightMines = event.rightMines,
                        totalMines = event.totalMines,
                        time = event.timestamp,
                        received = event.receivedTips,
                        turn = event.turn,
                    ),
                ).run {
                    showAllowingStateLoss(supportFragmentManager, WinGameDialogFragment.TAG)

                    dialog?.setOnDismissListener {
                        if (!this@GameEventDialogs.activity.isFinishing) {
                            reviewWrapper.startInAppReview(this@GameEventDialogs.activity)
                        }
                    }
                }
            }
        } else {
            showEndGameToast(GameResult.Completed)
        }
    }

    private fun GameActivity.showEndGameToast(gameResult: GameResult) {
        warning?.dismiss()

        val message =
            when (gameResult) {
                GameResult.GameOver -> i18n.string.you_lost
                GameResult.Victory -> i18n.string.you_won
                GameResult.Completed -> i18n.string.you_finished
            }

        warning =
            Snackbar.make(
                binding.root,
                message,
                Snackbar.LENGTH_LONG,
            ).apply {
                if (preferencesRepository.controlStyle() == ControlStyle.SwitchMarkOpen) {
                    view.translationY = -dpToPx(GameActivity.TOAST_OFFSET_Y_DP).toFloat()
                }
                show()
            }
    }

    private fun GameActivity.showKonfettiView() {
        binding.konfettiView?.apply {
            start(
                Party(
                    speed = 0f,
                    maxSpeed = 30f,
                    damping = 0.9f,
                    spread = 360,
                    colors = GameActivity.CONFETTI_COLORS,
                    emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(GameActivity.MAX_CONFETTI_COUNT),
                    position = GameActivity.CONFETTI_POSITION,
                ),
            )
        }
    }
}
