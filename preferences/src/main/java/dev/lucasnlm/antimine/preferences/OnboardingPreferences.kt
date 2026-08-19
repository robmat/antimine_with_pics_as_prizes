package dev.lucasnlm.antimine.preferences

/**
 * Split out of [PreferencesRepository] - see [CorePreferences]'s doc.
 */
interface OnboardingPreferences {
    fun isFirstUse(): Boolean

    fun completeFirstUse()

    fun isTutorialCompleted(): Boolean

    fun setCompleteTutorial(value: Boolean)

    fun showTutorialButton(): Boolean

    fun setShowTutorialButton(value: Boolean)

    fun showMusicBanner(): Boolean

    fun setShowMusicBanner(value: Boolean)

    fun lastMusicBanner(): Long

    fun setLastMusicBanner(value: Long)
}

internal class OnboardingPreferencesImpl(
    private val preferencesManager: PreferencesManager,
) : OnboardingPreferences {
    override fun isFirstUse(): Boolean = preferencesManager.getBoolean(PreferenceKeys.PREFERENCE_FIRST_USE, true)

    override fun completeFirstUse() {
        preferencesManager.putBoolean(PreferenceKeys.PREFERENCE_FIRST_USE, false)
    }

    override fun isTutorialCompleted(): Boolean {
        return preferencesManager.getBoolean(PreferenceKeys.PREFERENCE_TUTORIAL_COMPLETED, false)
    }

    override fun setCompleteTutorial(value: Boolean) {
        preferencesManager.putBoolean(PreferenceKeys.PREFERENCE_TUTORIAL_COMPLETED, value)
    }

    override fun showTutorialButton(): Boolean {
        return preferencesManager.getBoolean(PreferenceKeys.PREFERENCE_SHOULD_SHOW_TUTORIAL_BUTTON, true)
    }

    override fun setShowTutorialButton(value: Boolean) {
        preferencesManager.putBoolean(PreferenceKeys.PREFERENCE_SHOULD_SHOW_TUTORIAL_BUTTON, value)
    }

    override fun showMusicBanner(): Boolean {
        return preferencesManager.getBoolean(PreferenceKeys.PREFERENCE_MUSIC_BANNER, true)
    }

    override fun setShowMusicBanner(value: Boolean) {
        preferencesManager.putBoolean(PreferenceKeys.PREFERENCE_MUSIC_BANNER, value)
    }

    override fun lastMusicBanner(): Long {
        return preferencesManager.getLong(PreferenceKeys.PREFERENCE_MUSIC_BANNER_LAST, 0L)
    }

    override fun setLastMusicBanner(value: Long) {
        preferencesManager.putLong(PreferenceKeys.PREFERENCE_MUSIC_BANNER_LAST, value)
    }
}
