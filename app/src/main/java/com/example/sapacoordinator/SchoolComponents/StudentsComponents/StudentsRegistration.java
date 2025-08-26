package com.example.sapacoordinator.SchoolComponents.StudentsComponents;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sapacoordinator.DatabaseConnector.ApiClient;
import com.example.sapacoordinator.DatabaseConnector.ApiInterface;
import com.example.sapacoordinator.DatabaseConnector.GenericResponse;
import com.example.sapacoordinator.R;
import com.google.android.material.chip.ChipGroup;

import java.util.Calendar;

import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
public class StudentsRegistration extends AppCompatActivity {
    Button btnAddStudent;
    EditText etFirstName, etLastName, etPhoneNumber, etEmailAddress, etDateOfBirth;
    ChipGroup genderChipGroup;

    // Edit mode variables
    private boolean isEditMode = false;
    private int studentId = -1;
    private String studentCode = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_students_registration);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.student_registration), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etEmailAddress = findViewById(R.id.etEmailAddress);
        etDateOfBirth = findViewById(R.id.etDateOfBirth);
        btnAddStudent = findViewById(R.id.btnAddStudent);
        genderChipGroup = findViewById(R.id.genderChipGroup);

        // Check if we're in edit mode
        checkEditMode();

        // DateSlot picker
        etDateOfBirth.setOnClickListener(v -> {
            DatePickerDialog datePicker = new DatePickerDialog(this,
                    (view, year, month, dayOfMonth) -> {
                        String date = year + "-" + (month + 1) + "-" + dayOfMonth; // YYYY-MM-DD
                        etDateOfBirth.setText(date);
                    },
                    Calendar.getInstance().get(Calendar.YEAR),
                    Calendar.getInstance().get(Calendar.MONTH),
                    Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
            );
            datePicker.show();
        });

        // Button click
        btnAddStudent.setOnClickListener(v -> {
            String firstName = etFirstName.getText().toString().trim();
            String lastName = etLastName.getText().toString().trim();
            String phoneNumber = etPhoneNumber.getText().toString().trim();
            String email = etEmailAddress.getText().toString().trim();
            String dateOfBirth = etDateOfBirth.getText().toString().trim();
            String sex = "";
            SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
            int userId = prefs.getInt("user_id", -1);

            if (userId == -1) {
                showAddStudentError("User not logged in.");
                return;
            }
            int selectedGenderId = genderChipGroup.getCheckedChipId();

            if (selectedGenderId == R.id.chipMale) {
                sex = "Male";
            } else if (selectedGenderId == R.id.chipFemale) {
                sex = "Female";
            } else {
                new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Missing Selection")
                        .setContentText("Please select a gender before proceeding.")
                        .setConfirmText("OK")
                        .setConfirmClickListener(SweetAlertDialog::dismissWithAnimation)
                        .show();
                return; // Stop execution if not selected
            }

            int schoolId = getIntent().getIntExtra("school_id", -1);
            if (schoolId == -1) {
                showAddStudentError("Invalid school selected.");
                return;
            }

            if (isEditMode) {
                updateStudent(userId, firstName, lastName, phoneNumber, email, sex, dateOfBirth, schoolId);
            } else {
                addStudent(userId, firstName, lastName, phoneNumber, email, sex, dateOfBirth, schoolId);
            }
        });
    }

    private void checkEditMode() {
        isEditMode = getIntent().getBooleanExtra("edit_mode", false);

        if (isEditMode) {
            // Change button text and populate fields
            btnAddStudent.setText("Update Student");

            // Get student data from intent
            studentId = getIntent().getIntExtra("student_id", -1);
            studentCode = getIntent().getStringExtra("student_code");
            String firstname = getIntent().getStringExtra("firstname");
            String lastname = getIntent().getStringExtra("lastname");
            String phoneNumber = getIntent().getStringExtra("phone_number");
            String email = getIntent().getStringExtra("email");
            String sex = getIntent().getStringExtra("sex");
            int age = getIntent().getIntExtra("age", 0);

            // Populate fields
            if (firstname != null) etFirstName.setText(firstname);
            if (lastname != null) etLastName.setText(lastname);
            if (phoneNumber != null) etPhoneNumber.setText(phoneNumber);
            if (email != null) etEmailAddress.setText(email);

            // Set gender
            if ("Male".equals(sex)) {
                genderChipGroup.check(R.id.chipMale);
            } else if ("Female".equals(sex)) {
                genderChipGroup.check(R.id.chipFemale);
            }

            // Calculate and set birth date based on age (approximate)
            if (age > 0) {
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.YEAR, -age);
                String birthDate = cal.get(Calendar.YEAR) + "-01-01"; // Approximate date
                etDateOfBirth.setText(birthDate);
            }
        }
    }

    private void addStudent(int userId,String firstName, String lastName, String phoneNumber, String email, String sex, String dateOfBirth, int schoolId) {
        ApiInterface api = ApiClient.getClient().create(ApiInterface.class);

        Call<GenericResponse> call = api.registerStudent(userId,firstName, lastName, phoneNumber, email, sex, dateOfBirth, schoolId);

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<GenericResponse> call, @NonNull Response<GenericResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    handleAddStudentResponse(response.body());
                } else {
                    showAddStudentError("Server error: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<GenericResponse> call, @NonNull Throwable t) {
                showAddStudentError("Connection Error: " + t.getMessage());
            }
        });
    }

    private void handleAddStudentResponse(GenericResponse res) {
        if (res.isSuccess()) {
            new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText("Success")
                    .setContentText(res.getMessage())
                    .setConfirmClickListener(sDialog -> {
                        sDialog.dismissWithAnimation();
                        etFirstName.setText("");
                        etLastName.setText("");
                        etDateOfBirth.setText("");
                        etPhoneNumber.setText("");
                        etEmailAddress.setText("");
                        genderChipGroup.clearCheck();
                    })
                    .show();
        } else {
            new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Failed")
                    .setContentText(res.getMessage())
                    .show();
        }
    }

    private void showAddStudentError(String message) {
        new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText("Error")
                .setContentText(message)
                .show();
    }

    private void updateStudent(int userId, String firstName, String lastName, String phoneNumber, String email, String sex, String dateOfBirth, int schoolId) {
        ApiInterface api = ApiClient.getClient().create(ApiInterface.class);

        Call<GenericResponse> call = api.updateStudent(studentId, userId, firstName, lastName, phoneNumber, email, sex, dateOfBirth, schoolId);

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<GenericResponse> call, @NonNull Response<GenericResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    handleUpdateStudentResponse(response.body());
                } else {
                    showAddStudentError("Server error: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<GenericResponse> call, @NonNull Throwable t) {
                showAddStudentError("Connection Error: " + t.getMessage());
            }
        });
    }

    private void handleUpdateStudentResponse(GenericResponse res) {
        if (res.isSuccess()) {
            new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText("Success")
                    .setContentText("Student updated successfully!")
                    .setConfirmClickListener(sDialog -> {
                        sDialog.dismissWithAnimation();
                        finish(); // Go back to student list
                    })
                    .show();
        } else {
            new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Failed")
                    .setContentText(res.getMessage())
                    .show();
        }
    }
}
