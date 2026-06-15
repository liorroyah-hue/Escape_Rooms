package com.example.escape_rooms.view.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.escape_rooms.R;
import com.example.escape_rooms.repository.GameRepository;

import java.util.List;

/**
 * Adapter לתצוגת לוח התוצאות ב-RecyclerView.
 * מציג מקום, שם שחקן, וזמן לכל שורה.
 */
public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {

    private final List<GameRepository.GameResult> results; // רשימת תוצאות (מגיעה מ-GameRepository)

    public LeaderboardAdapter(List<GameRepository.GameResult> results) {
        this.results = results;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // יוצר View לכל שורה בלוח
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_leaderboard_entry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GameRepository.GameResult result = results.get(position);

        holder.tvRank.setText("#" + (position + 1)); // מיקום: #1, #2, #3...

        // שם שחקן — אותיות גדולות, "UNKNOWN_OPERATIVE" אם חסר
        holder.tvUsername.setText(result.username != null
                ? result.username.toUpperCase()
                : "UNKNOWN_OPERATIVE");

        holder.tvTime.setText(formatTime(result.total_time_ms)); // זמן בפורמט MM:SS
    }

    @Override
    public int getItemCount() {
        return results.size(); // מספר השורות בלוח
    }

    /**
     * ממיר מילישניות לפורמט MM:SS
     */
    private String formatTime(long millis) {
        int seconds = (int) (millis / 1000) % 60;
        int minutes = (int) ((millis / (1000 * 60)) % 60);
        return String.format("%02d:%02d", minutes, seconds);
    }

    /**
     * ViewHolder — מחזיק הפניות לאלמנטי UI של כל שורה בלוח.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank;     // מיקום (#1, #2...)
        TextView tvUsername; // שם שחקן
        TextView tvTime;     // זמן

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tv_rank);
            tvUsername = itemView.findViewById(R.id.tv_username);
            tvTime = itemView.findViewById(R.id.tv_time);
        }
    }
}
