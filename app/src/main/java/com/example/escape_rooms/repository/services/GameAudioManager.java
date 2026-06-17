package com.example.escape_rooms.repository.services;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.util.Log;

import com.example.escape_rooms.R;

import java.util.Random;

/**
 * מנהל את כל האודיו במשחק — מוזיקת רקע, צלילי אפקטים, ורטט.
 * Singleton — קיים רק עותק אחד לאורך כל האפליקציה.
 */
public class GameAudioManager {
    private static GameAudioManager instance; // עותק יחיד
    private MediaPlayer ambientPlayer;  // נגן מוזיקת הרקע
    private MediaPlayer actionSoundPlayer;   //  נגן צלילי אפקטים כאשר השחקן צודק או טועה
    private final Context context;      // קונטקסט האפליקציה לטעינת קבצי שמע
    private final Handler handler = new Handler(Looper.getMainLooper()); // מאפשר פעולות מתוזמנות על Thread הראשי
    private final Vibrator vibrator;    // מנהל הרטט
    private boolean isAmbientEnabled = false; // האם מוזיקת הרקע מופעלת
    private final Random random = new Random(); // לבחירת קטע אקראי

    private final int LEVEL_DELAY_TIME = 2000; // עיכוב 2 שניות לפני צלילי אפקט

    private int lastTrackIndex = -1; // אינדקס הקטע האחרון שנוגן — למניעת חזרה

    // מערך של 3 קטעי מוזיקת רקע שונים
    private final int[] ambientTracks = {
            R.raw.ambient,
            R.raw.ambient_background,
            R.raw.dark_ambient_soundscape_dreamscape
    };

    private GameAudioManager(Context context) {
        this.context = context.getApplicationContext(); // שומר קונטקסט האפליקציה (לא Activity)
        // מקבל את מנהל הרטט מהמערכת
        VibratorManager vibratorManager = (VibratorManager) this.context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
        this.vibrator = vibratorManager.getDefaultVibrator();
    }

    /**
     * מחזיר את העותק היחיד. יוצר אותו אם לא קיים עדיין.
     */
    public static synchronized GameAudioManager getInstance(Context context) {
        if (instance == null) {
            instance = new GameAudioManager(context);
        }
        return instance;
    }

    /**
     * מפעיל מוזיקת רקע. אם כבר מתנגנת — לא עושה כלום.
     */
    public void startAmbientMusic() {
        isAmbientEnabled = true; // מסמן שמוזיקה אמורה לנגן
        if (ambientPlayer != null && ambientPlayer.isPlaying()) {
            return; // כבר מתנגן — אין צורך להפעיל שוב
        }
        playAmbient();
    }

    /**
     * בוחר קטע אקראי (שונה מהאחרון) ומנגן אותו בלופ.
     */
    private void playAmbient() {
        // לא מנגן אם מוזיקה כבויה או אם אפקט מתנגן עכשיו
        if (!isAmbientEnabled || (actionSoundPlayer != null && actionSoundPlayer.isPlaying())) return;

        // משחרר נגן קודם מהזיכרון
        if (ambientPlayer != null) {
            try { ambientPlayer.release(); } catch (Exception ignored) {}
            ambientPlayer = null;
        }

        // בוחר קטע אקראי שונה מהאחרון
        int nextTrackIndex;
        if (ambientTracks.length > 1) {
            do {
                nextTrackIndex = random.nextInt(ambientTracks.length);
            } while (nextTrackIndex == lastTrackIndex); // חוזר אם קיבל אותו קטע
        } else {
            nextTrackIndex = 0; // אם יש רק קטע אחד
        }

        lastTrackIndex = nextTrackIndex; // שומר לשימוש הפעם הבאה
        int resId = ambientTracks[nextTrackIndex];

        try {
            ambientPlayer = MediaPlayer.create(context, resId); // יוצר נגן חדש
            if (ambientPlayer != null) {
                ambientPlayer.setLooping(true);       // נגינה בלופ
                ambientPlayer.setVolume(0.4f, 0.4f); // 40% עוצמה בשני הערוצים
                ambientPlayer.start();
                Log.d("AudioManager", "Playing random track ID: " + resId);
            }
        } catch (Exception e) {
            Log.e("AudioManager", "Error playing ambient music", e);
        }
    }

    /**
     * משהה את מוזיקת הרקע מבלי לשחרר את הנגן
     */
    public void pauseAmbientMusic() {
        if (ambientPlayer != null && ambientPlayer.isPlaying()) {
            ambientPlayer.pause();
        }
    }

    /**
     * עוצר הכל — מוזיקה ואפקטים — ומשחרר זיכרון.
     * משמש בכניסה למסך הדירוג.
     */
    public void stopAllSounds() {
        isAmbientEnabled = false; // מסמן שמוזיקה כבויה
        handler.removeCallbacksAndMessages(null); // מבטל כל פעולה מתוזמנת

        // עצירה ושחרור נגן הרקע
        if (ambientPlayer != null) {
            try {
                if (ambientPlayer.isPlaying()) ambientPlayer.stop();
                ambientPlayer.release();
            } catch (Exception ignored) {}
            ambientPlayer = null;
        }

        // עצירה ושחרור נגן האפקטים
        if (actionSoundPlayer != null) {
            try {
                if (actionSoundPlayer.isPlaying()) actionSoundPlayer.stop();
                actionSoundPlayer.release();
            } catch (Exception ignored) {}
            actionSoundPlayer = null;
        }
    }

    /**
     * עוצר מוזיקת רקע — קורא ל-stopAllSounds
     */
    public void stopAmbientMusic() {
        stopAllSounds();
    }

    /**
     * מנגן צליל הצלחה ורטט קצר — כשהתשובה נכונה.
     * מחכה 2 שניות לפני הנגינה כדי לתת זמן לאנימציה.
     */
    public void playSuccessSound() {
        handler.removeCallbacksAndMessages(null); // מבטל תזמונים קודמים
        pauseAmbientMusic(); // משהה מוזיקה זמנית

        // רטט קצר כפול — תחושת הצלחה
        vibrate(new long[]{0, 50, 50, 50}, new int[]{0, 100, 0, 100});

        // אחרי 2 שניות — מנגן צליל פתיחת דלת
        handler.postDelayed(() -> {
            playDynamicSound(R.raw.door_open, mediaPlayer -> {
                if (isAmbientEnabled) {
                    // שנייה אחרי הצליל — מחדש את מוזיקת הרקע
                    handler.postDelayed(this::playAmbient, 1000);
                }
            });
        }, LEVEL_DELAY_TIME);
    }

    /**
     * מנגן צליל שגיאה ורטט ארוך — כשהתשובה שגויה.
     */
    public void playErrorSound() {
        handler.removeCallbacksAndMessages(null);
        pauseAmbientMusic();

        // רטט ארוך — תחושת שגיאה
        vibrate(new long[]{0, 400}, new int[]{0, 255});

        handler.postDelayed(() -> {
            playDynamicSound(R.raw.error_buzz, mediaPlayer -> {
                if (isAmbientEnabled) {
                    handler.postDelayed(this::playAmbient, 1000);
                }
            });
        }, LEVEL_DELAY_TIME);
    }

    /**
     * מפעיל רטט לפי דפוס זמנים ועוצמות.
     * בודק קודם שהמכשיר תומך ברטט.
     */
    private void vibrate(long[] timings, int[] amplitudes) {
        if (vibrator == null || !vibrator.hasVibrator()) return; // אין רטט — מדלג
        vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1)); // -1 = ללא חזרה
    }

    /**
     * מנגן צליל אפקט חד-פעמי ומריץ callback בסיום.
     *
     * ┌─────────────────────────────────────────────────────────────┐
     * │                    מה הפונקציה עושה                         │
     * ├─────────────────────────────────────────────────────────────┤
     * │  1. משחררת נגן קודם אם קיים (מניעת דליפות זיכרון).        │
     * │  2. יוצרת נגן חדש לצליל הנוכחי.                            │
     * │  3. מגדירה callback (OnCompletionListener) שנקרא            │
     * │     כשהצליל מסתיים — ב-callback: משחררת זיכרון              │
     * │     ומריצה את ה-customListener שהועבר מבחוץ.               │
     * └─────────────────────────────────────────────────────────────┘
     *
     * שלוש רמות של אסינכרוניות — זה הקושי:
     *
     *   רמה 1: הקורא (playSuccessSound/playErrorSound)
     *          ↓ קורא ל-playDynamicSound עם lambda
     *
     *   רמה 2: playDynamicSound מגדיר OnCompletionListener
     *          ↓ הצליל מתנגן... (זמן לא ידוע)
     *          ↓ כשמסיים — Android קורא ל-onCompletion
     *
     *   רמה 3: onCompletion מריץ את customListener
     *          ↓ customListener מפעיל handler.postDelayed
     *          ↓ אחרי שנייה — חוזר playAmbient
     *
     * זרימה מלאה לדוגמה של playSuccessSound:
     *
     *   playSuccessSound()
     *        ↓ (handler.postDelayed — אחרי 2 שניות)
     *   playDynamicSound(R.raw.door_open, mediaPlayer -> {
     *        ↓ (OnCompletionListener — כשהצליל נגמר)
     *   handler.postDelayed(this::playAmbient, 1000)
     *        ↓ (אחרי עוד שנייה)
     *   playAmbient() — מוזיקת הרקע חוזרת
     *   })
     *
     * למה בודקים "if (effectPlayer == mediaPlayer)"?
     *   בין הרגע שהצליל הסתיים לרגע שה-callback רץ,
     *   יתכן שנוצר כבר נגן חדש (effectPlayer).
     *   הבדיקה מוודאת שאנחנו מאפסים רק את הנגן
     *   שסיים — לא נגן חדש שכבר נוצר.
     *
     * למה קוראים ל-customListener גם אם effectPlayer == null?
     *   אם יצירת הנגן נכשלה (MediaPlayer.create החזיר null),
     *   הצליל לא ינוגן — אבל עדיין צריך להמשיך את הזרימה
     *   (למשל להחזיר את מוזיקת הרקע). לכן קוראים ל-callback
     *   גם במקרה של כישלון.
     */
    private void playDynamicSound(int resId, MediaPlayer.OnCompletionListener customListener) {
        try {
            // משחרר נגן קודם אם קיים
            if (actionSoundPlayer != null) {
                try {
                    if (actionSoundPlayer.isPlaying()) actionSoundPlayer.stop();
                    actionSoundPlayer.release();
                } catch (Exception ignored) {}
            }

            actionSoundPlayer = MediaPlayer.create(context, resId);
            if (actionSoundPlayer != null) {
                actionSoundPlayer.setOnCompletionListener(mediaPlayer -> {
                    mediaPlayer.release(); // משחרר זיכרון בסיום
                    if (actionSoundPlayer == mediaPlayer) actionSoundPlayer = null;
                    if (customListener != null) customListener.onCompletion(mediaPlayer); // קורא ל-callback
                });
                actionSoundPlayer.start();
            } else if (customListener != null) {
                customListener.onCompletion(null); // נגן לא נוצר — קורא ל-callback בכל זאת
            }
        } catch (Exception e) {
            Log.e("AudioManager", "Error playing sound ID: " + resId, e);
            if (customListener != null) customListener.onCompletion(null);
        }
    }
}
