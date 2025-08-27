package com.example.sapacoordinator.SchoolComponents;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sapacoordinator.R;

import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class SchoolsAdapter extends RecyclerView.Adapter<SchoolsAdapter.SchoolViewHolder> {

    private final List<School> schoolList;
    private final OnSchoolClickListener listener;
    private final Context context;

    public interface OnSchoolClickListener {

        void onSchoolClick(School school);
        void onSchoolLongClick(School school);

    }


    public SchoolsAdapter(Context context, List<School> schoolList, OnSchoolClickListener listener) {
        this.context = context;
        this.schoolList = schoolList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SchoolViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_school_card, parent, false);
        return new SchoolViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SchoolViewHolder holder, int position) {
        School school = schoolList.get(position);
        holder.tvSchoolName.setText(school.getName());
        holder.tvStatus.setText(school.getStatus());
        holder.tvAddress.setText(school.getAddress());
        holder.tvContact.setText(school.getContact());
        holder.tvAddedDate.setText("Added on " + school.getAddedDate());

        holder.itemView.setOnClickListener(v -> {
            if (school.getStatus().equalsIgnoreCase("Approved")) {
                listener.onSchoolClick(school);
            } else {
                // Using adapter's context field
                new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Pending Approval")
                        .setContentText("Please wait for the admin to approve it.")
                        .setConfirmText("OK")
                        .show();
            }
        });

        // Add long press listener
        holder.itemView.setOnLongClickListener(v -> {
            listener.onSchoolLongClick(school);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return schoolList.size();
    }

    public static class SchoolViewHolder extends RecyclerView.ViewHolder {
        TextView tvSchoolName, tvStatus, tvAddress, tvContact, tvAddedDate;

        public SchoolViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSchoolName = itemView.findViewById(R.id.tvSchoolName);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvContact = itemView.findViewById(R.id.tvContact);
            tvAddedDate = itemView.findViewById(R.id.tvAddedDate);
        }
    }
}
