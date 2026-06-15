package com.example.escape_rooms.repository;

import android.util.Log;
import androidx.annotation.NonNull;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * מנהל שמירת תוצאות משחק ושליפת לוח התוצאות מ-Supabase.
 */
public class GameRepository extends BaseRepository {

    /**
     * ממשק קריאת חזרה לשמירת תוצאה — הצלחה או שגיאה בלי נתונים
     */
    public interface GameResultCallback {
        void onSuccess();
        void onError(Exception e);
    }

    /**
     * ממשק קריאת חזרה ללוח התוצאות — מחזיר רשימת GameResult
     */
    public interface LeaderboardCallback {
        void onSuccess(List<GameResult> results);
        void onError(Exception e);
    }

    /**
     * מייצג שורה אחת מטבלת game_results — נתוני תוצאה של שחקן
     */
    public static class GameResult {
        public long user_id;       // מזהה השחקן
        public String username;    // שם המשתמש (נטען מטבלת User דרך JOIN)
        public long total_time_ms; // זמן כולל במילישניות
        public int levels_completed; // מספר רמות שהושלמו
    }

    /**
     * שומר תוצאת משחק ל-game_results ב-Supabase.
     * כולל room_id, question_id, id_picture לקישור הטבלאות.
     */
    public void saveGameResult(long userId, long totalTimeMillis, int levelsCompleted,
                                int roomId, int questionId, int idPicture,
                                GameResultCallback callback) {
        String url = SUPABASE_URL + "/rest/v1/game_results";

        // בנית אובייקט JSON לשליחה
        Map<String, Object> data = new HashMap<>();
        data.put("user_id", userId);           // מזהה השחקן
        data.put("total_time_ms", totalTimeMillis); // זמן כולל
        data.put("levels_completed", levelsCompleted); // רמות שהושלמו
        if (roomId > 0)     data.put("room_id", roomId);       // מזהה החדר — רק אם תקין
        if (questionId > 0) data.put("question_id", questionId); // מזהה השאלה — רק אם תקין
        if (idPicture > 0)  data.put("id_picture", idPicture);   // מזהה התמונה — רק אם תקין

        RequestBody body = RequestBody.create(gson.toJson(data), MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(url)
                .post(body) // POST — יצירת שורה חדשה
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) { callback.onError(e); }
            @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try (Response resp = response) {
                    if (resp.isSuccessful()) {
                        callback.onSuccess();
                    } else {
                        Log.e("GameRepo", "Save Error: " + resp.code() + " " + resp.body().string());
                        callback.onError(new Exception("Save failed: " + resp.code()));
                    }
                }
            }
        });
    }

    /**
     * שולף את לוח התוצאות מ-Supabase.
     * מחזיר Top 10 שחקנים — כל שחקן מופיע פעם אחת עם הזמן הטוב שלו.
     *
     * ┌─────────────────────────────────────────────────────────────┐
     * │                    מה הפונקציה עושה                         │
     * ├─────────────────────────────────────────────────────────────┤
     * │  1. שולחת שאילתה ל-Supabase עם JOIN לטבלת User.            │
     * │     הנתונים מגיעים ממוינים לפי זמן עולה.                   │
     * │                                                             │
     * │  2. הנתונים מגיעים כ-List<Map<String,Object>> ולא          │
     * │     כ-List<GameResult> — כי ה-JOIN יוצר מבנה מקונן         │
     * │     ש-Gson לא יכול להמיר ישירות לאובייקט.                  │
     * │     לכן צריך לחלץ כל שדה ידנית עם cast.                    │
     * │                                                             │
     * │  3. לוגיקת כפילויות: כל שחקן מופיע פעם אחת בלבד.          │
     * │     הטריק: LinkedHashMap עם user_id כמפתח.                 │
     * │     מכיוון שהנתונים ממוינים לפי זמן עולה —                 │
     * │     הפעם הראשונה שרואים שחקן היא הזמן הטוב שלו.            │
     * │     אם רואים אותו שוב — מדלגים (continue).                 │
     * └─────────────────────────────────────────────────────────────┘
     *
     * מבנה JSON שמגיע מ-Supabase (כולל ה-JOIN):
     *
     *   [
     *     {
     *       "user_id": 1,
     *       "total_time_ms": 12000,
     *       "levels_completed": 5,
     *       "User": {                  ← אובייקט מקונן מה-JOIN
     *         "username": "yoni"
     *       }
     *     },
     *     ...
     *   ]
     *
     * לכן:
     *   map.get("user_id")          → Number (לא long ישירות!) → צריך .longValue()
     *   map.get("User")             → Map<String,Object> מקונן
     *   ((Map)map.get("User"))
     *       .get("username")        → String שם המשתמש
     *
     * זרימת לוגיקת הכפילויות:
     *
     *   נתונים ממוינים: [יוני:12000, דנה:15000, יוני:18000, רון:20000]
     *        ↓
     *   לולאה:
     *     יוני:12000  → לא ב-map → מוסיף      { יוני:12000 }
     *     דנה:15000   → לא ב-map → מוסיף      { יוני:12000, דנה:15000 }
     *     יוני:18000  → כבר ב-map → continue! { יוני:12000, דנה:15000 }
     *     רון:20000   → לא ב-map → מוסיף      { יוני:12000, דנה:15000, רון:20000 }
     *        ↓
     *   תוצאה: כל שחקן פעם אחת עם הזמן הטוב שלו ✓
     *
     * למה LinkedHashMap ולא HashMap רגיל?
     *   HashMap רגיל לא מבטיח סדר — השחקנים יכולים לצאת בסדר אקראי.
     *   LinkedHashMap שומר את סדר ההכנסה — אז המקום ה-1 תמיד יהיה
     *   הראשון שהוכנס (הכי מהיר), המקום ה-2 השני, וכך הלאה.
     */
    public void getTopScores(LeaderboardCallback callback) {
        // JOIN עם טבלת User לשם המשתמש — מסודר לפי זמן עולה (הכי מהיר ראשון)
        String url = SUPABASE_URL + "/rest/v1/game_results"
                + "?select=user_id,total_time_ms,levels_completed,User(username)"
                + "&order=total_time_ms.asc";

        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) { callback.onError(e); }

            @Override
            @SuppressWarnings("unchecked")
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try (Response resp = response) {
                    if (resp.isSuccessful()) {
                        String json = resp.body().string();
                        Type listType = new TypeToken<List<Map<String, Object>>>() {}.getType();
                        List<Map<String, Object>> rawData = gson.fromJson(json, listType);

                        // LinkedHashMap — שומר סדר הכנסה, מפתח = user_id
                        Map<Long, GameResult> bestPerUser = new java.util.LinkedHashMap<>();

                        for (Map<String, Object> map : rawData) {
                            long uid = ((Number) map.get("user_id")).longValue();

                            // אם השחקן כבר מופיע — מדלגים (כי הנתונים מסודרים לפי זמן עולה, הראשון הוא הטוב ביותר)
                            if (bestPerUser.containsKey(uid)) continue;

                            GameResult gr = new GameResult();
                            gr.user_id = uid;
                            gr.total_time_ms = ((Number) map.get("total_time_ms")).longValue();
                            gr.levels_completed = ((Number) map.get("levels_completed")).intValue();

                            // שליפת שם המשתמש מה-JOIN עם טבלת User
                            Map<String, Object> userMap = (Map<String, Object>) map.get("User");
                            gr.username = (userMap != null) ? (String) userMap.get("username") : "Unknown Player";

                            bestPerUser.put(uid, gr);

                            // עצירה אחרי 10 שחקנים — Top 10 בלבד
                            if (bestPerUser.size() == 10) break;
                        }

                        callback.onSuccess(new ArrayList<>(bestPerUser.values()));
                    } else {
                        callback.onError(new Exception("Error " + resp.code()));
                    }
                }
            }
        });
    }
}
