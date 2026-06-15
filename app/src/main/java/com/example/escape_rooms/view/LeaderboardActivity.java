package com.example.escape_rooms.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.escape_rooms.R;
import com.example.escape_rooms.repository.GameRepository;
import com.example.escape_rooms.view.adapters.LeaderboardAdapter;

import java.util.List;

/**
 * מסך לוח התוצאות — מציג את 10 השחקנים הטובים ביותר.
 * כל שחקן מופיע פעם אחת עם הזמן הטוב שלו.
 */
public class LeaderboardActivity extends AppCompatActivity {

    private RecyclerView rvLeaderboard;  // רשימת השחקנים
    private ProgressBar pbLoading;       // מוצג בזמן טעינה
    private final GameRepository gameRepository = new GameRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        rvLeaderboard = findViewById(R.id.rv_leaderboard);
        pbLoading = findViewById(R.id.pb_leaderboard_loading);
        Button btnExit = findViewById(R.id.btn_exit_leaderboard);

        rvLeaderboard.setLayoutManager(new LinearLayoutManager(this)); // רשימה אנכית

        fetchLeaderboardData(); // טוען נתונים מ-Supabase

        // לחיצה על "יציאה" — חוזר לתפריט הראשי ומנקה את ה-back stack
        btnExit.setOnClickListener(v -> {
            Intent intent = new Intent(LeaderboardActivity.this, ChoosingGameVariantActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // מנקה stack
            startActivity(intent);
            finish();
        });
    }

    /**
     * שולף את התוצאות מ-Supabase ומציג ב-RecyclerView.
     */
    private void fetchLeaderboardData() {
        pbLoading.setVisibility(View.VISIBLE); // מציג טעינה
        gameRepository.getTopScores(new GameRepository.LeaderboardCallback() {
            @Override
            public void onSuccess(List<GameRepository.GameResult> results) {
                runOnUiThread(() -> {
                    pbLoading.setVisibility(View.GONE); // מסתיר טעינה
                    LeaderboardAdapter adapter = new LeaderboardAdapter(results);
                    rvLeaderboard.setAdapter(adapter); // מציג תוצאות
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    pbLoading.setVisibility(View.GONE);
                    Toast.makeText(LeaderboardActivity.this, "Failed to load leaderboard", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
