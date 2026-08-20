package dev.lucasnlm.antimine.gdx

import dev.lucasnlm.antimine.core.isPortrait
import dev.lucasnlm.antimine.gdx.models.GameTextures
import dev.lucasnlm.antimine.gdx.models.InternalPadding
import dev.lucasnlm.antimine.ui.model.AppSkin

/**
 * [GameApplicationListener]'s texture-loading and padding-computation logic,
 * split out of the class body - see its class doc.
 */
internal fun GameApplicationListener.loadGameTextures(currentSkin: AppSkin) {
    GameContext.run {
        canTintAreas = currentSkin.canTint

        atlas =
            GameTextureAtlas
                .loadTextureAtlas(
                    skinFile = currentSkin.file,
                    defaultBackground = currentSkin.background,
                ).apply {
                    gameTextures =
                        GameTextures(
                            areaBackground = findRegion(AtlasNames.SINGLE_BACKGROUND),
                            aroundMines =
                                listOf(
                                    AtlasNames.NUMBER_1,
                                    AtlasNames.NUMBER_2,
                                    AtlasNames.NUMBER_3,
                                    AtlasNames.NUMBER_4,
                                    AtlasNames.NUMBER_5,
                                    AtlasNames.NUMBER_6,
                                    AtlasNames.NUMBER_7,
                                    AtlasNames.NUMBER_8,
                                ).map(::findRegion),
                            pieces =
                                listOf(
                                    AtlasNames.CORE,
                                    AtlasNames.BOTTOM,
                                    AtlasNames.TOP,
                                    AtlasNames.RIGHT,
                                    AtlasNames.LEFT,
                                    AtlasNames.CORNER_TOP_LEFT,
                                    AtlasNames.CORNER_TOP_RIGHT,
                                    AtlasNames.CORNER_BOTTOM_RIGHT,
                                    AtlasNames.CORNER_BOTTOM_LEFT,
                                    AtlasNames.BORDER_CORNER_RIGHT,
                                    AtlasNames.BORDER_CORNER_LEFT,
                                    AtlasNames.BORDER_CORNER_BOTTOM_RIGHT,
                                    AtlasNames.BORDER_CORNER_BOTTOM_LEFT,
                                    AtlasNames.FILL_TOP_LEFT,
                                    AtlasNames.FILL_TOP_RIGHT,
                                    AtlasNames.FILL_BOTTOM_RIGHT,
                                    AtlasNames.FILL_BOTTOM_LEFT,
                                    AtlasNames.FULL,
                                ).associateWith(::findRegion),
                            mine = findRegion(AtlasNames.MINE),
                            flag = findRegion(AtlasNames.FLAG),
                            question = findRegion(AtlasNames.QUESTION),
                            detailedArea = findRegion(AtlasNames.SINGLE),
                        )
                }
    }
}

internal fun GameApplicationListener.getInternalPadding(): InternalPadding {
    val padding = dimensionRepository.areaSize()
    return when {
        context.isPortrait() -> {
            InternalPadding(
                start = padding,
                end = padding,
                bottom = padding,
                top = padding,
            )
        }

        else -> {
            InternalPadding(
                start = padding,
                end = padding,
                bottom = padding,
                top = padding,
            )
        }
    }
}
