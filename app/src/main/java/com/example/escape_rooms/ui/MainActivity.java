package com.example.escape_rooms.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.escape_rooms.R;
import com.example.escape_rooms.repository.QuestionRepository;
import com.example.escape_rooms.repository.services.GameAudioManager;
import com.example.escape_rooms.ui.adaptors.QuestionsAdapter;
import com.example.escape_rooms.viewmodel.ChoosingGameViewModel;
import com.example.escape_rooms.viewmodel.GameViewModel;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    // Unified Intent Keys
    public static final String EXTRA_LEVEL = "com.example.escape_rooms.LEVEL";
    public static final String EXTRA_TIMINGS = "com.example.escape_rooms.TIMINGS";
    public static final String EXTRA_CREATION_TYPE = "com.example.escape_rooms.CREATION_TYPE";
    public static final String EXTRA_AI_GAME_DATA = "com.example.escape_rooms.AI_GAME_DATA";

    private RecyclerView questionsRecyclerView;
    private QuestionsAdapter questionsAdapter;
    private GameViewModel viewModel;
    private GameAudioManager audioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        audioManager = GameAudioManager.getInstance(this);

        GameViewModel.Factory factory = new GameViewModel.Factory(getApplication(), QuestionRepository.getInstance());
        viewModel = new ViewModelProvider(this, factory).get(GameViewModel.class);

        questionsRecyclerView = findViewById(R.id.questions_recycler_view);
        Button btnSubmitAnswers = findViewById(R.id.btn_submit_answers);
        questionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        Intent intent = getIntent();
        @SuppressWarnings("unchecked")
        HashMap<Integer, Long> timings = (HashMap<Integer, Long>) intent.getSerializableExtra(EXTRA_TIMINGS);
        String creationType = intent.getStringExtra(EXTRA_CREATION_TYPE);
        int level = intent.getIntExtra(EXTRA_LEVEL, 1);

        if (getString(R.string.creation_option_ai).equals(creationType)) {
            ChoosingGameViewModel.QuizData quizData = (ChoosingGameViewModel.QuizData) intent.getSerializableExtra(EXTRA_AI_GAME_DATA);
            viewModel.initAiGame(quizData, level, timings);
        } else {
            level = intent.getIntExtra(EXTRA_LEVEL, 1);
            viewModel.initLevel(level, timings);
        }

        observeViewModel();

        btnSubmitAnswers.setOnClickListener(v -> {
            if (questionsAdapter != null) {
                viewModel.verifyAndSubmit(questionsAdapter.getSelectedAnswers());
            }
        });
    }

    private void observeViewModel() {
        viewModel.getCurrentQuestions().observe(this, questions -> {
            questionsAdapter = new QuestionsAdapter(
                    this,
                    questions.getQuestionsList(),
                    questions.getQuestionsToAnswers()
            );
            questionsRecyclerView.setAdapter(questionsAdapter);
        });

        viewModel.getToastMessage().observe(this, message -> {
            if (message == null) return;
            audioManager.playErrorSound();
            
            // Map known resource keys to direct R.string IDs to avoid getIdentifier reflection
            int resId = 0;
            if ("msg_answer_all".equals(message)) {
                resId = R.string.msg_answer_all;
            } else if ("msg_incorrect".equals(message)) {
                resId = R.string.msg_incorrect;
            }
            
            if (resId != 0) {
                showCustomToast(getString(resId), false);
            } else {
                showCustomToast(message, false);
            }
        });

        viewModel.getNavigationEvent().observe(this, event -> {
            if (event.target == GameViewModel.NavigationTarget.NEXT_LEVEL) {
                audioManager.playSuccessSound();
                audioManager.startAmbientMusic();
                showCustomToast(getString(R.string.msg_room_cleared, event.nextLevel - 1), true);
                
                Intent nextIntent = new Intent(this, DrawerActivity.class);
                nextIntent.putExtra(EXTRA_LEVEL, event.nextLevel);
                nextIntent.putExtra(EXTRA_CREATION_TYPE, getIntent().getStringExtra(EXTRA_CREATION_TYPE));
                
                if (event.aiData != null) {
                    nextIntent.putExtra(EXTRA_AI_GAME_DATA, event.aiData);
                }
                
                nextIntent.putExtra(EXTRA_TIMINGS, event.timings);
                startActivity(nextIntent);
                finish();
            } else {
                audioManager.playSuccessSound();
                Intent resultsIntent = new Intent(this, PlayerResultsActivity.class);
                resultsIntent.putExtra(EXTRA_TIMINGS, event.timings);
                startActivity(resultsIntent);
                finish();
            }
        });
    }

    private void showCustomToast(String message, boolean isSuccess) {
        LayoutInflater inflater = getLayoutInflater();
        // Fix: Inflate with null root for Toast to prevent crash
        View layout = inflater.inflate(R.layout.layout_custom_toast, null);

        TextView text = layout.findViewById(R.id.toast_text);
        ImageView icon = layout.findViewById(R.id.toast_icon);
        
        if (text != null) text.setText(message);
        if (icon != null) {
            icon.setImageResource(isSuccess ? R.drawable.ic_lock_open : R.drawable.ic_lock_closed);
        }

        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }
}
