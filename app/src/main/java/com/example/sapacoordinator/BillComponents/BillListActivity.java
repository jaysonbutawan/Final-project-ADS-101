package com.example.sapacoordinator.BillComponents;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.sapacoordinator.DatabaseConnector.ApiClient;
import com.example.sapacoordinator.DatabaseConnector.ApiInterface;
import com.example.sapacoordinator.DatabaseConnector.GenericResponse;
import com.example.sapacoordinator.R;
import com.example.sapacoordinator.SchoolComponents.ChooseAction;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BillListActivity extends AppCompatActivity implements BillAdapter.OnBillClickListener {

    private RecyclerView rvBills;
    private TextView tvEmptyMessage;
    private BillAdapter billAdapter;
    private List<Bill> billList;
    private int schoolId;
    private String schoolName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_bill_list);

        // Get data from intent
        Intent intent = getIntent();
        schoolId = intent.getIntExtra("school_id", -1);
        schoolName = intent.getStringExtra("school_name");

        if (schoolId == -1) {
            Toast.makeText(this, "Invalid school data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupRecyclerView();
        loadBills();
        setupBackNavigation();
    }

    private void initViews() {
        rvBills = findViewById(R.id.rvBills);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);

        // Set title if you have a toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.bills_title) + " - " + schoolName);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupRecyclerView() {
        billList = new ArrayList<>();
        billAdapter = new BillAdapter(billList, this);
        billAdapter.setOnBillClickListener(this);

        rvBills.setLayoutManager(new LinearLayoutManager(this));
        rvBills.setAdapter(billAdapter);
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
                        showEmptyState();
                    } else {
                        hideEmptyState();
                        billList.clear();
                        billList.addAll(bills);
                        billAdapter.updateBills(billList);
                    }
                } else {
                    Log.e("BillListActivity", "Failed to load bills: " + response.message());
                    showEmptyState();
                    Toast.makeText(BillListActivity.this, "Failed to load bills", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Bill>> call, @NonNull Throwable t) {
                Log.e("BillListActivity", "Error loading bills", t);
                showEmptyState();
                Toast.makeText(BillListActivity.this, "Error loading bills: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEmptyState() {
        rvBills.setVisibility(View.GONE);
        tvEmptyMessage.setVisibility(View.VISIBLE);
        tvEmptyMessage.setText(getString(R.string.no_bills_found));
    }

    private void hideEmptyState() {
        rvBills.setVisibility(View.VISIBLE);
        tvEmptyMessage.setVisibility(View.GONE);
    }

    @Override
    public void onPayNowClick(Bill bill) {
        new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Pay Bill")
                .setContentText("Pay bill " + bill.getBillReference() + " for " + bill.getFormattedAmount() + "?")
                .setConfirmText("Pay Now")
                .setCancelText("Cancel")
                .setConfirmClickListener(sweetAlertDialog -> {
                    sweetAlertDialog.dismissWithAnimation();
                })
                .setCancelClickListener(SweetAlertDialog::dismissWithAnimation)
                .show();
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

    private void setupBackNavigation() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(BillListActivity.this, ChooseAction.class);
                intent.putExtra("school_id", schoolId);
                intent.putExtra("school_name", schoolName);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @Override
    public boolean onSupportNavigateUp() {
        Intent intent = new Intent(BillListActivity.this, ChooseAction.class);
        intent.putExtra("school_id", schoolId);
        intent.putExtra("school_name", schoolName);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
        return true;
    }
}
