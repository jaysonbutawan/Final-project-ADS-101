package com.example.sapacoordinator.HospitalComponents.TimeSlotsComponents;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sapacoordinator.R;

import java.util.List;

public class DateAdapter extends RecyclerView.Adapter<DateAdapter.ViewHolder> {

    private final List<DateSlot> dateList;
    private OnDateClickListener listener;

    // Constructor
    public DateAdapter(List<DateSlot> dateList, OnDateClickListener listener) {
        this.dateList = dateList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_date_card, parent, false);
        return new ViewHolder(view);
    }
    private int selectedPosition = RecyclerView.NO_POSITION;

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DateSlot dateSlot = dateList.get(position);

        if (dateSlot != null && dateSlot.getSlotDate() != null) {
            Log.d("DEBUG", "Binding date at position " + position + ": " + dateSlot.getSlotDate());

            holder.tvDate.setText(dateSlot.getSlotDate());
        } else {
            holder.tvDate.setText("No Date"); // fallback
        }
        holder.itemView.setBackgroundColor(
                position == selectedPosition ? Color.LTGRAY : Color.TRANSPARENT
        );

        holder.itemView.setOnClickListener(v -> {
            Log.d("DEBUG", "Clicked on date ID: " + dateSlot.getSlotDateId() + " at position " + position);

            if (listener != null) {
                assert dateSlot != null;
                listener.onDateSelected(dateSlot.getSlotDateId());
            }
            notifyItemChanged(selectedPosition);
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(selectedPosition);
        });

        // 👇 Auto select first date when list loads
        if (position == 0 && selectedPosition == RecyclerView.NO_POSITION) {
            holder.itemView.performClick();
        }
    }
    public void setDefaultSelection() {
        if (!dateList.isEmpty()) {
            int firstDateId = dateList.get(0).getSlotDateId(); // assuming getter
            selectedPosition = 0;
            notifyDataSetChanged();
            if (listener != null) {
                listener.onDateSelected(firstDateId);
            }
        }
    }



    @Override
    public int getItemCount() {
        return dateList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvDate);
        }
    }

    // Click listener interface
    public interface OnDateClickListener {
        void onDateSelected(int dateId);
    }
}
