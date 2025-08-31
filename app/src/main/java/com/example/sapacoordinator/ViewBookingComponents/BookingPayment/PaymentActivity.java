package com.example.sapacoordinator.ViewBookingComponents.BookingPayment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Parcelable;
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
import androidx.fragment.app.FragmentTransaction;

import com.example.sapacoordinator.DatabaseConnector.ApiClient;
import com.example.sapacoordinator.DatabaseConnector.ApiInterface;
import com.example.sapacoordinator.DatabaseConnector.GenericResponse;
import com.example.sapacoordinator.R;
import com.example.sapacoordinator.SchoolComponents.StudentsComponents.Student;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentActivity extends AppCompatActivity {

    private TextView tvTotalStudents;
    private TextView tvTotalAmount;
    private TextView tvTrainingTime;
    private TextView tvTrainingDate;
    private Button btnConfirmPayment;

    private ArrayList<Parcelable> selectedStudents;
    private PaymentList paymentListFragment;

    // Booking details
    private int schoolId;
    private int hospitalId;
    private int departmentId;
    private int dateSlotId;
    private int timeSlotId;
    private String trainingDate, trainingTime;
    private double pricePerStudent = 50.0; // Default price, can be passed from intent

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_payment);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.payment_activity), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
        loadDataFromIntent();
        setupPaymentSummary();
        loadPaymentListFragment();
        setupConfirmButton();
    }

    private void initializeViews() {
        tvTotalStudents = findViewById(R.id.tvTotalStudents);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        tvTrainingDate = findViewById(R.id.tvTrainingDate);
        tvTrainingTime = findViewById(R.id.tvTimeSlot);
        btnConfirmPayment = findViewById(R.id.btnConfirmPayment);
    }

    private void loadDataFromIntent() {
        // Get selected students from intent
        selectedStudents = getIntent().getParcelableArrayListExtra("selected_students");
        if (selectedStudents == null) {
            selectedStudents = new ArrayList<>();
        }

        // Get booking details
        schoolId = getIntent().getIntExtra("school_id", -1);
        hospitalId = getIntent().getIntExtra("hospital_id", -1);
        departmentId = getIntent().getIntExtra("department_id", -1);
        dateSlotId = getIntent().getIntExtra("date_slot_id", -1);
        timeSlotId = getIntent().getIntExtra("time_slot_id", -1);
        pricePerStudent = getIntent().getDoubleExtra("price_per_student", 50.0);
        // Set current date as training date (or get from intent if available)
        trainingDate = getIntent().getStringExtra("training_date");
        trainingTime = getIntent().getStringExtra("time_slot");
    }

    private void setupPaymentSummary() {
        int totalStudents = selectedStudents.size();
        double totalAmount = totalStudents * pricePerStudent;

        tvTotalStudents.setText(String.valueOf(totalStudents));
        tvTotalAmount.setText(String.format(Locale.getDefault(), "$%.2f", totalAmount));


        if (trainingDate == null || trainingDate.isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            trainingDate = sdf.format(new Date());
        }
        tvTrainingDate.setText(trainingDate);
        tvTrainingTime.setText(trainingTime);
    }

    private void loadPaymentListFragment() {
        if (selectedStudents != null && !selectedStudents.isEmpty()) {
            paymentListFragment = PaymentList.newInstance(selectedStudents);

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragmentContainer, paymentListFragment);
            transaction.commit();
        }
    }

    private void setupConfirmButton() {
        btnConfirmPayment.setOnClickListener(v -> {
            if (selectedStudents.isEmpty()) {
                Toast.makeText(this, "No students selected for payment", Toast.LENGTH_SHORT).show();
                return;
            }

            showPaymentConfirmationDialog();
        });
    }

    private void showPaymentConfirmationDialog() {
        double totalAmount = selectedStudents.size() * pricePerStudent;

        new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Confirm Payment")
                .setContentText(String.format(Locale.getDefault(),
                        "Total Amount: $%.2f\nStudents: %d\n\nProceed with payment?",
                        totalAmount, selectedStudents.size()))
                .setConfirmText("Pay Now")
                .setCancelText("Pay Later")
                .setConfirmClickListener(dialog -> {
                    dialog.dismissWithAnimation();
                    processPayment();
                })
                .setCancelClickListener(SweetAlertDialog::dismissWithAnimation)
                .show();
    }

    private void processPayment() {
        // Show loading
        btnConfirmPayment.setEnabled(false);
        btnConfirmPayment.setText("Processing Payment...");

        // Simulate payment processing (replace with actual payment logic)
        new android.os.Handler().postDelayed(() -> {
            // After successful payment processing, submit the booking
            submitBookingToAPI();
        }, 2000);
    }

    private void submitBookingToAPI() {
        // Extract student IDs from selected students
        List<Integer> studentIds = new ArrayList<>();
        for (Parcelable parcelable : selectedStudents) {
            if (parcelable instanceof Student) {
                Student student = (Student) parcelable;
                studentIds.add(student.getStudentId());
            }
        }

        ApiInterface api = ApiClient.getClient().create(ApiInterface.class);
        String studentIdsJson = new Gson().toJson(studentIds);
        Call<GenericResponse> call = api.bookAppointment(schoolId, hospitalId, departmentId, dateSlotId, timeSlotId, studentIdsJson);


        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<GenericResponse> call, @NonNull Response<GenericResponse> response) {
                // Reset button state
                btnConfirmPayment.setEnabled(true);
                btnConfirmPayment.setText("Confirm Payment");

                if (response.isSuccessful() && response.body() != null) {
                    GenericResponse genericResponse = response.body();

                    boolean isSuccessful = genericResponse.isSuccess() ||
                                         (genericResponse.getMessage() != null &&
                                          genericResponse.getMessage().toLowerCase().contains("success")) ||
                                         (genericResponse.getMessage() != null &&
                                          genericResponse.getMessage().toLowerCase().contains("booked"));

                    if (isSuccessful) {
                        showPaymentSuccessDialog();
                    } else {
                        handleBookingError("Booking failed: " + genericResponse.getMessage());
                    }
                } else {
                    handleBookingError("API response failed: " + response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<GenericResponse> call, @NonNull Throwable t) {
                // Reset button state
                btnConfirmPayment.setEnabled(true);
                btnConfirmPayment.setText("Confirm Payment");

                Log.e("PaymentActivity", "❌ Network error during booking", t);
                handleBookingError("Network error: " + t.getMessage());
            }
        });
    }

    private void handleBookingError(String errorMessage) {
        Log.e("PaymentActivity", errorMessage);

        // Use SweetAlertDialog for error message
        new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText("Booking Failed!")
                .setContentText("Unable to complete your booking request.\n\n" +
                        errorMessage + "\n\n")
                .setConfirmText("OK")
                .show();
    }

    private void showError(String message) {
        new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText("Error")
                .setContentText(message)
                .setConfirmText("OK")
                .setConfirmClickListener(SweetAlertDialog::dismissWithAnimation)
                .show();
    }

    private void showPaymentSuccessDialog() {
        new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText("Payment Successful!")
                .setContentText("Your payment has been processed successfully. " +
                        "The booking is now confirmed!")
                .setConfirmText("Done")
                .setConfirmClickListener(dialog -> {
                    dialog.dismissWithAnimation();
                    finish(); // Close activity
                })
                .show();
    }
}