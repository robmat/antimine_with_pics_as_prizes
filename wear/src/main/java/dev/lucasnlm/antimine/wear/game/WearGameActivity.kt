package dev.lucasnlm.antimine.wear.game

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.text.format.DateUtils
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import com.badlogic.gdx.backends.android.AndroidFragmentApplication
import dev.lucasnlm.antimine.common.level.view.GameRenderFragment
import dev.lucasnlm.antimine.common.level.viewmodel.GameEvent
import dev.lucasnlm.antimine.common.level.viewmodel.GameState
import dev.lucasnlm.antimine.common.level.viewmodel.GameViewModel
import dev.lucasnlm.antimine.core.models.Difficulty
import dev.lucasnlm.antimine.core.serializableNonSafe
import dev.lucasnlm.antimine.preferences.PreferencesRepositoryImpl
import dev.lucasnlm.antimine.preferences.models.Action
import dev.lucasnlm.antimine.preferences.models.ControlStyle
import dev.lucasnlm.antimine.ui.ext.ThemedActivity
import dev.lucasnlm.antimine.wear.databinding.ActivityGameBinding
import dev.lucasnlm.antimine.wear.message.GameOverActivity
import dev.lucasnlm.antimine.wear.message.VictoryActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import dev.lucasnlm.antimine.i18n.R as i18n

class WearGameActivity : ThemedActivity(), AndroidFragmentApplication.Callbacks {
    private lateinit var binding: ActivityGameBinding

    private val gameViewModel by viewModel<GameViewModel>()
    private val preferencesRepository: PreferencesRepositoryImpl by inject()

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

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return true
    }

    override fun onResume() {
        super.onResume()
        refreshSwitchButtons()
    }

    private fun refreshSwitchButtons() {
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
            val queryParamDifficulty = intent.data?.getQueryParameter("difficulty")
            when {
                queryParamDifficulty != null -> {
                    val upperDifficulty = queryParamDifficulty.uppercase()
                    val difficulty = Difficulty.values().firstOrNull { it.id == upperDifficulty }
                    if (difficulty == null) {
                        gameViewModel.loadLastGame(this@WearGameActivity)
                    } else {
                        gameViewModel.startNewGame(this@WearGameActivity, difficulty)
                    }
                }
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

    private fun applyGameState(state: GameState) {
        updateTapToBeginLabel(state)

        if (state.isCreatingGame) {
            lifecycleScope.launch {
                // Show loading indicator only when it takes more than:
                delay(LOADING_INDICATOR_DELAY_MS)
                if (gameViewModel.singleState().isCreatingGame) {
                    binding.loadingGame.show()
                }
            }
        } else if (binding.loadingGame.isVisible) {
            binding.loadingGame.hide()
        }

        updateTimerLabel(state)

        if (state.isGameCompleted) {
            binding.newGame.setOnClickListener {
                lifecycleScope.launch {
                    gameViewModel.startNewGame(this@WearGameActivity)
                }
            }
            binding.newGame.isVisible = true
        } else {
            binding.newGame.isVisible = false
        }

        keepScreenOn(state.isActive)
        refreshSwitchButtons()
    }

    private fun updateTapToBeginLabel(state: GameState) {
        val hasNoProgress = state.saveId == 0L || state.isLoadingMap || state.isCreatingGame
        val shouldShowTapToBegin = state.turn == 0 && hasNoProgress
        if (shouldShowTapToBegin) {
            binding.tapToBegin.apply {
                text =
                    when {
                        state.isCreatingGame -> getString(i18n.string.creating_valid_game)
                        state.isLoadingMap -> getString(i18n.string.loading)
                        else -> getString(i18n.string.tap_to_begin)
                    }
                isVisible = true
            }
        } else {
            binding.tapToBegin.isVisible = false
        }
    }

    private fun updateTimerLabel(state: GameState) {
        if (state.duration % TIMER_BLINK_PERIOD_SECONDS > TIMER_BLINK_VISIBLE_SECONDS) {
            binding.timer.apply {
                isVisible = preferencesRepository.showTimer()
                alpha = TIMER_ALPHA
                text = DateUtils.formatElapsedTime(state.duration)
            }
        } else if (state.duration > 0) {
            binding.timer.apply {
                text = getString(i18n.string.mines_remaining, state.mineCount)
            }
        } else {
            binding.timer.isVisible = false
        }
    }

    private fun handleGameEvent(event: GameEvent) {
        when (event) {
            is GameEvent.ShowNoGuessFailWarning -> {}
            is GameEvent.ShowNewGameDialog -> {}
            is GameEvent.VictoryDialog -> startDialogActivity(VictoryActivity::class.java)
            is GameEvent.GameOverDialog -> startDialogActivity(GameOverActivity::class.java)
            is GameEvent.GameCompleteDialog -> startDialogActivity(VictoryActivity::class.java)
            else -> {
                // Empty
            }
        }
    }

    private fun startDialogActivity(activityClass: Class<*>) {
        val intent =
            Intent(applicationContext, activityClass).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        startActivity(intent)
    }

    private fun keepScreenOn(enabled: Boolean) {
        if (enabled) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
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
