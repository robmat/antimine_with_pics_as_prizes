package dev.lucasnlm.antimine.gdx.stages

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import dev.lucasnlm.antimine.core.models.Area
import dev.lucasnlm.antimine.gdx.actors.AreaActor

/**
 * [MinefieldStage]'s actor-rebuilding/refresh logic, split out of the class
 * body - see its class doc.
 */
internal fun MinefieldStage.rebuildActors(boundAreas: List<Area>) {
    clear()
    if (boundAreas.size < actors.size) {
        root.children.shrink()
    } else {
        root.children.ensureCapacity(boundAreas.size)
    }

    boundAreas.forEach {
        addActor(
            AreaActor(
                theme = renderSettings.theme,
                size = renderSettings.areaSize,
                area = it,
                field = boundAreas,
                onInputEvent = { event -> handleGameEvent(event) },
                enableLigatures = renderSettings.joinAreas,
            ),
        )
    }
    refreshVisibleActorsIfNeeded()
}

internal fun MinefieldStage.updateExistingActors(boundAreas: List<Area>) {
    val reset = boundAreas.count { it.hasMine } == 0

    actors.forEach {
        if (it.isVisible) {
            val areaActor = (it as AreaActor)
            val area = boundAreas[areaActor.boundAreaId()]
            areaActor.bindArea(reset, renderSettings.joinAreas, area, boundAreas)
        }
    }
}

internal fun MinefieldStage.refreshAreas(forceRefresh: Boolean) {
    if (forceRefresh || forceRefreshVisibleAreas) {
        val boundAreas = newBoundAreas ?: this.boundAreas

        newBoundAreas?.let {
            this.boundAreas = it
            this.newBoundAreas = null
        }

        if (actors.size != boundAreas.size) {
            rebuildActors(boundAreas)
        } else {
            updateExistingActors(boundAreas)
        }

        callbacks.onEngineReady()

        forceRefreshVisibleAreas = false
        Gdx.graphics.requestRendering()
    }
}

internal fun MinefieldStage.refreshVisibleActorsIfNeeded(): Boolean {
    val camera = camera as OrthographicCamera
    val cameraChanged: Boolean = !camera.position.epsilonEquals(lastCameraPosition) || lastZoom != camera.zoom
    if (cameraChanged || forceRefreshVisibleAreas) {
        lastCameraPosition = camera.position.cpy()
        lastZoom = camera.zoom
    }
    return cameraChanged
}
