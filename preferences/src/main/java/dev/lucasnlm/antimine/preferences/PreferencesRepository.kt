package dev.lucasnlm.antimine.preferences

/**
 * See [CorePreferences]'s doc for why this interface no longer declares its
 * ~100 functions directly - it now inherits them from cohesive
 * sub-interfaces so existing callers keep seeing a single unified contract.
 */
interface PreferencesRepository :
    CorePreferences,
    OnboardingPreferences,
    TimingThemeLocalePreferences,
    StatsRatingPremiumPreferences,
    SupportHelpPreferences,
    TipsControlActionPreferences,
    FeedbackAnimationPreferences,
    SoundDisplayPreferences,
    UserInterfacePreferences,
    GameFlowPreferences
