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

public class TimeSlotList extends Fragment {

    private RecyclerView recyclerView;
    private TextView tvEmptyMessage;
    private TimeSlotAdapter adapter;
    private final List<TimeSlotModel> timeSlotList = new ArrayList<>();
    private ApiInterface api;
    private OnTimeSlotSelectedListener callback;

    public static TimeSlotList newInstance(int dateSlotId) {
        TimeSlotList fragment = new TimeSlotList();
        Bundle args = new Bundle();
        args.putInt("date_slot_id", dateSlotId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        api = ApiClient.getClient().create(ApiInterface.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.time_list, container, false);
        recyclerView = view.findViewById(R.id.rvTimeSlot);
        tvEmptyMessage = view.findViewById(R.id.tvEmptyMessage);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new TimeSlotAdapter(timeSlotList, this::handleTimeSlotSelection);
        recyclerView.setAdapter(adapter);

        if (getArguments() != null) {
            int dateSlotId = getArguments().getInt("date_slot_id", -1);
            loadTimeSlots(dateSlotId);
        }

        return view;
    }
    public void updateTimeSlots(int newDateSlotId) {
        loadTimeSlots(newDateSlotId); // 🔥 refresh dynamically
    }


    // ✅ Single dynamic method for loading timeslots
    public void loadTimeSlots(int dateSlotId) {
        if (dateSlotId == -1) {
            showMessage("Invalid date slot.", true);
            return;
        }

        // clear current slots while fetching new ones
        timeSlotList.clear(); // Clear the main list
        adapter.updateData(new ArrayList<>());

        Call<List<TimeSlotModel>> call = api.getTimeSlots(dateSlotId);
        call.enqueue(new Callback<>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(@NonNull Call<List<TimeSlotModel>> call, @NonNull Response<List<TimeSlotModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<TimeSlotModel> newSlots = response.body();

                    if (newSlots.isEmpty()) {
                        showMessage("No timeslot found for this date", true);
                    } else {
                        showMessage("", false);
                        timeSlotList.clear(); // Clear the main list
                        timeSlotList.addAll(newSlots); // Update the main list
                        adapter.updateData(newSlots); // ✅ refresh adapter with new slots
                    }

                    Log.d("API_RESPONSE", "Time slots loaded: " + newSlots.size());
                    Log.d("API_RESPONSE", new Gson().toJson(newSlots));
                } else {
                    showMessage("Failed to load time slots.", true);
                    Log.d("API_RESPONSE", "Failed: " + response.message());
                }
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onFailure(@NonNull Call<List<TimeSlotModel>> call, @NonNull Throwable t) {
                showMessage("Failed to load time slots.", true);
                Log.e("API_RESPONSE", "Error: " + t.getMessage(), t);
            }
        });
    }

    // ✅ Helper method to toggle empty message / recycler
    private void showMessage(String message, boolean show) {
        if (show) {
            tvEmptyMessage.setText(message);
            tvEmptyMessage.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmptyMessage.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void handleTimeSlotSelection(int timeSlotId) {
        if (callback != null) {
            callback.onTimeSlotSelected(timeSlotId);
        }
    }

    // ✅ Method to get the capacity of a selected time slot
    public int getSelectedTimeSlotCapacity(int timeSlotId) {
        for (TimeSlotModel timeSlot : timeSlotList) {
            if (timeSlot.getTime_slot_id() == timeSlotId) {
                return timeSlot.getCapacity();
            }
        }
        return 10; // Default capacity if not found
    }

    public interface OnTimeSlotSelectedListener {
        void onTimeSlotSelected(int timeSlotId);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnTimeSlotSelectedListener) {
            callback = (OnTimeSlotSelectedListener) context;
        }
    }
}
