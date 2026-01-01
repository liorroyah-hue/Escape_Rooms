package com.example.escape_rooms.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.escape_rooms.model.User;
import com.example.escape_rooms.repository.UserRepository;

import org.json.JSONObject;

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
                String rawError = e.getMessage();
                
                if (rawError != null && rawError.contains("Server Error: ")) {
                    try {
                        // Extract the JSON part
                        String jsonStr = rawError.replace("Server Error: ", "");
                        JSONObject json = new JSONObject(jsonStr);
                        
                        String message = json.optString("message", "");
                        String details = json.optString("details", "");
                        
                        if (message.contains("duplicate key value violates unique constraint")) {
                            errorMessage.postValue("Registration failed: This account already exists (ID conflict).");
                        } else {
                            errorMessage.postValue("Server Error: " + message + (details.isEmpty() ? "" : " - " + details));
                        }
                    } catch (Exception ex) {
                        errorMessage.postValue("Registration failed: " + rawError);
                    }
                } else {
                    errorMessage.postValue("Registration failed: " + rawError);
                }
            }
        });
    }
}
