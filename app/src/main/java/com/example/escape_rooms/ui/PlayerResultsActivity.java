package com.example.escape_rooms.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.escape_rooms.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class PlayerResultsActivity extends AppCompatActivity {

    public static final String EXTRA_TIMINGS = "com.example.escape_rooms.TIMINGS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_results);

        LinearLayout container = findViewById(R.id.results_container);
        TextView tvTotalTime = findViewById(R.id.tv_total_time);
        Button btnBackHome = findViewById(R.id.btn_back_home);

        HashMap<Integer, Long> timings = (HashMap<Integer, Long>) getIntent().getSerializableExtra(EXTRA_TIMINGS);

        long totalMillis = 0;
        if (timings != null) {
            // Sort levels in ascending order (Room 1 -> Room 10)
            List<Integer> sortedLevels = new ArrayList<>(timings.keySet());
            Collections.sort(sortedLevels);

            for (int level : sortedLevels) {
                long duration = timings.get(level);
                totalMillis += duration;

                // Room Name on Left : Time on Right
                addResultRow(container, "Room " + level + ": ", formatTime(duration));
            }
        }

        tvTotalTime.setText("TOTAL TIME: " + formatTime(totalMillis));

        btnBackHome.setOnClickListener(v -> {
            Intent intent = new Intent(PlayerResultsActivity.this, HomePage.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void addResultRow(LinearLayout container, String roomLabel, String timeValue) {
        // Inflate the ConstraintLayout based row
        View row = LayoutInflater.from(this).inflate(R.layout.item_player_result, container, false);

        TextView tvLabel = row.findViewById(R.id.tv_room_label);
        TextView tvValue = row.findViewById(R.id.tv_time_value);

        tvLabel.setText(roomLabel);
        tvValue.setText(timeValue);

        container.addView(row);
    }

    private String formatTime(long millis) {
        int seconds = (int) (millis / 1000) % 60;
        int minutes = (int) ((millis / (1000 * 60)) % 60);
        return String.format("%02d:%02d", minutes, seconds);
    }
}
