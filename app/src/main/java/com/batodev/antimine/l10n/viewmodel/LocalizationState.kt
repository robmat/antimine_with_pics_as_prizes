package com.batodev.antimine.l10n.viewmodel

import com.batodev.antimine.l10n.models.GameLanguage

data class LocalizationState(
    val loading: Boolean,
    val languages: List<GameLanguage>,
)
