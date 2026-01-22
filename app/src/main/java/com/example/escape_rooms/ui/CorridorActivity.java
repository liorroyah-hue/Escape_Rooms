package com.example.escape_rooms.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.escape_rooms.R;
import com.example.escape_rooms.repository.services.GameAudioManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CorridorActivity extends AppCompatActivity {

    private ImageView doorImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_corridor);
        
        // Start the background music already in the corridor
        GameAudioManager.getInstance(this).startAmbientMusic();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        doorImage = findViewById(R.id.door_image);

        List<Integer> doorImages = new ArrayList<>();
        doorImages.add(R.drawable.ic_escape_lock_closed);
        doorImages.add(android.R.drawable.ic_dialog_map);
        doorImages.add(android.R.drawable.ic_menu_search);
        doorImages.add(android.R.drawable.ic_menu_help);

        Random random = new Random();
        int randomImageResId = doorImages.get(random.nextInt(doorImages.size()));
        doorImage.setImageResource(randomImageResId);

        Intent originalIntent = getIntent();

        doorImage.setOnClickListener(v -> {
            Intent gameIntent = new Intent(CorridorActivity.this, MainActivity.class);
            // Unified way to pass all extras including EXTRA_AI_GAME_DATA
            if (originalIntent.getExtras() != null) {
                gameIntent.putExtras(originalIntent.getExtras());
            }
            startActivity(gameIntent);
            finish();
        });
    }
}
