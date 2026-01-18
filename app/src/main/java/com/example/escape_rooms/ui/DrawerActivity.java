package com.example.escape_rooms.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.escape_rooms.R;
import com.example.escape_rooms.repository.services.GameAudioManager;
import com.example.escape_rooms.viewmodel.ChoosingGameViewModel;

import java.util.HashMap;

public class DrawerActivity extends AppCompatActivity {

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

        // Logic for image based on level
        int[] drawerImages = {
            R.drawable.table_with_drawer, R.drawable.drawer_old, R.drawable.drawer_white,
            R.drawable.drawer_black, R.drawable.safe_black, R.drawable.sade_white,
            R.drawable.table_with_drawer, R.drawable.drawer_old, R.drawable.drawer_white,
            R.drawable.drawer_black
        };
        imageDrawer.setImageResource(drawerImages[(level - 1) % 10]);

        imageDrawer.setOnClickListener(v -> {
            // Trigger a NEW random ambient track when entering the room
            GameAudioManager.getInstance(this).stopAmbientMusic();
            GameAudioManager.getInstance(this).startAmbientMusic();

            Intent intent = new Intent(DrawerActivity.this, MainActivity.class);
            // Forward everything correctly using the unified keys
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
