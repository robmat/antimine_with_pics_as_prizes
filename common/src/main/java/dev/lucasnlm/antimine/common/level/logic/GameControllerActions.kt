package dev.lucasnlm.antimine.common.level.logic

import dev.lucasnlm.antimine.common.level.database.models.FirstOpen
import dev.lucasnlm.antimine.common.level.solver.LimitedCheckNeighborsSolver
import dev.lucasnlm.antimine.core.models.Area
import kotlinx.coroutines.withTimeout

/**
 * Minefield creation on first action, split out of [GameController] - see its
 * class doc.
 */
internal fun GameController.createAndOpen(
    newField: List<Area>,
    safeId: Int,
): MinefieldHandler {
    field = newField
    val fieldCopy = field.map { it.copy() }.toMutableList()
    return MinefieldHandler(
        field = fieldCopy,
        useQuestionMark = false,
        individualActions = useIndividualActions(),
    ).apply {
        openAt(safeId, false)
    }
}

internal suspend fun GameController.plantMinesExcept(safeId: Int): Boolean {
    if (!creatingMinefield) {
        val solver = LimitedCheckNeighborsSolver()
        creatingMinefield = true

        runCatching {
            // Try using native implementation first.
            // If it fails, use fallback random generator.
            withTimeout(GameController.MAX_CREATION_TIME_MS) {
                createAndOpen(minefieldCreator.create(safeId), safeId)
            }
        }.onFailure {
            do {
                var solvable: Boolean
                val minefieldHandler = createAndOpen(fallbackCreator.create(safeId), safeId)
                solvable = solver.trySolve(minefieldHandler.result().toMutableList())
                noGuessTestedLevel = solvable
            } while (solver.keepTrying() && !noGuessTestedLevel)
        }

        firstOpen = FirstOpen.Position(safeId)
        creatingMinefield = false
        return true
    } else {
        return false
    }
}

internal fun GameController.newMinefieldHandler(): MinefieldHandler =
    MinefieldHandler(
        field = field.toMutableList(),
        useQuestionMark = useQuestionMark,
        individualActions = useIndividualActions(),
    )

internal suspend fun GameController.handleFirstAction(target: Area): MinefieldHandler? {
    val created = plantMinesExcept(target.id)
    if (!created) {
        return null
    }

    val minefieldHandler = newMinefieldHandler()
    minefieldHandler.openAt(target.id, false)

    if (!noGuessTestedLevel) {
        onCreateUnsafeLevel?.invoke()
    }
    return minefieldHandler
}
