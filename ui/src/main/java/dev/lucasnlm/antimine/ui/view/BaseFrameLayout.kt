package dev.lucasnlm.antimine.ui.view

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout

/**
 * A [FrameLayout] base that centralizes the boilerplate 3-constructor delegation
 * (plain XML inflation, and manual instantiation) needed by every custom view here.
 */
abstract class BaseFrameLayout
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : FrameLayout(context, attrs, defStyleAttr)
