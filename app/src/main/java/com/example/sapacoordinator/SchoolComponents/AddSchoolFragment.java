package com.example.sapacoordinator.SchoolComponents;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.sapacoordinator.DatabaseConnector.ApiClient;
import com.example.sapacoordinator.DatabaseConnector.ApiInterface;
import com.example.sapacoordinator.DatabaseConnector.GenericResponse;
import com.example.sapacoordinator.R;

import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddSchoolFragment extends Fragment {
    private EditText etSchoolName, etAddress, etContactInfo;
    private Button btnRegister;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_school, container, false);

        etSchoolName = view.findViewById(R.id.etSchoolName);
        etAddress = view.findViewById(R.id.etAddress);
        etContactInfo = view.findViewById(R.id.etContactInfo);
        btnRegister = view.findViewById(R.id.btnRegister);

        btnRegister.setOnClickListener(v -> {
            String schoolName = etSchoolName.getText().toString().trim();
            String address = etAddress.getText().toString().trim();
            String contactInfo = etContactInfo.getText().toString().trim();
            SharedPreferences prefs = requireActivity().getSharedPreferences("UserSession", requireActivity().MODE_PRIVATE);
            int userId = prefs.getInt("user_id", -1);

            if (userId == -1) {
                new SweetAlertDialog(requireContext(), SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Error")
                        .setContentText("User session expired. Please log in again.")
                        .show();
            } else if (schoolName.isEmpty() || address.isEmpty()) {
                new SweetAlertDialog(requireContext(), SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Missing Fields")
                        .setContentText("Please fill all required fields.")
                        .show();
            } else {
                addSchool(schoolName, address, contactInfo, userId);
            }
        });

        return view;
    }

    private void addSchool(String schoolName, String address, String contactInfo, int userId) {
        ApiInterface api = ApiClient.getClient().create(ApiInterface.class);

        Call<GenericResponse> call = api.registerSchool(schoolName, address, contactInfo, userId);

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<GenericResponse> call, @NonNull Response<GenericResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    handleAddSchoolResponse(response.body());
                } else {
                    showAddSchoolError("Server error: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<GenericResponse> call, @NonNull Throwable t) {
                showAddSchoolError("Connection Error: " + t.getMessage());
            }
        });
    }


    private void handleAddSchoolResponse(GenericResponse res) {
        if (res.isSuccess()) {
            new SweetAlertDialog(requireContext(), SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText("Success")
                    .setContentText(res.getMessage())
                    .setConfirmClickListener(sDialog -> {
                        sDialog.dismissWithAnimation();
                        etSchoolName.setText("");
                        etAddress.setText("");
                        etContactInfo.setText("");
                    })
                    .show();
        } else {
            new SweetAlertDialog(requireContext(), SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Failed")
                    .setContentText(res.getMessage())
                    .show();
        }
    }

    private void showAddSchoolError(String message) {
        new SweetAlertDialog(requireContext(), SweetAlertDialog.ERROR_TYPE)
                .setTitleText("Connection Error")
                .setContentText(message)
                .show();
    }

}