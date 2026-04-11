package com.example.escape_rooms.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.escape_rooms.model.QuizData;
import com.example.escape_rooms.repository.services.GeminiService;
import com.google.gson.Gson;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChoosingGameViewModel extends ViewModel {

    private final GeminiService geminiService = new GeminiService();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<QuizData> navigateToGame = new MutableLiveData<>();

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<QuizData> getNavigateToGame() {
        return navigateToGame;
    }

    public void generateAiGame(String subject) {
        isLoading.setValue(true);
        executor.execute(() -> {
            try {
                // Request 10 questions (2 per level for 5 levels)
                String jsonResponse = geminiService.generateQandA(subject, 10);
                
                String cleanedJson = jsonResponse.replace("```json", "").replace("```", "").trim();
                
                Gson gson = new Gson();
                QuizData quizData = gson.fromJson(cleanedJson, QuizData.class);
                
                if (quizData != null && quizData.getQuestions() != null && quizData.getQuestions().size() >= 10) {
                    navigateToGame.postValue(quizData);
                } else {
                    errorMessage.postValue("ה-AI לא הצליח לייצר מספיק שאלות. נסו שוב.");
                }

            } catch (Exception e) {
                errorMessage.postValue("שגיאה ביצירת המשחק: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }
}
