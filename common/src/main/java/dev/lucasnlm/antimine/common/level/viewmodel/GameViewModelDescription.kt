package dev.lucasnlm.antimine.common.level.viewmodel

import android.content.Context
import android.text.SpannedString
import android.util.LayoutDirection
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.text.layoutDirection
import dev.lucasnlm.antimine.preferences.models.ControlStyle
import java.util.Locale
import dev.lucasnlm.antimine.i18n.R as i18n

/**
 * Control-scheme description text shown to the player, split out of
 * [GameViewModel] - see its class doc.
 */
private data class ControlDescriptionLabels(
    val openAction: String,
    val openReaction: String,
    val flagAction: String,
    val flagReaction: String,
)

private fun GameViewModel.controlDescriptionLabels(context: Context): ControlDescriptionLabels? =
    when (preferencesRepository.controlStyle()) {
        ControlStyle.Standard ->
            ControlDescriptionLabels(
                openAction = context.getString(i18n.string.single_click),
                openReaction = context.getString(i18n.string.open),
                flagAction = context.getString(i18n.string.long_press),
                flagReaction = context.getString(i18n.string.flag_tile),
            )
        ControlStyle.FastFlag ->
            ControlDescriptionLabels(
                openAction = context.getString(i18n.string.single_click),
                openReaction = context.getString(i18n.string.flag_tile),
                flagAction = context.getString(i18n.string.long_press),
                flagReaction = context.getString(i18n.string.open),
            )
        ControlStyle.DoubleClick ->
            ControlDescriptionLabels(
                openAction = context.getString(i18n.string.single_click),
                openReaction = context.getString(i18n.string.flag_tile),
                flagAction = context.getString(i18n.string.double_click),
                flagReaction = context.getString(i18n.string.open),
            )
        ControlStyle.DoubleClickInverted ->
            ControlDescriptionLabels(
                openAction = context.getString(i18n.string.single_click),
                openReaction = context.getString(i18n.string.open),
                flagAction = context.getString(i18n.string.double_click),
                flagReaction = context.getString(i18n.string.flag_tile),
            )
        else -> {
            // With switch button, it doesn't require toast
            null
        }
    }

private fun formatActionReaction(
    action: String,
    reaction: String,
    isLeftToRight: Boolean,
): SpannedString =
    buildSpannedString {
        if (isLeftToRight) {
            bold { append(action) }
            append(" - ")
            append(reaction)
        } else {
            bold { append(reaction) }
            append(" - ")
            append(action)
        }
    }

fun GameViewModel.getControlDescription(context: Context): SpannedString? {
    val labels = controlDescriptionLabels(context) ?: return null
    val isLeftToRight = Locale.getDefault().layoutDirection == LayoutDirection.LTR

    val first = formatActionReaction(labels.openAction, labels.openReaction, isLeftToRight)
    val second = formatActionReaction(labels.flagAction, labels.flagReaction, isLeftToRight)

    return buildSpannedString {
        append(first)
        appendLine()
        append(second)
        appendLine()
        append(context.getString(i18n.string.tap_to_customize))
    }
}

internal fun GameViewModel.refreshField() {
    sendEvent(GameEvent.UpdateMinefield(gameController.field()))
}
