package com.example.escape_rooms.repository;

import androidx.annotation.NonNull;
import com.example.escape_rooms.model.Question;
import com.example.escape_rooms.model.FindItemTask;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class QuestionRepository extends BaseRepository {
    private final Random random = new Random();
    private static QuestionRepository instance;

    public static synchronized QuestionRepository getInstance() {
        if (instance == null) instance = new QuestionRepository();
        return instance;
    }

    public static class RoomData {
        @com.google.gson.annotations.SerializedName("room_id")
        public int roomId;
        @com.google.gson.annotations.SerializedName("room_name")
        public String roomName;
        @com.google.gson.annotations.SerializedName("background")
        public String background;
        @com.google.gson.annotations.SerializedName("array_of_clickable_objects")
        public String clickableObjectsRaw;

        public List<String> getClickableObjects() {
            if (clickableObjectsRaw == null || clickableObjectsRaw.isEmpty()) return new ArrayList<>();
            String cleaned = clickableObjectsRaw.trim()
                    .replaceAll("^[\\[{]", "")
                    .replaceAll("[\\]}]$", "");
            List<String> result = new ArrayList<>();
            for (String part : cleaned.split(",")) {
                String name = part.trim()
                        .replaceAll("^\"|\"$", "")
                        .replaceAll("^'|'$", "");
                if (!name.isEmpty()) result.add(name);
            }
            return result;
        }
    }

    public interface RoomCallback {
        void onSuccess(RoomData room);
        void onError(Exception e);
    }

    public void getRandomRoom(RoomCallback callback) {
        String url = SUPABASE_URL + "/rest/v1/Escape_Room?select=*";
        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Accept", "application/json")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) { callback.onError(e); }
            @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try (Response resp = response) {
                    if (resp.isSuccessful()) {
                        String json = resp.body().string();
                        Type listType = new TypeToken<List<RoomData>>() {}.getType();
                        List<RoomData> rooms = gson.fromJson(json, listType);
                        if (rooms != null && !rooms.isEmpty()) {
                            callback.onSuccess(rooms.get(random.nextInt(rooms.size())));
                        } else { callback.onError(new Exception("No rooms found")); }
                    } else { callback.onError(new Exception("Fetch Error: " + resp.code())); }
                }
            }
        });
    }

    public void getRoomById(int roomId, RoomCallback callback) {
        String url = SUPABASE_URL + "/rest/v1/Escape_Room?room_id=eq." + roomId + "&select=*";
        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Accept", "application/json")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) { callback.onError(e); }
            @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try (Response resp = response) {
                    if (resp.isSuccessful()) {
                        String json = resp.body().string();
                        Type listType = new TypeToken<List<RoomData>>() {}.getType();
                        List<RoomData> rooms = gson.fromJson(json, listType);
                        if (rooms != null && !rooms.isEmpty()) {
                            callback.onSuccess(rooms.get(0));
                        } else { callback.onError(new Exception("Room not found: " + roomId)); }
                    } else { callback.onError(new Exception("Fetch Error: " + resp.code())); }
                }
            }
        });
    }

    /**
     * Fetches questions for the given room, shuffles them, and returns 2 random ones.
     * Questions are tied to a specific room via room_id.
     */
    public void get2RandomQuestions(int roomId, QuestionsCallback callback) {
        String url = SUPABASE_URL + "/rest/v1/questions?room_id=eq." + roomId + "&select=*";
        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Accept", "application/json")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) { callback.onError(e); }
            @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try (Response resp = response) {
                    if (resp.isSuccessful()) {
                        String json = resp.body().string();
                        Type listType = new TypeToken<List<Question>>() {}.getType();
                        List<Question> all = gson.fromJson(json, listType);
                        if (all != null && !all.isEmpty()) {
                            Collections.shuffle(all);
                            callback.onSuccess(all.subList(0, Math.min(2, all.size())));
                        } else { callback.onError(new Exception("No questions found for room " + roomId)); }
                    } else { callback.onError(new Exception("Fetch Error: " + resp.code())); }
                }
            }
        });
    }

    public void getRandomFindItemTask(FindItemCallback callback) {
        String url = SUPABASE_URL + "/rest/v1/find_item_tasks?select=*";
        Request request = new Request.Builder()
                .url(url)
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Accept", "application/json")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(@NonNull Call call, @NonNull IOException e) { callback.onError(e); }
            @Override public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try (Response resp = response) {
                    if (resp.isSuccessful()) {
                        String json = resp.body().string();
                        Type listType = new TypeToken<List<FindItemTask>>() {}.getType();
                        List<FindItemTask> tasks = gson.fromJson(json, listType);
                        if (tasks != null && !tasks.isEmpty()) {
                            callback.onSuccess(tasks.get(random.nextInt(tasks.size())));
                        } else { callback.onError(new Exception("No FindItem tasks found")); }
                    } else { callback.onError(new Exception("Supabase Error " + resp.code())); }
                }
            }
        });
    }

    public interface QuestionsCallback { void onSuccess(List<Question> questions); void onError(Exception e); }
    public interface FindItemCallback { void onSuccess(FindItemTask task); void onError(Exception e); }
}
