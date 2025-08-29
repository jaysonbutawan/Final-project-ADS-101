package com.example.sapacoordinator.ViewBookingComponents;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sapacoordinator.HospitalComponents.Hospital;
import com.example.sapacoordinator.R;
import com.example.sapacoordinator.ViewBookingComponents.ViewBooking.ViewBookingActivity;

import java.util.List;

public class ChooseActionBookingAdapter extends RecyclerView.Adapter<ChooseActionBookingAdapter.ViewHolder> {
    private final List<Hospital> hospitals;
    private final Context context;
    private final int schoolId;

    @SuppressLint("NotifyDataSetChanged")
    public ChooseActionBookingAdapter(List<Hospital> hospitals, Context context, int schoolId) {
        this.hospitals = hospitals;
        this.context = context;
        this.schoolId = schoolId;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChooseActionBookingAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.hospital_view_booking_card, parent, false);
        return new ChooseActionBookingAdapter.ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ChooseActionBookingAdapter.ViewHolder holder, int position) {
        Hospital hospital = hospitals.get(position);

        // ✅ Enhanced debugging: Log hospital data at binding time
        Log.d("BookingAdapter", "onBindViewHolder - Position: " + position);
        Log.d("BookingAdapter", "Hospital ID: " + hospital.getHospitalId());
        Log.d("BookingAdapter", "Hospital Name: " + hospital.getHospitalName());
        Log.d("BookingAdapter", "Hospital Address: " + hospital.getHospitalAddress());

        // ✅ Validate hospital ID before setting data
        if (hospital.getHospitalId() <= 0) {
            Log.e("BookingAdapter", "⚠️ CRITICAL: Invalid hospital ID (" + hospital.getHospitalId() +
                    ") at position " + position + " for hospital: " + hospital.getHospitalName());
        }

        holder.tvHospitalName.setText(hospital.getHospitalName());
        holder.tvAddress.setText(hospital.getHospitalAddress());
        holder.tvPhone.setText(hospital.getContactInfo());

        holder.itemView.setOnClickListener(v -> {
            // ✅ Enhanced debugging: Log click event details
            Log.d("BookingAdapter", "=== BOOKING HOSPITAL CLICK EVENT ===");
            Log.d("BookingAdapter", "Clicked position: " + position);
            Log.d("BookingAdapter", "Clicked hospital ID: " + hospital.getHospitalId());
            Log.d("BookingAdapter", "Clicked hospital name: " + hospital.getHospitalName());
            Log.d("BookingAdapter", "School ID being passed: " + schoolId);
            Log.d("BookingAdapter", "Total hospitals in list: " + hospitals.size());

            // ✅ Double-check the hospital object integrity
            Hospital clickedHospital = hospitals.get(position);
            Log.d("BookingAdapter", "Double-check clicked hospital ID: " + clickedHospital.getHospitalId());

            if (clickedHospital.getHospitalId() != hospital.getHospitalId()) {
                Log.e("BookingAdapter", "🚨 MISMATCH DETECTED! Original ID: " + hospital.getHospitalId() +
                        ", Retrieved ID: " + clickedHospital.getHospitalId());
            }

            Intent intent = new Intent(context, ViewBookingActivity.class);
            intent.putExtra("hospital_id", hospital.getHospitalId());
            intent.putExtra("school_id", schoolId);

            Log.d("BookingAdapter", "Intent extras set:");
            Log.d("BookingAdapter", "  hospital_id: " + hospital.getHospitalId());
            Log.d("BookingAdapter", "  school_id: " + schoolId);
            Log.d("BookingAdapter", "Starting ViewBookingActivity...");

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return hospitals.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvHospitalName, tvAddress, tvPhone;

        public ViewHolder(View itemView) {
            super(itemView);
            tvHospitalName = itemView.findViewById(R.id.tvHospitalName);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvPhone = itemView.findViewById(R.id.tvContact);
        }
    }
}
