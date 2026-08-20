package com.batodev.antimine.playgames.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import com.batodev.antimine.databinding.ViewPlayGamesButtonBinding
import dev.lucasnlm.antimine.ui.view.BaseFrameLayout

class PlayGamesButtonView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : BaseFrameLayout(context, attrs, defStyleAttr) {
        val binding: ViewPlayGamesButtonBinding

        init {
            val layoutInflater = LayoutInflater.from(context)
            binding = ViewPlayGamesButtonBinding.inflate(layoutInflater, this, true)
        }
    }
