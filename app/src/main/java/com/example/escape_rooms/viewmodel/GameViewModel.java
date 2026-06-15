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

/**
 * מנהל את לוגיקת המשחק — טעינת שאלות, בדיקת תשובות,
 * מדידת זמן, וניווט בין מסכים.
 * AndroidViewModel — שורד סיבובי מסך.
 */
public class GameViewModel extends AndroidViewModel {

    private final QuestionRepository repository; // מקור הנתונים — שאלות וחדרים

    // LiveData — ה-View מאזין לשינויים ומתעדכן אוטומטית
    private final MutableLiveData<Questions> currentQuestions = new MutableLiveData<>(); // שאלות נוכחיות
    private final MutableLiveData<String> toastMessage = new MutableLiveData<>();        // הודעות למשתמש
    private final MutableLiveData<NavigationEvent> navigationEvent = new MutableLiveData<>(); // אירועי ניווט

    private int currentLevel = 1;      // הרמה הנוכחית (1-5)
    private int currentRoomId = 0;     // ID החדר הנוכחי — לסינון שאלות לפי חדר
    private long startTime;            // זמן תחילת הרמה — למדידת משך הזמן
    private HashMap<Integer, Long> levelTimings = new HashMap<>(); // זמן לכל רמה שהושלמה
    private QuizData fullAiQuizData;   // שאלות AI לכל הרמות
    private int lastQuestionId = 0;    // ID השאלה הראשונה שנענתה — נשמר ב-game_results

    public static final int MAX_LEVELS = 5; // מספר הרמות הכולל במשחק

    public GameViewModel(@NonNull Application application, @NonNull QuestionRepository questionRepository) {
        super(application);
        this.repository = questionRepository;
    }

    // getters ל-LiveData — ה-View מאזין דרך אלה
    public LiveData<Questions> getCurrentQuestions() { return currentQuestions; }
    public LiveData<String> getToastMessage() { return toastMessage; }
    public LiveData<NavigationEvent> getNavigationEvent() { return navigationEvent; }

    /**
     * מאתחל רמה רגילה מהמסד נתונים.
     * @param roomId — ID החדר הנוכחי לסינון שאלות מתאימות
     */
    public void initLevel(int level, HashMap<Integer, Long> timings, int roomId) {
        this.currentLevel = level;
        this.currentRoomId = roomId; // שומר את החדר לשימוש בטעינת שאלות
        if (timings != null) this.levelTimings = timings; // שומר תזמונים קודמים
        loadLevel(); // טוען שאלות מהDB
    }

    /**
     * מאתחל רמה עם שאלות שנוצרו ע"י AI.
     */
    public void initAiGame(QuizData quizData, int level, HashMap<Integer, Long> timings) {
        this.fullAiQuizData = quizData; // שאלות לכל הרמות
        this.currentLevel = level;
        if (timings != null) this.levelTimings = timings;
        loadAiLevel(); // טוען שאלות מהAI data
    }

    /**
     * שולף 2 שאלות אקראיות מה-DB לפי roomId ומתחיל מדידת זמן.
     */
    private void loadLevel() {
        startTime = System.currentTimeMillis(); // מתחיל מדידת זמן
        repository.get2RandomQuestions(currentRoomId, new QuestionRepository.QuestionsCallback() {
            @Override
            public void onSuccess(List<Question> questions) {
                if (questions == null || questions.isEmpty()) {
                    toastMessage.postValue("No questions found for level " + currentLevel);
                } else {
                    lastQuestionId = questions.get(0).getId(); // שומר ID של השאלה הראשונה
                    currentQuestions.postValue(new Questions(questions)); // מעדכן את ה-UI
                }
            }
            @Override
            public void onError(Exception e) {
                Log.e("GameViewModel", "Failed to load questions", e);
                toastMessage.postValue("Failed to load questions");
            }
        });
    }

    /**
     * מחלץ 2 שאלות AI לפי מספר הרמה הנוכחית.
     * 2 שאלות לרמה × 5 רמות = 10 שאלות סה"כ.
     *
     * ┌─────────────────────────────────────────────────────────────┐
     * │                    מה הפונקציה עושה                         │
     * ├─────────────────────────────────────────────────────────────┤
     * │  Gemini יצר 10 שאלות מראש לכל המשחק.                       │
     * │  הפונקציה חותכת "פרוסה" מהרשימה לפי הרמה הנוכחית:         │
     * │  רמה 1 → שאלות 0-1, רמה 2 → שאלות 2-3, וכו'.              │
     * │                                                             │
     * │  הקושי: חייבים לחתוך 3 מערכים במקביל ולשמור אותם          │
     * │  מסונכרנים — questions, answers, correctAnswers.            │
     * │  אינדקס 0 ב-questions חייב לתאים לאינדקס 0 ב-answers       │
     * │  ולאינדקס 0 ב-correctAnswers.                               │
     * └─────────────────────────────────────────────────────────────┘
     *
     * חישוב האינדקסים:
     *
     *   questionsPerLevel = 2
     *   startIndex = (currentLevel - 1) * 2
     *   endIndex   = startIndex + 2
     *
     *   רמה 1: startIndex = (1-1)*2 = 0,  endIndex = 2  → שאלות [0,1]
     *   רמה 2: startIndex = (2-1)*2 = 2,  endIndex = 4  → שאלות [2,3]
     *   רמה 3: startIndex = (3-1)*2 = 4,  endIndex = 6  → שאלות [4,5]
     *   רמה 4: startIndex = (4-1)*2 = 6,  endIndex = 8  → שאלות [6,7]
     *   רמה 5: startIndex = (5-1)*2 = 8,  endIndex = 10 → שאלות [8,9]
     *
     * מבנה fullAiQuizData (כפי שמגיע מ-Gemini):
     *
     *   questions:      ["שאלה0","שאלה1","שאלה2","שאלה3",...,"שאלה9"]
     *   answers:        [["א","ב","ג","ד"],["א","ב","ג","ד"],...] ← 10 מערכים
     *   correctAnswers: ["ב","ד","א","ג",...,"ב"]
     *
     * לרמה 2 (startIndex=2, endIndex=4):
     *
     *   questions.subList(2,4)      → ["שאלה2","שאלה3"]
     *   answers.subList(2,4)        → [["א","ב","ג","ד"],["א","ב","ג","ד"]]
     *   correctAnswers.subList(2,4) → ["א","ג"]
     *
     * למה new ArrayList<>(subList(...)) ולא subList ישירות?
     *   subList מחזיר "תצוגה" של הרשימה המקורית — לא עותק.
     *   אם fullAiQuizData ישתנה בהמשך, ה-subList ישתנה גם הוא.
     *   new ArrayList<>() יוצר עותק עצמאי ובטוח.
     *
     * למה Math.min(startIndex + questionsPerLevel, size)?
     *   הגנה מפני מצב שיש פחות שאלות ממה שמצופה.
     *   למשל אם Gemini החזיר 9 שאלות במקום 10,
     *   ברמה 5 endIndex יהיה 10 אבל size=9 → ייצא מגבולות.
     *   Math.min מבטיח שלא נצא מהגבולות.
     */
    private void loadAiLevel() {
        if (fullAiQuizData == null || fullAiQuizData.getQuestions() == null) return;
        startTime = System.currentTimeMillis();
        int questionsPerLevel = 2;
        int startIndex = (currentLevel - 1) * questionsPerLevel; // אינדקס התחלה לרמה זו
        int endIndex = Math.min(startIndex + questionsPerLevel, fullAiQuizData.getQuestions().size()); // לא יוצא מגבולות

        if (startIndex < endIndex) {
            // יוצר תת-קבוצה של שאלות לרמה הנוכחית
            QuizData levelSubset = new QuizData();
            levelSubset.setQuestions(new ArrayList<>(fullAiQuizData.getQuestions().subList(startIndex, endIndex)));
            levelSubset.setAnswers(new ArrayList<>(fullAiQuizData.getAnswers().subList(startIndex, endIndex)));
            levelSubset.setCorrectAnswers(new ArrayList<>(fullAiQuizData.getCorrectAnswers().subList(startIndex, endIndex)));
            currentQuestions.postValue(new Questions(levelSubset));
        } else {
            toastMessage.postValue("No more AI questions for this level.");
        }
    }

    /**
     * בודק את תשובות השחקן.
     * אם הכל נכון — מחשב זמן ומנווט הלאה.
     * אם לא — שולח הודעת שגיאה.
     */
    public void verifyAndSubmit(Map<String, String> selectedAnswers) {
        Questions data = currentQuestions.getValue();
        if (data == null || data.getQuestionsList().isEmpty()) return;

        // בודק שהשחקן ענה על כל השאלות
        if (selectedAnswers.size() != data.getQuestionsList().size()) {
            toastMessage.setValue("msg_answer_all"); // הודעה: יש לענות על כל השאלות
            return;
        }

        // בודק כל תשובה מול התשובה הנכונה
        boolean allCorrect = true;
        for (Map.Entry<String, String> entry : data.getCorrectAnswers().entrySet()) {
            if (!entry.getValue().equals(selectedAnswers.get(entry.getKey()))) {
                allCorrect = false;
                break; // מספיק למצוא שגיאה אחת
            }
        }

        if (allCorrect) {
            long duration = System.currentTimeMillis() - startTime; // חישוב זמן הרמה
            levelTimings.put(currentLevel, duration); // שמירת זמן הרמה
            int nextLevel = currentLevel + 1;
            // שולח אירוע ניווט ל-FindTheItemActivity
            navigationEvent.setValue(new NavigationEvent(
                    NavigationTarget.FIND_ITEM, nextLevel, levelTimings, fullAiQuizData, lastQuestionId));
        } else {
            toastMessage.setValue("msg_incorrect"); // הודעה: תשובה שגויה
        }
    }

    /**
     * יעדי ניווט אפשריים מה-ViewModel
     */
    public enum NavigationTarget {
        FIND_ITEM,  // עבור ל-FindTheItemActivity
        RESULTS     // עבור ל-PlayerResultsActivity
    }

    /**
     * אובייקט המכיל את כל המידע הנדרש לניווט למסך הבא.
     */
    public static class NavigationEvent {
        public final NavigationTarget target;        // לאן לנווט
        public final int nextLevel;                  // הרמה הבאה
        public final HashMap<Integer, Long> timings; // תזמוני כל הרמות
        public final QuizData aiData;                // נתוני AI (אם במצב AI)
        public final int questionId;                 // ID השאלה — לשמירה ב-game_results

        public NavigationEvent(NavigationTarget target, int nextLevel,
                               HashMap<Integer, Long> timings, QuizData aiData, int questionId) {
            this.target = target;
            this.nextLevel = nextLevel;
            this.timings = timings;
            this.aiData = aiData;
            this.questionId = questionId;
        }
    }

    /**
     * Factory — מאפשר ל-ViewModel לקבל QuestionRepository דרך הזרקת תלויות.
     * נדרש כי ViewModel רגיל לא יכול לקבל פרמטרים בנאי.
     */
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
