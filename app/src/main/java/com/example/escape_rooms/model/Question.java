package com.example.escape_rooms.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Question {
    private int id;
    private int level;
    
    @SerializedName("questions for object")
    private String question;
    
    @SerializedName("answer for the questions")
    private String correctAnswer;
    
    @SerializedName("options for the questions")
    private List<String> answers;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getQuestion() {
        return question;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public List<String> getAnswers() {
        return answers;
    }

    // Unused setters can be removed for cleaner code
    // public void setLevel(int level) { this.level = level; }
    // public void setQuestion(String question) { this.question = question; }
    // public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }
    // public void setAnswers(List<String> answers) { this.answers = answers; }
}
