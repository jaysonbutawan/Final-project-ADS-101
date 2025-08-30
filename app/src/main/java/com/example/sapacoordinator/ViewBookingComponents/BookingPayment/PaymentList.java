package com.example.sapacoordinator.ViewBookingComponents.BookingPayment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sapacoordinator.R;
import com.example.sapacoordinator.SchoolComponents.StudentsComponents.Student;

import java.util.ArrayList;
import java.util.List;

public class PaymentList extends Fragment {

    private RecyclerView recyclerSelectedStudents;
    private TextView tvEmptyMessage;
    private PaymentStudentAdapter adapter;
    private List<Student> selectedStudents = new ArrayList<>();

    public static PaymentList newInstance(ArrayList<Parcelable> students) {
        PaymentList fragment = new PaymentList();
        Bundle args = new Bundle();
        args.putParcelableArrayList("selected_students", students);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_payment_list, container, false);

        initializeViews(view);
        loadSelectedStudents();
        setupRecyclerView();

        return view;
    }

    private void initializeViews(View view) {
        recyclerSelectedStudents = view.findViewById(R.id.recyclerSelectedStudents);
        tvEmptyMessage = view.findViewById(R.id.tvEmptyMessage);
    }

    private void loadSelectedStudents() {
        if (getArguments() != null) {
            ArrayList<Student> students = getArguments().getParcelableArrayList("selected_students");
            if (students != null) {
                selectedStudents.clear();
                selectedStudents.addAll(students);

                // ✅ Add debugging to track data flow
                Log.d("PaymentList", "=== STUDENTS RECEIVED IN PAYMENT LIST ===");
                Log.d("PaymentList", "Total students received: " + students.size());
                for (int i = 0; i < students.size(); i++) {
                    Student student = students.get(i);
                    Log.d("PaymentList", "Student " + (i + 1) + ": " + student.getFirstname() + " " + student.getLastname() + " (ID: " + student.getStudentId() + ")");
                }
            } else {
                Log.w("PaymentList", "No students data found in arguments");
            }
        } else {
            Log.w("PaymentList", "No arguments bundle found");
        }
    }

    @SuppressLint("SetTextI18n")
    private void setupRecyclerView() {
        if (selectedStudents.isEmpty()) {
            recyclerSelectedStudents.setVisibility(View.GONE);
            tvEmptyMessage.setVisibility(View.VISIBLE);
            tvEmptyMessage.setText("No students selected for payment");
        } else {
            recyclerSelectedStudents.setVisibility(View.VISIBLE);
            tvEmptyMessage.setVisibility(View.GONE);

            adapter = new PaymentStudentAdapter(selectedStudents);
            recyclerSelectedStudents.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerSelectedStudents.setAdapter(adapter);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateStudentList(List<Student> students) {
        selectedStudents.clear();
        selectedStudents.addAll(students);
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        setupRecyclerView();
    }
}
