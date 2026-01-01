package com.example.escape_rooms.repository;

import android.content.Context;
import android.content.SharedPreferences;

public class RatingRepository {
    private static final String PREF_NAME = "escape_room_prefs";
    private static final String KEY_RATING = "user_rating";
    private final SharedPreferences sharedPreferences;

    public RatingRepository(Context context) {
        this.sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveRating(float rating) {
        int roundedRating = Math.max(1, Math.min(5, Math.round(rating)));
        sharedPreferences.edit().putInt(KEY_RATING, roundedRating).apply();
    }

    public int getRating() {
        return sharedPreferences.getInt(KEY_RATING, 0);
    }
}
