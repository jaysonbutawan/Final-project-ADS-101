package com.example.sapacoordinator.HospitalComponents.TimeSlotsComponents;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sapacoordinator.R;
import java.util.ArrayList;
import java.util.List;

public class TimeSlotAdapter extends RecyclerView.Adapter<TimeSlotAdapter.ViewHolder> {
    private final List<TimeSlotModel> timeSlotList;
    private final OnTimeSlotClickListener listener;
    private int selectedPosition = RecyclerView.NO_POSITION; // 🔥 track selection

    public TimeSlotAdapter(List<TimeSlotModel> timeSlotList, OnTimeSlotClickListener listener) {
        this.timeSlotList = (timeSlotList != null) ? timeSlotList : new ArrayList<>();
        this.listener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<TimeSlotModel> newSlots) {
        timeSlotList.clear();
        if (newSlots != null && !newSlots.isEmpty()) {
            timeSlotList.addAll(newSlots);
        }
        selectedPosition = RecyclerView.NO_POSITION; // 🔥 reset selection when new data loads
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_time_slot, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        TimeSlotModel timeSlot = timeSlotList.get(position);

        // Format time range
        String timeRange = timeSlot.getStart_time() + " - " + timeSlot.getEnd_time();
        holder.tvTime.setText(timeRange);

        // Show capacity if available
        if (timeSlot.getCapacity() > 0) {
            holder.tvCapacity.setVisibility(View.VISIBLE);
            holder.tvCapacity.setText("Slot Available " + timeSlot.getBooked_count()+ "/" + timeSlot.getCapacity());
        } else {
            holder.tvCapacity.setVisibility(View.GONE);
        }

        // 🔥 Highlight if selected
        if (selectedPosition == position) {
            holder.itemView.setBackgroundColor(Color.parseColor("#FFDD55")); // yellow highlight
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT);
        }

        // Click listener
        holder.itemView.setOnClickListener(v -> {
            int previousPosition = selectedPosition;
            selectedPosition = position;

            // Refresh both old and new selected items
            notifyItemChanged(previousPosition);
            notifyItemChanged(selectedPosition);

            if (listener != null) {
                listener.onTimeSlotClick(timeSlot.getTime_slot_id());
            }
        });
    }

    @Override
    public int getItemCount() {
        return timeSlotList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime, tvCapacity;

        public ViewHolder(View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tvTimeSlot);
            tvCapacity = itemView.findViewById(R.id.tvCapacity);
        }
    }

    public interface OnTimeSlotClickListener {
        void onTimeSlotClick(int timeSlotId);
    }
}
