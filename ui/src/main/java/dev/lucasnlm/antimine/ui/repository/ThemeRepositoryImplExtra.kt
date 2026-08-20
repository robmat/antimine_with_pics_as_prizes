package dev.lucasnlm.antimine.ui.repository

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import dev.lucasnlm.antimine.ui.R
import dev.lucasnlm.antimine.ui.model.AppTheme
import dev.lucasnlm.antimine.ui.model.AreaPalette
import dev.lucasnlm.antimine.i18n.R as i18n

/**
 * Theme/palette building logic extracted from [ThemeRepositoryImpl] to keep
 * that class's function count under detekt's threshold.
 */
internal fun ThemeRepositoryImpl.getDefaultTheme(): AppTheme =
    if (preferenceRepository.isPremiumEnabled()) {
        buildSystemTheme()
    } else {
        Themes.lightTheme()
    }

internal fun ThemeRepositoryImpl.buildSystemTheme(): AppTheme =
    AppTheme(
        id = 0L,
        theme = R.style.AppTheme,
        palette =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                fromMaterialYou(context)
            } else {
                fromDefaultPalette(context)
            },
        isPremium = true,
        isDarkTheme = isDarkTheme(),
        name = i18n.string.system,
    )

internal fun ThemeRepositoryImpl.isDarkTheme(): Boolean {
    val mask = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    return mask == Configuration.UI_MODE_NIGHT_YES
}

internal fun ThemeRepositoryImpl.buildAreaPalette(
    context: Context,
    colors: AreaPaletteColors,
) = AreaPalette(
    accent = colors.accent,
    background = colors.background,
    covered = colors.covered,
    coveredOdd = colors.coveredOdd,
    uncovered = colors.uncovered,
    uncoveredOdd = colors.uncoveredOdd,
    minesAround1 = ContextCompat.getColor(context, R.color.mines_around_1),
    minesAround2 = ContextCompat.getColor(context, R.color.mines_around_2),
    minesAround3 = ContextCompat.getColor(context, R.color.mines_around_3),
    minesAround4 = ContextCompat.getColor(context, R.color.mines_around_4),
    minesAround5 = ContextCompat.getColor(context, R.color.mines_around_5),
    minesAround6 = ContextCompat.getColor(context, R.color.mines_around_6),
    minesAround7 = ContextCompat.getColor(context, R.color.mines_around_7),
    minesAround8 = ContextCompat.getColor(context, R.color.mines_around_8),
    highlight = ContextCompat.getColor(context, R.color.highlight),
    focus = colors.focus,
)

@RequiresApi(Build.VERSION_CODES.S)
internal fun ThemeRepositoryImpl.fromMaterialYou(context: Context): AreaPalette {
    val isDarkTheme = isDarkTheme()
    val background =
        if (isDarkTheme) {
            ContextCompat.getColor(context, R.color.background)
        } else {
            ContextCompat.getColor(context, android.R.color.background_light)
        }

    val coveredColor =
        if (isDarkTheme) {
            ContextCompat.getColor(context, android.R.color.system_accent1_300)
        } else {
            ContextCompat.getColor(context, android.R.color.system_accent1_600)
        }

    return buildAreaPalette(
        context = context,
        colors =
            AreaPaletteColors(
                accent = ContextCompat.getColor(context, android.R.color.system_accent1_500),
                background = background,
                covered = coveredColor,
                coveredOdd = coveredColor,
                uncovered = background,
                uncoveredOdd = background,
                focus = ContextCompat.getColor(context, android.R.color.system_accent1_500),
            ),
    )
}

internal fun ThemeRepositoryImpl.fromDefaultPalette(context: Context) =
    buildAreaPalette(
        context = context,
        colors =
            AreaPaletteColors(
                accent = ContextCompat.getColor(context, R.color.accent),
                background = ContextCompat.getColor(context, R.color.background),
                covered = ContextCompat.getColor(context, R.color.view_cover),
                coveredOdd = ContextCompat.getColor(context, R.color.view_cover),
                uncovered = ContextCompat.getColor(context, R.color.view_clean),
                uncoveredOdd = ContextCompat.getColor(context, R.color.view_clean),
                focus = ContextCompat.getColor(context, R.color.accent),
            ),
    )
