package com.batodev.antimine.custom.viewmodel

import dev.lucasnlm.antimine.preferences.models.Minefield

sealed class CustomEvent {
    data class UpdateCustomGameEvent(
        val minefield: Minefield,
    ) : CustomEvent()
}
