package dev.lucasnlm.antimine.ui.repository

import dev.lucasnlm.antimine.ui.R
import dev.lucasnlm.antimine.ui.model.AppTheme
import dev.lucasnlm.antimine.ui.model.AreaPalette

/**
 * Theme builders extracted from [Themes] to keep that object's function
 * count under detekt's threshold. See [Themes] for details.
 */
internal fun Themes.marineTheme() =
    AppTheme(
        id = 6L,
        theme = R.style.CustomMarineTheme,
        palette =
            areaPalette(
                colors =
                    AreaColors(
                        accent = 0x0277bd,
                        background = 0xFFFFFF,
                        covered = 0x0277bd,
                        coveredOdd = 0x006aa8,
                        uncovered = 0xc0d2d9,
                        uncoveredOdd = 0xc0d2d9,
                    ),
                mines = LIGHT_STANDARD_MINES,
                highlight = 0x212121,
            ),
        isDarkTheme = false,
    )

internal fun Themes.blueGreyTheme() =
    AppTheme(
        id = 7L,
        theme = R.style.CustomBlueGreyTheme,
        palette =
            AreaPalette(
                accent = 0x37474f,
                background = 0xFFFFFF,
                covered = 0x37474f,
                coveredOdd = 0x37474f,
                uncovered = 0xcfd8dc,
                uncoveredOdd = 0xcfd8dc,
                minesAround1 = 0x527F8D,
                minesAround2 = 0x2B8D43,
                minesAround3 = 0x546e7a,
                minesAround4 = 0x20A5f7,
                minesAround5 = 0xED1C24,
                minesAround6 = 0xFFC107,
                minesAround7 = 0x66126B,
                minesAround8 = 0x000000,
                highlight = 0x212121,
                focus = 0xD32F2F,
            ),
        isDarkTheme = false,
    )

internal fun Themes.darkBlueGreyTheme() =
    AppTheme(
        id = 25L,
        theme = R.style.CustomBlueGreyTheme,
        palette =
            areaPalette(
                colors =
                    AreaColors(
                        accent = 0x37474f,
                        background = 0x677075,
                        covered = 0x071821,
                        coveredOdd = 0x071821,
                        uncovered = 0xcfd8dc,
                        uncoveredOdd = 0xcfd8dc,
                    ),
                mines = DARK_STANDARD_MINES,
                highlight = 0xd1c4e9,
            ),
        isDarkTheme = false,
    )

internal fun Themes.darkOrangeTheme() =
    AppTheme(
        id = 8L,
        theme = R.style.CustomOrangeTheme,
        palette =
            AreaPalette(
                accent = 0xfb8c00,
                background = 0x212121,
                covered = 0xfb8c00,
                coveredOdd = 0xfb8c00,
                uncovered = 0x303030,
                uncoveredOdd = 0x252525,
                minesAround1 = 0xDDDDDD,
                minesAround2 = 0xEEEEEE,
                minesAround3 = 0xCCCCCC,
                minesAround4 = 0xBBBBBB,
                minesAround5 = 0xAAAAAA,
                minesAround6 = 0xFFFFFF,
                minesAround7 = 0xBBBBBB,
                minesAround8 = 0xEEEEEE,
                highlight = 0xfb8c00,
                focus = 0xD32F2F,
            ),
        isDarkTheme = true,
    )

internal fun Themes.darkLimeTheme() =
    AppTheme(
        id = 16L,
        theme = R.style.CustomLimeTheme,
        palette =
            areaPalette(
                colors =
                    AreaColors(
                        accent = 0xcddc39,
                        background = 0x212121,
                        covered = 0xcddc39,
                        coveredOdd = 0xcddc39,
                        uncovered = 0x212121,
                        uncoveredOdd = 0x1c1c1c,
                    ),
                mines = DARK_STANDARD_MINES,
                highlight = 0xd1c4e9,
            ),
        isDarkTheme = true,
    )

internal fun Themes.darkYellowTheme() =
    AppTheme(
        id = 18L,
        theme = R.style.BananaTheme,
        palette =
            areaPalette(
                colors =
                    AreaColors(
                        accent = 0xffeb3b,
                        background = 0x212121,
                        covered = 0xffeb3b,
                        coveredOdd = 0xe6d335,
                        uncovered = 0x212121,
                        uncoveredOdd = 0x1c1c1c,
                    ),
                mines = DARK_STANDARD_MINES,
                highlight = 0xd1c4e9,
            ),
        isDarkTheme = true,
    )

internal fun Themes.pinkTheme() =
    AppTheme(
        id = 9L,
        theme = R.style.CustomPinkTheme,
        palette =
            areaPalette(
                colors =
                    AreaColors(
                        accent = 0xf48fb1,
                        background = 0xFFFFFF,
                        covered = 0xf48fb1,
                        coveredOdd = 0xf48fb1,
                        uncovered = 0xfce4ec,
                        uncoveredOdd = 0xfce4ec,
                    ),
                mines = WARM_MINES,
                highlight = 0x212121,
            ),
        isDarkTheme = false,
    )

internal fun Themes.darkPinkTheme() =
    AppTheme(
        id = 26L,
        theme = R.style.CustomDarkPinkTheme,
        palette =
            areaPalette(
                colors =
                    AreaColors(
                        accent = 0xf48fb1,
                        background = 0x212121,
                        covered = 0xf48fb1,
                        coveredOdd = 0xf48fb1,
                        uncovered = 0xfce4ec,
                        uncoveredOdd = 0xfce4ec,
                    ),
                mines = DARK_STANDARD_MINES,
                highlight = 0xd1c4e9,
            ),
        isDarkTheme = true,
    )
