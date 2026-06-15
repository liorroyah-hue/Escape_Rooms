package com.example.escape_rooms.view;

import android.content.Intent;
import android.os.Bundle;
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
import com.example.escape_rooms.model.QuizData;
import com.example.escape_rooms.repository.QuestionRepository;
import com.example.escape_rooms.repository.services.GameAudioManager;
import com.example.escape_rooms.view.adapters.QuestionsAdapter;
import com.example.escape_rooms.viewmodel.GameViewModel;

import java.io.Serializable;
import java.util.HashMap;

/**
 * מסך השאלות — מציג 2 שאלות עם אפשרויות תשובה, בודק נכונות, ומנווט הלאה.
 */
public class MainActivity extends AppCompatActivity {

    // קבועים להעברת מידע בין Activities דרך Intent
    public static final String EXTRA_LEVEL         = "com.example.escape_rooms.LEVEL";        // רמה נוכחית
    public static final String EXTRA_TIMINGS       = "com.example.escape_rooms.TIMINGS";      // תזמוני רמות
    public static final String EXTRA_CREATION_TYPE = "com.example.escape_rooms.CREATION_TYPE"; // AI או DB
    public static final String EXTRA_AI_GAME_DATA  = "com.example.escape_rooms.AI_GAME_DATA";  // שאלות AI
    public static final String EXTRA_ROOM_ID       = "com.example.escape_rooms.ROOM_ID";       // ID החדר
    public static final String EXTRA_QUESTION_ID   = "com.example.escape_rooms.QUESTION_ID";   // ID השאלה
    public static final String EXTRA_PICTURE_ID    = "com.example.escape_rooms.PICTURE_ID";    // ID התמונה

    private RecyclerView questionsRecyclerView; // רשימת השאלות
    private QuestionsAdapter questionsAdapter;  // מנהל תצוגת השאלות
    private GameViewModel viewModel;
    private GameAudioManager audioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        audioManager = GameAudioManager.getInstance(this);
        audioManager.startAmbientMusic(); // מפעיל מוזיקת רקע

        // יוצר ViewModel עם Factory — מזריק את QuestionRepository
        GameViewModel.Factory factory = new GameViewModel.Factory(getApplication(), QuestionRepository.getInstance());
        viewModel = new ViewModelProvider(this, factory).get(GameViewModel.class);

        questionsRecyclerView = findViewById(R.id.questions_recycler_view);
        Button btnSubmitAnswers = findViewById(R.id.btn_submit_answers);
        questionsRecyclerView.setLayoutManager(new LinearLayoutManager(this)); // רשימה אנכית

        // קריאת נתונים מה-Intent
        Intent intent = getIntent();
        @SuppressWarnings("unchecked")
        HashMap<Integer, Long> timings = (HashMap<Integer, Long>) intent.getSerializableExtra(EXTRA_TIMINGS);
        String creationType = intent.getStringExtra(EXTRA_CREATION_TYPE);
        int level = intent.getIntExtra(EXTRA_LEVEL, 1);
        int roomId = intent.getIntExtra(EXTRA_ROOM_ID, 0); // ID החדר לסינון שאלות

        // מאתחל לפי סוג המשחק
        if (getString(R.string.creation_option_ai).equals(creationType)) {
            QuizData quizData = (QuizData) intent.getSerializableExtra(EXTRA_AI_GAME_DATA);
            viewModel.initAiGame(quizData, level, timings); // שאלות מ-Gemini
        } else {
            viewModel.initLevel(level, timings, roomId); // שאלות מ-Supabase לפי חדר
        }

        observeViewModel(roomId); // מתחיל להאזין לשינויים

        // לחיצה על "שלח תשובות" — שולח ל-ViewModel לבדיקה
        btnSubmitAnswers.setOnClickListener(v -> {
            if (questionsAdapter != null) {
                viewModel.verifyAndSubmit(questionsAdapter.getSelectedAnswers());
            }
        });
    }

    /**
     * מאזין לשינויים ב-ViewModel ומעדכן UI/מנווט בהתאם.
     */
    private void observeViewModel(int roomId) {
        // שאלות חדשות הגיעו — יוצר Adapter ומציג
        viewModel.getCurrentQuestions().observe(this, questions -> {
            questionsAdapter = new QuestionsAdapter(
                    this,
                    questions.getQuestionsList(),        // רשימת השאלות
                    questions.getQuestionsToAnswers()    // אפשרויות לכל שאלה
            );
            questionsRecyclerView.setAdapter(questionsAdapter);
        });

        // הודעת שגיאה/הנחיה — מציג Toast עם צליל שגיאה
        viewModel.getToastMessage().observe(this, message -> {
            if (message == null) return;
            audioManager.playErrorSound();
            int resId = 0;
            if ("msg_answer_all".equals(message))     resId = R.string.msg_answer_all; // "יש לענות על כל השאלות"
            else if ("msg_incorrect".equals(message)) resId = R.string.msg_incorrect;  // "תשובה שגויה"
            showCustomToast(resId != 0 ? getString(resId) : message, false);
        });

        // אירוע ניווט — תשובות נכונות
        viewModel.getNavigationEvent().observe(this, event -> {
            if (event.target == GameViewModel.NavigationTarget.FIND_ITEM) {
                audioManager.playSuccessSound(); // צליל הצלחה
                Intent intent = new Intent(this, FindTheItemActivity.class);
                intent.putExtra(EXTRA_LEVEL, event.nextLevel);       // הרמה הבאה
                intent.putExtra(EXTRA_TIMINGS, event.timings);       // תזמונים מצטברים
                intent.putExtra(EXTRA_CREATION_TYPE, getIntent().getStringExtra(EXTRA_CREATION_TYPE));
                intent.putExtra(EXTRA_ROOM_ID, roomId);              // לשמירה ב-game_results
                intent.putExtra(EXTRA_QUESTION_ID, event.questionId); // לשמירה ב-game_results
                if (event.aiData != null) intent.putExtra(EXTRA_AI_GAME_DATA, (Serializable) event.aiData);
                startActivity(intent);
                finish();
            } else if (event.target == GameViewModel.NavigationTarget.RESULTS) {
                audioManager.playSuccessSound();
                Intent intent = new Intent(this, PlayerResultsActivity.class);
                intent.putExtra(EXTRA_TIMINGS, event.timings);
                intent.putExtra(EXTRA_ROOM_ID, roomId);
                intent.putExtra(EXTRA_QUESTION_ID, event.questionId);
                startActivity(intent);
                finish();
            }
        });
    }

    /**
     * מציג Toast מותאם אישית עם אייקון נעילה פתוח/סגור.
     */
    private void showCustomToast(String message, boolean isSuccess) {
        View layout = getLayoutInflater().inflate(R.layout.layout_custom_toast, null, false);
        TextView text = layout.findViewById(R.id.toast_text);
        ImageView icon = layout.findViewById(R.id.toast_icon);
        if (text != null) text.setText(message);
        // אייקון שונה לפי הצלחה/כישלון
        if (icon != null) icon.setImageResource(isSuccess ? R.drawable.ic_lock_open : R.drawable.ic_lock_closed);
        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }
}
