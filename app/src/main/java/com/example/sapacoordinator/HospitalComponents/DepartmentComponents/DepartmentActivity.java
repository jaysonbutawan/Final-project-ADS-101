package com.example.sapacoordinator.HospitalComponents.DepartmentComponents;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;

import com.example.sapacoordinator.R;

public class DepartmentActivity extends AppCompatActivity {


    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_department);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.hospitalDepartment), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView tvHospitalName = findViewById(R.id.tvHospitalName);
        TextView address= findViewById(R.id.tvlocation);
        String hospitalName = getIntent().getStringExtra("hospital_name");
        String hospitalAddress = getIntent().getStringExtra("hospital_address");
        int hospitalId = getIntent().getIntExtra("hospital_id", -1);
        int schoolId = getIntent().getIntExtra("school_id", -1);

        if (tvHospitalName != null && hospitalName != null) {
            tvHospitalName.setText(hospitalName);
            address.setText(hospitalAddress);
        } else {
            assert tvHospitalName != null;
            tvHospitalName.setText("Hospital Name Not Available");
        }

        if (savedInstanceState == null) {
            DepartmentList departmentList = DepartmentList.newInstance(hospitalId, schoolId);

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.emptyState, departmentList);
            transaction.commit();
        }
    }
}
