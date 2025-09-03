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
    private OnDateSelectedListener callback;


    public static DateSlotList newInstance(int departmentId) {
        DateSlotList fragment = new DateSlotList();
        Bundle args = new Bundle();
        args.putInt("department_id", departmentId);
        fragment.setArguments(args);
        return fragment;
    }

    public interface OnDateSelectedListener {
        void onDateSelected(int dateSlotId);
    }

    public void setCallback(OnDateSelectedListener listener) {
        this.callback = listener;
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (callback == null) {
            if (context instanceof OnDateSelectedListener) {
                callback = (OnDateSelectedListener) context;
            }
        }
    }

    private void handleDateSelection(int dateSlotId) {
       assert  callback != null;
            callback.onDateSelected(dateSlotId);

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
        ApiInterface api = ApiClient.getClient().create(ApiInterface.class);
        Call<List<DateSlot>> call = api.getDateSlots(departmentId);

        call.enqueue(new Callback<>() {
            @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
            @Override
            public void onResponse(@NonNull Call<List<DateSlot>> call, @NonNull Response<List<DateSlot>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    dateSlotList.clear();
                    dateSlotList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    adapter.setDefaultSelection();

                    tvEmptyMessage.setVisibility(dateSlotList.isEmpty() ? View.VISIBLE : View.GONE);
                    recyclerView.setVisibility(dateSlotList.isEmpty() ? View.GONE : View.VISIBLE);
                } else {
                    tvEmptyMessage.setText("Failed to load date slots.");
                    tvEmptyMessage.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                }
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onFailure(@NonNull Call<List<DateSlot>> call, @NonNull Throwable t) {
                tvEmptyMessage.setText("Failed to load date slots.");
                tvEmptyMessage.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            }
        });
    }

    public String getSelectedDateString(int dateSlotId) {
        for (DateSlot dateSlot : dateSlotList) {
            if (dateSlot.getSlotDateId() == dateSlotId) {

                return dateSlot.getSlotDate();
            }
        }
        return "Unknown Date";
    }
}
