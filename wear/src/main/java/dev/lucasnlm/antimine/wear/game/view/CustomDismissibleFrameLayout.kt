package dev.lucasnlm.antimine.wear.game.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.fragment.app.Fragment
import dev.lucasnlm.antimine.ui.view.BaseFrameLayout
import java.lang.ref.WeakReference

class CustomDismissibleFrameLayout
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : BaseFrameLayout(context, attrs, defStyleAttr) {
    private var childFragment: WeakReference<Fragment>? = null

    fun setChildFragment(fragment: Fragment) {
        childFragment = WeakReference(fragment)
    }

    override fun onInterceptTouchEvent(event: MotionEvent?): Boolean {
        val child = childFragment?.get()
        return child?.view?.onTouchEvent(event) ?: true
    }
}
