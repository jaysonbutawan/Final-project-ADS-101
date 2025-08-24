package com.example.sapacoordinator.ViewBooking;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sapacoordinator.DatabaseConnector.ApiClient;
import com.example.sapacoordinator.DatabaseConnector.ApiInterface;
import com.example.sapacoordinator.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ViewBookingList extends Fragment {

    private RecyclerView rvBookings;
    private ViewBookingAdapter adapter;
    private List<ViewBookingModel> bookingList;
    private List<ViewBookingModel> filteredList;
    private TextView resultsCountTextView, tvEmptyMessage;
    private Spinner departmentSpinner, timeSpinner;
    private EditText dateEditText;
    private Button applyFiltersButton;
    private int schoolId;
    private static final String TAG = "ViewBookingList";

    public static ViewBookingList newInstance(int schoolId) {
        ViewBookingList fragment = new ViewBookingList();
        Bundle args = new Bundle();
        args.putInt("school_id", schoolId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            schoolId = getArguments().getInt("school_id", -1);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_booking_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        setupSpinners();
        setupDatePicker();
        setupRecyclerView();
        setupFilterButton();
        loadBookingDataFromAPI();
    }

    private void initializeViews(View view) {
        rvBookings = view.findViewById(R.id.rvBookings);
        tvEmptyMessage = view.findViewById(R.id.tvEmptyMessage);

        // Get views from parent activity
        if (getActivity() != null) {
            resultsCountTextView = getActivity().findViewById(R.id.resultsCountTextView);
            departmentSpinner = getActivity().findViewById(R.id.departmentSpinner);
            timeSpinner = getActivity().findViewById(R.id.timeSpinner);
            dateEditText = getActivity().findViewById(R.id.dateEditText);
            applyFiltersButton = getActivity().findViewById(R.id.applyFiltersButton);
        }
    }

    private void setupSpinners() {
        if (departmentSpinner != null && timeSpinner != null) {
            // Initialize with default values
            List<String> departments = new ArrayList<>();
            departments.add("All Departments");

            List<String> times = new ArrayList<>();
            times.add("All Times");

            ArrayAdapter<String> departmentAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, departments);
            departmentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            departmentSpinner.setAdapter(departmentAdapter);

            ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, times);
            timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            timeSpinner.setAdapter(timeAdapter);
        }
    }

    private void setupDatePicker() {
        if (dateEditText != null) {
            dateEditText.setOnClickListener(v -> {
                Calendar calendar = Calendar.getInstance();
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                    requireContext(),
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(year, month, dayOfMonth);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        dateEditText.setText(sdf.format(calendar.getTime()));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                );
                datePickerDialog.show();
            });
        }
    }

    private void loadBookingDataFromAPI() {
        if (schoolId == -1) {
            showEmptyState("Invalid school ID");
            return;
        }

        // Show loading dialog
        SweetAlertDialog loadingDialog = new SweetAlertDialog(requireContext(), SweetAlertDialog.PROGRESS_TYPE);
        loadingDialog.setTitleText("Loading bookings...");
        loadingDialog.setCancelable(false);
        loadingDialog.show();

        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<List<ViewBookingModel>> call = apiInterface.getBookedStudents(schoolId);

        call.enqueue(new Callback<>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call<List<ViewBookingModel>> call, @NonNull Response<List<ViewBookingModel>> response) {
                loadingDialog.dismiss();

                if (response.isSuccessful() && response.body() != null) {
                    bookingList = response.body();
                    filteredList = new ArrayList<>(bookingList);

                    if (bookingList.isEmpty()) {
                        showEmptyState("No bookings found for this school");
                    } else {
                        hideEmptyState();
                        updateSpinnerOptions();
                        adapter.notifyDataSetChanged();
                        updateResultsCount();
                    }
                } else {
                    Log.e(TAG, "API Error: " + response.code() + " - " + response.message());
                    showEmptyState("Failed to load bookings. Please try again.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ViewBookingModel>> call, @NonNull Throwable t) {
                loadingDialog.dismiss();
                Log.e(TAG, "Network Error: " + t.getMessage(), t);
                showEmptyState("Network error. Please check your connection.");
            }
        });
    }

    private void updateSpinnerOptions() {
        if (departmentSpinner != null && timeSpinner != null) {
            // Update department spinner
            Set<String> departments = new HashSet<>();
            departments.add("All Departments");
            for (ViewBookingModel booking : bookingList) {
                departments.add(booking.getDepartment());
            }

            ArrayAdapter<String> departmentAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, new ArrayList<>(departments));
            departmentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            departmentSpinner.setAdapter(departmentAdapter);

            // Update time spinner
            Set<String> times = new HashSet<>();
            times.add("All Times");
            for (ViewBookingModel booking : bookingList) {
                times.add(booking.getTimeSlot());
            }

            ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, new ArrayList<>(times));
            timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            timeSpinner.setAdapter(timeAdapter);
        }
    }

    private void setupRecyclerView() {
        bookingList = new ArrayList<>();
        filteredList = new ArrayList<>();

        adapter = new ViewBookingAdapter(filteredList, requireContext(), schoolId);
        rvBookings.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvBookings.setAdapter(adapter);
    }

    private void setupFilterButton() {
        if (applyFiltersButton != null) {
            applyFiltersButton.setOnClickListener(v -> applyFilters());
        }
    }

    private void applyFilters() {
        filteredList.clear();

        String selectedDepartment = departmentSpinner != null ? departmentSpinner.getSelectedItem().toString() : "All Departments";
        String selectedTime = timeSpinner != null ? timeSpinner.getSelectedItem().toString() : "All Times";
        String selectedDate = dateEditText != null ? dateEditText.getText().toString().trim() : "";

        for (ViewBookingModel booking : bookingList) {
            boolean matches = true;

            // Filter by department
            if (!selectedDepartment.equals("All Departments") && !booking.getDepartment().equals(selectedDepartment)) {
                matches = false;
            }

            // Filter by time
            if (!selectedTime.equals("All Times") && !booking.getTimeSlot().contains(selectedTime)) {
                matches = false;
            }

            // Filter by date
            if (!selectedDate.isEmpty() && !booking.getSlotDate().contains(selectedDate)) {
                matches = false;
            }

            if (matches) {
                filteredList.add(booking);
            }
        }

        if (filteredList.isEmpty()) {
            showEmptyState("No bookings match your filter criteria");
        } else {
            hideEmptyState();
        }

        adapter.notifyDataSetChanged();
        updateResultsCount();
    }

    private void updateResultsCount() {
        if (resultsCountTextView != null) {
            String countText = filteredList.size() + " of " + bookingList.size() + " bookings";
            resultsCountTextView.setText(countText);
        }
    }

    private void showEmptyState(String message) {
        if (tvEmptyMessage != null) {
            tvEmptyMessage.setText(message);
            tvEmptyMessage.setVisibility(View.VISIBLE);
        }
        if (rvBookings != null) {
            rvBookings.setVisibility(View.GONE);
        }
    }

    private void hideEmptyState() {
        if (tvEmptyMessage != null) {
            tvEmptyMessage.setVisibility(View.GONE);
        }
        if (rvBookings != null) {
            rvBookings.setVisibility(View.VISIBLE);
        }
    }

    public void refreshData() {
        loadBookingDataFromAPI();
    }
}
