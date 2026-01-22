package com.example.escape_rooms.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.escape_rooms.R;
import com.example.escape_rooms.repository.GameRepository;
import com.example.escape_rooms.repository.services.GameAudioManager;
import com.example.escape_rooms.viewmodel.ChoosingGameViewModel;

import java.util.HashMap;

public class DrawerActivity extends AppCompatActivity {

    private final GameRepository gameRepository = new GameRepository();

    @Override
    @SuppressWarnings("unchecked")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);

        // Get the data passed from ChoosingGameVarient or MainActivity
        Intent incomingIntent = getIntent();
        String creationType = incomingIntent.getStringExtra(MainActivity.EXTRA_CREATION_TYPE);
        ChoosingGameViewModel.QuizData aiData = (ChoosingGameViewModel.QuizData) incomingIntent.getSerializableExtra(MainActivity.EXTRA_AI_GAME_DATA);
        int level = incomingIntent.getIntExtra(MainActivity.EXTRA_LEVEL, 1);
        HashMap<Integer, Long> timings = (HashMap<Integer, Long>) incomingIntent.getSerializableExtra(MainActivity.EXTRA_TIMINGS);

        // --- Make each ImageView Draggable ---
        ViewGroup container = findViewById(R.id.image_container);
        int[] viewIds = {R.id.image1, R.id.image2, R.id.image3, R.id.image4, R.id.image5, R.id.image6};
        
        for (int id : viewIds) {
            ImageView imageView = container.findViewById(id);
            if (imageView != null) {
                imageView.setOnTouchListener(new DraggableTouchListener());
                
                // Add click listener to enter the level
                imageView.setOnClickListener(v -> {
                    // 1. Save results to DB
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
        }
    }

    private void saveProgressToDatabase(int lastCompletedLevel, HashMap<Integer, Long> timings) {
        if (timings == null || lastCompletedLevel < 1) return;

        SharedPreferences prefs = getSharedPreferences("EscapeRoomPrefs", Context.MODE_PRIVATE);
        String username = prefs.getString("current_username", "Guest_User");

        long totalTime = 0;
        for (Long time : timings.values()) {
            totalTime += time;
        }

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

    private static class DraggableTouchListener implements View.OnTouchListener {
        private float dX, dY;
        private static final int CLICK_ACTION_THRESHOLD = 10;
        private float startX, startY;

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startX = event.getRawX();
                    startY = event.getRawY();
                    dX = view.getX() - startX;
                    dY = view.getY() - startY;
                    view.bringToFront();
                    break;

                case MotionEvent.ACTION_MOVE:
                    view.animate()
                            .x(event.getRawX() + dX)
                            .y(event.getRawY() + dY)
                            .setDuration(0)
                            .start();
                    break;

                case MotionEvent.ACTION_UP:
                    float endX = event.getRawX();
                    float endY = event.getRawY();
                    if (Math.abs(startX - endX) < CLICK_ACTION_THRESHOLD && Math.abs(startY - endY) < CLICK_ACTION_THRESHOLD) {
                        view.performClick();
                    }
                    break;

                default:
                    return false;
            }
            return true;
        }
    }
}
