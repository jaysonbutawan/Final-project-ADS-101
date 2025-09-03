package com.example.sapacoordinator.ViewBookingComponents.BookingPayment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

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
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FinalPayment extends AppCompatActivity {

    // Bill payment parameters
    private int appointmentId, billId, schoolId;
    private String billReference, schoolName, schoolAddress, schoolContact;
    private double totalAmount;

    // UI Components
    private TextView tvTotalAmount;
    private Button btnPayNow, btnBack;
    private TextInputLayout amountInputLayout;
    private TextInputEditText amountInput;

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

        getBillDataFromIntent();
        initializeViews();
        setupListeners();
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

        Log.d("FinalPayment", "Received bill data - ID: " + billId +
                ", Amount: " + totalAmount + ", Appointment ID: " + appointmentId);
    }

    private void initializeViews() {
        tvTotalAmount = findViewById(R.id.totalAmount);
        btnPayNow = findViewById(R.id.btnPayNow);
        btnBack = findViewById(R.id.btnBack);
        amountInputLayout = findViewById(R.id.amountInputLayout);
        amountInput = findViewById(R.id.amountInput);

        // Set total amount
        tvTotalAmount.setText(formatAmount(totalAmount));
        amountInput.setText(String.format("%.2f", totalAmount));

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Payment - " +
                    (billReference != null ? billReference : "Bill"));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupListeners() {
        btnPayNow.setOnClickListener(v -> showPaymentConfirmation());
        btnBack.setOnClickListener(v -> navigateBackToChooseAction());
    }

    private void showPaymentConfirmation() {
        new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Confirm Payment")
                .setContentText("Pay bill " + billReference + " for " + formatAmount(totalAmount) + "?")
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
        String amountStr = amountInput.getText() != null ? amountInput.getText().toString().trim() : "";
        double paymentAmount;

        try {
            paymentAmount = Double.parseDouble(amountStr);
            if (paymentAmount <= 0) {
                amountInputLayout.setError("Please enter a valid amount");
                return;
            }
        } catch (NumberFormatException e) {
            amountInputLayout.setError("Please enter a valid amount");
            return;
        }

        SweetAlertDialog loadingDialog = createLoadingDialog("Processing Payment...", "Please wait while we process your payment");
        loadingDialog.show();

        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<GenericResponse> call = apiInterface.payBill(billId, paymentAmount);

        call.enqueue(new Callback<GenericResponse>() {
            @Override
            public void onResponse(@NonNull Call<GenericResponse> call, @NonNull Response<GenericResponse> response) {
                loadingDialog.dismissWithAnimation();
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    showPaymentSuccess();
                } else {
                    showPaymentError(response.body() != null ? response.body().getMessage() : "Failed to process payment");
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
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void setupBackNavigation() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navigateBackToChooseAction();
            }
        });
    }

    // Utility helpers
    private SweetAlertDialog createLoadingDialog(String title, String message) {
        SweetAlertDialog dialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        dialog.setTitleText(title);
        dialog.setContentText(message);
        dialog.setCancelable(false);
        return dialog;
    }

    private String formatAmount(double amount) {
        return String.format("$%.2f", amount);
    }
}
