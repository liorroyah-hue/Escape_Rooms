package com.example.escape_rooms.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.escape_rooms.R;
import com.example.escape_rooms.model.FindItemTask;
import com.example.escape_rooms.repository.QuestionRepository;
import com.example.escape_rooms.repository.services.GameAudioManager;
import com.example.escape_rooms.viewmodel.GameViewModel;

import java.io.Serializable;

public class FindTheItemActivity extends AppCompatActivity {
    private Button invisibleButton;
    private ImageView findItemImage;
    private TextView textForImage;

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

        // Fetch task from Supabase
        QuestionRepository.getInstance().getRandomFindItemTask(new QuestionRepository.FindItemCallback() {
            @Override
            public void onSuccess(FindItemTask task) {
                runOnUiThread(() -> {
                    textForImage.setText(task.getPromptText());
                    
                    // Use Glide to load the image from the URL in the database
                    Glide.with(FindTheItemActivity.this)
                            .load(task.getImageName()) // image_name field must contain the full public URL
                            .placeholder(R.drawable.find_the_item2) // Default while loading
                            .error(android.R.drawable.stat_notify_error) // Show if URL is broken
                            .centerCrop()
                            .into(findItemImage);
                    
                    // Place button using coordinates from DB
                    MoveButtonToCorrectPlace(invisibleButton, task.getXCord(), task.getYCord());
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    Log.e("FindTheItem", "Failed to fetch task", e);
                    Toast.makeText(FindTheItemActivity.this, "Error loading task from cloud", Toast.LENGTH_SHORT).show();
                });
            }
        });

        invisibleButton.setOnClickListener(v -> {
            GameAudioManager.getInstance(this).playSuccessSound();

            if (nextLevel > GameViewModel.MAX_LEVELS) {
                Intent resultsIntent = new Intent(FindTheItemActivity.this, PlayerResultsActivity.class);
                resultsIntent.putExtra(MainActivity.EXTRA_TIMINGS, timings);
                startActivity(resultsIntent);
            } else {
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
            }
            finish();
        });
    }

    /**
     * Places the button using coordinates from the database.
     */
    public void MoveButtonToCorrectPlace(Button button, int x_dp, int y_dp) {
        float density = getResources().getDisplayMetrics().density;
        float cordX_px = (float) x_dp * density;
        float cordY_px = (float) y_dp * density;

        button.setTranslationX(cordX_px);
        button.setTranslationY(cordY_px);
    }
}
