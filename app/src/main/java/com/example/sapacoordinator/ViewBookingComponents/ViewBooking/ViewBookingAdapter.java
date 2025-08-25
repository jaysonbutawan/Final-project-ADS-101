package com.example.sapacoordinator.ViewBookingComponents.ViewBooking;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sapacoordinator.R;

import java.util.ArrayList;
import java.util.List;

public class ViewBookingAdapter extends RecyclerView.Adapter<ViewBookingAdapter.ViewHolder> {
    private final List<ViewBookingModel> bookingList;
    private final Context context;
    private boolean isSelectionMode = false;
    private final List<Integer> selectedItems = new ArrayList<>();
    private OnSelectionModeListener selectionModeListener;

    public interface OnSelectionModeListener {
        void onSelectionModeEntered();
        void onSelectionModeExited();
        void onSelectionChanged(int selectedCount);
        void onCancelSelected(List<ViewBookingModel> selectedBookings);
    }

    public void setOnSelectionModeListener(OnSelectionModeListener listener) {
        this.selectionModeListener = listener;
    }

    public boolean isSelectionMode() {
        return isSelectionMode;
    }

    // Method to enter selection mode
    public void enterSelectionMode(int position) {
        isSelectionMode = true;
        selectedItems.clear();
        selectedItems.add(position);
        notifyDataSetChanged();
        if (selectionModeListener != null) {
            selectionModeListener.onSelectionModeEntered();
            selectionModeListener.onSelectionChanged(selectedItems.size());
        }
    }

    // Method to exit selection mode
    public void exitSelectionMode() {
        isSelectionMode = false;
        selectedItems.clear();
        notifyDataSetChanged();
        if (selectionModeListener != null) {
            selectionModeListener.onSelectionModeExited();
        }
    }

    // Method to get selected bookings
    public List<ViewBookingModel> getSelectedBookings() {
        List<ViewBookingModel> selected = new ArrayList<>();
        for (Integer position : selectedItems) {
            if (position < bookingList.size()) {
                selected.add(bookingList.get(position));
            }
        }
        return selected;
    }

    public int getSelectedCount() {
        return selectedItems.size();
    }

    public ViewBookingAdapter(List<ViewBookingModel> bookingList, Context context, int schoolId) {
        this.bookingList = bookingList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_booking_card, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ViewBookingModel booking = bookingList.get(position);

        holder.tvStudentName.setText(booking.getStudentName());
        holder.tvStudentCode.setText("ID: " + booking.getStudentCode());
        holder.tvDepartment.setText(booking.getDepartment());
        holder.tvBookingDate.setText(booking.getSlotDate());
        holder.tvTimeSlot.setText(booking.getTimeSlot());
        holder.tvStatus.setText("Status: " + booking.getAppointmentStatus());

        // Set checkbox visibility based on selection mode
        holder.cbSelect.setVisibility(isSelectionMode ? View.VISIBLE : View.GONE);

        // Set checkbox state based on selection
        boolean isSelected = selectedItems.contains(position);
        holder.cbSelect.setChecked(isSelected);

        // Update background based on selection state
        if (isSelectionMode && isSelected) {
            holder.containerLayout.setBackgroundResource(R.drawable.item_selected_background);
        } else {
            holder.containerLayout.setBackgroundResource(R.drawable.item_normal_background);
        }

        // Add alpha effect for better visual feedback
        if (isSelectionMode && isSelected) {
            holder.itemView.setAlpha(0.8f);
        } else {
            holder.itemView.setAlpha(1.0f);
        }

        // Handle checkbox clicks
        holder.cbSelect.setOnClickListener(v -> {
            if (holder.cbSelect.isChecked()) {
                if (!selectedItems.contains(position)) {
                    selectedItems.add(position);
                }
            } else {
                selectedItems.remove(Integer.valueOf(position));
            }

            // Update visual state immediately
            notifyItemChanged(position);

            if (selectionModeListener != null) {
                selectionModeListener.onSelectionChanged(selectedItems.size());
            }

            // Exit selection mode if no items selected
            if (selectedItems.isEmpty()) {
                exitSelectionMode();
            }
        });

        // Handle item clicks
        holder.itemView.setOnClickListener(v -> {
            if (isSelectionMode) {
                // Toggle selection in selection mode
                if (selectedItems.contains(position)) {
                    selectedItems.remove(Integer.valueOf(position));
                } else {
                    selectedItems.add(position);
                }
                notifyItemChanged(position);

                if (selectionModeListener != null) {
                    selectionModeListener.onSelectionChanged(selectedItems.size());
                }

                // Exit selection mode if no items selected
                if (selectedItems.isEmpty()) {
                    exitSelectionMode();
                }
            }
        });

        // Handle long press to enter selection mode
        holder.itemView.setOnLongClickListener(v -> {
            if (!isSelectionMode) {
                enterSelectionMode(position);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStudentName, tvStudentCode, tvDepartment, tvBookingDate, tvTimeSlot, tvStatus;
        CheckBox cbSelect;
        View containerLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvStudentCode = itemView.findViewById(R.id.tvStudentCode);
            tvDepartment = itemView.findViewById(R.id.tvDepartment);
            tvBookingDate = itemView.findViewById(R.id.tvBookingDate);
            tvTimeSlot = itemView.findViewById(R.id.tvTimeSlot);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            cbSelect = itemView.findViewById(R.id.cbSelect);
            containerLayout = itemView.findViewById(R.id.containerLayout);
        }
    }
}
