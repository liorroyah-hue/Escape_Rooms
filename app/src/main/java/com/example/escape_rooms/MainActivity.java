package com.example.escape_rooms;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_LEVEL = "com.example.escape_rooms.LEVEL";

    private RecyclerView questionsRecyclerView;
    private QuestionsAdapter questionsAdapter;
    private Questions questionsData;
    private Button btnSubmitAnswers;
    private int currentLevel = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Determine the current level from the intent
        currentLevel = getIntent().getIntExtra(EXTRA_LEVEL, 1);

        // 2. Initialize views
        questionsRecyclerView = findViewById(R.id.questions_recycler_view);
        btnSubmitAnswers = findViewById(R.id.btn_submit_answers);

        // 3. Prepare data for the current level
        questionsData = new Questions(currentLevel);

        // 4. Setup RecyclerView
        questionsAdapter = new QuestionsAdapter(
                this,
                questionsData.getQuestionsList(),
                questionsData.getQuestionsToAnswers()
        );
        questionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        questionsRecyclerView.setAdapter(questionsAdapter);

        // 5. Set the click listener for the submit button
        btnSubmitAnswers.setOnClickListener(v -> verifyAnswers());
    }

    private void verifyAnswers() {
        HashMap<String, String> selectedAnswers = questionsAdapter.getSelectedAnswers();
        HashMap<String, String> correctAnswers = questionsData.getCorrectAnswers();

        if (selectedAnswers.size() != questionsData.getQuestionsList().size()) {
            Toast.makeText(this, "Please answer all questions.", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean allCorrect = true;
        for (Map.Entry<String, String> entry : correctAnswers.entrySet()) {
            String question = entry.getKey();
            String correctAnswer = entry.getValue();
            String selectedAnswer = selectedAnswers.get(question);

            if (selectedAnswer == null || !selectedAnswer.equals(correctAnswer)) {
                allCorrect = false;
                break;
            }
        }

        if (allCorrect) {
            Toast.makeText(this, "Success! Loading next room...", Toast.LENGTH_LONG).show();
            // --- Navigate to the next identical room ---
            Intent intent = new Intent(MainActivity.this, MainActivity.class);
            // Pass the next level number to the new activity
            intent.putExtra(EXTRA_LEVEL, currentLevel + 1);
            startActivity(intent);
            // Finish the current activity so the user can't go back to it
            finish();
        } else {
            Toast.makeText(this, "Some answers are incorrect. Please try again.", Toast.LENGTH_LONG).show();
        }
    }
}
