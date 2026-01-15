package com.example.escape_rooms.repository;

import androidx.annotation.NonNull;
import com.example.escape_rooms.model.Question;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class QuestionRepository {
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();

    private static QuestionRepository instance;

    public static synchronized QuestionRepository getInstance() {
        if (instance == null) {
            instance = new QuestionRepository();
        }
        return instance;
    }

    public static synchronized void setTestInstance(QuestionRepository testInstance) {
        instance = testInstance;
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

    public interface QuestionsCallback {
        void onSuccess(List<Question> questions);
        void onError(Exception e);
    }
}
