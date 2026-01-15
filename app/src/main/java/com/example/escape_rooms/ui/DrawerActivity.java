package com.example.escape_rooms.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.escape_rooms.R;
import com.example.escape_rooms.viewmodel.ChoosingGameViewModel;

import java.util.HashMap;

public class DrawerActivity extends AppCompatActivity {

    @Override
    @SuppressWarnings("unchecked")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);

        ImageView imageDrawer = findViewById(R.id.imageDrawer);

        // Get the data passed from ChoosingGameVarient or MainActivity
        Intent incomingIntent = getIntent();
        String creationType = incomingIntent.getStringExtra(MainActivity.EXTRA_CREATION_TYPE);
        ChoosingGameViewModel.QuizData aiData = (ChoosingGameViewModel.QuizData) incomingIntent.getSerializableExtra(MainActivity.EXTRA_AI_GAME_DATA);
        int level = incomingIntent.getIntExtra(MainActivity.EXTRA_LEVEL, 1);
        HashMap<Integer, Long> timings = (HashMap<Integer, Long>) incomingIntent.getSerializableExtra(MainActivity.EXTRA_TIMINGS);

        // Change image based on level
        int[] drawerImages = {
            R.drawable.table_with_drawer, // Level 1
            R.drawable.drawer_old,        // Level 2
            R.drawable.drawer_white,      // Level 3
            R.drawable.drawer_black,      // Level 4
            R.drawable.safe_black,        // Level 5
            R.drawable.sade_white,        // Level 6
            R.drawable.table_with_drawer, // Level 7 (Reuse)
            R.drawable.drawer_old,        // Level 8
            R.drawable.drawer_white,      // Level 9
            R.drawable.drawer_black       // Level 10
        };

        // Safety check for level index
        int imageIndex = (level - 1) % drawerImages.length;
        imageDrawer.setImageResource(drawerImages[imageIndex]);

        imageDrawer.setOnClickListener(v -> {
            Intent intent = new Intent(DrawerActivity.this, MainActivity.class);
            
            // Forward all data to MainActivity
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
