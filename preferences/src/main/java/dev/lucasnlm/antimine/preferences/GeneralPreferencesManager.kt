package dev.lucasnlm.antimine.preferences

import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * Split out of [PreferencesManager] - see [BooleanPreferencesManager]'s doc.
 */
interface GeneralPreferencesManager {
    fun removeKey(key: String)

    fun clear()

    fun contains(key: String): Boolean

    fun toMap(): Map<String, Any?>
}

internal class GeneralPreferencesManagerImpl(
    private val preferences: SharedPreferences,
) : GeneralPreferencesManager {
    override fun contains(key: String): Boolean = preferences.contains(key)

    override fun removeKey(key: String) = preferences.edit { remove(key) }

    override fun clear() = preferences.edit { clear() }

    override fun toMap(): Map<String, Any?> = preferences.all.toMap()
}
