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
import com.example.escape_rooms.repository.QuestionRepository;

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
    private static final int MAX_LEVELS = 10;

    // Standard constructor using the Singleton Repository
    public GameViewModel(@NonNull Application application) {
        this(application, QuestionRepository.getInstance());
    }

    // Secondary constructor for testing / injection
    public GameViewModel(@NonNull Application application, QuestionRepository repository) {
        super(application);
        this.repository = repository;
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

    private void loadLevel() {
        startTime = System.currentTimeMillis();
        repository.getQuestionsForLevel(currentLevel, new QuestionRepository.QuestionsCallback() {
            @Override
            public void onSuccess(List<Question> questions) {
                if (questions == null || questions.isEmpty()) {
                    toastMessage.postValue("No questions found for level " + currentLevel);
                } else {
                    currentQuestions.postValue(new Questions(questions));
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e("GameViewModel", "Failed to load questions", e);
                toastMessage.postValue("Failed to load questions");
            }
        });
    }

    public void verifyAndSubmit(Map<String, String> selectedAnswers) {
        Questions data = currentQuestions.getValue();
        if (data == null) return;

        if (selectedAnswers.size() != data.getQuestionsList().size()) {
            toastMessage.setValue("msg_answer_all");
            return;
        }

        boolean allCorrect = true;
        Map<String, String> correctAnswers = data.getCorrectAnswers();
        for (Map.Entry<String, String> entry : correctAnswers.entrySet()) {
            if (!entry.getValue().equals(selectedAnswers.get(entry.getKey()))) {
                allCorrect = false;
                break;
            }
        }

        if (allCorrect) {
            long duration = System.currentTimeMillis() - startTime;
            levelTimings.put(currentLevel, duration);

            if (currentLevel < MAX_LEVELS) {
                navigationEvent.setValue(new NavigationEvent(NavigationTarget.NEXT_LEVEL, currentLevel + 1, levelTimings));
            } else {
                navigationEvent.setValue(new NavigationEvent(NavigationTarget.RESULTS, 0, levelTimings));
            }
        } else {
            toastMessage.setValue("msg_incorrect");
        }
    }

    public enum NavigationTarget { NEXT_LEVEL, RESULTS }

    public static class NavigationEvent {
        public final NavigationTarget target;
        public final int nextLevel;
        public final HashMap<Integer, Long> timings;

        public NavigationEvent(NavigationTarget target, int nextLevel, HashMap<Integer, Long> timings) {
            this.target = target;
            this.nextLevel = nextLevel;
            this.timings = timings;
        }
    }

    public static class Factory implements ViewModelProvider.Factory {
        private final Application application;
        private final QuestionRepository repository;

        public Factory(Application application, QuestionRepository repository) {
            this.application = application;
            this.repository = repository;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new GameViewModel(application, repository);
        }
    }
}
