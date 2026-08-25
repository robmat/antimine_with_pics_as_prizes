package dev.lucasnlm.antimine.preferences

/**
 * Split out of [PreferencesRepository] - see [CorePreferences]'s doc.
 */
interface GameFlowPreferences {
    fun showContinueGame(): Boolean

    fun setContinueGameLabel(value: Boolean)

    fun showNewThemesIcon(): Boolean

    fun setNewThemesIcon(visible: Boolean)

    fun exportData(): Map<String, Any?>

    fun importData(data: Map<String, Any?>)

    fun keepRequestPlayGames(): Boolean

    fun setRequestPlayGames(showRequest: Boolean)

    fun lastAppVersion(): Int?

    fun setLastAppVersion(versionCode: Int)
}

internal class GameFlowPreferencesImpl(
    private val preferencesManager: PreferencesManager,
) : GameFlowPreferences {
    override fun showContinueGame(): Boolean =
        preferencesManager.getBoolean(PreferenceKeys.PREFERENCE_SHOW_CONTINUE, false)

    override fun setContinueGameLabel(value: Boolean) {
        preferencesManager.putBoolean(PreferenceKeys.PREFERENCE_SHOW_CONTINUE, value)
    }

    override fun showNewThemesIcon(): Boolean =
        preferencesManager.getBoolean(PreferenceKeys.PREFERENCE_NEW_THEMES_ICON, true)

    override fun setNewThemesIcon(visible: Boolean) {
        preferencesManager.putBoolean(PreferenceKeys.PREFERENCE_NEW_THEMES_ICON, visible)
    }

    override fun exportData(): Map<String, Any?> = preferencesManager.toMap()

    override fun importData(data: Map<String, Any?>) {
        val wasPremium = preferencesManager.getBoolean(PreferenceKeys.PREFERENCE_PREMIUM_FEATURES, false)

        preferencesManager.clear()

        data
            .filter {
                it.key != PreferenceKeys.PREFERENCE_PREMIUM_FEATURES
            }.forEach { (key, value) ->
                when (value) {
                    null -> {
                        // Ignore
                    }

                    is Long -> {
                        preferencesManager.putLong(key, value)
                    }

                    is Int -> {
                        preferencesManager.putInt(key, value)
                    }

                    is String -> {
                        preferencesManager.putString(key, value)
                    }

                    is Boolean -> {
                        preferencesManager.putBoolean(key, value)
                    }
                }
            }

        if (wasPremium) {
            preferencesManager.putBoolean(PreferenceKeys.PREFERENCE_PREMIUM_FEATURES, true)
        }
    }

    override fun keepRequestPlayGames(): Boolean =
        preferencesManager.getBoolean(PreferenceKeys.PREFERENCE_REQUEST_PLAY_GAMES, true)

    override fun setRequestPlayGames(showRequest: Boolean) {
        preferencesManager.putBoolean(PreferenceKeys.PREFERENCE_REQUEST_PLAY_GAMES, showRequest)
    }

    override fun lastAppVersion(): Int? = preferencesManager.getIntOrNull(PreferenceKeys.PREFERENCE_LAST_VERSION)

    override fun setLastAppVersion(versionCode: Int) {
        if (versionCode > 0) {
            preferencesManager.putInt(PreferenceKeys.PREFERENCE_LAST_VERSION, versionCode)
        }
    }
}
