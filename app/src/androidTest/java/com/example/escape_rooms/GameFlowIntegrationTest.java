package com.example.escape_rooms;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

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

@RunWith(AndroidJUnit4.class)
@LargeTest
public class GameFlowIntegrationTest {

    @Test
    public void testRoomOne_InitialState() {
        // Start MainActivity for Level 1
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class);
        intent.putExtra(MainActivity.EXTRA_LEVEL, 1);
        
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(intent)) {
            // Verify Room 1 questions are loaded (based on questions.json)
            onView(withId(R.id.questions_recycler_view))
                    .check(matches(hasDescendant(withText("What is the color of the sky?"))));
            
            // Verify submit button is visible
            onView(withId(R.id.btn_submit_answers)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void testSubmitWithoutAnswering_StaysOnScreen() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class);
        intent.putExtra(MainActivity.EXTRA_LEVEL, 1);
        
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(intent)) {
            // Click submit without selecting answers
            onView(withId(R.id.btn_submit_answers)).perform(click());
            
            // Verify we are still on the same screen (RecyclerView is still there)
            onView(withId(R.id.questions_recycler_view)).check(matches(isDisplayed()));
        }
    }
}
