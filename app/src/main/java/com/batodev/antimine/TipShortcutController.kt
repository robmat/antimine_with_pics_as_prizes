package com.batodev.antimine

import android.animation.Animator
import android.animation.ValueAnimator
import android.os.Build
import androidx.annotation.StringRes
import androidx.appcompat.widget.TooltipCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.material.progressindicator.CircularProgressIndicator
import dev.lucasnlm.antimine.common.level.viewmodel.GameEvent
import dev.lucasnlm.antimine.common.level.viewmodel.getTips
import dev.lucasnlm.antimine.common.level.viewmodel.revealRandomMine
import dev.lucasnlm.antimine.core.models.Analytics
import dev.lucasnlm.antimine.preferences.models.ControlStyle
import dev.lucasnlm.antimine.ui.ext.showWarning
import kotlinx.coroutines.launch
import dev.lucasnlm.antimine.i18n.R as i18n

/**
 * Owns the top-right hint/shortcut icon in its "request a hint" state (with
 * ad-gated hints once the free ones run out) - split out of [GameActivity]
 * since refreshTipShortcutIcon() alone was too long, too complex, and nested
 * too deeply.
 */
class TipShortcutController(
    private val activity: GameActivity,
) {
    fun refresh() {
        val dt = System.currentTimeMillis() - activity.preferencesRepository.lastHelpUsed()
        val canUseHelpNow = dt > GameActivity.TIP_COOLDOWN_MS
        val canRequestHelpWithAds = activity.gameViewModel.getTips() == 0 && activity.adsManager.isAvailable()

        activity.binding.hintCounter.apply {
            isVisible = canUseHelpNow
            text =
                if (canRequestHelpWithAds) {
                    "+5"
                } else {
                    activity.gameViewModel.getTips().toL10nString()
                }
        }

        activity.binding.shortcutIcon.apply {
            TooltipCompat.setTooltipText(this, activity.getString(i18n.string.help))
            setImageResource(R.drawable.hint)
            setColorFilter(activity.binding.minesCount.currentTextColor)

            if (canUseHelpNow) {
                bindActiveHint(canRequestHelpWithAds)
            } else {
                bindCooldownHint(dt)
            }
        }
    }

    private fun bindActiveHint(canRequestHelpWithAds: Boolean) {
        activity.binding.shortcutIcon.apply {
            activity.binding.hintCooldown.apply {
                animate().alpha(0.0f).start()
                isVisible = false
                progress = 0
            }

            animate().alpha(1.0f).start()

            if (canRequestHelpWithAds) {
                setOnClickListener {
                    activity.lifecycleScope.launch {
                        requestHintWithAd()
                    }
                }
            } else {
                setOnClickListener {
                    activity.lifecycleScope.launch {
                        revealRandomMine()
                    }
                }
            }
        }
    }

    private fun requestHintWithAd() {
        activity.analyticsManager.sentEvent(Analytics.RequestMoreHints)
        val wasPlaying = activity.gameAudioManager.isPlayingMusic()
        activity.adsManager.showRewardedAd(
            activity = activity,
            skipIfFrequent = false,
            onStart = {
                if (wasPlaying) {
                    activity.gameAudioManager.pauseMusic()
                }
            },
            onRewarded = {
                if (wasPlaying) {
                    activity.gameAudioManager.resumeMusic()
                }
                activity.gameViewModel.revealRandomMine(false)
                activity.gameViewModel.sendEvent(GameEvent.GiveMoreTip)
            },
            onFail = {
                if (wasPlaying) {
                    activity.gameAudioManager.resumeMusic()
                }
                showGameWarning(i18n.string.fail_to_load_ad)
            },
        )
    }

    private fun bindCooldownHint(dt: Long) {
        activity.binding.shortcutIcon.apply {
            activity.binding.hintCooldown.apply {
                animate().alpha(1.0f).start()
                if (progress == 0) {
                    startCooldownAnimation()
                }
                isVisible = true
                max = GameActivity.TIP_COOLDOWN_MS.toInt()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    setProgress(dt.toInt(), true)
                } else {
                    progress = dt.toInt()
                }
            }

            animate().alpha(0.0f).start()

            setOnClickListener {
                showGameWarning(i18n.string.cant_do_it_now)
            }
        }
    }

    private fun CircularProgressIndicator.startCooldownAnimation() {
        ValueAnimator.ofInt(0, GameActivity.TIP_COOLDOWN_MS.toInt()).apply {
            duration = GameActivity.TIP_COOLDOWN_MS
            repeatCount = 0
            addUpdateListener {
                progress = it.animatedValue as Int
            }
            activity.revealBombFeedback = this

            addListener(cooldownAnimatorListener())
            start()
        }
    }

    private fun cooldownAnimatorListener(): Animator.AnimatorListener =
        object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                // Ignore
            }

            override fun onAnimationEnd(animation: Animator) {
                if (!activity.isGamePaused) {
                    activity.gameAudioManager.playRevealBombReloaded()
                }
            }

            override fun onAnimationCancel(animation: Animator) {
                // Ignore
            }

            override fun onAnimationRepeat(animation: Animator) {
                // Ignore
            }
        }

    fun revealRandomMine() {
        activity.analyticsManager.sentEvent(Analytics.UseHint)

        val hintAmount = activity.gameViewModel.getTips()
        if (hintAmount > 0) {
            val revealedId = activity.gameViewModel.revealRandomMine()
            if (revealedId == null) {
                showGameWarning(i18n.string.cant_do_it_now)
            } else {
                showGameWarning(i18n.string.mine_revealed)
            }
        } else {
            showGameWarning(i18n.string.help_win_a_game)
        }
    }

    fun showGameWarning(
        @StringRes text: Int,
    ) {
        val isSwitchAndOpen = activity.preferencesRepository.controlStyle() == ControlStyle.SwitchMarkOpen
        activity.warning?.dismiss()
        activity.warning = activity.showWarning(text, isSwitchAndOpen)
    }
}
