package dev.lucasnlm.antimine.common.level.viewmodel

import dev.lucasnlm.antimine.common.level.repository.MinefieldRepository
import dev.lucasnlm.antimine.common.level.repository.SavesRepository
import dev.lucasnlm.antimine.common.level.repository.StatsRepository
import dev.lucasnlm.antimine.common.level.repository.TipRepository
import dev.lucasnlm.antimine.common.level.utils.Clock
import dev.lucasnlm.antimine.core.audio.GameAudioManager
import dev.lucasnlm.antimine.core.haptic.HapticFeedbackManager
import dev.lucasnlm.antimine.core.repository.DimensionRepository
import dev.lucasnlm.antimine.preferences.PreferencesRepository
import dev.lucasnlm.external.AnalyticsManager
import dev.lucasnlm.external.FeatureFlagManager
import dev.lucasnlm.external.PlayGamesManager

/**
 * [GameViewModel]'s dependencies grouped into cohesive bundles, since its own
 * constructor had too many parameters as a flat list.
 */
class GameDataDependencies(
    val savesRepository: SavesRepository,
    val statsRepository: StatsRepository,
    val minefieldRepository: MinefieldRepository,
    val tipRepository: TipRepository,
)

class GameEnvironmentDependencies(
    val dimensionRepository: DimensionRepository,
    val preferencesRepository: PreferencesRepository,
    val featureFlagManager: FeatureFlagManager,
    val clock: Clock,
)

class GameFeedbackDependencies(
    val hapticFeedbackManager: HapticFeedbackManager,
    val soundManager: GameAudioManager,
    val analyticsManager: AnalyticsManager,
    val playGamesManager: PlayGamesManager,
)
