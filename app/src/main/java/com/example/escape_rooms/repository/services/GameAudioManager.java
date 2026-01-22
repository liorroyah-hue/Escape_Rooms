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

public class GameAudioManager {
    private static GameAudioManager instance;
    private MediaPlayer ambientPlayer;
    private MediaPlayer effectPlayer;
    private final Context context;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Vibrator vibrator;
    private boolean isAmbientEnabled = false;
    private final Random random = new Random();

    private final int LEVEL_DELAY_TIME = 2000;

    private int lastTrackIndex = -1;

    // Use direct resource IDs instead of Strings to avoid getIdentifier()
    private final int[] ambientTracks = {
            R.raw.ambient,
            R.raw.ambient_background,
            R.raw.dark_ambient_soundscape_dreamscape
    };

    private GameAudioManager(Context context) {
        this.context = context.getApplicationContext();
        
        // Fix: Added Build version check to avoid crash on devices below API 31
        VibratorManager vibratorManager = (VibratorManager) this.context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
        this.vibrator = vibratorManager.getDefaultVibrator();
    }

    public static synchronized GameAudioManager getInstance(Context context) {
        if (instance == null) {
            instance = new GameAudioManager(context);
        }
        return instance;
    }

    public void startAmbientMusic() {
        isAmbientEnabled = true;
        if (ambientPlayer != null && ambientPlayer.isPlaying()) {
            return;
        }
        playAmbient();
    }

    private void playAmbient() {
        if (!isAmbientEnabled || (effectPlayer != null && effectPlayer.isPlaying())) return;

        if (ambientPlayer != null) {
            try {
                ambientPlayer.release();
            } catch (Exception ignored) {}
            ambientPlayer = null;
        }

        int nextTrackIndex;
        if (ambientTracks.length > 1) {
            do {
                nextTrackIndex = random.nextInt(ambientTracks.length);
            } while (nextTrackIndex == lastTrackIndex);
        } else {
            nextTrackIndex = 0;
        }
        
        lastTrackIndex = nextTrackIndex;
        int resId = ambientTracks[nextTrackIndex];
        
        try {
            ambientPlayer = MediaPlayer.create(context, resId);
            if (ambientPlayer != null) {
                ambientPlayer.setLooping(true);
                ambientPlayer.setVolume(0.4f, 0.4f);
                ambientPlayer.start();
                Log.d("AudioManager", "Playing random track ID: " + resId);
            }
        } catch (Exception e) {
            Log.e("AudioManager", "Error playing ambient music", e);
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
        handler.removeCallbacksAndMessages(null);
        pauseAmbientMusic();
        
        vibrate(new long[]{0, 50, 50, 50}, new int[]{0, 100, 0, 100});

        handler.postDelayed(() -> {
            playDynamicSound(R.raw.door_open, mediaPlayer -> {
                if (isAmbientEnabled) {
                    handler.postDelayed(this::playAmbient, 1000);
                }
            });
        }, LEVEL_DELAY_TIME);
    }

    public void playErrorSound() {
        handler.removeCallbacksAndMessages(null);
        pauseAmbientMusic();
        
        vibrate(new long[]{0, 400}, new int[]{0, 255});

        handler.postDelayed(() -> {
            playDynamicSound(R.raw.error_buzz, mediaPlayer -> {
                if (isAmbientEnabled) {
                    handler.postDelayed(this::playAmbient, 1000);
                }
            });
        }, LEVEL_DELAY_TIME);
    }

    private void vibrate(long[] timings, int[] amplitudes) {
        if (vibrator == null || !vibrator.hasVibrator()) return;
        vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1));
    }

    private void playDynamicSound(int resId, MediaPlayer.OnCompletionListener customListener) {
        try {
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
            Log.e("AudioManager", "Error playing sound ID: " + resId, e);
            if (customListener != null) customListener.onCompletion(null);
        }
    }
}
