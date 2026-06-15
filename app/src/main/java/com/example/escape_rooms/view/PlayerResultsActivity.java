package com.example.escape_rooms.view;

import android.content.Intent;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class PlayerResultsActivity extends AppCompatActivity {

    private static final String TAG = "PlayerResultsActivity";

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

            long totalMillis = 0;
            if (timings != null) {
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

            // השמירה ל-Supabase כבר נעשתה ב-GameViewModel.saveLevelResult
            // אחרי כל רמה — כאן רק מציגים את התוצאות

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
