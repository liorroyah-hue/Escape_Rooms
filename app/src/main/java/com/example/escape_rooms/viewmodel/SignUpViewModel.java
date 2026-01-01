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
        // Create user WITHOUT id field so Supabase can auto-generate it
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
