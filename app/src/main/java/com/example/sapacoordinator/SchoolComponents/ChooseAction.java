package com.example.sapacoordinator.SchoolComponents;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import android.widget.FrameLayout;
import android.widget.TextView;

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
    private int schoolId, userId;

    private TextView tvSchoolName, tvAddress, tvContact, tvStudentsCount, tvAppointmentsCount;
    private RecyclerView rvBills;
    private TextView tvEmptyMessage, tvViewAll;
    private BillAdapter billAdapter;
    private final List<Bill> billList = new ArrayList<>();

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

        // Load user and school data
        userId = getSharedPreferences("UserSession", MODE_PRIVATE).getInt("user_id", -1);
        updateSchoolDataFromIntent(getIntent());

        // Setup UI
        tvSchoolName = findViewById(R.id.tvSchoolName);
        tvAddress = findViewById(R.id.tvAddress);
        tvContact = findViewById(R.id.tvContact);
        tvStudentsCount = findViewById(R.id.tvStudentsCount);
        tvAppointmentsCount = findViewById(R.id.tvappointmentsCount);

        updateSchoolUI();
        initializeBillContainer();
        setupClickListeners();

        refreshAllData();

        // Back navigation
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() {
                startActivity(new Intent(ChooseAction.this, activity_register_school.class)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                finish();
            }
        });
    }

    private void setupClickListeners() {
        findViewById(R.id.btnAddStudent).setOnClickListener(v ->
                startActivity(withSchoolExtras(new Intent(this, StudentsRegistration.class)))
        );

        findViewById(R.id.btnBookAppointment).setOnClickListener(v -> {
            Intent i = new Intent(this, HospitalActivity.class);
            i.putExtra("school_id", schoolId);
            i.putExtra("user_id", userId);
            startActivity(i);
        });

        findViewById(R.id.studentsCountContainer).setOnClickListener(v ->
                startActivity(new Intent(this, StudentActivity.class)
                        .putExtra("user_id", userId)
                        .putExtra("school_id", schoolId))
        );

        findViewById(R.id.appointmentsCountContainer).setOnClickListener(v ->
                startActivity(new Intent(this, ChooseActionBooking.class)
                        .putExtra("school_id", schoolId))
        );
    }

    private void initializeBillContainer() {
        FrameLayout billContainer = findViewById(R.id.billContainer);
        View billListView = LayoutInflater.from(this).inflate(R.layout.fragment_bill_list, billContainer, false);
        billContainer.addView(billListView);

        rvBills = billListView.findViewById(R.id.rvBills);
        tvEmptyMessage = billListView.findViewById(R.id.tvEmptyMessage);
        tvViewAll = billListView.findViewById(R.id.tvViewAll);

        billAdapter = new BillAdapter(billList, this);
        rvBills.setLayoutManager(new LinearLayoutManager(this));
        rvBills.setAdapter(billAdapter);

        tvViewAll.setOnClickListener(v ->
                startActivity(withSchoolExtras(new Intent(this, BillListActivity.class)))
        );
    }

    private void loadBills() {
        ApiClient.getClient().create(ApiInterface.class)
                .getBillsBySchool(schoolId)
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<List<Bill>> call, @NonNull Response<List<Bill>> resp) {
                        if (resp.isSuccessful() && resp.body() != null && !resp.body().isEmpty()) {
                            hideEmptyBillState();
                            billList.clear();
                            billList.addAll(resp.body().size() > 3 ? resp.body().subList(0, 3) : resp.body());
                            billAdapter.updateBills(billList);
                            tvViewAll.setVisibility(resp.body().size() > 3 ? View.VISIBLE : View.GONE);
                        } else showEmptyBillState();
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<Bill>> call, @NonNull Throwable t) {
                        Log.e("ChooseAction", "Error loading bills", t);
                        showEmptyBillState();
                    }
                });
    }

    private void fetchCounts() {
        ApiInterface api = ApiClient.getClient().create(ApiInterface.class);

        api.getStudentCount(userId, schoolId).enqueue(new Callback<>() {
            @Override public void onResponse(@NonNull Call<GenericResponse> call, @NonNull Response<GenericResponse> resp) {
                tvStudentsCount.setText(resp.isSuccessful() && resp.body() != null ?
                        String.valueOf(resp.body().getStudent_count()) : "0");
            }
            @Override public void onFailure(@NonNull Call<GenericResponse> call, @NonNull Throwable t) {
                tvStudentsCount.setText("0");
            }
        });

        api.getBookingCount(schoolId).enqueue(new Callback<>() {
            @Override public void onResponse(@NonNull Call<GenericResponse> call, @NonNull Response<GenericResponse> resp) {
                tvAppointmentsCount.setText(resp.isSuccessful() && resp.body() != null ?
                        String.valueOf(resp.body().getBooking_count()) : "0");
            }
            @Override public void onFailure(@NonNull Call<GenericResponse> call, @NonNull Throwable t) {
                tvAppointmentsCount.setText("0");
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

    @Override
    public void onPayNowClick(Bill bill) {
        startActivity(withSchoolExtras(new Intent(this, FinalPayment.class)
                .putExtra("appointment_id", bill.getAppointmentId())
                .putExtra("bill_id", bill.getBillId())
                .putExtra("bill_reference", bill.getBillReference())
                .putExtra("total_amount", bill.getTotalAmount())));
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

    private Intent withSchoolExtras(Intent intent) {
        return intent.putExtra("school_id", schoolId)
                .putExtra("school_name", schoolName)
                .putExtra("school_address", schoolAddress)
                .putExtra("school_contact", schoolContact);
    }

    private void updateSchoolDataFromIntent(Intent intent) {
        schoolId = intent.getIntExtra("school_id", -1);
        schoolName = intent.getStringExtra("school_name");
        schoolAddress = intent.getStringExtra("school_address");
        schoolContact = intent.getStringExtra("school_contact");
    }

    private void updateSchoolUI() {
        if (schoolName != null) tvSchoolName.setText(schoolName);
        if (schoolAddress != null) tvAddress.setText(schoolAddress);
        if (schoolContact != null) tvContact.setText(schoolContact);
    }

    private void refreshAllData() {
        if (schoolId == -1) {
            Log.e("ChooseAction", "Cannot refresh - invalid school ID");
            return;
        }
        fetchCounts();
        loadBills();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        updateSchoolDataFromIntent(intent);
        updateSchoolUI();
        refreshAllData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (schoolId != -1) refreshAllData();
    }
}
