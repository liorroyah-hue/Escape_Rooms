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

/**
 * מסך התוצאות — מציג זמן לכל רמה וזמן כולל, שומר תוצאה ל-Supabase.
 */
public class PlayerResultsActivity extends AppCompatActivity {

    private static final String TAG = "PlayerResultsActivity";
    private final GameRepository gameRepository = new GameRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_results);

        // קישור אלמנטי UI
        LinearLayout container = findViewById(R.id.results_container); // מיכל שורות התוצאות
        TextView tvTotalTime   = findViewById(R.id.tv_total_time);      // תצוגת זמן כולל
        Button btnProceed      = findViewById(R.id.btn_back_home);      // כפתור המשך

        try {
            // קריאת כל הנתונים מה-Intent
            @SuppressWarnings("unchecked")
            HashMap<Integer, Long> timings = (HashMap<Integer, Long>)
                    getIntent().getSerializableExtra(MainActivity.EXTRA_TIMINGS); // זמן לכל רמה
            int roomId     = getIntent().getIntExtra(MainActivity.EXTRA_ROOM_ID, 0);     // ID החדר
            int questionId = getIntent().getIntExtra(MainActivity.EXTRA_QUESTION_ID, 0); // ID השאלה
            int pictureId  = getIntent().getIntExtra(MainActivity.EXTRA_PICTURE_ID, 0);  // ID התמונה

            long totalMillis = 0;
            int levelsCompleted = 0;

            if (timings != null) {
                levelsCompleted = timings.size(); // מספר הרמות שהושלמו

                // ממיין רמות בסדר עולה להצגה בסדר הנכון
                List<Integer> sortedLevels = new ArrayList<>(timings.keySet());
                Collections.sort(sortedLevels);

                for (int level : sortedLevels) {
                    Long duration = timings.get(level);
                    if (duration != null) {
                        totalMillis += duration; // מחשב זמן כולל
                        // מוסיף שורת תוצאה לכל רמה
                        addResultRow(container, getString(R.string.label_room, level), formatTime(duration));
                    }
                }
            }

            tvTotalTime.setText(getString(R.string.label_total_time, formatTime(totalMillis))); // מציג זמן כולל

            // שומר את התוצאה המלאה ל-Supabase כולל כל ה-IDs
            saveFinalResultToDatabase(totalMillis, levelsCompleted, roomId, questionId, pictureId);

            // לחיצה על "המשך" — עובר למסך הדירוג
            btnProceed.setOnClickListener(v -> {
                startActivity(new Intent(this, RatingActivity.class));
                finish();
            });

        } catch (Exception e) {
            Log.e(TAG, "Error loading results", e);
            Toast.makeText(this, getString(R.string.connection_error), Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, ChoosingGameVariantActivity.class)); // חוזר לתפריט
            finish();
        }
    }

    /**
     * שומר תוצאת משחק מלאה ל-Supabase.
     * קורא user_id מ-SharedPreferences.
     */
    private void saveFinalResultToDatabase(long totalTime, int levels,
                                            int roomId, int questionId, int pictureId) {
        SharedPreferences prefs = getSharedPreferences("EscapeRoomPrefs", Context.MODE_PRIVATE);
        long userId = prefs.getLong("current_user_id", -1); // -1 = לא מחובר

        if (userId == -1) {
            Log.e(TAG, "Cannot save: no user ID in session."); // לא שומר ללא משתמש
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

    /**
     * מוסיף שורת תוצאה (תווית + זמן) למיכל התוצאות.
     */
    private void addResultRow(LinearLayout container, String label, String time) {
        View row = LayoutInflater.from(this).inflate(R.layout.item_player_result, container, false);
        TextView tvLabel = row.findViewById(R.id.tv_room_label);
        TextView tvValue = row.findViewById(R.id.tv_time_value);
        tvLabel.setText(label); // שם הרמה
        tvValue.setText(time);  // זמן הרמה
        container.addView(row);
    }

    /**
     * ממיר מילישניות לפורמט MM:SS להצגה.
     */
    private String formatTime(long millis) {
        return String.format("%02d:%02d",
                (int) ((millis / (1000 * 60)) % 60), // דקות
                (int) (millis / 1000) % 60);           // שניות
    }
}
