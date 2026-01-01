package com.example.escape_rooms.viewmodel;

import android.app.Application;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;
import androidx.test.core.app.ApplicationProvider;

import com.example.escape_rooms.model.Questions;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class GameViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private GameViewModel viewModel;
    private Application application;

    @Mock
    private Observer<Questions> questionsObserver;
    @Mock
    private Observer<String> toastObserver;
    @Mock
    private Observer<GameViewModel.NavigationEvent> navObserver;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        application = ApplicationProvider.getApplicationContext();
        viewModel = new GameViewModel(application);
        
        viewModel.getCurrentQuestions().observeForever(questionsObserver);
        viewModel.getToastMessage().observeForever(toastObserver);
        viewModel.getNavigationEvent().observeForever(navObserver);
    }

    @Test
    public void initLevel_loadsQuestions() {
        viewModel.initLevel(1, new HashMap<>());
        
        verify(questionsObserver, atLeastOnce()).onChanged(any(Questions.class));
        Questions questions = viewModel.getCurrentQuestions().getValue();
        assertNotNull(questions);
        assertEquals("מהו צבע השמיים?", questions.getQuestionsList().get(0));
    }

    @Test
    public void verifyAndSubmit_withMissingAnswers_triggersToast() {
        viewModel.initLevel(1, new HashMap<>());
        
        // Level 1 has 2 questions, we provide only 1
        Map<String, String> selected = new HashMap<>();
        selected.put("מהו צבע השמיים?", "כחול");
        
        viewModel.verifyAndSubmit(selected);
        
        verify(toastObserver).onChanged("msg_answer_all");
    }

    @Test
    public void verifyAndSubmit_withIncorrectAnswer_triggersToast() {
        viewModel.initLevel(1, new HashMap<>());
        
        Map<String, String> selected = new HashMap<>();
        selected.put("מהו צבע השמיים?", "אדום"); // Wrong
        selected.put("איזה גז בני אדם צריכים כדי לנשום?", "חמצן");
        
        viewModel.verifyAndSubmit(selected);
        
        verify(toastObserver).onChanged("msg_incorrect");
    }

    @Test
    public void verifyAndSubmit_withCorrectAnswers_triggersNextLevel() {
        viewModel.initLevel(1, new HashMap<>());
        
        Map<String, String> selected = new HashMap<>();
        selected.put("מהו צבע השמיים?", "כחול");
        selected.put("איזה גז בני אדם צריכים כדי לנשום?", "חמצן");
        
        viewModel.verifyAndSubmit(selected);
        
        verify(navObserver).onChanged(any(GameViewModel.NavigationEvent.class));
        GameViewModel.NavigationEvent event = viewModel.getNavigationEvent().getValue();
        assertNotNull(event);
        assertEquals(GameViewModel.NavigationTarget.NEXT_LEVEL, event.target);
        assertEquals(2, event.nextLevel);
    }

    @Test
    public void verifyAndSubmit_onLastLevel_triggersResults() {
        // Init level 10 (Max level)
        viewModel.initLevel(10, new HashMap<>());
        
        Map<String, String> selected = new HashMap<>();
        selected.put("מהו המדבר הגדול ביותר בעולם?", "סהרה");
        selected.put("איזה מדען מפורסם פיתח את תורת היחסות?", "איינשטיין");
        
        viewModel.verifyAndSubmit(selected);
        
        verify(navObserver).onChanged(any(GameViewModel.NavigationEvent.class));
        GameViewModel.NavigationEvent event = viewModel.getNavigationEvent().getValue();
        assertNotNull(event);
        assertEquals(GameViewModel.NavigationTarget.RESULTS, event.target);
    }
}
