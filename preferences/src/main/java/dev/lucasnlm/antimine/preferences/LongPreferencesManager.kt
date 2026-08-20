package dev.lucasnlm.antimine.preferences

import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * Split out of [PreferencesManager] - see [BooleanPreferencesManager]'s doc.
 */
interface LongPreferencesManager {
    fun getLong(
        key: String,
        defaultValue: Long,
    ): Long

    fun putLong(
        key: String,
        value: Long,
    )

    fun getLongOrNull(key: String): Long?
}

internal class LongPreferencesManagerImpl(
    private val preferences: SharedPreferences,
) : LongPreferencesManager {
    override fun getLong(
        key: String,
        defaultValue: Long,
    ): Long = preferences.getLong(key, defaultValue)

    override fun putLong(
        key: String,
        value: Long,
    ) = preferences.edit { putLong(key, value) }

    override fun getLongOrNull(key: String): Long? =
        if (preferences.contains(key)) {
            preferences.getLong(key, -1)
        } else {
            null
        }
}
