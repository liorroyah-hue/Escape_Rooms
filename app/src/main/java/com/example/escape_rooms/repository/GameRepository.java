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

public class GameRepository extends BaseRepository {

    public interface GameResultCallback {
        void onSuccess();
        void onError(Exception e);
    }

    public interface LeaderboardCallback {
        void onSuccess(List<GameResult> results);
        void onError(Exception e);
    }

    public static class GameResult {
        public long user_id;
        public String username;
        public long total_time_ms; // סכום כל 5 הרמות
        public int levels_completed;
    }

    /**
     * שומר תוצאה לרמה בודדת ב-game_results.
     * נקרא בסוף כל רמה עם הזמן הספציפי של אותה רמה
     * + ה-IDs של החדר, השאלה, והתמונה שהיו באותה רמה.
     */
    public void saveLevelResult(long userId, int level, long levelTimeMs,
                                 int roomId, int questionId, int pictureId,
                                 GameResultCallback callback) {
        String url = SUPABASE_URL + "/rest/v1/game_results";

        Map<String, Object> data = new HashMap<>();
        data.put("user_id", userId);
        data.put("total_time_ms", levelTimeMs);   // זמן הרמה הספציפית
        data.put("levels_completed", level);       // מספר הרמה (1-5)
        if (roomId > 0)     data.put("room_id", roomId);
        if (questionId > 0) data.put("question_id", questionId);
        if (pictureId > 0)  data.put("id_picture", pictureId);

        RequestBody body = RequestBody.create(gson.toJson(data), MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) { callback.onError(e); }
            @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try (Response resp = response) {
                    if (resp.isSuccessful()) {
                        Log.d("GameRepo", "Level " + level + " saved for user " + userId);
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
     * מחשב סכום זמנים של כל 5 הרמות לכל שחקן.
     * מציג רק שחקנים שהשלימו את כל 5 הרמות — סכום הוא הזמן האמיתי של המשחק.
     * כל שחקן מופיע פעם אחת עם הסכום הטוב ביותר שלו.
     */
    public void getTopScores(LeaderboardCallback callback) {
        // שולף את כל השורות — נחשב סכום לכל שחקן בקוד
        String url = SUPABASE_URL + "/rest/v1/game_results"
                + "?select=user_id,total_time_ms,levels_completed,User(username)"
                + "&order=user_id.asc,levels_completed.asc";

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

                        // מבנה נתונים: user_id → { username, רשימת זמנים לפי רמה }
                        // Map<userId, Map<level, timeMs>>
                        Map<Long, Map<Integer, Long>> userLevelTimes = new java.util.LinkedHashMap<>();
                        Map<Long, String> usernames = new HashMap<>();

                        for (Map<String, Object> row : rawData) {
                            long uid   = ((Number) row.get("user_id")).longValue();
                            long time  = ((Number) row.get("total_time_ms")).longValue();
                            int  level = ((Number) row.get("levels_completed")).intValue();

                            // שם המשתמש מה-JOIN
                            Map<String, Object> userMap = (Map<String, Object>) row.get("User");
                            String username = (userMap != null) ? (String) userMap.get("username") : "Unknown";
                            usernames.put(uid, username);

                            // שומר את הזמן של הרמה הזו לשחקן הזה
                            userLevelTimes.computeIfAbsent(uid, k -> new HashMap<>()).put(level, time);
                        }

                        // מחשב סכום זמנים רק לשחקנים שהשלימו את כל 5 הרמות
                        Map<Long, Long> totalPerUser = new HashMap<>();
                        for (Map.Entry<Long, Map<Integer, Long>> entry : userLevelTimes.entrySet()) {
                            Map<Integer, Long> levels = entry.getValue();
                            if (levels.size() >= 5) { // רק מי שעשה 5 רמות
                                long sum = 0;
                                for (long t : levels.values()) sum += t;
                                totalPerUser.put(entry.getKey(), sum);
                            }
                        }

                        // ממיין לפי סכום עולה — הכי מהיר ראשון
                        List<Map.Entry<Long, Long>> sorted = new ArrayList<>(totalPerUser.entrySet());
                        sorted.sort((a, b) -> Long.compare(a.getValue(), b.getValue()));

                        // בונה רשימת תוצאות — Top 10
                        List<GameResult> results = new ArrayList<>();
                        for (int i = 0; i < Math.min(10, sorted.size()); i++) {
                            long uid = sorted.get(i).getKey();
                            GameResult gr = new GameResult();
                            gr.user_id = uid;
                            gr.total_time_ms = sorted.get(i).getValue(); // סכום 5 הרמות
                            gr.levels_completed = 5;
                            gr.username = usernames.getOrDefault(uid, "Unknown");
                            results.add(gr);
                        }

                        callback.onSuccess(results);
                    } else {
                        callback.onError(new Exception("Error " + resp.code()));
                    }
                }
            }
        });
    }
}
