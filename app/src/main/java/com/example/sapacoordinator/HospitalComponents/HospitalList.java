package com.example.sapacoordinator.HospitalComponents;

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
import com.example.sapacoordinator.ViewBookingComponents.ChooseActionBookingAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HospitalList extends Fragment {

    private RecyclerView recyclerView;
    private TextView tvEmptyMessage;
    private RecyclerView.Adapter<?> adapter;
    private final List<Hospital> hospitalList = new ArrayList<>();
    private int schoolId;
    private boolean useBookingAdapter = false;

    public static HospitalList newInstance(int schoolId, boolean useBookingAdapter) {
        HospitalList fragment = new HospitalList();
        Bundle args = new Bundle();
        args.putInt("school_id", schoolId);
        args.putBoolean("use_booking_adapter", useBookingAdapter);
        fragment.setArguments(args);
        return fragment;
    }

    public static HospitalList newInstance(int schoolId) {
        return newInstance(schoolId, false);
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_hospital_list, container, false);

        if (getArguments() != null) {
            schoolId = getArguments().getInt("school_id", -1);
            useBookingAdapter = getArguments().getBoolean("use_booking_adapter", false);
        }

        recyclerView = view.findViewById(R.id.rvHospitals);
        tvEmptyMessage = view.findViewById(R.id.tvEmptyMessage);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = useBookingAdapter
                ? new ChooseActionBookingAdapter(hospitalList, requireContext(), schoolId)
                : new HospitalAdapter(hospitalList, requireContext(), schoolId);

        recyclerView.setAdapter(adapter);
        loadHospitals();

        return view;
    }

    private void toggleEmptyState(boolean isEmpty, String message) {
        if (isEmpty) {
            tvEmptyMessage.setText(message);
            tvEmptyMessage.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmptyMessage.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void logHospitals(List<Hospital> hospitals) {
        Log.d("HospitalDebug", "Hospitals received: " + hospitals.size());
        for (int i = 0; i < hospitals.size(); i++) {
            Hospital h = hospitals.get(i);
            Log.d("HospitalDebug", String.format(
                    "Hospital[%d] -> ID=%d, Name=%s, Address=%s",
                    i, h.getHospitalId(), h.getHospitalName(), h.getHospitalAddress()
            ));
            if (h.getHospitalId() <= 0) {
                Log.e("HospitalDebug", "⚠️ INVALID HOSPITAL ID: " + h.getHospitalName());
            }
        }
    }

    private void loadHospitals() {
        ApiInterface api = ApiClient.getClient().create(ApiInterface.class);
        api.getHospitals().enqueue(new Callback<List<Hospital>>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call<List<Hospital>> call,
                                   @NonNull Response<List<Hospital>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Hospital> hospitals = response.body();
                    logHospitals(hospitals);

                    hospitalList.clear();
                    hospitalList.addAll(hospitals);
                    adapter.notifyDataSetChanged();

                    toggleEmptyState(hospitalList.isEmpty(), "No hospitals found.");
                } else {
                    Log.e("HospitalDebug", "Response failed. Code=" + response.code());
                    try {
                        if (response.errorBody() != null) {
                            Log.e("HospitalDebug", "Error body: " + response.errorBody().string());
                        }
                    } catch (Exception e) {
                        Log.e("HospitalDebug", "Failed to read error body", e);
                    }
                    toggleEmptyState(true, "Failed to load hospitals (empty response).");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Hospital>> call, @NonNull Throwable t) {
                Log.e("HospitalDebug", "API call failed", t);
                toggleEmptyState(true, "Failed to load hospitals.");
            }
        });
    }
}
