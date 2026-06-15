package com.example.escape_rooms.view;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.escape_rooms.R;
import com.example.escape_rooms.model.FindItemTask;
import com.example.escape_rooms.repository.QuestionRepository;
import com.example.escape_rooms.repository.services.GameAudioManager;
import com.example.escape_rooms.viewmodel.GameViewModel;

import java.io.Serializable;

/**
 * מסך "מצא את הפריט" — מציג תמונה עם פריט חבוי.
 * השחקן צריך ללחוץ על הפריט הנכון בתמונה.
 */
public class FindTheItemActivity extends AppCompatActivity {
    private Button invisibleButton; // כפתור שקוף הממוקם בדיוק על הפריט החבוי
    private ImageView findItemImage; // תמונת הרקע עם הפריט החבוי
    private TextView textForImage;   // הנחיה — "מצא את ה..."
    private ProgressBar loadingProgress; // מוצג בזמן טעינת התמונה

    // מזהה פרויקט Supabase לבניית URL תמונות
    private static final String PROJECT_ID = "wjwbshqrvbgdtqanztqz";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fing_the_item);

        // קישור אלמנטי UI
        invisibleButton  = findViewById(R.id.invisibleButton);
        findItemImage    = findViewById(R.id.findItemImage);
        textForImage     = findViewById(R.id.textForImage);
        loadingProgress  = findViewById(R.id.loadingProgress);

        // קריאת נתונים מה-Intent
        Intent intent    = getIntent();
        int nextLevel    = intent.getIntExtra(MainActivity.EXTRA_LEVEL, 1);           // הרמה הבאה
        String creationType = intent.getStringExtra(MainActivity.EXTRA_CREATION_TYPE); // AI או DB
        Serializable aiData  = intent.getSerializableExtra(MainActivity.EXTRA_AI_GAME_DATA); // נתוני AI
        Serializable timings = intent.getSerializableExtra(MainActivity.EXTRA_TIMINGS);      // תזמונים
        int roomId       = intent.getIntExtra(MainActivity.EXTRA_ROOM_ID, 0);         // לשמירה ב-game_results
        int questionId   = intent.getIntExtra(MainActivity.EXTRA_QUESTION_ID, 0);     // לשמירה ב-game_results

        if (loadingProgress != null) loadingProgress.setVisibility(View.VISIBLE); // מציג טעינה

        // שולף משימה אקראית מ-Supabase
        QuestionRepository.getInstance().getRandomFindItemTask(new QuestionRepository.FindItemCallback() {
            @Override
            public void onSuccess(FindItemTask task) {
                if (isFinishing() || isDestroyed()) return; // Activity כבר נסגרה

                int pictureId = task.getId(); // id_picture — ישמר ב-game_results

                // בניית URL מלא לתמונה אם צריך
                String imageUrl = task.getImageName();
                if (!imageUrl.startsWith("http")) {
                    imageUrl = "https://" + PROJECT_ID +
                            ".supabase.co/storage/v1/object/public/find-item-images/" + imageUrl;
                }

                final String finalUrl = imageUrl;
                final int xCord = task.getXCord(); // קואורדינטת X של הפריט
                final int yCord = task.getYCord(); // קואורדינטת Y של הפריט

                runOnUiThread(() -> {
                    textForImage.setText(task.getPromptText()); // מציג הנחיה

                    // טוען תמונה עם Glide
                    Glide.with(FindTheItemActivity.this)
                            .load(finalUrl)
                            .thumbnail(0.1f) // תמונה מוקטנת תחילה בזמן טעינה
                            .error(android.R.drawable.stat_notify_error) // אייקון שגיאה אם נכשל
                            .diskCacheStrategy(DiskCacheStrategy.ALL) // שמירה בקאש לטעינות מהירות
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                            Target<Drawable> target, boolean isFirstResource) {
                                    Log.e("FindTheItem", "Glide Load Failed", e);
                                    if (loadingProgress != null) loadingProgress.setVisibility(View.GONE);
                                    return false;
                                }
                                @Override
                                public boolean onResourceReady(Drawable resource, Object model,
                                                               Target<Drawable> target, DataSource dataSource,
                                                               boolean isFirstResource) {
                                    // התמונה נטענה — מסתיר ProgressBar
                                    if (loadingProgress != null) loadingProgress.setVisibility(View.GONE);
                                    return false;
                                }
                            })
                            .centerCrop() // חותך את התמונה כך שתמלא את ה-ImageView
                            .into(findItemImage);

                    // ממקם את הכפתור השקוף בדיוק על הפריט לפי הקואורדינטות מה-DB
                    MoveButtonToCorrectPlace(invisibleButton, xCord, yCord);

                    // מגדיר לחיצה על הכפתור השקוף
                    // pictureId נלכד כאן ב-closure כדי לעבור הלאה
                    invisibleButton.setOnClickListener(v -> {
                        GameAudioManager.getInstance(FindTheItemActivity.this).playSuccessSound();

                        if (nextLevel > GameViewModel.MAX_LEVELS) {
                            // סיום המשחק — עובר לתוצאות עם כל ה-IDs
                            Intent resultsIntent = new Intent(FindTheItemActivity.this, PlayerResultsActivity.class);
                            resultsIntent.putExtra(MainActivity.EXTRA_TIMINGS, timings);
                            resultsIntent.putExtra(MainActivity.EXTRA_ROOM_ID, roomId);
                            resultsIntent.putExtra(MainActivity.EXTRA_QUESTION_ID, questionId);
                            resultsIntent.putExtra(MainActivity.EXTRA_PICTURE_ID, pictureId); // id_picture
                            startActivity(resultsIntent);
                        } else {
                            // ממשיך לרמה הבאה — חוזר ל-DrawerActivity
                            Intent corridorIntent = new Intent(FindTheItemActivity.this, DrawerActivity.class);
                            corridorIntent.putExtra(MainActivity.EXTRA_LEVEL, nextLevel);
                            corridorIntent.putExtra(MainActivity.EXTRA_CREATION_TYPE, creationType);
                            if (aiData != null) corridorIntent.putExtra(MainActivity.EXTRA_AI_GAME_DATA, aiData);
                            if (timings != null) corridorIntent.putExtra(MainActivity.EXTRA_TIMINGS, timings);
                            startActivity(corridorIntent);
                        }
                        finish(); // סוגר את המסך הנוכחי
                    });
                });
            }

            @Override
            public void onError(Exception e) {
                Log.e("FindTheItem", "Repository Error", e);
                runOnUiThread(() -> {
                    if (loadingProgress != null) loadingProgress.setVisibility(View.GONE);
                    Toast.makeText(FindTheItemActivity.this, "Database Connection Error", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    /**
     * ממקם את הכפתור השקוף בדיוק על הפריט בתמונה.
     * ממתין שה-ImageView יסיים לצייר לפני חישוב המיקומים.
     */
    public void MoveButtonToCorrectPlace(Button button, int x_dp, int y_dp) {
        findItemImage.post(() -> { // post = ממתין לסיום ציור ה-layout
            float density = getResources().getDisplayMetrics().density; // יחס dp לפיקסלים

            // מיקום ה-ImageView על המסך
            int[] imageLocation = new int[2];
            findItemImage.getLocationOnScreen(imageLocation);

            // מיקום הכפתור על המסך
            int[] buttonLocation = new int[2];
            button.getLocationOnScreen(buttonLocation);

            // מחשב את הזזת הכפתור: מיקום התמונה + קואורדינטות הפריט
            button.setTranslationX((imageLocation[0] - buttonLocation[0]) + (x_dp * density));
            button.setTranslationY((imageLocation[1] - buttonLocation[1]) + (y_dp * density));
        });
    }
}
