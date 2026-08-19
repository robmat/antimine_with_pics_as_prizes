package dev.lucasnlm.antimine.common.level.logic

/**
 * Whole-field mutating operations, split out of [GameController] - see its
 * class doc.
 */
fun GameController.runFlagAssistant() {
    field =
        FlagAssistant(field.toMutableList()).run {
            runFlagAssistant()
            result()
        }
}

fun GameController.runNumberDimmer() {
    field =
        NumberDimmer(field.toMutableList()).run {
            runDimmer()
            result()
        }
}

fun GameController.runNumberDimmerToAllMines() {
    field =
        NumberDimmer(field.toMutableList()).run {
            runDimmerAll()
            result()
        }
}

fun GameController.showAllMistakes() {
    field =
        MinefieldHandler(
            field = field.toMutableList(),
            useQuestionMark = false,
            individualActions = useIndividualActions(),
        ).run {
            showAllMines()
            showAllWrongFlags()
            result()
        }
}

fun GameController.flagAllMines() {
    field =
        MinefieldHandler(
            field = field.toMutableList(),
            useQuestionMark = false,
            individualActions = useIndividualActions(),
        ).run {
            flagAllMines()
            result()
        }
}

fun GameController.showWrongFlags() {
    field =
        field.map {
            if (!it.hasMine && it.mark.isFlag()) {
                it.copy(mistake = true)
            } else {
                it
            }
        }
}

fun GameController.revealAllEmptyAreas() {
    field =
        MinefieldHandler(
            field = field.toMutableList(),
            useQuestionMark = false,
            individualActions = useIndividualActions(),
        ).run {
            revealAllEmptyAreas()
            result()
        }
}

fun GameController.revealRandomMine(): Int? {
    val resultId: Int?
    field =
        MinefieldHandler(
            field = field.toMutableList(),
            useQuestionMark = false,
            individualActions = useIndividualActions(),
        ).run {
            resultId = revealRandomMineNearUncoveredArea(lastIdInteractionX, lastIdInteractionY)
            result()
        }
    return resultId
}

fun GameController.dismissMistake() {
    val minefieldHandler =
        MinefieldHandler(
            field = field.toMutableList(),
            useQuestionMark = useQuestionMark,
            individualActions = useIndividualActions(),
        )
    minefieldHandler.dismissMistake()
    field = minefieldHandler.result()
}
