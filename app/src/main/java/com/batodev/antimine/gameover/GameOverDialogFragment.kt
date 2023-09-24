package com.batodev.antimine.gameover

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.text.format.DateUtils
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.batodev.antimine.R
import dev.lucasnlm.antimine.common.level.viewmodel.GameEvent
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModel
import dev.lucasnlm.antimine.core.models.Analytics
import dev.lucasnlm.antimine.core.parcelable
import com.batodev.antimine.databinding.GameOverDialogBinding
import com.batodev.antimine.gameover.model.CommonDialogState
import com.batodev.antimine.gameover.model.GameResult
import com.batodev.antimine.gameover.viewmodel.EndGameDialogEvent
import com.batodev.antimine.gameover.viewmodel.EndGameDialogViewModel
import dev.lucasnlm.antimine.tutorial.TutorialActivity
import dev.lucasnlm.external.AnalyticsManager
import dev.lucasnlm.external.FeatureFlagManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class GameOverDialogFragment : CommonGameDialogFragment() {
    private val analyticsManager: AnalyticsManager by inject()
    private val dialogViewModel by viewModel<EndGameDialogViewModel>()
    private val gameViewModel by sharedViewModel<GameViewModel>()
    private val featureFlagManager: FeatureFlagManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments
            ?.parcelable<CommonDialogState>(DIALOG_STATE)
            ?.run {
                dialogViewModel.sendEvent(
                    EndGameDialogEvent.BuildCustomEndGame(
                        gameResult =
                            if (totalMines > 0) {
                                gameResult
                            } else {
                                GameResult.GameOver
                            },
                        showContinueButton = showContinueButton,
                        time = time,
                        rightMines = rightMines,
                        totalMines = totalMines,
                        received = received,
                        turn = turn,
                    ),
                )
            }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = requireContext()
        return MaterialAlertDialogBuilder(context).apply {
            val layoutInflater = LayoutInflater.from(context)
            val binding = GameOverDialogBinding.inflate(layoutInflater, null, false)

            binding.run {
                lifecycleScope.launch {
                    dialogViewModel.observeState().collect { state ->
                        title.text = state.title
                        subtitle.text = state.message

                        titleEmoji.apply {
                            setImageResource(state.titleEmoji)
                            setOnClickListener {
                                analyticsManager.sentEvent(Analytics.ClickEmoji)
                                dialogViewModel.sendEvent(
                                    EndGameDialogEvent.ChangeEmoji(state.gameResult, state.titleEmoji),
                                )
                            }
                        }

                        newGame.setOnClickListener {
                            lifecycleScope.launch {
                                gameViewModel.startNewGame(context)
                            }
                            dismissAllowingStateLoss()
                        }

                        continueGame.setOnClickListener {
                            analyticsManager.sentEvent(Analytics.ContinueGame)
                            if (featureFlagManager.isAdsOnContinueEnabled && !isPremiumEnabled) {
                                showAdsAndContinue()
                            } else {
                                gameViewModel.sendEvent(GameEvent.ContinueGame)
                                dismissAllowingStateLoss()
                            }
                        }

                        settings.setOnClickListener {
                            analyticsManager.sentEvent(Analytics.OpenSettings)
                            showSettings()
                        }

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

                        if (featureFlagManager.isFoss && canRequestDonation) {
                            showDonationDialog(adFrame)
                        } else if (!isPremiumEnabled && featureFlagManager.isBannerAdEnabled) {
                            showAdBannerDialog(adFrame)
                        }

                        if (!state.showTutorial &&
                            state.showContinueButton &&
                            featureFlagManager.isContinueGameEnabled
                        ) {
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
                                        countdown.text = (CONTINUE_COUNTDOWN_SECONDS - it).toString()
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

                        if (state.showTutorial) {
                            tutorial.isVisible = true
                            tutorial.setOnClickListener {
                                val intent = Intent(context, TutorialActivity::class.java)
                                context.startActivity(intent)
                            }
                        }
                    }
                }
            }

            setOnKeyListener { _, _, keyEvent ->
                if (keyEvent.keyCode == KeyEvent.KEYCODE_BACK) {
                    activity?.let {
                        if (!it.isFinishing) {
                            gameViewModel.viewModelScope.launch {
                                gameViewModel.revealMines()
                            }
                        }
                    }
                    dismissAllowingStateLoss()
                    true
                } else {
                    false
                }
            }

            setView(binding.root)
        }.create().apply {
            setCanceledOnTouchOutside(false)

            window?.apply {
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                    attributes?.blurBehindRadius = BACKGROUND_BLUR_RADIUS
                }
            }
        }
    }

    override fun continueGame() {
        gameViewModel.sendEvent(GameEvent.ContinueGame)
        dismissAllowingStateLoss()
    }

    override fun canShowMusicBanner(): Boolean {
        return dialogViewModel.singleState().showMusicDialog
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
