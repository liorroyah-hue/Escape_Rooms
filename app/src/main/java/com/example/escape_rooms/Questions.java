package com.example.escape_rooms;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.HashMap;

public class Questions {
    private final HashMap<String, ArrayList<String>> questionsToAnswers = new HashMap<>();
    private final ArrayList<String> questionsList = new ArrayList<>();
    private final HashMap<String, String> correctAnswers = new HashMap<>();

    /**
     * Constructor that loads questions based on a level.
     * @param level The level/room number to load questions for.
     */
    public Questions(int level) {
        loadQuestionsForLevel(level);
    }

    private void loadQuestionsForLevel(int level) {
        questionsList.clear();
        questionsToAnswers.clear();
        correctAnswers.clear();

        switch (level) {
            case 1:
                addQuestion("what is the color of the sky", "blue", new ArrayList<String>() {{ add("red"); add("green"); add("blue"); add("white"); }});
                addQuestion("if you throw a child of a building that is 1000 meters tall in a velocity of 20 meters per seconds how long does his flight lasts?", "12 seconds", new ArrayList<String>() {{ add("10 seconds"); add("12 seconds"); add("14 seconds"); add("16 seconds"); }});
                break;
            case 2:
                addQuestion("What is the capital of France?", "Paris", new ArrayList<String>() {{ add("London"); add("Berlin"); add("Paris"); add("Madrid"); }});
                addQuestion("Which planet is known as the Red Planet?", "Mars", new ArrayList<String>() {{ add("Earth"); add("Mars"); add("Jupiter"); add("Venus"); }});
                break;
            case 3:
                addQuestion("Level 3 Question 1", "Answer A", new ArrayList<String>() {{ add("Answer A"); add("Answer B"); }});
                addQuestion("Level 3 Question 2", "Answer C", new ArrayList<String>() {{ add("Answer C"); add("Answer D"); }});
                break;
            case 4:
                addQuestion("Level 4 Question 1", "B", new ArrayList<String>() {{ add("A"); add("B"); }});
                addQuestion("Level 4 Question 2", "D", new ArrayList<String>() {{ add("C"); add("D"); }});
                break;
            case 5:
                addQuestion("Level 5 Question 1", "Right", new ArrayList<String>() {{ add("Right"); add("Wrong"); }});
                addQuestion("Level 5 Question 2", "True", new ArrayList<String>() {{ add("True"); add("False"); }});
                break;
            case 6:
                addQuestion("Level 6 Q1", "A1", new ArrayList<String>() {{ add("A1"); add("A2"); }});
                addQuestion("Level 6 Q2", "B2", new ArrayList<String>() {{ add("B1"); add("B2"); }});
                break;
            case 7:
                addQuestion("Level 7 Q1", "1", new ArrayList<String>() {{ add("1"); add("2"); }});
                addQuestion("Level 7 Q2", "4", new ArrayList<String>() {{ add("3"); add("4"); }});
                break;
            case 8:
                addQuestion("Level 8 Q1", "Yes", new ArrayList<String>() {{ add("Yes"); add("No"); }});
                addQuestion("Level 8 Q2", "Maybe", new ArrayList<String>() {{ add("Maybe"); add("So"); }});
                break;
            case 9:
                addQuestion("Level 9 Q1", "First", new ArrayList<String>() {{ add("First"); add("Second"); }});
                addQuestion("Level 9 Q2", "Fourth", new ArrayList<String>() {{ add("Third"); add("Fourth"); }});
                break;
            case 10:
                addQuestion("Level 10 Question 1", "End", new ArrayList<String>() {{ add("Start"); add("End"); }});
                addQuestion("Level 10 Question 2", "Finish", new ArrayList<String>() {{ add("Begin"); add("Finish"); }});
                break;
            default:
                // If the level is higher than 10, or something else, you could end the game or loop back.
                // For now, let's just show a final message.
                addQuestion("You have completed all the rooms!", "", new ArrayList<>());
                break;
        }
    }

    private void addQuestion(String question, String correctAnswer, ArrayList<String> allAnswers) {
        questionsList.add(question);
        questionsToAnswers.put(question, allAnswers);
        correctAnswers.put(question, correctAnswer);
    }

    public void showAddQuestionPopup(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.pop_question, null);
        builder.setView(view);
        EditText etQuestion = view.findViewById(R.id.etQuestion);
        Button btnSaveQuestion = view.findViewById(R.id.btnSaveQuestion);
        AlertDialog dialog = builder.create();
        dialog.show();
        btnSaveQuestion.setOnClickListener(v -> {
            String question = etQuestion.getText().toString().trim();
            if (!question.isEmpty()) {
                questionsList.add(question);
                dialog.dismiss();
            }
        });
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
