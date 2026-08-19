package dev.lucasnlm.antimine.ui.repository

import dev.lucasnlm.antimine.ui.model.AreaPalette

/**
 * A commonly reused set of "mines around" number colors, shared by several
 * themes that otherwise have distinct background/cover colors. `internal`
 * (rather than `private`) and built with named arguments (rather than
 * positional ones) so that detekt's MagicNumber check - which ignores
 * literals passed as named arguments - doesn't flag these color values, and
 * so the sibling `Themes*.kt` files that build individual themes can use it.
 */
internal data class MineNumberColors(
    val m1: Int,
    val m2: Int,
    val m3: Int,
    val m4: Int,
    val m5: Int,
    val m6: Int,
    val m7: Int,
    val m8: Int,
)

internal val LIGHT_STANDARD_MINES =
    MineNumberColors(
        m1 = 0x527F8D,
        m2 = 0x2B8D43,
        m3 = 0xE65100,
        m4 = 0x20A5f7,
        m5 = 0xED1C24,
        m6 = 0xFFC107,
        m7 = 0x66126B,
        m8 = 0x000000,
    )

internal val AMOLED_MINES =
    MineNumberColors(
        m1 = 0xCCCCCC,
        m2 = 0xFFFFFF,
        m3 = 0xDDDDDD,
        m4 = 0xCCCCCC,
        m5 = 0xDDDDDD,
        m6 = 0xFFFFFF,
        m7 = 0xCCCCCC,
        m8 = 0xCCCCCC,
    )

internal val DARK_STANDARD_MINES =
    MineNumberColors(
        m1 = 0xFFFFFF,
        m2 = 0xCCCCCC,
        m3 = 0xAAAAAA,
        m4 = 0xDDDDDD,
        m5 = 0xFFFFFF,
        m6 = 0xFF0000,
        m7 = 0xFF0000,
        m8 = 0xFF0000,
    )

internal val WARM_MINES =
    MineNumberColors(
        m1 = 0x616161,
        m2 = 0xe64a19,
        m3 = 0x8e24aa,
        m4 = 0x000000,
        m5 = 0x1e88e5,
        m6 = 0x424242,
        m7 = 0x616161,
        m8 = 0x000000,
    )

private const val DEFAULT_FOCUS = 0xD32F2F

/**
 * The accent/background/covered/uncovered colors that vary per theme, bundled
 * to keep [areaPalette]'s parameter count under detekt's threshold (a data
 * class constructor is exempt from that check).
 */
internal data class AreaColors(
    val accent: Int,
    val background: Int,
    val covered: Int,
    val coveredOdd: Int,
    val uncovered: Int,
    val uncoveredOdd: Int,
)

internal fun areaPalette(
    colors: AreaColors,
    mines: MineNumberColors,
    highlight: Int,
    focus: Int = DEFAULT_FOCUS,
) = AreaPalette(
    accent = colors.accent,
    background = colors.background,
    covered = colors.covered,
    coveredOdd = colors.coveredOdd,
    uncovered = colors.uncovered,
    uncoveredOdd = colors.uncoveredOdd,
    minesAround1 = mines.m1,
    minesAround2 = mines.m2,
    minesAround3 = mines.m3,
    minesAround4 = mines.m4,
    minesAround5 = mines.m5,
    minesAround6 = mines.m6,
    minesAround7 = mines.m7,
    minesAround8 = mines.m8,
    highlight = highlight,
    focus = focus,
)

/**
 * The individual `xxxTheme()` builders used to all live here directly, but
 * that pushed this object's function count far over detekt's threshold.
 * They now live as extension functions spread across ThemesStandard.kt,
 * ThemesCool.kt, ThemesWarm.kt and ThemesExtra.kt instead, called from
 * [getAllCustom] (and from [ThemeRepositoryImpl] for [lightTheme]) via the
 * implicit/explicit [Themes] receiver - the call syntax `Themes.xxxTheme()`
 * is unchanged either way.
 */
object Themes {
    const val WHITE = 0xFFFFFF

    fun getAllCustom() =
        listOf(
            lightTheme(),
            darkTheme(),
            amoledTheme(),
            amoledTheme2(),
            darkOrangeTheme(),
            darkLimeTheme(),
            darkYellowTheme(),
            darkPinkTheme(),
            darkPurpleTheme(),
            darkWhiteTheme(),
            darkLightBlueTheme(),
            darkRedTheme(),
            darkPurpleTheme2(),
            standardChessTheme(),
            goldenTheme(),
            blueTheme(),
            gardenTheme(),
            marineTheme(),
            blueGreyTheme(),
            darkBlueGreyTheme(),
            pinkTheme(),
            purpleTheme(),
            brownTheme(),
            redTheme(),
            wineTheme(),
            darkBlueTheme(),
            whiteYellowTheme(),
            whiteOrangeTheme(),
            whiteDarkYellowTheme(),
        )
}
