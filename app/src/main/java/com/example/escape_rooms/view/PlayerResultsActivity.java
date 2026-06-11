package com.example.escape_rooms.view;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.escape_rooms.R;
import com.example.escape_rooms.repository.GameRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class PlayerResultsActivity extends AppCompatActivity {

    private static final String TAG = "PlayerResultsActivity";
    private final GameRepository gameRepository = new GameRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_results);

        LinearLayout container = findViewById(R.id.results_container);
        TextView tvTotalTime   = findViewById(R.id.tv_total_time);
        Button btnProceed      = findViewById(R.id.btn_back_home);

        try {
            @SuppressWarnings("unchecked")
            HashMap<Integer, Long> timings = (HashMap<Integer, Long>)
                    getIntent().getSerializableExtra(MainActivity.EXTRA_TIMINGS);
            int roomId     = getIntent().getIntExtra(MainActivity.EXTRA_ROOM_ID, 0);
            int questionId = getIntent().getIntExtra(MainActivity.EXTRA_QUESTION_ID, 0);
            int pictureId  = getIntent().getIntExtra(MainActivity.EXTRA_PICTURE_ID, 0);

            long totalMillis = 0;
            int levelsCompleted = 0;
            if (timings != null) {
                levelsCompleted = timings.size();
                List<Integer> sortedLevels = new ArrayList<>(timings.keySet());
                Collections.sort(sortedLevels);
                for (int level : sortedLevels) {
                    Long duration = timings.get(level);
                    if (duration != null) {
                        totalMillis += duration;
                        addResultRow(container, getString(R.string.label_room, level), formatTime(duration));
                    }
                }
            }

            tvTotalTime.setText(getString(R.string.label_total_time, formatTime(totalMillis)));

            saveFinalResultToDatabase(totalMillis, levelsCompleted, roomId, questionId, pictureId);

            btnProceed.setOnClickListener(v -> {
                startActivity(new Intent(this, RatingActivity.class));
                finish();
            });

        } catch (Exception e) {
            Log.e(TAG, "Error loading results", e);
            Toast.makeText(this, getString(R.string.connection_error), Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, ChoosingGameVariantActivity.class));
            finish();
        }
    }

    private void saveFinalResultToDatabase(long totalTime, int levels,
                                            int roomId, int questionId, int pictureId) {
        SharedPreferences prefs = getSharedPreferences("EscapeRoomPrefs", Context.MODE_PRIVATE);
        long userId = prefs.getLong("current_user_id", -1);

        if (userId == -1) {
            Log.e(TAG, "Cannot save: no user ID in session.");
            return;
        }

        gameRepository.saveGameResult(userId, totalTime, levels, roomId, questionId, pictureId,
                new GameRepository.GameResultCallback() {
                    @Override public void onSuccess() {
                        Log.d(TAG, "Saved: userId=" + userId + " roomId=" + roomId
                                + " questionId=" + questionId + " pictureId=" + pictureId);
                    }
                    @Override public void onError(Exception e) {
                        Log.e(TAG, "Failed to save result", e);
                    }
                });
    }

    private void addResultRow(LinearLayout container, String label, String time) {
        View row = LayoutInflater.from(this).inflate(R.layout.item_player_result, container, false);
        TextView tvLabel = row.findViewById(R.id.tv_room_label);
        TextView tvValue = row.findViewById(R.id.tv_time_value);
        tvLabel.setText(label);
        tvValue.setText(time);
        container.addView(row);
    }

    private String formatTime(long millis) {
        return String.format("%02d:%02d",
                (int) ((millis / (1000 * 60)) % 60),
                (int) (millis / 1000) % 60);
    }
}
