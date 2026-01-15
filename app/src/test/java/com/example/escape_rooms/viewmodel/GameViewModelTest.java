package com.example.escape_rooms.viewmodel;

import android.app.Application;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.test.core.app.ApplicationProvider;

import com.example.escape_rooms.model.Question;
import com.example.escape_rooms.repository.QuestionRepository;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * 100% Mockito-free unit test for GameViewModel.
 * Uses Robolectric only to provide a valid Application context for AndroidViewModel.
 */
@RunWith(RobolectricTestRunner.class)
public class GameViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private GameViewModel viewModel;
    private FakeQuestionRepository fakeRepository;

    @Before
    public void setUp() {
        fakeRepository = new FakeQuestionRepository();
        // Use ApplicationProvider to get a valid context for AndroidViewModel
        Application application = ApplicationProvider.getApplicationContext();
        viewModel = new GameViewModel(application, fakeRepository);
    }

    @Test
    public void initLevel_loadsQuestionsFromRepository() {
        viewModel.initLevel(1, new HashMap<>());

        // Verify state on the Fake
        assertEquals(1, fakeRepository.lastLevelRequested);
        
        // Trigger fake success
        fakeRepository.triggerSuccess(createMockQuestions());

        // Verify LiveData directly
        assertNotNull(viewModel.getCurrentQuestions().getValue());
        assertEquals("Q1", viewModel.getCurrentQuestions().getValue().getQuestionsList().get(0));
    }

    @Test
    public void verifyAndSubmit_withCorrectAnswers_triggersNextLevel() {
        viewModel.initLevel(1, new HashMap<>());
        fakeRepository.triggerSuccess(createMockQuestions());

        Map<String, String> selected = new HashMap<>();
        selected.put("Q1", "A1");

        viewModel.verifyAndSubmit(selected);

        // Verify LiveData results
        assertNotNull(viewModel.getNavigationEvent().getValue());
        assertEquals(2, viewModel.getNavigationEvent().getValue().nextLevel);
    }

    @Test
    public void verifyAndSubmit_withIncorrectAnswers_triggersToast() {
        viewModel.initLevel(1, new HashMap<>());
        fakeRepository.triggerSuccess(createMockQuestions());

        Map<String, String> selected = new HashMap<>();
        selected.put("Q1", "WRONG");

        viewModel.verifyAndSubmit(selected);

        // Verify toast message LiveData
        assertEquals("msg_incorrect", viewModel.getToastMessage().getValue());
    }

    private List<Question> createMockQuestions() {
        Question q1 = new Question();
        q1.setQuestion("Q1");
        q1.setCorrectAnswer("A1");
        q1.setAnswers(Arrays.asList("A1", "A2"));
        return Arrays.asList(q1);
    }

    /**
     * Manual Fake Repository subclass.
     */
    private static class FakeQuestionRepository extends QuestionRepository {
        public int lastLevelRequested = -1;
        public QuestionsCallback lastCallback = null;

        @Override
        public void getQuestionsForLevel(int level, QuestionsCallback callback) {
            this.lastLevelRequested = level;
            this.lastCallback = callback;
        }

        public void triggerSuccess(List<Question> questions) {
            if (lastCallback != null) lastCallback.onSuccess(questions);
        }
    }
}
