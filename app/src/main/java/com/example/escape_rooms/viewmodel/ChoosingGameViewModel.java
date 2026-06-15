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

/**
 * מנהל את לוגיקת בחירת סוג המשחק.
 * אחראי על יצירת שאלות AI דרך Gemini.
 */
public class ChoosingGameViewModel extends AndroidViewModel {
    private static final String TAG = "ChoosingGameViewModel";

    private final GeminiService geminiService = new GeminiService(); // שירות Gemini AI
    // Thread נפרד לפעולות רשת — לא חוסם את ה-UI Thread
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Gson gson = new Gson(); // לפרסור JSON

    // LiveData — ה-View מאזין לשינויים
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false); // מצב טעינה
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();    // הודעת שגיאה
    private final MutableLiveData<QuizData> navigateToGame = new MutableLiveData<>(); // אות ניווט עם נתוני AI

    public ChoosingGameViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<QuizData> getNavigateToGame() { return navigateToGame; }

    /**
     * מפעיל את Gemini AI ליצירת שאלות לפי נושא.
     * @param subject הנושא שנבחר (גיאוגרפיה, היסטוריה, וכו')
     */
    public void generateAiGame(String subject) {
        isLoading.setValue(true); // מציג אנימציית טעינה
        executor.execute(() -> { // מריץ על Thread נפרד
            try {
                // מבקש 10 שאלות — 2 לכל אחת מ-5 הרמות
                String jsonResponse = geminiService.generateQandA(subject, 10);
                QuizData quizData = gson.fromJson(jsonResponse, QuizData.class); // ממיר JSON ל-QuizData

                if (quizData != null && quizData.getQuestions() != null && !quizData.getQuestions().isEmpty()) {
                    isLoading.postValue(false);       // מסתיר טעינה
                    navigateToGame.postValue(quizData); // מאותת ל-View לנווט עם נתוני AI
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
        executor.shutdown(); // כשה-ViewModel נהרס — מכבה את ה-Thread למניעת דליפות
    }
}
