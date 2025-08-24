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
        holder.tvHospitalName.setText(hospital.getHospitalName());
        holder.tvAddress.setText(hospital.getHospitalAddress());
        holder.tvPhone.setText(hospital.getContactInfo());
        holder.tvDescription.setText(hospital.getDescriptions());
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DepartmentActivity.class);
            intent.putExtra("hospital_id", hospital.getHospitalId());
            intent.putExtra("hospital_name", hospital.getHospitalName());
            intent.putExtra("hospital_address", hospital.getHospitalAddress());
            intent.putExtra("school_id", schoolId); // ✅ Pass school_id to next activity
            Log.d("DEBUG_", "✅ Im going to department activity with hospital ID: " + hospital.getHospitalId());
            Log.d("DEBUG_", "✅ Im going to department activity with hospital name: " + hospital.getHospitalName());
            Log.d("DEBUG_", "✅ Passing school_id: " + schoolId); // ✅ Log school_id
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