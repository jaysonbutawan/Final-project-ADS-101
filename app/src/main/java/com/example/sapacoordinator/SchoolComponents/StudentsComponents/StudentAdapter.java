package com.example.sapacoordinator.SchoolComponents.StudentsComponents;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sapacoordinator.R;

import java.util.List;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {

    private final Context context;
    private final List<Student> studentList;
    private OnStudentActionListener listener;

    public interface OnStudentActionListener {
        void onEdit(Student student);
        void onDelete(Student student);
    }

    @SuppressLint("NotifyDataSetChanged")
    public StudentAdapter(Context context, List<Student> studentList) {
        this.context = context;
        this.studentList = studentList;
        notifyDataSetChanged();
    }

    public void setOnStudentActionListener(OnStudentActionListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_student_card, parent, false);
        return new StudentViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        Student student = studentList.get(position);

        // Full name
        holder.tvStudentName.setText(student.getFirstname() + " " + student.getLastname());
        holder.tvStudentCode.setText(student.getStudentCode());

        // Gender & Age
        holder.tvGender.setText(student.getSex());
        if (student.getAge() != 0) {
            holder.tvAge.setText(student.getAge() + " years");
        } else {
            holder.tvAge.setText("N/A");
        }

        holder.tvPhone.setText(student.getPhoneNumber() != null ? student.getPhoneNumber() : "N/A");
        holder.tvEmail.setText(student.getEmail() != null ? student.getEmail() : "N/A");

        // Set click listeners for buttons
        holder.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(context, StudentsRegistration.class);
            intent.putExtra("school_id", student.getSchoolId());
            intent.putExtra("edit_mode", true);
            intent.putExtra("student_id", student.getStudentId());
            intent.putExtra("student_code", student.getStudentCode());
            intent.putExtra("firstname", student.getFirstname());
            intent.putExtra("lastname", student.getLastname());
            intent.putExtra("phone_number", student.getPhoneNumber());
            intent.putExtra("email", student.getEmail());
            intent.putExtra("sex", student.getSex());
            intent.putExtra("age", student.getAge());
            context.startActivity(intent);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDelete(student);
            }
        });
    }

    @Override
    public int getItemCount() {
        return studentList != null ? studentList.size() : 0;
    }

    public static class StudentViewHolder extends RecyclerView.ViewHolder {
        TextView tvStudentName, tvStudentCode, tvGender, tvAge, tvPhone, tvEmail;
        ImageButton btnEdit, btnDelete;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvStudentCode = itemView.findViewById(R.id.tvStudentCode);
            tvGender = itemView.findViewById(R.id.tvGender);
            tvAge = itemView.findViewById(R.id.tvAge);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
