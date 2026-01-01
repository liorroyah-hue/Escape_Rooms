package com.example.escape_rooms.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.escape_rooms.repository.RatingRepository;

public class RatingViewModel extends AndroidViewModel {
    private final RatingRepository repository;
    
    private final MutableLiveData<Integer> savedRating = new MutableLiveData<>();
    private final MutableLiveData<Boolean> navigateBack = new MutableLiveData<>();

    public RatingViewModel(@NonNull Application application) {
        super(application);
        // Using Application context to initialize the repository safely
        this.repository = new RatingRepository(application.getApplicationContext());
    }

    public LiveData<Integer> getSavedRating() {
        return savedRating;
    }

    public LiveData<Boolean> getNavigateBack() {
        return navigateBack;
    }

    public void submitRating(float rating) {
        repository.saveRating(rating);
        int currentRating = repository.getRating();
        savedRating.setValue(currentRating);
        navigateBack.setValue(true);
    }
}
