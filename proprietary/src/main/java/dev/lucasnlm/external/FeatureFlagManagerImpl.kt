package dev.lucasnlm.external

class FeatureFlagManagerImpl : FeatureFlagManager() {
    private val defaultMap by lazy {
        mapOf(
            HISTORY_ENABLED to isGameHistoryEnabled,
            RATE_US_ENABLED to isRateUsEnabled,
            GAMEPLAY_EVENTS_ENABLED to isGameplayAnalyticsEnabled,
            GAME_OVER_AD_ENABLED to isGameOverAdEnabled,
            SHOW_ADS_ON_CONTINUE_ENABLED to isAdsOnContinueEnabled,
            SHOW_ADS_ON_NEW_GAME_ENABLED to isAdsOnNewGameEnabled,
            CONTINUE_ENABLED to isContinueGameEnabled,
            MIN_USAGE_TO_REVIEW to minUsageToReview,
            USE_INTERSTITIAL_AD to useInterstitialAd,
            BANNER_AD_ENABLED to isBannerAdEnabled,
            SHOW_COUNTDOWN_TO_CONTINUE to showCountdownToContinue,
        )
    }

    private fun getBoolean(
        key: String,
        default: Boolean,
    ): Boolean {
        return default
    }

    private fun getInt(
        key: String,
        default: Int,
    ): Int {
        return default
    }

    override val isFoss: Boolean = false

    override val isGameHistoryEnabled: Boolean by lazy {
        getBoolean(HISTORY_ENABLED, false)
    }

    override val isRateUsEnabled: Boolean by lazy {
        getBoolean(RATE_US_ENABLED, true)
    }

    override val isGameplayAnalyticsEnabled: Boolean by lazy {
        getBoolean(GAMEPLAY_EVENTS_ENABLED, false)
    }

    override val isGameOverAdEnabled: Boolean by lazy {
        getBoolean(GAME_OVER_AD_ENABLED, true)
    }

    override val isAdsOnContinueEnabled: Boolean by lazy {
        getBoolean(SHOW_ADS_ON_CONTINUE_ENABLED, true)
    }

    override val isAdsOnNewGameEnabled: Boolean by lazy {
        getBoolean(SHOW_ADS_ON_NEW_GAME_ENABLED, true)
    }

    override val isContinueGameEnabled: Boolean by lazy {
        getBoolean(CONTINUE_ENABLED, true)
    }

    override val minUsageToReview: Int by lazy {
        getInt(MIN_USAGE_TO_REVIEW, DEFAULT_MIN_USAGE_TO_REVIEW)
    }

    override val useInterstitialAd: Boolean by lazy {
        getBoolean(USE_INTERSTITIAL_AD, true)
    }

    override val isBannerAdEnabled: Boolean by lazy {
        getBoolean(BANNER_AD_ENABLED, true)
    }

    override val showCountdownToContinue: Boolean by lazy {
        getBoolean(SHOW_COUNTDOWN_TO_CONTINUE, true)
    }

    override suspend fun refresh() {
    }

    companion object {
        private val TAG = FeatureFlagManagerImpl::class.simpleName

        private const val HISTORY_ENABLED = "history_enabled"
        private const val RATE_US_ENABLED = "rate_us_enabled"
        private const val GAMEPLAY_EVENTS_ENABLED = "gameplay_events_enabled"
        private const val GAME_OVER_AD_ENABLED = "game_over_ad_enabled"
        private const val SHOW_ADS_ON_CONTINUE_ENABLED = "ad_on_continue_enabled"
        private const val SHOW_ADS_ON_NEW_GAME_ENABLED = "ad_on_new_game_enabled"
        private const val CONTINUE_ENABLED = "continue_enabled"
        private const val MIN_USAGE_TO_REVIEW = "min_usage_to_review"
        private const val USE_INTERSTITIAL_AD = "use_interstitial_ad"
        private const val BANNER_AD_ENABLED = "banner_ad_enabled"
        private const val SHOW_COUNTDOWN_TO_CONTINUE = "show_countdown_to_continue"

        private const val DEFAULT_MIN_USAGE_TO_REVIEW = 5
    }
}
