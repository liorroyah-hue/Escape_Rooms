package com.example.escape_rooms.model;

import com.example.escape_rooms.viewmodel.GameViewModel;

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
     * This is now robust and uses the correct QuizData type.
     */
    public Questions(GameViewModel.QuizData quizData) {
        if (quizData != null && quizData.getQuestions() != null && !quizData.getQuestions().isEmpty() &&
            quizData.getAnswers() != null && quizData.getCorrectAnswers() != null &&
            quizData.getQuestions().size() == quizData.getAnswers().size() &&
            quizData.getQuestions().size() == quizData.getCorrectAnswers().size()) {
            
            for (int i = 0; i < quizData.getQuestions().size(); i++) {
                String question = quizData.getQuestions().get(i);
                String correctAnswer = quizData.getCorrectAnswers().get(i);
                List<String> answers = quizData.getAnswers().get(i);

                if (question != null && correctAnswer != null && answers != null) {
                    addQuestion(question, correctAnswer, new ArrayList<>(answers));
                }
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
