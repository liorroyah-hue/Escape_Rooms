package com.example.escape_rooms.view;

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

/**
 * מסך בחירת סוג משחק — AI (שאלות מ-Gemini) או DB (שאלות מ-Supabase).
 * כולל בחירת נושא לשאלות AI ואנימציית התקדמות.
 */
public class ChoosingGameVariantActivity extends AppCompatActivity {
    private RadioGroup radioGroupGameSubject; // בחירת נושא (גיאוגרפיה, היסטוריה...)
    private RadioGroup radioGroupGameSource;  // בחירת מקור (AI או DB)
    private View cardSelection;              // כרטיס בחירת נושא — מוסתר במצב DB
    private String selectedCategory, selectedCreationType; // ערכים שנבחרו
    private Button startGameButton;
    private ChoosingGameViewModel viewModel;
    private View progressFrame;              // מסגרת אנימציית ההתקדמות
    private View[] segments;                 // 10 פסי ההתקדמות

    // Handler לתזמון אנימציות על ה-UI Thread
    private final Handler progressHandler = new Handler(Looper.getMainLooper());
    private int currentSegmentCount = 0; // כמה פסים מוצגים כרגע
    private boolean isNavigating = false; // מונע ניווט כפול

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_choosing_game_varient);

        // Padding לפי גבולות המסך
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main),
                new androidx.core.view.OnApplyWindowInsetsListener() {
                    // מאזין לשינויים בגבולות המסך — מוסיף padding כדי שתוכן לא יתחבא מאחורי שורת הסטטוס והניווט
                    @Override
                    public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
                        Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                        v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                        return insets;
                    }
                });

        viewModel = new ViewModelProvider(this).get(ChoosingGameViewModel.class);

        // קישור אלמנטי UI
        startGameButton = findViewById(R.id.startGameButton);
        radioGroupGameSubject = findViewById(R.id.radio_group_game_subject);
        radioGroupGameSource = findViewById(R.id.radio_group_game_source);
        cardSelection = findViewById(R.id.cardSelection);
        progressFrame = findViewById(R.id.progress_frame);

        initializeSegments(); // מאתחל את 10 פסי ההתקדמות

        // ברירת מחדל — AI (כפי שמסומן ב-layout)
        selectedCreationType = getString(R.string.creation_option_ai);

        observeViewModel();

        // מאזין לשינוי מקור השאלות
        radioGroupGameSource.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_source_db) {
                selectedCreationType = getString(R.string.creation_option_existing);
                cardSelection.setVisibility(View.GONE); // DB לא צריך בחירת נושא
            } else {
                selectedCreationType = getString(R.string.creation_option_ai);
                cardSelection.setVisibility(View.VISIBLE); // AI צריך בחירת נושא
            }
        });

        // מאזין לבחירת נושא
        radioGroupGameSubject.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_geography) selectedCategory = getString(R.string.category_geography);
            else if (checkedId == R.id.radio_history) selectedCategory = getString(R.string.category_history);
            else if (checkedId == R.id.radio_famous) selectedCategory = getString(R.string.category_famous);
            else if (checkedId == R.id.radio_sports) selectedCategory = getString(R.string.category_sports);
            else if (checkedId == R.id.radio_science) selectedCategory = getString(R.string.category_science);
        });

        // לחיצה על "התחל משחק"
        startGameButton.setOnClickListener(v -> {
            String aiOption = getString(R.string.creation_option_ai);
            if (aiOption.equals(selectedCreationType)) {
                // מצב AI — חייב לבחור נושא
                if (selectedCategory == null) {
                    Toast.makeText(this, R.string.select_category_prompt, Toast.LENGTH_SHORT).show();
                    return;
                }
                viewModel.generateAiGame(selectedCategory); // מפעיל Gemini
            } else {
                // מצב DB — עובר ישירות לחדר ללא AI
                navigateToDrawer();
            }
        });
    }

    /**
     * מאתחל מערך של 10 פסי התקדמות ומאפס אותם
     */
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

    /**
     * מאפס את האנימציה — מסתיר את כל הפסים
     */
    private void resetProgressUI() {
        currentSegmentCount = 0;
        for (View s : segments) s.setAlpha(0f); // כל הפסים שקופים
    }

    /**
     * מפעיל אנימציית התקדמות — פס אחד כל 2 שניות
     */
    private void startDiscreteProgress(boolean autoNavigate) {
        progressHandler.removeCallbacksAndMessages(null); // מבטל תזמונים קודמים
        resetProgressUI();
        progressFrame.setVisibility(View.VISIBLE); // מציג את מסגרת ההתקדמות
        setControlsEnabled(false); // משבית כפתורים בזמן טעינה
        isNavigating = false;
        incrementProgress(autoNavigate);
    }

    /**
     * מוסיף פס אחד כל 2 שניות ומנווט בסיום אם autoNavigate=true
     */
    private void incrementProgress(boolean autoNavigate) {
        if (currentSegmentCount < 10) {
            currentSegmentCount++;
            // מציג פסים עד לספירה הנוכחית
            for (int i = 0; i < segments.length; i++) segments[i].setAlpha(i < currentSegmentCount ? 1.0f : 0f);

            if (!autoNavigate && currentSegmentCount == 9) return; // עוצר לפני האחרון אם לא autoNavigate

            if (currentSegmentCount < 10) {
                // ממשיך אחרי 2 שניות
                progressHandler.postDelayed(() -> incrementProgress(autoNavigate), 2000);
            } else if (autoNavigate) {
                // כל הפסים מלאים — ממתין 0.5 שניה ומנווט
                progressHandler.postDelayed(this::navigateToDrawer, 500);
            }
        }
    }

    /**
     * משבית/מאפשר פקדי ה-UI
     */
    private void setControlsEnabled(boolean enabled) {
        startGameButton.setEnabled(enabled);
        radioGroupGameSubject.setEnabled(enabled);
        // משבית/מאפשר כל כפתור הרדיו בנפרד
        for (int i = 0; i < radioGroupGameSubject.getChildCount(); i++)
            radioGroupGameSubject.getChildAt(i).setEnabled(enabled);
    }

    /**
     * מנווט ל-DrawerActivity עם level=1 (תחילת משחק).
     * isNavigating מונע ניווט כפול.
     */
    private void navigateToDrawer() {
        if (isNavigating) return; // מונע כפילות
        isNavigating = true;
        Intent intent = new Intent(this, DrawerActivity.class);
        intent.putExtra(MainActivity.EXTRA_CREATION_TYPE, selectedCreationType);
        intent.putExtra(MainActivity.EXTRA_LEVEL, 1); // תמיד מתחיל ברמה 1
        startActivity(intent);
        finish(); // סוגר את המסך הנוכחי
    }

    /**
     * מאזין לשינויים ב-ViewModel
     */
    private void observeViewModel() {
        // AI מתחיל לייצר שאלות — מפעיל אנימציה
        viewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading != null && isLoading) startDiscreteProgress(false);
        });

        // שגיאת AI — מציג הודעה ומאפשר ניסיון חוזר
        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                progressHandler.removeCallbacksAndMessages(null);
                progressFrame.setVisibility(View.INVISIBLE);
                setControlsEnabled(true);
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });

        // AI סיים — ממלא את כל הפסים ומנווט לחדר
        viewModel.getNavigateToGame().observe(this, quizData -> {
            if (quizData != null) {
                progressHandler.removeCallbacksAndMessages(null);
                for (View s : segments) s.setAlpha(1.0f); // כל הפסים מלאים

                // אחרי 0.6 שניות — עובר ל-DrawerActivity עם נתוני AI
                progressFrame.postDelayed(() -> {
                    Intent intent = new Intent(this, DrawerActivity.class);
                    intent.putExtra(MainActivity.EXTRA_CREATION_TYPE, getString(R.string.creation_option_ai));
                    intent.putExtra(MainActivity.EXTRA_AI_GAME_DATA, quizData); // שאלות AI
                    intent.putExtra(MainActivity.EXTRA_LEVEL, 1);
                    startActivity(intent);
                    finish();
                }, 600);
            }
        });
    }

    @Override
    protected void onDestroy() {
        progressHandler.removeCallbacksAndMessages(null); // מנקה תזמונים — מונע דליפות זיכרון
        super.onDestroy();
    }
}
