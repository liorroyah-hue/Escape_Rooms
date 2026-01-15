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
import com.example.escape_rooms.viewmodel.ChoosingGameViewModel;
import com.example.escape_rooms.viewmodel.GameViewModel;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    public static final String EXTRA_LEVEL = "com.example.escape_rooms.LEVEL";
    public static final String EXTRA_TIMINGS = "com.example.escape_rooms.TIMINGS";

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

        // Get initial data from Intent
        Intent intent = getIntent();
        String creationType = intent.getStringExtra("CREATION_TYPE");
        @SuppressWarnings("unchecked")
        HashMap<Integer, Long> timings = (HashMap<Integer, Long>) intent.getSerializableExtra(EXTRA_TIMINGS);

        if (getString(R.string.creation_option_ai).equals(creationType)) {
            ChoosingGameViewModel.QuizData quizData = (ChoosingGameViewModel.QuizData) intent.getSerializableExtra("AI_GAME_DATA");
            viewModel.initAiGame(quizData, timings);
        } else {
            int level = intent.getIntExtra(EXTRA_LEVEL, 1);
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

        viewModel.getToastMessage().observe(this, messageKey -> {
            int resId = 0;
            if ("msg_answer_all".equals(messageKey)) resId = R.string.msg_answer_all;
            else if ("msg_incorrect".equals(messageKey)) resId = R.string.msg_incorrect;
            
            if (resId != 0) {
                showCustomToast(getString(resId), false);
            }
        });

        viewModel.getNavigationEvent().observe(this, event -> {
            if (event.target == GameViewModel.NavigationTarget.NEXT_LEVEL) {
                showCustomToast(getString(R.string.msg_room_cleared, event.nextLevel - 1), true);
                
                // Navigate to DrawerActivity instead of directly to MainActivity
                Intent intent = new Intent(this, DrawerActivity.class);
                intent.putExtra("CREATION_TYPE", getIntent().getStringExtra("CREATION_TYPE"));
                if (getIntent().hasExtra("AI_GAME_DATA")) {
                    intent.putExtra("AI_GAME_DATA", getIntent().getSerializableExtra("AI_GAME_DATA"));
                }
                intent.putExtra(EXTRA_LEVEL, event.nextLevel);
                intent.putExtra(EXTRA_TIMINGS, event.timings);
                startActivity(intent);
                finish();
            } else {
                Intent intent = new Intent(this, PlayerResultsActivity.class);
                intent.putExtra(EXTRA_TIMINGS, event.timings);
                startActivity(intent);
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
            icon.setImageResource(R.drawable.ic_lock_open);
        } else {
            icon.setImageResource(R.drawable.ic_lock_closed);
        }

        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }
}
