package com.example.escape_rooms.repository;

import android.util.Log;
import androidx.annotation.NonNull;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RatingRepository extends BaseRepository {

    public interface RatingCallback {
        void onSuccess();
        void onError(Exception e);
    }

    public void saveRating(String username, float rating, RatingCallback callback) {
        String url = SUPABASE_URL + "/rest/v1/ratings";

        Map<String, Object> data = new HashMap<>();
        data.put("username", username);
        data.put("rating_value", Math.round(rating));

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
                        Log.e("RatingRepository", "Error: " + resp.code());
                        callback.onError(new Exception("Server Error: " + resp.code()));
                    }
                }
            }
        });
    }
}
