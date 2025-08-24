package com.example.sapacoordinator.ViewBooking;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sapacoordinator.R;

import java.util.List;

public class ViewBookingAdapter extends RecyclerView.Adapter<ViewBookingAdapter.ViewHolder> {
    private final List<ViewBookingModel> bookingList;
    private final Context context;
    private final int schoolId;


    public ViewBookingAdapter(List<ViewBookingModel> bookingList, Context context, int schoolId) {
        this.bookingList = bookingList;
        this.context = context;
        this.schoolId = schoolId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_booking_card, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ViewBookingModel booking = bookingList.get(position);

        holder.tvStudentName.setText(booking.getStudentName());
        holder.tvStudentCode.setText("ID: " + booking.getStudentCode());
        holder.tvDepartment.setText(booking.getDepartment());
        holder.tvAddedDate.setText(booking.getAddedDate());
        holder.tvTimeSlot.setText(booking.getTimeSlot());
        holder.tvStatus.setText("Status: " + booking.getAppointmentStatus());
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStudentName, tvStudentCode, tvDepartment, tvAddedDate, tvTimeSlot, tvStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvStudentCode = itemView.findViewById(R.id.tvStudentCode);
            tvDepartment = itemView.findViewById(R.id.tvDepartment);
            tvAddedDate = itemView.findViewById(R.id.tvAddedDate);
            tvTimeSlot = itemView.findViewById(R.id.tvTimeSlot);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}
