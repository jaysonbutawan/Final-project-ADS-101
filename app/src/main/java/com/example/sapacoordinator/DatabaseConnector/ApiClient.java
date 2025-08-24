package com.example.sapacoordinator.DatabaseConnector;

import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static Retrofit retrofit = null;
    private static String BASE_URL;

    public static void setBaseUrl(String baseUrl) {
        BASE_URL = baseUrl;
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS) // prevents hanging
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();
    }

    public static Retrofit getClient() {
        if (retrofit == null) {
            throw new IllegalStateException("Retrofit not initialized. Call setBaseUrl() first.");
        }
        return retrofit;
    }
}
