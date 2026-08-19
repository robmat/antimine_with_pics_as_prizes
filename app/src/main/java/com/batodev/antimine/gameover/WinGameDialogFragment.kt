package com.batodev.antimine.gameover

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.batodev.antimine.GalleryActivity
import com.batodev.antimine.R
import com.batodev.antimine.databinding.WinDialogBinding
import com.batodev.antimine.gameover.model.CommonDialogState
import com.batodev.antimine.gameover.model.GameResult
import com.batodev.antimine.gameover.viewmodel.EndGameDialogState
import com.batodev.antimine.stats.StatsActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev.lucasnlm.antimine.common.level.viewmodel.startNewGame
import dev.lucasnlm.antimine.core.models.Analytics
import kotlinx.coroutines.launch
import dev.lucasnlm.antimine.i18n.R as i18n

class WinGameDialogFragment : CommonGameDialogFragment() {
    override fun continueGame() {
        val context = requireContext()
        activity?.let { _ ->
            lifecycleScope.launch {
                gameViewModel.startNewGame(context)
            }
            dismissAllowingStateLoss()
        }
    }

    override fun canShowMusicBanner(): Boolean {
        return dialogViewModel.singleState().showMusicDialog
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = requireContext()
        val binding = WinDialogBinding.inflate(LayoutInflater.from(context), null, false)
        val builder = MaterialAlertDialogBuilder(context)

        binding.run {
            lifecycleScope.launch {
                dialogViewModel.observeState().collect { state ->
                    bindDialogHeader(title, subtitle, titleEmoji, state)
                    bindStatsButton(context, state)
                    bindNewGameButton()
                    bindMonetizationBanner(state)
                    bindSettingsButton()
                    bindGalleryButton(context)
                    bindReceivedMessage(state)
                }
            }
        }

        return builder.finalizeGameDialog(binding.root)
    }

    private fun WinDialogBinding.bindStatsButton(context: Context, state: EndGameDialogState) {
        stats.setOnClickListener {
            analyticsManager.sentEvent(Analytics.OpenStats)
            Intent(context, StatsActivity::class.java).apply {
                startActivity(this)
            }
        }

        if (state.gameResult == GameResult.Victory || state.gameResult == GameResult.Completed) {
            close.setOnClickListener {
                dismissAllowingStateLoss()
            }
            stats.isVisible = true
        }
    }

    private fun WinDialogBinding.bindNewGameButton() {
        newGame.setOnClickListener {
            if (featureFlagManager.isAdsOnContinueEnabled && !isPremiumEnabled) {
                monetization.showAdsAndContinue()
            } else {
                continueGame()
            }
        }

        if (!isPremiumEnabled && featureFlagManager.isAdsOnContinueEnabled) {
            newGame.compoundDrawablePadding = 0
            newGame.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.watch_ads_icon,
                0,
                0,
                0,
            )
        }
    }

    private fun WinDialogBinding.bindMonetizationBanner(state: EndGameDialogState) {
        if (featureFlagManager.isFoss && canRequestDonation) {
            monetization.showDonationDialog(adFrame)
        } else if (!isPremiumEnabled && featureFlagManager.isBannerAdEnabled) {
            monetization.showAdBannerDialog(adFrame)
        } else if (state.showMusicDialog) {
            monetization.showMusicDialog(adFrame)
        }
    }

    private fun WinDialogBinding.bindSettingsButton() {
        settings.setOnClickListener {
            analyticsManager.sentEvent(Analytics.OpenSettings)
            showSettings()
        }
    }

    private fun WinDialogBinding.bindGalleryButton(context: Context) {
        if (!isPremiumEnabled && !isInstantMode && featureFlagManager.isGameOverAdEnabled) {
            activity?.let {
                val label = context.getString(i18n.string.pictures_gallery)
                picturesGalllery.apply {
                    isVisible = true
                    text = label
                    setOnClickListener {
                        startActivity(Intent(context, GalleryActivity::class.java))
                    }
                }
            }
        }
    }

    private fun WinDialogBinding.bindReceivedMessage(state: EndGameDialogState) {
        val wonHelpReward = state.received > 0 && state.gameResult == GameResult.Victory
        receivedMessage.apply {
            if (wonHelpReward && preferencesRepository.useHelp() && isPremiumEnabled) {
                isVisible = true
                text = getString(i18n.string.you_have_received, state.received)
            } else {
                isVisible = false
            }
        }
    }

    companion object {
        fun newInstance(commonDialogState: CommonDialogState) =
            WinGameDialogFragment().apply {
                arguments =
                    Bundle().apply {
                        putParcelable(DIALOG_STATE, commonDialogState)
                    }
            }

        val TAG = WinGameDialogFragment::class.simpleName!!
    }
}
