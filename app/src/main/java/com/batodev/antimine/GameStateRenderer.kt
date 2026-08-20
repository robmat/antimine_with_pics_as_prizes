package com.batodev.antimine

import android.animation.ValueAnimator
import android.content.Intent
import android.content.res.ColorStateList
import android.text.format.DateUtils
import android.view.animation.AnimationUtils
import androidx.appcompat.widget.TooltipCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev.lucasnlm.antimine.common.level.viewmodel.GameState
import dev.lucasnlm.antimine.common.level.viewmodel.getControlDescription
import dev.lucasnlm.antimine.common.level.viewmodel.startNewGame
import dev.lucasnlm.antimine.control.ControlActivity
import dev.lucasnlm.antimine.core.models.Analytics
import dev.lucasnlm.antimine.ui.ext.toAndroidColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import dev.lucasnlm.antimine.i18n.R as i18n

/**
 * Renders every UI update driven by [dev.lucasnlm.antimine.common.level.viewmodel.GameViewModel.observeState] -
 * split out of [GameActivity], whose combined bindViewModel() was 223 lines
 * at cyclomatic complexity 41.
 */
class GameStateRenderer(
    private val activity: GameActivity,
) {
    suspend fun render(state: GameState) =
        with(activity) {
            if (state.isNewGame) {
                withContext(Dispatchers.Main) { stopKonfettiView() }
            }
            bindTapToBeginText(state)
            bindMusicAndLoading(state)
            bindControlsToast(state)
            bindTimerAndMines(state)
            binding.hintCounter.text = state.hints.toL10nString()

            if (!state.isGameCompleted && state.isActive && state.useHelp) {
                tipShortcut.refresh()
            } else {
                refreshRetryShortcut(state.hasMines)
            }

            bindPrizeImage()
            keepScreenOn(state.isActive)
        }

    private fun GameActivity.bindTapToBeginText(state: GameState) {
        if (state.isNewGame) {
            warning?.dismiss()
            warning = null

            val color = currentTheme.palette.covered.toAndroidColor(GameActivity.COVERED_TINT_ALPHA)
            val tint = ColorStateList.valueOf(color)

            binding.tapToBegin.apply {
                text =
                    when {
                        state.isCreatingGame -> {
                            getString(i18n.string.creating_valid_game)
                        }

                        state.isLoadingMap -> {
                            getString(i18n.string.loading)
                        }

                        else -> {
                            getString(i18n.string.tap_to_begin)
                        }
                    }
                isVisible = true
                backgroundTintList = tint
            }
        } else {
            binding.tapToBegin.isVisible = false
        }
    }

    private fun GameActivity.bindMusicAndLoading(state: GameState) {
        if (state.isGameStarted && state.isActive) {
            gameAudioManager.playMusic()
        }

        if (state.isCreatingGame) {
            lifecycleScope.launch {
                // Show loading indicator only when it takes more than:
                delay(GameActivity.LOADING_INDICATOR_MS)
                if (gameViewModel.singleState().isCreatingGame) {
                    binding.loadingGame.show()
                }
            }
        } else if (binding.loadingGame.isVisible) {
            binding.loadingGame.hide()
        }
    }

    private fun GameActivity.bindControlsToast(state: GameState) {
        if (state.shouldShowControls) {
            val color = currentTheme.palette.covered.toAndroidColor(GameActivity.COVERED_TINT_ALPHA)
            val tint = ColorStateList.valueOf(color)
            val controlText = gameViewModel.getControlDescription(applicationContext)

            if (!controlText.isNullOrBlank()) {
                binding.controlsToast.apply {
                    isVisible = true
                    backgroundTintList = tint
                    text = controlText

                    setOnClickListener {
                        val intent = Intent(activity, ControlActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
            } else {
                binding.controlsToast.isVisible = false
            }
        } else {
            binding.controlsToast.isVisible = false
        }
    }

    private fun GameActivity.bindTimerAndMines(state: GameState) {
        binding.timer.apply {
            isVisible = preferencesRepository.showTimer() && state.duration != 0L
            text = DateUtils.formatElapsedTime(state.duration)
        }

        bindMinesCount(state.mineCount)
    }

    private fun GameActivity.bindMinesCount(currentMineCount: Int?) {
        binding.minesCount.apply {
            if (currentMineCount == null) {
                isVisible = false
                return@apply
            }

            val oldValue = text.toString().toIntOrNull()
            if (oldValue == null) {
                text = currentMineCount.toL10nString()
                isVisible = true
                return@apply
            }

            if (currentMineCount < 0 && oldValue > currentMineCount) {
                startAnimation(AnimationUtils.loadAnimation(context, R.anim.fast_shake))
            }
            startCountAnimation(oldValue, currentMineCount) { animateIt ->
                text = animateIt.toL10nString()
            }
            isVisible = true
        }
    }

    private fun GameActivity.refreshRetryShortcut(enabled: Boolean) {
        fun startNewGameWithAds() {
            if (!preferencesRepository.isPremiumEnabled() && featureFlagManager.isAdsOnNewGameEnabled) {
                if (featureFlagManager.useInterstitialAd) {
                    adsManager.showInterstitialAd(
                        activity = activity,
                        onDismiss = {
                            lifecycleScope.launch {
                                gameViewModel.startNewGame(activity)
                            }
                        },
                    )
                } else {
                    adsManager.showRewardedAd(
                        activity = activity,
                        skipIfFrequent = true,
                        onRewarded = {
                            lifecycleScope.launch {
                                gameViewModel.startNewGame(activity)
                            }
                        },
                        onFail = {
                            lifecycleScope.launch {
                                gameViewModel.startNewGame(activity)
                            }
                        },
                    )
                }
            } else {
                lifecycleScope.launch {
                    gameViewModel.startNewGame(activity)
                }
            }
        }

        fun newGameConfirmation(action: () -> Unit) {
            MaterialAlertDialogBuilder(activity).apply {
                setTitle(i18n.string.new_game)
                setMessage(i18n.string.retry_sure)
                setPositiveButton(i18n.string.resume) { _, _ -> action() }
                setNegativeButton(i18n.string.cancel, null)
                show()
            }
        }

        binding.shortcutIcon.apply {
            TooltipCompat.setTooltipText(this, getString(i18n.string.new_game))
            setImageResource(R.drawable.retry)
            setColorFilter(binding.minesCount.currentTextColor)
            setOnClickListener {
                lifecycleScope.launch {
                    gameAudioManager.playClickSound()
                    val confirmResign = gameViewModel.singleState().isActive
                    analyticsManager.sentEvent(Analytics.TapGameReset(confirmResign))

                    if (confirmResign) {
                        newGameConfirmation {
                            startNewGameWithAds()
                        }
                    } else {
                        startNewGameWithAds()
                    }
                }
            }
        }

        binding.hintCounter.isVisible = false

        binding.shortcutIcon.apply {
            isClickable = enabled
            val alphaValue =
                if (enabled) {
                    GameActivity.ENABLED_SHORTCUT_ALPHA
                } else {
                    GameActivity.DISABLED_SHORTCUT_ALPHA
                }
            animate().alpha(alphaValue).start()
        }
    }

    private fun GameActivity.startCountAnimation(
        from: Int,
        to: Int,
        updateMineCount: (Int) -> Unit,
    ) {
        ValueAnimator
            .ofInt(from, to)
            .apply {
                duration = GameActivity.MINE_COUNTER_ANIM_COUNTER_MS
                addUpdateListener { animation ->
                    updateMineCount(animation.animatedValue as Int)
                }
            }.start()
    }

    private fun GameActivity.stopKonfettiView() {
        binding.konfettiView?.stopGracefully()
    }
}
