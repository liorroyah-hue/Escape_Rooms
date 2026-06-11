package com.example.escape_rooms.viewmodel;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.escape_rooms.model.User;
import com.example.escape_rooms.repository.UserRepository;

import java.util.List;

public class LoginViewModel extends AndroidViewModel {
    private final UserRepository userRepository = new UserRepository();
    
    private final MutableLiveData<Boolean> loginResult = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public LoginViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<Boolean> getLoginResult() { return loginResult; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }

    public void login(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            errorMessage.setValue("אנא הזן שם משתמש");
            return;
        }
        if (password == null || password.isEmpty()) {
            errorMessage.setValue("אנא הזן סיסמה");
            return;
        }

        isLoading.setValue(true);
        userRepository.getAllUsers(new UserRepository.UsersCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> userList) {
                isLoading.postValue(false);
                User authenticatedUser = null;
                for (User user : userList) {
                    if (user.getUsername().equalsIgnoreCase(username.trim()) && 
                        user.getPassword().equals(password)) {
                        authenticatedUser = user;
                        break;
                    }
                }
                
                if (authenticatedUser != null) {
                    // Save both ID and username to SharedPreferences
                    saveUserToPrefs(authenticatedUser);
                    loginResult.postValue(true);
                } else {
                    errorMessage.postValue("שם משתמש או סיסמה שגויים");
                }
            }

            @Override
            public void onError(Exception e) {
                isLoading.postValue(false);
                errorMessage.postValue("שגיאת חיבור: " + e.getMessage());
            }
        });
    }

    private void saveUserToPrefs(User user) {
        SharedPreferences prefs = getApplication().getSharedPreferences("EscapeRoomPrefs", Context.MODE_PRIVATE);
        prefs.edit()
             .putLong("current_user_id", user.getId() != null ? user.getId().longValue() : -1)
             .putString("current_username", user.getUsername())
             .apply();
    }
}
