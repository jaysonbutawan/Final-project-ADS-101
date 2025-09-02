package com.example.sapacoordinator.ViewBookingComponents.BookingPayment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sapacoordinator.DatabaseConnector.ApiClient;
import com.example.sapacoordinator.DatabaseConnector.ApiInterface;
import com.example.sapacoordinator.DatabaseConnector.GenericResponse;
import com.example.sapacoordinator.R;
import com.example.sapacoordinator.SchoolComponents.ChooseAction;

import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FinalPayment extends AppCompatActivity {

    // Bill payment parameters
    private int appointmentId;
    private int billId;
    private String billReference;
    private double totalAmount;
    private int schoolId;
    private String schoolName;
    private String schoolAddress;
    private String schoolContact;

    // UI Components
    private TextView tvTotalAmount;
    private Button btnPayNow;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_final_payment);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.final_payment), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get bill data from intent
        getBillDataFromIntent();

        // Initialize UI components
        initializeViews();

        // Setup payment functionality
        setupPaymentButtons();

        // Setup back navigation
        setupBackNavigation();
    }

    private void getBillDataFromIntent() {
        Intent intent = getIntent();
        appointmentId = intent.getIntExtra("appointment_id", -1);
        billId = intent.getIntExtra("bill_id", -1);
        billReference = intent.getStringExtra("bill_reference");
        totalAmount = intent.getDoubleExtra("total_amount", 0.0);
        schoolId = intent.getIntExtra("school_id", -1);
        schoolName = intent.getStringExtra("school_name");
        schoolAddress = intent.getStringExtra("school_address");
        schoolContact = intent.getStringExtra("school_contact");

        Log.d("FinalPayment", "Received bill data - ID: " + billId + ", Amount: " + totalAmount + ", Appointment ID: " + appointmentId);
    }

    private void initializeViews() {
        tvTotalAmount = findViewById(R.id.totalAmount);
        btnPayNow = findViewById(R.id.btnPayNow);
        // Set the total amount
        tvTotalAmount.setText(String.format("$%.2f", totalAmount));

        // Set title if available
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Payment - " + (billReference != null ? billReference : "Bill"));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupPaymentButtons() {
        // Pay Now button functionality
        btnPayNow.setOnClickListener(v -> {
            showPaymentConfirmation();
        });

    }

    private void showPaymentConfirmation() {
        new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Confirm Payment")
                .setContentText("Pay bill " + billReference + " for " + String.format("$%.2f", totalAmount) + "?")
                .setConfirmText("Pay Now")
                .setCancelText("Cancel")
                .setConfirmClickListener(sweetAlertDialog -> {
                    sweetAlertDialog.dismissWithAnimation();
                    processBillPayment();
                })
                .setCancelClickListener(SweetAlertDialog::dismissWithAnimation)
                .show();
    }

    private void processBillPayment() {
        // Show loading dialog
        SweetAlertDialog loadingDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        loadingDialog.setTitleText("Processing Payment...");
        loadingDialog.setContentText("Please wait while we process your payment");
        loadingDialog.setCancelable(false);
        loadingDialog.show();

        // Call the API to process payment
        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<GenericResponse> call = apiInterface.payBill(billId);

        call.enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(@NonNull Call<GenericResponse> call, @NonNull Response<GenericResponse> response) {
                loadingDialog.dismissWithAnimation();

                if (response.isSuccessful() && response.body() != null) {
                    if (response.body().isSuccess()) {
                        showPaymentSuccess();
                    } else {
                        showPaymentError(response.body().getMessage());
                    }
                } else {
                    showPaymentError("Failed to process payment. Please try again.");
                }
            }

            @Override
            public void onFailure(@NonNull Call<GenericResponse> call, @NonNull Throwable t) {
                loadingDialog.dismissWithAnimation();
                Log.e("FinalPayment", "Payment failed", t);
                showPaymentError("Payment failed: " + t.getMessage());
            }
        });
    }

    private void showPaymentSuccess() {
        new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText("Payment Successful!")
                .setContentText("Bill " + billReference + " has been paid successfully.")
                .setConfirmText("OK")
                .setConfirmClickListener(sweetAlertDialog -> {
                    sweetAlertDialog.dismissWithAnimation();
                    navigateBackToChooseAction();
                })
                .show();
    }

    private void showPaymentError(String errorMessage) {
        new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText("Payment Failed")
                .setContentText(errorMessage)
                .setConfirmText("OK")
                .show();
    }

    private void navigateBackToChooseAction() {
        Intent intent = new Intent(FinalPayment.this, ChooseAction.class);
        intent.putExtra("school_id", schoolId);
        intent.putExtra("school_name", schoolName);
        intent.putExtra("school_address", schoolAddress);
        intent.putExtra("school_contact", schoolContact);
        startActivity(intent);
        finish();
    }

    private void setupBackNavigation() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navigateBackToChooseAction();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }


}