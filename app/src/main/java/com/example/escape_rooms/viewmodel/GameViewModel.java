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
import com.example.escape_rooms.model.QuizData;
import com.example.escape_rooms.repository.QuestionRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameViewModel extends AndroidViewModel {
    private final QuestionRepository repository;
    private final MutableLiveData<Questions> currentQuestions = new MutableLiveData<>();
    private final MutableLiveData<String> toastMessage = new MutableLiveData<>();
    private final MutableLiveData<NavigationEvent> navigationEvent = new MutableLiveData<>();

    private int currentLevel = 1;
    private long startTime;
    private HashMap<Integer, Long> levelTimings = new HashMap<>();
    private QuizData fullAiQuizData;
    private static final int MAX_LEVELS = 5;

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

    public void initAiGame(QuizData quizData, int level, HashMap<Integer, Long> timings) {
        this.fullAiQuizData = quizData;
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
                currentQuestions.postValue(new Questions(questions));
            }

            @Override
            public void onError(Exception e) {
                Log.e("GameViewModel", "Failed to load questions", e);
                toastMessage.postValue("Failed to load questions");
            }
        });
    }

    private void loadAiLevel() {
        if (fullAiQuizData == null) return;
        startTime = System.currentTimeMillis();

        int questionsPerLevel = 2;
        int startIndex = (currentLevel - 1) * questionsPerLevel;
        int endIndex = Math.min(startIndex + questionsPerLevel, fullAiQuizData.getQuestions().size());

        if (startIndex < endIndex) {
            QuizData levelSubset = new QuizData();
            levelSubset.setQuestions(new ArrayList<>(fullAiQuizData.getQuestions().subList(startIndex, endIndex)));
            levelSubset.setAnswers(new ArrayList<>(fullAiQuizData.getAnswers().subList(startIndex, endIndex)));
            levelSubset.setCorrectAnswers(new ArrayList<>(fullAiQuizData.getCorrectAnswers().subList(startIndex, endIndex)));
            currentQuestions.postValue(new Questions(levelSubset));
        } else {
            toastMessage.postValue("No more AI questions for this level.");
        }
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
                int nextLevel = currentLevel + 1;
                // Move to FindTheItem instead of going directly to Drawer
                navigationEvent.setValue(new NavigationEvent(NavigationTarget.FIND_ITEM, nextLevel, levelTimings, fullAiQuizData));
            } else {
                navigationEvent.setValue(new NavigationEvent(NavigationTarget.RESULTS, 0, levelTimings, null));
            }
        } else {
            toastMessage.setValue("msg_incorrect");
        }
    }

    public enum NavigationTarget { FIND_ITEM, RESULTS }

    public static class NavigationEvent {
        public final NavigationTarget target;
        public final int nextLevel;
        public final HashMap<Integer, Long> timings;
        public final QuizData aiData;

        public NavigationEvent(NavigationTarget target, int nextLevel, HashMap<Integer, Long> timings, QuizData aiData) {
            this.target = target;
            this.nextLevel = nextLevel;
            this.timings = timings;
            this.aiData = aiData;
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
            return (T) new GameViewModel(application, questionRepository);
        }
    }
}
