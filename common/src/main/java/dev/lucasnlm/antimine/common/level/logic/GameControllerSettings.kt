package dev.lucasnlm.antimine.common.level.logic

import dev.lucasnlm.antimine.preferences.models.Action
import dev.lucasnlm.antimine.preferences.models.GameControl

/**
 * Gameplay-preference setters, split out of [GameController] - see its class
 * doc.
 */
fun GameController.updateGameControl(newGameControl: GameControl) {
    this.gameControl = newGameControl
}

fun GameController.useQuestionMark(useQuestionMark: Boolean) {
    this.useQuestionMark = useQuestionMark
}

fun GameController.useClickOnNumbers(clickNumbers: Boolean) {
    this.useClickOnNumbers = clickNumbers
}

fun GameController.changeSwitchControlAction(action: Action) {
    this.selectedAction = action
}

fun GameController.letNumbersPutFlag(enabled: Boolean) {
    this.letNumbersPutFlag = enabled
}

fun GameController.increaseErrorToleranceByWrongMines() {
    val value = mines().count { !it.isCovered }
    increaseErrorTolerance(value)
}

fun GameController.increaseErrorTolerance(value: Int = 1) {
    errorTolerance += value
}

fun GameController.getErrorTolerance(): Int = errorTolerance

fun GameController.hadMistakes(): Boolean {
    return errorTolerance != 0
}
