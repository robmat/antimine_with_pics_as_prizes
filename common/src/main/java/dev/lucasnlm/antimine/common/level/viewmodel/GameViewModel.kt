package dev.lucasnlm.antimine.common.level.viewmodel

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import dev.lucasnlm.antimine.common.level.logic.GameController
import dev.lucasnlm.antimine.common.level.logic.doubleClick
import dev.lucasnlm.antimine.common.level.logic.longPress
import dev.lucasnlm.antimine.common.level.logic.singleClick
import dev.lucasnlm.antimine.core.models.Difficulty
import dev.lucasnlm.antimine.core.viewmodel.IntentViewModel
import dev.lucasnlm.antimine.preferences.models.Minefield
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow

/**
 * The bulk of this class's original behavior - event handling, save/load,
 * click dispatch, achievements, victory/game-over side effects, control
 * description text - was split out into extension functions in sibling files
 * (GameViewModelEvents.kt, GameViewModelCompletion.kt, GameViewModelLifecycle.kt,
 * GameViewModelPersistence.kt, GameViewModelInput.kt, GameViewModelClock.kt,
 * GameViewModelOutcomes.kt, GameViewModelAchievements.kt,
 * GameViewModelDescription.kt), since this class's function count and
 * constructor parameter count were over threshold. Only functions that must be
 * actual class members - overrides, `open` functions meant to be overridden by
 * callers, and a couple of passthroughs exposing otherwise-protected base
 * class members to those extension functions - remain here. Several fields
 * are `internal` rather than `private` only because those extension functions,
 * living outside the class body, need access to them.
 */
open class GameViewModel(
    dataDependencies: GameDataDependencies,
    environmentDependencies: GameEnvironmentDependencies,
    feedbackDependencies: GameFeedbackDependencies,
) : IntentViewModel<GameEvent, GameState>() {
    internal val savesRepository = dataDependencies.savesRepository
    internal val statsRepository = dataDependencies.statsRepository
    internal val minefieldRepository = dataDependencies.minefieldRepository
    internal val tipRepository = dataDependencies.tipRepository

    internal val dimensionRepository = environmentDependencies.dimensionRepository
    internal val preferencesRepository = environmentDependencies.preferencesRepository
    internal val featureFlagManager = environmentDependencies.featureFlagManager
    internal val clock = environmentDependencies.clock

    internal val hapticFeedbackManager = feedbackDependencies.hapticFeedbackManager
    internal val soundManager = feedbackDependencies.soundManager
    internal val analyticsManager = feedbackDependencies.analyticsManager
    internal val playGamesManager = feedbackDependencies.playGamesManager

    var prizeImage: String = ""
    lateinit var context: Context
    internal lateinit var gameController: GameController
    internal var initialized = false

    internal val gameState: GameState get() = state

    internal fun postSideEffect(event: GameEvent) = sendSideEffect(event)

    override fun initialState(): GameState {
        return GameState(
            turn = 0,
            field = listOf(),
            duration = 0L,
            difficulty = Difficulty.Beginner,
            mineCount = null,
            minefield = Minefield(DEFAULT_MINEFIELD_SIZE, DEFAULT_MINEFIELD_SIZE, DEFAULT_MINEFIELD_SIZE),
            seed = 0L,
            hints = tipRepository.getTotalTips(),
            isGameCompleted = false,
            isActive = false,
            hasMines = false,
            isCreatingGame = false,
            useHelp = preferencesRepository.useHelp(),
            isLoadingMap = true,
            showTutorial = preferencesRepository.showTutorialButton(),
        )
    }

    override suspend fun mapEventToState(event: GameEvent): Flow<GameState> =
        flow {
            when (event) {
                is GameEvent.CreatingGameEvent -> emit(state.copy(isCreatingGame = true))
                is GameEvent.SetGameActivation -> emit(state.copy(isActive = event.active))
                is GameEvent.ShowNewGameDialog -> sendSideEffect(GameEvent.ShowNewGameDialog)
                is GameEvent.GiveMoreTip -> onGiveMoreTip(this)
                is GameEvent.ConsumeTip -> onConsumeTip(this)
                is GameEvent.UpdateSave -> emit(state.copy(saveId = event.saveId))
                is GameEvent.NewGame -> emit(event.newState)
                is GameEvent.ContinueGame -> onContinueGame(this)
                is GameEvent.EngineReady -> onEngineReady(this)
                is GameEvent.LoadingNewGame -> onLoadingNewGame(this)
                is GameEvent.UpdateTime -> emit(state.copy(duration = event.time))
                is GameEvent.UpdateMinefield -> onUpdateMinefield(this, event)
                else -> {
                    // Empty
                }
            }
        }

    open suspend fun onLongClick(index: Int) {
        if (!gameController.hasMines()) {
            sendEvent(GameEvent.CreatingGameEvent)
        }

        gameController
            .longPress(index)
            .filterNotNull()
            .collect { actionCompleted ->
                onFeedbackAnalytics(actionCompleted.action, index)
                onPostAction()
                playActionSound(actionCompleted)
                refreshField()

                if (preferencesRepository.useHapticFeedback()) {
                    hapticFeedbackManager.longPressFeedback()
                }
            }
    }

    open suspend fun onDoubleClick(index: Int) {
        if (!gameController.hasMines()) {
            sendEvent(GameEvent.CreatingGameEvent)
        }

        gameController
            .doubleClick(index)
            .filterNotNull()
            .collect { actionCompleted ->
                onFeedbackAnalytics(actionCompleted.action, index)
                onPostAction()
                playActionSound(actionCompleted)
                refreshField()
            }
    }

    open suspend fun onSingleClick(index: Int) {
        if (!gameController.hasMines()) {
            sendEvent(GameEvent.CreatingGameEvent)
        }

        gameController
            .singleClick(index)
            .filterNotNull()
            .collect { actionCompleted ->
                onFeedbackAnalytics(actionCompleted.action, index)
                onPostAction()
                playActionSound(actionCompleted)
                refreshField()
            }
    }

    companion object {
        const val EXPLOSION_DELAY = 400L
        const val THIRTY_SECONDS_ACHIEVEMENT = 30L
        const val MIN_REWARD_GAME_SECONDS = 10L
        const val REWARD_RATIO_WITH_MISTAKES = 0.025
        const val REWARD_RATIO_WITHOUT_MISTAKES = 0.05
        const val MIN_ACTION_TO_REWARD = 7
        const val MIN_ACTION_TO_NO_LUCK = 3
        const val DEFAULT_MINEFIELD_SIZE = 9
        const val TIP_INCREASE_AMOUNT = 5
    }
}

/**
 * Resolves the `difficulty` query parameter from [intent] (used by deep links) and, if
 * present, starts the matching game. Returns true when the intent was handled this way.
 */
suspend fun GameViewModel.startGameFromDifficultyQueryParam(
    activity: AppCompatActivity,
    intent: Intent,
): Boolean {
    val queryParamDifficulty = intent.data?.getQueryParameter("difficulty") ?: return false
    val upperDifficulty = queryParamDifficulty.uppercase()
    val difficulty = Difficulty.values().firstOrNull { it.id == upperDifficulty }
    if (difficulty == null) {
        loadLastGame(activity)
    } else {
        startNewGame(activity, difficulty)
    }
    return true
}
