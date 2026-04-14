package com.example.escape_rooms.repository;

import androidx.annotation.NonNull;
import com.example.escape_rooms.model.Question;
import com.example.escape_rooms.model.FindItemTask;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Random;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class QuestionRepository {
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();
    private final Random random = new Random();

    private static QuestionRepository instance;

    public static synchronized QuestionRepository getInstance() {
        if (instance == null) {
            instance = new QuestionRepository();
        }
        return instance;
    }

    public void getQuestionsForLevel(int level, QuestionsCallback callback) {
        String url = UserRepository.SUPABASE_URL + "/rest/v1/questions?level=eq." + level + "&select=*&limit=2";

        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", UserRepository.SUPABASE_KEY)
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
                        Type listType = new TypeToken<List<Question>>() {}.getType();
                        List<Question> questions = gson.fromJson(json, listType);
                        callback.onSuccess(questions);
                    } else {
                        callback.onError(new Exception("Fetch Error: " + resp.code()));
                    }
                }
            }
        });
    }

    public void getRandomFindItemTask(FindItemCallback callback) {
        // Ensure the table name 'find_item_tasks' exists in your Supabase DB
        String url = UserRepository.SUPABASE_URL + "/rest/v1/find_item_tasks?select=*&limit=10";

        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", UserRepository.SUPABASE_KEY)
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
                        Type listType = new TypeToken<List<FindItemTask>>() {}.getType();
                        List<FindItemTask> tasks = gson.fromJson(json, listType);
                        
                        if (tasks != null && !tasks.isEmpty()) {
                            int randomIndex = random.nextInt(tasks.size());
                            callback.onSuccess(tasks.get(randomIndex));
                        } else {
                            callback.onError(new Exception("No FindItem tasks found"));
                        }
                    } else {
                        // Log the error body to see exactly why Supabase rejected the request
                        String errorBody = resp.body() != null ? resp.body().string() : "Unknown error";
                        callback.onError(new Exception("Supabase Error " + resp.code() + ": " + errorBody));
                    }
                }
            }
        });
    }

    public interface QuestionsCallback {
        void onSuccess(List<Question> questions);
        void onError(Exception e);
    }

    public interface FindItemCallback {
        void onSuccess(FindItemTask task);
        void onError(Exception e);
    }
}
