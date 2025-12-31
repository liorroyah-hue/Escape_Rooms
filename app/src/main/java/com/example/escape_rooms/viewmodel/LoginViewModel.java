package com.example.escape_rooms.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.escape_rooms.model.User;
import com.example.escape_rooms.repository.UserRepository;

import java.util.List;

public class LoginViewModel extends ViewModel {
    private final UserRepository userRepository = new UserRepository();
    
    private final MutableLiveData<Boolean> loginResult = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public LiveData<Boolean> getLoginResult() { return loginResult; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }

    public void login(String username, String password) {
        isLoading.setValue(true);
        userRepository.getAllUsers(new UserRepository.UsersCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> userList) {
                isLoading.postValue(false);
                boolean success = false;
                for (User user : userList) {
                    if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
                        success = true;
                        break;
                    }
                }
                if (success) {
                    loginResult.postValue(true);
                } else {
                    errorMessage.postValue("Invalid username or password");
                }
            }

            @Override
            public void onError(Exception e) {
                isLoading.postValue(false);
                errorMessage.postValue("Connection error: " + e.getMessage());
            }
        });
    }
}
