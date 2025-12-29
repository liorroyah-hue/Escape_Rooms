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
                addQuestion("What is the color of the sky?", "Blue", new ArrayList<String>() {{ add("Red"); add("Green"); add("Blue"); add("White"); }});
                addQuestion("Which gas do humans need to breathe?", "Oxygen", new ArrayList<String>() {{ add("Carbon Dioxide"); add("Nitrogen"); add("Oxygen"); add("Helium"); }});
                break;
            case 2:
                addQuestion("What is the capital of France?", "Paris", new ArrayList<String>() {{ add("London"); add("Berlin"); add("Paris"); add("Madrid"); }});
                addQuestion("Which planet is known as the Red Planet?", "Mars", new ArrayList<String>() {{ add("Earth"); add("Mars"); add("Jupiter"); add("Venus"); }});
                break;
            case 3:
                addQuestion("How many continents are there on Earth?", "7", new ArrayList<String>() {{ add("5"); add("6"); add("7"); add("8"); }});
                addQuestion("What is the largest ocean in the world?", "Pacific", new ArrayList<String>() {{ add("Atlantic"); add("Indian"); add("Pacific"); add("Arctic"); }});
                break;
            case 4:
                addQuestion("What is the square root of 64?", "8", new ArrayList<String>() {{ add("6"); add("7"); add("8"); add("9"); }});
                addQuestion("Which element has the chemical symbol 'O'?", "Oxygen", new ArrayList<String>() {{ add("Gold"); add("Oxygen"); add("Silver"); add("Iron"); }});
                break;
            case 5:
                addQuestion("Who wrote 'Romeo and Juliet'?", "Shakespeare", new ArrayList<String>() {{ add("Dickens"); add("Shakespeare"); add("Twain"); add("Hemingway"); }});
                addQuestion("What is the fastest land animal?", "Cheetah", new ArrayList<String>() {{ add("Lion"); add("Cheetah"); add("Horse"); add("Leopard"); }});
                break;
            case 6:
                addQuestion("In which year did World War II end?", "1945", new ArrayList<String>() {{ add("1940"); add("1945"); add("1950"); add("1939"); }});
                addQuestion("What is the capital city of Japan?", "Tokyo", new ArrayList<String>() {{ add("Seoul"); add("Beijing"); add("Tokyo"); add("Bangkok"); }});
                break;
            case 7:
                addQuestion("How many colors are in a rainbow?", "7", new ArrayList<String>() {{ add("5"); add("6"); add("7"); add("8"); }});
                addQuestion("Which is the largest planet in our solar system?", "Jupiter", new ArrayList<String>() {{ add("Saturn"); add("Earth"); add("Jupiter"); add("Neptune"); }});
                break;
            case 8:
                addQuestion("What is the hardest natural substance on Earth?", "Diamond", new ArrayList<String>() {{ add("Gold"); add("Iron"); add("Diamond"); add("Granite"); }});
                addQuestion("Which country is home to the Kangaroo?", "Australia", new ArrayList<String>() {{ add("India"); add("Brazil"); add("Australia"); add("South Africa"); }});
                break;
            case 9:
                addQuestion("What is the currency of the United Kingdom?", "Pound Sterling", new ArrayList<String>() {{ add("Euro"); add("Dollar"); add("Pound Sterling"); add("Yen"); }});
                addQuestion("Which organ pumps blood throughout the body?", "Heart", new ArrayList<String>() {{ add("Brain"); add("Lungs"); add("Heart"); add("Liver"); }});
                break;
            case 10:
                addQuestion("What is the largest desert in the world?", "Sahara", new ArrayList<String>() {{ add("Gobi"); add("Sahara"); add("Arctic"); add("Kalahari"); }});
                addQuestion("Which famous scientist developed the theory of relativity?", "Einstein", new ArrayList<String>() {{ add("Newton"); add("Einstein"); add("Galileo"); add("Tesla"); }});
                break;
            default:
                addQuestion("You have completed all the rooms!", "Win", new ArrayList<String>() {{ add("Win"); }});
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
