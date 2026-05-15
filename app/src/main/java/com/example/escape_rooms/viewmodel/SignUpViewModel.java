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

        // Password validation: only numbers, 4-12 digits long
        if (!password.matches("^[0-9]{4,12}$")) {
            errorMessage.setValue("Password must be 4-12 digits (numbers only)");
            return;
        }

        isLoading.setValue(true);

        // First, check if the username is already taken
        userRepository.isUsernameTaken(username, new UserRepository.UsersCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean isTaken) {
                if (isTaken) {
                    isLoading.postValue(false);
                    errorMessage.postValue("Registration failed: Username is already taken.");
                } else {
                    // Username is available, proceed with sign up
                    performActualSignUp(username, password);
                }
            }

            @Override
            public void onError(Exception e) {
                isLoading.postValue(false);
                errorMessage.postValue("Failed to check username availability: " + e.getMessage());
            }
        });
    }

    private void performActualSignUp(String username, String password) {
        User newUser = new User(username, password);
        
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
