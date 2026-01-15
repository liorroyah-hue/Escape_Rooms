package com.example.escape_rooms.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.escape_rooms.R;
import com.example.escape_rooms.viewmodel.ChoosingGameViewModel;

public class DrawerActivity extends AppCompatActivity {

    @Override
    @SuppressWarnings("unchecked")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);

        ImageView imageDrawer = findViewById(R.id.imageDrawer);

        // Get the data passed from ChoosingGameVarient
        Intent incomingIntent = getIntent();
        String creationType = incomingIntent.getStringExtra("CREATION_TYPE");
        ChoosingGameViewModel.QuizData aiData = (ChoosingGameViewModel.QuizData) incomingIntent.getSerializableExtra("AI_GAME_DATA");
        int level = incomingIntent.getIntExtra(MainActivity.EXTRA_LEVEL, 1);

        imageDrawer.setOnClickListener(v -> {
            Intent intent = new Intent(DrawerActivity.this, MainActivity.class);
            
            // Forward all data to MainActivity
            intent.putExtra("CREATION_TYPE", creationType);
            if (aiData != null) {
                intent.putExtra("AI_GAME_DATA", aiData);
            }
            intent.putExtra(MainActivity.EXTRA_LEVEL, level);
            
            startActivity(intent);
            finish(); // Remove this screen from back stack
        });
    }
}
