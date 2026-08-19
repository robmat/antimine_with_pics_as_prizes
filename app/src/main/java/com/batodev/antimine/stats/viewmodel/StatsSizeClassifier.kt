package com.batodev.antimine.stats.viewmodel

import dev.lucasnlm.antimine.common.level.database.models.Stats
import dev.lucasnlm.antimine.common.level.repository.MinefieldRepository
import dev.lucasnlm.antimine.core.models.Difficulty
import dev.lucasnlm.antimine.core.repository.DimensionRepository
import dev.lucasnlm.antimine.preferences.PreferencesRepository
import dev.lucasnlm.antimine.preferences.models.Minefield

/**
 * Classifies [Stats] rows by which named difficulty (or "fixed size" custom
 * board matching a standard difficulty's dimensions, possibly transposed)
 * they belong to - split out of [StatsViewModel] since these size-comparison
 * helpers were the bulk of its function count.
 */
class StatsSizeClassifier(
    private val minefieldRepository: MinefieldRepository,
    private val dimensionRepository: DimensionRepository,
    private val preferenceRepository: PreferencesRepository,
) {
    val legendSize = sizeOf(Difficulty.Legend)
    val masterSize = sizeOf(Difficulty.Master)
    val expertSize = sizeOf(Difficulty.Expert)
    val intermediateSize = sizeOf(Difficulty.Intermediate)
    val beginnerSize = sizeOf(Difficulty.Beginner)
    val standardSize = minefieldRepository.baseStandardSize(dimensionRepository, 0)

    private fun sizeOf(difficulty: Difficulty): Minefield {
        return minefieldRepository.fromDifficulty(
            difficulty,
            dimensionRepository,
            preferenceRepository,
        )
    }

    private fun Stats.isSizeOf(minefield: Minefield): Boolean {
        return this.mines == minefield.mines && this.width == minefield.width && this.height == minefield.height
    }

    fun isExpert(stats: Stats) = stats.isSizeOf(expertSize)

    fun isMaster(stats: Stats) = stats.isSizeOf(masterSize)

    fun isLegend(stats: Stats) = stats.isSizeOf(legendSize)

    fun isFixedSize(stats: Stats) = stats.isSizeOf(standardSize)

    fun isIntermediate(stats: Stats) = stats.isSizeOf(intermediateSize)

    fun isBeginner(stats: Stats) = stats.isSizeOf(beginnerSize)

    fun filterStandard(stats: List<Stats>) =
        stats.filter {
            val baseWidth = (it.width - standardSize.width)
            val baseHeight = (it.height - standardSize.height)
            val baseWidthInv = (it.height - standardSize.width)
            val baseHeightInv = (it.width - standardSize.height)

            val baseCheck = (baseWidth >= 0 && baseWidth % 2 == 0 && baseHeight >= 0 && baseHeight % 2 == 0)
            val baseInvCheck =
                (baseWidthInv >= 0 && baseWidthInv % 2 == 0 && baseHeightInv >= 0 && baseHeightInv % 2 == 0)

            (baseCheck || baseInvCheck) &&
                listOf(::isExpert, ::isMaster, ::isLegend, ::isIntermediate, ::isBeginner)
                    .any { func -> func.invoke(it) }
                    .not()
        }

    fun filterNotStandard(stats: Sequence<Stats>) =
        stats.filterNot {
            (it.width == standardSize.width && it.height == standardSize.height) ||
                (it.width == standardSize.height && it.height == standardSize.width)
        }
}
