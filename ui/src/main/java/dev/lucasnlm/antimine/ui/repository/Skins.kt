package dev.lucasnlm.antimine.ui.repository

import dev.lucasnlm.antimine.ui.R
import dev.lucasnlm.antimine.ui.model.AppSkin

/**
 * Some of the skin builders used to live here directly, but that pushed this
 * object's function count over detekt's threshold. The `square*`, `stone2`
 * and `defaultNoJoin` builders now live as extension functions in
 * SkinsExtra.kt instead, called from [getAllSkins] via the implicit
 * [Skins] receiver.
 */
object Skins {
    private fun default() =
        AppSkin(
            id = 0,
            file = "standard.png",
            canTint = true,
            isPremium = false,
            hasPadding = true,
            thumbnailImageRes = R.drawable.skin_standard,
        )

    private fun classic() =
        AppSkin(
            id = 4,
            file = "classic.png",
            canTint = true,
            isPremium = true,
            hasPadding = true,
            thumbnailImageRes = R.drawable.skin_classic,
            background = 4,
        )

    private fun classic2() =
        AppSkin(
            id = 5,
            file = "classic.png",
            canTint = false,
            isPremium = true,
            hasPadding = true,
            thumbnailImageRes = R.drawable.skin_classic,
            background = 4,
        )

    private fun glass() =
        AppSkin(
            id = 6,
            file = "glass.png",
            canTint = true,
            isPremium = true,
            hasPadding = true,
            thumbnailImageRes = R.drawable.skin_glass_2,
        )

    private fun stone() =
        AppSkin(
            id = 7,
            file = "stone.png",
            canTint = false,
            isPremium = true,
            hasPadding = true,
            thumbnailImageRes = R.drawable.skin_stone,
            background = 4,
        )

    fun getAllSkins() =
        listOf(
            default(),
            classic(),
            classic2(),
            stone(),
            glass(),
            square(),
            square2(),
            square3(),
            stone2(),
            defaultNoJoin(),
        )
}
