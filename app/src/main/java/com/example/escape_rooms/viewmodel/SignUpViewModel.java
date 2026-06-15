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

/**
 * מנהל את לוגיקת ההרשמה — ולידציה, בדיקת כפילות שם, יצירת משתמש.
 */
public class SignUpViewModel extends AndroidViewModel {
    private final UserRepository userRepository = new UserRepository();

    private final MutableLiveData<Boolean> signUpResult = new MutableLiveData<>(); // הצלחת הרשמה
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();  // הודעת שגיאה
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();    // מצב טעינה

    public SignUpViewModel(@NonNull Application application) { super(application); }

    public LiveData<Boolean> getSignUpResult() { return signUpResult; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }

    /**
     * מבצע ולידציה ומפעיל תהליך הרשמה.
     */
    public void signUp(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            errorMessage.setValue("Please enter both username and password");
            return;
        }

        // סיסמה חייבת להיות 4-12 ספרות בלבד
        if (!password.matches("^[0-9]{4,12}$")) {
            errorMessage.setValue("Password must be 4-12 digits (numbers only)");
            return;
        }

        isLoading.setValue(true);
        // שלב 1: בודק אם השם כבר תפוס לפני יצירה
        userRepository.isUsernameTaken(username, new UserRepository.UsersCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean isTaken) {
                if (isTaken) {
                    isLoading.postValue(false);
                    errorMessage.postValue("Registration failed: Username is already taken.");
                } else {
                    performActualSignUp(username, password); // שם פנוי — ממשיך ליצירה
                }
            }

            @Override
            public void onError(Exception e) {
                isLoading.postValue(false);
                errorMessage.postValue("Failed to check username availability: " + e.getMessage());
            }
        });
    }

    /**
     * שלב 2: יוצר משתמש חדש ב-Supabase.
     */
    private void performActualSignUp(String username, String password) {
        User newUser = new User(username, password); // יוצר אובייקט משתמש

        userRepository.addUser(newUser, new UserRepository.UsersCallback<User>() {
            @Override
            public void onSuccess(User result) {
                if (result != null) {
                    saveUserToPrefs(result); // שומר פרטים לכניסה אוטומטית
                }
                isLoading.postValue(false);
                signUpResult.postValue(true); // מאותת הצלחה ל-View
            }

            @Override
            public void onError(Exception e) {
                isLoading.postValue(false);
                errorMessage.postValue("Registration failed: " + e.getMessage());
            }
        });
    }

    /**
     * שומר פרטי משתמש חדש ב-SharedPreferences — מאפשר כניסה אוטומטית אחרי הרשמה.
     */
    private void saveUserToPrefs(User user) {
        SharedPreferences prefs = getApplication().getSharedPreferences("EscapeRoomPrefs", Context.MODE_PRIVATE);
        prefs.edit()
             .putLong("current_user_id", user.getId() != null ? user.getId().longValue() : -1)
             .putString("current_username", user.getUsername())
             .apply();
    }
}
