package com.example.escape_rooms.model;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

public class Questions {
    private final HashMap<String, ArrayList<String>> questionsToAnswers = new HashMap<>();
    private final ArrayList<String> questionsList = new ArrayList<>();
    private final HashMap<String, String> correctAnswers = new HashMap<>();

    /**
     * Constructor that loads questions based on a level using the native JSON API.
     * @param context Activity or application context.
     * @param level The level/room number to load questions for.
     */
    public Questions(Context context, int level) {
        loadQuestionsFromJson(context, level);
    }

    private void loadQuestionsFromJson(Context context, int level) {
        questionsList.clear();
        questionsToAnswers.clear();
        correctAnswers.clear();

        String jsonString = loadJSONFromAsset(context, "questions.json");
        if (jsonString == null) return;

        try {
            JSONArray allLevelsArray = new JSONArray(jsonString);
            boolean levelFound = false;

            for (int i = 0; i < allLevelsArray.length(); i++) {
                JSONObject levelObject = allLevelsArray.getJSONObject(i);
                int jsonLevel = levelObject.getInt("level");

                if (jsonLevel == level) {
                    JSONArray questionsArray = levelObject.getJSONArray("questions");
                    for (int j = 0; j < questionsArray.length(); j++) {
                        JSONObject qObject = questionsArray.getJSONObject(j);
                        String question = qObject.getString("question");
                        String correctAnswer = qObject.getString("correctAnswer");
                        
                        JSONArray answersArray = qObject.getJSONArray("answers");
                        ArrayList<String> answersList = new ArrayList<>();
                        for (int k = 0; k < answersArray.length(); k++) {
                            answersList.add(answersArray.getString(k));
                        }
                        
                        addQuestion(question, correctAnswer, answersList);
                    }
                    levelFound = true;
                    break;
                }
            }

            if (!levelFound) {
                // Default case if level not found
                addQuestion("You have completed all the rooms!", "Win", new ArrayList<String>() {{ add("Win"); }});
            }

        } catch (JSONException e) {
            Log.e("Questions", "Error parsing JSON", e);
        }
    }

    private String loadJSONFromAsset(Context context, String fileName) {
        String json;
        try {
            InputStream is = context.getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            Log.e("Questions", "Error reading JSON asset", ex);
            return null;
        }
        return json;
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
