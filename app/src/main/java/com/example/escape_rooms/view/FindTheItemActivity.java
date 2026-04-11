package com.example.escape_rooms.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.escape_rooms.R;
import com.example.escape_rooms.repository.services.GameAudioManager;
import com.example.escape_rooms.viewmodel.GameViewModel;

import java.io.Serializable;
import java.util.Random;

public class FindTheItemActivity extends AppCompatActivity {
    Button invisibleButton;
    ImageView findItemImage;
    TextView textForImage;
    private int[] findItemInImages = {R.drawable.find_the_item1, R.drawable.find_the_item2, R.drawable.find_the_item3, R.drawable.find_the_item4, R.drawable.find_the_item5};
    String[] textForImageString = {"find the butterfly", "find the bunny", "find the bone", "find the giraffe", "find the leaf"};
    private int[] CordsX = {80, 252, 20, 16, 336};
    private int[] CordsY = {632, 480, 476, 100, 264};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fing_the_item);

        invisibleButton = findViewById(R.id.invisibleButton);
        findItemImage = findViewById(R.id.findItemImage);
        textForImage = findViewById(R.id.textForImage);

        Intent intent = getIntent();
        int nextLevel = intent.getIntExtra(MainActivity.EXTRA_LEVEL, 1);
        String creationType = intent.getStringExtra(MainActivity.EXTRA_CREATION_TYPE);
        Serializable aiData = intent.getSerializableExtra(MainActivity.EXTRA_AI_GAME_DATA);
        Serializable timings = intent.getSerializableExtra(MainActivity.EXTRA_TIMINGS);

        Random random = new Random();
        int randomIndex = random.nextInt(textForImageString.length);
        textForImage.setText(textForImageString[randomIndex]);
        findItemImage.setImageResource(findItemInImages[randomIndex]);

        MoveButtonToCorrectPlace(invisibleButton, randomIndex);

        invisibleButton.setOnClickListener(v -> {
            GameAudioManager.getInstance(this).playSuccessSound();

            Intent nextIntent;
            // Logic to determine if we move to the next room briefing or results
            if (nextLevel > GameViewModel.MAX_LEVELS) {
                // If we finished the last level (now 5), move to Results
                nextIntent = new Intent(FindTheItemActivity.this, PlayerResultsActivity.class);
            } else {
                // Move to the next room briefing
                nextIntent = new Intent(FindTheItemActivity.this, DrawerActivity.class);
            }

            nextIntent.putExtra(MainActivity.EXTRA_LEVEL, nextLevel);
            nextIntent.putExtra(MainActivity.EXTRA_CREATION_TYPE, creationType);
            if (aiData != null) {
                nextIntent.putExtra(MainActivity.EXTRA_AI_GAME_DATA, aiData);
            }
            if (timings != null) {
                nextIntent.putExtra(MainActivity.EXTRA_TIMINGS, timings);
            }
            startActivity(nextIntent);
            finish();
        });
    }

    public void MoveButtonToCorrectPlace(Button button, int index) {
        float density = getResources().getDisplayMetrics().density;
        button.setTranslationX((float) CordsX[index] * density);
        button.setTranslationY((float) CordsY[index] * density);
    }
}
