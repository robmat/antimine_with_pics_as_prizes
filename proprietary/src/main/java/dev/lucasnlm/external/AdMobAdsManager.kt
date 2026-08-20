package dev.lucasnlm.external

import android.app.Activity
import android.content.Context
import android.view.View
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.initialization.AdapterStatus
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.rewarded.RewardedAd
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * The ad-loading/retry helper functions ([loadRewardAd] and its siblings)
 * were split out into AdMobAdsManagerLoading.kt since this class's function
 * count was over threshold; several fields are `internal` rather than
 * `private` only because those extension functions, living outside the
 * class body, need access to them.
 */
class AdMobAdsManager(
    internal val context: Context,
    private val crashReporter: CrashReporter,
    internal val scope: CoroutineScope,
) : AdsManager {
    internal var rewardedAd: RewardedAd? = null
    internal var secondRewardedAd: RewardedAd? = null

    internal var interstitialAd: InterstitialAd? = null
    internal var secondInterstitialAd: InterstitialAd? = null

    internal var failErrorCause: String? = null
    private var lastShownAd = 0L

    private var initialized = false

    override fun isAvailable(): Boolean = initialized

    override fun start(context: Context) {
        if (!initialized) {
            initialized = true

            MobileAds.initialize(context) {
                val providerCount =
                    it.adapterStatusMap.count { provider ->
                        provider.value.initializationState == AdapterStatus.State.READY
                    }

                if (providerCount != 0) {
                    scope.launch(Dispatchers.Main) {
                        preloadAds()
                    }
                } else {
                    initialized = false
                }
            }
        }
    }

    override fun showRewardedAd(
        activity: Activity,
        skipIfFrequent: Boolean,
        onStart: (() -> Unit)?,
        onRewarded: (() -> Unit)?,
        onFail: (() -> Unit)?,
    ) {
        if (skipIfFrequent && (System.currentTimeMillis() - lastShownAd < Ads.MIN_FREQUENCY)) {
            onRewarded?.invoke()
        } else {
            val rewardedAd = this.rewardedAd
            val secondRewardedAd = this.secondRewardedAd

            when {
                secondRewardedAd != null -> {
                    onStart?.invoke()
                    secondRewardedAd.show(activity) {
                        if (!activity.isFinishing) {
                            lastShownAd = System.currentTimeMillis()
                            onRewarded?.invoke()
                            scope.launch(Dispatchers.Main) {
                                loadSecondRewardAd()
                            }
                        }
                    }
                }

                rewardedAd != null -> {
                    onStart?.invoke()
                    rewardedAd.show(activity) {
                        if (!activity.isFinishing) {
                            lastShownAd = System.currentTimeMillis()
                            onRewarded?.invoke()
                            scope.launch(Dispatchers.Main) {
                                loadRewardAd()
                            }
                        }
                    }
                }

                else -> {
                    val message = failErrorCause?.let { "Fail to load Ad\n$it" } ?: "Fail to load Ad"
                    crashReporter.sendError(message)
                    onFail?.invoke()
                }
            }
        }
    }

    private fun buildFullScreenContentCallback(
        onDismissed: () -> Unit,
        onDismiss: () -> Unit,
        onError: (() -> Unit)?,
    ): FullScreenContentCallback =
        object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                onDismissed()
                onDismiss.invoke()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                crashReporter.sendError(
                    listOf(
                        "Fail to show InterstitialAd",
                        "Code: ${adError.code}",
                        "Message: ${adError.message}",
                        "Cause: ${adError.cause}",
                    ).joinToString("\n"),
                )
                (onError ?: onDismiss).invoke()
            }

            override fun onAdShowedFullScreenContent() {
                // Empty
            }
        }

    override fun showInterstitialAd(
        activity: Activity,
        onStart: (() -> Unit)?,
        onDismiss: (() -> Unit),
        onError: (() -> Unit)?,
    ) {
        secondInterstitialAd?.fullScreenContentCallback =
            buildFullScreenContentCallback(
                onDismissed = { loadSecondInterstitialAd() },
                onDismiss = onDismiss,
                onError = onError,
            )

        interstitialAd?.fullScreenContentCallback =
            buildFullScreenContentCallback(
                onDismissed = { scope.launch(Dispatchers.Main) { loadInterstitialAd() } },
                onDismiss = onDismiss,
                onError = onError,
            )

        if (interstitialAd == null && secondInterstitialAd == null) {
            (onError ?: onDismiss).invoke()
        } else {
            (secondInterstitialAd ?: interstitialAd)?.show(activity)
        }
    }

    override fun createBannerAd(
        context: Context,
        onError: (() -> Unit)?,
    ): View {
        val adRequest = AdRequest.Builder().build()
        return AdView(context).apply {
            setAdSize(AdSize.FULL_BANNER)
            adUnitId = Ads.BANNER_AD
            loadAd(adRequest)
            adListener =
                object : AdListener() {
                    override fun onAdFailedToLoad(error: LoadAdError) {
                        super.onAdFailedToLoad(error)
                        onError?.invoke()
                    }
                }
        }
    }

    companion object {
        const val MAX_RETRY = 3
        const val RETRY_DELAY_MS = 2000L
    }
}
