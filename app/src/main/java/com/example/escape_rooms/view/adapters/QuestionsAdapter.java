package com.example.escape_rooms.view.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.escape_rooms.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Adapter לתצוגת שאלות ב-RecyclerView.
 * מציג כל שאלה עם אפשרויות תשובה (RadioButtons) בסדר אקראי.
 */
public class QuestionsAdapter extends RecyclerView.Adapter<QuestionsAdapter.QuestionViewHolder> {

    private final Context context;
    private final List<String> questionsList;                             // רשימת השאלות
    private final HashMap<String, ArrayList<String>> questionsToAnswers;  // אפשרויות לכל שאלה
    private final HashMap<String, String> selectedAnswers = new HashMap<>(); // תשובות שנבחרו

    public QuestionsAdapter(Context context, List<String> questionsList,
                            HashMap<String, ArrayList<String>> questionsToAnswers) {
        this.context = context;
        this.questionsList = questionsList;
        this.questionsToAnswers = questionsToAnswers;
    }

    @NonNull
    @Override
    public QuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // יוצר View חדש לכל שאלה מה-layout
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_question, parent, false);
        return new QuestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestionViewHolder holder, int position) {
        String currentQuestion = questionsList.get(position); // השאלה הנוכחית
        holder.questionText.setText(currentQuestion); // מציג טקסט השאלה

        List<String> answers = questionsToAnswers.get(currentQuestion); // אפשרויות התשובה

        holder.answersRadioGroup.removeAllViews(); // מנקה RadioButtons קודמים

        if (answers != null && !answers.isEmpty()) {
            // מערבב את סדר האפשרויות — כך התשובה הנכונה לא תמיד באותו מקום
            List<String> shuffledAnswers = new ArrayList<>(answers);
            Collections.shuffle(shuffledAnswers);

            // צבעים לכפתורי הרדיו — ציאן כשפעיל, אפור כשלא
            ColorStateList colorStateList = new ColorStateList(
                    new int[][]{
                            new int[]{-android.R.attr.state_enabled}, // לא פעיל
                            new int[]{android.R.attr.state_enabled}   // פעיל
                    },
                    new int[]{
                            ContextCompat.getColor(context, R.color.room_text_dim), // אפור
                            ContextCompat.getColor(context, R.color.room_accent)    // ציאן
                    }
            );

            // יוצר RadioButton לכל אפשרות תשובה
            for (String answer : shuffledAnswers) {
                RadioButton radioButton = new RadioButton(context);
                radioButton.setText(answer);
                radioButton.setTextColor(ContextCompat.getColor(context, R.color.room_text));
                radioButton.setTextSize(16);
                radioButton.setButtonTintList(colorStateList); // צבע עיגול הרדיו
                radioButton.setPadding(32, 16, 32, 16);
                radioButton.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START); // ימין לשמאל לעברית

                // רווח בין כפתורי הרדיו
                RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 8, 0, 8);
                radioButton.setLayoutParams(params);

                holder.answersRadioGroup.addView(radioButton);
            }
        }

        // כשהשחקן בוחר תשובה — שומר ב-selectedAnswers
        holder.answersRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton selectedRadioButton = group.findViewById(checkedId);
            if (selectedRadioButton != null) {
                // מפתח: השאלה, ערך: התשובה שנבחרה
                selectedAnswers.put(currentQuestion, selectedRadioButton.getText().toString());
            }
        });
    }

    @Override
    public int getItemCount() {
        return questionsList.size(); // מספר השאלות
    }

    /**
     * מחזיר את כל התשובות שנבחרו — נשלח ל-ViewModel לבדיקה.
     */
    public HashMap<String, String> getSelectedAnswers() {
        return selectedAnswers;
    }

    /**
     * ViewHolder — מחזיק הפניות לאלמנטי ה-UI של כל שאלה.
     */
    public static class QuestionViewHolder extends RecyclerView.ViewHolder {
        TextView questionText;        // טקסט השאלה
        RadioGroup answersRadioGroup; // קבוצת כפתורי הרדיו

        public QuestionViewHolder(@NonNull View itemView) {
            super(itemView);
            questionText = itemView.findViewById(R.id.question_text);
            answersRadioGroup = itemView.findViewById(R.id.answers_radio_group);
        }
    }
}
