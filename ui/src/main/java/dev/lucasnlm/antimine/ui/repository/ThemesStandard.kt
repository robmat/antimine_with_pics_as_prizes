package dev.lucasnlm.antimine.ui.repository

import dev.lucasnlm.antimine.ui.R
import dev.lucasnlm.antimine.ui.model.AppTheme
import dev.lucasnlm.antimine.ui.model.AreaPalette

/**
 * Theme builders extracted from [Themes] to keep that object's function
 * count under detekt's threshold. See [Themes] for details.
 */
fun Themes.lightTheme() =
    AppTheme(
        id = 1L,
        theme = R.style.CustomLightTheme,
        palette =
            areaPalette(
                colors =
                    AreaColors(
                        accent = 0xD32F2F,
                        background = 0xFFFFFF,
                        covered = 0x424242,
                        coveredOdd = 0x424242,
                        uncovered = 0xd5d2cc,
                        uncoveredOdd = 0xd5d2cc,
                    ),
                mines = LIGHT_STANDARD_MINES,
                highlight = 0x212121,
            ),
        isPremium = true,
        isDarkTheme = false,
    )

internal fun Themes.darkTheme() =
    AppTheme(
        id = 3L,
        theme = R.style.CustomDarkTheme,
        palette =
            AreaPalette(
                accent = 0xFFFFFF,
                background = 0x212121,
                covered = 0xd5d2cc,
                coveredOdd = 0xd5d2cc,
                uncovered = 0x424242,
                uncoveredOdd = 0x424242,
                minesAround1 = 0xd5d2cc,
                minesAround2 = 0xd5d2cc,
                minesAround3 = 0xd5d2cc,
                minesAround4 = 0xd5d2cc,
                minesAround5 = 0xd5d2cc,
                minesAround6 = 0xd5d2cc,
                minesAround7 = 0xd5d2cc,
                minesAround8 = 0xd5d2cc,
                highlight = 0xFFFFFF,
                focus = 0xFFFFFF,
            ),
        isPremium = true,
        isDarkTheme = true,
    )

internal fun Themes.amoledTheme() =
    AppTheme(
        id = 2L,
        theme = R.style.CustomAmoledTheme,
        palette =
            areaPalette(
                colors =
                    AreaColors(
                        accent = 0xFFFFFF,
                        background = 0x000000,
                        covered = 0x616161,
                        coveredOdd = 0x616161,
                        uncovered = 0x000000,
                        uncoveredOdd = 0x050505,
                    ),
                mines = AMOLED_MINES,
                highlight = 0x212121,
            ),
        isDarkTheme = true,
    )

internal fun Themes.amoledTheme2() =
    AppTheme(
        id = 22L,
        theme = R.style.CustomAmoledTheme,
        palette =
            areaPalette(
                colors =
                    AreaColors(
                        accent = 0xFFFFFF,
                        background = 0x000000,
                        covered = 0xEEEEEE,
                        coveredOdd = 0xDDDDDD,
                        uncovered = 0x000000,
                        uncoveredOdd = 0x050505,
                    ),
                mines = AMOLED_MINES,
                highlight = 0x212121,
            ),
        isDarkTheme = true,
    )

internal fun Themes.standardChessTheme() =
    AppTheme(
        id = 5L,
        theme = R.style.CustomLightTheme,
        palette =
            areaPalette(
                colors =
                    AreaColors(
                        accent = 0x37474f,
                        background = 0xFFFFFF,
                        covered = 0x4a4a4a,
                        coveredOdd = 0x383838,
                        uncovered = 0xe2e1da,
                        uncoveredOdd = 0xd5d2cc,
                    ),
                mines = LIGHT_STANDARD_MINES,
                highlight = 0x212121,
            ),
        isDarkTheme = false,
    )

internal fun Themes.goldenTheme() =
    AppTheme(
        id = 23L,
        theme = R.style.CustomLightTheme,
        palette =
            areaPalette(
                colors =
                    AreaColors(
                        accent = 0x37474f,
                        background = 0xFFFFFF,
                        covered = 0xf9a825,
                        coveredOdd = 0xf9a825,
                        uncovered = 0xe2e1da,
                        uncoveredOdd = 0xd5d2cc,
                    ),
                mines = LIGHT_STANDARD_MINES,
                highlight = 0x212121,
            ),
        isDarkTheme = false,
    )

internal fun Themes.blueTheme() =
    AppTheme(
        id = 24L,
        theme = R.style.CustomMarineTheme,
        palette =
            areaPalette(
                colors =
                    AreaColors(
                        accent = 0x37474f,
                        background = 0xFFFFFF,
                        covered = 0x0277bd,
                        coveredOdd = 0x0277bd,
                        uncovered = 0xe2e1da,
                        uncoveredOdd = 0xd5d2cc,
                    ),
                mines = LIGHT_STANDARD_MINES,
                highlight = 0x212121,
            ),
        isDarkTheme = false,
    )

internal fun Themes.gardenTheme() =
    AppTheme(
        id = 4L,
        theme = R.style.CustomGardenTheme,
        palette =
            areaPalette(
                colors =
                    AreaColors(
                        accent = 0x689f38,
                        background = 0xefebe9,
                        covered = 0x689f38,
                        coveredOdd = 0x558b2f,
                        uncovered = 0xefebe9,
                        uncoveredOdd = 0xd7ccc8,
                    ),
                mines = LIGHT_STANDARD_MINES,
                highlight = 0x689f38,
                focus = 0xFFFFFF,
            ),
        isDarkTheme = false,
    )
