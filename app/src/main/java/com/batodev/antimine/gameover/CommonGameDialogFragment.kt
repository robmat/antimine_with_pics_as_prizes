package com.batodev.antimine.gameover

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.viewModelScope
import com.batodev.antimine.gameover.model.CommonDialogState
import com.batodev.antimine.gameover.model.GameResult
import com.batodev.antimine.gameover.viewmodel.EndGameDialogEvent
import com.batodev.antimine.gameover.viewmodel.EndGameDialogState
import com.batodev.antimine.gameover.viewmodel.EndGameDialogViewModel
import com.batodev.antimine.preferences.PreferencesActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModel
import dev.lucasnlm.antimine.common.level.viewmodel.revealMines
import dev.lucasnlm.antimine.core.audio.GameAudioManagerImpl
import dev.lucasnlm.antimine.core.models.Analytics
import dev.lucasnlm.antimine.core.parcelable
import dev.lucasnlm.antimine.preferences.PreferencesRepository
import dev.lucasnlm.external.AdsManager
import dev.lucasnlm.external.AnalyticsManager
import dev.lucasnlm.external.BillingManager
import dev.lucasnlm.external.FeatureFlagManager
import dev.lucasnlm.external.InstantAppManager
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

abstract class CommonGameDialogFragment : AppCompatDialogFragment() {
    private val adsManager: AdsManager by inject()
    private val gameAudioManager: GameAudioManagerImpl by inject()
    private val instantAppManager: InstantAppManager by inject()
    protected val analyticsManager: AnalyticsManager by inject()

    protected val preferencesRepository: PreferencesRepository by inject()
    protected val billingManager: BillingManager by inject()
    protected val featureFlagManager: FeatureFlagManager by inject()

    protected val dialogViewModel by viewModel<EndGameDialogViewModel>()
    protected val gameViewModel by activityViewModel<GameViewModel>()

    protected val isPremiumEnabled: Boolean by lazy {
        preferencesRepository.isPremiumEnabled()
    }

    protected val canRequestDonation: Boolean by lazy {
        preferencesRepository.requestDonation()
    }

    protected val isInstantMode: Boolean by lazy {
        context?.let { instantAppManager.isEnabled(it) } == true
    }

    protected val monetization: DialogMonetization by lazy {
        DialogMonetization(
            fragment = this,
            adsManager = adsManager,
            gameAudioManager = gameAudioManager,
            preferencesRepository = preferencesRepository,
            analyticsManager = analyticsManager,
            billingManager = billingManager,
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!preferencesRepository.isPremiumEnabled()) {
            billingManager.start()
        }

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

    fun showAllowingStateLoss(
        manager: FragmentManager,
        tag: String?,
    ) {
        val fragmentTransaction = manager.beginTransaction()
        fragmentTransaction.add(this, tag)
        fragmentTransaction.commitAllowingStateLoss()
    }

    abstract fun continueGame()

    abstract fun canShowMusicBanner(): Boolean

    /** Updates the title/subtitle/emoji header shared by every end-game dialog. */
    protected fun bindDialogHeader(
        title: MaterialTextView,
        subtitle: MaterialTextView,
        titleEmoji: ImageView,
        state: EndGameDialogState,
    ) {
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
    }

    /**
     * Wires the "back reveals mines" key listener and the blurred-window behavior shared
     * by every end-game dialog, then creates and returns the [Dialog].
     */
    protected fun MaterialAlertDialogBuilder.finalizeGameDialog(rootView: View): Dialog {
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

        setView(rootView)

        return create().apply {
            setCanceledOnTouchOutside(false)

            window?.apply {
                setBackgroundDrawable(Color.TRANSPARENT.toDrawable())

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
                    attributes?.blurBehindRadius = BACKGROUND_BLUR_RADIUS
                }
            }
        }
    }

    protected fun showSettings() {
        startActivity(Intent(requireContext(), PreferencesActivity::class.java))
    }

    companion object {
        const val DIALOG_STATE = "dialog_state"
        const val HEX_BANNER_HEIGHT_DP = 75
        const val BACKGROUND_BLUR_RADIUS = 8
    }
}
