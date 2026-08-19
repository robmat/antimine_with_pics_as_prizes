package dev.lucasnlm.antimine.common.level.logic

import dev.lucasnlm.antimine.common.level.models.ActionCompleted
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Public click entry points, split out of [GameController] - see its class
 * doc.
 */
fun GameController.singleClick(index: Int): Flow<ActionCompleted> =
    flow {
        if (!creatingMinefield) {
            getArea(index)?.let { target ->
                val action =
                    if (target.isCovered) {
                        gameControl.onCovered.singleClick
                    } else {
                        gameControl.onUncovered.singleClick
                    }
                action?.let {
                    val initActions = actions
                    handleAction(target, action)
                    emit(
                        ActionCompleted(action, actions - initActions),
                    )
                }
            }
        }
    }

fun GameController.doubleClick(index: Int): Flow<ActionCompleted> =
    flow {
        if (!creatingMinefield) {
            getArea(index)?.let { target ->
                val action =
                    if (target.isCovered) {
                        gameControl.onCovered.doubleClick
                    } else {
                        gameControl.onUncovered.doubleClick
                    }
                action?.let {
                    val initActions = actions
                    handleAction(target, action)
                    emit(
                        ActionCompleted(action, actions - initActions),
                    )
                }
            }
        }
    }

fun GameController.longPress(index: Int): Flow<ActionCompleted> =
    flow {
        if (!creatingMinefield) {
            getArea(index)?.let { target ->
                if (target.isCovered || target.minesAround != 0) {
                    val action =
                        if (target.isCovered) {
                            gameControl.onCovered.longPress
                        } else {
                            gameControl.onUncovered.longPress
                        }
                    action?.let {
                        val initActions = actions
                        handleAction(target, action)
                        emit(
                            ActionCompleted(action, actions - initActions),
                        )
                    }
                }
            }
        }
    }
