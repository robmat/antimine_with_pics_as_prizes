package dev.lucasnlm.antimine.gdx.actors

import dev.lucasnlm.antimine.gdx.AtlasNames

/**
 * [Int.toAtlasNames] split into one function per group of atlas pieces,
 * since combining every entry into a single expression drove its cyclomatic
 * complexity over threshold.
 */
private fun Int.edgeEntries(): Map<String, Boolean> =
    mapOf(
        AtlasNames.CORE to true,
        AtlasNames.TOP to top(),
        AtlasNames.LEFT to left(),
        AtlasNames.BOTTOM to bottom(),
        AtlasNames.RIGHT to right(),
    )

private fun Int.cornerEntries(): Map<String, Boolean> =
    mapOf(
        AtlasNames.CORNER_TOP_LEFT to (!top() && !left()),
        AtlasNames.CORNER_TOP_RIGHT to (!top() && !right()),
        AtlasNames.CORNER_BOTTOM_LEFT to (!bottom() && !left()),
        AtlasNames.CORNER_BOTTOM_RIGHT to (!bottom() && !right()),
    )

private fun Int.borderCornerEntries(): Map<String, Boolean> =
    mapOf(
        AtlasNames.BORDER_CORNER_RIGHT to (top() && right() && !topRight()),
        AtlasNames.BORDER_CORNER_LEFT to (top() && left() && !topLeft()),
        AtlasNames.BORDER_CORNER_BOTTOM_RIGHT to (bottom() && right() && !bottomRight()),
        AtlasNames.BORDER_CORNER_BOTTOM_LEFT to (bottom() && left() && !bottomLeft()),
    )

private fun Int.fillEntries(): Map<String, Boolean> =
    mapOf(
        AtlasNames.FILL_TOP_LEFT to (top() && left() && topLeft()),
        AtlasNames.FILL_TOP_RIGHT to (top() && right() && topRight()),
        AtlasNames.FILL_BOTTOM_LEFT to (bottom() && left() && bottomLeft()),
        AtlasNames.FILL_BOTTOM_RIGHT to (bottom() && right() && bottomRight()),
    )

fun Int.toAtlasNames(): Map<String, Boolean> = edgeEntries() + cornerEntries() + borderCornerEntries() + fillEntries()
