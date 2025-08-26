package com.example.sapacoordinator.SchoolComponents.StudentsComponents;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.sapacoordinator.DatabaseConnector.ApiClient;
import com.example.sapacoordinator.DatabaseConnector.ApiInterface;
import com.example.sapacoordinator.DatabaseConnector.GenericResponse;
import com.example.sapacoordinator.R;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentList extends Fragment {

    private static final String ARG_SCHOOL_ID = "school_id";

    private RecyclerView recyclerView;
    private TextView tvEmptyMessage;
    private StudentAdapter adapter;
    private final List<Student> studentList = new ArrayList<>();
    private int schoolId;

    public static StudentList newInstance(int schoolId) {
        StudentList fragment = new StudentList();
        Bundle args = new Bundle();
        args.putInt(ARG_SCHOOL_ID, schoolId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            schoolId = getArguments().getInt(ARG_SCHOOL_ID, -1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_student_list, container, false);

        recyclerView = view.findViewById(R.id.recyclerStudents);
        tvEmptyMessage = view.findViewById(R.id.tvEmptyMessage);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new StudentAdapter(requireContext(), studentList);

        // Set up delete action listener
        adapter.setOnStudentActionListener(new StudentAdapter.OnStudentActionListener() {
            @Override
            public void onEdit(Student student) {
                // Edit functionality is already handled in the adapter
            }

            @Override
            public void onDelete(Student student) {
                showDeleteConfirmationDialog(student);
            }
        });

        recyclerView.setAdapter(adapter);

        loadStudents();

        return view;
    }

    public void refreshStudents() {
        loadStudents();
    }

    @SuppressLint("SetTextI18n")
    private void loadStudents() {
        SharedPreferences prefs = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);

        if (userId == -1) {
            tvEmptyMessage.setText("User session expired. Please log in again.");
            tvEmptyMessage.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            return;
        }

        Log.d("StudentList", "Loading students for school_id: " + schoolId + ", user_id: " + userId);

        ApiInterface api = ApiClient.getClient().create(ApiInterface.class);
        Call<List<Student>> call = api.getStudents(userId, schoolId);

        call.enqueue(new Callback<>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call<List<Student>> call, @NonNull Response<List<Student>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    studentList.clear();
                    studentList.addAll(response.body());
                    adapter.notifyDataSetChanged();

                    Log.d("StudentList", "Students loaded successfully: " + studentList.size() + " students");
                    Log.d("API_RESPONSE", new Gson().toJson(response.body()));

                    if (studentList.isEmpty()) {
                        tvEmptyMessage.setText("No students found.");
                        tvEmptyMessage.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        tvEmptyMessage.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                } else {
                    Log.e("StudentList", "Failed to load students: " + response.code() + " - " + response.message());
                    tvEmptyMessage.setText("Failed to load students");
                    tvEmptyMessage.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Student>> call, @NonNull Throwable t) {
                Log.e("StudentList", "Error loading students", t);
                tvEmptyMessage.setText("Failed to load students");
                tvEmptyMessage.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
        });
    }

    private void showDeleteConfirmationDialog(Student student) {
        new SweetAlertDialog(requireContext(), SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Delete Student")
                .setContentText("Are you sure you want to delete " + student.getFirstname() + " " + student.getLastname() + "?\n\nThis action cannot be undone.")
                .setConfirmText("Yes, Delete")
                .setCancelText("Cancel")
                .showCancelButton(true)
                .setConfirmClickListener(sDialog -> {
                    sDialog.dismissWithAnimation();
                    deleteStudent(student);
                })
                .setCancelClickListener(SweetAlertDialog::dismissWithAnimation)
                .show();
    }

    private void deleteStudent(Student student) {
        SharedPreferences prefs = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);

        if (userId == -1) {
            showErrorDialog("User session expired. Please log in again.");
            return;
        }

        // Show loading dialog
        SweetAlertDialog loadingDialog = new SweetAlertDialog(requireContext(), SweetAlertDialog.PROGRESS_TYPE)
                .setTitleText("Deleting Student")
                .setContentText("Please wait...");
        loadingDialog.setCancelable(false);
        loadingDialog.show();

        ApiInterface api = ApiClient.getClient().create(ApiInterface.class);
        Call<GenericResponse> call = api.deleteStudent(student.getStudentId(),student.getSchoolId());
        Log.d("DELETE_STUDENT", "Request: student_id=" + student.getStudentId() + ", school_id=" + student.getSchoolId());

        call.enqueue(new Callback<GenericResponse>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call<GenericResponse> call, @NonNull Response<GenericResponse> response) {
                loadingDialog.dismissWithAnimation();

                if (response.isSuccessful() && response.body() != null) {
                    GenericResponse res = response.body();
                    if (res.isSuccess()) {
                        // Remove student from list and refresh UI
                        studentList.remove(student);
                        adapter.notifyDataSetChanged();

                        // Check if list is empty
                        if (studentList.isEmpty()) {
                            tvEmptyMessage.setText("No students found.");
                            tvEmptyMessage.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        }

                        new SweetAlertDialog(requireContext(), SweetAlertDialog.SUCCESS_TYPE)
                                .setTitleText("Success")
                                .setContentText("Student deleted successfully!")
                                .setConfirmClickListener(SweetAlertDialog::dismissWithAnimation)
                                .show();
                    } else {
                        showErrorDialog(res.getMessage());
                    }
                } else {
                    showErrorDialog("Server error: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<GenericResponse> call, @NonNull Throwable t) {
                loadingDialog.dismissWithAnimation();
                showErrorDialog("Connection Error: " + t.getMessage());
            }
        });
    }

    private void showErrorDialog(String message) {
        new SweetAlertDialog(requireContext(), SweetAlertDialog.ERROR_TYPE)
                .setTitleText("Error")
                .setContentText(message)
                .setConfirmClickListener(SweetAlertDialog::dismissWithAnimation)
                .show();
    }

}
