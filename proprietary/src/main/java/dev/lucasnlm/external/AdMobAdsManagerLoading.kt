package dev.lucasnlm.external

import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * [AdMobAdsManager]'s ad-loading/retry logic, split out of the class body -
 * see its class doc.
 */
internal suspend fun AdMobAdsManager.loadRewardAd() {
    var rewardedAdRetry = 0
    val adRequest = AdRequest.Builder().build()
    RewardedAd.load(
        context,
        Ads.REWARD_AD,
        adRequest,
        object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                failErrorCause = adError.message
                rewardedAd = null

                if (rewardedAdRetry < AdMobAdsManager.MAX_RETRY) {
                    rewardedAdRetry++
                    scope.launch(Dispatchers.Main) {
                        delay(AdMobAdsManager.RETRY_DELAY_MS)
                        loadRewardAd()
                    }
                }
            }

            override fun onAdLoaded(result: RewardedAd) {
                rewardedAd = result
                rewardedAdRetry = 0
            }
        },
    )
}

internal suspend fun AdMobAdsManager.loadSecondRewardAd() {
    var rewardedAdRetry = 0
    val adRequest = AdRequest.Builder().build()
    RewardedAd.load(
        context,
        Ads.SECOND_REWARD_AD,
        adRequest,
        object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                failErrorCause = adError.message
                secondRewardedAd = null

                if (rewardedAdRetry < AdMobAdsManager.MAX_RETRY) {
                    rewardedAdRetry++
                    scope.launch(Dispatchers.Main) {
                        delay(AdMobAdsManager.RETRY_DELAY_MS)
                        loadSecondRewardAd()
                    }
                }
            }

            override fun onAdLoaded(result: RewardedAd) {
                secondRewardedAd = result
                rewardedAdRetry = 0
            }
        },
    )
}

internal suspend fun AdMobAdsManager.loadInterstitialAd() {
    var interstitialAdRetry = 0
    val adRequest = AdRequest.Builder().build()
    InterstitialAd.load(
        context,
        Ads.INTERSTITIAL_AD,
        adRequest,
        object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                failErrorCause = adError.message
                interstitialAd = null

                if (interstitialAdRetry < AdMobAdsManager.MAX_RETRY) {
                    scope.launch(Dispatchers.Main) {
                        delay(AdMobAdsManager.RETRY_DELAY_MS)
                        loadInterstitialAd()
                        interstitialAdRetry++
                    }
                }
            }

            override fun onAdLoaded(result: InterstitialAd) {
                interstitialAd = result
                interstitialAdRetry = 0
            }
        },
    )
}

internal fun AdMobAdsManager.loadSecondInterstitialAd() {
    var interstitialAdRetry = 0
    val adRequest = AdRequest.Builder().build()
    InterstitialAd.load(
        context,
        Ads.SECOND_INTERSTITIAL_AD,
        adRequest,
        object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                failErrorCause = adError.message
                secondInterstitialAd = null

                if (interstitialAdRetry < AdMobAdsManager.MAX_RETRY) {
                    scope.launch(Dispatchers.Main) {
                        delay(AdMobAdsManager.RETRY_DELAY_MS)
                        loadSecondInterstitialAd()
                        interstitialAdRetry++
                    }
                }
            }

            override fun onAdLoaded(result: InterstitialAd) {
                secondInterstitialAd = result
                interstitialAdRetry = 0
            }
        },
    )
}

internal suspend fun AdMobAdsManager.preloadAds() {
    withContext(Dispatchers.Main) {
        loadRewardAd()
        loadSecondRewardAd()
        loadInterstitialAd()
        loadSecondInterstitialAd()
    }
}
