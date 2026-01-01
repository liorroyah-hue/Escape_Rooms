package com.example.escape_rooms;

import android.content.Intent;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.contrib.RecyclerViewActions;

import com.example.escape_rooms.ui.MainActivity;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertTrue;

/**
 * Robolectric-based integration test for MainActivity game flow.
 */
@RunWith(RobolectricTestRunner.class)
public class GameFlowIntegrationTest {

    @Test
    public void testRoomOne_InitialState() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class);
        intent.putExtra(MainActivity.EXTRA_LEVEL, 1);
        
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.questions_recycler_view))
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
            // Force scenario to RESUMED state to ensure Espresso can interact with it
            scenario.moveToState(Lifecycle.State.RESUMED);

            // 1. Click "כחול" (Blue)
            onView(withId(R.id.questions_recycler_view))
                    .perform(RecyclerViewActions.scrollTo(hasDescendant(withText("מהו צבע השמיים?"))));
            onView(withText("כחול")).perform(click());

            // 2. Click "חמצן" (Oxygen)
            onView(withId(R.id.questions_recycler_view))
                    .perform(RecyclerViewActions.scrollTo(hasDescendant(withText("איזה גז בני אדם צריכים כדי לנשום?"))));
            onView(withText("חמצן")).perform(click());
            
            // 3. Click submit
            onView(withId(R.id.btn_submit_answers)).perform(click());

            // 4. Verify activity is transitioning
            scenario.onActivity(activity -> {
                assertTrue("Activity should be finishing to load next room", 
                        activity.isFinishing() || activity.isDestroyed());
            });
        }
    }
}
