package com.example.sapacoordinator.ViewBookingComponents.ViewBooking;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;

import com.example.sapacoordinator.R;

public class ViewBookingActivity extends AppCompatActivity {

    private static final String TAG = "ViewBookingActivity";

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_booking);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.view_booking), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get parameters from intent
        int schoolId = getIntent().getIntExtra("school_id", -1);
        int hospitalId = getIntent().getIntExtra("hospital_id", -1);
        int departmentId = getIntent().getIntExtra("department_id", -1);

        // Log parameters for debugging
        Log.d(TAG, "Received parameters - schoolId: " + schoolId + ", hospitalId: " + hospitalId + ", departmentId: " + departmentId);

        // Validate parameters
        if (schoolId == -1) {
            Log.e(TAG, "Invalid school_id received");
            Toast.makeText(this, "Error: Invalid school ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load fragment only if savedInstanceState is null (first time)
        if (savedInstanceState == null) {
            try {
                ViewBookingList viewBookingList = ViewBookingList.newInstance(schoolId, hospitalId, departmentId);

                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, viewBookingList); // Fixed: Use correct container ID
                transaction.commit();

                Log.d(TAG, "Fragment loaded successfully");
            } catch (Exception e) {
                Log.e(TAG, "Error loading fragment: " + e.getMessage(), e);
                Toast.makeText(this, "Error loading bookings", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}