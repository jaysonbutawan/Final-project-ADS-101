package com.example.sapacoordinator.SchoolComponents;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sapacoordinator.DatabaseConnector.ApiClient;
import com.example.sapacoordinator.DatabaseConnector.ApiInterface;
import com.example.sapacoordinator.DatabaseConnector.GenericResponse;
import com.example.sapacoordinator.HospitalComponents.HospitalActivity;
import com.example.sapacoordinator.HospitalComponents.HospitalList;
import com.example.sapacoordinator.R;
import com.example.sapacoordinator.SchoolComponents.StudentsComponents.StudentActivity;
import com.example.sapacoordinator.SchoolComponents.StudentsComponents.StudentsRegistration;
import com.example.sapacoordinator.ViewBookingComponents.ChooseActionBooking;
import com.example.sapacoordinator.ViewBookingComponents.ViewBooking.ViewBookingActivity;
import com.example.sapacoordinator.ViewBookingComponents.ViewBooking.ViewBookingList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ChooseAction extends AppCompatActivity {

    private String schoolName, schoolAddress, schoolContact;
    private int schoolId;
    private int userId;
    private TextView tvStudentsCount, tvAppointmentsCount;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_choose_action);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.choose_action), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get current logged-in user_id from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);

        // Get school data from Intent
        Intent receivedIntent = getIntent();
        schoolName = receivedIntent.getStringExtra("school_name");
        schoolAddress = receivedIntent.getStringExtra("school_address");
        schoolContact = receivedIntent.getStringExtra("school_contact");
        schoolId = receivedIntent.getIntExtra("school_id", -1);

        // ✅ Add debug logging to track school_id
        Log.d("DEBUG_", "Received school_id: " + schoolId);
        Log.d("DEBUG_", "Received school_name: " + schoolName);

        // Bind TextViews
        TextView tvSchoolName = findViewById(R.id.tvSchoolName);
        TextView tvAddress = findViewById(R.id.tvAddress);
        TextView tvContact = findViewById(R.id.tvContact);
        tvStudentsCount = findViewById(R.id.tvStudentsCount);
        tvAppointmentsCount = findViewById(R.id.tvappointmentsCount);

        if (schoolName != null) tvSchoolName.setText(schoolName);
        if (schoolAddress != null) tvAddress.setText(schoolAddress);
        if (schoolContact != null) tvContact.setText(schoolContact);

        // Handle Add Student Button
        Button btnAddStudent = findViewById(R.id.btnAddStudent);
        btnAddStudent.setOnClickListener(v -> {
            Intent intent = new Intent(ChooseAction.this, StudentsRegistration.class);
            intent.putExtra("school_id", schoolId);
            intent.putExtra("school_name", schoolName);
            intent.putExtra("school_address", schoolAddress);
            intent.putExtra("school_contact", schoolContact);
            Log.d("DEBUG_", "Navigating to StudentsRegistration with school_id: " + schoolId);
            startActivity(intent);
        });

        Button bookAppointment = findViewById(R.id.btnBookAppointment);
        bookAppointment.setOnClickListener(v -> {
            Intent intent = new Intent(ChooseAction.this, HospitalActivity.class);
            intent.putExtra("school_id", schoolId);
            intent.putExtra("user_id", userId);
            Log.d("DEBUG_", "Navigating to StudentsRegistration with school_id: " + schoolId);
            startActivity(intent);
        });

        Button firstbookAppointment = findViewById(R.id.btnContinueBooking);
        firstbookAppointment.setOnClickListener(v -> {
            Intent intent = new Intent(ChooseAction.this, HospitalActivity.class);
            intent.putExtra("school_id", schoolId);
            intent.putExtra("user_id", userId);
            Log.d("DEBUG_", "Navigating to StudentsRegistration with school_id: " + schoolId);
            startActivity(intent);
        });



        LinearLayout llStudentsCount = findViewById(R.id.studentsCountContainer);
        llStudentsCount.setOnClickListener(v -> {
            Intent intent = new Intent(ChooseAction.this, StudentActivity.class);
            intent.putExtra("user_id", userId);
            intent.putExtra("school_id", schoolId);
            startActivity(intent);
        });


        LinearLayout appointmentsCount = findViewById(R.id.appointmentsCountContainer);
        appointmentsCount.setOnClickListener(v -> {
            Intent intent = new Intent(ChooseAction.this, ChooseActionBooking.class);
            intent.putExtra("school_id", schoolId);
            startActivity(intent);


        });
        fetchBookingCount();
        fetchStudentCount();

    }

            private void fetchStudentCount() {
                ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
                Call<GenericResponse> call = apiInterface.getStudentCount(userId, schoolId);

                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<GenericResponse> call, @NonNull Response<GenericResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            tvStudentsCount.setText(String.valueOf(response.body().getStudent_count()));
                        } else {
                            tvStudentsCount.setText("0");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<GenericResponse> call, @NonNull Throwable t) {
                        tvStudentsCount.setText("0");
                        Toast.makeText(ChooseAction.this, "Failed to load count", Toast.LENGTH_SHORT).show();
                    }
                });
            }

    private void fetchBookingCount() {
        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<GenericResponse> call = apiInterface.getBookingCount(schoolId);

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<GenericResponse> call, @NonNull Response<GenericResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("COUNT_DEBUG", "Booking count response: " + response.body().getBooking_count());
                    tvAppointmentsCount.setText(String.valueOf(response.body().getBooking_count()));
                } else {
                    tvAppointmentsCount.setText("0");
                }
            }

            @Override
            public void onFailure(@NonNull Call<GenericResponse> call, @NonNull Throwable t) {
                tvAppointmentsCount.setText("0");
                Toast.makeText(ChooseAction.this, "Failed to load booking count", Toast.LENGTH_SHORT).show();
            }
        });
    }


}
