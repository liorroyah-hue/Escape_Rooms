package com.example.escape_rooms.repository.services;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import java.util.Random;

public class GameAudioManager {
    private static GameAudioManager instance;
    private MediaPlayer ambientPlayer;
    private MediaPlayer effectPlayer;
    private final Context context;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Vibrator vibrator;
    private boolean isAmbientEnabled = false;
    private final Random random = new Random();

    private final String[] ambientTracks = {"ambient", "ambient_background"};

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
        isAmbientEnabled = true;
        playAmbient();
    }

    private void playAmbient() {
        // וודא שלא מתנגן אפקט כרגע כדי למנוע כפל
        if (!isAmbientEnabled || (effectPlayer != null && effectPlayer.isPlaying())) return;

        if (ambientPlayer == null) {
            String randomTrack = ambientTracks[random.nextInt(ambientTracks.length)];
            int resId = context.getResources().getIdentifier(randomTrack, "raw", context.getPackageName());
            
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

    public void stopAllSounds() {
        isAmbientEnabled = false;
        handler.removeCallbacksAndMessages(null);
        
        if (ambientPlayer != null) {
            try {
                if (ambientPlayer.isPlaying()) ambientPlayer.stop();
                ambientPlayer.release();
            } catch (Exception ignored) {}
            ambientPlayer = null;
        }

        if (effectPlayer != null) {
            try {
                if (effectPlayer.isPlaying()) effectPlayer.stop();
                effectPlayer.release();
            } catch (Exception ignored) {}
            effectPlayer = null;
        }
    }

    public void stopAmbientMusic() {
        stopAllSounds();
    }

    public void playSuccessSound() {
        // עצירה מוחלטת של כל טיימר או סאונד קודם
        handler.removeCallbacksAndMessages(null);
        pauseAmbientMusic();
        
        vibrate(new long[]{0, 50, 50, 50}, new int[]{0, 100, 0, 100});

        // מרווח שקט של שנייה
        handler.postDelayed(() -> {
            playDynamicSound("door_open", mediaPlayer -> {
                // רק אם מוזיקת הרקע עדיין אמורה לפעול, נחזיר אותה אחרי עוד שנייה של שקט
                if (isAmbientEnabled) {
                    handler.postDelayed(this::playAmbient, 1000);
                }
            });
        }, 1000);
    }

    public void playErrorSound() {
        handler.removeCallbacksAndMessages(null);
        pauseAmbientMusic();
        
        vibrate(new long[]{0, 400}, new int[]{0, 255});

        handler.postDelayed(() -> {
            playDynamicSound("error_buzz", mediaPlayer -> {
                if (isAmbientEnabled) {
                    handler.postDelayed(this::playAmbient, 1000);
                }
            });
        }, 1000);
    }

    private void vibrate(long[] timings, int[] amplitudes) {
        if (vibrator == null || !vibrator.hasVibrator()) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1));
        } else {
            long totalDuration = 0;
            for (long t : timings) totalDuration += t;
            vibrator.vibrate(totalDuration);
        }
    }

    private void playDynamicSound(String fileName, MediaPlayer.OnCompletionListener customListener) {
        int resId = context.getResources().getIdentifier(fileName, "raw", context.getPackageName());
        if (resId != 0) {
            try {
                // שחרור האפקט הקודם אם קיים
                if (effectPlayer != null) {
                    try {
                        if (effectPlayer.isPlaying()) effectPlayer.stop();
                        effectPlayer.release();
                    } catch (Exception ignored) {}
                }

                effectPlayer = MediaPlayer.create(context, resId);
                if (effectPlayer != null) {
                    effectPlayer.setOnCompletionListener(mediaPlayer -> {
                        mediaPlayer.release();
                        if (effectPlayer == mediaPlayer) effectPlayer = null;
                        if (customListener != null) {
                            customListener.onCompletion(mediaPlayer);
                        }
                    });
                    effectPlayer.start();
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
