package com.batodev.antimine.stats.viewmodel

import com.batodev.antimine.stats.model.StatsModel
import com.batodev.antimine.stats.model.StatsState
import dev.lucasnlm.antimine.common.level.database.models.Stats
import dev.lucasnlm.antimine.common.level.repository.MinefieldRepository
import dev.lucasnlm.antimine.common.level.repository.StatsRepository
import dev.lucasnlm.antimine.core.repository.DimensionRepository
import dev.lucasnlm.antimine.core.viewmodel.IntentViewModel
import dev.lucasnlm.antimine.preferences.PreferencesRepository
import kotlinx.coroutines.flow.flow
import dev.lucasnlm.antimine.i18n.R as i18n

class StatsViewModel(
    private val statsRepository: StatsRepository,
    private val preferenceRepository: PreferencesRepository,
    minefieldRepository: MinefieldRepository,
    dimensionRepository: DimensionRepository,
) : IntentViewModel<StatsEvent, StatsState>() {
    private val classifier = StatsSizeClassifier(minefieldRepository, dimensionRepository, preferenceRepository)

    private suspend fun loadStatsModel(): List<StatsModel> {
        val minId = preferenceRepository.getStatsBase()
        val stats = statsRepository.getAllStats(minId)

        return with(stats) {
            listOf(
                // General
                fold().copy(title = i18n.string.general),
                // Progressive
                classifier.filterStandard(this).fold().copy(title = i18n.string.progressive),
                // Fixed Size
                filter(classifier::isFixedSize).fold().copy(title = i18n.string.fixed_size),
                // Legend
                filter(classifier::isLegend).fold().copy(title = i18n.string.legend),
                // Master
                filter(classifier::isMaster).fold().copy(title = i18n.string.master),
                // Expert
                filter(classifier::isExpert).fold().copy(title = i18n.string.expert),
                // Intermediate
                filter(classifier::isIntermediate).fold().copy(title = i18n.string.intermediate),
                // Beginner
                filter(classifier::isBeginner).fold().copy(title = i18n.string.beginner),
                // Custom
                classifier
                    .filterNotStandard(
                        asSequence()
                            .filterNot(classifier::isExpert)
                            .filterNot(classifier::isIntermediate)
                            .filterNot(classifier::isBeginner)
                            .filterNot(classifier::isMaster)
                            .filterNot(classifier::isLegend)
                            .filterNot(classifier::isFixedSize),
                    ).toList()
                    .fold()
                    .copy(title = i18n.string.custom),
            ).filter {
                it.totalGames > 0
            }
        }
    }

    private suspend fun deleteAll() {
        statsRepository.getAllStats(0).lastOrNull()?.let {
            preferenceRepository.updateStatsBase(it.uid + 1)
        }
    }

    private fun accumulateStats(
        acc: StatsModel,
        value: Stats,
    ): StatsModel {
        val victoryTime =
            acc.victoryTime +
                if (value.victory != 0) {
                    value.duration
                } else {
                    0
                }

        val shortestTime =
            if (value.victory != 0) {
                if (acc.shortestTime == 0L) {
                    value.duration
                } else {
                    acc.shortestTime.coerceAtMost(value.duration)
                }
            } else {
                acc.shortestTime
            }

        return StatsModel(
            title = 0,
            totalGames = acc.totalGames,
            totalTime = acc.totalTime + value.duration,
            victoryTime = victoryTime,
            averageTime = 0,
            shortestTime = shortestTime,
            mines = acc.mines + value.mines,
            victory = acc.victory + value.victory,
            openArea = acc.openArea + value.openArea,
        )
    }

    private fun emptyStatsModel(totalGames: Int = 0): StatsModel =
        StatsModel(
            title = 0,
            totalGames = totalGames,
            totalTime = 0,
            victoryTime = 0,
            averageTime = 0,
            shortestTime = 0,
            mines = 0,
            victory = 0,
            openArea = 0,
        )

    private fun List<Stats>.fold(): StatsModel {
        if (isEmpty()) {
            return emptyStatsModel()
        }

        return fold(emptyStatsModel(totalGames = size), ::accumulateStats)
            .run {
                if (victory > 0) {
                    copy(averageTime = victoryTime / victory)
                } else {
                    this
                }
            }
    }

    override fun initialState() =
        StatsState(
            stats = listOf(),
        )

    override suspend fun mapEventToState(event: StatsEvent) =
        flow {
            when (event) {
                is StatsEvent.LoadStats -> {
                    emit(state.copy(stats = loadStatsModel()))
                }

                is StatsEvent.DeleteStats -> {
                    deleteAll()
                    emit(state.copy(stats = loadStatsModel()))
                }
            }
        }
}
