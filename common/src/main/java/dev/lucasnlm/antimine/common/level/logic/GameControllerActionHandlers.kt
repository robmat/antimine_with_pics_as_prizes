package dev.lucasnlm.antimine.common.level.logic

import dev.lucasnlm.antimine.core.models.Area
import dev.lucasnlm.antimine.core.models.Mark
import dev.lucasnlm.antimine.preferences.models.Action

/**
 * Per-action handling once mines are already planted, split out of
 * [GameController] - see its class doc.
 */
private fun GameController.openOrRemoveMark(
    minefieldHandler: MinefieldHandler,
    target: Area,
) {
    if (target.mark.isNotNone()) {
        minefieldHandler.removeMarkAt(target.id)
    } else {
        minefieldHandler.openAt(target.id, false)
    }
}

private fun GameController.handleOpenTileAction(
    minefieldHandler: MinefieldHandler,
    target: Area,
) {
    if (target.mark.isNotNone()) {
        minefieldHandler.removeMarkAt(target.id)
    } else {
        this.actions++
        minefieldHandler.openAt(target.id, false)
    }
}

private fun GameController.handleSwitchMarkAction(
    minefieldHandler: MinefieldHandler,
    target: Area,
) {
    if (!hasMines()) {
        openOrRemoveMark(minefieldHandler, target)
    } else {
        minefieldHandler.switchMarkAt(target.id)
    }
}

private fun GameController.handleOpenNeighborsAction(
    minefieldHandler: MinefieldHandler,
    target: Area,
) {
    if (useClickOnNumbers) {
        this.actions++
        if (letNumbersPutFlag) {
            minefieldHandler.openOrFlagNeighborsOf(target.id)
        } else {
            minefieldHandler.openNeighborsOf(target.id)
        }
    }
}

private fun GameController.handleSelectedSubAction(
    minefieldHandler: MinefieldHandler,
    target: Area,
) {
    when (selectedAction) {
        Action.OpenTile -> {
            if (target.mark.isNone()) {
                minefieldHandler.openAt(target.id, false)
            } else {
                minefieldHandler.removeMarkAt(target.id)
            }
        }
        Action.SwitchMark -> {
            minefieldHandler.switchMarkAt(target.id)
        }
        Action.QuestionMark -> {
            minefieldHandler.toggleMarkAt(target.id, Mark.Question)
        }
        else -> {
            // Unexpected Action. Ignore.
        }
    }
}

private fun GameController.handleOpenOrMarkAction(
    minefieldHandler: MinefieldHandler,
    target: Area,
) {
    if (!hasMines()) {
        openOrRemoveMark(minefieldHandler, target)
    } else {
        this.actions++
        handleSelectedSubAction(minefieldHandler, target)
    }
}

internal fun GameController.handleSubsequentAction(
    target: Area,
    action: Action?,
): MinefieldHandler {
    val minefieldHandler = newMinefieldHandler()

    when (action) {
        Action.OpenTile -> handleOpenTileAction(minefieldHandler, target)
        Action.SwitchMark -> handleSwitchMarkAction(minefieldHandler, target)
        Action.OpenNeighbors -> handleOpenNeighborsAction(minefieldHandler, target)
        Action.OpenOrMark -> handleOpenOrMarkAction(minefieldHandler, target)
        else -> {}
    }

    return minefieldHandler
}

internal suspend fun GameController.handleAction(
    target: Area,
    action: Action?,
) {
    if (creatingMinefield) {
        // Ignore because the game is not ready for any action.
        return
    }

    val minefieldHandler =
        if (!hasMines()) {
            handleFirstAction(target)
        } else {
            handleSubsequentAction(target, action)
        }

    lastIdInteractionX = target.posX
    lastIdInteractionY = target.posY

    minefieldHandler?.let {
        field = it.result()
    }
}
