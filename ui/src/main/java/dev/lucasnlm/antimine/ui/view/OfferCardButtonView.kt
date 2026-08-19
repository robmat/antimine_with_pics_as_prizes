package dev.lucasnlm.antimine.ui.view

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.view.isVisible
import dev.lucasnlm.antimine.ui.databinding.ViewOfferCardButtonBinding
import dev.lucasnlm.antimine.ui.ext.toAndroidColor
import dev.lucasnlm.antimine.ui.model.AppTheme

/**
 * Bundles [OfferCardButtonView.bind]'s arguments to keep its parameter count
 * under detekt's threshold (a data class constructor is exempt from that
 * check).
 */
data class OfferCardButtonConfig(
    val theme: AppTheme,
    val text: String,
    val onAction: (View) -> Unit,
    val invert: Boolean = false,
    val price: String? = null,
    val showOffer: Boolean = false,
    val centralize: Boolean = false,
    @DrawableRes val startIcon: Int? = null,
)

class OfferCardButtonView
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : BaseFrameLayout(context, attrs, defStyleAttr) {
    private val binding: ViewOfferCardButtonBinding by lazy {
        ViewOfferCardButtonBinding.bind(this)
    }

    init {
        val layoutInflater = LayoutInflater.from(context)
        ViewOfferCardButtonBinding.inflate(layoutInflater, this, true)
    }

    fun bind(config: OfferCardButtonConfig) {
        bindView(config)
    }

    private fun bindLabel(
        text: String,
        centralize: Boolean,
        color: Int,
    ) = binding.label.apply {
        this.text = text
        if (centralize) {
            gravity = Gravity.CENTER_HORIZONTAL
        }
        setTextColor(color)
    }

    private fun bindPrice(
        price: String?,
        invert: Boolean,
        color: Int,
    ) = binding.price.apply {
        isVisible = price != null
        if (price != null) {
            this.text = price
        }

        if (invert) {
            setTextColor(color)
        }
    }

    private fun bindOffer(
        showOffer: Boolean,
        invert: Boolean,
        color: Int,
    ) = binding.priceOff.apply {
        isVisible = showOffer
        if (invert) {
            imageTintList = ColorStateList.valueOf(color)
        }
    }

    private fun bindIcon(
        theme: AppTheme,
        @DrawableRes startIcon: Int?,
    ) = binding.icon.apply {
        isVisible = startIcon != null
        if (startIcon != null) {
            val tintColor = theme.palette.covered.toAndroidColor()
            imageTintList = ColorStateList.valueOf(tintColor)
            setImageResource(startIcon)
        }
    }

    private fun setupFocusChangeListener(
        theme: AppTheme,
        label: TextView,
        priceView: TextView,
        iconView: ImageView,
        offerView: ImageView,
    ) {
        binding.cardView.setOnFocusChangeListener { _, focused ->
            val focusedBackgroundColor =
                if (focused) {
                    theme.palette.accent.toAndroidColor()
                } else {
                    theme.palette.background.toAndroidColor()
                }

            val inverted =
                if (focused) {
                    theme.palette.background.toAndroidColor()
                } else {
                    theme.palette.accent.toAndroidColor()
                }

            label.setTextColor(inverted)
            priceView.setTextColor(inverted)
            iconView.setColorFilter(inverted)
            binding.cardView.setCardBackgroundColor(focusedBackgroundColor)
            offerView.imageTintList = ColorStateList.valueOf(inverted)
        }
    }

    private fun bindView(config: OfferCardButtonConfig) {
        val theme = config.theme
        val text = config.text
        val onAction = config.onAction
        val invert = config.invert
        val price = config.price
        val showOffer = config.showOffer
        val centralize = config.centralize
        val startIcon = config.startIcon

        val color =
            if (invert || isFocused) {
                theme.palette.background.toAndroidColor()
            } else {
                theme.palette.covered.toAndroidColor()
            }

        val backgroundColor =
            if (invert || isFocused) {
                theme.palette.covered.toAndroidColor()
            } else {
                theme.palette.background.toAndroidColor()
            }

        val label = bindLabel(text, centralize, color)
        val priceView = bindPrice(price, invert, color)
        val offerView = bindOffer(showOffer, invert, color)
        val iconView = bindIcon(theme, startIcon)

        binding.cardView.apply {
            setOnClickListener(onAction)
            strokeColor = if (!invert) color else backgroundColor
            setCardBackgroundColor(backgroundColor)
            isSoundEffectsEnabled = false
        }

        setupFocusChangeListener(theme, label, priceView, iconView, offerView)
    }
}
