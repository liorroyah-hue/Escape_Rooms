package com.example.escape_rooms;

import androidx.test.ext.junit.rules.ActivityScenarioRule;

import com.example.escape_rooms.ui.LoginActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

/**
 * Robolectric-based integration test for LoginActivity.
 * Runs on the JVM without an emulator.
 */
@RunWith(RobolectricTestRunner.class)
public class LoginIntegrationTest {

    @Rule
    public ActivityScenarioRule<LoginActivity> activityRule =
            new ActivityScenarioRule<>(LoginActivity.class);

    @Test
    public void loginScreen_initialState() {
        // Updated to use the correct Hebrew title or Resource ID
        onView(withId(R.id.textTitle)).check(matches(withText(R.string.login_title)));
        onView(withId(R.id.textTitle)).check(matches(isDisplayed()));
        onView(withId(R.id.textStatus)).check(matches(not(isDisplayed())));
    }

    @Test
    public void loginWithEmptyCredentials_staysOnScreen() {
        onView(withId(R.id.buttonLogin)).perform(click());
        onView(withId(R.id.buttonLogin)).check(matches(isDisplayed()));
    }

    @Test
    public void loginInput_isWorking() {
        onView(withId(R.id.inputUsername))
                .perform(typeText("agent_007"), closeSoftKeyboard());
        onView(withId(R.id.inputPassword))
                .perform(typeText("password123"), closeSoftKeyboard());

        onView(withId(R.id.inputUsername)).check(matches(withText("agent_007")));
    }
}
