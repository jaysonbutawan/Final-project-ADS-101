package com.example.sapacoordinator.SchoolComponents;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.sapacoordinator.DatabaseConnector.ApiClient;
import com.example.sapacoordinator.DatabaseConnector.ApiInterface;
import com.example.sapacoordinator.DatabaseConnector.GenericResponse;
import com.example.sapacoordinator.R;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class SchoolList extends Fragment {
    private static final String ARG_STATUS = "status";

    private RecyclerView recyclerView;
    private TextView tvEmptyMessage;
    private SchoolsAdapter adapter;
    private List<School> schoolList = new ArrayList<>();
    private String name, status, address, contact;

    public static SchoolList newInstance(String status) {
        SchoolList fragment = new SchoolList();
        Bundle args = new Bundle();
        args.putString(ARG_STATUS, status);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            status = getArguments().getString(ARG_STATUS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_school_list, container, false);
        recyclerView = view.findViewById(R.id.recyclerSchools);
        tvEmptyMessage = view.findViewById(R.id.tvEmptyMessage);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new SchoolsAdapter(requireContext(), schoolList, new SchoolsAdapter.OnSchoolClickListener() {
            @Override
            public void onSchoolClick(School selectedSchool) {
                // ✅ Add debug logging to track school_id being passed
                Log.d("DEBUG_", "Selected school_id: " + selectedSchool.getId());
                Log.d("DEBUG_", "Selected school_name: " + selectedSchool.getName());

                Intent intent = new Intent(getContext(), ChooseAction.class);
                intent.putExtra("school_id", selectedSchool.getId());
                intent.putExtra("school_name", selectedSchool.getName());
                intent.putExtra("school_address", selectedSchool.getAddress());
                intent.putExtra("school_contact", selectedSchool.getContact());
                intent.putExtra("school_status", selectedSchool.getStatus());
                startActivity(intent);
            }

            @Override
            public void onSchoolLongClick(School school) {
                showSchoolActionDialog(school);
            }
        });

        recyclerView.setAdapter(adapter);

        loadSchools();
        return view;
    }


    @SuppressLint("SetTextI18n")
    private void loadSchools() {
        SharedPreferences prefs = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);
        if (userId == -1) {
            tvEmptyMessage.setText("User session expired. Please log in again.");
            tvEmptyMessage.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            return;
        }


        ApiInterface api = ApiClient.getClient().create(ApiInterface.class);
        Call<List<School>> call = api.getSchools(userId,status);

        call.enqueue(new Callback<>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call<List<School>> call, @NonNull Response<List<School>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // ✅ Add detailed logging of API response
                    Log.d("DEBUG_", "API Response successful, schools count: " + response.body().size());

                    // ✅ Log each school's details including ID
                    for (int i = 0; i < response.body().size(); i++) {
                        School school = response.body().get(i);
                        Log.d("DEBUG_", "School " + i + ":");
                        Log.d("DEBUG_", "  ID: " + school.getId());
                        Log.d("DEBUG_", "  Name: " + school.getName());
                        Log.d("DEBUG_", "  Status: " + school.getStatus());
                    }

                    schoolList.clear();
                    schoolList.addAll(response.body());
                    adapter.notifyDataSetChanged();

                    if (schoolList.isEmpty()) {
                        tvEmptyMessage.setText("No " + status.toLowerCase() + " schools");
                        tvEmptyMessage.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                    } else {
                        tvEmptyMessage.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                } else {
                    Log.e("DEBUG_", "API Response failed: " + response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<School>> call, Throwable t) {
                tvEmptyMessage.setText("Failed to load " + status.toLowerCase() + " schools");
                tvEmptyMessage.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
        });
    }

    private void showSchoolActionDialog(School school) {
        new SweetAlertDialog(requireContext(), SweetAlertDialog.NORMAL_TYPE)
                .setTitleText("School Actions")
                .setContentText("What would you like to do with " + school.getName() + "?")
                .setConfirmText("Edit")
                .setCancelText("Delete")
                .showCancelButton(true)
                .setConfirmClickListener(sDialog -> {
                    sDialog.dismissWithAnimation();
                    editSchool(school);
                })
                .setCancelClickListener(sDialog -> {
                    sDialog.dismissWithAnimation();
                    showDeleteConfirmationDialog(school);
                })
                .show();
    }

    private void editSchool(School school) {
        // Instead of using EditSchoolActivity, launch the existing activity_register_school
        // but pass edit mode data through intent
        Intent intent = new Intent(getContext(), activity_register_school.class);
        intent.putExtra("edit_mode", true);
        intent.putExtra("school_id", school.getId());
        intent.putExtra("school_name", school.getName());
        intent.putExtra("school_address", school.getAddress());
        intent.putExtra("school_contact", school.getContact());
        intent.putExtra("direct_to_edit", true); // Flag to go directly to the Add School tab
        startActivity(intent);
    }

    private void showDeleteConfirmationDialog(School school) {
        new SweetAlertDialog(requireContext(), SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Delete School")
                .setContentText("Are you sure you want to delete " + school.getName() + "?\n\nThis action cannot be undone and will remove all associated students and appointments.")
                .setConfirmText("Yes, Delete")
                .setCancelText("Cancel")
                .showCancelButton(true)
                .setConfirmClickListener(sDialog -> {
                    sDialog.dismissWithAnimation();
                    deleteSchool(school);
                })
                .setCancelClickListener(SweetAlertDialog::dismissWithAnimation)
                .show();
    }

    private void deleteSchool(School school) {
        SharedPreferences prefs = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);

        if (userId == -1) {
            showErrorDialog("User session expired. Please log in again.");
            return;
        }

        // Show loading dialog
        SweetAlertDialog loadingDialog = new SweetAlertDialog(requireContext(), SweetAlertDialog.PROGRESS_TYPE)
                .setTitleText("Deleting School")
                .setContentText("Please wait...");
        loadingDialog.setCancelable(false);
        loadingDialog.show();

        ApiInterface api = ApiClient.getClient().create(ApiInterface.class);
        Call<GenericResponse> call = api.deleteSchool(school.getId(), userId);

        call.enqueue(new Callback<GenericResponse>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call<GenericResponse> call, @NonNull Response<GenericResponse> response) {
                loadingDialog.dismissWithAnimation();

                if (response.isSuccessful() && response.body() != null) {
                    GenericResponse res = response.body();
                    if (res.isSuccess()) {
                        // Remove school from list and refresh UI
                        schoolList.remove(school);
                        adapter.notifyDataSetChanged();

                        // Check if list is empty
                        if (schoolList.isEmpty()) {
                            tvEmptyMessage.setText("No " + status.toLowerCase() + " schools");
                            tvEmptyMessage.setVisibility(View.VISIBLE);
                            recyclerView.setVisibility(View.GONE);
                        }

                        new SweetAlertDialog(requireContext(), SweetAlertDialog.SUCCESS_TYPE)
                                .setTitleText("Success")
                                .setContentText("School deleted successfully!")
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
