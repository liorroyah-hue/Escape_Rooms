package com.example.escape_rooms.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.escape_rooms.R;
import com.example.escape_rooms.viewmodel.ChoosingGameViewModel;

public class GameSetupActivity extends AppCompatActivity {
    private RadioGroup radioGroupGameSubject, radioGroupCreationType;
    private String selectedCategory, selectedCreationType;
    private Button startGameButton;
    private ChoosingGameViewModel viewModel;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_choosing_game_varient);
        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        viewModel = new ViewModelProvider(this).get(ChoosingGameViewModel.class);

        startGameButton = findViewById(R.id.startGameButton);
        radioGroupGameSubject = findViewById(R.id.radio_group_game_subject);
        radioGroupCreationType = findViewById(R.id.radio_group_creation_type);
        // Note: Using the progress frame from the shared layout if available
        View progressFrame = findViewById(R.id.progress_frame);

        radioGroupCreationType.check(R.id.radio_existing_game);
        selectedCreationType = getString(R.string.creation_option_existing);

        observeViewModel(progressFrame);

        radioGroupGameSubject.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_geography) selectedCategory = getString(R.string.category_geography);
            else if (checkedId == R.id.radio_history) selectedCategory = getString(R.string.category_history);
            else if (checkedId == R.id.radio_famous) selectedCategory = getString(R.string.category_famous);
            else if (checkedId == R.id.radio_sports) selectedCategory = getString(R.string.category_sports);
            else if (checkedId == R.id.radio_science) selectedCategory = getString(R.string.category_science);
        });

        radioGroupCreationType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_existing_game) {
                selectedCreationType = getString(R.string.creation_option_existing);
                radioGroupGameSubject.setVisibility(View.GONE);
            } else if (checkedId == R.id.radio_ai_game) {
                selectedCreationType = getString(R.string.creation_option_ai);
                radioGroupGameSubject.setVisibility(View.VISIBLE);
            }
        });

        startGameButton.setOnClickListener(v -> {
            if (selectedCreationType.equals(getString(R.string.creation_option_ai))) {
                if (selectedCategory == null) {
                    Toast.makeText(this, R.string.select_category_prompt, Toast.LENGTH_SHORT).show();
                    return; 
                }
                viewModel.generateAiGame(selectedCategory);
            } else {
                navigateToGame(null);
            }
        });
    }

    private void observeViewModel(View progressFrame) {
        viewModel.getIsLoading().observe(this, isLoading -> {
            if (progressFrame != null) progressFrame.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            startGameButton.setEnabled(!isLoading);
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        });

        viewModel.getNavigateToGame().observe(this, quizData -> {
            if (quizData != null) {
                navigateToGame(quizData);
            }
        });
    }

    private void navigateToGame(ChoosingGameViewModel.QuizData quizData) {
        Intent intent = new Intent(this, CorridorActivity.class);
        intent.putExtra(MainActivity.EXTRA_CREATION_TYPE, selectedCreationType);
        intent.putExtra(MainActivity.EXTRA_LEVEL, 1);
        if (quizData != null) {
            intent.putExtra(MainActivity.EXTRA_AI_GAME_DATA, quizData);
        }
        startActivity(intent);
        finish();
    }
}
