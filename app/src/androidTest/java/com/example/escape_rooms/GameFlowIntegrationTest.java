package com.example.escape_rooms;

import android.content.Intent;
import android.view.View;

import androidx.lifecycle.Lifecycle;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.ViewAssertion;
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
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class GameFlowIntegrationTest {

    @Test
    public void testRoomOne_InitialState() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class);
        intent.putExtra(MainActivity.EXTRA_LEVEL, 1);
        
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(intent)) {
            // Wait for RecyclerView content using a more reliable check
            waitForAdapterPopulated(R.id.questions_recycler_view, 15000);

            onView(withId(R.id.questions_recycler_view))
                    .check(matches(isDisplayed()))
                    .check(matches(hasDescendant(withText("מהו צבע השמיים?"))));
            
            onView(withId(R.id.btn_submit_answers)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void testSubmitCorrectAnswers_NavigatesToNextLevel() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class);
        intent.putExtra(MainActivity.EXTRA_LEVEL, 1);
        
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(intent)) {
            waitForAdapterPopulated(R.id.questions_recycler_view, 15000);

            // Select correct answers (Hebrew)
            onView(withText("כחול")).perform(click());
            onView(withText("חמצן")).perform(click());
            
            // Trigger navigation
            onView(withId(R.id.btn_submit_answers)).perform(click());

            InstrumentationRegistry.getInstrumentation().waitForIdleSync();

            Lifecycle.State state = scenario.getState();
            assertTrue("Activity should be finishing or gone. Current state: " + state,
                    state == Lifecycle.State.DESTROYED || state == Lifecycle.State.CREATED);
        }
    }

    /**
     * Helper that waits for the RecyclerView adapter to have items.
     * Prevents NullPointerException when Espresso tries to perform actions on a list that hasn't loaded yet.
     */
    private void waitForAdapterPopulated(int viewId, long timeout) {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < timeout) {
            try {
                onView(withId(viewId)).check(new ViewAssertion() {
                    @Override
                    public void check(View view, NoMatchingViewException noViewFoundException) {
                        if (noViewFoundException != null) throw noViewFoundException;
                        RecyclerView rv = (RecyclerView) view;
                        if (rv.getAdapter() == null || rv.getAdapter().getItemCount() == 0) {
                            throw new RuntimeException("Adapter not populated yet");
                        }
                    }
                });
                return; // Populated!
            } catch (Exception | Error e) {
                try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
            }
        }
        throw new RuntimeException("Timeout waiting for RecyclerView to populate");
    }
}
