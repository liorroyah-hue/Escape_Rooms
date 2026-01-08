package com.example.escape_rooms.repository;

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

public class GeminiService {
    private static final String TAG = "GeminiService";
    private static final String API_KEY = BuildConfig.GEMINI_API_KEY;
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();
            
    private final Gson gson = new Gson();

    public String generateQandA(String subject, int numberOfQuestions) throws IOException {
        String prompt = buildPrompt(subject, numberOfQuestions);
        
        try {
            return callGeminiApi("v1beta", "gemma-3-12b", prompt);
        } catch (IOException e) {
            if (e.getMessage() != null && e.getMessage().contains("404")) {
                Log.w(TAG, "gemma-3-12b not found, falling back to gemini-2.5-flash");
                try {
                    return callGeminiApi("v1", "gemini-2.5-flash", prompt);
                } catch (IOException e2) {
                    Log.w(TAG, "gemini-2.5-flash failed, falling back to gemini-2.0-flash");
                    return callGeminiApi("v1beta", "gemini-2.0-flash", prompt);
                }
            }
            throw e;
        }
    }

    private String callGeminiApi(String version, String model, String prompt) throws IOException {
        String url = String.format("https://generativelanguage.googleapis.com/%s/models/%s:generateContent?key=%s", 
                version, model, API_KEY);

        JsonObject textPart = new JsonObject();
        textPart.addProperty("text", prompt);

        JsonArray parts = new JsonArray();
        parts.add(textPart);

        JsonObject content = new JsonObject();
        content.add("parts", parts);

        JsonArray contents = new JsonArray();
        contents.add(content);

        JsonObject root = new JsonObject();
        root.add("contents", contents);

        RequestBody body = RequestBody.create(gson.toJson(root), JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseString = response.body() != null ? response.body().string() : "";
            
            if (!response.isSuccessful()) {
                int code = response.code();
                Log.e(TAG, "Gemini API " + version + "/" + model + " failed: " + code);
                Log.e(TAG, "Full error response: " + responseString);
                
                if (code == 429) {
                    throw new IOException("מכסת הבקשות של Gemini AI הסתיימה (429). אנא נסו שוב בעוד דקה.");
                }
                if (code == 404) {
                    throw new IOException("Gemini API error 404");
                }
                throw new IOException("Gemini API error " + code);
            }

            try {
                JsonObject jsonResponse = JsonParser.parseString(responseString).getAsJsonObject();
                return jsonResponse.getAsJsonArray("candidates")
                        .get(0).getAsJsonObject()
                        .getAsJsonObject("content")
                        .getAsJsonArray("parts")
                        .get(0).getAsJsonObject()
                        .get("text").getAsString();
            } catch (Exception e) {
                Log.e(TAG, "Failed to parse successful Gemini response: " + responseString, e);
                throw new IOException("שגיאה בפענוח תשובת ה-AI");
            }
        }
    }

    private String buildPrompt(String subject, int numberOfQuestions) {
        return String.format(
            "אתה מעצב חדרי בריחה יצירתי. המשימה שלך היא לייצר סדרה של חידות לחדר בריחה דיגיטלי בנושא נתון. כל התוכן (חידות, תשובות אפשריות, ותשובות נכונות) חייב להיות בשפה העברית.\n" +
            "הפלט חייב להיות אובייקט JSON תקין המכיל שלושה מפתחות: `questions`, `answers`, ו-`correctAnswers`.\n\n" +
            "- `questions`: מערך של מחרוזות, כאשר כל מחרוזת היא חידה או שאלה.\n" +
            "- `answers`: מערך של מערכי מחרוזות. כל מערך פנימי חייב להכיל בדיוק ארבע תשובות אפשריות עבור החידה התואמת.\n" +
            "- `correctAnswers`: מערך של מחרוזות, כאשר כל מחרוזת היא התשובה הנכונה לחידה התואמת. התשובה הנכונה חייבת להופיע גם בין האפשרויות במערך ה-`answers` התואם.\n\n" +
            "נושא החדר: %s\n" +
            "מספר חידות: %d\n\n" +
            "צור את ה-JSON כעת. התגובה כולה צריכה להיות אובייקט ה-JSON בלבד, ללא טקסט או הסברים נוספים.",
            subject, numberOfQuestions
        );
    }
}
