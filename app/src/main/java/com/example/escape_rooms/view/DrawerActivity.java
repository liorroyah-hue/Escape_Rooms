package com.example.escape_rooms.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.escape_rooms.R;
import com.example.escape_rooms.model.QuizData;
import com.example.escape_rooms.repository.GameRepository;
import com.example.escape_rooms.repository.QuestionRepository;
import com.example.escape_rooms.repository.services.GameAudioManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DrawerActivity extends AppCompatActivity {

    private static final String TAG = "DrawerActivity";
    private static final String PREFS_NAME = "EscapeRoomSolvedPrefs";
    private static final String KEY_SOLVED_IMAGES = "solved_images";
    private static final String KEY_SELECTED_ROOM_ID = "selected_room_id";

    private static final String STORAGE_BASE_URL =
            "https://wjwbshqrvbgdtqanztqz.supabase.co/storage/v1/object/public/";
    private static final String BACKGROUND_BUCKET = "Escape_Room_backround";
    private static final String OBJECTS_BUCKET    = "clickable_object";

    private final GameRepository gameRepository = new GameRepository();
    private final QuestionRepository questionRepository = QuestionRepository.getInstance();

    @Override
    @SuppressWarnings("unchecked")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);

        Intent incomingIntent = getIntent();
        String creationType = incomingIntent.getStringExtra(MainActivity.EXTRA_CREATION_TYPE);

        QuizData aiData;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            aiData = incomingIntent.getSerializableExtra(MainActivity.EXTRA_AI_GAME_DATA, QuizData.class);
        } else {
            aiData = (QuizData) incomingIntent.getSerializableExtra(MainActivity.EXTRA_AI_GAME_DATA);
        }

        int level = incomingIntent.getIntExtra(MainActivity.EXTRA_LEVEL, 1);
        HashMap<Integer, Long> timings = (HashMap<Integer, Long>) incomingIntent.getSerializableExtra(MainActivity.EXTRA_TIMINGS);

        SharedPreferences solvedPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        Set<String> solvedImageIds = new HashSet<>(solvedPrefs.getStringSet(KEY_SOLVED_IMAGES, new HashSet<>()));

        // On a new game clear state and pick a new random room
        if (level == 1) {
            solvedImageIds = new HashSet<>();
            solvedPrefs.edit()
                    .putStringSet(KEY_SOLVED_IMAGES, solvedImageIds)
                    .remove(KEY_SELECTED_ROOM_ID)
                    .apply();
        }

        final Set<String> finalSolvedImageIds = solvedImageIds;
        int savedRoomId = solvedPrefs.getInt(KEY_SELECTED_ROOM_ID, -1);

        if (savedRoomId != -1) {
            loadRoomAndPopulate(savedRoomId, finalSolvedImageIds, solvedPrefs, level, timings, creationType, aiData);
        } else {
            questionRepository.getRandomRoom(new QuestionRepository.RoomCallback() {
                @Override
                public void onSuccess(QuestionRepository.RoomData room) {
                    solvedPrefs.edit().putInt(KEY_SELECTED_ROOM_ID, room.roomId).apply();
                    runOnUiThread(() -> populateUI(room, finalSolvedImageIds, solvedPrefs,
                            level, timings, creationType, aiData));
                }
                @Override
                public void onError(Exception e) { Log.e(TAG, "Failed to pick random room", e); }
            });
        }
    }

    private void loadRoomAndPopulate(int roomId, Set<String> solvedImageIds,
                                     SharedPreferences solvedPrefs, int level,
                                     HashMap<Integer, Long> timings, String creationType, QuizData aiData) {
        questionRepository.getRoomById(roomId, new QuestionRepository.RoomCallback() {
            @Override
            public void onSuccess(QuestionRepository.RoomData room) {
                runOnUiThread(() -> populateUI(room, solvedImageIds, solvedPrefs,
                        level, timings, creationType, aiData));
            }
            @Override
            public void onError(Exception e) { Log.e(TAG, "Failed to load room id=" + roomId, e); }
        });
    }

    private void populateUI(QuestionRepository.RoomData room, Set<String> solvedImageIds,
                             SharedPreferences solvedPrefs, int level,
                             HashMap<Integer, Long> timings, String creationType, QuizData aiData) {

        // ── Background ──────────────────────────────────────────────────────
        View rootLayout = findViewById(R.id.main);
        if (rootLayout != null && room.background != null && !room.background.isEmpty()) {
            String bgUrl = STORAGE_BASE_URL + BACKGROUND_BUCKET + "/" + room.background.trim();
            Glide.with(this).load(bgUrl).into(new CustomTarget<Drawable>() {
                @Override
                public void onResourceReady(Drawable resource, @Nullable Transition<? super Drawable> t) {
                    rootLayout.setBackground(resource);
                }
                @Override public void onLoadCleared(@Nullable Drawable placeholder) {}
            });
        }

        // ── Clickable images ─────────────────────────────────────────────────
        List<String> objects = room.getClickableObjects();
        ViewGroup container = findViewById(R.id.image_container);
        LinearLayout bottomTray = findViewById(R.id.bottom_panel_tray);
        int[] viewIds = {R.id.image1, R.id.image2, R.id.image3, R.id.image4, R.id.image5,
                         R.id.image6, R.id.image7, R.id.image8, R.id.image9, R.id.image10};

        GameAudioManager audioManager = GameAudioManager.getInstance(this);

        for (int i = 0; i < viewIds.length; i++) {
            ImageView imageView = findViewById(viewIds[i]);
            if (imageView == null) continue;

            if (objects != null && i < objects.size()) {
                String imgUrl = STORAGE_BASE_URL + OBJECTS_BUCKET + "/" + objects.get(i).trim();
                Glide.with(this).load(imgUrl).into(imageView);
            }

            String idStr = String.valueOf(viewIds[i]);
            if (solvedImageIds.contains(idStr)) {
                moveViewToTray(imageView, container, bottomTray);
            } else {
                imageView.setOnTouchListener(new DraggableTouchListener());
                Set<String> finalSolvedImageIds = solvedImageIds;
                imageView.setOnClickListener(v -> {
                    finalSolvedImageIds.add(String.valueOf(v.getId()));
                    solvedPrefs.edit().putStringSet(KEY_SOLVED_IMAGES, finalSolvedImageIds).apply();

                    audioManager.stopAmbientMusic();
                    audioManager.startAmbientMusic();

                    Intent intent = new Intent(DrawerActivity.this, MainActivity.class);
                    intent.putExtra(MainActivity.EXTRA_CREATION_TYPE, creationType);
                    if (aiData != null) intent.putExtra(MainActivity.EXTRA_AI_GAME_DATA, aiData);
                    intent.putExtra(MainActivity.EXTRA_LEVEL, level);
                    intent.putExtra(MainActivity.EXTRA_TIMINGS, timings);
                    intent.putExtra(MainActivity.EXTRA_ROOM_ID, room.roomId); // pass room_id forward
                    startActivity(intent);
                    finish();
                });
            }
        }
    }

    private void moveViewToTray(ImageView view, ViewGroup originalContainer, LinearLayout tray) {
        originalContainer.removeView(view);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dpToPx(50), dpToPx(50));
        params.setMargins(dpToPx(4), 0, dpToPx(4), 0);
        view.setLayoutParams(params);
        view.setOnTouchListener(null);
        view.setOnClickListener(null);
        view.setAlpha(0.6f);
        tray.addView(view);
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    private static class DraggableTouchListener implements View.OnTouchListener {
        private float dX, dY, startX, startY;
        private static final int THRESHOLD = 10;

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View view, android.view.MotionEvent event) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent == null) return false;
            switch (event.getAction()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    startX = event.getRawX(); startY = event.getRawY();
                    dX = view.getX() - startX; dY = view.getY() - startY;
                    view.bringToFront(); break;
                case android.view.MotionEvent.ACTION_MOVE:
                    view.animate()
                        .x(Math.max(0, Math.min(event.getRawX() + dX, parent.getWidth() - view.getWidth())))
                        .y(Math.max(0, Math.min(event.getRawY() + dY, parent.getHeight() - view.getHeight())))
                        .setDuration(0).start(); break;
                case android.view.MotionEvent.ACTION_UP:
                    if (Math.abs(startX - event.getRawX()) < THRESHOLD &&
                        Math.abs(startY - event.getRawY()) < THRESHOLD) view.performClick();
                    break;
                default: return false;
            }
            return true;
        }
    }
}
