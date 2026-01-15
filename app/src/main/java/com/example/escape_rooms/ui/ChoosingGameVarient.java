package com.example.escape_rooms.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
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
    private ValueAnimator currentAnimator;

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
            if (checkedId == R.id.radio_geography) {
                selectedCategory = getString(R.string.category_geography);
            } else if (checkedId == R.id.radio_history) {
                selectedCategory = getString(R.string.category_history);
            } else if (checkedId == R.id.radio_famous) {
                selectedCategory = getString(R.string.category_famous);
            } else if (checkedId == R.id.radio_sports) {
                selectedCategory = getString(R.string.category_sports);
            } else if (checkedId == R.id.radio_science) {
                selectedCategory = getString(R.string.category_science);
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
                startProgressAnimation(3000, true);
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
        
        resetProgress();
    }

    private void resetProgress() {
        for (View s : segments) {
            s.setAlpha(0f);
        }
    }

    private void startProgressAnimation(long duration, boolean navigateOnEnd) {
        if (currentAnimator != null) currentAnimator.cancel();
        
        progressFrame.setVisibility(View.VISIBLE);
        startGameButton.setEnabled(false);
        resetProgress();
        
        // Use float for smoother calculation of integer steps
        currentAnimator = ValueAnimator.ofFloat(0f, 10f);
        currentAnimator.setDuration(duration);
        currentAnimator.setInterpolator(new LinearInterpolator());
        currentAnimator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            int count = (int) Math.ceil(value);
            if (count < 1) count = 1;
            
            // In AI mode, we wait at segment 9 until the API call finishes
            if (!navigateOnEnd && count >= 10) {
                count = 9;
            }
            
            updateProgressDisplay(count);
        });
        
        if (navigateOnEnd) {
            currentAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    navigateToDrawer();
                }
            });
        }
        currentAnimator.start();
    }

    private void updateProgressDisplay(int count) {
        for (int i = 0; i < segments.length; i++) {
            if (i < count) {
                segments[i].setAlpha(1.0f);
            } else {
                segments[i].setAlpha(0f);
            }
        }
    }

    private void navigateToDrawer() {
        Intent intent = new Intent(this, DrawerActivity.class);
        intent.putExtra(MainActivity.EXTRA_CREATION_TYPE, selectedCreationType);
        intent.putExtra(MainActivity.EXTRA_LEVEL, 1);
        startActivity(intent);
    }

    private void observeViewModel() {
        viewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading != null && isLoading) {
                // For AI, start a slow animation that fills 9 segments
                startProgressAnimation(15000, false);
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                if (currentAnimator != null) currentAnimator.cancel();
                progressFrame.setVisibility(View.INVISIBLE);
                startGameButton.setEnabled(true);
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getNavigateToGame().observe(this, quizData -> {
            if (quizData != null) {
                if (currentAnimator != null) currentAnimator.cancel();
                
                // Gemini is done! Instantly fill all segments.
                updateProgressDisplay(10);
                
                progressFrame.postDelayed(() -> {
                    Intent intent = new Intent(this, DrawerActivity.class);
                    intent.putExtra(MainActivity.EXTRA_CREATION_TYPE, getString(R.string.creation_option_ai));
                    intent.putExtra(MainActivity.EXTRA_AI_GAME_DATA, quizData);
                    startActivity(intent);
                }, 600);
            }
        });
    }
}
