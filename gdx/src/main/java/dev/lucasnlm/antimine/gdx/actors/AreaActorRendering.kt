package dev.lucasnlm.antimine.gdx.actors

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.Batch
import dev.lucasnlm.antimine.gdx.AtlasNames
import dev.lucasnlm.antimine.gdx.DrawBounds
import dev.lucasnlm.antimine.gdx.GameContext
import dev.lucasnlm.antimine.gdx.alpha
import dev.lucasnlm.antimine.gdx.drawAsset
import dev.lucasnlm.antimine.gdx.drawRegion
import dev.lucasnlm.antimine.gdx.models.GameTextures

/**
 * [AreaActor]'s covered-tile drawing logic, split out of the class body - see
 * its class doc. The pressed/uncovered counterpart lives in
 * AreaActorPressedRendering.kt.
 */
internal fun AreaActor.drawBackground(
    batch: Batch,
    isOdd: Boolean,
) {
    if (!isOdd && !area.isCovered && GameContext.zoomLevelAlpha > 0.0f) {
        GameContext.gameTextures?.areaBackground?.let {
            batch.drawRegion(
                texture = it,
                bounds = DrawBounds(x = x, y = y, width = width, height = height),
                blend = false,
                color = GameContext.backgroundColor,
            )
        }
    }
}

private fun AreaActor.drawCoveredPieces(
    batch: Batch,
    coverColor: Color,
) {
    pieces.forEach { piece ->
        if (piece.value) {
            batch.drawRegion(
                texture = GameContext.gameTextures!!.pieces[piece.key]!!,
                bounds = DrawBounds(x = x - 0.5f, y = y - 0.5f, width = width + 0.5f, height = height + 0.5f),
                color = coverColor,
                blend = false,
            )
        }
    }
}

internal fun AreaActor.drawCovered(batch: Batch) {
    val coverColor =
        when {
            !GameContext.canTintAreas -> GameContext.whiteColor
            area.mark.isNotNone() -> GameContext.coveredMarkedAreaColor
            else -> GameContext.coveredAreaColor
        }

    GameContext.atlas?.let { atlas ->
        if (areaForm == AREA_FULL_FORM) {
            batch.drawRegion(
                texture = atlas.findRegion(AtlasNames.FULL),
                bounds = DrawBounds(x = x - 0.5f, y = y - 0.5f, width = width + 0.5f, height = height + 0.5f),
                color = coverColor,
                blend = false,
            )
        } else {
            drawCoveredPieces(batch, coverColor)
        }
    }
}

private fun AreaActor.drawMinePieces(
    batch: Batch,
    coverColor: Color,
) {
    pieces.forEach { piece ->
        if (piece.value) {
            batch.drawRegion(
                texture = GameContext.atlas!!.findRegion(piece.key),
                bounds = DrawBounds(x = x - 0.5f, y = y - 0.5f, width = width + 1.0f, height = height + 1.0f),
                color = coverColor,
                blend = false,
            )
        }
    }
}

internal fun AreaActor.drawMineBackground(batch: Batch) {
    val coverColor =
        Color(
            AreaActor.MISTAKE_TINT_RED,
            AreaActor.MISTAKE_TINT_GREEN,
            AreaActor.MISTAKE_TINT_BLUE,
            1.0f,
        )

    GameContext.atlas?.let {
        drawMinePieces(batch, coverColor)
    }
}

private fun AreaActor.drawFlagIcon(
    batch: Batch,
    textures: GameTextures,
    isAboveOthers: Boolean,
) {
    val color =
        if (area.mistake) {
            Color(AreaActor.MISTAKE_TINT_RED, AreaActor.MISTAKE_TINT_GREEN, AreaActor.MISTAKE_TINT_BLUE, 1.0f)
        } else {
            GameContext.markColor
        }

    drawAsset(
        batch = batch,
        texture = textures.flag,
        color = color,
        scale = if (isAboveOthers) focusScale else AreaActor.BASE_ICON_SCALE,
    )
}

internal fun AreaActor.drawCoveredIcons(batch: Batch) {
    val isAboveOthers = isPressed

    GameContext.gameTextures?.let { textures ->
        when {
            area.mark.isFlag() -> drawFlagIcon(batch, textures, isAboveOthers)
            area.mark.isQuestion() -> {
                drawAsset(
                    batch = batch,
                    texture = textures.question,
                    color = GameContext.markColor,
                    scale = if (isAboveOthers) focusScale else AreaActor.BASE_ICON_SCALE,
                )
            }
            area.revealed -> {
                drawAsset(
                    batch = batch,
                    texture = textures.mine,
                    color = GameContext.markColor.cpy().alpha(AreaActor.REVEALED_MINE_ALPHA),
                    scale = AreaActor.BASE_ICON_SCALE,
                )
            }
        }
    }
}
