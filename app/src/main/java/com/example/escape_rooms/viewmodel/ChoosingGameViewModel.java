package com.example.escape_rooms.viewmodel;

import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.escape_rooms.model.QuizData;
import com.example.escape_rooms.repository.services.GeminiService;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChoosingGameViewModel extends AndroidViewModel {
    private static final String TAG = "ChoosingGameViewModel";
    private final GeminiService geminiService = new GeminiService();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Gson gson = new Gson();

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<QuizData> navigateToGame = new MutableLiveData<>();

    public ChoosingGameViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<QuizData> getNavigateToGame() { return navigateToGame; }

    public void generateAiGame(String subject) {
        isLoading.setValue(true);
        executor.execute(() -> {
            try {
                // Request 10 questions (2 per level for 5 levels)
                String jsonResponse = geminiService.generateQandA(subject, 10);
                QuizData quizData = gson.fromJson(jsonResponse, QuizData.class);

                if (quizData != null && quizData.getQuestions() != null && !quizData.getQuestions().isEmpty()) {
                    isLoading.postValue(false);
                    navigateToGame.postValue(quizData);
                } else {
                    isLoading.postValue(false);
                    errorMessage.postValue("ה-AI החזיר תשובה ריקה. נסה שוב.");
                }
            } catch (IOException e) {
                Log.e(TAG, "AI Generation failed", e);
                isLoading.postValue(false);
                errorMessage.postValue("שגיאה ביצירת המשחק: " + e.getMessage());
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}
