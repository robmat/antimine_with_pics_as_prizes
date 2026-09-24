package com.batodev.antimine

import android.app.Activity
import android.app.Instrumentation.ActivityResult
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasType
import androidx.test.espresso.intent.matcher.IntentMatchers.isInternal
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

// GalleryActivity reads SettingsHelper(this).preferences.uncoveredPics in
// onResume(), guarded by an isEmpty() check (unlike some sibling apps in
// this workspace, an empty list here just leaves the PhotoView unset rather
// than recursing/crashing) - still seeded with real assets/prize-images
// filenames for a deterministic, realistic test. Reached directly via
// Intent here rather than through MainActivity's own gallery button, since
// that button's visibility depends on the build flavor (always visible on
// foss, gated behind IAP/billing on google/googleInstant) - orthogonal to
// what's actually being tested.
@RunWith(AndroidJUnit4::class)
class GalleryActivityTest {
    private lateinit var images: List<String>

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        images = context.assets.list(PRIZE_IMAGES)!!.take(3)
        context
            .getSharedPreferences("SettingsHelper", android.content.Context.MODE_PRIVATE)
            .edit()
            .putString("uncoveredPics", images.joinToString(","))
            .putInt("lastSeenGalleryPic", 0)
            .apply()
        Intents.init()
        // Espresso-Intents only *records* outgoing intents; anything not matched by an intending()
        // stub is still dispatched for real. On a physical device that opens the Play Store on top
        // of the app, so the next interaction fails with NoActivityResumedException - or the run
        // wedges until the harness watchdog kills it. Stub external intents only: the app's own
        // activity launches must still happen, since tests assert on the screens they open.
        intending(not(isInternal())).respondWith(ActivityResult(Activity.RESULT_OK, null))
    }

    @After
    fun releaseIntents() {
        Intents.release()
    }

    private fun launch(): ActivityScenario<GalleryActivity> = ActivityScenario.launch(GalleryActivity::class.java)

    @Test
    fun launchesShowingFirstImage() {
        val scenario = launch()

        onView(withId(R.id.gallery_activity_background)).check(matches(isDisplayed()))
        scenario.close()
    }

    @Test
    fun leftClickAtFirstImageNoOpsWithoutCrashing() {
        val scenario = launch()

        onView(withId(R.id.gallery_left)).perform(click())

        onView(withId(R.id.gallery_activity_background)).check(matches(isDisplayed()))
        scenario.close()
    }

    @Test
    fun rightClickPastLastImageNoOpsWithoutCrashing() {
        val scenario = launch()

        repeat(images.size + 1) {
            onView(withId(R.id.gallery_right)).perform(click())
        }

        onView(withId(R.id.gallery_activity_background)).check(matches(isDisplayed()))
        scenario.close()
    }

    @Test
    fun shareButtonSharesCurrentImage() {
        val scenario = launch()

        onView(withId(R.id.gallery_share_btn)).perform(click())

        intended(hasAction(Intent.ACTION_SEND))
        intended(hasType("image/*"))
        scenario.close()
    }

    @Test
    fun backButtonFinishesActivity() {
        val scenario = launch()

        onView(withId(R.id.gallery_back_btn)).perform(click())

        assertEventuallyDestroyed(scenario)
    }

    @Test
    fun systemBackPressFinishesActivity() {
        val scenario = launch()

        assertBackPressFinishesScenario(scenario)
    }
}
