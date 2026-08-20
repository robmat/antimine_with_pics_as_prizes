package dev.lucasnlm.antimine.ui.repository

import dev.lucasnlm.antimine.ui.R
import dev.lucasnlm.antimine.ui.model.AppTheme

/**
 * Theme builders extracted from [Themes] to keep that object's function
 * count under detekt's threshold. See [Themes] for details.
 */
internal fun Themes.purpleTheme() =
    AppTheme(
        id = 10L,
        theme = R.style.CustomPurpleTheme,
        palette =
            areaPalette(
                colors =
                    AreaColors(
                        accent = 0x6a1b9a,
                        background = 0xFFFFFF,
                        covered = 0x6a1b9a,
                        coveredOdd = 0x6a1b9a,
                        uncovered = 0xd1c4e9,
                        uncoveredOdd = 0xd1c4e9,
                    ),
                mines = WARM_MINES,
                highlight = 0xd1c4e9,
            ),
        isDarkTheme = false,
    )

internal fun Themes.darkPurpleTheme() =
    AppTheme(
        id = 27L,
        theme = R.style.DarkCustomPurpleTheme,
        palette =
            areaPalette(
                colors =
                    AreaColors(
                        accent = 0x6a1b9a,
                        background = 0x212121,
                        covered = 0x6a1b9a,
                        coveredOdd = 0x6a1b9a,
                        uncovered = 0xd1c4e9,
                        uncoveredOdd = 0xd1c4e9,
                    ),
                mines = DARK_STANDARD_MINES,
                highlight = 0xd1c4e9,
            ),
        isDarkTheme = true,
    )

internal fun Themes.brownTheme() =
    AppTheme(
        id = 11L,
        theme = R.style.CustomLightTheme,
        palette =
            areaPalette(
                colors =
                    AreaColors(
                        accent = 0x3e2723,
                        background = 0xFFFFFF,
                        covered = 0x3e2723,
                        coveredOdd = 0x4e342e,
                        uncovered = 0xd7ccc8,
                        uncoveredOdd = 0xefebe9,
                    ),
                mines = WARM_MINES,
                highlight = 0xd1c4e9,
            ),
        isDarkTheme = false,
    )

internal fun Themes.redTheme() =
    AppTheme(
        id = 12L,
        theme = R.style.CustomLightTheme,
        palette =
            areaPalette(
                colors =
                    AreaColors(
                        accent = 0xc62828,
                        background = 0xFFFFFF,
                        covered = 0xc62828,
                        coveredOdd = 0xb71c1c,
                        uncovered = 0xd7ccc8,
                        uncoveredOdd = 0xefebe9,
                    ),
                mines = WARM_MINES,
                highlight = 0xd1c4e9,
            ),
        isDarkTheme = false,
    )

internal fun Themes.wineTheme() =
    AppTheme(
        id = 13L,
        theme = R.style.CustomLightTheme,
        palette =
            areaPalette(
                colors =
                    AreaColors(
                        accent = 0x880e4f,
                        background = 0xFFFFFF,
                        covered = 0x880e4f,
                        coveredOdd = 0x750b42,
                        uncovered = 0xd7ccc8,
                        uncoveredOdd = 0xefebe9,
                    ),
                mines = WARM_MINES,
                highlight = 0xd1c4e9,
            ),
        isDarkTheme = false,
    )

internal fun Themes.darkBlueTheme() =
    AppTheme(
        id = 14L,
        theme = R.style.CustomLightTheme,
        palette =
            areaPalette(
                colors =
                    AreaColors(
                        accent = 0x0d47a1,
                        background = 0xFFFFFF,
                        covered = 0x0d47a1,
                        coveredOdd = 0x0a3984,
                        uncovered = 0xedf1f2,
                        uncoveredOdd = 0xdcdee0,
                    ),
                mines = WARM_MINES,
                highlight = 0xd1c4e9,
            ),
        isDarkTheme = false,
    )

internal fun Themes.darkWhiteTheme() =
    AppTheme(
        id = 15L,
        theme = R.style.CustomDarkTheme,
        palette =
            areaPalette(
                colors =
                    AreaColors(
                        accent = 0xedf1f2,
                        background = 0x212121,
                        covered = 0xedf1f2,
                        coveredOdd = 0xdcdee0,
                        uncovered = 0x212121,
                        uncoveredOdd = 0x1c1c1c,
                    ),
                mines = DARK_STANDARD_MINES,
                highlight = 0xd1c4e9,
            ),
        isDarkTheme = true,
    )
