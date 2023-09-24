package com.batodev.antimine.stats.viewmodel

sealed class StatsEvent {
    data object LoadStats : StatsEvent()
    data object DeleteStats : StatsEvent()
}
