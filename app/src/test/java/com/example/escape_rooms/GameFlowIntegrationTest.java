package com.example.escape_rooms;

import android.content.Intent;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.contrib.RecyclerViewActions;

import com.example.escape_rooms.model.Question;
import com.example.escape_rooms.repository.QuestionRepository;
import com.example.escape_rooms.ui.MainActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.List;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 31) // Matches your target SDK
public class GameFlowIntegrationTest {

    @Before
    public void setUp() {
        QuestionRepository fakeRepository = new QuestionRepository() {
            @Override
            public void getQuestionsForLevel(int level, QuestionsCallback callback) {
                callback.onSuccess(createMockQuestions());
            }
        };
        QuestionRepository.setTestInstance(fakeRepository);
    }

    @After
    public void tearDown() {
        QuestionRepository.setTestInstance(null);
    }

    private List<Question> createMockQuestions() {
        Question q1 = new Question();
        q1.setQuestion("מהו צבע השמיים?");
        q1.setCorrectAnswer("כחול");
        q1.setAnswers(Arrays.asList("אדום", "ירוק", "כחול", "לבן"));

        Question q2 = new Question();
        q2.setQuestion("איזה גז בני אדם צריכים כדי לנשום?");
        q2.setCorrectAnswer("חמצן");
        q2.setAnswers(Arrays.asList("פחמן דו חמצני", "חנקן", "חמצן", "הליום"));

        return Arrays.asList(q1, q2);
    }

    @Test
    public void testRoomOne_InitialState() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class);
        intent.putExtra(MainActivity.EXTRA_LEVEL, 1);
        
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(intent)) {
            scenario.moveToState(Lifecycle.State.RESUMED);
            
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
            scenario.moveToState(Lifecycle.State.RESUMED);
            
            onView(withId(R.id.btn_submit_answers)).perform(click());
            onView(withId(R.id.questions_recycler_view)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void testSubmitCorrectAnswers_NavigatesToNextLevel() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), MainActivity.class);
        intent.putExtra(MainActivity.EXTRA_LEVEL, 1);
        intent.putExtra(MainActivity.EXTRA_CREATION_TYPE, "EXISTING"); // Ensure data required by logic is present
        
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(intent)) {
            scenario.moveToState(Lifecycle.State.RESUMED);

            // Answer Question 1
            onView(withId(R.id.questions_recycler_view))
                    .perform(RecyclerViewActions.scrollTo(hasDescendant(withText("מהו צבע השמיים?"))));
            onView(allOf(withText("כחול"), isDisplayed(), isEnabled())).perform(click());

            // Answer Question 2
            onView(withId(R.id.questions_recycler_view))
                    .perform(RecyclerViewActions.scrollTo(hasDescendant(withText("איזה גז בני אדם צריכים כדי לנשום?"))));
            onView(allOf(withText("חמצן"), isDisplayed(), isEnabled())).perform(click());
            
            // Submit
            onView(withId(R.id.btn_submit_answers)).perform(click());

            scenario.onActivity(activity -> {
                // We assert that the activity has responded to the correct answers
                assertTrue("Activity should be processing transition", 
                        activity.isFinishing() || activity.isDestroyed());
            });
        }
    }
}
