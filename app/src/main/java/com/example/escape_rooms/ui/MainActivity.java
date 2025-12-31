package com.example.escape_rooms.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.escape_rooms.Questions;
import com.example.escape_rooms.QuestionsAdapter;
import com.example.escape_rooms.R;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_LEVEL = "com.example.escape_rooms.LEVEL";
    public static final String EXTRA_TIMINGS = "com.example.escape_rooms.TIMINGS";

    private RecyclerView questionsRecyclerView;
    private QuestionsAdapter questionsAdapter;
    private Questions questionsData;
    private Button btnSubmitAnswers;
    private int currentLevel = 1;

    private static final int NUMBER_OF_LEVELS = 10;


    // Tracking time
    private long startTime;
    private HashMap<Integer, Long> levelTimings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        currentLevel = getIntent().getIntExtra(EXTRA_LEVEL, 1);
        levelTimings = (HashMap<Integer, Long>) getIntent().getSerializableExtra(EXTRA_TIMINGS);
        if (levelTimings == null) {
            levelTimings = new HashMap<>();
        }

        // Start timer for this room
        startTime = System.currentTimeMillis();

        questionsRecyclerView = findViewById(R.id.questions_recycler_view);
        btnSubmitAnswers = findViewById(R.id.btn_submit_answers);

        questionsData = new Questions(currentLevel);

        questionsAdapter = new QuestionsAdapter(
                this,
                questionsData.getQuestionsList(),
                questionsData.getQuestionsToAnswers()
        );
        questionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        questionsRecyclerView.setAdapter(questionsAdapter);

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
            // Calculate time spent in this room
            long timeSpent = System.currentTimeMillis() - startTime;
            levelTimings.put(currentLevel, timeSpent);

            if (currentLevel < NUMBER_OF_LEVELS) {
                Toast.makeText(this, "Room " + currentLevel + " Cleared!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                intent.putExtra(EXTRA_LEVEL, currentLevel + 1);
                intent.putExtra(EXTRA_TIMINGS, levelTimings);
                startActivity(intent);
                finish();
            } else {
                // Game Finished
                Intent intent = new Intent(MainActivity.this, PlayerResultsActivity.class);
                intent.putExtra(EXTRA_TIMINGS, levelTimings);
                startActivity(intent);
                finish();
            }
        } else {
            Toast.makeText(this, "Incorrect. The clock is ticking!", Toast.LENGTH_SHORT).show();
        }
    }
}
