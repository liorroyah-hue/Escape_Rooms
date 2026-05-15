package com.example.escape_rooms.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.OkHttpClient;

/**
 * Base class for all repositories to share common network and serialization logic.
 */
public abstract class BaseRepository {
    public static final String SUPABASE_URL = "https://wjwbshqrvbgdtqanztqz.supabase.co";
    public static final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Indqd2JzaHFydmJnZHRxYW56dHF6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjMyOTA1MzgsImV4cCI6MjA3ODg2NjUzOH0.2LL7iEfi0gv_JAaR2984X2ybn4LclXGSTwR-9runIhM";

    protected final OkHttpClient client;
    protected final Gson gson;

    public BaseRepository() {
        this.client = new OkHttpClient();
        // Configuration to skip nulls during serialization (important for Supabase IDs)
        this.gson = new GsonBuilder().create();
    }

    public interface RepositoryCallback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }
}
