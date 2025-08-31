package com.example.sapacoordinator.HospitalComponents.TimeSlotsComponents;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.sapacoordinator.R;
import com.example.sapacoordinator.SchoolComponents.StudentsComponents.Student;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BookingStudentAdapter extends RecyclerView.Adapter<BookingStudentAdapter.BookingStudentViewHolder> {

    private final Context context;
    private final List<Student> studentList;
    private final Set<Integer> selectedStudentIds;
    private final OnStudentSelectionListener listener;
    private int maxSelections = 0;

    public interface OnStudentSelectionListener {
        void onStudentSelected(Student student, boolean isSelected);
        void onSelectionLimitReached(int maxLimit);
    }

    public BookingStudentAdapter(Context context, List<Student> studentList, OnStudentSelectionListener listener) {
        this.context = context;
        this.studentList = studentList;
        this.selectedStudentIds = new HashSet<>();
        this.listener = listener;
    }

    public void setMaxSelections(int maxSelections) {
        this.maxSelections = maxSelections;
    }

    public int getSelectedCount() {
        return selectedStudentIds.size();
    }

    public Set<Integer> getSelectedStudentIds() {
        return new HashSet<>(selectedStudentIds);
    }

    
    @NonNull
    @Override
    public BookingStudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_booking_student_card, parent, false);
        return new BookingStudentViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull BookingStudentViewHolder holder, int position) {
        Student student = studentList.get(position);
        int studentId = student.getStudentId();

        holder.tvStudentName.setText(student.getFirstname() + " " + student.getLastname());
        holder.tvStudentCode.setText(student.getStudentCode());
        holder.tvGradeAge.setText(student.getSex() + " • " + student.getAge() + " years");
        boolean isSelected = selectedStudentIds.contains(studentId);
        holder.cbSelect.setChecked(isSelected);

        // Set background color based on selection
        if (isSelected) {
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.yellow));
        } else {
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.white));
        }

        holder.cbSelect.setOnCheckedChangeListener(null);
        holder.cbSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (selectedStudentIds.size() >= maxSelections) {
                    android.util.Log.d("BookingStudentAdapter", "Selection limit reached: " + maxSelections);
                    holder.cbSelect.setChecked(false);
                    if (listener != null) {
                        listener.onSelectionLimitReached(maxSelections);
                    }
                    return;
                }
                selectedStudentIds.add(studentId);
            } else {
                selectedStudentIds.remove(studentId);
            }

            // Update card background color immediately
            if (isChecked) {
                holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.yellow));
            } else {
                holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, R.color.white));
            }

            if (listener != null) {
                listener.onStudentSelected(student, isChecked);
            }
        });
        holder.itemView.setOnClickListener(v -> holder.cbSelect.performClick());
    }

    @Override
    public int getItemCount() {
        return studentList != null ? studentList.size() : 0;
    }

    public static class BookingStudentViewHolder extends RecyclerView.ViewHolder {
        TextView tvStudentName, tvStudentCode, tvGradeAge;
        CheckBox cbSelect;
        CardView cardView;

        public BookingStudentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvStudentCode = itemView.findViewById(R.id.tvStudentCode);
            tvGradeAge = itemView.findViewById(R.id.tvGradeAge);
            cbSelect = itemView.findViewById(R.id.cbSelect);
            cardView = (CardView) itemView;
        }
    }
}
