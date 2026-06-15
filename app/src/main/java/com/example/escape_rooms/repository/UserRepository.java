package com.example.escape_rooms.repository;

import android.util.Log;
import androidx.annotation.NonNull;
import com.example.escape_rooms.model.User;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * מנהל את כל הפעולות על טבלת User ב-Supabase.
 * יורש SUPABASE_URL, SUPABASE_KEY, client, gson מ-BaseRepository.
 */
public class UserRepository extends BaseRepository {

    /**
     * ממשק גנרי לקריאות חזרה — T הוא סוג הנתונים המוחזרים
     */
    public interface UsersCallback<T> {
        void onSuccess(T result); // נקרא כשהפעולה הצליחה
        void onError(Exception e); // נקרא כשהייתה שגיאה
    }

    /**
     * שולף את כל המשתמשים מטבלת User.
     * משמש לאימות בזמן כניסה — בודק אם שם משתמש וסיסמה תואמים.
     */
    public void getAllUsers(UsersCallback<List<User>> callback) {
        // בנית URL לשליפת כל המשתמשים
        String url = SUPABASE_URL + "/rest/v1/User?select=*";

        // בנית בקשת GET עם headers לאימות
        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", SUPABASE_KEY) // מפתח הגישה ל-Supabase
                .addHeader("Accept", "application/json") // מבקש תגובה בפורמט JSON
                .build();

        // שליחת הבקשה בצורה אסינכרונית (לא חוסם את ה-UI)
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                // שגיאת רשת — מעביר את השגיאה למאזין
                callback.onError(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                // try-with-resources — סוגר את ה-Response אוטומטית בסיום
                try (Response resp = response) {
                    if (resp.isSuccessful()) {
                        String json = resp.body().string(); // קריאת גוף התגובה כ-String
                        Type listType = new TypeToken<List<User>>() {}.getType(); // מגדיר סוג לפרסור
                        List<User> users = gson.fromJson(json, listType); // ממיר JSON לרשימת User
                        callback.onSuccess(users);
                    } else {
                        callback.onError(new Exception("Fetch Error: " + resp.code()));
                    }
                }
            }
        });
    }

    /**
     * בודק אם שם משתמש כבר קיים בטבלה.
     * משמש בהרשמה כדי למנוע כפילויות.
     */
    public void isUsernameTaken(String username, UsersCallback<Boolean> callback) {
        // סינון לפי שם משתמש ספציפי — eq. זה "שווה ל"
        String url = SUPABASE_URL + "/rest/v1/User?username=eq." + username + "&select=username";

        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Accept", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onError(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try (Response resp = response) {
                    if (resp.isSuccessful()) {
                        String json = resp.body().string();
                        Type listType = new TypeToken<List<User>>() {}.getType();
                        List<User> users = gson.fromJson(json, listType);
                        // אם הרשימה לא ריקה — השם תפוס
                        callback.onSuccess(users != null && !users.isEmpty());
                    } else {
                        callback.onError(new Exception("Check Error: " + resp.code()));
                    }
                }
            }
        });
    }

    /**
     * מוסיף משתמש חדש לטבלה.
     * משמש בהרשמה — מחזיר את המשתמש שנוצר כולל ה-ID שנוצר אוטומטית.
     */
    public void addUser(User newUser, UsersCallback<User> callback) {
        String url = SUPABASE_URL + "/rest/v1/User";
        String json = gson.toJson(newUser); // ממיר אובייקט User ל-JSON

        Log.d("UserRepository", "Registering user with JSON: " + json);

        // יוצר גוף הבקשה מה-JSON
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(url)
                .post(body) // בקשת POST — יוצר שורה חדשה
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=representation") // מבקש שה-Supabase יחזיר את השורה שנוצרה (כולל ה-ID)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onError(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try (Response resp = response) {
                    String responseBody = resp.body().string();
                    if (resp.isSuccessful()) {
                        // Supabase מחזיר רשימה גם עבור הכנסה בודדת
                        Type listType = new TypeToken<List<User>>() {}.getType();
                        List<User> addedUsers = gson.fromJson(responseBody, listType);
                        if (addedUsers != null && !addedUsers.isEmpty()) {
                            callback.onSuccess(addedUsers.get(0)); // מחזיר את המשתמש הראשון (והיחיד)
                        } else {
                            callback.onError(new Exception("Failed to parse response"));
                        }
                    } else {
                        Log.e("UserRepository", "Supabase Error: " + responseBody);
                        callback.onError(new Exception("Server Error: " + responseBody));
                    }
                }
            }
        });
    }
}
