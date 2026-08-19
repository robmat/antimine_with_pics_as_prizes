package dev.lucasnlm.external

class FeatureFlagManagerImpl : FeatureFlagManager() {
    override val isFoss: Boolean = false

    override val isGameHistoryEnabled: Boolean = false

    override val isRateUsEnabled: Boolean = true

    override val isGameplayAnalyticsEnabled: Boolean = false

    override val isGameOverAdEnabled: Boolean = true

    override val isAdsOnContinueEnabled: Boolean = true

    override val isAdsOnNewGameEnabled: Boolean = true

    override val isContinueGameEnabled: Boolean = true

    override val minUsageToReview: Int = DEFAULT_MIN_USAGE_TO_REVIEW

    override val useInterstitialAd: Boolean = true

    override val isBannerAdEnabled: Boolean = true

    override val showCountdownToContinue: Boolean = true

    override suspend fun refresh() {
        // No-op: remote config backend not available in this build.
    }

    companion object {
        private const val DEFAULT_MIN_USAGE_TO_REVIEW = 5
    }
}
