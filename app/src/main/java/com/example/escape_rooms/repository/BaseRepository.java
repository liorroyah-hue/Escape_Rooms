package com.example.escape_rooms.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.OkHttpClient;

/**
 * מחלקת בסיס שממנה כל ה-Repositories יורשים.
 * מגדירה את כל הכלים המשותפים לתקשורת עם Supabase.
 */
public abstract class BaseRepository {

    // כתובת השרת של Supabase — כל בקשת HTTP מתחילה מכאן
    public static final String SUPABASE_URL = "https://wjwbshqrvbgdtqanztqz.supabase.co";

    // מפתח ה-API שמאמת גישה ל-Supabase (anon key)
    public static final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Indqd2JzaHFydmJnZHRxYW56dHF6Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjMyOTA1MzgsImV4cCI6MjA3ODg2NjUzOH0.2LL7iEfi0gv_JAaR2984X2ybn4LclXGSTwR-9runIhM";

    // הלקוח שמבצע את כל בקשות ה-HTTP לשרת
    protected final OkHttpClient client;

    // ממיר בין JSON לאובייקטי Java ובחזרה
    protected final Gson gson;

    public BaseRepository() {
        // יוצר לקוח HTTP חדש
        this.client = new OkHttpClient();
        // יוצר Gson רגיל — גם ערכי null ישלחו אם יש
        this.gson = new GsonBuilder().create();
    }

    /**
     * ממשק גנרי לקריאות חזרה (Callback) מהשרת.
     * T = סוג הנתונים שמוחזרים בהצלחה
     */
    public interface RepositoryCallback<T> {
        void onSuccess(T result); // נקרא כשהפעולה הצליחה
        void onError(Exception e); // נקרא כשהייתה שגיאה
    }
}
