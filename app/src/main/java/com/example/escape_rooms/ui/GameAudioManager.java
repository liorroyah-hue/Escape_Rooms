package com.example.escape_rooms.ui;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class GameAudioManager {
    private static GameAudioManager instance;
    private MediaPlayer ambientPlayer;
    private final Context context;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private GameAudioManager(Context context) {
        this.context = context.getApplicationContext();
    }

    public static synchronized GameAudioManager getInstance(Context context) {
        if (instance == null) {
            instance = new GameAudioManager(context);
        }
        return instance;
    }

    public void startAmbientMusic() {
        if (ambientPlayer == null) {
            int resId = context.getResources().getIdentifier("ambient", "raw", context.getPackageName());
            if (resId != 0) {
                try {
                    ambientPlayer = MediaPlayer.create(context, resId);
                    if (ambientPlayer != null) {
                        ambientPlayer.setLooping(true);
                        ambientPlayer.setVolume(0.4f, 0.4f);
                        ambientPlayer.start();
                    }
                } catch (Exception e) {
                    Log.e("AudioManager", "Error playing ambient music", e);
                }
            }
        } else if (!ambientPlayer.isPlaying()) {
            ambientPlayer.start();
        }
    }

    public void pauseAmbientMusic() {
        if (ambientPlayer != null && ambientPlayer.isPlaying()) {
            ambientPlayer.pause();
        }
    }

    public void stopAmbientMusic() {
        handler.removeCallbacksAndMessages(null);
        if (ambientPlayer != null) {
            try {
                ambientPlayer.stop();
                ambientPlayer.release();
            } catch (Exception ignored) {}
            ambientPlayer = null;
        }
    }

    public void playSuccessSound() {
        // ביטול פעולות ממתינות קודמות למניעת התנגשויות
        handler.removeCallbacksAndMessages(null);
        
        // 1. השהיית מוזיקת הרקע
        pauseAmbientMusic();

        // 2. המתנת שנייה של שקט, ואז השמעת צליל הצלחה
        handler.postDelayed(() -> {
            playDynamicSound("door_open", mediaPlayer -> {
                // 3. המתנת שנייה של שקט לאחר סיום הצליל, ואז חזרה למוזיקה
                handler.postDelayed(this::startAmbientMusic, 1000);
            });
        }, 1000);
    }

    public void playErrorSound() {
        // ביטול פעולות ממתינות קודמות
        handler.removeCallbacksAndMessages(null);

        // 1. השהיית מוזיקת הרקע
        pauseAmbientMusic();

        // 2. המתנת שנייה של שקט, ואז השמעת צליל טעות
        handler.postDelayed(() -> {
            playDynamicSound("error_buzz", mediaPlayer -> {
                // 3. המתנת שנייה של שקט לאחר סיום הצליל, ואז חזרה למוזיקה
                handler.postDelayed(this::startAmbientMusic, 1000);
            });
        }, 1000);
    }

    private void playDynamicSound(String fileName, MediaPlayer.OnCompletionListener customListener) {
        int resId = context.getResources().getIdentifier(fileName, "raw", context.getPackageName());
        if (resId != 0) {
            try {
                MediaPlayer mp = MediaPlayer.create(context, resId);
                if (mp != null) {
                    mp.setOnCompletionListener(mediaPlayer -> {
                        mediaPlayer.release();
                        if (customListener != null) {
                            customListener.onCompletion(mediaPlayer);
                        }
                    });
                    mp.start();
                } else if (customListener != null) {
                    customListener.onCompletion(null);
                }
            } catch (Exception e) {
                Log.e("AudioManager", "Error playing sound: " + fileName, e);
                if (customListener != null) customListener.onCompletion(null);
            }
        } else if (customListener != null) {
            customListener.onCompletion(null);
        }
    }
}
