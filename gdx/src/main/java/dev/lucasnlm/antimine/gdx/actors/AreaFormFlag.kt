package dev.lucasnlm.antimine.gdx.actors

object AreaFormFlag {
    const val TOP = 0b00000001
    const val BOTTOM = 0b00000010
    const val LEFT = 0b00000100
    const val RIGHT = 0b00001000
    const val TOP_LEFT = 0b00010000
    const val TOP_RIGHT = 0b00100000
    const val BOTTOM_LEFT = 0b01000000
    const val BOTTOM_RIGHT = 0b10000000
}

private fun Int.checkForm(flag: Int): Boolean {
    return (this and flag) != 0
}

fun Int.top(): Boolean = checkForm(AreaFormFlag.TOP)

fun Int.bottom(): Boolean = checkForm(AreaFormFlag.BOTTOM)

fun Int.left(): Boolean = checkForm(AreaFormFlag.LEFT)

fun Int.right(): Boolean = checkForm(AreaFormFlag.RIGHT)

fun Int.topLeft(): Boolean = checkForm(AreaFormFlag.TOP_LEFT)

fun Int.topRight(): Boolean = checkForm(AreaFormFlag.TOP_RIGHT)

fun Int.bottomLeft(): Boolean = checkForm(AreaFormFlag.BOTTOM_LEFT)

fun Int.bottomRight(): Boolean = checkForm(AreaFormFlag.BOTTOM_RIGHT)

data class NeighborLinks(
    val top: Boolean,
    val bottom: Boolean,
    val left: Boolean,
    val right: Boolean,
    val topLeft: Boolean = false,
    val topRight: Boolean = false,
    val bottomLeft: Boolean = false,
    val bottomRight: Boolean = false,
)

fun areaFormOf(links: NeighborLinks): Int {
    var result = 0x00

    if (links.top) {
        result = result or AreaFormFlag.TOP
    }

    if (links.bottom) {
        result = result or AreaFormFlag.BOTTOM
    }

    if (links.left) {
        result = result or AreaFormFlag.LEFT
    }

    if (links.right) {
        result = result or AreaFormFlag.RIGHT
    }

    if (links.topLeft) {
        result = result or AreaFormFlag.TOP_LEFT
    }

    if (links.topRight) {
        result = result or AreaFormFlag.TOP_RIGHT
    }

    if (links.bottomLeft) {
        result = result or AreaFormFlag.BOTTOM_LEFT
    }

    if (links.bottomRight) {
        result = result or AreaFormFlag.BOTTOM_RIGHT
    }

    return result
}

const val AREA_NO_FORM = 0b00000000

const val AREA_FULL_FORM = 0b11111111
