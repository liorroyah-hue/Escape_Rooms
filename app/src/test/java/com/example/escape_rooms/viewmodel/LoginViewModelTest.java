package com.example.escape_rooms.viewmodel;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

import com.example.escape_rooms.model.User;
import com.example.escape_rooms.repository.UserRepository;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

public class LoginViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private LoginViewModel loginViewModel;

    @Mock
    private Observer<Boolean> loginResultObserver;
    @Mock
    private Observer<String> errorObserver;
    @Mock
    private Observer<Boolean> loadingObserver;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        loginViewModel = new LoginViewModel();
        
        loginViewModel.getLoginResult().observeForever(loginResultObserver);
        loginViewModel.getErrorMessage().observeForever(errorObserver);
        loginViewModel.getIsLoading().observeForever(loadingObserver);
    }

    @Test
    public void login_SetsLoadingState() {
        loginViewModel.login("admin", "1234");
        verify(loadingObserver).onChanged(true);
    }

    // Note: To test the actual success/failure, we'd ideally mock the UserRepository.
    // Since the repository is currently instantiated inside the ViewModel, we test 
    // the ViewModel's state handling logic.
}
