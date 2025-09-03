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
            holder.tvHospitalName.setText(hospital.getHospitalName());
            holder.tvAddress.setText(hospital.getHospitalAddress());
            holder.tvPhone.setText(hospital.getContactInfo());
            holder.itemView.setOnClickListener(v -> {

                Intent intent = new Intent(context, ViewBookingActivity.class);
                intent.putExtra("hospital_id", hospital.getHospitalId());
                intent.putExtra("school_id", schoolId);
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
