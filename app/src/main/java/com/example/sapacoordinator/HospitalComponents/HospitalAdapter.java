package com.example.sapacoordinator.HospitalComponents;

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

import com.example.sapacoordinator.HospitalComponents.DepartmentComponents.DepartmentActivity;
import com.example.sapacoordinator.R;

import java.util.List;

public class HospitalAdapter extends RecyclerView.Adapter<HospitalAdapter.ViewHolder> {
    private final List<Hospital> hospitals;
    private final Context context;
    private final int schoolId; // ✅ Add school_id field

    @SuppressLint("NotifyDataSetChanged")
    public HospitalAdapter(List<Hospital> hospitals, Context context, int schoolId) {
        this.hospitals = hospitals;
        this.context = context;
        this.schoolId = schoolId; // ✅ Store school_id
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_hospital_card, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Hospital hospital = hospitals.get(position);

        // ✅ Enhanced debugging: Log hospital data at binding time
        Log.d("HospitalAdapter", "onBindViewHolder - Position: " + position);
        Log.d("HospitalAdapter", "Hospital ID: " + hospital.getHospitalId());
        Log.d("HospitalAdapter", "Hospital Name: " + hospital.getHospitalName());
        Log.d("HospitalAdapter", "Hospital Address: " + hospital.getHospitalAddress());

        // ✅ Validate hospital ID before setting data
        if (hospital.getHospitalId() <= 0) {
            Log.e("HospitalAdapter", "⚠️ CRITICAL: Invalid hospital ID (" + hospital.getHospitalId() +
                    ") at position " + position + " for hospital: " + hospital.getHospitalName());
        }

        holder.tvHospitalName.setText(hospital.getHospitalName());
        holder.tvAddress.setText(hospital.getHospitalAddress());
        holder.tvPhone.setText(hospital.getContactInfo());
        holder.tvDescription.setText(hospital.getDescriptions());

        holder.itemView.setOnClickListener(v -> {
            // ✅ Enhanced debugging: Log click event details
            Log.d("HospitalAdapter", "=== HOSPITAL CLICK EVENT ===");
            Log.d("HospitalAdapter", "Clicked position: " + position);
            Log.d("HospitalAdapter", "Clicked hospital ID: " + hospital.getHospitalId());
            Log.d("HospitalAdapter", "Clicked hospital name: " + hospital.getHospitalName());
            Log.d("HospitalAdapter", "School ID being passed: " + schoolId);
            Log.d("HospitalAdapter", "Total hospitals in list: " + hospitals.size());

            // ✅ Double-check the hospital object integrity
            Hospital clickedHospital = hospitals.get(position);
            Log.d("HospitalAdapter", "Double-check clicked hospital ID: " + clickedHospital.getHospitalId());

            if (clickedHospital.getHospitalId() != hospital.getHospitalId()) {
                Log.e("HospitalAdapter", "🚨 MISMATCH DETECTED! Original ID: " + hospital.getHospitalId() +
                        ", Retrieved ID: " + clickedHospital.getHospitalId());
            }

            Intent intent = new Intent(context, DepartmentActivity.class);
            intent.putExtra("hospital_id", hospital.getHospitalId());
            intent.putExtra("hospital_name", hospital.getHospitalName());
            intent.putExtra("hospital_address", hospital.getHospitalAddress());
            intent.putExtra("school_id", schoolId);

            Log.d("HospitalAdapter", "Intent extras set:");
            Log.d("HospitalAdapter", "  hospital_id: " + hospital.getHospitalId());
            Log.d("HospitalAdapter", "  hospital_name: " + hospital.getHospitalName());
            Log.d("HospitalAdapter", "  school_id: " + schoolId);
            Log.d("HospitalAdapter", "Starting DepartmentActivity...");

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return hospitals.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvHospitalName, tvAddress, tvPhone, tvDescription;

        public ViewHolder(View itemView) {
            super(itemView);
            tvHospitalName = itemView.findViewById(R.id.tvHospitalName);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvDescription = itemView.findViewById(R.id.tvDescription);
        }
    }
}