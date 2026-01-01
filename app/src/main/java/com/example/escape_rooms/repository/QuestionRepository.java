package com.example.escape_rooms.repository;

import android.content.Context;
import com.example.escape_rooms.model.Questions;

public class QuestionRepository {
    private final Context context;

    public QuestionRepository(Context context) {
        this.context = context.getApplicationContext();
    }

    public Questions getQuestionsForLevel(int level) {
        return new Questions(context, level);
    }
}
