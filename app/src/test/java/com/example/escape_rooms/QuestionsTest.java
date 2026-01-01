package com.example.escape_rooms;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.example.escape_rooms.model.Questions;

@RunWith(RobolectricTestRunner.class)
public class QuestionsTest {

    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
    }

    @Test
    public void testLevelOneQuestions() {
        Questions questions = new Questions(context, 1);
        ArrayList<String> questionsList = questions.getQuestionsList();

        assertFalse("Level 1 should have questions", questionsList.isEmpty());
        // Verify strings match Hebrew questions.json
        assertTrue("Should contain sky color question", questionsList.contains("מהו צבע השמיים?"));
        assertEquals("כחול", questions.getCorrectAnswers().get("מהו צבע השמיים?"));
    }

    @Test
    public void testLevelThreeQuestions() {
        Questions questions = new Questions(context, 3);
        String question = "כמה יבשות יש על כדור הארץ?";

        assertTrue("Level 3 should contain continent question", questions.getQuestionsList().contains(question));
        ArrayList<String> answers = questions.getQuestionsToAnswers().get(question);

        assertNotNull(answers);
        assertEquals(4, answers.size());
        assertTrue(answers.contains("7"));
        assertEquals("7", questions.getCorrectAnswers().get(question));
    }

    @Test
    public void testLevelTenQuestions() {
        Questions questions = new Questions(context, 10);
        String question = "מהו המדבר הגדול ביותר בעולם?";

        assertTrue(questions.getQuestionsList().contains(question));
        assertEquals("סהרה", questions.getCorrectAnswers().get(question));
    }

    @Test
    public void testInvalidLevelHandling() {
        // Test level beyond defined range (1-10)
        Questions questions = new Questions(context, 99);
        ArrayList<String> questionsList = questions.getQuestionsList();
        
        assertFalse("Should have a message for completed rooms", questionsList.isEmpty());
        assertEquals("You have completed all the rooms!", questionsList.get(0));
        assertEquals("Win", questions.getCorrectAnswers().get(questionsList.get(0)));
    }
}
