package com.example.sapacoordinator.HospitalComponents.TimeSlotsComponents;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sapacoordinator.R;

public class DateTimeSlotSelectionActivity extends AppCompatActivity
        implements DateSlotList.OnDateSelectedListener, TimeSlotList.OnTimeSlotSelectedListener {

    private int departmentId;
    private int schoolId = -1;
    private int selectedDateSlotId = -1;
    private int selectedTimeSlotId = -1;
    private int selectedTimeSlotCapacity = 0;
    private  int selectedBookedCount = 0;
    private Button btnBookAppointment;
    private int hospitalId =-1;
    private String selectedTrainingDate = "";
    private String selectedTimeSlotText = "";
    private String departmentmentName, hospitalName;
    private TextView tvdepartmentName, tvHospitalName;
    private double price;


    @SuppressLint("MissingInflatedId")
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
            departmentmentName = getIntent().getStringExtra("department_name");
            hospitalName = getIntent().getStringExtra("hospital_name");
            price = getIntent().getDoubleExtra("price_per_student", 0.0);

                tvdepartmentName = findViewById(R.id.tvDepartment);
                tvHospitalName = findViewById(R.id.tvHospitalName);
                tvdepartmentName.setText(departmentmentName);
                tvHospitalName.setText(hospitalName);

                btnBookAppointment = findViewById(R.id.btnBookAppointment);
                btnBookAppointment.setEnabled(false);

        updateBookButtonState();

        btnBookAppointment.setOnClickListener(v -> {
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
        selectedDateSlotId = dateSlotId;

        DateSlotList dateSlotFragment = (DateSlotList) getSupportFragmentManager()
                .findFragmentById(R.id.dateSlotContainer);
        if (dateSlotFragment != null) {
            selectedTrainingDate = dateSlotFragment.getSelectedDateString(dateSlotId);
        }

            selectedTimeSlotId = -1;
            selectedTimeSlotText = "";
            selectedTimeSlotCapacity = 0;

        updateBookButtonState();

        TimeSlotList timeslotList = TimeSlotList.newInstance(dateSlotId);
        timeslotList.setCallback(this);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.timeSlotContainer, timeslotList)
                .commit();
    }


    @Override
    public void onTimeSlotSelected(int timeSlotId) {
        selectedTimeSlotId = timeSlotId;

        TimeSlotList timeSlotFragment = (TimeSlotList) getSupportFragmentManager()
                .findFragmentById(R.id.timeSlotContainer);
        if (timeSlotFragment != null) {
            selectedTimeSlotCapacity = timeSlotFragment.getSelectedTimeSlotCapacity(timeSlotId);
            selectedTimeSlotText = timeSlotFragment.getSelectedTimeSlotText(timeSlotId);

        }

        updateBookButtonState();
    }

    private boolean isBookingDataValid() {
        return departmentId != -1 &&
                schoolId != -1 &&
                selectedDateSlotId != -1 &&
                selectedTimeSlotId != -1;
    }

    private void updateBookButtonState() {
        boolean hasValidData = selectedDateSlotId != -1 &&
                              selectedTimeSlotId != -1 &&
                              schoolId != -1 &&
                              departmentId != -1;

        btnBookAppointment.setEnabled(hasValidData);
    }

    private void proceedToStudentSelection() {
        Intent intent = new Intent(this, SelectStudentActivity.class);
                intent.putExtra("school_id", schoolId);
                intent.putExtra("hospital_id", hospitalId);
                intent.putExtra("department_id", departmentId);
                intent.putExtra("date_slot_id", selectedDateSlotId);
                intent.putExtra("time_slot_id", selectedTimeSlotId);
                intent.putExtra("capacity", selectedTimeSlotCapacity);
                intent.putExtra("booked_count",selectedBookedCount);
                intent.putExtra("training_date", selectedTrainingDate);
                intent.putExtra("time_slot", selectedTimeSlotText);
                intent.putExtra("price_per_student", price);

        startActivity(intent);
    }

}
