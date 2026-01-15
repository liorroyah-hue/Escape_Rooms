package com.example.escape_rooms.model;

import com.example.escape_rooms.viewmodel.ChoosingGameViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Questions {
    private final HashMap<String, ArrayList<String>> questionsToAnswers = new HashMap<>();
    private final ArrayList<String> questionsList = new ArrayList<>();
    private final HashMap<String, String> correctAnswers = new HashMap<>();

    /**
     * Constructor for Supabase questions.
     */
    public Questions(List<Question> questions) {
        if (questions != null && !questions.isEmpty()) {
            for (Question q : questions) {
                if (q != null && q.getQuestion() != null && q.getCorrectAnswer() != null && q.getAnswers() != null) {
                    addQuestion(q.getQuestion(), q.getCorrectAnswer(), new ArrayList<>(q.getAnswers()));
                }
            }
        }
        if (questionsList.isEmpty()) {
            addQuestion("You have completed all the rooms!", "Win", new ArrayList<String>() {{ add("Win"); }});
        }
    }

    /**
     * Constructor for AI-generated questions.
     */
    public Questions(ChoosingGameViewModel.QuizData quizData) {
        if (quizData != null && quizData.questions != null && !quizData.questions.isEmpty()) {
            for (int i = 0; i < quizData.questions.size(); i++) {
                String question = quizData.questions.get(i);
                String correctAnswer = quizData.correctAnswers.get(i);
                List<String> answers = quizData.answers.get(i);
                addQuestion(question, correctAnswer, new ArrayList<>(answers));
            }
        }
        if (questionsList.isEmpty()) {
            addQuestion("You have completed all the rooms!", "Win", new ArrayList<String>() {{ add("Win"); }});
        }
    }

    private void addQuestion(String question, String correctAnswer, ArrayList<String> allAnswers) {
        questionsList.add(question);
        questionsToAnswers.put(question, allAnswers);
        correctAnswers.put(question, correctAnswer);
    }

    // Getters
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
