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
    private final int schoolId; // ✅ Add school_id field

    @SuppressLint("NotifyDataSetChanged")
    public DepartmentAdapter(List<Department> departmentList, Context context, int schoolId) {
        this.departmentList = departmentList;
        this.context = context;
        this.schoolId = schoolId; // ✅ Store school_id

        // ✅ Add debug logging to track school_id in adapter constructor
        Log.d("DEBUG_", "DepartmentAdapter created with school_id: " + schoolId);

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
            Intent intent = new Intent(context, DateTimeSlotSelectionActivity.class);
            intent.putExtra("hospital_id", department.getDepartment_id());
            intent.putExtra("hospital_name", department.getHospital_name());
            intent.putExtra("department_id", department.getDepartment_id());
            intent.putExtra("school_id", schoolId); // ✅ Pass school_id to DateTimeSlotSelectionActivity

            Log.d("DEBUG_", "✅ Going to DateTimeSlotSelectionActivity");
            Log.d("DEBUG_", "✅ Hospital name: " + department.getHospital_name());
            Log.d("DEBUG_", "✅ Department ID: " + department.getDepartment_id());
            Log.d("DEBUG_", "✅ School ID: " + schoolId+ " Select date and time"); // ✅ Log school_id

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
