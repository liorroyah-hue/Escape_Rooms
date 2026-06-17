package com.example.escape_rooms.viewmodel;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
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
import com.example.escape_rooms.repository.GameRepository;
import com.example.escape_rooms.repository.QuestionRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GameViewModel extends AndroidViewModel {
    private final QuestionRepository repository;
    private final GameRepository gameRepository = new GameRepository();

    private final MutableLiveData<Questions> currentQuestions = new MutableLiveData<>();
    private final MutableLiveData<String> toastMessage = new MutableLiveData<>();
    private final MutableLiveData<NavigationEvent> navigationEvent = new MutableLiveData<>();

    private int currentLevel = 1;
    private int currentRoomId = 0;
    private long startTime;
    private HashMap<Integer, Long> levelTimings = new HashMap<>();
    private QuizData fullAiQuizData;
    private int lastQuestionId = 0;
    private int lastPictureId = 0;

    // Set של IDs של שאלות שכבר הוצגו בכל 5 הרמות — נשמר ב-SharedPreferences
    private final Set<Integer> usedQuestionIds = new HashSet<>();

    /** טוען את רשימת השאלות המשומשות מה-SharedPreferences */
    private void loadUsedQuestions() {
        SharedPreferences prefs = getApplication()
                .getSharedPreferences("EscapeRoomPrefs", Context.MODE_PRIVATE);
        Set<String> stored = prefs.getStringSet("used_question_ids", new HashSet<>());
        usedQuestionIds.clear();
        for (String id : stored) {
            try { usedQuestionIds.add(Integer.parseInt(id)); } catch (Exception ignored) {}
        }
    }

    /** שומר את רשימת השאלות המשומשות ל-SharedPreferences */
    private void saveUsedQuestions() {
        Set<String> toStore = new HashSet<>();
        for (int id : usedQuestionIds) toStore.add(String.valueOf(id));
        getApplication().getSharedPreferences("EscapeRoomPrefs", Context.MODE_PRIVATE)
                .edit().putStringSet("used_question_ids", toStore).apply();
    }

    /** מוחק את רשימת השאלות המשומשות — נקרא בתחילת משחק חדש */
    private void clearUsedQuestions() {
        usedQuestionIds.clear();
        getApplication().getSharedPreferences("EscapeRoomPrefs", Context.MODE_PRIVATE)
                .edit().remove("used_question_ids").apply();
    }

    public static final int MAX_LEVELS = 5;

    public GameViewModel(@NonNull Application application, @NonNull QuestionRepository questionRepository) {
        super(application);
        this.repository = questionRepository;
    }

    public LiveData<Questions> getCurrentQuestions() { return currentQuestions; }
    public LiveData<String> getToastMessage() { return toastMessage; }
    public LiveData<NavigationEvent> getNavigationEvent() { return navigationEvent; }

    public void initLevel(int level, HashMap<Integer, Long> timings, int roomId) {
        this.currentLevel = level;
        this.currentRoomId = roomId;
        if (timings != null) this.levelTimings = timings;
        if (level == 1) {
            clearUsedQuestions(); // משחק חדש — מאפס שאלות שהוצגו
        } else {
            loadUsedQuestions(); // טוען מ-SharedPreferences את מה שהוצג ברמות הקודמות
        }
        loadLevel();
    }

    public void initAiGame(QuizData quizData, int level, HashMap<Integer, Long> timings) {
        this.fullAiQuizData = quizData;
        this.currentLevel = level;
        if (timings != null) this.levelTimings = timings;
        loadAiLevel();
    }

    /**
     * נקרא מ-FindTheItemActivity כשהשחקן מצא את הפריט.
     * שומר את picture_id ומפעיל שמירת תוצאת הרמה ל-Supabase.
     */
    /**
     * נקרא מ-FindTheItemActivity כדי לעדכן את ה-ViewModel
     * עם הקשר הרמה שהסתיימה — roomId ו-questionId —
     * לפני ש-onPictureFound נקרא ושומר ל-Supabase.
     */
    public void setLevelContext(int level, int roomId, int questionId) {
        this.currentLevel = level;
        this.currentRoomId = roomId;
        this.lastQuestionId = questionId;
        loadUsedQuestions(); // טוען שאלות משומשות כדי שה-Set יהיה עדכני
    }

    public void onPictureFound(int pictureId) {
        this.lastPictureId = pictureId;
        saveLevelResult();
    }

    /**
     * שומר תוצאת הרמה הנוכחית ל-Supabase.
     * נקרא אחרי שהשחקן מצא את הפריט — כלומר סיים את הרמה במלואה.
     */
    private void saveLevelResult() {
        SharedPreferences prefs = getApplication()
                .getSharedPreferences("EscapeRoomPrefs", Context.MODE_PRIVATE);
        long userId = prefs.getLong("current_user_id", -1);

        if (userId == -1) return;

        Long levelTime = levelTimings.get(currentLevel);
        if (levelTime == null) return;

        gameRepository.saveLevelResult(
                userId,
                currentLevel,   // מספר הרמה (1-5)
                levelTime,      // הזמן של הרמה הספציפית
                currentRoomId,  // החדר של הרמה
                lastQuestionId, // השאלה של הרמה
                lastPictureId,  // הפריט שנמצא בסוף הרמה
                new GameRepository.GameResultCallback() {
                    @Override public void onSuccess() {
                        Log.d("GameViewModel", "Level " + currentLevel + " saved to Supabase");
                    }
                    @Override public void onError(Exception e) {
                        Log.e("GameViewModel", "Failed to save level " + currentLevel, e);
                    }
                }
        );
    }

    private void loadLevel() {
        startTime = System.currentTimeMillis();
        repository.get2RandomQuestions(currentRoomId, usedQuestionIds, new QuestionRepository.QuestionsCallback() {
            @Override
            public void onSuccess(List<Question> questions) {
                if (questions == null || questions.isEmpty()) {
                    toastMessage.postValue("No questions found for level " + currentLevel);
                } else {
                    lastQuestionId = questions.get(0).getId();
                    // מוסיף את ה-IDs של השאלות שהוצגו ושומר ל-SharedPreferences
                    for (Question q : questions) usedQuestionIds.add(q.getId());
                    saveUsedQuestions();
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

    private void loadAiLevel() {
        if (fullAiQuizData == null || fullAiQuizData.getQuestions() == null) return;
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
            int nextLevel = currentLevel + 1;
            navigationEvent.setValue(new NavigationEvent(
                    NavigationTarget.FIND_ITEM, nextLevel, levelTimings, fullAiQuizData, lastQuestionId));
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
        public final int questionId;

        public NavigationEvent(NavigationTarget target, int nextLevel,
                               HashMap<Integer, Long> timings, QuizData aiData, int questionId) {
            this.target = target;
            this.nextLevel = nextLevel;
            this.timings = timings;
            this.aiData = aiData;
            this.questionId = questionId;
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
