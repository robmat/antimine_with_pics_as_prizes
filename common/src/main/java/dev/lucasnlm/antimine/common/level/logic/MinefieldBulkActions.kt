package dev.lucasnlm.antimine.common.level.logic

/**
 * Whole-field operations (reveal everything, flag every mine, clear mistakes)
 * used at game-over/setup time - split out of [MinefieldHandler] since that
 * class's function count was over threshold. Delegated into [MinefieldHandler]
 * via `by`.
 */
interface MinefieldBulkActions {
    fun showAllMines()

    fun showAllWrongFlags()

    fun flagAllMines()

    fun revealAllEmptyAreas()

    fun dismissMistake()

    fun revealRandomMineNearUncoveredArea(
        lastX: Int? = null,
        lastY: Int? = null,
    ): Int?
}
