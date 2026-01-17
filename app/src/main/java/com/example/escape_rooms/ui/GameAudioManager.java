package com.example.escape_rooms.ui;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

public class GameAudioManager {
    private static GameAudioManager instance;
    private MediaPlayer ambientPlayer;
    private final Context context;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Vibrator vibrator;

    private GameAudioManager(Context context) {
        this.context = context.getApplicationContext();
        this.vibrator = (Vibrator) this.context.getSystemService(Context.VIBRATOR_SERVICE);
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
        handler.removeCallbacksAndMessages(null);
        pauseAmbientMusic();

        // משוב רטט "פעימה" עדין להצלחה
        vibrate(new long[]{0, 50, 50, 50}, new int[]{0, 100, 0, 100});

        handler.postDelayed(() -> {
            playDynamicSound("door_open", mediaPlayer -> {
                handler.postDelayed(this::startAmbientMusic, 1000);
            });
        }, 1000);
    }

    public void playErrorSound() {
        handler.removeCallbacksAndMessages(null);
        pauseAmbientMusic();

        // משוב רטט קצר וחזק לטעות
        vibrate(new long[]{0, 400}, new int[]{0, 255});

        handler.postDelayed(() -> {
            playDynamicSound("error_buzz", mediaPlayer -> {
                handler.postDelayed(this::startAmbientMusic, 1000);
            });
        }, 1000);
    }

    private void vibrate(long[] timings, int[] amplitudes) {
        if (vibrator == null || !vibrator.hasVibrator()) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1));
        } else {
            // Fallback למכשירים ישנים (ללא שליטה בעוצמה)
            long totalDuration = 0;
            for (long t : timings) totalDuration += t;
            vibrator.vibrate(totalDuration);
        }
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
