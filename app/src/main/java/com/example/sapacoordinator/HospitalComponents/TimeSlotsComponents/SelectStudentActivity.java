package com.example.sapacoordinator.HospitalComponents.TimeSlotsComponents;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sapacoordinator.DatabaseConnector.ApiClient;
import com.example.sapacoordinator.DatabaseConnector.ApiInterface;
import com.example.sapacoordinator.DatabaseConnector.GenericResponse;
import com.example.sapacoordinator.R;
import com.example.sapacoordinator.SchoolComponents.StudentsComponents.Student;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SelectStudentActivity extends AppCompatActivity implements BookingStudentAdapter.OnStudentSelectionListener {

    private int schoolId;
    private int departmentId;
    private int dateSlotId;
    private int timeSlotId;
    private  int hospitalId = -1;
    private int maxCapacity = 0;
    private int currentBookedCount = 0;
    private int availableSlots = 0;

    private TextView tvSelectionSummary;
    private RecyclerView rvAvailableStudent;
    private Button btnContinueBooking;

    private BookingStudentAdapter adapter;
    private List<Student> studentList = new ArrayList<>();
    private int selectedCount = 0;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.select_students_activity);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.select_student), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get booking data from intent
        schoolId = getIntent().getIntExtra("school_id", -1);
        hospitalId = getIntent().getIntExtra("hospital_id", -1);
        departmentId = getIntent().getIntExtra("department_id", -1);
        dateSlotId = getIntent().getIntExtra("date_slot_id", -1);
        timeSlotId = getIntent().getIntExtra("time_slot_id", -1);
        maxCapacity = getIntent().getIntExtra("capacity", 0);
        currentBookedCount = getIntent().getIntExtra("booked_count", 0);
        availableSlots = maxCapacity - currentBookedCount;

        // ✅ Enhanced debugging for SelectStudentActivity
        Log.d("SelectStudentActivity", "=== BOOKING DATA RECEIVED ===");
        Log.d("SelectStudentActivity", "School ID: " + schoolId);
        Log.d("SelectStudentActivity", "Hospital ID: " + hospitalId);
        Log.d("SelectStudentActivity", "Department ID: " + departmentId);
        Log.d("SelectStudentActivity", "Date Slot ID: " + dateSlotId);
        Log.d("SelectStudentActivity", "Time Slot ID: " + timeSlotId);
        Log.d("SelectStudentActivity", "Max Capacity: " + maxCapacity);
        Log.d("SelectStudentActivity", "Booked Count: " + currentBookedCount);
        Log.d("SelectStudentActivity", "Available Slots: " + availableSlots);

        // ✅ Critical validation for hospital ID
        if (hospitalId <= 0) {
            Log.e("SelectStudentActivity", "🚨 CRITICAL: Invalid hospital ID received: " + hospitalId);
        } else if (hospitalId < 14 || hospitalId > 16) {
            Log.e("SelectStudentActivity", "🚨 SUSPICIOUS: Hospital ID " + hospitalId + " is outside expected range (14-16)");
            Log.e("SelectStudentActivity", "This is likely where the invalid ID 11 is coming from!");
        } else {
            Log.d("SelectStudentActivity", "✅ Hospital ID " + hospitalId + " is within expected range");
        }

        // Initialize views
        initializeViews();

        // Validate booking data
        if (!isBookingDataValid()) {
            Log.e("SelectStudentActivity", "Invalid booking data received");
            new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Invalid Data!")
                    .setContentText("Some booking information is missing. Please go back and try again.")
                    .setConfirmText("OK")
                    .setConfirmClickListener(dialog -> {
                        dialog.dismissWithAnimation();
                        finish();
                    })
                    .show();
            return;
        }
        refreshBookingCount();

        setupRecyclerView();

        loadAvailableStudents();
    }


    private void setupRecyclerView() {
        adapter = new BookingStudentAdapter(this, studentList, this);
        adapter.setMaxSelections(availableSlots); // Set the actual available slots
        rvAvailableStudent.setLayoutManager(new LinearLayoutManager(this));
        rvAvailableStudent.setAdapter(adapter);
    }

    private void refreshBookingCount() {
        ApiInterface api = ApiClient.getClient().create(ApiInterface.class);
        Call<List<TimeSlotModel>> call = api.getTimeSlots(dateSlotId);

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<TimeSlotModel>> call, @NonNull Response<List<TimeSlotModel>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<TimeSlotModel> timeSlots = response.body();

                    // Find the specific time slot we're working with
                    for (TimeSlotModel slot : timeSlots) {
                        if (slot.getTime_slot_id() == timeSlotId) {
                            // Update the booking count
                            currentBookedCount = slot.getBooked_count();
                            availableSlots = maxCapacity - currentBookedCount;

                            Log.d("DEBUG_", "Refreshed booking count: " + currentBookedCount);
                            Log.d("DEBUG_", "Updated available slots: " + availableSlots);

                            // Update the adapter with the new limit
                            adapter.setMaxSelections(availableSlots);
                            updateUI();
                            break;
                        }
                    }
                } else {
                    Log.e("DEBUG_", "Failed to refresh booking count: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<TimeSlotModel>> call, @NonNull Throwable t) {
                Log.e("DEBUG_", "Error refreshing booking count", t);
            }
        });
    }

    private void initializeViews() {
        rvAvailableStudent = findViewById(R.id.rvAvailableStudent);
        btnContinueBooking = findViewById(R.id.btnContinueBooking);
        tvSelectionSummary = findViewById(R.id.tvSelectionSummary);

        // Handle continue button click
        btnContinueBooking.setOnClickListener(v -> {
            if (selectedCount > 0) {
                proceedToFinalBooking();
            } else {
                Toast.makeText(this, "Please select at least one student", Toast.LENGTH_SHORT).show();
            }
        });

    }


    private boolean isBookingDataValid() {
        return schoolId != -1 && departmentId != -1 && dateSlotId != -1 && timeSlotId != -1;
    }

    private void loadAvailableStudents() {
        Log.d("DEBUG_", "Loading available students for school ID: " + schoolId);

        SharedPreferences prefs = getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);

        if (userId == -1) {
            Toast.makeText(this, "User session expired. Please log in again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ApiInterface api = ApiClient.getClient().create(ApiInterface.class);
        Call<List<Student>> call = api.getAvailableStudents(userId, schoolId,timeSlotId);

        call.enqueue(new Callback<>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call<List<Student>> call, @NonNull Response<List<Student>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    studentList.clear();
                    studentList.addAll(response.body());
                    adapter.notifyDataSetChanged();

                    Log.d("DEBUG_", "Students loaded: " + studentList.size());
                    Log.d("API_RESPONSE", new Gson().toJson(response.body()));


                } else {
                    Log.e("DEBUG_", "Failed to load students: " + response.code());
                    Toast.makeText(SelectStudentActivity.this, "Failed to load students", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Student>> call, @NonNull Throwable t) {
                Log.e("DEBUG_", "Error loading students", t);
                Toast.makeText(SelectStudentActivity.this, "Error loading students", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onStudentSelected(Student student, boolean isSelected) {
        selectedCount = adapter.getSelectedCount();
        Log.d("DEBUG_", "Student " + student.getFirstname() + " " +
              (isSelected ? "selected" : "deselected") + ". Total selected: " + selectedCount);
        updateUI();
    }
    @Override
    public void onSelectionLimitReached(int maxLimit) {
         new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Selection Limit Reached!")
                .setContentText("You can select up to " + maxLimit + " students for this time slot.\n")
                .setConfirmText("OK")
                 .show();
    }

    @SuppressLint("SetTextI18n")
    private void updateUI() {
        if (tvSelectionSummary != null) {
            if (selectedCount == 0) {
                tvSelectionSummary.setText("No students selected");
            } else if (selectedCount == 1) {
                tvSelectionSummary.setText("1 Student Selected");
            } else {
                tvSelectionSummary.setText(selectedCount + " Students Selected");
            }
        }

        // Update continue button state
        if (btnContinueBooking != null) {
            btnContinueBooking.setEnabled(selectedCount > 0);
        }
    }

    private void proceedToFinalBooking() {
        Set<Integer> selectedStudentIds = adapter.getSelectedStudentIds();

        Log.d("DEBUG_", "Proceeding to final booking with:");
        Log.d("DEBUG_", "Selected students: " + selectedStudentIds.size());
        Log.d("DEBUG_", "Student IDs: " + selectedStudentIds.toString());

        // Show loading state
        btnContinueBooking.setEnabled(false);
        btnContinueBooking.setText("Submitting Booking...");


        // Submit booking to API - send all students at once
        submitBookingForAllStudents(new ArrayList<>(selectedStudentIds), hospitalId);
    }

    private void submitBookingForAllStudents(List<Integer> studentIds, int hospitalId) {
        // ✅ Enhanced debugging before API call
        Log.d("SelectStudentActivity", "=== SUBMITTING BOOKING TO API ===");
        Log.d("SelectStudentActivity", "🏥 Hospital ID being sent: " + hospitalId);
        Log.d("SelectStudentActivity", "🏫 School ID: " + schoolId);
        Log.d("SelectStudentActivity", "🏢 Department ID: " + departmentId);
        Log.d("SelectStudentActivity", "📅 Date Slot ID: " + dateSlotId);
        Log.d("SelectStudentActivity", "⏰ Time Slot ID: " + timeSlotId);
        Log.d("SelectStudentActivity", "👥 Student IDs: " + studentIds);

//        // ✅ Final validation before sending
//        if (hospitalId < 14 || hospitalId > 16) {
//            Log.e("SelectStudentActivity", "🚨 ABOUT TO SEND INVALID HOSPITAL ID: " + hospitalId);
//            Log.e("SelectStudentActivity", "Expected range: 14-16, but got: " + hospitalId);
//
//            // Show error and abort
//            handleBookingError("Invalid hospital ID detected: " + hospitalId + ". Expected range: 14-16");
//            return;
//        }

        ApiInterface api = ApiClient.getClient().create(ApiInterface.class);
        String studentIdsJson = new Gson().toJson(studentIds);
        Call<GenericResponse> call = api.bookAppointment(schoolId, hospitalId, departmentId, dateSlotId, timeSlotId, studentIdsJson);

        // ✅ Log the exact parameters being sent to API
        Log.d("SelectStudentActivity", "API call parameters:");
        Log.d("SelectStudentActivity", "  schoolId: " + schoolId);
        Log.d("SelectStudentActivity", "  hospitalId: " + hospitalId);
        Log.d("SelectStudentActivity", "  departmentId: " + departmentId);
        Log.d("SelectStudentActivity", "  dateSlotId: " + dateSlotId);
        Log.d("SelectStudentActivity", "  timeSlotId: " + timeSlotId);
        Log.d("SelectStudentActivity", "  studentIdsJson: " + studentIdsJson);

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<GenericResponse> call, @NonNull Response<GenericResponse> response) {
                // ✅ Log the response details
                Log.d("SelectStudentActivity", "=== API RESPONSE RECEIVED ===");
                Log.d("SelectStudentActivity", "Response code: " + response.code());
                Log.d("SelectStudentActivity", "Response message: " + response.message());

                if (response.isSuccessful() && response.body() != null) {
                    GenericResponse genericResponse = response.body();

                    // Add debug logging to see the actual response
                    Log.d("SelectStudentActivity", "Response body:");
                    Log.d("SelectStudentActivity", "Raw response: " + new Gson().toJson(genericResponse));
                    Log.d("SelectStudentActivity", "Status: " + response.code());
                    Log.d("SelectStudentActivity", "Message: " + genericResponse.getMessage());
                    Log.d("SelectStudentActivity", "isSuccess(): " + genericResponse.isSuccess());

                    // Check for success using multiple conditions
                    boolean isSuccessful = genericResponse.isSuccess() ||
                                         (genericResponse.getMessage() != null &&
                                          genericResponse.getMessage().toLowerCase().contains("success")) ||
                                         (genericResponse.getMessage() != null &&
                                          genericResponse.getMessage().toLowerCase().contains("booked"));

                    if (isSuccessful) {
                        Log.d("SelectStudentActivity", "✅ Booking successful for all students");
                        Log.d("SelectStudentActivity", "Response: " + genericResponse.getMessage());
                        refreshBookingCount();
                        showBookingSuccessDialog();
                    } else {
                        // Booking failed
                        Log.e("SelectStudentActivity", "❌ Booking failed - Status check failed");
                        Log.e("SelectStudentActivity", "Message received: " + genericResponse.getMessage());
                        handleBookingError("Booking failed: " + genericResponse.getMessage());
                    }
                } else {
                    Log.e("SelectStudentActivity", "❌ API response failed: " + response.code() + " - " + response.message());
                    handleBookingError("API response failed: " + response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<GenericResponse> call, @NonNull Throwable t) {
                Log.e("SelectStudentActivity", "❌ Network error during booking", t);
                handleBookingError("Network error: " + t.getMessage());
            }
        });
    }

    private void handleBookingError(String errorMessage) {
        // Reset button state
        btnContinueBooking.setText("Continue to Final Booking");
        btnContinueBooking.setEnabled(true);

        Log.e("DEBUG_", errorMessage);

        // Use SweetAlertDialog for error message
        new SweetAlertDialog(SelectStudentActivity.this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText("Booking Failed!")
                .setContentText("Unable to complete your booking request.\n\n" +
                        errorMessage + "\n\n")
                .setConfirmText("OK")
                .show();
    }

    // Show success dialog with booking details
    private void showBookingSuccessDialog() {
        // Reset button state
        btnContinueBooking.setText("Continue to Final Booking");
        btnContinueBooking.setEnabled(true);

        new SweetAlertDialog(SelectStudentActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText("Booking Successful!")
                .setContentText("Appointments have been submitted successfully! 🎉\n\n" +
                        "📊 Students Selected: " + selectedCount + "\n" +
                        "You will receive a notification once the hospital responds to your booking request.")
                .setConfirmText("Great!")
                .setConfirmClickListener(sweetAlertDialog -> {
                    sweetAlertDialog.dismissWithAnimation();
                    finish();
                })
                .show();
    }
}
