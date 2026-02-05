package com.example.escape_rooms.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class QuizData implements Serializable {
    @SerializedName("questions")
    private List<String> questions;
    
    @SerializedName("answers")
    private List<List<String>> answers;
    
    @SerializedName("correctAnswers")
    private List<String> correctAnswers;

    public List<String> getQuestions() { return questions; }
    public void setQuestions(List<String> questions) { this.questions = questions; }

    public List<List<String>> getAnswers() { return answers; }
    public void setAnswers(List<List<String>> answers) { this.answers = answers; }

    public List<String> getCorrectAnswers() { return correctAnswers; }
    public void setCorrectAnswers(List<String> correctAnswers) { this.correctAnswers = correctAnswers; }
}
