package com.example.escape_rooms.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.escape_rooms.R;
import com.example.escape_rooms.repository.services.GameAudioManager;

import java.io.Serializable;
import java.util.Random;

public class FindTheItemActivity extends AppCompatActivity {
    Button invisibleButton;
    ImageView findItemImage;
    TextView textForImage;
    private int[] findItemInImages = {R.drawable.find_the_item1, R.drawable.find_the_item2, R.drawable.find_the_item3, R.drawable.find_the_item4, R.drawable.find_the_item5};
    String[] textForImageString = {"find the butterfly", "find the bunny", "find the bone", "find the giraffe", "find the leaf"};
    private int[] CordsX = {80, 252, 20, 16, 336};
    private int[] CordsY = {632, 408, 476, 100, 264};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fing_the_item);

        // You can get the screen's DPI like this:
        android.util.DisplayMetrics displayMetrics = new android.util.DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int dpi = displayMetrics.densityDpi;
        // Log it to see the value for your device/emulator
        Log.d("FindTheItemActivity", "Device DPI: " + dpi);

        invisibleButton = findViewById(R.id.invisibleButton);
        findItemImage =findViewById(R.id.findItemImage);
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

        findItemImage.setOnClickListener(v -> {

                });

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
     * Correctly places the button using density-independent (dp) coordinates.
     * This method converts DP values to pixels, so the button scales correctly
     * on screens with different densities.
     */
    public void MoveButtonToCorrectPlace(Button button, int index) {
        double cordX_dp = CordsX[index];
        double cordY_dp = CordsY[index];

        float density = getResources().getDisplayMetrics().density;
        float cordX_px = (float) cordX_dp * density;
        float cordY_px = (float) cordY_dp * density;

        button.setTranslationX(cordX_px);
        button.setTranslationY(cordY_px);
    }
}
