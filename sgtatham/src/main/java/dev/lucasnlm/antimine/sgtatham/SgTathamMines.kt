package dev.lucasnlm.antimine.sgtatham

class SgTathamMines {
    /**
     * Uses the same minefield generator of SgTatham mines game.
     *
     * This is a native (JNI) declaration, so its parameters must stay plain
     * primitives that the C++ side in sgtatham.cpp can read directly -
     * unlike the pure-Kotlin LongParameterList fixes elsewhere in this
     * codebase, a Kotlin data class can't simply replace them. [dimensions]
     * bundles [width, height, mines] into a single [IntArray] instead, read
     * back via `GetIntArrayRegion` on the native side, to keep this
     * function's parameter count under detekt's threshold.
     *
     * @param seed The seed to be used to generate the minefield.
     * @param dimensions The [width, height, mines] of the minefield.
     * @param x The x coordinate of the first click.
     * @param y The y coordinate of the first click.
     */
    external fun createMinefield(
        seed: Long,
        dimensions: IntArray,
        x: Int,
        y: Int,
    ): String

    companion object {
        // Used to load the 'sgtatham' library on application startup.
        init {
            System.loadLibrary("sgtatham")
        }
    }
}
