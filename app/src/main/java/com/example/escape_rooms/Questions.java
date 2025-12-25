package com.example.escape_rooms;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.Arrays;
public class Questions  {
ArrayList<String> questions;
    private ArrayList<ArrayList<String>> ListOfQuestions;
    public Questions() {
        questions = new ArrayList<>();
        questions.add("what is the color of the sky" +
                "1) red" +
                "2) green" +
                "3) blue" +
                "4) white");
        questions.add("if you throw a child of a building that is 1000 meters tall in a velocity of 20 meters per seconds how long does his flight lasts?" +
                "1)10 seconds" +
                "2)12 seconds" +
                "3)14 seconds" +
                "4)16 seconds");
        questions.add("Question 3");
        questions.add("Question 4");
        questions.add("Question 5");
        questions.add("Question 6");
    }
    public void showAddQuestionPopup(Context pop_question) {// if you want to add a question for yourself
        AlertDialog.Builder builder = new AlertDialog.Builder(pop_question);
        LayoutInflater inflater = LayoutInflater.from(builder.getContext());
        View view = inflater.inflate(R.layout.pop_question, null);

        builder.setView(view);

        EditText etQuestion = view.findViewById(R.id.etQuestion);
        Button btnSaveQuestion = view.findViewById(R.id.btnSaveQuestion);

        AlertDialog dialog = builder.create();
        dialog.show();

        btnSaveQuestion.setOnClickListener(v -> {
            String question = etQuestion.getText().toString();

            if (!question.isEmpty()) {
                questions.add(question);
                dialog.dismiss();
            }
        });
    }
}
