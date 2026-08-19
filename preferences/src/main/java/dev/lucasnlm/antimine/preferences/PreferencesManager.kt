package dev.lucasnlm.antimine.preferences

/**
 * See [BooleanPreferencesManager]'s doc for why this interface no longer
 * declares its 14 functions directly - it now inherits them from
 * per-type sub-interfaces so existing callers keep seeing a single unified
 * contract.
 */
interface PreferencesManager :
    BooleanPreferencesManager,
    IntPreferencesManager,
    LongPreferencesManager,
    StringPreferencesManager,
    GeneralPreferencesManager
