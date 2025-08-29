package com.example.sapacoordinator.HospitalComponents.DepartmentComponents;

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

import com.example.sapacoordinator.HospitalComponents.TimeSlotsComponents.DateTimeSlotSelectionActivity;
import com.example.sapacoordinator.R;

import java.util.List;

public class DepartmentAdapter extends RecyclerView.Adapter<DepartmentAdapter.ViewHolder> {

    private final List<Department> departmentList;
    private final Context context;
    private final int schoolId;
    private final int hospitalId; // ✅ Add hospital_id field

    @SuppressLint("NotifyDataSetChanged")
    public DepartmentAdapter(List<Department> departmentList, Context context, int schoolId, int hospitalId) {
        this.departmentList = departmentList;
        this.context = context;
        this.schoolId = schoolId;
        this.hospitalId = hospitalId; // ✅ Store hospital_id correctly

        // ✅ Enhanced debug logging
        Log.d("DepartmentAdapter", "=== ADAPTER CREATED ===");
        Log.d("DepartmentAdapter", "School ID: " + schoolId);
        Log.d("DepartmentAdapter", "Hospital ID: " + hospitalId);

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DepartmentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_department_card, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull DepartmentAdapter.ViewHolder holder, int position) {
        Department department = departmentList.get(position);
        holder.tvDepartmentName.setText(department.getSection_name());
        holder.tvPrice.setText(String.valueOf(department.getPrice_per_student()));
        holder.tvHospitalName.setText(department.getHospital_name());

        holder.itemView.setOnClickListener(v -> {
            // ✅ Enhanced debugging before intent creation
            Log.d("DepartmentAdapter", "=== DEPARTMENT CLICK EVENT ===");
            Log.d("DepartmentAdapter", "Department clicked: " + department.getSection_name());
            Log.d("DepartmentAdapter", "Department ID: " + department.getDepartment_id());
            Log.d("DepartmentAdapter", "Hospital ID (from adapter): " + hospitalId);
            Log.d("DepartmentAdapter", "School ID: " + schoolId);

            Intent intent = new Intent(context, DateTimeSlotSelectionActivity.class);
            // ✅ FIXED: Pass the correct hospital_id instead of department_id
            intent.putExtra("hospital_id", hospitalId); // ✅ CORRECT: Use hospitalId from adapter
            intent.putExtra("hospital_name", department.getHospital_name());
            intent.putExtra("department_id", department.getDepartment_id());
            intent.putExtra("school_id", schoolId);

            // ✅ Log the corrected intent extras
            Log.d("DepartmentAdapter", "Intent extras (CORRECTED):");
            Log.d("DepartmentAdapter", "  hospital_id: " + hospitalId + " ✅ FIXED!");
            Log.d("DepartmentAdapter", "  department_id: " + department.getDepartment_id());
            Log.d("DepartmentAdapter", "  school_id: " + schoolId);
            Log.d("DepartmentAdapter", "Starting DateTimeSlotSelectionActivity...");

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return departmentList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDepartmentName,tvPrice, tvHospitalName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDepartmentName = itemView.findViewById(R.id.tvSectionName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvHospitalName = itemView.findViewById(R.id.tvHospitalName);
        }
    }
}
