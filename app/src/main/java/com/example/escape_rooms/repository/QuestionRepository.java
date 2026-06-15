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

/**
 * מנהל שליפת חדרים, שאלות, ומשימות "מצא את הפריט" מ-Supabase.
 * Singleton — קיים רק עותק אחד לאורך כל חיי האפליקציה.
 */
public class QuestionRepository extends BaseRepository {

    private final Random random = new Random(); // לבחירת פריטים אקראיים
    private static QuestionRepository instance; // עותק יחיד (Singleton)

    /**
     * מחזיר את העותק היחיד של ה-Repository.
     * synchronized — בטוח לשימוש ממספר Threads בו-זמנית.
     */
    public static synchronized QuestionRepository getInstance() {
        if (instance == null) instance = new QuestionRepository();
        return instance;
    }

    /**
     * מייצג שורה מטבלת Escape_Room.
     * SerializedName מגשר בין שמות שדות Java לשמות עמודות Supabase.
     */
    public static class RoomData {
        @com.google.gson.annotations.SerializedName("room_id")
        public int roomId; // מזהה החדר

        @com.google.gson.annotations.SerializedName("room_name")
        public String roomName; // שם החדר (bedroom, zoo, space)

        @com.google.gson.annotations.SerializedName("background")
        public String background; // שם קובץ הרקע (למשל emptyroom.png)

        @com.google.gson.annotations.SerializedName("array_of_clickable_objects")
        public String clickableObjectsRaw; // הרשימה כ-String גולמי מהDB (למשל "[img1.png, img2.png]")

        /**
         * מפרסר את מחרוזת הטקסט הגולמית לרשימת שמות קבצים.
         * פותר את בעיית text[] של Supabase שמגיע כ-String ולא כמערך JSON.
         */
        public List<String> getClickableObjects() {
            if (clickableObjectsRaw == null || clickableObjectsRaw.isEmpty()) return new ArrayList<>();

            // מסיר סוגריים מרובעים/מסולסלים מתחילה וסוף
            String cleaned = clickableObjectsRaw.trim()
                    .replaceAll("^[\\[{]", "")
                    .replaceAll("[\\]}]$", "");

            List<String> result = new ArrayList<>();
            for (String part : cleaned.split(",")) {
                // מסיר רווחים וגרשיים עוטפים מכל שם קובץ
                String name = part.trim()
                        .replaceAll("^\"|\"$", "")
                        .replaceAll("^'|'$", "");
                if (!name.isEmpty()) result.add(name);
            }
            return result;
        }
    }

    /**
     * ממשק קריאת חזרה לטעינת חדר
     */
    public interface RoomCallback {
        void onSuccess(RoomData room);
        void onError(Exception e);
    }

    /**
     * שולף את כל החדרים ובוחר אחד אקראי.
     * משמש בתחילת משחק חדש (level == 1).
     */
    public void getRandomRoom(RoomCallback callback) {
        String url = SUPABASE_URL + "/rest/v1/Escape_Room?select=*"; // שליפת כל עמודות כל החדרים
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
                            // בוחר אינדקס אקראי מתוך הרשימה
                            callback.onSuccess(rooms.get(random.nextInt(rooms.size())));
                        } else { callback.onError(new Exception("No rooms found")); }
                    } else { callback.onError(new Exception("Fetch Error: " + resp.code())); }
                }
            }
        });
    }

    /**
     * שולף חדר ספציפי לפי room_id.
     * משמש לאורך המשחק כדי לטעון את אותו החדר שנבחר בתחילה.
     */
    public void getRoomById(int roomId, RoomCallback callback) {
        // eq. = "שווה ל" — מסנן לפי ID ספציפי
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
                            callback.onSuccess(rooms.get(0)); // לוקח את הראשון (יש רק אחד)
                        } else { callback.onError(new Exception("Room not found: " + roomId)); }
                    } else { callback.onError(new Exception("Fetch Error: " + resp.code())); }
                }
            }
        });
    }

    /**
     * שולף שאלות לפי room_id, מערבב אותן ומחזיר 2 אקראיות.
     * כך כל רמה מציגה שאלות שונות מהחדר הנוכחי.
     */
    public void get2RandomQuestions(int roomId, QuestionsCallback callback) {
        // מסנן שאלות לפי room_id — רק שאלות של החדר הנוכחי
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
                            Collections.shuffle(all); // מערבב את כל השאלות
                            // מחזיר רק 2 (או פחות אם יש פחות מ-2)
                            callback.onSuccess(all.subList(0, Math.min(2, all.size())));
                        } else { callback.onError(new Exception("No questions found for room " + roomId)); }
                    } else { callback.onError(new Exception("Fetch Error: " + resp.code())); }
                }
            }
        });
    }

    /**
     * שולף משימת "מצא את הפריט" אקראית מהטבלה.
     */
    public void getRandomFindItemTask(FindItemCallback callback) {
        String url = SUPABASE_URL + "/rest/v1/find_item_tasks?select=*"; // כל המשימות
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
                            // בוחר משימה אקראית מהרשימה
                            callback.onSuccess(tasks.get(random.nextInt(tasks.size())));
                        } else { callback.onError(new Exception("No FindItem tasks found")); }
                    } else { callback.onError(new Exception("Supabase Error " + resp.code())); }
                }
            }
        });
    }

    // ממשקי קריאות חזרה
    public interface QuestionsCallback { void onSuccess(List<Question> questions); void onError(Exception e); }
    public interface FindItemCallback { void onSuccess(FindItemTask task); void onError(Exception e); }
}
