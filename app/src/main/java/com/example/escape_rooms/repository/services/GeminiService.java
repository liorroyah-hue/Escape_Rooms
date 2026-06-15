package com.example.escape_rooms.repository.services;

import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.example.escape_rooms.BuildConfig;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * שירות לתקשורת עם Gemini AI של Google.
 * אחראי על יצירת שאלות לחדר הבריחה לפי נושא שנבחר.
 */
public class GeminiService {
    private static final String TAG = "GeminiService";

    // מפתח ה-API של Gemini
    private static final String GeminiApiKey = "AIzaSyCl8gWUJBNplra6FZ85ZC8G3fOj1QLmEC0";
    private static final String API_KEY = GeminiApiKey;

    // סוג התוכן שנשלח לשרת
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    // לקוח HTTP עם timeout ארוך — Gemini יכול לקחת זמן לייצר שאלות
    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)  // 60 שניות להתחברות
            .readTimeout(60, TimeUnit.SECONDS)     // 60 שניות לקריאת תגובה
            .writeTimeout(60, TimeUnit.SECONDS)    // 60 שניות לשליחת בקשה
            .build();

    private final Gson gson = new Gson(); // לסידור JSON

    /**
     * יוצר שאלות ותשובות לפי נושא.
     * מנסה מודלים שונים של Gemini אם הראשון לא זמין.
     *
     * @param subject           נושא השאלות (גיאוגרפיה, היסטוריה, וכו')
     * @param numberOfQuestions כמות השאלות לייצר (10 = 2 לכל אחת מ-5 הרמות)
     * @return JSON string עם השאלות, התשובות, והתשובות הנכונות
     */
    public String generateQandA(String subject, int numberOfQuestions) throws IOException {
        String prompt = buildPrompt(subject, numberOfQuestions); // בונה את ההנחיה ל-AI

        try {
            // ניסיון ראשון — המודל המהיר ביותר
            return callGeminiApi("v1beta", "gemini-3.0-flash", prompt);
        } catch (IOException e) {
            if (e.getMessage() != null && e.getMessage().contains("404")) {
                // המודל לא נמצא — מנסה גרסה ישנה יותר
                Log.w(TAG, "gemma-3-12b not found, falling back to gemini-2.5-flash");
                try {
                    return callGeminiApi("v1", "gemini-2.5-flash", prompt);
                } catch (IOException e2) {
                    // גם זה נכשל — מנסה גרסה נוספת
                    Log.w(TAG, "gemini-2.5-flash failed, falling back to gemini-2.0-flash");
                    return callGeminiApi("v1beta", "gemini-2.0-flash", prompt);
                }
            }
            throw e; // שגיאה שאינה 404 — זורק הלאה
        }
    }

    /**
     * שולח בקשה ל-Gemini API ומחזיר את הטקסט שנוצר.
     *
     * @param version גרסת ה-API (v1 או v1beta)
     * @param model   שם המודל (gemini-3.0-flash וכו')
     * @param prompt  ההנחיה לשליחה ל-AI
     */
    private String callGeminiApi(String version, String model, String prompt) throws IOException {
        // בניית URL דינמי לפי גרסה ומודל
        String url = String.format(
                "https://generativelanguage.googleapis.com/%s/models/%s:generateContent?key=%s",
                version, model, API_KEY);

        // בניית מבנה JSON של הבקשה שה-Gemini API מצפה לו
        /*
         * בניית מבנה ה-JSON שה-Gemini API מצפה לקבל.
         * Gemini תוכנן לתמוך בשיחות מורכבות (מספר הודעות, טקסט + תמונות),
         * אז המבנה מקונן — אפילו כשיש לנו רק הודעה אחת עם טקסט אחד.
         *
         * המבנה הסופי שנשלח לשרת נראה כך:
         *
         * {
         *   "contents": [                        ← מערך של הודעות (כאן רק אחת)
         *     {
         *       "parts": [                       ← מערך חלקים (טקסט, תמונה וכו') — כאן רק אחד
         *         {
         *           "text": "אתה מעצב חדרי בריחה..."  ← ההנחיה עצמה
         *         }
         *       ]
         *     }
         *   ]
         * }
         *
         * שכבה 1 — textPart: האובייקט הפנימי ביותר — מכיל את הטקסט עצמו:
         *   { "text": "אתה מעצב חדרי בריחה..." }
         *
         * שכבה 2 — parts: מערך החלקים. Gemini תוכנן לקבל גם תמונות וגם טקסט
         *   באותה הודעה, לכן מצפה למערך. כאן רק חלק אחד — הטקסט:
         *   [ { "text": "..." } ]
         *
         * שכבה 3 — content: עוטף את parts בתוך אובייקט הודעה אחת:
         *   { "parts": [ { "text": "..." } ] }
         *
         * שכבה 4 — contents: מערך ההודעות. Gemini תוכנן לשיחות עם היסטוריה
         *   (שאלה→תשובה→שאלה...). כאן רק הודעה אחת:
         *   [ { "parts": [ { "text": "..." } ] } ]
         *
         * שכבה 5 — root: אובייקט השורש הסופי שנשלח ל-API:
         *   { "contents": [ { "parts": [ { "text": "..." } ] } ] }
         */

        // שכבה 1 — הטקסט עצמו
        JsonObject textPart = new JsonObject();
        textPart.addProperty("text", prompt);

        // שכבה 2 — מערך חלקים (כאן רק טקסט אחד, אבל Gemini תוכנן גם לתמונות)
        JsonArray parts = new JsonArray();
        parts.add(textPart);

        // שכבה 3 — אובייקט הודעה אחת שעוטף את החלקים
        JsonObject content = new JsonObject();
        content.add("parts", parts);

        // שכבה 4 — מערך הודעות (כאן רק אחת, אבל Gemini תוכנן לשיחות ארוכות)
        JsonArray contents = new JsonArray();
        contents.add(content);

        // שכבה 5 — אובייקט השורש הסופי שנשלח לשרת
        JsonObject root = new JsonObject();
        root.add("contents", contents);

        // יוצר את גוף הבקשה מה-JSON
        RequestBody body = RequestBody.create(gson.toJson(root), JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body) // POST עם גוף הבקשה
                .build();

        // שולח הבקשה ומטפל בתגובה — try-with-resources סוגר את ה-Response אוטומטית
        try (Response response = client.newCall(request).execute()) {
            String responseString = response.body() != null ? response.body().string() : "";

            if (!response.isSuccessful()) {
                int code = response.code();
                Log.e(TAG, "Gemini API " + version + "/" + model + " failed: " + code);
                Log.e(TAG, "Full error response: " + responseString);

                if (code == 429) {
                    // מכסת הבקשות הסתיימה — מציג הודעה ידידותית
                    throw new IOException("מכסת הבקשות של Gemini AI הסתיימה (429). אנא נסו שוב בעוד דקה.");
                }
                if (code == 404) {
                    // המודל לא נמצא — הקורא ינסה מודל אחר
                    throw new IOException("Gemini API error 404");
                }
                throw new IOException("Gemini API error " + code);
            }

            try {
                // פרסור התגובה — מחלץ את הטקסט מהמבנה הבא:
                // response → candidates[0] → content → parts[0] → text
                JsonObject jsonResponse = JsonParser.parseString(responseString).getAsJsonObject();
                return jsonResponse.getAsJsonArray("candidates")
                        .get(0).getAsJsonObject()   // המועמד הראשון (ה-AI מחזיר לפחות אחד)
                        .getAsJsonObject("content")
                        .getAsJsonArray("parts")
                        .get(0).getAsJsonObject()   // החלק הראשון של התגובה
                        .get("text").getAsString();  // הטקסט עצמו
            } catch (Exception e) {
                Log.e(TAG, "Failed to parse successful Gemini response: " + responseString, e);
                throw new IOException("שגיאה בפענוח תשובת ה-AI");
            }
        }
    }

    /**
     * בונה את ההנחיה (prompt) לשליחה ל-Gemini.
     * מגדיר את הפורמט המדויק שה-AI צריך להחזיר.
     *
     * @param subject           נושא השאלות
     * @param numberOfQuestions כמה שאלות לייצר
     */
    private String buildPrompt(String subject, int numberOfQuestions) {
        return String.format(
            // מסביר ל-AI מה תפקידו
            "אתה מעצב חדרי בריחה יצירתי. המשימה שלך היא לייצר סדרה של חידות לחדר בריחה דיגיטלי בנושא נתון. כל התוכן (חידות, תשובות אפשריות, ותשובות נכונות) חייב להיות בשפה העברית.\n" +
            // מגדיר את פורמט הפלט — JSON עם 3 מפתחות
            "הפלט חייב להיות אובייקט JSON תקין המכיל שלושה מפתחות: `questions`, `answers`, ו-`correctAnswers`.\n\n" +
            // הסבר על כל מפתח
            "- `questions`: מערך של מחרוזות, כאשר כל מחרוזת היא חידה או שאלה.\n" +
            "- `answers`: מערך של מערכי מחרוזות. כל מערך פנימי חייב להכיל בדיוק ארבע תשובות אפשריות עבור החידה התואמת.\n" +
            "- `correctAnswers`: מערך של מחרוזות, כאשר כל מחרוזת היא התשובה הנכונה לחידה התואמת. התשובה הנכונה חייבת להופיע גם בין האפשרויות במערך ה-`answers` התואם.\n\n" +
            // הנתונים המשתנים
            "נושא החדר: %s\n" +
            "מספר חידות: %d\n\n" +
            // הדגשה שהפלט צריך להיות JSON בלבד — ללא טקסט מסביב
            "צור את ה-JSON כעת. התגובה כולה צריכה להיות אובייקט ה-JSON בלבד, ללא טקסט או הסברים נוספים.",
            subject, numberOfQuestions
        );
    }
}
