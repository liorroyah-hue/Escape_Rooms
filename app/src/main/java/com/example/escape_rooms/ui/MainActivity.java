package com.example.escape_rooms.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.escape_rooms.R;
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

        viewModel = new ViewModelProvider(this).get(GameViewModel.class);

        // Initialize views
        questionsRecyclerView = findViewById(R.id.questions_recycler_view);
        btnSubmitAnswers = findViewById(R.id.btn_submit_answers);
        questionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Get initial data from Intent
        int level = getIntent().getIntExtra(EXTRA_LEVEL, 1);
        @SuppressWarnings("unchecked")
        HashMap<Integer, Long> timings = (HashMap<Integer, Long>) getIntent().getSerializableExtra(EXTRA_TIMINGS);
        
        viewModel.initLevel(level, timings);

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
                Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getNavigationEvent().observe(this, event -> {
            Intent intent;
            if (event.target == GameViewModel.NavigationTarget.NEXT_LEVEL) {
                Toast.makeText(this, getString(R.string.msg_room_cleared, event.nextLevel - 1), Toast.LENGTH_SHORT).show();
                intent = new Intent(this, MainActivity.class);
                intent.putExtra(EXTRA_LEVEL, event.nextLevel);
            } else {
                intent = new Intent(this, PlayerResultsActivity.class);
            }
            intent.putExtra(EXTRA_TIMINGS, event.timings);
            startActivity(intent);
            finish();
        });
    }
}
