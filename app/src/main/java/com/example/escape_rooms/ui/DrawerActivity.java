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
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.escape_rooms.R;
import com.example.escape_rooms.repository.GameRepository;
import com.example.escape_rooms.repository.services.GameAudioManager;
import com.example.escape_rooms.viewmodel.ChoosingGameViewModel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class DrawerActivity extends AppCompatActivity {

    private final GameRepository gameRepository = new GameRepository();
    private static final String PREFS_NAME = "EscapeRoomSolvedPrefs";
    private static final String KEY_SOLVED_IMAGES = "solved_images";

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

        // --- Handle Solved Images Logic ---
        SharedPreferences solvedPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        Set<String> solvedImageIds = solvedPrefs.getStringSet(KEY_SOLVED_IMAGES, new HashSet<>());
        
        // Reset solved images if it's the first level
        if (level == 1) {
            solvedImageIds = new HashSet<>();
            solvedPrefs.edit().putStringSet(KEY_SOLVED_IMAGES, solvedImageIds).apply();
        }

        ViewGroup container = findViewById(R.id.image_container);
        LinearLayout bottomTray = findViewById(R.id.bottom_panel_tray);
        int[] viewIds = {R.id.image1, R.id.image2, R.id.image3, R.id.image4, R.id.image5, R.id.image6};
        
        for (int id : viewIds) {
            ImageView imageView = findViewById(id);
            if (imageView != null) {
                String idStr = String.valueOf(id);
                
                if (solvedImageIds.contains(idStr)) {
                    // Move to bottom tray if already solved
                    moveViewToTray(imageView, container, bottomTray);
                } else {
                    // Normal state: Draggable and Clickable
                    imageView.setOnTouchListener(new DraggableTouchListener());
                    imageView.setOnClickListener(v -> {
                        // Mark this image as solved for the next time we return
                        Set<String> updatedSolved = new HashSet<>(solvedPrefs.getStringSet(KEY_SOLVED_IMAGES, new HashSet<>()));
                        updatedSolved.add(String.valueOf(v.getId()));
                        solvedPrefs.edit().putStringSet(KEY_SOLVED_IMAGES, updatedSolved).apply();

                        // Save progress to DB
                        saveProgressToDatabase(level - 1, timings);

                        // Audio shuffle
                        GameAudioManager.getInstance(this).stopAmbientMusic();
                        GameAudioManager.getInstance(this).startAmbientMusic();

                        // Navigation
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
    }

    private void moveViewToTray(ImageView view, ViewGroup originalContainer, LinearLayout tray) {
        // Remove from original relative container
        originalContainer.removeView(view);
        
        // Set fixed size for the tray
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                dpToPx(60), dpToPx(60));
        params.setMargins(dpToPx(8), 0, dpToPx(8), 0);
        view.setLayoutParams(params);
        
        // Disable interaction
        view.setOnTouchListener(null);
        view.setOnClickListener(null);
        view.setAlpha(0.7f); // Visual indicator it's "collected"
        
        // Add to tray
        tray.addView(view);
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
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
