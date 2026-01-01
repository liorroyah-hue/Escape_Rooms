package com.example.escape_rooms;

import android.content.Intent;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.escape_rooms.ui.MainActivity;

import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertTrue;

/**
 * Instrumented integration test for MainActivity game flow.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class GameFlowIntegrationTest {

    @Test
    public void testRoomOne_InitialState() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class);
        intent.putExtra(MainActivity.EXTRA_LEVEL, 1);
        
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.questions_recycler_view))
                    .check(matches(isDisplayed()))
                    .check(matches(hasDescendant(withText("מהו צבע השמיים?"))));
            
            onView(withId(R.id.btn_submit_answers)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void testSubmitWithoutAnswering_StaysOnScreen() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class);
        intent.putExtra(MainActivity.EXTRA_LEVEL, 1);
        
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.btn_submit_answers)).perform(click());
            onView(withId(R.id.questions_recycler_view)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void testSubmitCorrectAnswers_NavigatesToNextLevel() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class);
        intent.putExtra(MainActivity.EXTRA_LEVEL, 1);
        
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(intent)) {
            // 1. Select "כחול" (Blue)
            onView(withId(R.id.questions_recycler_view))
                    .perform(RecyclerViewActions.scrollTo(hasDescendant(withText("מהו צבע השמיים?"))));
            onView(allOf(withText("כחול"), isDisplayed())).perform(click());

            // 2. Select "חמצן" (Oxygen)
            onView(withId(R.id.questions_recycler_view))
                    .perform(RecyclerViewActions.scrollTo(hasDescendant(withText("איזה גז בני אדם צריכים כדי לנשום?"))));
            onView(allOf(withText("חמצן"), isDisplayed())).perform(click());
            
            // 3. Trigger navigation
            onView(withId(R.id.btn_submit_answers)).perform(click());

            // 4. Wait for idle to allow transition to begin
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();

            // 5. Verify that the activity is in a finishing or destroyed state
            // We use scenario.getState() instead of onActivity to avoid NoActivityResumedException
            Lifecycle.State state = scenario.getState();
            assertTrue("Activity should be finishing or gone. Current state: " + state,
                    state == Lifecycle.State.DESTROYED || state == Lifecycle.State.CREATED);
        }
    }
}
