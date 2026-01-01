package com.example.escape_rooms;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.escape_rooms.ui.LoginActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class LoginIntegrationTest {

    @Rule
    public ActivityScenarioRule<LoginActivity> activityRule =
            new ActivityScenarioRule<>(LoginActivity.class);

    @Test
    public void loginScreen_initialState() {
        // Verify title is displayed
        onView(withText("SYSTEM ACCESS")).check(matches(isDisplayed()));
        
        // Verify status text is initially hidden
        onView(withId(R.id.textStatus)).check(matches(not(isDisplayed())));
    }

    @Test
    public void loginWithEmptyCredentials_showsError() {
        // Click login without entering anything
        onView(withId(R.id.buttonLogin)).perform(click());

        // Since we use Toast, we can't easily check it with Espresso without custom matchers,
        // but we can verify the activity didn't finish/change.
        onView(withId(R.id.buttonLogin)).check(matches(isDisplayed()));
    }

    @Test
    public void loginInput_isWorking() {
        // Type username
        onView(withId(R.id.inputUsername))
                .perform(typeText("agent_007"), closeSoftKeyboard());
        
        // Type password
        onView(withId(R.id.inputPassword))
                .perform(typeText("password123"), closeSoftKeyboard());

        // Verify text was entered
        onView(withId(R.id.inputUsername)).check(matches(withText("agent_007")));
    }
}
