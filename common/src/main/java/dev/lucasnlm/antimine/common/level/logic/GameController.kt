package dev.lucasnlm.antimine.common.level.logic

import dev.lucasnlm.antimine.common.level.database.models.FirstOpen
import dev.lucasnlm.antimine.common.level.database.models.Save
import dev.lucasnlm.antimine.core.models.Area
import dev.lucasnlm.antimine.preferences.models.Action
import dev.lucasnlm.antimine.preferences.models.GameControl
import dev.lucasnlm.antimine.preferences.models.Minefield

/**
 * Core game session state and the most fundamental field accessors.
 * The rest of the class's original behavior (click/action handling, bulk field
 * operations, stats/queries, save/settings) was split out into extension
 * functions in sibling files - [GameControllerActions], [GameControllerFieldOps],
 * [GameControllerStats] and [GameControllerSettings] - since this class's
 * function count was over threshold. Several fields are `internal` rather than
 * `private` only because those extension functions, living outside the class
 * body, need access to them.
 */
class GameController {
    internal val minefield: Minefield
    internal val startTime = System.currentTimeMillis()
    internal var saveId = 0
    internal var actions = 0
    var prizeImage = ""
    internal var firstOpen: FirstOpen = FirstOpen.Unknown
    internal var gameControl: GameControl = GameControl.Standard
    internal var useQuestionMark = true
    internal var selectedAction = Action.SwitchMark
    internal var useClickOnNumbers = true
    internal var letNumbersPutFlag = true
    internal var errorTolerance = 0
    private var useSimonTatham = true
    internal var creatingMinefield = false

    internal var lastIdInteractionX: Int? = null
    internal var lastIdInteractionY: Int? = null

    val seed: Long

    internal val minefieldCreator: MinefieldCreator
    internal val fallbackCreator: MinefieldCreator
    internal var field: List<Area>
    internal var noGuessTestedLevel = true
    internal var onCreateUnsafeLevel: (() -> Unit)? = null

    constructor(
        minefield: Minefield,
        seed: Long,
        useSimonTatham: Boolean,
        saveId: Int? = null,
        onCreateUnsafeLevel: (() -> Unit)? = null,
        prizeImage: String,
    ) {
        val creationSeed = minefield.seed ?: seed
        val shouldUseSimonTatham = useSimonTatham
        this.fallbackCreator = MinefieldCreatorImpl(minefield, creationSeed)
        this.minefieldCreator =
            if (shouldUseSimonTatham) {
                MinefieldCreatorNativeImpl(minefield, creationSeed)
            } else {
                fallbackCreator
            }
        this.minefield = minefield
        this.seed = seed
        this.saveId = saveId ?: 0
        this.actions = 0
        this.onCreateUnsafeLevel = onCreateUnsafeLevel
        this.field = minefieldCreator.createEmpty()
        this.useSimonTatham = shouldUseSimonTatham
        this.prizeImage = prizeImage
    }

    constructor(
        save: Save,
        useSimonTatham: Boolean,
    ) {
        this.minefield = save.minefield
        this.seed = save.seed
        this.saveId = save.uid
        this.firstOpen = save.firstOpen
        this.field = save.field
        this.actions = save.actions
        this.fallbackCreator = MinefieldCreatorImpl(minefield, seed)
        this.minefieldCreator =
            if (useSimonTatham) {
                MinefieldCreatorNativeImpl(minefield, seed)
            } else {
                fallbackCreator
            }
    }

    fun field() = field

    fun field(predicate: (Area) -> Boolean) = field.filter(predicate)

    fun mines() = field.filter { it.hasMine }

    fun hasMines() = field.firstOrNull { it.hasMine } != null

    internal fun useIndividualActions(): Boolean = gameControl == GameControl.SwitchMarkOpen

    internal fun getArea(id: Int) = field.firstOrNull { it.id == id }

    internal companion object {
        const val MAX_CREATION_TIME_MS = 30000L
    }
}
