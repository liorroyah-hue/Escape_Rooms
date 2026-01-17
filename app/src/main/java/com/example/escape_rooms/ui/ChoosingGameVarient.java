package com.example.escape_rooms.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
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

public class ChoosingGameVarient extends AppCompatActivity {
    private RadioGroup radioGroupGameSubject, radioGroupCreationType;
    private String selectedCategory, selectedCreationType;
    private Button startGameButton;
    private ChoosingGameViewModel viewModel;
    private View progressFrame; 
    private View[] segments;
    
    private final Handler progressHandler = new Handler(Looper.getMainLooper());
    private int currentSegmentCount = 0;
    private boolean isNavigating = false;

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
        progressFrame = findViewById(R.id.progress_frame);
        
        initializeSegments();

        radioGroupCreationType.check(R.id.radio_existing_game);
        selectedCreationType = getString(R.string.creation_option_existing);

        observeViewModel();

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
                // Navigate immediately for existing games without showing progress bar
                navigateToDrawer();
            }
        });
    }

    private void initializeSegments() {
        segments = new View[10];
        segments[0] = findViewById(R.id.segment1);
        segments[1] = findViewById(R.id.segment2);
        segments[2] = findViewById(R.id.segment3);
        segments[3] = findViewById(R.id.segment4);
        segments[4] = findViewById(R.id.segment5);
        segments[5] = findViewById(R.id.segment6);
        segments[6] = findViewById(R.id.segment7);
        segments[7] = findViewById(R.id.segment8);
        segments[8] = findViewById(R.id.segment9);
        segments[9] = findViewById(R.id.segment10);
        resetProgressUI();
    }

    private void resetProgressUI() {
        currentSegmentCount = 0;
        for (View s : segments) s.setAlpha(0f);
    }

    private void startDiscreteProgress(boolean autoNavigate) {
        progressHandler.removeCallbacksAndMessages(null);
        resetProgressUI();
        progressFrame.setVisibility(View.VISIBLE);
        setControlsEnabled(false);
        isNavigating = false;
        incrementProgress(autoNavigate);
    }

    private void incrementProgress(boolean autoNavigate) {
        if (currentSegmentCount < 10) {
            currentSegmentCount++;
            for (int i = 0; i < segments.length; i++) segments[i].setAlpha(i < currentSegmentCount ? 1.0f : 0f);
            
            if (!autoNavigate && currentSegmentCount == 9) return; 

            if (currentSegmentCount < 10) {
                progressHandler.postDelayed(() -> incrementProgress(autoNavigate), 2000);
            } else if (autoNavigate) {
                progressHandler.postDelayed(this::navigateToDrawer, 500);
            }
        }
    }

    private void setControlsEnabled(boolean enabled) {
        startGameButton.setEnabled(enabled);
        radioGroupCreationType.setEnabled(enabled);
        for (int i = 0; i < radioGroupCreationType.getChildCount(); i++) radioGroupCreationType.getChildAt(i).setEnabled(enabled);
        radioGroupGameSubject.setEnabled(enabled);
        for (int i = 0; i < radioGroupGameSubject.getChildCount(); i++) radioGroupGameSubject.getChildAt(i).setEnabled(enabled);
    }

    private void navigateToDrawer() {
        if (isNavigating) return;
        isNavigating = true;
        Intent intent = new Intent(this, DrawerActivity.class);
        intent.putExtra(MainActivity.EXTRA_CREATION_TYPE, selectedCreationType);
        intent.putExtra(MainActivity.EXTRA_LEVEL, 1);
        startActivity(intent);
        finish();
    }

    private void observeViewModel() {
        viewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading != null && isLoading) startDiscreteProgress(false);
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                progressHandler.removeCallbacksAndMessages(null);
                progressFrame.setVisibility(View.INVISIBLE);
                setControlsEnabled(true);
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getNavigateToGame().observe(this, quizData -> {
            if (quizData != null) {
                progressHandler.removeCallbacksAndMessages(null);
                for (View s : segments) s.setAlpha(1.0f);
                
                progressFrame.postDelayed(() -> {
                    Intent intent = new Intent(this, DrawerActivity.class);
                    intent.putExtra(MainActivity.EXTRA_CREATION_TYPE, getString(R.string.creation_option_ai));
                    intent.putExtra(MainActivity.EXTRA_AI_GAME_DATA, quizData);
                    intent.putExtra(MainActivity.EXTRA_LEVEL, 1);
                    startActivity(intent);
                    finish();
                }, 600);
            }
        });
    }

    @Override
    protected void onDestroy() {
        progressHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
