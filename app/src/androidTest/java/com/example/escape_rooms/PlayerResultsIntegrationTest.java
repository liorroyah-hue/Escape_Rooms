package com.example.escape_rooms;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.escape_rooms.ui.PlayerResultsActivity;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class PlayerResultsIntegrationTest {

    @Test
    public void testResultsDisplay_WithSampleData() {
        // Create sample timing data: Room 1 took 10 seconds, Room 2 took 6 seconds
        HashMap<Integer, Long> testTimings = new HashMap<>();
        testTimings.put(1, 10000L); // 00:10
        testTimings.put(2, 6000L);  // 00:06

        Context context = ApplicationProvider.getApplicationContext();
        Intent intent = new Intent(context, PlayerResultsActivity.class);
        intent.putExtra(PlayerResultsActivity.EXTRA_TIMINGS, testTimings);

        try (ActivityScenario<PlayerResultsActivity> scenario = ActivityScenario.launch(intent)) {
            // Verify Title
            onView(withText("MISSION DEBRIEF")).check(matches(isDisplayed()));

            // Verify Room 1 entry: Check if the row contains "Room 1: " and "00:10"
            onView(withId(R.id.results_container))
                    .check(matches(hasDescendant(allOf(withText("Room 1:  "), isDisplayed()))));
            onView(withId(R.id.results_container))
                    .check(matches(hasDescendant(allOf(withText("00:10"), isDisplayed()))));

            // Verify Total Time: 10s + 6s = 16s -> 00:16
            onView(withId(R.id.tv_total_time)).check(matches(withText("TOTAL TIME: 00:16")));
        }
    }
}
