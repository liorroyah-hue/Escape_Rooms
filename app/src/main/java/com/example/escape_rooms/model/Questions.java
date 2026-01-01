package com.example.escape_rooms.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Questions {
    private final HashMap<String, ArrayList<String>> questionsToAnswers = new HashMap<>();
    private final ArrayList<String> questionsList = new ArrayList<>();
    private final HashMap<String, String> correctAnswers = new HashMap<>();

    /**
     * Constructor that processes a list of Question objects.
     * @param questions The list of Question objects to process.
     */
    public Questions(List<Question> questions) {
        if (questions != null && !questions.isEmpty()) {
            for (Question q : questions) {
                // Add a null check to prevent crashes if a question is malformed
                if (q != null && q.getQuestion() != null && q.getCorrectAnswer() != null && q.getAnswers() != null) {
                    addQuestion(q.getQuestion(), q.getCorrectAnswer(), new ArrayList<>(q.getAnswers()));
                }
            }
        }
        
        // If, after processing, no valid questions were added, show the default message.
        if (questionsList.isEmpty()) {
            addQuestion("You have completed all the rooms!", "Win", new ArrayList<String>() {{ add("Win"); }});
        }
    }

    private void addQuestion(String question, String correctAnswer, ArrayList<String> allAnswers) {
        questionsList.add(question);
        questionsToAnswers.put(question, allAnswers);
        correctAnswers.put(question, correctAnswer);
    }

    // Getter methods
    public ArrayList<String> getQuestionsList() {
        return questionsList;
    }

    public HashMap<String, ArrayList<String>> getQuestionsToAnswers() {
        return questionsToAnswers;
    }

    public HashMap<String, String> getCorrectAnswers() {
        return correctAnswers;
    }
}
