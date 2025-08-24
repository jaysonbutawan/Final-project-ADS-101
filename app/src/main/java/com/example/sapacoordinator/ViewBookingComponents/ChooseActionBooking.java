package com.example.sapacoordinator.ViewBookingComponents;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;

import com.example.sapacoordinator.HospitalComponents.HospitalList;
import com.example.sapacoordinator.R;

public class ChooseActionBooking extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.hospital_action_view_booking);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.choose_booking), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        int schoolId = getIntent().getIntExtra("school_id", -1);

        if (savedInstanceState == null) {
            HospitalList hospitalListFragment = HospitalList.newInstance(schoolId,true);

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.emptyState, hospitalListFragment);
            transaction.commit();

        }
    }
}