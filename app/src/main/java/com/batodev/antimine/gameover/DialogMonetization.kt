package com.batodev.antimine.gameover

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.batodev.antimine.R
import com.google.android.material.textview.MaterialTextView
import dev.lucasnlm.antimine.core.audio.GameAudioManagerImpl
import dev.lucasnlm.antimine.core.dpToPx
import dev.lucasnlm.antimine.core.models.Analytics
import dev.lucasnlm.antimine.core.openExternalLink
import dev.lucasnlm.antimine.preferences.PreferencesRepository
import dev.lucasnlm.external.AdsManager
import dev.lucasnlm.external.AnalyticsManager
import dev.lucasnlm.external.BillingManager
import kotlinx.coroutines.launch
import dev.lucasnlm.antimine.i18n.R as i18n

/**
 * Owns the ad/donation/music-composer banners and the rewarded-ad-then-continue
 * flow shared by every end-game dialog - split out of [CommonGameDialogFragment]
 * since these were the bulk of its function count.
 */
class DialogMonetization(
    private val fragment: CommonGameDialogFragment,
    private val adsManager: AdsManager,
    private val gameAudioManager: GameAudioManagerImpl,
    private val preferencesRepository: PreferencesRepository,
    private val analyticsManager: AnalyticsManager,
    private val billingManager: BillingManager,
) {
    fun showMusicDialog(adFrame: FrameLayout) {
        gameAudioManager.getComposerData().firstOrNull()?.let { composer ->
            adFrame.isVisible = true

            preferencesRepository.setLastMusicBanner(System.currentTimeMillis())

            val view = View.inflate(fragment.context, R.layout.music_link, null)
            view.run {
                findViewById<MaterialTextView>(R.id.music_by).text =
                    fragment.getString(i18n.string.music_by, composer.composer)

                setOnClickListener {
                    analyticsManager.sentEvent(
                        Analytics.OpenMusicLink(from = "End Game"),
                    )
                    preferencesRepository.setShowMusicBanner(false)
                    gameAudioManager.playMonetization()
                    openComposer(composer.composerLink)
                }
            }

            adFrame.addView(
                view,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER_HORIZONTAL,
                ),
            )
        }
    }

    fun showAdBannerDialog(adFrame: FrameLayout) {
        adFrame.apply {
            isVisible = true

            post {
                addView(
                    adsManager.createBannerAd(
                        fragment.requireContext(),
                        onError = {
                            showHexBanner(this)
                        },
                    ),
                    FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                    ),
                )
            }
        }
    }

    fun showDonationDialog(adFrame: FrameLayout) {
        adFrame.isVisible = true

        val view = View.inflate(fragment.context, R.layout.donation_request, null)
        view.apply {
            setOnClickListener {
                gameAudioManager.playMonetization()
                fragment.activity?.let {
                    fragment.lifecycleScope.launch {
                        billingManager.charge(it)
                        preferencesRepository.setRequestDonation(false)
                    }
                }
            }
        }

        adFrame.addView(
            view,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER_HORIZONTAL,
            ),
        )
    }

    fun showAdsAndContinue() {
        fragment.activity?.let { activity ->
            if (!activity.isFinishing) {
                adsManager.showRewardedAd(
                    activity,
                    skipIfFrequent = false,
                    onRewarded = {
                        fragment.continueGame()
                    },
                    onFail = {
                        adsManager.showInterstitialAd(
                            activity,
                            onDismiss = {
                                fragment.continueGame()
                            },
                            onError = {
                                Toast.makeText(fragment.context, i18n.string.no_network, Toast.LENGTH_SHORT).show()
                            },
                        )
                    },
                )
            }
        }
    }

    private fun openHexLink(context: Context) {
        context.openExternalLink(HEX_URI) {
            Toast.makeText(
                context.applicationContext,
                i18n.string.unknown_error,
                Toast.LENGTH_SHORT,
            ).show()
        }
    }

    private fun showHexBanner(adFrame: FrameLayout) {
        val context = adFrame.context

        val view = View.inflate(context, R.layout.hex_banner, null)
        view.setOnClickListener {
            openHexLink(context)
        }

        adFrame.apply {
            addView(
                view,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    context.dpToPx(CommonGameDialogFragment.HEX_BANNER_HEIGHT_DP),
                    Gravity.CENTER_HORIZONTAL,
                ),
            )
        }
    }

    private fun openComposer(composerLink: String) {
        val context = fragment.requireContext()
        context.openExternalLink(composerLink) {
            Toast.makeText(
                context.applicationContext,
                i18n.string.unknown_error,
                Toast.LENGTH_SHORT,
            ).show()
        }
    }

    companion object {
        private const val HEX_URI = "https://play.google.com/store/apps/details?id=dev.lucasnlm.hexo"
    }
}
