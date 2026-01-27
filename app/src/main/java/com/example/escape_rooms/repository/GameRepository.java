package com.example.escape_rooms.repository;

import androidx.annotation.NonNull;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GameRepository {
    private static final String SUPABASE_URL = UserRepository.SUPABASE_URL;
    private static final String SUPABASE_KEY = UserRepository.SUPABASE_KEY;
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();

    public interface GameResultCallback {
        void onSuccess();
        void onError(Exception e);
    }

    public interface LeaderboardCallback {
        void onSuccess(List<GameResult> results);
        void onError(Exception e);
    }

    public static class GameResult {
        public String username;
        public long total_time_ms;
        public int levels_completed;
    }

    /**
     * Saves the total game time and details to Supabase.
     */
    public void saveGameResult(String username, long totalTimeMillis, int levelsCompleted, GameResultCallback callback) {
        String url = SUPABASE_URL + "/rest/v1/game_results";

        Map<String, Object> data = new HashMap<>();
        data.put("username", username);
        data.put("total_time_ms", totalTimeMillis);
        data.put("levels_completed", levelsCompleted);

        String json = gson.toJson(data);
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                .addHeader("Content-Type", "application/json")
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
                        callback.onSuccess();
                    } else {
                        callback.onError(new Exception("Server Error: " + resp.code()));
                    }
                }
            }
        });
    }

    /**
     * Fetches the top 10 fastest game completions from Supabase.
     */
    public void getTopScores(LeaderboardCallback callback) {
        // Fetch top 10 results, ordered by time ascending (fastest first)
        String url = SUPABASE_URL + "/rest/v1/game_results?select=username,total_time_ms,levels_completed&order=total_time_ms.asc&limit=10";

        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
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
                        Type listType = new TypeToken<List<GameResult>>() {}.getType();
                        List<GameResult> results = gson.fromJson(json, listType);
                        callback.onSuccess(results);
                    } else {
                        callback.onError(new Exception("Server Error: " + resp.code()));
                    }
                }
            }
        });
    }
}
