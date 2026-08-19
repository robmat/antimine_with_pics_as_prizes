package dev.lucasnlm.antimine.preferences

import dev.lucasnlm.antimine.preferences.models.Action

/**
 * Split out of [PreferencesRepository] - see [CorePreferences]'s doc.
 */
interface TipsControlActionPreferences {
    fun setSimonTathamAlgorithm(enabled: Boolean)

    fun getTips(): Int

    fun setTips(tips: Int)

    fun getExtraTips(): Int

    fun setExtraTips(tips: Int)

    fun getSwitchControlAction(): Action

    fun setSwitchControl(action: Action)

    fun useFlagAssistant(): Boolean

    fun setFlagAssistant(value: Boolean)

    fun dimNumbers(): Boolean
}

internal class TipsControlActionPreferencesImpl(
    private val preferencesManager: PreferencesManager,
) : TipsControlActionPreferences {
    override fun setSimonTathamAlgorithm(enabled: Boolean) {
        preferencesManager.putBoolean(PreferenceKeys.PREFERENCE_SIMON_TATHAM_ALGORITHM, enabled)
    }

    override fun getTips(): Int {
        return preferencesManager.getInt(PreferenceKeys.PREFERENCE_HINTS, DEFAULT_HINTS)
    }

    override fun setTips(tips: Int) {
        preferencesManager.putInt(PreferenceKeys.PREFERENCE_HINTS, tips)
    }

    override fun getExtraTips(): Int {
        return preferencesManager.getInt(PreferenceKeys.PREFERENCE_EXTRA_HINTS, 0)
    }

    override fun setExtraTips(tips: Int) {
        preferencesManager.putInt(PreferenceKeys.PREFERENCE_EXTRA_HINTS, tips)
    }

    override fun getSwitchControlAction(): Action {
        return preferencesManager.getInt(
            PreferenceKeys.PREFERENCE_USE_OPEN_SWITCH_CONTROL,
            Action.OpenTile.ordinal,
        ).let {
            Action.values()[it]
        }
    }

    override fun setSwitchControl(action: Action) {
        preferencesManager.putInt(PreferenceKeys.PREFERENCE_USE_OPEN_SWITCH_CONTROL, action.ordinal)
    }

    override fun useFlagAssistant(): Boolean = preferencesManager.getBoolean(PreferenceKeys.PREFERENCE_ASSISTANT, true)

    override fun setFlagAssistant(value: Boolean) {
        preferencesManager.putBoolean(PreferenceKeys.PREFERENCE_ASSISTANT, value)
    }

    override fun dimNumbers(): Boolean {
        return preferencesManager.getBoolean(PreferenceKeys.PREFERENCE_DIM_NUMBERS, true)
    }

    private companion object {
        const val DEFAULT_HINTS = 5
    }
}
