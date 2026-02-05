package com.example.escape_rooms.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.escape_rooms.view.CorridorActivity;
import com.example.escape_rooms.R;
import com.example.escape_rooms.repository.services.GameAudioManager;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Random;

public class FindTheItemActivity extends AppCompatActivity {
    Button invisibleButton;
    ImageView findItemImage;
    TextView textForImage;
    private int[] findItemInImages = {R.drawable.find_the_item1, R.drawable.find_the_item2, R.drawable.find_the_item3, R.drawable.find_the_item4, R.drawable.find_the_item5};
    String[] textForImageString = {"find the butterfly", "find the bunny", "find the bone", "find the giraffe", "find the leaf"};
    private int[] CordsX = {80, 252, 16, 16, 336};
    private int[] CordsY = {60, 204, 208, 568, 424};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fing_the_item);

        invisibleButton = findViewById(R.id.invisibleButton);
        findItemImage = findViewById(R.id.findItemImage);
        textForImage = findViewById(R.id.textForImage);

        // Get the data for the *next* level, passed from MainActivity
        Intent intent = getIntent();
        int nextLevel = intent.getIntExtra(MainActivity.EXTRA_LEVEL, 1);
        String creationType = intent.getStringExtra(MainActivity.EXTRA_CREATION_TYPE);
        Serializable aiData = intent.getSerializableExtra(MainActivity.EXTRA_AI_GAME_DATA);
        Serializable timings = intent.getSerializableExtra(MainActivity.EXTRA_TIMINGS);

        // Set up the current level's puzzle
        Random random = new Random();
        int randomIndex = random.nextInt(textForImageString.length);
        textForImage.setText(textForImageString[randomIndex]);
        findItemImage.setImageResource(findItemInImages[randomIndex]);

        // Call the corrected method to place the button
        MoveButtonToCorrectPlace(invisibleButton, randomIndex);

        invisibleButton.setOnClickListener(v -> {
            GameAudioManager.getInstance(this).playSuccessSound();

            // Navigate to the Corridor, passing the data for the next level
            Intent corridorIntent = new Intent(FindTheItemActivity.this, DrawerActivity.class);
            corridorIntent.putExtra(MainActivity.EXTRA_LEVEL, nextLevel);
            corridorIntent.putExtra(MainActivity.EXTRA_CREATION_TYPE, creationType);
            if (aiData != null) {
                corridorIntent.putExtra(MainActivity.EXTRA_AI_GAME_DATA, aiData);
            }
            if (timings != null) {
                corridorIntent.putExtra(MainActivity.EXTRA_TIMINGS, timings);
            }
            startActivity(corridorIntent);
            finish();
        });
    }

    /**
     * Correctly places the button using the provided index.
     */
    public void MoveButtonToCorrectPlace(Button button, int index) {
        // The coordinates are now accessed directly with the correct index
        button.setX(CordsX[index]);
        button.setY(CordsY[index]);
    }
}
