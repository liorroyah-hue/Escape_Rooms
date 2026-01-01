package com.example.escape_rooms.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.escape_rooms.model.User;
import com.example.escape_rooms.repository.UserRepository;

public class SignUpViewModel extends ViewModel {
    private final UserRepository userRepository = new UserRepository();
    
    private final MutableLiveData<Boolean> signUpResult = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public LiveData<Boolean> getSignUpResult() { return signUpResult; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }

    public void signUp(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            errorMessage.setValue("Please enter both username and password");
            return;
        }

        isLoading.setValue(true);
        // Using a dummy ID 0, Supabase should handle auto-increment or we'd handle it differently
        User newUser = new User(0, username, password);
        
        userRepository.addUser(newUser, new UserRepository.UsersCallback<User>() {
            @Override
            public void onSuccess(User result) {
                isLoading.postValue(false);
                signUpResult.postValue(true);
            }

            @Override
            public void onError(Exception e) {
                isLoading.postValue(false);
                errorMessage.postValue("Registration failed: " + e.getMessage());
            }
        });
    }
}
