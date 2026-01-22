package com.example.escape_rooms.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.escape_rooms.ui.adapters.QuestionsAdapter;
import com.example.escape_rooms.viewmodel.GameViewModel;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_LEVEL = "com.example.escape_rooms.LEVEL";
    public static final String EXTRA_TIMINGS = "com.example.escape_rooms.TIMINGS";
    public static final String EXTRA_CREATION_TYPE = "CREATION_TYPE";
    public static final String EXTRA_AI_SUBJECT = "SELECTED_CATEGORY";

    private RecyclerView questionsRecyclerView;
    private QuestionsAdapter questionsAdapter;
    private Button btnSubmitAnswers;
    private GameViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GameViewModel.Factory factory = new GameViewModel.Factory(getApplication(), QuestionRepository.getInstance());
        viewModel = new ViewModelProvider(this, factory).get(GameViewModel.class);

        questionsRecyclerView = findViewById(R.id.questions_recycler_view);
        btnSubmitAnswers = findViewById(R.id.btn_submit_answers);
        questionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        Intent intent = getIntent();
        String creationType = intent.getStringExtra(EXTRA_CREATION_TYPE);

        if (getString(R.string.creation_option_ai).equals(creationType)) {
            String subject = intent.getStringExtra(EXTRA_AI_SUBJECT);
            int level = intent.getIntExtra(EXTRA_LEVEL, 1);
            viewModel.initAiGame(subject, level, null);
        } else {
            int level = intent.getIntExtra(EXTRA_LEVEL, 1);
            viewModel.initLevel(level, null);
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
            int resId = getResources().getIdentifier(message, "string", getPackageName());
            if (resId != 0) {
                showCustomToast(getString(resId), false);
            } else {
                showCustomToast(message, false);
            }
        });

        // Observe the new level complete event
        viewModel.getLevelCompleteEvent().observe(this, isComplete -> {
            if (isComplete) {
                showCustomToast(getString(R.string.msg_room_cleared, getIntent().getIntExtra(EXTRA_LEVEL, 0)), true);
                // Set the result and finish to return to DrawerActivity
                Intent resultIntent = new Intent();
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
    }

    private void showCustomToast(String message, boolean isSuccess) {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.layout_custom_toast, (ViewGroup) findViewById(R.id.custom_toast_container), false);

        TextView text = layout.findViewById(R.id.toast_text);
        ImageView icon = layout.findViewById(R.id.toast_icon);
        
        text.setText(message);
        
        if (isSuccess) {
            icon.setImageResource(R.drawable.ic_escape_lock_open);
        } else {
            icon.setImageResource(R.drawable.ic_escape_lock_closed);
        }

        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }
}
