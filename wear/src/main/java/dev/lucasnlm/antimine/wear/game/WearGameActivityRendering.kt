package dev.lucasnlm.antimine.wear.game

import android.content.Intent
import android.text.format.DateUtils
import android.view.WindowManager
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import dev.lucasnlm.antimine.common.level.viewmodel.GameEvent
import dev.lucasnlm.antimine.common.level.viewmodel.GameState
import dev.lucasnlm.antimine.common.level.viewmodel.startNewGame
import dev.lucasnlm.antimine.wear.message.GameOverActivity
import dev.lucasnlm.antimine.wear.message.VictoryActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import dev.lucasnlm.antimine.i18n.R as i18n

/**
 * [WearGameActivity]'s game-state rendering logic, split out of the class
 * body - see its class doc.
 */
internal fun WearGameActivity.applyGameState(state: GameState) {
    updateTapToBeginLabel(state)

    if (state.isCreatingGame) {
        lifecycleScope.launch {
            // Show loading indicator only when it takes more than:
            delay(WearGameActivity.LOADING_INDICATOR_DELAY_MS)
            if (gameViewModel.singleState().isCreatingGame) {
                binding.loadingGame.show()
            }
        }
    } else if (binding.loadingGame.isVisible) {
        binding.loadingGame.hide()
    }

    updateTimerLabel(state)

    if (state.isGameCompleted) {
        binding.newGame.setOnClickListener {
            lifecycleScope.launch {
                gameViewModel.startNewGame(this@applyGameState)
            }
        }
        binding.newGame.isVisible = true
    } else {
        binding.newGame.isVisible = false
    }

    keepScreenOn(state.isActive)
    refreshSwitchButtons()
}

internal fun WearGameActivity.updateTapToBeginLabel(state: GameState) {
    val hasNoProgress = state.saveId == 0L || state.isLoadingMap || state.isCreatingGame
    val shouldShowTapToBegin = state.turn == 0 && hasNoProgress
    if (shouldShowTapToBegin) {
        binding.tapToBegin.apply {
            text =
                when {
                    state.isCreatingGame -> getString(i18n.string.creating_valid_game)
                    state.isLoadingMap -> getString(i18n.string.loading)
                    else -> getString(i18n.string.tap_to_begin)
                }
            isVisible = true
        }
    } else {
        binding.tapToBegin.isVisible = false
    }
}

internal fun WearGameActivity.updateTimerLabel(state: GameState) {
    if (state.duration % WearGameActivity.TIMER_BLINK_PERIOD_SECONDS > WearGameActivity.TIMER_BLINK_VISIBLE_SECONDS) {
        binding.timer.apply {
            isVisible = preferencesRepository.showTimer()
            alpha = WearGameActivity.TIMER_ALPHA
            text = DateUtils.formatElapsedTime(state.duration)
        }
    } else if (state.duration > 0) {
        binding.timer.apply {
            text = getString(i18n.string.mines_remaining, state.mineCount)
        }
    } else {
        binding.timer.isVisible = false
    }
}

internal fun WearGameActivity.handleGameEvent(event: GameEvent) {
    when (event) {
        is GameEvent.ShowNoGuessFailWarning -> {}

        is GameEvent.ShowNewGameDialog -> {}

        is GameEvent.VictoryDialog -> {
            startDialogActivity(VictoryActivity::class.java)
        }

        is GameEvent.GameOverDialog -> {
            startDialogActivity(GameOverActivity::class.java)
        }

        is GameEvent.GameCompleteDialog -> {
            startDialogActivity(VictoryActivity::class.java)
        }

        else -> {
            // Empty
        }
    }
}

internal fun WearGameActivity.startDialogActivity(activityClass: Class<*>) {
    val intent =
        Intent(applicationContext, activityClass).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
    startActivity(intent)
}

internal fun WearGameActivity.keepScreenOn(enabled: Boolean) {
    if (enabled) {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    } else {
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}
