package dev.lucasnlm.antimine.preferences

/**
 * Split out of [PreferencesRepository] - see [CorePreferences]'s doc.
 */
interface UserInterfacePreferences {
    fun userId(): String?

    fun setUserId(userId: String)

    fun showTutorialDialog(): Boolean

    fun setTutorialDialog(show: Boolean)

    fun allowTapOnNumbers(): Boolean

    fun setAllowTapOnNumbers(allow: Boolean)

    fun letNumbersAutoFlag(): Boolean

    fun setNumbersAutoFlag(allow: Boolean)

    fun showTimer(): Boolean

    fun setTimerVisible(visible: Boolean)
}

internal class UserInterfacePreferencesImpl(
    private val preferencesManager: PreferencesManager,
) : UserInterfacePreferences {
    override fun userId(): String? = preferencesManager.getString(PreferenceKeys.PREFERENCE_USER_ID)

    override fun setUserId(userId: String) {
        if (userId.isBlank()) {
            preferencesManager.removeKey(userId)
        } else {
            preferencesManager.putString(PreferenceKeys.PREFERENCE_USER_ID, userId)
        }
    }

    override fun showTutorialDialog(): Boolean =
        preferencesManager.getBoolean(PreferenceKeys.PREFERENCE_TUTORIAL_DIALOG, true)

    override fun setTutorialDialog(show: Boolean) {
        preferencesManager.putBoolean(PreferenceKeys.PREFERENCE_TUTORIAL_DIALOG, show)
    }

    override fun allowTapOnNumbers(): Boolean =
        preferencesManager.getBoolean(PreferenceKeys.PREFERENCE_ALLOW_TAP_NUMBER, true)

    override fun setAllowTapOnNumbers(allow: Boolean) {
        preferencesManager.putBoolean(PreferenceKeys.PREFERENCE_ALLOW_TAP_NUMBER, allow)
    }

    override fun letNumbersAutoFlag(): Boolean =
        preferencesManager.getBoolean(PreferenceKeys.PREFERENCE_LET_NUMBERS_AUTO_FLAG, true)

    override fun setNumbersAutoFlag(allow: Boolean) {
        preferencesManager.putBoolean(PreferenceKeys.PREFERENCE_LET_NUMBERS_AUTO_FLAG, allow)
    }

    override fun showTimer(): Boolean = preferencesManager.getBoolean(PreferenceKeys.PREFERENCE_SHOW_CLOCK, true)

    override fun setTimerVisible(visible: Boolean) {
        preferencesManager.putBoolean(PreferenceKeys.PREFERENCE_SHOW_CLOCK, visible)
    }
}
