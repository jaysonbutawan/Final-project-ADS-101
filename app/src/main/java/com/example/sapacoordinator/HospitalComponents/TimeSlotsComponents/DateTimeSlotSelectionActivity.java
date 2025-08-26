package com.example.sapacoordinator.HospitalComponents.TimeSlotsComponents;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sapacoordinator.R;

public class DateTimeSlotSelectionActivity extends AppCompatActivity
        implements DateSlotList.OnDateSelectedListener, TimeSlotList.OnTimeSlotSelectedListener {

    private int departmentId;
    private int schoolId = -1; // You'll need to pass this from previous activity
    private int selectedDateSlotId = -1;
    private int selectedTimeSlotId = -1;
    private int selectedTimeSlotCapacity = 0;
    private  int selectedBookedCount = 0;
    private Button btnBookAppointment;
    private int hospitalId =-1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_date_slot_selection);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.date_timeselection), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        departmentId = getIntent().getIntExtra("department_id", -1);
        schoolId = getIntent().getIntExtra("school_id", -1);
        hospitalId =getIntent().getIntExtra("hospital_id",-1);

        // ✅ Log the received values immediately
        Log.d("DEBUG_", "DateTimeSlotSelection onCreate:");
        Log.d("DEBUG_", "  Received departmentId: " + departmentId);
        Log.d("DEBUG_", "  Received schoolId: " + schoolId);
        Log.d("DEBUG_", "  Received hospitalId----------------------------------: " + hospitalId);

        // Initialize button
        btnBookAppointment = findViewById(R.id.btnBookAppointment);
        btnBookAppointment.setEnabled(false); // Disabled until both date and time are selected

        // ✅ Update button state after getting intent data
        updateBookButtonState();

        btnBookAppointment.setOnClickListener(v -> {
            // Check specific missing selections and provide detailed feedback
            if (selectedDateSlotId == -1 && selectedTimeSlotId == -1) {
                Toast.makeText(this, "Please select a date and time slot first", Toast.LENGTH_LONG).show();
                return;
            }

            if (selectedDateSlotId == -1) {
                Toast.makeText(this, "Please select a date first", Toast.LENGTH_SHORT).show();
                return;
            }

            if (selectedTimeSlotId == -1) {
                Toast.makeText(this, "Please select a time slot first", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isBookingDataValid()) {
                proceedToStudentSelection();
            } else {
                Toast.makeText(this, "Please ensure all booking information is complete", Toast.LENGTH_SHORT).show();
            }
        });

        if (savedInstanceState == null) {
            DateSlotList dateSlotListFragment = DateSlotList.newInstance(departmentId);
            dateSlotListFragment.setCallback(this);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.dateSlotContainer, dateSlotListFragment)
                    .commit();
        }
    }

    @Override
    public void onDateSelected(int dateSlotId) {
        Log.d("DEBUG_", "Selected dateSlotId: " + dateSlotId);
        selectedDateSlotId = dateSlotId;
        // Reset time slot selection when date changes
        selectedTimeSlotId = -1;
        selectedTimeSlotCapacity = 0; // Reset capacity when date changes
        updateBookButtonState();

        TimeSlotList existingFragment = (TimeSlotList) getSupportFragmentManager()
                .findFragmentById(R.id.timeSlotContainer);

        if (existingFragment != null) {
            existingFragment.updateTimeSlots(dateSlotId);
        } else {
            TimeSlotList timeslotList = TimeSlotList.newInstance(dateSlotId);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.timeSlotContainer, timeslotList)
                    .commit();
        }
    }

    @Override
    public void onTimeSlotSelected(int timeSlotId) {
        Log.d("DEBUG_", "Selected timeSlotId: " + timeSlotId);
        selectedTimeSlotId = timeSlotId;

        // Get the selected time slot capacity from the TimeSlotList fragment
        TimeSlotList timeSlotFragment = (TimeSlotList) getSupportFragmentManager().findFragmentById(R.id.timeSlotContainer);
        if (timeSlotFragment != null) {
            selectedTimeSlotCapacity = timeSlotFragment.getSelectedTimeSlotCapacity(timeSlotId);
            Log.d("DEBUG_", "Selected time slot capacity: " + selectedTimeSlotCapacity);
        }

        updateBookButtonState();
    }

    private boolean isBookingDataValid() {
        // Add debug logging to see what values we have
        Log.d("DEBUG_", "Checking booking data:");
        Log.d("DEBUG_", "departmentId: " + departmentId);
        Log.d("DEBUG_", "schoolId: " + schoolId);
        Log.d("DEBUG_", "selectedDateSlotId: " + selectedDateSlotId);
        Log.d("DEBUG_", "selectedTimeSlotId: " + selectedTimeSlotId);

        boolean isValid = departmentId != -1 &&
                schoolId != -1 &&
                selectedDateSlotId != -1 &&
                selectedTimeSlotId != -1;

        Log.d("DEBUG_", "Is valid: " + isValid);
        return isValid;
    }

    private void updateBookButtonState() {
        // ✅ Include school_id in button state validation
        boolean hasValidData = selectedDateSlotId != -1 &&
                              selectedTimeSlotId != -1 &&
                              schoolId != -1 &&
                              departmentId != -1;

        btnBookAppointment.setEnabled(hasValidData);

        Log.d("DEBUG_", "Button state updated:");
        Log.d("DEBUG_", "  hasValidData: " + hasValidData);
        Log.d("DEBUG_", "  schoolId: " + schoolId);
        Log.d("DEBUG_", "  departmentId: " + departmentId);
        Log.d("DEBUG_", "  selectedDateSlotId: " + selectedDateSlotId);
        Log.d("DEBUG_", "  selectedTimeSlotId: " + selectedTimeSlotId);
    }

    private void proceedToStudentSelection() {
        Intent intent = new Intent(this, SelectStudentActivity.class);
        intent.putExtra("school_id", schoolId);
        intent.putExtra("hospital_id", hospitalId);
        intent.putExtra("department_id", departmentId);
        intent.putExtra("date_slot_id", selectedDateSlotId);  // Fixed: correct key name
        intent.putExtra("time_slot_id", selectedTimeSlotId);
        intent.putExtra("capacity", selectedTimeSlotCapacity);
        intent.putExtra("booked_count",selectedBookedCount);// Fixed: correct key name

        Log.d("BookingData", "Proceeding with: school_id=" + schoolId +
                ", department_id=" + departmentId +
                ", date_slot_id=" + selectedDateSlotId +
                ", time_slot_id=" + selectedTimeSlotId +
                ", capacity=" + selectedTimeSlotCapacity);

        startActivity(intent);
    }
}
