package dev.lucasnlm.antimine.control.viewmodel

import dev.lucasnlm.antimine.control.models.ControlDetails
import dev.lucasnlm.antimine.core.haptic.HapticFeedbackManager
import dev.lucasnlm.antimine.core.viewmodel.IntentViewModel
import dev.lucasnlm.antimine.preferences.PreferencesRepository
import dev.lucasnlm.antimine.preferences.models.ControlStyle
import kotlinx.coroutines.flow.flow
import dev.lucasnlm.antimine.i18n.R as i18n

class ControlViewModel(
    private val preferencesRepository: PreferencesRepository,
    private val hapticFeedbackManager: HapticFeedbackManager,
) : IntentViewModel<ControlEvent, ControlState>() {

    private val gameControlOptions =
        listOf(
            ControlDetails(
                id = 0L,
                controlStyle = ControlStyle.Standard,
                firstActionId = i18n.string.single_click,
                firstActionResponseId = i18n.string.open_tile,
                secondActionId = i18n.string.long_press,
                secondActionResponseId = i18n.string.flag_tile,
            ),
            ControlDetails(
                id = 1L,
                controlStyle = ControlStyle.FastFlag,
                firstActionId = i18n.string.single_click,
                firstActionResponseId = i18n.string.flag_tile,
                secondActionId = i18n.string.long_press,
                secondActionResponseId = i18n.string.open_tile,
            ),
            ControlDetails(
                id = 2L,
                controlStyle = ControlStyle.DoubleClick,
                firstActionId = i18n.string.single_click,
                firstActionResponseId = i18n.string.flag_tile,
                secondActionId = i18n.string.double_click,
                secondActionResponseId = i18n.string.open_tile,
            ),
            ControlDetails(
                id = 3L,
                controlStyle = ControlStyle.DoubleClickInverted,
                firstActionId = i18n.string.single_click,
                firstActionResponseId = i18n.string.open_tile,
                secondActionId = i18n.string.double_click,
                secondActionResponseId = i18n.string.flag_tile,
            ),
            ControlDetails(
                id = 4L,
                controlStyle = ControlStyle.SwitchMarkOpen,
                firstActionId = i18n.string.switch_control_desc,
                firstActionResponseId = 0,
                secondActionId = 0,
                secondActionResponseId = 0,
            ),
        )

    private fun hasChangedPreferences(): Boolean {
        return preferencesRepository.hasControlCustomizations()
    }

    override fun initialState(): ControlState {
        val controlDetails =
            gameControlOptions.firstOrNull {
                it.controlStyle == preferencesRepository.controlStyle()
            }
        return ControlState(
            touchSensibility = preferencesRepository.touchSensibility(),
            longPress = preferencesRepository.customLongPressTimeout().toInt(),
            doubleClick = preferencesRepository.getDoubleClickTimeout().toInt(),
            selected = controlDetails?.controlStyle ?: ControlStyle.Standard,
            controls = gameControlOptions,
            hapticFeedbackLevel = preferencesRepository.getHapticFeedbackLevel(),
            showReset = hasChangedPreferences(),
        )
    }

    override suspend fun mapEventToState(event: ControlEvent) =
        flow {
            val newState =
                when (event) {
                    is ControlEvent.UpdateHapticFeedbackLevel -> onUpdateHapticFeedbackLevel(event.value)
                    is ControlEvent.UpdateDoubleClick -> onUpdateDoubleClick(event.value)
                    is ControlEvent.UpdateLongPress -> onUpdateLongPress(event.value)
                    is ControlEvent.UpdateTouchSensibility -> onUpdateTouchSensibility(event.value)
                    is ControlEvent.Reset -> onReset()
                    is ControlEvent.SelectControlStyle -> onSelectControlStyle(event.controlStyle)
                }
            emit(newState)
        }

    private fun onUpdateHapticFeedbackLevel(value: Int): ControlState {
        val coercedValue = value.coerceIn(0, MAX_HAPTIC_VALUE)
        preferencesRepository.setHapticFeedbackLevel(coercedValue)
        hapticFeedbackManager.longPressFeedback()
        preferencesRepository.setHapticFeedback(coercedValue != 0)
        return state.copy(showReset = hasChangedPreferences())
    }

    private fun onUpdateDoubleClick(value: Int): ControlState {
        preferencesRepository.setDoubleClickTimeout(value.toLong())
        return state.copy(doubleClick = value, showReset = hasChangedPreferences())
    }

    private fun onUpdateLongPress(value: Int): ControlState {
        preferencesRepository.setCustomLongPressTimeout(value.toLong())
        return state.copy(longPress = value, showReset = hasChangedPreferences())
    }

    private fun onUpdateTouchSensibility(value: Int): ControlState {
        preferencesRepository.setTouchSensibility(value)
        return state.copy(touchSensibility = value, showReset = hasChangedPreferences())
    }

    private fun onReset(): ControlState {
        preferencesRepository.resetControls()
        return state.copy(
            longPress = preferencesRepository.customLongPressTimeout().toInt(),
            touchSensibility = preferencesRepository.touchSensibility(),
            doubleClick = preferencesRepository.getDoubleClickTimeout().toInt(),
            showReset = hasChangedPreferences(),
        )
    }

    private fun onSelectControlStyle(controlStyle: ControlStyle): ControlState {
        preferencesRepository.useControlStyle(controlStyle)
        val selected = state.controls.first { it.controlStyle == controlStyle }
        return state.copy(selected = selected.controlStyle)
    }

    companion object {
        const val MAX_HAPTIC_VALUE = 200
    }
}
