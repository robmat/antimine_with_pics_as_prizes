package com.batodev.antimine.playgames.viewmodel

sealed class PlayGamesEvent {
    data object OpenAchievements : PlayGamesEvent()
    data object OpenLeaderboards : PlayGamesEvent()
}
