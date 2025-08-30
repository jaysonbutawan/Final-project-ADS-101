package com.example.sapacoordinator.HospitalComponents.TimeSlotsComponents;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
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
    private String selectedTimeSlotText = "";
    private String getSelectedDateSlotText="";


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

        schoolId = getIntent().getIntExtra("school_id", -1);
        hospitalId = getIntent().getIntExtra("hospital_id", -1);
        departmentId = getIntent().getIntExtra("department_id", -1);
        dateSlotId = getIntent().getIntExtra("date_slot_id", -1);
        timeSlotId = getIntent().getIntExtra("time_slot_id", -1);
        maxCapacity = getIntent().getIntExtra("capacity", 0);
        selectedTimeSlotText = getIntent().getStringExtra("time_slot");
        getSelectedDateSlotText = getIntent().getStringExtra("training_date");
        currentBookedCount = getIntent().getIntExtra("booked_count", 0);

        availableSlots = maxCapacity - currentBookedCount;


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

                    for (TimeSlotModel slot : timeSlots) {
                        if (slot.getTime_slot_id() == timeSlotId) {
                            currentBookedCount = slot.getBooked_count();
                            availableSlots = maxCapacity - currentBookedCount;
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
        List<Student> selectedStudents = new ArrayList<>();

        // Get the actual Student objects for the selected IDs
        for (Integer studentId : selectedStudentIds) {
            for (Student student : studentList) {
                if (student.getStudentId() == studentId) {
                    selectedStudents.add(student);
                    break;
                }
            }
        }

        Log.d("DEBUG_", "Proceeding to payment with:");
        Log.d("DEBUG_", "Selected students: " + selectedStudents.size());
        Log.d("DEBUG_", "Student IDs: " + selectedStudentIds.toString());

        // Navigate to PaymentActivity with all the booking data
        Intent paymentIntent = new Intent(this, com.example.sapacoordinator.ViewBookingComponents.BookingPayment.PaymentActivity.class);

        // Pass selected students as parcelable list
        paymentIntent.putParcelableArrayListExtra("selected_students", new ArrayList<>(selectedStudents));

        // Pass all booking details
        paymentIntent.putExtra("school_id", schoolId);
        paymentIntent.putExtra("hospital_id", hospitalId);
        paymentIntent.putExtra("department_id", departmentId);
        paymentIntent.putExtra("date_slot_id", dateSlotId);
        paymentIntent.putExtra("time_slot_id", timeSlotId);
        paymentIntent.putExtra("max_capacity", maxCapacity);
        paymentIntent.putExtra("current_booked_count", currentBookedCount);
        paymentIntent.putExtra("available_slots", availableSlots);
        paymentIntent.putExtra("price_per_student", 50.0); // Set your price here

        // Optional: Pass additional booking details for display
        paymentIntent.putExtra("training_date", getSelectedDateSlotText);
        paymentIntent.putExtra("time_slot", selectedTimeSlotText);
        Log.d("DEBUG_", "Selected Time Slot:proceed to selection " + selectedTimeSlotText);
        Log.d("DEBUG_", "Selected date proceed to selection " + getSelectedDateSlotText);
        Log.d("SelectStudentActivity", "🚀 Navigating to PaymentActivity with:");
        Log.d("SelectStudentActivity", "   Students: " + selectedStudents.size());
        Log.d("SelectStudentActivity", "   Hospital ID: " + hospitalId);
        Log.d("SelectStudentActivity", "   School ID: " + schoolId);

        startActivity(paymentIntent);
    }

}
