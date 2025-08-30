package com.example.sapacoordinator.ViewBookingComponents.BookingPayment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sapacoordinator.R;
import com.example.sapacoordinator.SchoolComponents.StudentsComponents.Student;

import java.util.List;

public class PaymentStudentAdapter extends RecyclerView.Adapter<PaymentStudentAdapter.PaymentStudentViewHolder> {

    private final List<Student> students;

    public PaymentStudentAdapter(List<Student> students) {
        this.students = students;
    }

    @NonNull
    @Override
    public PaymentStudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_student_booking_card, parent, false);
        return new PaymentStudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentStudentViewHolder holder, int position) {
        Student student = students.get(position);
        holder.bind(student);
    }

    @Override
    public int getItemCount() {
        return students.size();
    }

    public static class PaymentStudentViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvStudentName;
        private final TextView tvTimeSlot;
        private final TextView tvDepartment;

        public PaymentStudentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvTimeSlot = itemView.findViewById(R.id.tvTimeSlot);
            tvDepartment = itemView.findViewById(R.id.tvDepartment);
        }

        public void bind(Student student) {
            String studentName = student.getFirstname() + " " + student.getLastname();
            tvStudentName.setText("Student: " + studentName);
            tvTimeSlot.setText("ID: " + student.getStudentCode());
            tvDepartment.setText("Email: " + student.getEmail());
        }
    }
}
