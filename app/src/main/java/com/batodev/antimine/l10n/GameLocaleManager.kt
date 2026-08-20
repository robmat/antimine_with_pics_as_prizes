package com.batodev.antimine.l10n

import java.util.Locale

interface GameLocaleManager {
    fun getAllGameLocaleTags(): List<String>

    fun setGameLocale(tag: String)

    fun getGameLocale(): Locale?

    fun applyPreferredLocaleIfNeeded()
}
