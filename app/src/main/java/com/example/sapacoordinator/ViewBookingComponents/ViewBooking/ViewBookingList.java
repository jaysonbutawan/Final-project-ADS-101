package com.example.sapacoordinator.ViewBookingComponents.ViewBooking;
import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sapacoordinator.DatabaseConnector.ApiClient;
import com.example.sapacoordinator.DatabaseConnector.ApiInterface;
import com.example.sapacoordinator.DatabaseConnector.GenericResponse;
import com.example.sapacoordinator.HospitalComponents.DepartmentComponents.Department;
import com.example.sapacoordinator.HospitalComponents.TimeSlotsComponents.DateSlot;
import com.example.sapacoordinator.HospitalComponents.TimeSlotsComponents.TimeSlotModel;
import com.example.sapacoordinator.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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
    private TextView resultsCountTextView, tvEmptyMessage, selectionCountTextView;
    private Spinner departmentSpinner, timeSpinner, dateSpinner;
    private Button applyFiltersButton;
    private Button cancelSelectedButton;
    private int schoolId;
    private int hospitalId;
    private int departmentId;

    private int selectedDepartmentId = -1;
    private int selectedDateId = -1;
    private int selectedTimeId = -1;

    private static final String TAG = "ViewBookingList";

    // For cascading filter data
    private List<String> allDepartments;
    private List<String> allDates;
    private List<String> allTimes;

    public boolean isSelectionModeActive() {
        return adapter != null && adapter.isSelectionMode();
    }

    public void exitSelectionMode() {
        if (adapter != null) {
            adapter.exitSelectionMode();
        }
    }


    public static ViewBookingList newInstance(int schoolId, int hospitalId, int departmentId) {
        ViewBookingList fragment = new ViewBookingList();
        Bundle args = new Bundle();
        args.putInt("hospital_id", hospitalId);
        args.putInt("school_id", schoolId);
        args.putInt("department_id", departmentId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            schoolId = getArguments().getInt("school_id", -1);
            hospitalId = getArguments().getInt("hospital_id", -1);
            departmentId = getArguments().getInt("department_id", -1);
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
        setupRecyclerView();
        setupSpinners();
        setupFilterListeners();
//        loadBookingDataFromAPI();
        loadDepartments();
        // Add this in onViewCreated of ViewBookingList.java
        // Use the view parameter directly as the root view
        view.setOnClickListener(v -> {
            if (adapter != null && adapter.isSelectionMode()) {
                adapter.exitSelectionMode();
            }
        });
    }

    private void initializeViews(View view) {
        rvBookings = view.findViewById(R.id.rvBookings);
        tvEmptyMessage = view.findViewById(R.id.tvEmptyMessage);

        // Get views from parent activity
        if (getActivity() != null) {
            resultsCountTextView = getActivity().findViewById(R.id.resultsCountTextView);
            departmentSpinner = getActivity().findViewById(R.id.departmentSpinner);
            timeSpinner = getActivity().findViewById(R.id.timeSpinner);
            dateSpinner = getActivity().findViewById(R.id.dateSpinner);
            applyFiltersButton = getActivity().findViewById(R.id.applyFiltersButton);
        }
    }

    private void setupSpinners() {
        if (departmentSpinner != null && timeSpinner != null && dateSpinner != null) {
            // Initialize with default values
            allDepartments = new ArrayList<>();
            allDates = new ArrayList<>();
            allTimes = new ArrayList<>();

            // Setup department spinner
            List<String> departments = new ArrayList<>();
            departments.add("All Departments");
            ArrayAdapter<String> departmentAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, departments);
            departmentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            departmentSpinner.setAdapter(departmentAdapter);

            // Setup date spinner
            List<String> dates = new ArrayList<>();
            dates.add("All Dates");
            ArrayAdapter<String> dateAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, dates);
            dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            dateSpinner.setAdapter(dateAdapter);

            // Setup time spinner
            List<String> times = new ArrayList<>();
            times.add("All Times");
            ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, times);
            timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            timeSpinner.setAdapter(timeAdapter);
        }
    }

    private void setupFilterListeners() {
        if (departmentSpinner != null) {
            departmentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    // Exit selection mode when user interacts with spinner
                    if (adapter != null && adapter.isSelectionMode()) {
                        adapter.exitSelectionMode();
                    }

                    if (position == 0) {
                        selectedDepartmentId = -1;
                        setupEmptyDateSpinner();
                        setupEmptyTimeSpinner();
                        selectedDateId = -1;
                        selectedTimeId = -1;
                        return;
                    }
                    Department selectedDept = departments.get(position - 1);
                    selectedDepartmentId = selectedDept.getDepartment_id();

                    Log.d(TAG, "User selected Department: " + selectedDept.getSection_name() + " (ID: " + selectedDepartmentId + ")");
                    loadDates(selectedDepartmentId);
                    selectedDateId = -1;
                    selectedTimeId = -1;
                    setupEmptyTimeSpinner();
                    applyFilters();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        }

        if (dateSpinner != null) {
            dateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    // Exit selection mode when user interacts with spinner
                    if (adapter != null && adapter.isSelectionMode()) {
                        adapter.exitSelectionMode();
                    }

                    if (position == 0) {
                        // "All Dates"
                        selectedDateId = -1;
                        selectedTimeId = -1;
                        setupEmptyTimeSpinner();
                        return;
                    }

                    String selectedDate = parent.getSelectedItem().toString();
                    Log.d(TAG, "Selected Date: " + selectedDate);
                    DateSlot selectedSlot = dateSlots.get(position - 1);
                    selectedDateId = selectedSlot.getSlotDateId();

                    Log.d(TAG, "Selected DateSlotID: " + selectedDateId);
                    loadTimes(selectedDateId);

                    selectedTimeId = -1;
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        }

        if (timeSpinner != null) {
            timeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    // Exit selection mode when user interacts with spinner
                    if (adapter != null && adapter.isSelectionMode()) {
                        adapter.exitSelectionMode();
                    }

                    if (position == 0) {
                        // "All Times"
                        selectedTimeId = -1;
                        return;
                    }

                    String selectedTime = parent.getSelectedItem().toString();
                    Log.d(TAG, "Selected Time: " + selectedTime);

                    // ✅ Get the selected time slot object
                    TimeSlotModel selectedSlot = timeSlots.get(position - 1); // offset for "All Times"
                    selectedTimeId = selectedSlot.getTime_slot_id();

                    Log.d(TAG, "Selected TimeSlotID: " + selectedTimeId);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        }

        if (applyFiltersButton != null) {
            applyFiltersButton.setOnClickListener(v -> {
                // Exit selection mode when apply filters is clicked (if in normal mode)
                if (adapter != null && adapter.isSelectionMode()) {
                    // If in selection mode, this button acts as cancel - don't exit here
                    // The cancel functionality will handle this
                } else {
                    loadBookingDataFromAPI();
                }
            });
        }

        if (cancelSelectedButton != null) {
            cancelSelectedButton.setOnClickListener(v -> {
                if (adapter != null && adapter.isSelectionMode()) {
                    adapter.exitSelectionMode();
                }
            });
        }
    }


    private void loadBookingDataFromAPI() {
        if (schoolId == -1 && hospitalId == -1) {
            Log.w("DEBUG_", "Skipping booking load - missing required IDs. schoolId=" + schoolId + ", hospitalId=" + hospitalId);
            return;
        }

        SweetAlertDialog loadingDialog = new SweetAlertDialog(requireContext(), SweetAlertDialog.PROGRESS_TYPE);
        loadingDialog.setTitleText("Loading bookings...");
        loadingDialog.setCancelable(false);
        loadingDialog.show();

        ApiInterface api = ApiClient.getClient().create(ApiInterface.class);

        // Always use one API, send 0 when no filter
        int deptId = (selectedDepartmentId == -1) ? 0 : selectedDepartmentId;
        int dateId = (selectedDateId == -1) ? 0 : selectedDateId;
        int timeId = (selectedTimeId == -1) ? 0 : selectedTimeId;

        Log.d("DEBUG_", "Fetching bookings with - SchoolID: " + schoolId +
                ", DeptID: " + deptId +
                ", DateID: " + dateId +
                ", TimeID: " + timeId);

        Call<List<ViewBookingModel>> call = api.getFilteredBookedStudents(schoolId, deptId, dateId, timeId);

        // Execute
        call.enqueue(new Callback<>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call<List<ViewBookingModel>> call,
                                   @NonNull Response<List<ViewBookingModel>> response) {
                loadingDialog.dismiss();

                Log.d("DEBUG_", "API reached! Response code = " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    Log.d("DEBUG_", "Response size = " + response.body().size());
                    handleBookingResponse(response);
                } else {
                    Log.e("DEBUG_", "Empty/Failed Response: " + response.code());
                    showEmptyState("No bookings found.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ViewBookingModel>> call, @NonNull Throwable t) {
                loadingDialog.dismiss();
                Log.e("DEBUG_", "Network Error: " + t.getMessage(), t);
                showEmptyState("Please check your connection.");
            }
        });
    }
    private void handleBookingResponse(Response<List<ViewBookingModel>> response) {
        if (response.isSuccessful() && response.body() != null) {
            bookingList = response.body();
            Log.d(TAG, "✅ Bookings fetched: " + bookingList.size());
            for (ViewBookingModel b : bookingList) {
                Log.d(TAG, "Booking -> Dept: " + b.getDepartment() +
                        ", Date: " + b.getSlotDate() +
                        ", Time: " + b.getTimeSlot());
            }

            filteredList.clear();
            filteredList.addAll(bookingList);

            if (bookingList.isEmpty()) {
                showEmptyState("No bookings found");
            } else {
                hideEmptyState();
                if (selectedDepartmentId == -1 && selectedDateId == -1 && selectedTimeId == -1) {
                    extractFilterData();
                    updateSpinnerOptions();
                }
                adapter.notifyDataSetChanged();
                updateResultsCount();
            }
        } else {
            Log.e(TAG, "❌ API Error: " + response.code() + " - " + response.message());
            showEmptyState("Failed to load bookings. Please try again.");
        }
    }


    private List<Department> departments = new ArrayList<>();
    private void loadDepartments() {
        ApiInterface api = ApiClient.getClient().create(ApiInterface.class);
        Call<List<Department>> call = api.getDepartments(hospitalId);

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<Department>> call, @NonNull Response<List<Department>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // ✅ assign to the field, not a local var
                    departments = response.body();
                    Log.d("DEBUG_", "Dates fetched: " + departments.size());
                    Log.d("DEBUG_", "Departments fetched: " + departments.size());
                    List<String> names = new ArrayList<>();
                    names.add("All Departments");
                    for (Department d : departments) {
                        names.add(d.getSection_name());
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            requireContext(),
                            android.R.layout.simple_spinner_item,
                            names
                    );
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                    if (departmentSpinner != null) {
                        departmentSpinner.setAdapter(adapter);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Department>> call, @NonNull Throwable t) {
                Log.e(TAG, "Error fetching departments", t);
            }
        });
    }

    private  List<DateSlot> dateSlots = new ArrayList<>();
    private void loadDates(int departmentId) {
        ApiInterface api = ApiClient.getClient().create(ApiInterface.class);
        Call<List<DateSlot>> call = api.getDateSlots(departmentId);

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<DateSlot>> call, @NonNull Response<List<DateSlot>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    dateSlots = response.body(); // ✅ keep full objects

                    Log.d(TAG, "Dates fetched: " + dateSlots.size());

                    List<String> dateList = new ArrayList<>();
                    dateList.add("All Dates");

                    for (DateSlot d : dateSlots) {
                        Log.d(TAG, "Loaded date: " + d.getSlotDate());
                        dateList.add(d.getSlotDate());
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                            android.R.layout.simple_spinner_item, dateList);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    dateSpinner.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<DateSlot>> call, @NonNull Throwable t) {
                Log.e(TAG, "Error fetching dates", t);
            }
        });
    }

    private List<TimeSlotModel> timeSlots = new ArrayList<>();
    private void loadTimes( int slotDateId) {
        ApiInterface api = ApiClient.getClient().create(ApiInterface.class);
        Call<List<TimeSlotModel>> call = api.getTimeSlots(slotDateId);

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<TimeSlotModel>> call, @NonNull Response<List<TimeSlotModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    timeSlots = response.body(); // ✅ keep the full objects

                    List<String> timeList = new ArrayList<>();
                    timeList.add("All Times");

                    for (TimeSlotModel t : timeSlots) {
                        timeList.add(t.getStart_time() + " - " + t.getEnd_time());
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                            android.R.layout.simple_spinner_item, timeList);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    timeSpinner.setAdapter(adapter);
                }
            }

            @Override
            public void onFailure(Call<List<TimeSlotModel>> call, Throwable t) {
                Log.e(TAG, "Error fetching times", t);
            }
        });
    }




    private void extractFilterData() {
        // Extract unique departments, dates, and times from booking data
        Set<String> departmentSet = new HashSet<>();
        Set<String> dateSet = new HashSet<>();
        Set<String> timeSet = new HashSet<>();

        for (ViewBookingModel booking : bookingList) {
            departmentSet.add(booking.getDepartment());
            dateSet.add(booking.getSlotDate());
            timeSet.add(booking.getTimeSlot());
        }

        allDepartments = new ArrayList<>(departmentSet);
        allDates = new ArrayList<>(dateSet);
        allTimes = new ArrayList<>(timeSet);

        // Sort the lists
        Collections.sort(allDepartments);
        Collections.sort(allDates);
        Collections.sort(allTimes);
    }

    private void updateSpinnerOptions() {
        if (departmentSpinner != null) {
            // Update department spinner
            List<String> departments = new ArrayList<>();
            departments.add("All Departments");
            departments.addAll(allDepartments);

            ArrayAdapter<String> departmentAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, departments);
            departmentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            departmentSpinner.setAdapter(departmentAdapter);
        }

        // Initialize date and time spinners with "All" options
        updateDateSpinner("All Departments");
        updateTimeSpinner("All Departments", "All Dates");
    }

    private void updateDateSpinner(String selectedDepartment) {
        if (dateSpinner == null) return;

        List<String> availableDates = new ArrayList<>();
        availableDates.add("All Dates");

        if (selectedDepartment.equals("All Departments")) {
            availableDates.addAll(allDates);
        } else {
            // Filter dates based on selected department
            Set<String> dateSet = new HashSet<>();
            for (ViewBookingModel booking : bookingList) {
                if (booking.getDepartment().equals(selectedDepartment)) {
                    dateSet.add(booking.getSlotDate());
                }
            }
            List<String> filteredDates = new ArrayList<>(dateSet);
            Collections.sort(filteredDates);
            availableDates.addAll(filteredDates);
        }

        ArrayAdapter<String> dateAdapter = new ArrayAdapter<>(
            requireContext(), android.R.layout.simple_spinner_item, availableDates);
        dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dateSpinner.setAdapter(dateAdapter);
    }

    private void updateTimeSpinner(String selectedDepartment, String selectedDate) {
        if (timeSpinner == null) return;

        List<String> availableTimes = new ArrayList<>();
        availableTimes.add("All Times");

        Set<String> timeSet = new HashSet<>();
        for (ViewBookingModel booking : bookingList) {
            boolean departmentMatch = selectedDepartment.equals("All Departments") ||
                                    booking.getDepartment().equals(selectedDepartment);
            boolean dateMatch = selectedDate.equals("All Dates") ||
                              booking.getSlotDate().equals(selectedDate);

            if (departmentMatch && dateMatch) {
                timeSet.add(booking.getTimeSlot());
            }
        }

        List<String> filteredTimes = new ArrayList<>(timeSet);
        Collections.sort(filteredTimes);
        availableTimes.addAll(filteredTimes);

        ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(
            requireContext(), android.R.layout.simple_spinner_item, availableTimes);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeSpinner.setAdapter(timeAdapter);
    }

    private void setupRecyclerView() {
        bookingList = new ArrayList<>();
        filteredList = new ArrayList<>();

        adapter = new ViewBookingAdapter(filteredList, requireContext(), schoolId);

        // Set up selection mode listener
        adapter.setOnSelectionModeListener(new ViewBookingAdapter.OnSelectionModeListener() {
            @Override
            public void onSelectionModeEntered() {
                // Show cancel button and hide apply filters button
                if (applyFiltersButton != null) {
                    applyFiltersButton.setVisibility(View.GONE);
                }
                showCancelButton();
            }

            @Override
            public void onSelectionModeExited() {
                // Hide cancel button and show apply filters button
                hideCancelButton();
                if (applyFiltersButton != null) {
                    applyFiltersButton.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onSelectionChanged(int selectedCount) {
                updateSelectionCount(selectedCount);
            }

            @Override
            public void onCancelSelected(List<ViewBookingModel> selectedBookings) {
                confirmCancelAppointments(selectedBookings);
            }
        });

        rvBookings.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvBookings.setAdapter(adapter);
    }

    private void applyFilters() {
        if (bookingList == null || departmentSpinner == null || dateSpinner == null || timeSpinner == null) {
            return;
        }

        filteredList.clear();

        String selectedDepartment = departmentSpinner.getSelectedItem().toString();
        String selectedDate = dateSpinner.getSelectedItem().toString();
        String selectedTime = timeSpinner.getSelectedItem().toString();

        for (ViewBookingModel booking : bookingList) {
            boolean matches = true;

            if (!selectedDepartment.equals("All Departments") &&
                !booking.getDepartment().equals(selectedDepartment)) {
                matches = false;
            }

            if (!selectedDate.equals("All Dates") &&
                !booking.getSlotDate().equals(selectedDate)) {
                matches = false;
            }

            if (!selectedTime.equals("All Times") &&
                !booking.getTimeSlot().equals(selectedTime)) {
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
        if (resultsCountTextView != null && bookingList != null) {
            String countText =bookingList.size() + " Bookings";
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


    private void setupEmptyDateSpinner() {
        if (dateSpinner != null) {
            List<String> dates = new ArrayList<>();
            dates.add("All Dates");
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_spinner_item, dates);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            dateSpinner.setAdapter(adapter);
        }
    }

    private void setupEmptyTimeSpinner() {
        if (timeSpinner != null) {
            List<String> times = new ArrayList<>();
            times.add("All Times");
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_spinner_item, times);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            timeSpinner.setAdapter(adapter);
        }
    }

    // Selection mode UI management methods
    @SuppressLint("SetTextI18n")
    private void showCancelButton() {
        if (getActivity() != null) {
            // Create cancel button dynamically or show existing one
            if (cancelSelectedButton == null) {
                // If cancel button doesn't exist, transform apply button temporarily
                if (applyFiltersButton != null) {
                    applyFiltersButton.setText("Cancel Selected (" + adapter.getSelectedCount() + ")");
                    applyFiltersButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(android.R.color.holo_red_dark)));
                    applyFiltersButton.setOnClickListener(v -> {
                        List<ViewBookingModel> selectedBookings = adapter.getSelectedBookings();
                        confirmCancelAppointments(selectedBookings);
                    });
                    applyFiltersButton.setVisibility(View.VISIBLE);
                }
            } else {
                cancelSelectedButton.setVisibility(View.VISIBLE);
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void hideCancelButton() {
        if (getActivity() != null) {
            if (cancelSelectedButton != null) {
                cancelSelectedButton.setVisibility(View.GONE);
            } else {
                // Restore apply button to original state
                if (applyFiltersButton != null) {
                    applyFiltersButton.setText("Apply Filters");
                    applyFiltersButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.Blue)));
                    applyFiltersButton.setOnClickListener(v -> loadBookingDataFromAPI());
                }
            }
        }
    }

    private void updateSelectionCount(int selectedCount) {
        if (applyFiltersButton != null && adapter.isSelectionMode()) {
            applyFiltersButton.setText("Cancel Selected (" + selectedCount + ")");
        }
    }

    private void confirmCancelAppointments(List<ViewBookingModel> selectedBookings) {
        if (selectedBookings.isEmpty()) {
            return;
        }

        String message = "Are you sure you want to cancel " + selectedBookings.size() +
                        " appointment" + (selectedBookings.size() > 1 ? "s" : "") + "?";

        new SweetAlertDialog(requireContext(), SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Cancel Appointments")
                .setContentText(message)
                .setConfirmText("Yes, Cancel")
                .setCancelText("No")
                .setConfirmClickListener(sweetAlertDialog -> {
                    sweetAlertDialog.dismiss();
                    cancelAppointments(selectedBookings);
                })
                .setCancelClickListener(SweetAlertDialog::dismiss)
                .show();
    }

    private void cancelAppointments(List<ViewBookingModel> selectedBookings) {
        // Show loading dialog
        SweetAlertDialog loadingDialog = new SweetAlertDialog(requireContext(), SweetAlertDialog.PROGRESS_TYPE);
        loadingDialog.setTitleText("Cancelling appointments...");
        loadingDialog.setCancelable(false);
        loadingDialog.show();

        // Extract student IDs from selected bookings
        List<Integer> studentIds = new ArrayList<>();
        for (ViewBookingModel booking : selectedBookings) {
            studentIds.add(booking.getStudentId());
        }

        ApiInterface api = ApiClient.getClient().create(ApiInterface.class);
        Call<GenericResponse> call = api.cancelAppointment(studentIds);

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<GenericResponse> call, @NonNull Response<GenericResponse> response) {
                loadingDialog.dismiss();

                if (response.isSuccessful() && response.body() != null) {
                    GenericResponse result = response.body();
                    if (result.isSuccess()) {
                        new SweetAlertDialog(requireContext(), SweetAlertDialog.SUCCESS_TYPE)
                                .setTitleText("Success!")
                                .setContentText("Appointments cancelled successfully")
                                .setConfirmClickListener(sweetAlertDialog -> {
                                    sweetAlertDialog.dismiss();
                                    adapter.exitSelectionMode();
                                    loadBookingDataFromAPI();
                                })
                                .show();
                    } else {
                        showErrorDialog("Failed to cancel appointments: " + result.getMessage());
                    }
                } else {
                    showErrorDialog("Failed to cancel appointments. Please try again.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<GenericResponse> call, @NonNull Throwable t) {
                loadingDialog.dismiss();
                showErrorDialog("Network error: " + t.getMessage());
            }
        });
    }

    private void showErrorDialog(String message) {
        new SweetAlertDialog(requireContext(), SweetAlertDialog.ERROR_TYPE)
                .setTitleText("Error")
                .setContentText(message)
                .setConfirmText("OK")
                .show();
    }
}
