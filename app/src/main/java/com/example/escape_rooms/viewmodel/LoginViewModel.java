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

/**
 * מנהל את לוגיקת הכניסה — אימות שם משתמש וסיסמה ושמירת פרטי המשתמש.
 */
public class LoginViewModel extends AndroidViewModel {
    private final UserRepository userRepository = new UserRepository();

    private final MutableLiveData<Boolean> loginResult = new MutableLiveData<>();  // הצלחת כניסה
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();  // הודעת שגיאה
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();    // מצב טעינה

    public LoginViewModel(@NonNull Application application) { super(application); }

    public LiveData<Boolean> getLoginResult() { return loginResult; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }

    /**
     * מאמת שם משתמש וסיסמה מול רשימת המשתמשים ב-Supabase.
     */
    public void login(String username, String password) {
        // ולידציה בסיסית לפני שליחה לשרת
        if (username == null || username.trim().isEmpty()) {
            errorMessage.setValue("אנא הזן שם משתמש");
            return;
        }
        if (password == null || password.isEmpty()) {
            errorMessage.setValue("אנא הזן סיסמה");
            return;
        }

        isLoading.setValue(true);
        // שולף את כל המשתמשים ומחפש התאמה בצד הלקוח
        userRepository.getAllUsers(new UserRepository.UsersCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> userList) {
                isLoading.postValue(false);
                User authenticatedUser = null;

                // מחפש משתמש עם שם וסיסמה תואמים
                for (User user : userList) {
                    if (user.getUsername().equalsIgnoreCase(username.trim()) && // לא רגיש לאותיות
                        user.getPassword().equals(password)) {
                        authenticatedUser = user;
                        break;
                    }
                }

                if (authenticatedUser != null) {
                    saveUserToPrefs(authenticatedUser); // שומר פרטים לשימוש בהמשך
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

    /**
     * שומר user_id ושם משתמש ב-SharedPreferences.
     * נתונים אלה משמשים בכל הפרויקט לזיהוי המשתמש המחובר.
     */
    private void saveUserToPrefs(User user) {
        SharedPreferences prefs = getApplication().getSharedPreferences("EscapeRoomPrefs", Context.MODE_PRIVATE);
        prefs.edit()
             .putLong("current_user_id", user.getId() != null ? user.getId().longValue() : -1) // שומר ID
             .putString("current_username", user.getUsername()) // שומר שם משתמש
             .apply(); // שמירה אסינכרונית
    }
}
