package dev.lucasnlm.antimine.preferences

/**
 * Split out of [PreferencesRepository] - see [CorePreferences]'s doc.
 */
interface StatsRatingPremiumPreferences {
    fun updateStatsBase(statsBase: Int)

    fun getStatsBase(): Int

    fun getUseCount(): Int

    fun incrementUseCount()

    fun incrementProgressiveValue()

    fun decrementProgressiveValue()

    fun getProgressiveValue(): Int

    fun isRequestRatingEnabled(): Boolean

    fun disableRequestRating()

    fun setPremiumFeatures(status: Boolean)
}

internal class StatsRatingPremiumPreferencesImpl(
    private val preferencesManager: PreferencesManager,
) : StatsRatingPremiumPreferences {
    override fun updateStatsBase(statsBase: Int) {
        preferencesManager.putInt(PreferenceKeys.PREFERENCE_STATS_BASE, statsBase)
    }

    override fun getStatsBase(): Int = preferencesManager.getInt(PreferenceKeys.PREFERENCE_STATS_BASE, 0)

    override fun getUseCount(): Int = preferencesManager.getInt(PreferenceKeys.PREFERENCE_USE_COUNT, 0)

    override fun incrementUseCount() {
        val current = preferencesManager.getInt(PreferenceKeys.PREFERENCE_USE_COUNT, 0)
        preferencesManager.putInt(PreferenceKeys.PREFERENCE_USE_COUNT, current + 1)
    }

    override fun incrementProgressiveValue() {
        val value = preferencesManager.getInt(PreferenceKeys.PREFERENCE_PROGRESSIVE_VALUE, 0)
        preferencesManager.putInt(PreferenceKeys.PREFERENCE_PROGRESSIVE_VALUE, value + 1)
    }

    override fun decrementProgressiveValue() {
        val value = preferencesManager.getInt(PreferenceKeys.PREFERENCE_PROGRESSIVE_VALUE, 0)
        preferencesManager.putInt(PreferenceKeys.PREFERENCE_PROGRESSIVE_VALUE, (value - 1).coerceAtLeast(0))
    }

    override fun getProgressiveValue(): Int = preferencesManager.getInt(PreferenceKeys.PREFERENCE_PROGRESSIVE_VALUE, 0)

    override fun isRequestRatingEnabled(): Boolean =
        preferencesManager.getBoolean(PreferenceKeys.PREFERENCE_REQUEST_RATING, true)

    override fun disableRequestRating() {
        preferencesManager.putBoolean(PreferenceKeys.PREFERENCE_REQUEST_RATING, false)
    }

    override fun setPremiumFeatures(status: Boolean) {
        if (!preferencesManager.getBoolean(PreferenceKeys.PREFERENCE_PREMIUM_FEATURES, false)) {
            preferencesManager.putBoolean(PreferenceKeys.PREFERENCE_PREMIUM_FEATURES, status)
        }
    }
}
