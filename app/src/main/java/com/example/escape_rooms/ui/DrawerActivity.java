package com.example.escape_rooms.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.escape_rooms.R;
import com.example.escape_rooms.repository.GameRepository;
import com.example.escape_rooms.repository.services.GameAudioManager;
import com.example.escape_rooms.viewmodel.ChoosingGameViewModel;

import java.util.HashMap;

public class DrawerActivity extends AppCompatActivity {

    private GameRepository gameRepository = new GameRepository();

    @Override
    @SuppressWarnings("unchecked")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);

        ImageView imageDrawer = findViewById(R.id.imageDrawer);

        Intent incomingIntent = getIntent();
        String creationType = incomingIntent.getStringExtra(MainActivity.EXTRA_CREATION_TYPE);
        ChoosingGameViewModel.QuizData aiData = (ChoosingGameViewModel.QuizData) incomingIntent.getSerializableExtra(MainActivity.EXTRA_AI_GAME_DATA);
        int level = incomingIntent.getIntExtra(MainActivity.EXTRA_LEVEL, 1);
        HashMap<Integer, Long> timings = (HashMap<Integer, Long>) incomingIntent.getSerializableExtra(MainActivity.EXTRA_TIMINGS);

        // Selection of drawer image based on level
        int[] drawerImages = {
            R.drawable.table_with_drawer, R.drawable.drawer_old, R.drawable.drawer_white,
            R.drawable.drawer_black, R.drawable.safe_black, R.drawable.sade_white,
            R.drawable.table_with_drawer, R.drawable.drawer_old, R.drawable.drawer_white,
            R.drawable.drawer_black
        };
        imageDrawer.setImageResource(drawerImages[(level - 1) % 10]);

        imageDrawer.setOnClickListener(v -> {
            // 1. Save results to DB before moving to next level
            saveProgressToDatabase(level - 1, timings);

            // 2. Audio shuffle
            GameAudioManager.getInstance(this).stopAmbientMusic();
            GameAudioManager.getInstance(this).startAmbientMusic();

            // 3. Navigation
            Intent intent = new Intent(DrawerActivity.this, MainActivity.class);
            intent.putExtra(MainActivity.EXTRA_CREATION_TYPE, creationType);
            if (aiData != null) {
                intent.putExtra(MainActivity.EXTRA_AI_GAME_DATA, aiData);
            }
            intent.putExtra(MainActivity.EXTRA_LEVEL, level);
            intent.putExtra(MainActivity.EXTRA_TIMINGS, timings);
            startActivity(intent);
            finish(); 
        });
    }

    private void saveProgressToDatabase(int lastCompletedLevel, HashMap<Integer, Long> timings) {
        if (timings == null || lastCompletedLevel < 1) return;

        // Retrieve current username from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("EscapeRoomPrefs", Context.MODE_PRIVATE);
        String username = prefs.getString("current_username", "Guest_User");

        // Calculate total time until now
        long totalTime = 0;
        for (Long time : timings.values()) {
            totalTime += time;
        }

        // Save to Supabase via Repository
        gameRepository.saveGameResult(username, totalTime, lastCompletedLevel, new GameRepository.GameResultCallback() {
            @Override
            public void onSuccess() {
                Log.d("DrawerActivity", "Progress saved for " + username);
            }

            @Override
            public void onError(Exception e) {
                Log.e("DrawerActivity", "Failed to save progress", e);
            }
        });
    }
}
