package com.example.sapacoordinator.ViewBooking;

import android.annotation.SuppressLint;
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
import com.example.sapacoordinator.HospitalComponents.DepartmentComponents.Department;
import com.example.sapacoordinator.HospitalComponents.TimeSlotsComponents.DateSlot;
import com.example.sapacoordinator.HospitalComponents.TimeSlotsComponents.TimeSlotList;
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
    private TextView resultsCountTextView, tvEmptyMessage;
    private Spinner departmentSpinner, timeSpinner, dateSpinner;
    private Button applyFiltersButton;
    private int schoolId;
    private int hospitalId;
    private int departmentId;

    private static final String TAG = "ViewBookingList";

    // For cascading filter data
    private List<String> allDepartments;
    private List<String> allDates;
    private List<String> allTimes;

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
        loadBookingDataFromAPI();
        loadDepartments();
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
                    if (position == 0) {
                        // "All Departments"
                        setupEmptyDateSpinner();
                        setupEmptyTimeSpinner();
                        applyFilters();
                        return;
                    }

                    // ✅ Use the departments list populated in loadDepartments()
                    Department selectedDept = departments.get(position - 1); // -1 for "All Departments"
                    int departmentId = selectedDept.getDepartment_id();

                    Log.d(TAG, "User selected Department: " + selectedDept.getSection_name() +
                            " (ID: " + departmentId + ")");

                    // ✅ Now load dates for THIS department
                    loadDates(departmentId);

                    // Reset times
                    setupEmptyTimeSpinner();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });

        }

        if (dateSpinner != null) {
            dateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position == 0) {
                        // "All Dates"
                        setupEmptyTimeSpinner();
                        applyFilters();
                        return;
                    }

                    String selectedDate = parent.getSelectedItem().toString();
                    Log.d(TAG, "Selected Date: " + selectedDate);

                    // ✅ Find the DateSlot object
                    DateSlot selectedSlot = dateSlots.get(position - 1); // offset for "All Dates"
                    int slotDateId = selectedSlot.getSlotDateId(); // make sure your model has this field
                    int selectedDeptId = departmentId; // fallback

                    if (departmentSpinner.getSelectedItemPosition() > 0) {
                        Department selectedDept = departments.get(departmentSpinner.getSelectedItemPosition() - 1);
                        selectedDeptId = selectedDept.getDepartment_id();
                    }

                    Log.d(TAG, "Loading times for DeptID: " + selectedDeptId + " SlotDateID: " + slotDateId);
                    loadTimes(slotDateId);

                    applyFilters();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        }

        if (timeSpinner != null) {
            timeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (position == 0) {
                        // "All Times"
                        applyFilters();
                        return;
                    }

                    String selectedTime = parent.getSelectedItem().toString();
                    Log.d(TAG, "Selected Time: " + selectedTime);

                    // ✅ Get the selected time slot object
                    TimeSlotModel selectedSlot = timeSlots.get(position - 1); // offset for "All Times"
                    int slotTimeId = selectedSlot.getTime_slot_id();

                    Log.d(TAG, "Selected TimeSlotID: " + slotTimeId);

                    applyFilters();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        }



        if (applyFiltersButton != null) {
            applyFiltersButton.setOnClickListener(v -> applyFilters());
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
                        extractFilterData();
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

    private List<Department> departments = new ArrayList<>();
    private void loadDepartments() {
        ApiInterface api = ApiClient.getClient().create(ApiInterface.class);
        Call<List<Department>> call = api.getDepartments(getHospitalIdFromBookings());

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<Department>> call, @NonNull Response<List<Department>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // ✅ assign to the field, not a local var
                    departments = response.body();

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


    /**
     * Helper method to get hospital ID from existing booking data
     * This assumes your booking data contains hospital information
     */
    private int getHospitalIdFromBookings() {
        // Use the hospitalId passed from the activity first
        if (hospitalId != -1) {
            Log.d(TAG, "Using hospitalId from arguments: " + hospitalId);
            return hospitalId;
        }

        // Fallback: If you pass hospital ID from previous activity
        int intentHospitalId = getActivity() != null ? getActivity().getIntent().getIntExtra("hospital_id", -1) : -1;
        if (intentHospitalId != -1) {
            Log.d(TAG, "Using hospitalId from intent: " + intentHospitalId);
            return intentHospitalId;
        }

        // Last resort: Use default hospital ID
        Log.w(TAG, "No hospital ID found, using default value 1");
        return 1;
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

            // Filter by department
            if (!selectedDepartment.equals("All Departments") &&
                !booking.getDepartment().equals(selectedDepartment)) {
                matches = false;
            }

            // Filter by date
            if (!selectedDate.equals("All Dates") &&
                !booking.getSlotDate().equals(selectedDate)) {
                matches = false;
            }

            // Filter by time
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
        loadDepartments();
        loadDates(departmentId);
    }

    // ===== NEW FUNCTIONS FOR RETRIEVING HIERARCHICAL DATA =====

    /**
     * Retrieves all unique departments from the booking data
     * @return List of department names
     */
    public List<String> getAllDepartments() {
        if (bookingList == null || bookingList.isEmpty()) {
            return new ArrayList<>();
        }

        Set<String> departmentSet = new HashSet<>();
        for (ViewBookingModel booking : bookingList) {
            departmentSet.add(booking.getDepartment());
        }

        List<String> departments = new ArrayList<>(departmentSet);
        Collections.sort(departments);
        return departments;
    }

    /**
     * Retrieves all dates available for a specific department
     * @param departmentName The department to filter by
     * @return List of dates available for the specified department
     */
    public List<String> getDatesForDepartment(String departmentName) {
        if (bookingList == null || bookingList.isEmpty() || departmentName == null) {
            return new ArrayList<>();
        }

        Set<String> dateSet = new HashSet<>();
        for (ViewBookingModel booking : bookingList) {
            if (booking.getDepartment().equals(departmentName)) {
                dateSet.add(booking.getSlotDate());
            }
        }

        List<String> dates = new ArrayList<>(dateSet);
        Collections.sort(dates);
        return dates;
    }

    /**
     * Retrieves all time slots available for a specific department and date
     * @param departmentName The department to filter by
     * @param date The date to filter by
     * @return List of time slots available for the specified department and date
     */
    public List<String> getTimesForDepartmentAndDate(String departmentName, String date) {
        if (bookingList == null || bookingList.isEmpty() || departmentName == null || date == null) {
            return new ArrayList<>();
        }

        Set<String> timeSet = new HashSet<>();
        for (ViewBookingModel booking : bookingList) {
            if (booking.getDepartment().equals(departmentName) &&
                booking.getSlotDate().equals(date)) {
                timeSet.add(booking.getTimeSlot());
            }
        }

        List<String> times = new ArrayList<>(timeSet);
        Collections.sort(times);
        return times;
    }

    /**
     * Retrieves all bookings for a specific department
     * @param departmentName The department to filter by
     * @return List of bookings for the specified department
     */
    public List<ViewBookingModel> getBookingsForDepartment(String departmentName) {
        if (bookingList == null || bookingList.isEmpty() || departmentName == null) {
            return new ArrayList<>();
        }

        List<ViewBookingModel> departmentBookings = new ArrayList<>();
        for (ViewBookingModel booking : bookingList) {
            if (booking.getDepartment().equals(departmentName)) {
                departmentBookings.add(booking);
            }
        }
        return departmentBookings;
    }

    /**
     * Retrieves all bookings for a specific department and date
     * @param departmentName The department to filter by
     * @param date The date to filter by
     * @return List of bookings for the specified department and date
     */
    public List<ViewBookingModel> getBookingsForDepartmentAndDate(String departmentName, String date) {
        if (bookingList == null || bookingList.isEmpty() || departmentName == null || date == null) {
            return new ArrayList<>();
        }

        List<ViewBookingModel> filteredBookings = new ArrayList<>();
        for (ViewBookingModel booking : bookingList) {
            if (booking.getDepartment().equals(departmentName) &&
                booking.getSlotDate().equals(date)) {
                filteredBookings.add(booking);
            }
        }
        return filteredBookings;
    }

    /**
     * Retrieves all bookings for a specific department, date, and time
     * @param departmentName The department to filter by
     * @param date The date to filter by
     * @param timeSlot The time slot to filter by
     * @return List of bookings for the specified criteria
     */
    public List<ViewBookingModel> getBookingsForDepartmentDateAndTime(String departmentName, String date, String timeSlot) {
        if (bookingList == null || bookingList.isEmpty() ||
            departmentName == null || date == null || timeSlot == null) {
            return new ArrayList<>();
        }

        List<ViewBookingModel> filteredBookings = new ArrayList<>();
        for (ViewBookingModel booking : bookingList) {
            if (booking.getDepartment().equals(departmentName) &&
                booking.getSlotDate().equals(date) &&
                booking.getTimeSlot().equals(timeSlot)) {
                filteredBookings.add(booking);
            }
        }
        return filteredBookings;
    }

    /**
     * Gets the currently selected department from the spinner
     * @return Selected department name or null if nothing selected
     */
    public String getSelectedDepartment() {
        if (departmentSpinner != null && departmentSpinner.getSelectedItem() != null) {
            String selected = departmentSpinner.getSelectedItem().toString();
            return selected.equals("All Departments") ? null : selected;
        }
        return null;
    }

    /**
     * Gets the currently selected date from the spinner
     * @return Selected date or null if nothing selected
     */
    public String getSelectedDate() {
        if (dateSpinner != null && dateSpinner.getSelectedItem() != null) {
            String selected = dateSpinner.getSelectedItem().toString();
            return selected.equals("All Dates") ? null : selected;
        }
        return null;
    }

    /**
     * Gets the currently selected time from the spinner
     * @return Selected time or null if nothing selected
     */
    public String getSelectedTime() {
        if (timeSpinner != null && timeSpinner.getSelectedItem() != null) {
            String selected = timeSpinner.getSelectedItem().toString();
            return selected.equals("All Times") ? null : selected;
        }
        return null;
    }

    /**
     * Programmatically set the department spinner selection
     * @param departmentName The department to select
     */
    public void setSelectedDepartment(String departmentName) {
        if (departmentSpinner != null && departmentName != null) {
            ArrayAdapter<String> adapter = (ArrayAdapter<String>) departmentSpinner.getAdapter();
            if (adapter != null) {
                int position = adapter.getPosition(departmentName);
                if (position >= 0) {
                    departmentSpinner.setSelection(position);
                }
            }
        }
    }

    /**
     * Programmatically set the date spinner selection
     * @param date The date to select
     */
    public void setSelectedDate(String date) {
        if (dateSpinner != null && date != null) {
            ArrayAdapter<String> adapter = (ArrayAdapter<String>) dateSpinner.getAdapter();
            if (adapter != null) {
                int position = adapter.getPosition(date);
                if (position >= 0) {
                    dateSpinner.setSelection(position);
                }
            }
        }
    }

    /**
     * Programmatically set the time spinner selection
     * @param timeSlot The time slot to selectlo
     */
    public void setSelectedTime(String timeSlot) {
        if (timeSpinner != null && timeSlot != null) {
            ArrayAdapter<String> adapter = (ArrayAdapter<String>) timeSpinner.getAdapter();
            if (adapter != null) {
                int position = adapter.getPosition(timeSlot);
                if (position >= 0) {
                    timeSpinner.setSelection(position);
                }
            }
        }
    }

    /**
     * Check if data has been loaded
     * @return true if booking data is available
     */
    public boolean isDataLoaded() {
        return bookingList != null && !bookingList.isEmpty();
    }

    /**
     * Get total number of bookings
     * @return Total booking count
     */
    public int getTotalBookingCount() {
        return bookingList != null ? bookingList.size() : 0;
    }

    /**
     * Get filtered booking count
     * @return Filtered booking count
     */
    public int getFilteredBookingCount() {
        return filteredList != null ? filteredList.size() : 0;
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
}
