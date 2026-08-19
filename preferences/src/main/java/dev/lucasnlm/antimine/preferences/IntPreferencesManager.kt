package dev.lucasnlm.antimine.preferences

import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * Split out of [PreferencesManager] - see [BooleanPreferencesManager]'s doc.
 */
interface IntPreferencesManager {
    fun getInt(
        key: String,
        defaultValue: Int,
    ): Int

    fun getIntOrNull(key: String): Int?

    fun putInt(
        key: String,
        value: Int,
    )
}

internal class IntPreferencesManagerImpl(
    private val preferences: SharedPreferences,
) : IntPreferencesManager {
    override fun getInt(
        key: String,
        defaultValue: Int,
    ) = preferences.getInt(key, defaultValue)

    override fun getIntOrNull(key: String): Int? {
        return if (preferences.contains(key)) {
            preferences.getInt(key, -1)
        } else {
            null
        }
    }

    override fun putInt(
        key: String,
        value: Int,
    ) = preferences.edit { putInt(key, value) }
}
