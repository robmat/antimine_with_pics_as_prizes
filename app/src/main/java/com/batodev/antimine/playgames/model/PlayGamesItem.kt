package com.batodev.antimine.playgames.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.batodev.antimine.playgames.viewmodel.PlayGamesEvent

data class PlayGamesItem(
    val id: Int,
    @DrawableRes val iconRes: Int,
    @StringRes val stringRes: Int,
    val triggerEvent: PlayGamesEvent,
)
