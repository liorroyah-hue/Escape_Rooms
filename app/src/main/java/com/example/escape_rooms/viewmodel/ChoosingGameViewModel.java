package com.example.escape_rooms.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.escape_rooms.repository.GeminiService;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;
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
                // We'll generate 2 questions as per the previous requirement
                String jsonResponse = geminiService.generateQandA(subject, 2);
                
                // The response from Gemini might include markdown characters, so we clean it.
                String cleanedJson = jsonResponse.replace("```json", "").replace("```", "").trim();
                
                Gson gson = new Gson();
                QuizData quizData = gson.fromJson(cleanedJson, QuizData.class);
                navigateToGame.postValue(quizData);

            } catch (Exception e) {
                errorMessage.postValue("שגיאה ביצירת המשחק: " + e.getMessage());
            } finally {
                isLoading.postValue(false);
            }
        });
    }

    // Helper class for parsing the JSON from Gemini
    public static class QuizData implements Serializable {
        @SerializedName("questions")
        private List<String> questions;
        @SerializedName("answers")
        private List<List<String>> answers;
        @SerializedName("correctAnswers")
        private List<String> correctAnswers;

        public List<String> getQuestions() { return questions; }
        public List<List<String>> getAnswers() { return answers; }
        public List<String> getCorrectAnswers() { return correctAnswers; }
    }
}
