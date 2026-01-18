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
import com.example.escape_rooms.viewmodel.GameSetupViewModel;

public class GameSetupActivity extends AppCompatActivity {
    private RadioGroup radioGroupGameSubject, radioGroupCreationType;
    private String selectedCategory, selectedCreationType;
    private Button startGameButton;
    private GameSetupViewModel viewModel;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_game_setup);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        viewModel = new ViewModelProvider(this).get(GameSetupViewModel.class);

        startGameButton = findViewById(R.id.startGameButton);
        radioGroupGameSubject = findViewById(R.id.radio_group_game_subject);
        radioGroupCreationType = findViewById(R.id.radio_group_creation_type);
        progressBar = findViewById(R.id.progressBar);

        // Set default creation type
        radioGroupCreationType.check(R.id.radio_existing_game);
        selectedCreationType = getString(R.string.creation_option_existing);

        observeViewModel();

        radioGroupGameSubject.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_geography) {
                selectedCategory = getString(R.string.category_geography);
            } else if (checkedId == R.id.radio_history) {
                selectedCategory = getString(R.string.category_history);
            } else if (checkedId == R.id.radio_famous) {
                selectedCategory = getString(R.string.category_famous);
            } else if (checkedId == R.id.radio_sports) {
                selectedCategory = getString(R.string.category_sports);
            }
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
            if (selectedCreationType == null) {
                Toast.makeText(this, R.string.select_creation_type_prompt, Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedCreationType.equals(getString(R.string.creation_option_ai))) {
                if (selectedCategory == null) {
                    Toast.makeText(this, R.string.select_category_prompt, Toast.LENGTH_SHORT).show();
                    return;
                }
                viewModel.generateAiGame(selectedCategory);
            } else {
                Intent intent = new Intent(this, CorridorActivity.class);
                intent.putExtra("CREATION_TYPE", selectedCreationType);
                intent.putExtra(MainActivity.EXTRA_LEVEL, 1); // Start at level 1
                startActivity(intent);
                finish();
            }
        });
    }

    private void observeViewModel() {
        viewModel.getIsLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            startGameButton.setEnabled(!isLoading);
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getNavigateToGame().observe(this, quizData -> {
            if (quizData != null) {
                Intent intent = new Intent(this, CorridorActivity.class);
                intent.putExtra("CREATION_TYPE", getString(R.string.creation_option_ai));
                intent.putExtra("AI_GAME_DATA", quizData);
                intent.putExtra(MainActivity.EXTRA_LEVEL, 1); // Start at level 1
                startActivity(intent);
                finish();
            }
        });
    }
}
