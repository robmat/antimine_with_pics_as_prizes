package dev.lucasnlm.antimine.preferences

import android.os.Build

/**
 * Split out of [PreferencesRepository] - see [CorePreferences]'s doc.
 */
interface SoundDisplayPreferences {
    fun isSoundEffectsEnabled(): Boolean

    fun setSoundEffectsEnabled(value: Boolean)

    fun isMusicEnabled(): Boolean

    fun setMusicEnabled(value: Boolean)

    fun touchSensibility(): Int

    fun setTouchSensibility(sensibility: Int)

    fun showWindowsWhenFinishGame(): Boolean

    fun mustShowWindowsWhenFinishGame(enabled: Boolean)

    fun openGameDirectly(): Boolean

    fun setOpenGameDirectly(value: Boolean)
}

internal class SoundDisplayPreferencesImpl(
    private val preferencesManager: PreferencesManager,
) : SoundDisplayPreferences {
    override fun isSoundEffectsEnabled(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            preferencesManager.getBoolean(PreferenceKeys.PREFERENCE_SOUND_EFFECTS, true)
    }

    override fun setSoundEffectsEnabled(value: Boolean) {
        preferencesManager.putBoolean(PreferenceKeys.PREFERENCE_SOUND_EFFECTS, value)
    }

    override fun isMusicEnabled(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            preferencesManager.getBoolean(PreferenceKeys.PREFERENCE_MUSIC, true)
    }

    override fun setMusicEnabled(value: Boolean) {
        preferencesManager.putBoolean(PreferenceKeys.PREFERENCE_MUSIC, value)
    }

    override fun touchSensibility(): Int =
        preferencesManager.getInt(PreferenceKeys.PREFERENCE_TOUCH_SENSIBILITY, DEFAULT_TOUCH_SENSIBILITY)

    override fun setTouchSensibility(sensibility: Int) {
        preferencesManager.putInt(PreferenceKeys.PREFERENCE_TOUCH_SENSIBILITY, sensibility)
    }

    override fun showWindowsWhenFinishGame(): Boolean {
        return preferencesManager.getBoolean(PreferenceKeys.PREFERENCE_SHOW_WINDOWS, true)
    }

    override fun mustShowWindowsWhenFinishGame(enabled: Boolean) {
        preferencesManager.putBoolean(PreferenceKeys.PREFERENCE_SHOW_WINDOWS, enabled)
    }

    override fun openGameDirectly(): Boolean {
        return preferencesManager.getBoolean(PreferenceKeys.PREFERENCE_OPEN_DIRECTLY, false)
    }

    override fun setOpenGameDirectly(value: Boolean) {
        preferencesManager.putBoolean(PreferenceKeys.PREFERENCE_OPEN_DIRECTLY, value)
    }

    private companion object {
        const val DEFAULT_TOUCH_SENSIBILITY = 5
    }
}
