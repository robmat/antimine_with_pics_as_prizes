package dev.lucasnlm.antimine.ui.repository

import dev.lucasnlm.antimine.ui.R
import dev.lucasnlm.antimine.ui.model.AppSkin

/**
 * Skin builders extracted from [Skins] to keep that object's function count
 * under detekt's threshold. Called from [Skins.getAllSkins] via the implicit
 * [Skins] receiver, since these are declared in the same package.
 */
internal fun Skins.square() =
    AppSkin(
        id = 1,
        file = "square.png",
        canTint = true,
        isPremium = true,
        hasPadding = true,
        thumbnailImageRes = R.drawable.skin_square,
    )

internal fun Skins.square2() =
    AppSkin(
        id = 2,
        file = "square-2.png",
        canTint = true,
        isPremium = true,
        hasPadding = true,
        thumbnailImageRes = R.drawable.skin_square_2,
    )

internal fun Skins.square3() =
    AppSkin(
        id = 3,
        file = "square-3.png",
        canTint = true,
        isPremium = true,
        hasPadding = false,
        thumbnailImageRes = R.drawable.skin_square_3,
        showPadding = false,
    )

internal fun Skins.stone2() =
    AppSkin(
        id = 8,
        file = "stone-2.png",
        canTint = true,
        isPremium = true,
        hasPadding = true,
        thumbnailImageRes = R.drawable.skin_stone_2,
        background = 4,
    )

internal fun Skins.defaultNoJoin() =
    AppSkin(
        id = 9,
        file = "standard.png",
        canTint = true,
        isPremium = true,
        hasPadding = false,
        thumbnailImageRes = R.drawable.skin_standard_no_connection,
        showPadding = false,
    )
