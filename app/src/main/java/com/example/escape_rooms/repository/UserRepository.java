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

public class UserRepository extends BaseRepository {

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
                try (Response resp = response) {
                    if(resp.isSuccessful()) {
                        String json = resp.body().string();
                        Type listType = new TypeToken<List<User>>() {}.getType();
                        List<User> users = gson.fromJson(json, listType);
                        callback.onSuccess(users);
                    } else {
                        callback.onError(new Exception("Fetch Error: " + resp.code()));
                    }
                }
            }
        });
    }

    /**
     * Checks if a username already exists in the database.
     */
    public void isUsernameTaken(String username, UsersCallback<Boolean> callback) {
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
                        callback.onSuccess(users != null && !users.isEmpty());
                    } else {
                        callback.onError(new Exception("Check Error: " + resp.code()));
                    }
                }
            }
        });
    }

    public void addUser(User newUser, UsersCallback<User> callback){
        String url = SUPABASE_URL + "/rest/v1/User";
        String json = gson.toJson(newUser);
        
        Log.d("UserRepository", "Registering user with JSON: " + json);
        
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
                try (Response resp = response) {
                    String responseBody = resp.body().string();
                    if(resp.isSuccessful()){
                        Type listType = new TypeToken<List<User>>(){}.getType();
                        List<User> addedUsers = gson.fromJson(responseBody, listType);
                        if(addedUsers != null && !addedUsers.isEmpty()){
                            callback.onSuccess(addedUsers.get(0));
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
