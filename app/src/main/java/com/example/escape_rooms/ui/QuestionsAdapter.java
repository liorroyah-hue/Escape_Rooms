package com.example.escape_rooms.ui;

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
import java.util.HashMap;
import java.util.List;

public class QuestionsAdapter extends RecyclerView.Adapter<QuestionsAdapter.QuestionViewHolder> {

    private final Context context;
    private final List<String> questionsList;
    private final HashMap<String, ArrayList<String>> questionsToAnswers;
    private final HashMap<String, String> selectedAnswers = new HashMap<>();

    public QuestionsAdapter(Context context, List<String> questionsList, HashMap<String, ArrayList<String>> questionsToAnswers) {
        this.context = context;
        this.questionsList = questionsList;
        this.questionsToAnswers = questionsToAnswers;
    }

    @NonNull
    @Override
    public QuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_question, parent, false);
        return new QuestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestionViewHolder holder, int position) {
        String currentQuestion = questionsList.get(position);
        holder.questionText.setText(currentQuestion);

        List<String> answers = questionsToAnswers.get(currentQuestion);

        holder.answersRadioGroup.removeAllViews();

        if (answers != null && !answers.isEmpty()) {
            ColorStateList colorStateList = new ColorStateList(
                    new int[][]{
                            new int[]{-android.R.attr.state_enabled},
                            new int[]{android.R.attr.state_enabled}
                    },
                    new int[]{
                            ContextCompat.getColor(context, R.color.room_text_dim),
                            ContextCompat.getColor(context, R.color.room_accent) // Neon Cyan circle
                    }
            );

            for (String answer : answers) {
                RadioButton radioButton = new RadioButton(context);
                radioButton.setText(answer);
                radioButton.setTextColor(ContextCompat.getColor(context, R.color.room_text));
                radioButton.setTextSize(16);
                radioButton.setButtonTintList(colorStateList);
                radioButton.setPadding(32, 16, 32, 16);
                radioButton.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
                
                // Add margins between answers
                RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, 
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 8, 0, 8);
                radioButton.setLayoutParams(params);

                holder.answersRadioGroup.addView(radioButton);
            }
        }

        holder.answersRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton selectedRadioButton = group.findViewById(checkedId);
            if (selectedRadioButton != null) {
                selectedAnswers.put(currentQuestion, selectedRadioButton.getText().toString());
            }
        });
    }

    @Override
    public int getItemCount() {
        return questionsList.size();
    }

    public HashMap<String, String> getSelectedAnswers() {
        return selectedAnswers;
    }

    public static class QuestionViewHolder extends RecyclerView.ViewHolder {
        TextView questionText;
        RadioGroup answersRadioGroup;

        public QuestionViewHolder(@NonNull View itemView) {
            super(itemView);
            questionText = itemView.findViewById(R.id.question_text);
            answersRadioGroup = itemView.findViewById(R.id.answers_radio_group);
        }
    }
}
