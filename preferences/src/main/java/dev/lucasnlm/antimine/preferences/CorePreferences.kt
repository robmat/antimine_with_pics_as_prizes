package dev.lucasnlm.antimine.preferences

import dev.lucasnlm.antimine.preferences.models.ControlStyle
import dev.lucasnlm.antimine.preferences.models.Minefield

/**
 * [PreferencesRepository] used to declare all of its ~100 functions
 * directly, far over detekt's threshold. Its members are now spread across
 * this and several sibling sub-interfaces (OnboardingPreferences,
 * TimingThemeLocalePreferences, StatsRatingPremiumPreferences,
 * SupportHelpPreferences, TipsControlActionPreferences,
 * FeedbackAnimationPreferences, SoundDisplayPreferences,
 * UserInterfacePreferences, GameFlowPreferences), each implemented by its
 * own class and composed into [PreferencesRepositoryImpl] via interface
 * delegation, so existing callers keep seeing a single unified contract.
 */
interface CorePreferences {
    fun hasCustomizations(): Boolean

    fun reset()

    fun hasControlCustomizations(): Boolean

    fun resetControls()

    fun customGameMode(): Minefield

    fun updateCustomGameMode(minefield: Minefield)

    fun forgetCustomSeed()

    fun controlStyle(): ControlStyle

    fun hasCustomControlStyle(): Boolean

    fun useControlStyle(controlStyle: ControlStyle)
}

internal class CorePreferencesImpl(
    private val preferencesManager: PreferencesManager,
) : CorePreferences {
    private val listOfControlCustoms =
        listOf(
            PreferenceKeys.PREFERENCE_TOUCH_SENSIBILITY,
            PreferenceKeys.PREFERENCE_LONG_PRESS_TIMEOUT,
            PreferenceKeys.PREFERENCE_DOUBLE_CLICK_TIMEOUT,
        )

    private val listOfSettingsCustoms =
        listOf(
            PreferenceKeys.PREFERENCE_ASSISTANT,
            PreferenceKeys.PREFERENCE_ANIMATION,
            PreferenceKeys.PREFERENCE_QUESTION_MARK,
            PreferenceKeys.PREFERENCE_USE_HINT,
            PreferenceKeys.PREFERENCE_SOUND_EFFECTS,
            PreferenceKeys.PREFERENCE_SHOW_WINDOWS,
            PreferenceKeys.PREFERENCE_OPEN_DIRECTLY,
            PreferenceKeys.PREFERENCE_ALLOW_TAP_NUMBER,
            PreferenceKeys.PREFERENCE_SHOW_CLOCK,
            PreferenceKeys.PREFERENCE_DIM_NUMBERS,
            PreferenceKeys.PREFERENCE_LET_NUMBERS_AUTO_FLAG,
        )

    override fun hasCustomizations(): Boolean {
        val vibrationDisabled = preferencesManager.getBoolean(PreferenceKeys.PREFERENCE_VIBRATION, true)
        return listOfSettingsCustoms.any { preferencesManager.contains(it) } || !vibrationDisabled
    }

    override fun hasControlCustomizations(): Boolean {
        return listOfControlCustoms.fold(false) { acc, current ->
            acc || preferencesManager.contains(current)
        }
    }

    override fun resetControls() {
        listOfControlCustoms.forEach { preferencesManager.removeKey(it) }
    }

    override fun reset() {
        listOfSettingsCustoms.forEach { preferencesManager.removeKey(it) }
    }

    override fun forgetCustomSeed() {
        preferencesManager.removeKey(PreferenceKeys.PREFERENCE_CUSTOM_GAME_SEED)
    }

    override fun customGameMode(): Minefield =
        with(preferencesManager) {
            Minefield(
                getInt(PreferenceKeys.PREFERENCE_CUSTOM_GAME_WIDTH, DEFAULT_CUSTOM_GAME_SIZE),
                getInt(PreferenceKeys.PREFERENCE_CUSTOM_GAME_HEIGHT, DEFAULT_CUSTOM_GAME_SIZE),
                getInt(PreferenceKeys.PREFERENCE_CUSTOM_GAME_MINES, DEFAULT_CUSTOM_GAME_SIZE),
                getLongOrNull(PreferenceKeys.PREFERENCE_CUSTOM_GAME_SEED),
            )
        }

    override fun updateCustomGameMode(minefield: Minefield) {
        preferencesManager.apply {
            putInt(PreferenceKeys.PREFERENCE_CUSTOM_GAME_WIDTH, minefield.width)
            putInt(PreferenceKeys.PREFERENCE_CUSTOM_GAME_HEIGHT, minefield.height)
            putInt(PreferenceKeys.PREFERENCE_CUSTOM_GAME_MINES, minefield.mines)
            if (minefield.seed != null) {
                putLong(PreferenceKeys.PREFERENCE_CUSTOM_GAME_SEED, minefield.seed)
            } else {
                removeKey(PreferenceKeys.PREFERENCE_CUSTOM_GAME_SEED)
            }
        }
    }

    override fun controlStyle(): ControlStyle {
        val index = preferencesManager.getInt(PreferenceKeys.PREFERENCE_CONTROL_STYLE, -1)
        return ControlStyle.values().getOrNull(index) ?: ControlStyle.SwitchMarkOpen
    }

    override fun hasCustomControlStyle(): Boolean {
        return preferencesManager.contains(PreferenceKeys.PREFERENCE_CONTROL_STYLE)
    }

    override fun useControlStyle(controlStyle: ControlStyle) {
        preferencesManager.putInt(PreferenceKeys.PREFERENCE_CONTROL_STYLE, controlStyle.ordinal)
    }

    private companion object {
        const val DEFAULT_CUSTOM_GAME_SIZE = 9
    }
}
