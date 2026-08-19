package dev.lucasnlm.antimine.preferences

import android.view.ViewConfiguration

/**
 * Split out of [PreferencesRepository] - see [CorePreferences]'s doc.
 */
interface TimingThemeLocalePreferences {
    fun customLongPressTimeout(): Long

    fun setCustomLongPressTimeout(value: Long)

    fun getDoubleClickTimeout(): Long

    fun setDoubleClickTimeout(value: Long)

    fun themeId(): Long?

    fun useTheme(themeId: Long)

    fun skinId(): Long

    fun useSkin(skinId: Long)

    fun setPreferredLocale(locale: String)

    fun getPreferredLocale(): String?
}

internal class TimingThemeLocalePreferencesImpl(
    private val preferencesManager: PreferencesManager,
) : TimingThemeLocalePreferences {
    override fun customLongPressTimeout(): Long =
        preferencesManager.getInt(
            PreferenceKeys.PREFERENCE_LONG_PRESS_TIMEOUT,
            ViewConfiguration.getLongPressTimeout(),
        ).toLong()

    override fun setCustomLongPressTimeout(value: Long) {
        preferencesManager.putInt(PreferenceKeys.PREFERENCE_LONG_PRESS_TIMEOUT, value.toInt())
    }

    override fun getDoubleClickTimeout(): Long {
        return preferencesManager.getInt(
            PreferenceKeys.PREFERENCE_DOUBLE_CLICK_TIMEOUT,
            DEFAULT_DOUBLE_CLICK_TIMEOUT_MS,
        ).toLong()
    }

    override fun setDoubleClickTimeout(value: Long) {
        preferencesManager.putInt(PreferenceKeys.PREFERENCE_DOUBLE_CLICK_TIMEOUT, value.toInt())
    }

    override fun themeId(): Long? = preferencesManager.getIntOrNull(PreferenceKeys.PREFERENCE_CUSTOM_THEME)?.toLong()

    override fun useTheme(themeId: Long) {
        preferencesManager.putInt(PreferenceKeys.PREFERENCE_CUSTOM_THEME, themeId.toInt())
    }

    override fun skinId(): Long {
        return preferencesManager.getInt(PreferenceKeys.PREFERENCE_CUSTOM_SKIN, 0).toLong()
    }

    override fun useSkin(skinId: Long) {
        preferencesManager.putInt(PreferenceKeys.PREFERENCE_CUSTOM_SKIN, skinId.toInt())
    }

    override fun setPreferredLocale(locale: String) {
        preferencesManager.putString(PreferenceKeys.PREFERENCE_LOCALE, locale)
    }

    override fun getPreferredLocale(): String? {
        return preferencesManager.getString(PreferenceKeys.PREFERENCE_LOCALE)
    }

    private companion object {
        const val DEFAULT_DOUBLE_CLICK_TIMEOUT_MS = 250
    }
}
