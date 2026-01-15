package com.example.escape_rooms.viewmodel;

import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.escape_rooms.model.Question;
import com.example.escape_rooms.model.Questions;
import com.example.escape_rooms.repository.GeminiService;
import com.example.escape_rooms.repository.QuestionRepository;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameViewModel extends AndroidViewModel {
    private final QuestionRepository repository;
    private final GeminiService geminiService = new GeminiService();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final MutableLiveData<Questions> currentQuestions = new MutableLiveData<>();
    private final MutableLiveData<String> toastMessage = new MutableLiveData<>();
    private final MutableLiveData<NavigationEvent> navigationEvent = new MutableLiveData<>();

    private int currentLevel = 1;
    private long startTime;
    private HashMap<Integer, Long> levelTimings = new HashMap<>();
    private static final int MAX_LEVELS = 10; // Can be adjusted for AI games
    private String aiGameSubject; // Store the subject for multi-level AI games

    public GameViewModel(@NonNull Application application, @NonNull QuestionRepository questionRepository) {
        super(application);
        this.repository = questionRepository;
    }

    public LiveData<Questions> getCurrentQuestions() { return currentQuestions; }
    public LiveData<String> getToastMessage() { return toastMessage; }
    public LiveData<NavigationEvent> getNavigationEvent() { return navigationEvent; }

    public void initLevel(int level, HashMap<Integer, Long> timings) {
        this.currentLevel = level;
        if (timings != null) {
            this.levelTimings = timings;
        }
        loadLevel();
    }

    public void initAiGame(String subject, int level, HashMap<Integer, Long> timings) {
        this.aiGameSubject = subject;
        this.currentLevel = level;
        if (timings != null) {
            this.levelTimings = timings;
        }
        loadAiLevel();
    }

    private void loadLevel() {
        startTime = System.currentTimeMillis();
        repository.getQuestionsForLevel(currentLevel, new QuestionRepository.QuestionsCallback() {
            @Override
            public void onSuccess(List<Question> questions) {
                if (questions == null || questions.isEmpty()) {
                    toastMessage.postValue("No questions found for level " + currentLevel + ". Check your Supabase table.");
                } else {
                    currentQuestions.postValue(new Questions(questions));
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e("GameViewModel", "Failed to load questions", e);
                toastMessage.postValue("Failed to load questions. Please check your connection.");
            }
        });
    }

    private void loadAiLevel() {
        startTime = System.currentTimeMillis();
        toastMessage.postValue("Generating AI questions for level " + currentLevel + "...");
        executor.execute(() -> {
            try {
                // Generate 2 new questions for the current level
                String jsonResponse = geminiService.generateQandA(aiGameSubject, 2);
                String cleanedJson = jsonResponse.replace("```json", "").replace("```", "").trim();
                Gson gson = new Gson();
                QuizData quizData = gson.fromJson(cleanedJson, QuizData.class);
                currentQuestions.postValue(new Questions(quizData));
            } catch (Exception e) {
                toastMessage.postValue("AI Error: " + e.getMessage());
                Log.e("GameViewModel", "Failed to generate AI questions", e);
            }
        });
    }

    public void verifyAndSubmit(Map<String, String> selectedAnswers) {
        Questions data = currentQuestions.getValue();
        if (data == null || data.getQuestionsList().isEmpty()) return;

        if (selectedAnswers.size() != data.getQuestionsList().size()) {
            toastMessage.setValue("msg_answer_all");
            return;
        }

        boolean allCorrect = true;
        for (Map.Entry<String, String> entry : data.getCorrectAnswers().entrySet()) {
            if (!entry.getValue().equals(selectedAnswers.get(entry.getKey()))) {
                allCorrect = false;
                break;
            }
        }

        if (allCorrect) {
            long duration = System.currentTimeMillis() - startTime;
            levelTimings.put(currentLevel, duration);

            if (currentLevel < MAX_LEVELS) {
                navigationEvent.setValue(new NavigationEvent(NavigationTarget.NEXT_LEVEL, currentLevel + 1, levelTimings, aiGameSubject));
            } else {
                navigationEvent.setValue(new NavigationEvent(NavigationTarget.RESULTS, 0, levelTimings, aiGameSubject));
            }
        } else {
            toastMessage.setValue("msg_incorrect");
        }
    }

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

    public enum NavigationTarget { NEXT_LEVEL, RESULTS }

    public static class NavigationEvent {
        public final NavigationTarget target;
        public final int nextLevel;
        public final HashMap<Integer, Long> timings;
        public final String aiGameSubject; // Pass subject to next level

        public NavigationEvent(NavigationTarget target, int nextLevel, HashMap<Integer, Long> timings, String aiGameSubject) {
            this.target = target;
            this.nextLevel = nextLevel;
            this.timings = timings;
            this.aiGameSubject = aiGameSubject;
        }
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private final Application application;
        private final QuestionRepository questionRepository;

        public Factory(Application application, QuestionRepository questionRepository) {
            this.application = application;
            this.questionRepository = questionRepository;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(GameViewModel.class)) {
                return (T) new GameViewModel(application, questionRepository);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
