package dev.lucasnlm.antimine.preferences

import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * Split out of [PreferencesManager] - see [BooleanPreferencesManager]'s doc.
 */
interface StringPreferencesManager {
    fun getString(key: String): String?

    fun putString(
        key: String,
        value: String,
    )
}

internal class StringPreferencesManagerImpl(
    private val preferences: SharedPreferences,
) : StringPreferencesManager {
    override fun getString(key: String): String? = preferences.getString(key, null)

    override fun putString(
        key: String,
        value: String,
    ) = preferences.edit { putString(key, value) }
}
