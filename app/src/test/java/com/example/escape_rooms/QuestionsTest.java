package com.example.escape_rooms;

import com.example.escape_rooms.model.Question;
import com.example.escape_rooms.model.Questions;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class QuestionsTest {

    @Test
    public void testQuestionsProcessing() {
        // Create sample questions
        Question q1 = new Question();
        q1.setQuestion("מהו צבע השמיים?");
        q1.setCorrectAnswer("כחול");
        q1.setAnswers(Arrays.asList("אדום", "ירוק", "כחול", "לבן"));

        Question q2 = new Question();
        q2.setQuestion("איזה גז בני אדם צריכים כדי לנשום?");
        q2.setCorrectAnswer("חמצן");
        q2.setAnswers(Arrays.asList("פחמן דו חמצני", "חנקן", "חמצן", "הליום"));

        List<Question> questionList = Arrays.asList(q1, q2);

        // Process them through the Questions model
        Questions questions = new Questions(questionList);

        // Assertions
        assertEquals(2, questions.getQuestionsList().size());
        assertTrue(questions.getQuestionsList().contains("מהו צבע השמיים?"));
        assertEquals("כחול", questions.getCorrectAnswers().get("מהו צבע השמיים?"));
        assertEquals(4, questions.getQuestionsToAnswers().get("מהו צבע השמיים?").size());
    }

    @Test
    public void testEmptyQuestionsFallback() {
        Questions questions = new Questions(new ArrayList<>());
        assertEquals(1, questions.getQuestionsList().size());
        assertEquals("You have completed all the rooms!", questions.getQuestionsList().get(0));
    }

    @Test
    public void testNullQuestionsListFallback() {
        // Fix: Explicitly cast null to List<Question> to resolve constructor ambiguity
        Questions questions = new Questions((List<Question>) null);
        assertEquals(1, questions.getQuestionsList().size());
        assertEquals("You have completed all the rooms!", questions.getQuestionsList().get(0));
    }

    @Test
    public void testMalformedQuestionsSkipped() {
        // Create a list with one valid and one malformed question (missing fields)
        Question valid = new Question();
        valid.setQuestion("Valid Question?");
        valid.setCorrectAnswer("Yes");
        valid.setAnswers(Arrays.asList("Yes", "No"));

        Question malformed = new Question();
        malformed.setQuestion(null); // Missing text

        List<Question> list = Arrays.asList(valid, malformed, null);

        Questions questions = new Questions(list);

        // Only the valid one should be processed
        assertEquals(1, questions.getQuestionsList().size());
        assertEquals("Valid Question?", questions.getQuestionsList().get(0));
    }

    @Test
    public void testCorrectAnswerMapping() {
        Question q = new Question();
        q.setQuestion("Q1");
        q.setCorrectAnswer("A1");
        q.setAnswers(Arrays.asList("A1", "A2"));

        Questions questions = new Questions(Arrays.asList(q));

        assertEquals("A1", questions.getCorrectAnswers().get("Q1"));
    }
}
