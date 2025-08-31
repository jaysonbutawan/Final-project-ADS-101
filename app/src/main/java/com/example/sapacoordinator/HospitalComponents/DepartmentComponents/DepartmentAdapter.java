package com.example.sapacoordinator.HospitalComponents.DepartmentComponents;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sapacoordinator.HospitalComponents.TimeSlotsComponents.DateTimeSlotSelectionActivity;
import com.example.sapacoordinator.R;
import java.util.List;

public class DepartmentAdapter extends RecyclerView.Adapter<DepartmentAdapter.ViewHolder> {

    private final List<Department> departmentList;
    private final Context context;
    private final int schoolId;
    private final int hospitalId;
    private int selectedPosition = -1; // Track selected position

    @SuppressLint("NotifyDataSetChanged")
    public DepartmentAdapter(List<Department> departmentList, Context context, int schoolId, int hospitalId) {
        this.departmentList = departmentList;
        this.context = context;
        this.schoolId = schoolId;
        this.hospitalId = hospitalId;
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
    public void onBindViewHolder(@NonNull DepartmentAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Department department = departmentList.get(position);
        holder.tvDepartmentName.setText(department.getSection_name());
        holder.tvPrice.setText(String.valueOf(department.getPrice_per_student()));
        holder.tvHospitalName.setText(department.getHospital_name());

        // Set background color based on selection
        if (selectedPosition == position) {
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.yellow));
        } else {
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.white));
        }

        holder.itemView.setOnClickListener(v -> {
            // Update selected position
            int previousPosition = selectedPosition;
            selectedPosition = position;

            // Notify adapter to update colors
            if (previousPosition != -1) {
                notifyItemChanged(previousPosition);
            }
            notifyItemChanged(selectedPosition);

            Intent intent = new Intent(context, DateTimeSlotSelectionActivity.class);
            intent.putExtra("hospital_id", hospitalId);
            intent.putExtra("hospital_name", department.getHospital_name());
            intent.putExtra("department_name", department.getSection_name());
            intent.putExtra("department_id", department.getDepartment_id());
            intent.putExtra("school_id", schoolId);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return departmentList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDepartmentName,tvPrice, tvHospitalName;
        CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDepartmentName = itemView.findViewById(R.id.tvSectionName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvHospitalName = itemView.findViewById(R.id.tvHospitalName);
            cardView = (CardView) itemView; // Get reference to the CardView
        }
    }
}
