package dev.lucasnlm.antimine.gdx.models

data class GameInputCallbacks(
    val onSingleTap: (Int) -> Unit,
    val onDoubleTap: (Int) -> Unit,
    val onLongTap: (Int) -> Unit,
    val onEngineReady: () -> Unit,
)
