package dev.lucasnlm.antimine.gdx.stages

import com.badlogic.gdx.Gdx
import dev.lucasnlm.antimine.gdx.events.GdxEvent

/**
 * [MinefieldStage]'s tap/touch-event bookkeeping logic, split out of the
 * class body - see its class doc.
 */
internal fun MinefieldStage.handleGameEvent(gdxEvent: GdxEvent) {
    if (inputEvents.firstOrNull { it.id != gdxEvent.id } != null) {
        inputEvents.clear()
    }

    if (inputEvents.firstOrNull { it is GdxEvent.TouchUpEvent } == null) {
        inputInit = System.currentTimeMillis()
    }

    if (gdxEvent is GdxEvent.TouchUpEvent && inputEvents.firstOrNull { it is GdxEvent.TouchDownEvent } == null) {
        // Ignore unpaired up event
        return
    }

    inputEvents.add(gdxEvent)
}

internal fun MinefieldStage.handleTapWithDoubleTapSupport(
    dt: Long,
    touchUpEvents: List<GdxEvent.TouchUpEvent>,
) {
    if (dt > actionSettings.doubleTapTimeout) {
        touchUpEvents.groupBy { it.id }
            .entries
            .first()
            .let {
                when (it.value.count()) {
                    1 -> callbacks.onSingleTap(it.key)
                    2 -> callbacks.onDoubleTap(it.key)
                    else -> {
                    }
                }
            }.also {
                inputEvents.clear()
            }
    }
}

internal fun MinefieldStage.handleTouchUp(
    dt: Long,
    touchUpEvents: List<GdxEvent.TouchUpEvent>,
    touchDownEvents: List<GdxEvent.TouchDownEvent>,
) {
    if (touchUpEvents.size == touchDownEvents.size) {
        if (actionSettings.handleDoubleTaps) {
            handleTapWithDoubleTapSupport(dt, touchUpEvents)
        } else {
            touchUpEvents.map { it.id }
                .first()
                .run(callbacks.onSingleTap)
                .also {
                    inputEvents.clear()
                }
        }
    }

    Gdx.graphics.requestRendering()
}

internal fun MinefieldStage.handleTouchDown(
    dt: Long,
    touchDownEvents: List<GdxEvent.TouchDownEvent>,
) {
    if (dt > actionSettings.longTapTimeout) {
        touchDownEvents.map { it.id }
            .first()
            .run(callbacks.onLongTap)
            .also {
                inputEvents.clear()
            }
    }

    Gdx.graphics.requestRendering()
}

internal fun MinefieldStage.checkGameTouchInput(now: Long) {
    if (inputEvents.isEmpty()) {
        return
    }

    val dt = now - inputInit
    val touchUpEvents = inputEvents.filterIsInstance<GdxEvent.TouchUpEvent>()
    val touchDownEvents = inputEvents.filterIsInstance<GdxEvent.TouchDownEvent>()

    if (touchUpEvents.isNotEmpty()) {
        handleTouchUp(dt, touchUpEvents, touchDownEvents)
    } else if (touchDownEvents.isNotEmpty()) {
        handleTouchDown(dt, touchDownEvents)
    }
}
