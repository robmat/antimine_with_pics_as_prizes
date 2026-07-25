package com.batodev.antimine

import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.pressBack
import androidx.test.espresso.NoActivityResumedException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import org.hamcrest.Matcher
import org.junit.Assert.assertEquals

// Shared across MainActivityTest/GameActivityTest/GalleryActivityTest.
//
// This is the real, large open-source "AntiMine" project (MVVM, Koin DI, 20
// Gradle modules) with a thin com.batodev.antimine "pictures as prizes"
// layer bolted on - not a small custom app like most others in this
// workspace. Scoped proportionally: core navigation and the prize-gallery
// mechanic get real coverage; the many secondary screens (themes, controls,
// stats, history, language, Play Games, about) and actual minesweeper
// gameplay do not. Gameplay specifically is not attempted at all - the
// board itself is rendered via a LibGDX/OpenGL fragment (GameRenderFragment),
// not individual Android Views, so there's no Espresso-addressable per-cell
// target the way there is in every other "solve it for real" app covered
// in this pass.
//
// MainActivity.handleBackPressed() calls finishAffinity() (no confirmation
// dialog, no exitProcess()/System.exit()) once the difficulty picker isn't
// showing - unlike android_tetris's HideStatusBarActivity, there's no wrong
// button that could kill the instrumentation process, so the standard
// press-back-and-expect-DESTROYED pattern is safe to use directly.

fun assertEventuallyDestroyed(scenario: ActivityScenario<*>, timeoutMs: Long = 8_000) {
    val deadline = System.currentTimeMillis() + timeoutMs
    while (scenario.state != Lifecycle.State.DESTROYED && System.currentTimeMillis() < deadline) {
        Thread.sleep(50)
    }
    assertEquals(Lifecycle.State.DESTROYED, scenario.state)
}

fun assertBackPressFinishesScenario(scenario: ActivityScenario<*>) {
    try {
        pressBack()
    } catch (expected: NoActivityResumedException) {
    }
    assertEventuallyDestroyed(scenario)
}

/**
 * Menu navigation (StartNewGameEvent etc.) flows through a Kotlin Flow-based
 * MainViewModel side-effect channel rather than a direct synchronous
 * startActivity() call - a small settle wait after triggering it is more
 * robust than assuming Espresso's default idling always catches up with
 * coroutine-dispatched navigation immediately.
 */
fun waitFor(millis: Long): ViewAction = object : ViewAction {
    override fun getConstraints(): Matcher<View> = isRoot()
    override fun getDescription(): String = "wait for ${millis}ms while pumping the main looper"
    override fun perform(uiController: UiController, view: View) {
        uiController.loopMainThreadForAtLeast(millis)
    }
}

/**
 * GameActivity.onOpenAppActions() shows a "Do you know how to play
 * minesweeper?" AlertDialog on eligible launches - not relevant to what
 * these tests verify, so it's dismissed via its negative "Close" button
 * whenever it's actually showing. Implemented as a raw findViewById() check
 * inside a single ViewAction (rather than onView(withId(...)).perform(click())
 * wrapped in try/catch) so it costs nothing when the dialog isn't showing -
 * Espresso's default root picker already resolves isRoot() to the dialog's
 * own root once it's focused, so this reaches the dialog's Close button
 * directly without waiting out a "view never appears" matcher timeout.
 */
fun dismissTutorialDialogIfShown(): ViewAction = object : ViewAction {
    override fun getConstraints(): Matcher<View> = isRoot()
    override fun getDescription(): String =
        "dismiss the 'do you know how to play' tutorial prompt dialog via its Close button, if currently showing"
    override fun perform(uiController: UiController, view: View) {
        val closeButton = view.findViewById<View>(android.R.id.button2)
        if (closeButton != null && closeButton.isShown) {
            closeButton.performClick()
            uiController.loopMainThreadUntilIdle()
        }
    }
}
