package dev.lucasnlm.antimine.preferences

import dev.lucasnlm.antimine.preferences.models.ControlStyle

/**
 * See [PreferencesRepository]'s doc - implementation is composed via
 * interface delegation to the per-domain Impl classes instead of declaring
 * all ~100 overrides directly, which was over detekt's threshold.
 */
class PreferencesRepositoryImpl(
    private val preferencesManager: PreferencesManager,
    private val defaultLongPressTimeout: Int,
) : PreferencesRepository,
    CorePreferences by CorePreferencesImpl(preferencesManager),
    OnboardingPreferences by OnboardingPreferencesImpl(preferencesManager),
    TimingThemeLocalePreferences by TimingThemeLocalePreferencesImpl(preferencesManager),
    StatsRatingPremiumPreferences by StatsRatingPremiumPreferencesImpl(preferencesManager),
    SupportHelpPreferences by SupportHelpPreferencesImpl(preferencesManager),
    TipsControlActionPreferences by TipsControlActionPreferencesImpl(preferencesManager),
    FeedbackAnimationPreferences by FeedbackAnimationPreferencesImpl(preferencesManager),
    SoundDisplayPreferences by SoundDisplayPreferencesImpl(preferencesManager),
    UserInterfacePreferences by UserInterfacePreferencesImpl(preferencesManager),
    GameFlowPreferences by GameFlowPreferencesImpl(preferencesManager) {
    init {
        migrateOldPreferences()
    }

    private fun migrateOldPreferences() {
        // Migrate Double Click to the new Control settings
        if (preferencesManager.contains(PreferenceKeys.PREFERENCE_OLD_DOUBLE_CLICK)) {
            if (preferencesManager.getBoolean(PreferenceKeys.PREFERENCE_OLD_DOUBLE_CLICK, false)) {
                useControlStyle(ControlStyle.DoubleClick)
            }

            preferencesManager.removeKey(PreferenceKeys.PREFERENCE_OLD_DOUBLE_CLICK)
        }

        if (!preferencesManager.contains(PreferenceKeys.PREFERENCE_LONG_PRESS_TIMEOUT)) {
            preferencesManager.putInt(PreferenceKeys.PREFERENCE_LONG_PRESS_TIMEOUT, defaultLongPressTimeout)
        }

        if (preferencesManager.contains(PreferenceKeys.PREFERENCE_FIRST_USE)) {
            preferencesManager.putBoolean(PreferenceKeys.PREFERENCE_TUTORIAL_COMPLETED, true)
        }
    }
}
