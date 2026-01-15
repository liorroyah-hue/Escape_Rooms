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

import java.util.Arrays;
import java.util.List;

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
 * Uses a manual FakeRepository to avoid Mockito matcher issues in parallel builds.
 */
@RunWith(RobolectricTestRunner.class)
public class GameFlowIntegrationTest {

    @Before
    public void setUp() {
        // Use a manual Fake instead of a Mockito Mock for the static singleton.
        // This is the most stable way to handle shared state in Robolectric.
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
        
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(intent)) {
            scenario.moveToState(Lifecycle.State.RESUMED);

            onView(withId(R.id.questions_recycler_view))
                    .perform(RecyclerViewActions.scrollTo(hasDescendant(withText("מהו צבע השמיים?"))));
            onView(withText("כחול")).perform(click());

            onView(withId(R.id.questions_recycler_view))
                    .perform(RecyclerViewActions.scrollTo(hasDescendant(withText("איזה גז בני אדם צריכים כדי לנשום?"))));
            onView(withText("חמצן")).perform(click());
            
            onView(withId(R.id.btn_submit_answers)).perform(click());

            scenario.onActivity(activity -> {
                assertTrue("Activity should be finishing to load next room", 
                        activity.isFinishing() || activity.isDestroyed());
            });
        }
    }
}
