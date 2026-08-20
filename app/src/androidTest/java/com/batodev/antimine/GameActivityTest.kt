package com.batodev.antimine

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.lucasnlm.antimine.core.models.Difficulty
import org.junit.Test
import org.junit.runner.RunWith

// GameActivity's board itself is rendered via a LibGDX/OpenGL fragment
// (GameRenderFragment), not individual Android Views, so there's no
// Espresso-addressable per-cell target to actually play a real game with -
// this only verifies the Activity launches into a real Beginner game
// without crashing and that back-press (a plain finish(), no confirmation
// dialog) works safely.
@RunWith(AndroidJUnit4::class)
class GameActivityTest {
    private fun launchWithDifficulty(): ActivityScenario<GameActivity> {
        val intent =
            Intent(
                InstrumentationRegistry.getInstrumentation().targetContext,
                GameActivity::class.java,
            ).apply { putExtra(GameActivity.DIFFICULTY, Difficulty.Beginner) }
        return ActivityScenario.launch(intent)
    }

    @Test
    fun launchesShowingGameToolbar() {
        val scenario = launchWithDifficulty()
        onView(isRoot()).perform(waitFor(1_000))
        onView(isRoot()).perform(dismissTutorialDialogIfShown())

        onView(withId(R.id.back)).check(matches(isDisplayed()))
        scenario.close()
    }

    @Test
    fun systemBackPressFinishesActivity() {
        val scenario = launchWithDifficulty()
        // Otherwise this can race the tutorial prompt dialog appearing -
        // pressBack() would dismiss the dialog instead of the Activity,
        // leaving it RESUMED instead of DESTROYED.
        onView(isRoot()).perform(waitFor(1_000))
        onView(isRoot()).perform(dismissTutorialDialogIfShown())

        assertBackPressFinishesScenario(scenario)
    }
}
