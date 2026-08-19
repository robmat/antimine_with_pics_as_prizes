package dev.lucasnlm.antimine.preferences

/**
 * Split out of [PreferencesRepository] - see [CorePreferences]'s doc.
 */
interface SupportHelpPreferences {
    fun isPremiumEnabled(): Boolean

    fun setRequestDonation(request: Boolean)

    fun requestDonation(): Boolean

    fun setShowSupport(show: Boolean)

    fun showSupport(): Boolean

    fun useHelp(): Boolean

    fun setHelp(value: Boolean)

    fun lastHelpUsed(): Long

    fun refreshLastHelpUsed()

    fun useSimonTathamAlgorithm(): Boolean
}

internal class SupportHelpPreferencesImpl(
    private val preferencesManager: PreferencesManager,
) : SupportHelpPreferences {
    override fun isPremiumEnabled(): Boolean {
        return preferencesManager.getBoolean(PreferenceKeys.PREFERENCE_PREMIUM_FEATURES, false)
    }

    override fun setRequestDonation(request: Boolean) {
        preferencesManager.putBoolean(PreferenceKeys.PREFERENCE_REQUEST_DONATION, request)
    }

    override fun requestDonation(): Boolean {
        return false // preferencesManager.getBoolean(PreferenceKeys.PREFERENCE_REQUEST_DONATION, true)
    }

    override fun setShowSupport(show: Boolean) {
        preferencesManager.putBoolean(PreferenceKeys.PREFERENCE_SHOW_SUPPORT, show)
    }

    override fun showSupport(): Boolean {
        return preferencesManager.getBoolean(PreferenceKeys.PREFERENCE_SHOW_SUPPORT, true)
    }

    override fun useHelp(): Boolean {
        return preferencesManager.getBoolean(PreferenceKeys.PREFERENCE_USE_HINT, true)
    }

    override fun setHelp(value: Boolean) {
        preferencesManager.putBoolean(PreferenceKeys.PREFERENCE_USE_HINT, value)
    }

    override fun lastHelpUsed(): Long {
        return preferencesManager.getLong(PreferenceKeys.PREFERENCE_LAST_HELP_USED, 0L)
    }

    override fun refreshLastHelpUsed() {
        preferencesManager.putLong(PreferenceKeys.PREFERENCE_LAST_HELP_USED, System.currentTimeMillis())
    }

    override fun useSimonTathamAlgorithm(): Boolean {
        return preferencesManager.getBoolean(PreferenceKeys.PREFERENCE_SIMON_TATHAM_ALGORITHM, true)
    }
}
