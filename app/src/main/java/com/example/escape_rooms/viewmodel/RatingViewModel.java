package com.example.escape_rooms.viewmodel;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.escape_rooms.repository.RatingRepository;

/**
 * מנהל שמירת דירוג שחקן ב-Supabase.
 */
public class RatingViewModel extends AndroidViewModel {
    private final RatingRepository repository;

    private final MutableLiveData<Integer> savedRating = new MutableLiveData<>();  // הדירוג שנשמר
    private final MutableLiveData<Boolean> navigateBack = new MutableLiveData<>(); // אות ניווט חזרה
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();  // הודעת שגיאה

    public RatingViewModel(@NonNull Application application) {
        super(application);
        this.repository = new RatingRepository();
    }

    public LiveData<Integer> getSavedRating() { return savedRating; }
    public LiveData<Boolean> getNavigateBack() { return navigateBack; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    /**
     * שולח דירוג ל-Supabase.
     * קורא שם משתמש מ-SharedPreferences ומשתמש בו לשמירה.
     */
    public void submitRating(float rating) {
        SharedPreferences prefs = getApplication().getSharedPreferences("EscapeRoomPrefs", Context.MODE_PRIVATE);
        // קורא שם משתמש — ברירת מחדל "Guest_User" אם אין משתמש מחובר
        String username = prefs.getString("current_username", "Guest_User");

        repository.saveRating(username, rating, new RatingRepository.RatingCallback() {
            @Override
            public void onSuccess() {
                savedRating.postValue(Math.round(rating)); // שומר ציון מעוגל
                navigateBack.postValue(true); // מאותת ל-View לנווט הלאה
            }

            @Override
            public void onError(Exception e) {
                errorMessage.postValue("Failed to save rating to cloud");
            }
        });
    }
}
