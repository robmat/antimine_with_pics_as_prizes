package dev.lucasnlm.antimine.wear.game

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import com.badlogic.gdx.backends.android.AndroidFragmentApplication
import dev.lucasnlm.antimine.common.level.view.GameRenderFragment
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModel
import dev.lucasnlm.antimine.common.level.viewmodel.changeSwitchControlAction
import dev.lucasnlm.antimine.common.level.viewmodel.loadGame
import dev.lucasnlm.antimine.common.level.viewmodel.loadLastGame
import dev.lucasnlm.antimine.common.level.viewmodel.retryGame
import dev.lucasnlm.antimine.common.level.viewmodel.startGameFromDifficultyQueryParam
import dev.lucasnlm.antimine.common.level.viewmodel.startNewGame
import dev.lucasnlm.antimine.core.models.Difficulty
import dev.lucasnlm.antimine.core.serializableNonSafe
import dev.lucasnlm.antimine.preferences.PreferencesRepositoryImpl
import dev.lucasnlm.antimine.preferences.models.Action
import dev.lucasnlm.antimine.preferences.models.ControlStyle
import dev.lucasnlm.antimine.ui.ext.ThemedActivity
import dev.lucasnlm.antimine.wear.databinding.ActivityGameBinding
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * [applyGameState] and its rendering-related siblings were split out into
 * WearGameActivityRendering.kt since this class's function count was over
 * detekt's threshold; [binding], [gameViewModel], [preferencesRepository]
 * and [refreshSwitchButtons] are `internal` rather than `private` only
 * because those extension functions, living outside the class body, need
 * access to them.
 */
class WearGameActivity :
    ThemedActivity(),
    AndroidFragmentApplication.Callbacks {
    internal lateinit var binding: ActivityGameBinding

    internal val gameViewModel by viewModel<GameViewModel>()
    internal val preferencesRepository: PreferencesRepositoryImpl by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        onBackPressedDispatcher.addCallback {
            finish()
        }

        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadGameFragment()
        bindViewModel()
    }

    @Suppress("UnnecessaryVariable")
    private fun View.tryHandleMotionEvent(event: MotionEvent?): Boolean {
        val shouldHandle =
            if (event == null) {
                false
            } else {
                val location = IntArray(2)
                getLocationOnScreen(location)
                val viewX = location[0]
                val viewY = location[1]

                val left = viewX
                val right = viewX + width
                val top = viewY
                val bottom = viewY + height

                val rect = Rect(left, top, right, bottom)
                rect.contains(event.x.toInt(), event.y.toInt())
            }

        return shouldHandle && dispatchTouchEvent(event)
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        val handledByView =
            listOf(
                binding.close,
                binding.selectFlag,
                binding.selectOpen,
                binding.newGame,
            ).firstOrNull {
                it.tryHandleMotionEvent(event)
            } != null

        return handledByView || binding.levelContainer.dispatchTouchEvent(event)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean = true

    override fun onResume() {
        super.onResume()
        refreshSwitchButtons()
    }

    internal fun refreshSwitchButtons() {
        val enabled = preferencesRepository.controlStyle() == ControlStyle.SwitchMarkOpen

        binding.close.setOnClickListener {
            finish()
        }

        val switchControlAction = preferencesRepository.getSwitchControlAction()

        binding.selectOpen.apply {
            isVisible = enabled
            alpha = if (switchControlAction == Action.OpenTile) 1.0f else INACTIVE_ACTION_ALPHA
            setOnClickListener {
                gameViewModel.changeSwitchControlAction(Action.OpenTile)
            }
        }

        binding.selectFlag.apply {
            isVisible = enabled
            alpha = if (switchControlAction == Action.SwitchMark) 1.0f else INACTIVE_ACTION_ALPHA
            setOnClickListener {
                gameViewModel.changeSwitchControlAction(Action.SwitchMark)
            }
        }
    }

    private fun loadGameFragment() {
        supportFragmentManager.commit(allowStateLoss = true) {
            val fragment = GameRenderFragment()
            replace(binding.levelContainer.id, fragment)
            handleIntent(intent)

            binding.levelContainer.setChildFragment(fragment)
        }
    }

    private fun handleIntent(intent: Intent) {
        lifecycleScope.launch {
            val extras = intent.extras ?: Bundle()

            if (gameViewModel.startGameFromDifficultyQueryParam(this@WearGameActivity, intent)) {
                return@launch
            }

            when {
                extras.containsKey(DIFFICULTY) -> {
                    intent.removeExtra(DIFFICULTY)
                    val difficulty = extras.serializableNonSafe<Difficulty>(DIFFICULTY)
                    gameViewModel.startNewGame(this@WearGameActivity, difficulty)
                }

                extras.containsKey(NEW_GAME) -> {
                    intent.removeExtra(NEW_GAME)
                    gameViewModel.startNewGame(this@WearGameActivity)
                }

                extras.containsKey(RETRY_GAME) -> {
                    val uid = extras.getInt(RETRY_GAME)
                    gameViewModel.retryGame(uid, this@WearGameActivity)
                }

                extras.containsKey(START_GAME) -> {
                    val uid = extras.getInt(START_GAME)
                    gameViewModel.loadGame(uid, this@WearGameActivity)
                }

                else -> {
                    gameViewModel.loadLastGame(this@WearGameActivity)
                }
            }
        }
    }

    private fun bindViewModel() =
        gameViewModel.apply {
            lifecycleScope.launchWhenCreated {
                observeState().collect { state ->
                    applyGameState(state)
                }
            }

            lifecycleScope.launchWhenCreated {
                gameViewModel.observeSideEffects().collect { event ->
                    handleGameEvent(event)
                }
            }
        }

    override fun exit() {
        // LibGDX exit callback
    }

    companion object {
        const val NEW_GAME = "new_game"
        const val DIFFICULTY = "difficulty"
        const val START_GAME = "start_game"
        const val RETRY_GAME = "retry_game"
        const val INACTIVE_ACTION_ALPHA = 0.5f
        const val LOADING_INDICATOR_DELAY_MS = 500L
        const val TIMER_BLINK_PERIOD_SECONDS = 10
        const val TIMER_BLINK_VISIBLE_SECONDS = 2
        const val TIMER_ALPHA = 0.7f
    }
}
