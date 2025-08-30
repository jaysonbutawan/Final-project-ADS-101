package com.example.sapacoordinator.HospitalComponents.TimeSlotsComponents;

import android.annotation.SuppressLint;
import android.content.Context;
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

public class DateSlotList extends Fragment {
    private RecyclerView recyclerView;
    private TextView tvEmptyMessage;
    private DateAdapter adapter;
    private final List<DateSlot> dateSlotList = new ArrayList<>();
    private int departmentId;

    // 🔑 Callback
    private OnDateSelectedListener callback;

    // Factory method
    public static DateSlotList newInstance(int departmentId) {
        DateSlotList fragment = new DateSlotList();
        Bundle args = new Bundle();
        args.putInt("department_id", departmentId);
        fragment.setArguments(args);
        return fragment;
    }

    // Interface
    public interface OnDateSelectedListener {
        void onDateSelected(int dateSlotId);
    }

    // ✅ Allow manual injection of callback
    public void setCallback(OnDateSelectedListener listener) {
        this.callback = listener;
    }

    // ✅ Fallback: if not manually injected, try to attach from activity
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (callback == null) {
            if (context instanceof OnDateSelectedListener) {
                callback = (OnDateSelectedListener) context;
                Log.d("DEBUG", "Callback attached via onAttach: " + callback);
            } else {
                Log.w("DEBUG", "Host activity does not implement OnDateSelectedListener");
            }
        }
    }

    private void handleDateSelection(int dateSlotId) {
        Log.d("DEBUG", "Date selected with ID: " + dateSlotId);
        if (callback != null) {
            callback.onDateSelected(dateSlotId);
        } else {
            Log.w("DEBUG", "Callback is null, cannot notify parent.");
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.date_list, container, false);

        recyclerView = view.findViewById(R.id.rvDates);
        tvEmptyMessage = view.findViewById(R.id.tvEmptyMessage);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new DateAdapter(dateSlotList, this::handleDateSelection);
        recyclerView.setAdapter(adapter);

        if (getArguments() != null) {
            departmentId = getArguments().getInt("department_id", -1);
        }

        if (departmentId != -1) {
            loadDateSlots(departmentId);
        } else {
            tvEmptyMessage.setText("Invalid department ID.");
            tvEmptyMessage.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }

        return view;
    }

    private void loadDateSlots(int departmentId) {
        Log.d("DEBUG", "Loading date slots for department: " + departmentId);
        ApiInterface api = ApiClient.getClient().create(ApiInterface.class);
        Call<List<DateSlot>> call = api.getDateSlots(departmentId);

        call.enqueue(new Callback<>() {
            @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
            @Override
            public void onResponse(@NonNull Call<List<DateSlot>> call, @NonNull Response<List<DateSlot>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("DEBUG", "API Success: " + new Gson().toJson(response.body()));
                    dateSlotList.clear();
                    dateSlotList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    adapter.setDefaultSelection();

                    tvEmptyMessage.setVisibility(dateSlotList.isEmpty() ? View.VISIBLE : View.GONE);
                    recyclerView.setVisibility(dateSlotList.isEmpty() ? View.GONE : View.VISIBLE);
                } else {
                    tvEmptyMessage.setText("Failed to load date slots.");
                    Log.e("DEBUG", "API Error: " + response.code());
                    tvEmptyMessage.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                }
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onFailure(@NonNull Call<List<DateSlot>> call, @NonNull Throwable t) {
                tvEmptyMessage.setText("Failed to load date slots.");
                Log.e("DEBUG", "API Failure: " + t.getMessage(), t);
                tvEmptyMessage.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
        });
    }

    // ✅ Method to get the date string for a selected date slot
    public String getSelectedDateString(int dateSlotId) {
        for (DateSlot dateSlot : dateSlotList) {
            if (dateSlot.getSlotDateId() == dateSlotId) {
                // Return the training date string
                return dateSlot.getSlotDate(); // Assuming getTraining_date() returns the date string
            }
        }
        return "Unknown Date"; // Default fallback if not found
    }
}
