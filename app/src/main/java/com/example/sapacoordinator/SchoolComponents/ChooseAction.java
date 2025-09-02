package com.example.sapacoordinator.SchoolComponents;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sapacoordinator.BillComponents.Bill;
import com.example.sapacoordinator.BillComponents.BillAdapter;
import com.example.sapacoordinator.BillComponents.BillListActivity;
import com.example.sapacoordinator.DatabaseConnector.ApiClient;
import com.example.sapacoordinator.DatabaseConnector.ApiInterface;
import com.example.sapacoordinator.DatabaseConnector.GenericResponse;
import com.example.sapacoordinator.HospitalComponents.HospitalActivity;
import com.example.sapacoordinator.R;
import com.example.sapacoordinator.SchoolComponents.StudentsComponents.StudentActivity;
import com.example.sapacoordinator.SchoolComponents.StudentsComponents.StudentsRegistration;
import com.example.sapacoordinator.ViewBookingComponents.BookingPayment.FinalPayment;
import com.example.sapacoordinator.ViewBookingComponents.ChooseActionBooking;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ChooseAction extends AppCompatActivity implements BillAdapter.OnBillClickListener {

    private String schoolName, schoolAddress, schoolContact;
    private int schoolId;
    private int userId;
    private TextView tvStudentsCount, tvAppointmentsCount;

    private RecyclerView rvBills;
    private TextView tvEmptyMessage, tvViewAll;
    private BillAdapter billAdapter;
    private List<Bill> billList;

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

        TextView tvSchoolName = findViewById(R.id.tvSchoolName);
        TextView tvAddress = findViewById(R.id.tvAddress);
        TextView tvContact = findViewById(R.id.tvContact);
        tvStudentsCount = findViewById(R.id.tvStudentsCount);
        tvAppointmentsCount = findViewById(R.id.tvappointmentsCount);

        if (schoolName != null) tvSchoolName.setText(schoolName);
        if (schoolAddress != null) tvAddress.setText(schoolAddress);
        if (schoolContact != null) tvContact.setText(schoolContact);

        // Initialize bill container
        initializeBillContainer();

        // Handle Add Student Button
        Button btnAddStudent = findViewById(R.id.btnAddStudent);
        btnAddStudent.setOnClickListener(v -> {
            Intent intent = new Intent(ChooseAction.this, StudentsRegistration.class);
            intent.putExtra("school_id", schoolId);
            intent.putExtra("school_name", schoolName);
            intent.putExtra("school_address", schoolAddress);
            intent.putExtra("school_contact", schoolContact);
            startActivity(intent);
        });

        Button bookAppointment = findViewById(R.id.btnBookAppointment);
        bookAppointment.setOnClickListener(v -> {
            Intent intent = new Intent(ChooseAction.this, HospitalActivity.class);
            intent.putExtra("school_id", schoolId);
            intent.putExtra("user_id", userId);
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
        loadBills(); // Load bills into the container

        // Handle back navigation using OnBackPressedDispatcher
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(ChooseAction.this, activity_register_school.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }


    private void initializeBillContainer() {
        // Bill-related components
        FrameLayout billContainer = findViewById(R.id.billContainer);

        // Inflate the bill list layout into the container
        View billListView = LayoutInflater.from(this).inflate(R.layout.fragment_bill_list, billContainer, false);
        billContainer.addView(billListView);

        // Initialize bill components
        rvBills = billListView.findViewById(R.id.rvBills);
        tvEmptyMessage = billListView.findViewById(R.id.tvEmptyMessage);
        tvViewAll = billListView.findViewById(R.id.tvViewAll);

        // Setup RecyclerView
        billList = new ArrayList<>();
        billAdapter = new BillAdapter(billList, this);
        billAdapter.setOnBillClickListener(this);

        rvBills.setLayoutManager(new LinearLayoutManager(this));
        rvBills.setAdapter(billAdapter);

        // Handle "View All" click
        tvViewAll.setOnClickListener(v -> {
            Intent intent = new Intent(ChooseAction.this, BillListActivity.class);
            intent.putExtra("school_id", schoolId);
            intent.putExtra("school_name", schoolName);
            startActivity(intent);
        });
    }

    private void loadBills() {
        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<List<Bill>> call = apiInterface.getBillsBySchool(schoolId);

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<Bill>> call, @NonNull Response<List<Bill>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Bill> bills = response.body();

                    if (bills.isEmpty()) {
                        showEmptyBillState();
                    } else {
                        hideEmptyBillState();
                        billList.clear();
                        // Show only first 3 bills in the overview, or all if less than 3
                        List<Bill> displayBills = bills.size() > 3 ? bills.subList(0, 3) : bills;
                        billList.addAll(displayBills);
                        billAdapter.updateBills(billList);

                        // Show/hide "View All" based on bill count
                        tvViewAll.setVisibility(bills.size() > 3 ? View.VISIBLE : View.GONE);
                    }
                } else {
                    Log.e("ChooseAction", "Failed to load bills: " + response.message());
                    showEmptyBillState();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Bill>> call, @NonNull Throwable t) {
                Log.e("ChooseAction", "Error loading bills", t);
                showEmptyBillState();
            }
        });
    }

    private void showEmptyBillState() {
        rvBills.setVisibility(View.GONE);
        tvEmptyMessage.setVisibility(View.VISIBLE);
        tvViewAll.setVisibility(View.GONE);
    }

    private void hideEmptyBillState() {
        rvBills.setVisibility(View.VISIBLE);
        tvEmptyMessage.setVisibility(View.GONE);
    }

    // Implement BillAdapter.OnBillClickListener methods
    @Override
    public void onPayNowClick(Bill bill) {

        navigateToFinalPayment(bill);
    }

    @Override
    public void onViewReceiptClick(Bill bill) {
        new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText("Receipt")
                .setContentText("Receipt for bill " + bill.getBillReference() +
                               "\nAmount: " + bill.getFormattedAmount() +
                               "\nPaid on: " + bill.getPaidDate())
                .setConfirmText("OK")
                .show();
    }

    private void navigateToFinalPayment(Bill bill) {
        Intent intent = new Intent(ChooseAction.this, FinalPayment.class);
        intent.putExtra("appointment_id", bill.getAppointmentId());
        intent.putExtra("bill_id", bill.getBillId());
        intent.putExtra("bill_reference", bill.getBillReference());
        intent.putExtra("total_amount", bill.getTotalAmount());
        intent.putExtra("school_id", bill.getSchoolId());
        intent.putExtra("school_name", schoolName);
        intent.putExtra("school_address", schoolAddress);
        intent.putExtra("school_contact", schoolContact);
        startActivity(intent);
    }

    private void fetchStudentCount() {
        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<GenericResponse> call = apiInterface.getStudentCount(userId, schoolId);

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<GenericResponse> call, @NonNull Response<GenericResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    tvStudentsCount.setText(String.valueOf(response.body().getStudent_count()));
                    Log.d("COUNT_DEBUG", "Student count response: " + response.body().getStudent_count());
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
