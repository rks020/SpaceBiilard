package com.spacebilliard.app;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class QuestAdapter extends RecyclerView.Adapter<QuestAdapter.QuestViewHolder> {

    private List<Quest> quests;

    public QuestAdapter(List<Quest> quests) {
        this.quests = quests;
    }

    @NonNull
    @Override
    public QuestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.quest_card, parent, false);
        return new QuestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestViewHolder holder, int position) {
        Quest quest = quests.get(position);

        holder.tvTitle.setText(quest.getTitle());
        holder.tvDescription.setText(quest.getDescription());
        holder.tvCoinReward.setText("💰 " + quest.getCoinReward());
        holder.tvProgress.setText(quest.getCurrentProgress() + "/" + quest.getTargetProgress());
        holder.progressBar.setProgress((int) quest.getProgressPercentage());

        // Dynamic Unique Icon Setup
        holder.tvQuestId.setText(String.valueOf(quest.getId()));

        // Define category color
        int categoryColor = Color.WHITE;
        String typeColor = "#FFFFFF";

        switch (quest.getType()) {
            case COMBAT:
                categoryColor = Color.parseColor("#FF6B6B"); // Red
                typeColor = "#FF6B6B";
                break;
            case BOSS:
                categoryColor = Color.parseColor("#FF4500"); // Orange-Red
                typeColor = "#FF4500";
                break;
            case LEVEL:
                categoryColor = Color.parseColor("#4169E1"); // Royal Blue
                typeColor = "#4169E1";
                break;
            case COLLECTION:
                categoryColor = Color.parseColor("#FFD700"); // Gold
                typeColor = "#FFD700";
                break;
            case SURVIVAL:
                categoryColor = Color.parseColor("#32CD32"); // Lime Green
                typeColor = "#32CD32";
                break;
        }

        // Apply tint to the icon background only
        holder.imgIconBg.setColorFilter(categoryColor);

        // Update progress bar tint (custom drawable might need different handling, but
        // this is safe for simplest case)
        // For LayerDrawable involving custom_progress_bar_horizontal, we rely on the
        // drawable's own colors or tint it:
        // holder.progressBar.getProgressDrawable().setColorFilter(categoryColor,
        // android.graphics.PorterDuff.Mode.SRC_IN);

        // Update description color
        holder.tvDescription.setTextColor(Color.parseColor(typeColor));

        // Grayed out if not completed
        if (quest.isCompleted()) {
            holder.cardView.setAlpha(1.0f);
            holder.tvCompleted.setVisibility(View.VISIBLE);
            holder.progressBar.setVisibility(View.GONE);
            holder.tvProgress.setVisibility(View.GONE);
            holder.tvTitle.setTextColor(Color.parseColor("#00FF00"));
            // Keep description visible but maybe dim it
            holder.tvDescription.setAlpha(0.8f);

            // Show claim button if not yet claimed
            if (!quest.isClaimed()) {
                holder.btnClaimReward.setVisibility(View.VISIBLE);
                holder.btnClaimReward.setOnClickListener(v -> {
                    // Claim reward
                    quest.setClaimed(true);

                    // Add coins
                    android.content.SharedPreferences prefs = holder.itemView.getContext()
                            .getSharedPreferences("SpaceBilliard", android.content.Context.MODE_PRIVATE);
                    int currentCoins = prefs.getInt("coins", 0);
                    prefs.edit().putInt("coins", currentCoins + quest.getCoinReward()).apply();

                    // Save claimed status
                    QuestManager.getInstance(holder.itemView.getContext()).saveQuestProgress();

                    // Hide button
                    holder.btnClaimReward.setVisibility(View.GONE);

                    // Show toast
                    android.widget.Toast.makeText(holder.itemView.getContext(),
                            "+" + quest.getCoinReward() + " coins!",
                            android.widget.Toast.LENGTH_SHORT).show();

                    // Trigger UI refresh if needed by notifying item changed
                    notifyItemChanged(position);
                });
            } else {
                holder.btnClaimReward.setVisibility(View.GONE);
            }
        } else {
            holder.cardView.setAlpha(0.5f); // Grayed out for incomplete quests
            holder.tvCompleted.setVisibility(View.GONE);
            holder.progressBar.setVisibility(View.VISIBLE);
            holder.tvProgress.setVisibility(View.VISIBLE);
            holder.tvTitle.setTextColor(Color.parseColor("#FFFFFF"));
            holder.tvDescription.setAlpha(0.7f); // Also dim description text
            holder.btnClaimReward.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return quests.size();
    }

    public void updateQuests(List<Quest> newQuests) {
        this.quests = newQuests;
        notifyDataSetChanged();
    }

    static class QuestViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvTitle, tvDescription, tvCoinReward, tvProgress, tvCompleted;
        ProgressBar progressBar;
        android.widget.Button btnClaimReward;

        // Dynamic Icon Views
        android.widget.ImageView imgIconBg;
        TextView tvQuestId;

        public QuestViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.questCard);
            tvTitle = itemView.findViewById(R.id.tvQuestTitle);
            tvDescription = itemView.findViewById(R.id.tvQuestDescription);
            tvCoinReward = itemView.findViewById(R.id.tvCoinReward);
            tvProgress = itemView.findViewById(R.id.tvProgress);
            tvCompleted = itemView.findViewById(R.id.tvCompleted);
            progressBar = itemView.findViewById(R.id.progressBar);
            btnClaimReward = itemView.findViewById(R.id.btnClaimReward);

            // Layout updated to FrameLayout container
            imgIconBg = itemView.findViewById(R.id.imgQuestIconBg);
            tvQuestId = itemView.findViewById(R.id.tvQuestId);
        }
    }
}
