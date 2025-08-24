package com.example.sapacoordinator.SchoolComponents.StudentsComponents;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;

import com.example.sapacoordinator.R;

public class StudentActivity extends AppCompatActivity {

    private StudentList studentListFragment;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_student);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.studentsAc), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get school_id from intent
        int schoolId = getIntent().getIntExtra("school_id", -1);

        if (savedInstanceState == null) {
            studentListFragment = StudentList.newInstance(schoolId);

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.emptyState, studentListFragment);
            transaction.commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // ✅ Refresh the student list when returning to this activity
        if (studentListFragment != null) {
            studentListFragment.refreshStudents();
        }
    }
}