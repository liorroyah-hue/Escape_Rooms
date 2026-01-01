package com.example.escape_rooms.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
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
            for (String answer : answers) {
                RadioButton radioButton = new RadioButton(context);
                radioButton.setText(answer);
                radioButton.setTextSize(16);
                radioButton.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
                radioButton.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
                holder.answersRadioGroup.addView(radioButton);
            }
        }

        // When an answer is selected, save it
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
