package com.example.escape_rooms;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.ArrayList;

public class QuestionsTest {

    @Test
    public void testLevelOneQuestions() {
        Questions questions = new Questions(1);
        ArrayList<String> questionsList = questions.getQuestionsList();
        
        assertFalse("Level 1 should have questions", questionsList.isEmpty());
        assertEquals("What is the color of the sky?", questionsList.get(0));
        assertEquals("Blue", questions.getCorrectAnswers().get(questionsList.get(0)));
    }

    @Test
    public void testLevelThreeQuestions() {
        Questions questions = new Questions(3);
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
        Questions questions = new Questions(10);
        String question = "What is the largest desert in the world?";
        
        assertTrue(questions.getQuestionsList().contains(question));
        assertEquals("Sahara", questions.getCorrectAnswers().get(question));
    }

    @Test
    public void testInvalidLevelHandling() {
        // Test level beyond defined range
        Questions questions = new Questions(99);
        assertEquals("You have completed all the rooms!", questions.getQuestionsList().get(0));
        assertEquals("Win", questions.getCorrectAnswers().get(questions.getQuestionsList().get(0)));
    }
}
