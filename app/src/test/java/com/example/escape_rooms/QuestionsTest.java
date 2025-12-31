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
        assertEquals("What is the color of the sky?", questionsList.get(0));
        assertEquals("Blue", questions.getCorrectAnswers().get(questionsList.get(0)));
    }

    @Test
    public void testLevelThreeQuestions() {
        Questions questions = new Questions(context, 3);
        String question = "How many continents are there on Earth?";

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
        String question = "What is the largest desert in the world?";

        assertTrue(questions.getQuestionsList().contains(question));
        assertEquals("Sahara", questions.getCorrectAnswers().get(question));
    }

    @Test
    public void testInvalidLevelHandling() {
        // Test level beyond defined range
        Questions questions = new Questions(context, 99);
        assertEquals("You have completed all the rooms!", questions.getQuestionsList().get(0));
        assertEquals("Win", questions.getCorrectAnswers().get(questions.getQuestionsList().get(0)));
    }
}
