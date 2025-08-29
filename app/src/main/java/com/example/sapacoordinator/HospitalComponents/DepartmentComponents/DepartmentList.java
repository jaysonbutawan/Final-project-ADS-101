package com.example.sapacoordinator.HospitalComponents.DepartmentComponents;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sapacoordinator.DatabaseConnector.ApiClient;
import com.example.sapacoordinator.DatabaseConnector.ApiInterface;
import com.example.sapacoordinator.R;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DepartmentList extends Fragment {

    private RecyclerView recyclerView;
    private TextView tvEmptyMessage;
    private DepartmentAdapter adapter;
    private final List<Department> departmentList = new ArrayList<>();
    private int hospitalId;
    private int schoolId;

    public DepartmentList() {}

    public static DepartmentList newInstance(int hospitalId, int schoolId) {
        DepartmentList fragment = new DepartmentList();
        Bundle args = new Bundle();
        args.putInt("hospital_id", hospitalId);
        args.putInt("school_id", schoolId);
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_department_list, container, false);

        recyclerView = view.findViewById(R.id.rvDepartments);
        tvEmptyMessage = view.findViewById(R.id.tvEmptyMessage);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        if (getArguments() != null) {
            hospitalId = getArguments().getInt("hospital_id", -1);
            schoolId = getArguments().getInt("school_id", -1);

            // ✅ Enhanced debugging for DepartmentList
            Log.d("DepartmentList", "=== RECEIVED ARGUMENTS ===");
            Log.d("DepartmentList", "Hospital ID: " + hospitalId);
            Log.d("DepartmentList", "School ID: " + schoolId);

            // ✅ Validate hospital ID range
            if (hospitalId < 14 || hospitalId > 16) {
                Log.e("DepartmentList", "🚨 SUSPICIOUS: Hospital ID " + hospitalId + " is outside expected range (14-16)");
            } else {
                Log.d("DepartmentList", "✅ Hospital ID " + hospitalId + " is within expected range");
            }
        }

        // ✅ FIXED: Pass hospitalId to adapter constructor
        adapter = new DepartmentAdapter(departmentList, requireContext(), schoolId, hospitalId);
        recyclerView.setAdapter(adapter);

        if (hospitalId != -1) {
            loadDepartments(hospitalId);
        } else {
            Log.e("DepartmentList", "🚨 CRITICAL: Invalid hospital ID (-1)");
            tvEmptyMessage.setText("Invalid hospital ID.");
            tvEmptyMessage.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }

        return view;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void loadDepartments(int hospitalId) {
        ApiInterface api = ApiClient.getClient().create(ApiInterface.class);
        Call<List<Department>> call = api.getDepartments(hospitalId);

        call.enqueue(new Callback<>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(@NonNull Call<List<Department>> call,@NonNull Response<List<Department>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    departmentList.clear();
                    departmentList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    Log.d("API_RESPONSE","API department has been called" + new Gson().toJson(response.body()));
                    Log.d("API_RESPONSE", "Departments loaded: " + new Gson().toJson(response.body()));
                    Log.d("API_STATUS", "✅ API Call Successful");
                    if (departmentList.isEmpty()) {
                        tvEmptyMessage.setText("No departments found.");
                        tvEmptyMessage.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                        Log.d("API_RESPONSE", "No departments found for hospital ID: " + hospitalId);
                        Log.d("API_STATUS", "✅ API Call not found");
                    } else {
                        tvEmptyMessage.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                    }
                } else {
                    tvEmptyMessage.setText("Failed to load departments.");
                    tvEmptyMessage.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                }
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onFailure(@NonNull Call<List<Department>> call, @NonNull Throwable t) {
                tvEmptyMessage.setText("Failed to loading departments.");
                tvEmptyMessage.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                Log.e("API_ERROR", "Department load failed", t);
                Log.d("API_STATUS", "✅ failed to load departments");

            }
        });
    }
}
