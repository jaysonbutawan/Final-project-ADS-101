package com.example.sapacoordinator;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sapacoordinator.DatabaseConnector.ApiClient;
import com.example.sapacoordinator.DatabaseConnector.ApiInterface;
import com.example.sapacoordinator.DatabaseConnector.GenericResponse;
import com.example.sapacoordinator.DatabaseConnector.ServerDetector;

import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        EditText login_email_input = findViewById(R.id.login_email_input);
        EditText login_password_input = findViewById(R.id.login_password_input);
        Button login_button = findViewById(R.id.login_button);

        TextView sign_in_direct = findViewById(R.id.sign_in_direct);
        sign_in_direct.setOnClickListener(v -> {
            Intent intent = new Intent(this, SignUpActivity.class);
            startActivity(intent);
        });

        login_button.setOnClickListener(v -> {
            String email = login_email_input.getText().toString().trim();
            String password = login_password_input.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                new SweetAlertDialog(LoginActivity.this, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Missing Fields")
                        .setContentText("Please fill in both Email and Password.")
                        .show();
                return;
            }

            showLoadingDialog();

            ServerDetector.detectServer(LoginActivity.this, new ServerDetector.OnServerFoundListener() {
                @Override
                public void onServerFound(String baseUrl) {
                    Log.d("ServerDetector", "Base URL detected: " + baseUrl);
                    runOnUiThread(() -> {
                        dismissLoadingDialog();
                        ApiClient.setBaseUrl(baseUrl);

                        loginUser(email, password);
                    });
                }

                @Override
                public void onServerNotFound() {
                    runOnUiThread(() -> {
                        dismissLoadingDialog();
                        new SweetAlertDialog(LoginActivity.this, SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Server Not Found")
                                .setContentText("Please check your network connection and try again.")
                                .show();
                    });
                }

                @Override
                public void onDetectionError(Exception e) {
                    runOnUiThread(() -> {
                        dismissLoadingDialog();
                        new SweetAlertDialog(LoginActivity.this, SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Detection Error")
                                .setContentText("Error detecting server: " + e.getMessage())
                                .show();
                    });
                }
            });
        });



        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.loginmain), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    // Show progress dialog while detecting server
    AlertDialog loadingDialog;

    private void showLoadingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_loading, null);
        builder.setView(view);
        builder.setCancelable(false); // same behavior as your ProgressDialog

        loadingDialog = builder.create();
        loadingDialog.show();
    }

    private void dismissLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }


    private void loginUser(String email, String password) {
        Log.d("LoginActivity", "Login API started: " + email);
        ApiInterface api = ApiClient.getClient().create(ApiInterface.class);
        Call<GenericResponse> call = api.loginUser(email, password);

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<GenericResponse> call, @NonNull Response<GenericResponse> response) {
                Log.d("LoginActivity", "API Response: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    handleLoginSuccess(response.body());
                } else {
                    showError("Login failed. Server error: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<GenericResponse> call, @NonNull Throwable t) {
                Log.e("LoginActivity", "Login API failed: " + t.getMessage(), t);
                showError("Connection Error: " + t.getMessage());
            }
        });
    }


    private void handleLoginSuccess(GenericResponse res) {
        if (res.isSuccess()) {
            int userId = res.getUserId();
            SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
            prefs.edit().putInt("user_id", userId).apply();

            new SweetAlertDialog(LoginActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText("Welcome!")
                    .setContentText(res.getMessage())
                    .setConfirmClickListener(sDialog -> {
                        sDialog.dismissWithAnimation();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    })
                    .show();
        } else {
            new SweetAlertDialog(LoginActivity.this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Login Failed")
                    .setContentText(res.getMessage())
                    .show();
        }
    }

    private void showError(String message) {
        new SweetAlertDialog(LoginActivity.this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText("Connection Error")
                .setContentText(message)
                .show();
    }


}