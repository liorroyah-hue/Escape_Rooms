package com.example.escape_rooms.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.escape_rooms.R;
import com.example.escape_rooms.model.QuizData;
import com.example.escape_rooms.repository.GameRepository;
import com.example.escape_rooms.repository.QuestionRepository;
import com.example.escape_rooms.repository.services.GameAudioManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * מסך ה"חדר" — טוען רקע ו-10 תמונות ניתנות לגרירה מ-Supabase Storage.
 * השחקן גורר/לוחץ על תמונה כדי לעבור לשאלות.
 */
public class DrawerActivity extends AppCompatActivity {

    private static final String TAG = "DrawerActivity";
    private static final String PREFS_NAME = "EscapeRoomSolvedPrefs"; // שם קובץ SharedPreferences
    private static final String KEY_SOLVED_IMAGES = "solved_images";    // מפתח לתמונות שנפתרו
    private static final String KEY_SELECTED_ROOM_ID = "selected_room_id"; // מפתח לחדר שנבחר

    // כתובת בסיס של Supabase Storage
    private static final String STORAGE_BASE_URL = "https://wjwbshqrvbgdtqanztqz.supabase.co/storage/v1/object/public/";
    private static final String BACKGROUND_BUCKET = "Escape_Room_backround"; // bucket של תמונות רקע
    private static final String OBJECTS_BUCKET    = "clickable_object";       // bucket של תמונות ניתנות ללחיצה

    private final GameRepository gameRepository = new GameRepository();
    private final QuestionRepository questionRepository = QuestionRepository.getInstance();

    @Override
    @SuppressWarnings("unchecked")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);

        // קריאת נתונים מה-Intent שהגיע מהמסך הקודם
        Intent incomingIntent = getIntent();
        String creationType = incomingIntent.getStringExtra(MainActivity.EXTRA_CREATION_TYPE); // AI או DB
        QuizData aiData;
        // קריאת נתוני AI בהתאם לגרסת Android
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            aiData = incomingIntent.getSerializableExtra(MainActivity.EXTRA_AI_GAME_DATA, QuizData.class);
        } else {
            aiData = (QuizData) incomingIntent.getSerializableExtra(MainActivity.EXTRA_AI_GAME_DATA);
        }
        int level = incomingIntent.getIntExtra(MainActivity.EXTRA_LEVEL, 1); // רמה נוכחית
        HashMap<Integer, Long> timings = (HashMap<Integer, Long>) incomingIntent.getSerializableExtra(MainActivity.EXTRA_TIMINGS); // תזמוני רמות קודמות

        // טעינת רשימת התמונות שכבר נפתרו מהזיכרון המקומי
        SharedPreferences solvedPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        Set<String> solvedImageIds = new HashSet<>(solvedPrefs.getStringSet(KEY_SOLVED_IMAGES, new HashSet<>()));

        // משחק חדש (רמה 1) — מאפס הכל ובוחר חדר חדש
        if (level == 1) {
            solvedImageIds = new HashSet<>();
            solvedPrefs.edit()
                    .putStringSet(KEY_SOLVED_IMAGES, solvedImageIds)
                    .remove(KEY_SELECTED_ROOM_ID) // מוחק חדר שמור — יבחר חדש
                    .apply();
        }

        final Set<String> finalSolvedImageIds = solvedImageIds;
        int savedRoomId = solvedPrefs.getInt(KEY_SELECTED_ROOM_ID, -1); // קורא חדר שמור (-1 = אין)

        if (savedRoomId != -1) {
            // חדר כבר נבחר בתחילת המשחק — טוען את אותו החדר
            loadRoomAndPopulate(savedRoomId, finalSolvedImageIds, solvedPrefs, level, timings, creationType, aiData);
        } else {
            // תחילת משחק — בוחר חדר אקראי חדש
            questionRepository.getRandomRoom(new QuestionRepository.RoomCallback() {
                @Override
                public void onSuccess(QuestionRepository.RoomData room) {
                    solvedPrefs.edit().putInt(KEY_SELECTED_ROOM_ID, room.roomId).apply(); // שומר לשימוש הרמות הבאות
                    runOnUiThread(() -> populateUI(room, finalSolvedImageIds, solvedPrefs, level, timings, creationType, aiData));
                }
                @Override
                public void onError(Exception e) { Log.e(TAG, "Failed to pick random room", e); }
            });
        }
    }

    /**
     * טוען חדר ספציפי לפי ID ומציג אותו.
     * משמש כשחדר כבר נבחר בתחילת המשחק.
     */
    private void loadRoomAndPopulate(int roomId, Set<String> solvedImageIds,
                                     SharedPreferences solvedPrefs, int level,
                                     HashMap<Integer, Long> timings, String creationType, QuizData aiData) {
        questionRepository.getRoomById(roomId, new QuestionRepository.RoomCallback() {
            @Override
            public void onSuccess(QuestionRepository.RoomData room) {
                runOnUiThread(() -> populateUI(room, solvedImageIds, solvedPrefs, level, timings, creationType, aiData));
            }
            @Override
            public void onError(Exception e) { Log.e(TAG, "Failed to load room id=" + roomId, e); }
        });
    }

    /**
     * מציג את החדר — טוען רקע ו-10 תמונות ניתנות ללחיצה.
     *
     * ┌─────────────────────────────────────────────────────────────┐
     * │                    מה הפונקציה עושה                         │
     * ├─────────────────────────────────────────────────────────────┤
     * │  חלק 1: טוענת תמונת רקע מ-Supabase Storage דרך Glide.     │
     * │         Glide עובד אסינכרונית — הרקע נטען ברקע בזמן        │
     * │         שהשאר ממשיך. CustomTarget הוא callback שנקרא        │
     * │         רק כשהתמונה מוכנה, ואז מגדיר אותה כ-background.    │
     * │                                                             │
     * │  חלק 2: מריץ לולאה על 10 ImageViews בדיוק.                 │
     * │         לכל אחד:                                            │
     * │         א. טוען תמונה מ-Supabase Storage (Glide)           │
     * │         ב. בודק אם התמונה כבר "נפתרה" (solvedImageIds)     │
     * │            - כן → מעביר לפס התחתון (moveViewToTray)        │
     * │            - לא → מגדיר גרירה + לחיצה                      │
     * │                                                             │
     * │  חלק 3: ה-lambda של setOnClickListener לוכד משתנים         │
     * │         חיצוניים — זה הקושי הגדול.                          │
     * │         Java דורשת שמשתנים שנלכדים ב-lambda יהיו           │
     * │         "effectively final" — לא משתנים לאחר הגדרתם.       │
     * │         לכן יש את "finalSolvedImageIds" ולא solvedImageIds  │
     * │         ישירות — כי solvedImageIds עלולה להשתנות בקוד.     │
     * └─────────────────────────────────────────────────────────────┘
     *
     * זרימת טעינת הרקע:
     *
     *   Glide.with(this).load(bgUrl)
     *        ↓ (טוען בThread נפרד)
     *   .into(new CustomTarget<Drawable>() {
     *        ↓ (נקרא כשהתמונה מוכנה)
     *   onResourceReady(resource, ...) {
     *        rootLayout.setBackground(resource) ← כאן הרקע מוגדר
     *   }
     *
     * זרימת לכידת המשתנים ב-lambda:
     *
     *   Set<String> finalSolvedImageIds = solvedImageIds  ← עותק סופי
     *        ↓
     *   imageView.setOnClickListener(v -> {
     *        finalSolvedImageIds.add(...)  ← משתמש בעותק הסופי
     *        solvedPrefs.edit()...         ← שומר לזיכרון המקומי
     *        startActivity(intent)         ← עובר למסך שאלות
     *   })
     *
     * למה צריך "finalSolvedImageIds" ולא solvedImageIds ישירות?
     *   solvedImageIds יכולה להשתנות (ב-if level==1 היא מוחלפת לחלוטין).
     *   Java לא מאפשרת ללכוד משתנה שעלול להשתנות ב-lambda.
     *   הפתרון: ליצור עותק סופי שהוא תמיד קבוע.
     */
    private void populateUI(QuestionRepository.RoomData room, Set<String> solvedImageIds,
                             SharedPreferences solvedPrefs, int level,
                             HashMap<Integer, Long> timings, String creationType, QuizData aiData) {

        // ── טעינת תמונת רקע ──────────────────────────────────────────────
        View rootLayout = findViewById(R.id.main);
        if (rootLayout != null && room.background != null && !room.background.isEmpty()) {
            // בונה URL מלא לתמונת הרקע ב-Supabase Storage
            String bgUrl = STORAGE_BASE_URL + BACKGROUND_BUCKET + "/" + room.background.trim();
            // טוען עם Glide ומגדיר כ-background של ה-Layout
            Glide.with(this).load(bgUrl).into(new CustomTarget<Drawable>() {
                @Override
                public void onResourceReady(Drawable resource, @Nullable Transition<? super Drawable> t) {
                    rootLayout.setBackground(resource); // מגדיר את התמונה כרקע
                }
                @Override public void onLoadCleared(@Nullable Drawable placeholder) {}
            });
        }

        // ── טעינת 10 תמונות ניתנות ללחיצה ──────────────────────────────
        List<String> objects = room.getClickableObjects(); // רשימת שמות קבצים מהDB
        ViewGroup container = findViewById(R.id.image_container); // מיכל התמונות בחדר
        LinearLayout bottomTray = findViewById(R.id.bottom_panel_tray); // פס תחתון לתמונות שנפתרו

        // מערך ID-ים של 10 ה-ImageViews בה-layout
        int[] viewIds = {R.id.image1, R.id.image2, R.id.image3, R.id.image4, R.id.image5,
                         R.id.image6, R.id.image7, R.id.image8, R.id.image9, R.id.image10};

        GameAudioManager audioManager = GameAudioManager.getInstance(this);

        for (int i = 0; i < viewIds.length; i++) {
            ImageView imageView = findViewById(viewIds[i]);
            if (imageView == null) continue; // לא נמצא ב-layout — מדלג

            // טעינת תמונה מ-Supabase Storage דרך Glide
            if (objects != null && i < objects.size()) {
                String imgUrl = STORAGE_BASE_URL + OBJECTS_BUCKET + "/" + objects.get(i).trim();
                Glide.with(this).load(imgUrl).into(imageView);
            }

            String idStr = String.valueOf(viewIds[i]);
            if (solvedImageIds.contains(idStr)) {
                // תמונה שכבר נפתרה — מעבירה לפס התחתון
                moveViewToTray(imageView, container, bottomTray);
            } else {
                // תמונה שעדיין לא נפתרה — מוסיף גרירה ולחיצה
                imageView.setOnTouchListener(new DraggableTouchListener());
                Set<String> finalSolvedImageIds = solvedImageIds;
                imageView.setOnClickListener(v -> {
                    finalSolvedImageIds.add(String.valueOf(v.getId())); // מסמן כנפתרת
                    solvedPrefs.edit().putStringSet(KEY_SOLVED_IMAGES, finalSolvedImageIds).apply(); // שומר

                    // מנגן מחדש מוזיקת רקע לאות מעבר לשאלות
                    audioManager.stopAmbientMusic();
                    audioManager.startAmbientMusic();

                    // עובר ל-MainActivity (שאלות) עם כל הנתונים
                    Intent intent = new Intent(DrawerActivity.this, MainActivity.class);
                    intent.putExtra(MainActivity.EXTRA_CREATION_TYPE, creationType);
                    if (aiData != null) intent.putExtra(MainActivity.EXTRA_AI_GAME_DATA, aiData);
                    intent.putExtra(MainActivity.EXTRA_LEVEL, level);
                    intent.putExtra(MainActivity.EXTRA_TIMINGS, timings);
                    intent.putExtra(MainActivity.EXTRA_ROOM_ID, room.roomId); // מעביר room_id לשאלות
                    startActivity(intent);
                    finish(); // סוגר את החדר
                });
            }
        }
    }

    /**
     * מעביר תמונה שנפתרה מהחדר לפס התחתון.
     * מקטינה, מאפיל אותה ומסיר את האינטראקציה.
     */
    private void moveViewToTray(ImageView view, ViewGroup originalContainer, LinearLayout tray) {
        originalContainer.removeView(view); // מסיר מהחדר
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dpToPx(50), dpToPx(50)); // 50dp × 50dp
        params.setMargins(dpToPx(4), 0, dpToPx(4), 0); // רווח בין תמונות בפס
        view.setLayoutParams(params);
        view.setOnTouchListener(null);  // מסיר גרירה
        view.setOnClickListener(null);  // מסיר לחיצה
        view.setAlpha(0.6f);            // 60% שקיפות — נראית "פתורה"
        tray.addView(view); // מוסיף לפס התחתון
    }

    /**
     * ממיר dp לפיקסלים לפי צפיפות המסך
     */
    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    /**
     * מנהל גרירה ולחיצה על תמונות בחדר.
     * מבחין בין גרירה (ACTION_MOVE) ולחיצה (ACTION_UP ללא תנועה).
     */
    private static class DraggableTouchListener implements View.OnTouchListener {
        private float dX, dY;   // הפרש בין מיקום האצבע למיקום התמונה
        private float startX, startY; // מיקום ההתחלה בלחיצה
        private static final int THRESHOLD = 10; // פיקסלים — מתחת לזה נחשב לחיצה

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent == null) return false;
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    // שומר נקודת התחלה ומחשב הפרש
                    startX = event.getRawX(); startY = event.getRawY();
                    dX = view.getX() - startX; dY = view.getY() - startY;
                    view.bringToFront(); // מביא לחזית — לא מכוסה ע"י אחרות
                    break;
                case MotionEvent.ACTION_MOVE:
                    // מחשב מיקום חדש תוך כדי מגבלות גבולות המכיל
                    view.animate()
                        .x(Math.max(0, Math.min(event.getRawX() + dX, parent.getWidth() - view.getWidth())))
                        .y(Math.max(0, Math.min(event.getRawY() + dY, parent.getHeight() - view.getHeight())))
                        .setDuration(0).start(); // ללא אנימציה — מידי
                    break;
                case MotionEvent.ACTION_UP:
                    // אם התנועה הייתה קטנה מ-THRESHOLD — נחשב לחיצה
                    if (Math.abs(startX - event.getRawX()) < THRESHOLD &&
                        Math.abs(startY - event.getRawY()) < THRESHOLD) view.performClick();
                    break;
                default: return false;
            }
            return true;
        }
    }
}
