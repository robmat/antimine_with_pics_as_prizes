package com.batodev.antimine.gameover

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.format.DateUtils
import android.view.LayoutInflater
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.batodev.antimine.R
import com.batodev.antimine.databinding.GameOverDialogBinding
import com.batodev.antimine.gameover.model.CommonDialogState
import com.batodev.antimine.gameover.viewmodel.EndGameDialogState
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev.lucasnlm.antimine.common.level.viewmodel.GameEvent
import dev.lucasnlm.antimine.common.level.viewmodel.revealMines
import dev.lucasnlm.antimine.common.level.viewmodel.startNewGame
import dev.lucasnlm.antimine.core.models.Analytics
import dev.lucasnlm.antimine.tutorial.TutorialActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

class GameOverDialogFragment : CommonGameDialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = requireContext()
        val binding = GameOverDialogBinding.inflate(LayoutInflater.from(context), null, false)
        val builder = MaterialAlertDialogBuilder(context)

        binding.run {
            lifecycleScope.launch {
                dialogViewModel.observeState().collect { state ->
                    bindDialogHeader(title, subtitle, titleEmoji, state)
                    bindNewGameButton(context)
                    bindContinueButton()
                    bindSettingsButton()
                    bindCloseButton()
                    bindMonetizationBanner()
                    bindContinueVisibilityAndCountdown(state)
                    bindTutorialButton(context, state)
                }
            }
        }

        return builder.finalizeGameDialog(binding.root)
    }

    override fun continueGame() {
        gameViewModel.sendEvent(GameEvent.ContinueGame)
        dismissAllowingStateLoss()
    }

    override fun canShowMusicBanner(): Boolean = dialogViewModel.singleState().showMusicDialog

    private fun GameOverDialogBinding.bindNewGameButton(context: Context) {
        newGame.setOnClickListener {
            lifecycleScope.launch {
                gameViewModel.startNewGame(context)
            }
            dismissAllowingStateLoss()
        }
    }

    private fun GameOverDialogBinding.bindContinueButton() {
        continueGame.setOnClickListener {
            analyticsManager.sentEvent(Analytics.ContinueGame)
            if (featureFlagManager.isAdsOnContinueEnabled && !isPremiumEnabled) {
                monetization.showAdsAndContinue()
            } else {
                gameViewModel.sendEvent(GameEvent.ContinueGame)
                dismissAllowingStateLoss()
            }
        }
    }

    private fun GameOverDialogBinding.bindSettingsButton() {
        settings.setOnClickListener {
            analyticsManager.sentEvent(Analytics.OpenSettings)
            showSettings()
        }
    }

    private fun GameOverDialogBinding.bindCloseButton() {
        close.setOnClickListener {
            analyticsManager.sentEvent(Analytics.CloseEndGameScreen)
            activity?.let {
                if (!it.isFinishing) {
                    lifecycleScope.launch {
                        gameViewModel.revealMines()
                    }
                }
            }
            dismissAllowingStateLoss()
        }
    }

    private fun GameOverDialogBinding.bindMonetizationBanner() {
        if (featureFlagManager.isFoss && canRequestDonation) {
            monetization.showDonationDialog(adFrame)
        } else if (!isPremiumEnabled && featureFlagManager.isBannerAdEnabled) {
            monetization.showAdBannerDialog(adFrame)
        }
    }

    private fun GameOverDialogBinding.bindContinueVisibilityAndCountdown(state: EndGameDialogState) {
        if (!state.showTutorial && state.showContinueButton && featureFlagManager.isContinueGameEnabled) {
            continueGame.isVisible = true
            if (!isPremiumEnabled && featureFlagManager.isAdsOnContinueEnabled) {
                continueGame.compoundDrawablePadding = 0
                continueGame.setCompoundDrawablesWithIntrinsicBounds(
                    R.drawable.watch_ads_icon,
                    0,
                    0,
                    0,
                )
            }

            if (!isPremiumEnabled && featureFlagManager.showCountdownToContinue) {
                countdown.isVisible = true
                lifecycleScope.launch {
                    repeat(CONTINUE_COUNTDOWN_SECONDS) {
                        countdown.text = String.format(Locale.US, "%d", CONTINUE_COUNTDOWN_SECONDS - it)
                        delay(DateUtils.SECOND_IN_MILLIS)
                    }
                    countdown.isVisible = false
                    continueGame.isVisible = false
                }
            }
        } else {
            continueGame.isVisible = false
            countdown.isVisible = false
        }
    }

    private fun GameOverDialogBinding.bindTutorialButton(
        context: Context,
        state: EndGameDialogState,
    ) {
        if (state.showTutorial) {
            tutorial.isVisible = true
            tutorial.setOnClickListener {
                val intent = Intent(context, TutorialActivity::class.java)
                context.startActivity(intent)
            }
        }
    }

    companion object {
        fun newInstance(state: CommonDialogState) =
            GameOverDialogFragment().apply {
                arguments =
                    Bundle().apply {
                        putParcelable(DIALOG_STATE, state)
                    }
            }

        private const val CONTINUE_COUNTDOWN_SECONDS = 10
    }
}
