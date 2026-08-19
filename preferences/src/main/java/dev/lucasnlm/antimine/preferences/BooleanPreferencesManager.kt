package dev.lucasnlm.antimine.preferences

import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * [PreferencesManager] used to declare all 14 of its functions directly,
 * over detekt's threshold. Its members are now spread across this and
 * several sibling by-type sub-interfaces (IntPreferencesManager,
 * LongPreferencesManager, StringPreferencesManager,
 * GeneralPreferencesManager), each implemented by its own class and
 * composed into [PreferencesManagerImpl] via interface delegation.
 */
interface BooleanPreferencesManager {
    fun getBoolean(
        key: String,
        defaultValue: Boolean,
    ): Boolean

    fun putBoolean(
        key: String,
        value: Boolean,
    )
}

internal class BooleanPreferencesManagerImpl(
    private val preferences: SharedPreferences,
) : BooleanPreferencesManager {
    override fun getBoolean(
        key: String,
        defaultValue: Boolean,
    ) = preferences.getBoolean(key, defaultValue)

    override fun putBoolean(
        key: String,
        value: Boolean,
    ) = preferences.edit { putBoolean(key, value) }
}
