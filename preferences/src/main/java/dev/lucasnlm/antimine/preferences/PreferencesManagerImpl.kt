package dev.lucasnlm.antimine.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

/**
 * See [PreferencesManager]'s doc - implementation is composed via interface
 * delegation to the per-type Impl classes instead of declaring all 14
 * overrides directly, which was over detekt's threshold.
 */
class PreferencesManagerImpl private constructor(
    preferences: SharedPreferences,
) : PreferencesManager,
    BooleanPreferencesManager by BooleanPreferencesManagerImpl(preferences),
    IntPreferencesManager by IntPreferencesManagerImpl(preferences),
    LongPreferencesManager by LongPreferencesManagerImpl(preferences),
    StringPreferencesManager by StringPreferencesManagerImpl(preferences),
    GeneralPreferencesManager by GeneralPreferencesManagerImpl(preferences) {
    constructor(context: Context) : this(PreferenceManager.getDefaultSharedPreferences(context))
}
