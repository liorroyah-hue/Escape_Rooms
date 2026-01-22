package com.example.escape_rooms;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.contrib.RecyclerViewActions;

import com.example.escape_rooms.model.Question;
import com.example.escape_rooms.repository.QuestionRepository;
import com.example.escape_rooms.ui.MainActivity;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;

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

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 31)
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
            ShadowLooper.idleMainLooper();
            
            onView(withId(R.id.questions_recycler_view))
                    .check(matches(hasDescendant(withText("מהו צבע השמיים?"))));
            
            onView(withId(R.id.btn_submit_answers)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void testSubmitCorrectAnswers_NavigatesToNextLevel() {
        Context context = ApplicationProvider.getApplicationContext();
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(MainActivity.EXTRA_LEVEL, 1);
        // השתמש במחרוזת העברית כדי להתאים ללוגיקה של ה-Activity
        intent.putExtra(MainActivity.EXTRA_CREATION_TYPE, context.getString(R.string.creation_option_existing));
        
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(intent)) {
            scenario.moveToState(Lifecycle.State.RESUMED);
            ShadowLooper.idleMainLooper();

            // בחר תשובה לשאלה 1 (נמצאת בפוזיציה 0)
            onView(withId(R.id.questions_recycler_view))
                    .perform(RecyclerViewActions.actionOnItemAtPosition(0, clickChildViewWithText("כחול")));

            // בחר תשובה לשאלה 2 (נמצאת בפוזיציה 1)
            onView(withId(R.id.questions_recycler_view))
                    .perform(RecyclerViewActions.actionOnItemAtPosition(1, clickChildViewWithText("חמצן")));
            
            // הגש תשובות
            onView(withId(R.id.btn_submit_answers)).perform(click());
            ShadowLooper.idleMainLooper();

            scenario.onActivity(activity -> {
                assertTrue("Activity should be finishing to load next screen", 
                        activity.isFinishing() || activity.isDestroyed());
            });
        }
    }

    /**
     * פעולה מותאמת אישית ללחיצה על תת-רכיב בתוך פריט ברשימה.
     */
    public static ViewAction clickChildViewWithText(final String text) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isDisplayed();
            }

            @Override
            public String getDescription() {
                return "Click on a child view with specified text: " + text;
            }

            @Override
            public void perform(UiController uiController, View view) {
                // מחפש את התת-רכיב עם הטקסט המבוקש בתוך ה-ViewHolder ומבצע לחיצה
                View child = findViewWithText(view, text);
                if (child != null) {
                    child.performClick();
                } else {
                    throw new RuntimeException("Could not find child with text: " + text);
                }
            }

            private View findViewWithText(View root, String text) {
                if (root instanceof android.view.ViewGroup) {
                    android.view.ViewGroup group = (android.view.ViewGroup) root;
                    for (int i = 0; i < group.getChildCount(); i++) {
                        View child = findViewWithText(group.getChildAt(i), text);
                        if (child != null) return child;
                    }
                }
                if (root instanceof android.widget.TextView) {
                    if (text.equals(((android.widget.TextView) root).getText().toString())) {
                        return root;
                    }
                }
                return null;
            }
        };
    }
}
