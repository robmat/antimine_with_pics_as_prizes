package com.batodev.antimine

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.batodev.antimine.main.MainActivity
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

// Covers the core MainActivity flow: menu launches, starting a game reaches
// real gameplay (GameActivity), and back-press exits. The many secondary
// buttons (themes, controls, stats, history, language, Play Games, about)
// are not covered - each opens its own real, otherwise-untested screen with
// its own DI dependencies, and none of them are the "pictures as prizes"
// mechanic this app was customized for.
@RunWith(AndroidJUnit4::class)
class MainActivityTest {
    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        context
            .getSharedPreferences("SettingsHelper", android.content.Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }

    @Test
    fun launchesShowingMainMenu() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)

        onView(withId(R.id.newGameShow)).check(matches(isDisplayed()))
        scenario.close()
    }

    @Test
    fun startingABeginnerGameReachesGameActivity() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)

        onView(withId(R.id.newGameShow)).perform(click())
        onView(withId(R.id.startBeginner)).perform(click())
        onView(isRoot()).perform(waitFor(1_000))
        onView(isRoot()).perform(dismissTutorialDialogIfShown())

        onView(withId(R.id.back)).check(matches(isDisplayed()))
        scenario.close()
    }

    @Test
    fun systemBackPressExitsFromTheMainMenu() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)

        assertBackPressFinishesScenario(scenario)
    }
}
