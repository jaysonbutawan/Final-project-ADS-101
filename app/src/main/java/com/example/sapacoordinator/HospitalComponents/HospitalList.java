package com.example.sapacoordinator.HospitalComponents;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
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
import com.example.sapacoordinator.ViewBookingComponents.ChooseActionBookingAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HospitalList extends Fragment {

    private RecyclerView recyclerView;
    private TextView tvEmptyMessage;
    private RecyclerView.Adapter<?> adapter; // Changed to generic type
    private final List<Hospital> hospitalList = new ArrayList<>();
    private int schoolId;
    private boolean useBookingAdapter = false; // Flag to determine which adapter to use

    public HospitalList() {
    }

    public static HospitalList newInstance(int schoolId) {
        return newInstance(schoolId, false); // Default to HospitalAdapter
    }

    // Updated factory method with adapter selection parameter
    public static HospitalList newInstance(int schoolId, boolean useBookingAdapter) {
        HospitalList fragment = new HospitalList();
        Bundle args = new Bundle();
        args.putInt("school_id", schoolId);
        args.putBoolean("use_booking_adapter", useBookingAdapter);
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_hospital_list, container, false);

        // Get arguments
        if (getArguments() != null) {
            schoolId = getArguments().getInt("school_id", -1);
            useBookingAdapter = getArguments().getBoolean("use_booking_adapter", false);
        }

        recyclerView = view.findViewById(R.id.rvHospitals);
        tvEmptyMessage = view.findViewById(R.id.tvEmptyMessage);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        // Choose adapter based on flag
        if (useBookingAdapter) {
            adapter = new ChooseActionBookingAdapter(hospitalList, requireContext(), schoolId);
        } else {
            adapter = new HospitalAdapter(hospitalList, requireContext(), schoolId);
        }

        recyclerView.setAdapter(adapter);

        loadHospitals();

        return view;
    }

    @SuppressLint("SetTextI18n")
    private void loadHospitals() {
        SharedPreferences prefs = requireContext().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);

        Log.d("HospitalDebug", "Loading hospitals for userId: " + userId);

        if (userId == -1) {
            tvEmptyMessage.setText("User session expired. Please log in again.");
            tvEmptyMessage.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            Log.w("HospitalDebug", "User session expired, stopping hospital load.");
            return;
        }

        ApiInterface api = ApiClient.getClient().create(ApiInterface.class);
        Call<List<Hospital>> call = api.getHospitals();

        Log.d("HospitalDebug", "API request initiated to fetch hospitals.");

        call.enqueue(new Callback<>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call<List<Hospital>> call, @NonNull Response<List<Hospital>> response) {
                Log.d("HospitalDebug", "API response received. Success=" + response.isSuccessful());

                if (response.isSuccessful() && response.body() != null) {
                    List<Hospital> hospitals = response.body();
                    Log.d("HospitalDebug", "Hospitals received: " + hospitals.size());

                    // ✅ Enhanced debugging: Log each hospital with detailed info
                    for (int i = 0; i < hospitals.size(); i++) {
                        Hospital hospital = hospitals.get(i);
                        Log.d("HospitalDebug", "Hospital[" + i + "] -> ID=" + hospital.getHospitalId() +
                                ", Name=" + hospital.getHospitalName() +
                                ", Address=" + hospital.getHospitalAddress());

                        // ✅ Validate hospital ID is not null/zero
                        if (hospital.getHospitalId() <= 0) {
                            Log.e("HospitalDebug", "⚠️ INVALID HOSPITAL ID DETECTED: " + hospital.getHospitalId() +
                                    " for hospital: " + hospital.getHospitalName());
                        }
                    }

                    hospitalList.clear();
                    hospitalList.addAll(hospitals);

                    // ✅ Verify data integrity after adding to list
                    Log.d("HospitalDebug", "Verifying hospital list integrity:");
                    for (int i = 0; i < hospitalList.size(); i++) {
                        Hospital hospital = hospitalList.get(i);
                        Log.d("HospitalDebug", "hospitalList[" + i + "] -> ID=" + hospital.getHospitalId() +
                                ", Name=" + hospital.getHospitalName());
                    }

                    adapter.notifyDataSetChanged();

                    if (hospitalList.isEmpty()) {
                        tvEmptyMessage.setText("No hospitals found.");
                        tvEmptyMessage.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                        Log.w("HospitalDebug", "Hospital list is empty.");
                    } else {
                        tvEmptyMessage.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);
                        Log.d("HospitalDebug", "Hospital list displayed with " + hospitalList.size() + " items.");
                    }
                } else {
                    Log.e("HospitalDebug", "Response failed or empty body. Code=" + response.code());
                    // ✅ Enhanced error logging
                    if (response.errorBody() != null) {
                        try {
                            String errorBody = response.errorBody().string();
                            Log.e("HospitalDebug", "Error body: " + errorBody);
                        } catch (Exception e) {
                            Log.e("HospitalDebug", "Failed to read error body: " + e.getMessage());
                        }
                    }
                    tvEmptyMessage.setText("Failed to load hospitals (empty response).");
                    tvEmptyMessage.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Hospital>> call, @NonNull Throwable t) {
                Log.e("API_ERROR", "Load failed: " + t.getMessage(), t);
                // ✅ Enhanced failure logging
                Log.e("HospitalDebug", "API call stack trace:", t);
                tvEmptyMessage.setText("Failed to load hospitals");
                tvEmptyMessage.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
        });
    }

}
