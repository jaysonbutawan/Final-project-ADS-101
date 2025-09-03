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

    private AlertDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        EditText emailInput = findViewById(R.id.login_email_input);
        EditText passwordInput = findViewById(R.id.login_password_input);

        findViewById(R.id.sign_in_direct).setOnClickListener(v ->
                startActivity(new Intent(this, SignUpActivity.class)));

        findViewById(R.id.login_button).setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                showDialog(SweetAlertDialog.WARNING_TYPE, "Missing Fields",
                        "Please fill in both Email and Password.");
                return;
            }

            showLoadingDialog();
            detectServerAndLogin(email, password);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.loginmain), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void detectServerAndLogin(String email, String password) {
        ServerDetector.detectServer(this, new ServerDetector.OnServerFoundListener() {
            @Override
            public void onServerFound(String baseUrl) {
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
                    showDialog(SweetAlertDialog.ERROR_TYPE, "Server Not Found",
                            "Please check your network connection and try again.");
                });
            }

            @Override
            public void onDetectionError(Exception e) {
                runOnUiThread(() -> {
                    dismissLoadingDialog();
                    showDialog(SweetAlertDialog.ERROR_TYPE, "Detection Error",
                            "Error detecting server: " + e.getMessage());
                });
            }
        });
    }

    private void showLoadingDialog() {
        if (loadingDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View view = getLayoutInflater().inflate(R.layout.dialog_loading, null);
            builder.setView(view).setCancelable(false);
            loadingDialog = builder.create();
        }
        loadingDialog.show();
    }

    private void dismissLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) loadingDialog.dismiss();
    }

    private void loginUser(String email, String password) {
        ApiInterface api = ApiClient.getClient().create(ApiInterface.class);
        api.loginUser(email, password).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<GenericResponse> call,
                                   @NonNull Response<GenericResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    handleLoginSuccess(response.body());
                } else {
                    showDialog(SweetAlertDialog.ERROR_TYPE, "Login Failed",
                            "Server error: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<GenericResponse> call, @NonNull Throwable t) {
                showDialog(SweetAlertDialog.ERROR_TYPE, "Connection Error", t.getMessage());
            }
        });
    }

    private void handleLoginSuccess(GenericResponse res) {
        if (res.isSuccess()) {
            getSharedPreferences("UserSession", MODE_PRIVATE)
                    .edit().putInt("user_id", res.getUserId()).apply();

            new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText("Welcome!")
                    .setContentText(res.getMessage())
                    .setConfirmClickListener(sDialog -> {
                        sDialog.dismissWithAnimation();
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    })
                    .show();
        } else {
            showDialog(SweetAlertDialog.ERROR_TYPE, "Login Failed", res.getMessage());
        }
    }

    private void showDialog(int type, String title, String message) {
        new SweetAlertDialog(this, type)
                .setTitleText(title)
                .setContentText(message)
                .show();
    }
}