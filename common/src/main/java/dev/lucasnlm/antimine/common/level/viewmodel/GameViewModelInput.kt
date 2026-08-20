package dev.lucasnlm.antimine.common.level.viewmodel

import dev.lucasnlm.antimine.common.level.logic.changeSwitchControlAction
import dev.lucasnlm.antimine.common.level.logic.isGameOver
import dev.lucasnlm.antimine.common.level.logic.isVictory
import dev.lucasnlm.antimine.common.level.logic.letNumbersPutFlag
import dev.lucasnlm.antimine.common.level.logic.runFlagAssistant
import dev.lucasnlm.antimine.common.level.logic.runNumberDimmer
import dev.lucasnlm.antimine.common.level.logic.updateGameControl
import dev.lucasnlm.antimine.common.level.logic.useClickOnNumbers
import dev.lucasnlm.antimine.common.level.logic.useQuestionMark
import dev.lucasnlm.antimine.common.level.models.ActionCompleted
import dev.lucasnlm.antimine.core.models.Analytics
import dev.lucasnlm.antimine.preferences.models.Action
import dev.lucasnlm.antimine.preferences.models.GameControl

/**
 * Click-driven sound/analytics/state feedback and control preferences, split
 * out of [GameViewModel] - see its class doc.
 */
internal fun GameViewModel.playActionSound(actionCompleted: ActionCompleted) {
    when (actionCompleted.action) {
        Action.OpenTile -> {
            soundManager.playOpenArea()
        }

        Action.OpenOrMark -> {
            if (preferencesRepository.getSwitchControlAction() == Action.OpenTile) {
                soundManager.playOpenArea()
            } else {
                soundManager.playPutFlag()
            }
        }

        Action.SwitchMark, Action.QuestionMark -> {
            soundManager.playPutFlag()
        }

        Action.OpenNeighbors -> {
            soundManager.playOpenMultipleArea()
        }

        else -> {
            // No sound
        }
    }
}

internal fun GameViewModel.onPostAction() {
    if (preferencesRepository.useFlagAssistant() && !gameController.isGameOver()) {
        gameController.runFlagAssistant()
    }

    if (preferencesRepository.dimNumbers() && !gameController.isGameOver()) {
        gameController.runNumberDimmer()
    }

    updateGameState()
}

internal fun GameViewModel.onFeedbackAnalytics(
    action: Action,
    index: Int,
) {
    if (featureFlagManager.isGameplayAnalyticsEnabled) {
        when (action) {
            Action.OpenTile -> {
                analyticsManager.sentEvent(Analytics.OpenTile(index))
            }

            Action.SwitchMark -> {
                analyticsManager.sentEvent(Analytics.SwitchMark(index))
            }

            Action.OpenNeighbors -> {
                analyticsManager.sentEvent(Analytics.OpenNeighbors(index))
            }

            Action.OpenOrMark -> {
                analyticsManager.sentEvent(Analytics.OpenOrFlagTile(index))
            }

            Action.QuestionMark -> {
                analyticsManager.sentEvent(Analytics.QuestionMark(index))
            }
        }
    }
}

internal fun GameViewModel.updateGameState() {
    when {
        gameController.isGameOver() -> {
            sendEvent(GameEvent.SetGameActivation(false))
        }

        else -> {
            sendEvent(GameEvent.SetGameActivation(true))
        }
    }

    if (gameController.isVictory()) {
        sendEvent(GameEvent.SetGameActivation(false))
    }
}

internal fun GameViewModel.refreshUserPreferences() {
    if (initialized) {
        gameController.apply {
            val controlType = preferencesRepository.controlStyle()
            val gameControl = GameControl.fromControlType(controlType)

            updateGameControl(gameControl)
            useQuestionMark(preferencesRepository.useQuestionMark())
            useClickOnNumbers(preferencesRepository.allowTapOnNumbers())
            letNumbersPutFlag(preferencesRepository.letNumbersAutoFlag())
        }
    }
}

fun GameViewModel.changeSwitchControlAction(action: Action) {
    if (initialized) {
        preferencesRepository.setSwitchControl(action)
        gameController.changeSwitchControlAction(action)
    }
}
