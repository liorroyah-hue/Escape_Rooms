package com.example.escape_rooms.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.escape_rooms.R;
import com.example.escape_rooms.repository.GameRepository;

import java.util.List;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {

    private final List<GameRepository.GameResult> results;

    public LeaderboardAdapter(List<GameRepository.GameResult> results) {
        this.results = results;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_leaderboard_entry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GameRepository.GameResult result = results.get(position);
        holder.tvRank.setText("#" + (position + 1));
        holder.tvUsername.setText(result.username != null ? result.username.toUpperCase() : "UNKNOWN_OPERATIVE");
        holder.tvTime.setText(formatTime(result.total_time_ms));
    }

    @Override
    public int getItemCount() {
        return results.size();
    }

    private String formatTime(long millis) {
        int seconds = (int) (millis / 1000) % 60;
        int minutes = (int) ((millis / (1000 * 60)) % 60);
        return String.format("%02d:%02d", minutes, seconds);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvUsername, tvTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tv_rank);
            tvUsername = itemView.findViewById(R.id.tv_username);
            tvTime = itemView.findViewById(R.id.tv_time);
        }
    }
}
