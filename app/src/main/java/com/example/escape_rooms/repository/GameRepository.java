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
        public long total_time_ms;
        public int levels_completed;
    }

    /**
     * Saves the final game result to game_results, including room_id, question_id, id_picture.
     */
    public void saveGameResult(long userId, long totalTimeMillis, int levelsCompleted,
                                int roomId, int questionId, int idPicture,
                                GameResultCallback callback) {
        String url = SUPABASE_URL + "/rest/v1/game_results";

        Map<String, Object> data = new HashMap<>();
        data.put("user_id", userId);
        data.put("total_time_ms", totalTimeMillis);
        data.put("levels_completed", levelsCompleted);
        if (roomId > 0)     data.put("room_id", roomId);
        if (questionId > 0) data.put("question_id", questionId);
        if (idPicture > 0)  data.put("id_picture", idPicture);

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
     * Fetches leaderboard — best time per user, each player appears only once, top 10.
     */
    public void getTopScores(LeaderboardCallback callback) {
        // Fetch all rows ordered by time asc — we deduplicate in code keeping best per user
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

                        // Deduplicate: keep only the best (lowest) time per user_id
                        // Since results are ordered asc, the first occurrence of each user IS their best
                        Map<Long, GameResult> bestPerUser = new java.util.LinkedHashMap<>();
                        for (Map<String, Object> map : rawData) {
                            long uid = ((Number) map.get("user_id")).longValue();
                            if (bestPerUser.containsKey(uid)) continue; // already have their best

                            GameResult gr = new GameResult();
                            gr.user_id = uid;
                            gr.total_time_ms = ((Number) map.get("total_time_ms")).longValue();
                            gr.levels_completed = ((Number) map.get("levels_completed")).intValue();
                            Map<String, Object> userMap = (Map<String, Object>) map.get("User");
                            gr.username = (userMap != null) ? (String) userMap.get("username") : "Unknown Player";
                            bestPerUser.put(uid, gr);

                            if (bestPerUser.size() == 10) break; // top 10 is enough
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
