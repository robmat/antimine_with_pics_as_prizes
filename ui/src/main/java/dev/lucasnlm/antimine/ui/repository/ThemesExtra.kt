package dev.lucasnlm.antimine.ui.repository

import dev.lucasnlm.antimine.ui.R
import dev.lucasnlm.antimine.ui.model.AppTheme

/**
 * Theme builders extracted from [Themes] to keep that object's function
 * count under detekt's threshold. See [Themes] for details.
 */
internal fun Themes.darkLightBlueTheme() =
    AppTheme(
        id = 28L,
        theme = R.style.DarkLightBlueTheme,
        palette =
        areaPalette(
            colors =
            AreaColors(
                accent = 0x42a5f5,
                background = 0x212121,
                covered = 0x42a5f5,
                coveredOdd = 0x42a5f5,
                uncovered = 0x212121,
                uncoveredOdd = 0x1c1c1c,
            ),
            mines = DARK_STANDARD_MINES,
            highlight = 0xd1c4e9,
        ),
        isDarkTheme = true,
    )

internal fun Themes.darkRedTheme() =
    AppTheme(
        id = 29L,
        theme = R.style.DarkRedTheme,
        palette =
        areaPalette(
            colors =
            AreaColors(
                accent = 0xf44336,
                background = 0x212121,
                covered = 0xf44336,
                coveredOdd = 0xf44336,
                uncovered = 0x212121,
                uncoveredOdd = 0x1c1c1c,
            ),
            mines = DARK_STANDARD_MINES,
            highlight = 0xd1c4e9,
        ),
        isDarkTheme = true,
    )

internal fun Themes.darkPurpleTheme2() =
    AppTheme(
        id = 30L,
        theme = R.style.DarkCustomPurpleTheme,
        palette =
        areaPalette(
            colors =
            AreaColors(
                accent = 0x7e57c2,
                background = 0x212121,
                covered = 0x7e57c2,
                coveredOdd = 0x7e57c2,
                uncovered = 0xd1c4e9,
                uncoveredOdd = 0xd1c4e9,
            ),
            mines = DARK_STANDARD_MINES,
            highlight = 0xd1c4e9,
        ),
        isDarkTheme = true,
    )

internal fun Themes.whiteYellowTheme() =
    AppTheme(
        id = 19L,
        theme = R.style.BananaThemeLight,
        palette =
        areaPalette(
            colors =
            AreaColors(
                accent = 0xfbc02d,
                background = 0xe0e0e0,
                covered = 0xfbc02d,
                coveredOdd = 0xfbc02d,
                uncovered = 0xf4f0d8,
                uncoveredOdd = 0xfff8e1,
            ),
            mines = WARM_MINES,
            highlight = 0xd1c4e9,
        ),
        isDarkTheme = false,
    )

internal fun Themes.whiteOrangeTheme() =
    AppTheme(
        id = 20L,
        theme = R.style.BananaThemeLight,
        palette =
        areaPalette(
            colors =
            AreaColors(
                accent = 0xf57c00,
                background = 0xe0e0e0,
                covered = 0xf57c00,
                coveredOdd = 0xf57c00,
                uncovered = 0xf4f0d8,
                uncoveredOdd = 0xfff8e1,
            ),
            mines = WARM_MINES,
            highlight = 0xd1c4e9,
        ),
        isDarkTheme = false,
    )

internal fun Themes.whiteDarkYellowTheme() =
    AppTheme(
        id = 21L,
        theme = R.style.BananaThemeLight,
        palette =
        areaPalette(
            colors =
            AreaColors(
                accent = 0x827717,
                background = 0xe0e0e0,
                covered = 0x827717,
                coveredOdd = 0x827717,
                uncovered = 0xf4f0d8,
                uncoveredOdd = 0xfff8e1,
            ),
            mines = WARM_MINES,
            highlight = 0xd1c4e9,
        ),
        isDarkTheme = false,
    )
