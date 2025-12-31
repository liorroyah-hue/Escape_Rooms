package com.example.escape_rooms.repository;

import android.util.Log;
import androidx.annotation.NonNull;
import com.example.escape_rooms.model.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UserRepository {
    public static final String SUPABASE_URL="https://wjwbshqrvbgdtqanztqz.supabase.co";
    public static final String SUPABASE_KEY="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Indqd2JzaHFydmJnZHRxYW56dHF6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjMyOTA1MzgsImV4cCI6MjA3ODg2NjUzOH0.2LL7iEfi0gv_JAaR2984X2ybn4LclXGSTwR-9runIhM";
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();

    public interface UsersCallback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }

    public void getAllUsers(UsersCallback<List<User>> callback){
        String url = SUPABASE_URL + "/rest/v1/User?select=*";

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
                if(response.isSuccessful()) {
                    String json = response.body().string();
                    Type listType = new TypeToken<List<User>>() {}.getType();
                    List<User> users = gson.fromJson(json, listType);
                    callback.onSuccess(users);
                } else {
                    callback.onError(new Exception("Error fetching users: " + response.code()));
                }
            }
        });
    }

    public void addUser(User newUser, UsersCallback<User> callback){
        String url = SUPABASE_URL + "/rest/v1/User";
        String json = gson.toJson(newUser);
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=representation")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onError(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if(response.isSuccessful()){
                    String jsonResponse = response.body().string();
                    Type listType = new TypeToken<List<User>>(){}.getType();
                    List<User> addedUsers = gson.fromJson(jsonResponse, listType);
                    if(addedUsers != null && !addedUsers.isEmpty()){
                        callback.onSuccess(addedUsers.get(0));
                    } else {
                        callback.onError(new Exception("Failed to parse added user"));
                    }
                } else {
                    callback.onError(new Exception("Error adding user: " + response.code()));
                }
            }
        });
    }
}
