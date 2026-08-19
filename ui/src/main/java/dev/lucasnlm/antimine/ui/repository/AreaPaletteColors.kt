package dev.lucasnlm.antimine.ui.repository

/**
 * The "mines around" colors, highlight and focus are shared by every
 * palette [ThemeRepositoryImpl] builds from the device's colors, so they're
 * bundled here to keep `buildAreaPalette`'s parameter count under detekt's
 * threshold (a data class constructor is exempt from that check).
 */
internal data class AreaPaletteColors(
    val accent: Int,
    val background: Int,
    val covered: Int,
    val coveredOdd: Int,
    val uncovered: Int,
    val uncoveredOdd: Int,
    val focus: Int,
)
