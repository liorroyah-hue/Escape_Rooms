package com.example.escape_rooms.viewmodel;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.escape_rooms.repository.RatingRepository;

public class RatingViewModel extends AndroidViewModel {
    private final RatingRepository repository;
    
    private final MutableLiveData<Integer> savedRating = new MutableLiveData<>();
    private final MutableLiveData<Boolean> navigateBack = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public RatingViewModel(@NonNull Application application) {
        super(application);
        // BaseRepository initialization no longer requires a context
        this.repository = new RatingRepository();
    }

    public LiveData<Integer> getSavedRating() {
        return savedRating;
    }

    public LiveData<Boolean> getNavigateBack() {
        return navigateBack;
    }
    
    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void submitRating(float rating) {
        // Fetch the current username from SharedPreferences
        SharedPreferences prefs = getApplication().getSharedPreferences("EscapeRoomPrefs", Context.MODE_PRIVATE);
        String username = prefs.getString("current_username", "Guest_User");

        repository.saveRating(username, rating, new RatingRepository.RatingCallback() {
            @Override
            public void onSuccess() {
                savedRating.postValue(Math.round(rating));
                navigateBack.postValue(true);
            }

            @Override
            public void onError(Exception e) {
                errorMessage.postValue("Failed to save rating to cloud");
            }
        });
    }
}
