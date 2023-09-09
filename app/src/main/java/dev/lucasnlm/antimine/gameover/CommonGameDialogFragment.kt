package dev.lucasnlm.antimine.gameover

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textview.MaterialTextView
import dev.lucasnlm.antimine.R
import dev.lucasnlm.antimine.core.audio.GameAudioManagerImpl
import dev.lucasnlm.antimine.core.dpToPx
import dev.lucasnlm.antimine.core.models.Analytics
import dev.lucasnlm.antimine.preferences.PreferencesActivity
import dev.lucasnlm.antimine.preferences.PreferencesRepository
import dev.lucasnlm.external.AdsManager
import dev.lucasnlm.external.AnalyticsManager
import dev.lucasnlm.external.BillingManager
import dev.lucasnlm.external.InstantAppManager
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import dev.lucasnlm.antimine.i18n.R as i18n

abstract class CommonGameDialogFragment : AppCompatDialogFragment() {
    private val adsManager: AdsManager by inject()
    private val gameAudioManager: GameAudioManagerImpl by inject()
    private val instantAppManager: InstantAppManager by inject()
    private val analyticsManager: AnalyticsManager by inject()

    protected val preferencesRepository: PreferencesRepository by inject()
    protected val billingManager: BillingManager by inject()

    protected val isPremiumEnabled: Boolean by lazy {
        preferencesRepository.isPremiumEnabled()
    }

    protected val canRequestDonation: Boolean by lazy {
        preferencesRepository.requestDonation()
    }

    protected val isInstantMode: Boolean by lazy {
        context?.let { instantAppManager.isEnabled(it) } == true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!preferencesRepository.isPremiumEnabled()) {
            billingManager.start()
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

    private fun openHexLink(context: Context) {
        runCatching {
            val hexUri = "https://play.google.com/store/apps/details?id=dev.lucasnlm.hexo"
            val intent =
                Intent(Intent.ACTION_VIEW, Uri.parse(hexUri)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            context.startActivity(intent)
        }.onFailure {
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
                    context.dpToPx(HEX_BANNER_HEIGHT_DP),
                    Gravity.CENTER_HORIZONTAL,
                ),
            )
        }
    }

    protected fun showMusicDialog(adFrame: FrameLayout) {
        gameAudioManager.getComposerData().firstOrNull()?.let { composer ->
            adFrame.isVisible = true

            preferencesRepository.setLastMusicBanner(System.currentTimeMillis())

            val view = View.inflate(context, R.layout.music_link, null)
            view.run {
                findViewById<MaterialTextView>(R.id.music_by).text =
                    getString(i18n.string.music_by, composer.composer)

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

    private fun openComposer(composerLink: String) {
        val context = requireContext()

        runCatching {
            val intent =
                Intent(Intent.ACTION_VIEW, Uri.parse(composerLink)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            context.startActivity(intent)
        }.onFailure {
            Toast.makeText(
                context.applicationContext,
                i18n.string.unknown_error,
                Toast.LENGTH_SHORT,
            ).show()
        }
    }

    protected fun showAdBannerDialog(adFrame: FrameLayout) {
        adFrame.apply {
            isVisible = true

            post {
                addView(
                    adsManager.createBannerAd(
                        requireContext(),
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

    protected fun showSettings() {
        startActivity(Intent(requireContext(), PreferencesActivity::class.java))
    }

    protected fun showDonationDialog(adFrame: FrameLayout) {
        adFrame.isVisible = true

        val view = View.inflate(context, R.layout.donation_request, null)
        view.apply {
            setOnClickListener {
                gameAudioManager.playMonetization()
                activity?.let {
                    lifecycleScope.launch {
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

    protected fun showAdsAndContinue() {
        activity?.let { activity ->
            if (!activity.isFinishing) {
                adsManager.showRewardedAd(
                    activity,
                    skipIfFrequent = false,
                    onRewarded = {
                        continueGame()
                    },
                    onFail = {
                        adsManager.showInterstitialAd(
                            activity,
                            onDismiss = {
                                continueGame()
                            },
                            onError = {
                                Toast.makeText(context, i18n.string.no_network, Toast.LENGTH_SHORT).show()
                            },
                        )
                    },
                )
            }
        }
    }

    companion object {
        const val DIALOG_STATE = "dialog_state"
        const val HEX_BANNER_HEIGHT_DP = 75
        const val BACKGROUND_BLUR_RADIUS = 8
    }
}
