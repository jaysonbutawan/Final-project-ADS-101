package com.example.sapacoordinator.DatabaseConnector;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.text.format.Formatter;
import android.util.Log;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerDetector {

    public interface OnServerFoundListener {
        void onServerFound(String baseUrl);
        void onServerNotFound();
        void onDetectionError(Exception e);
    }

    public static void detectServer(Context context, OnServerFoundListener listener) {
        Handler mainHandler = new Handler(Looper.getMainLooper());

        try {
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            String ipString = Formatter.formatIpAddress(wifiInfo.getIpAddress());

            if (ipString == null || !ipString.contains(".")) {
                mainHandler.post(listener::onServerNotFound);
                return;
            }

            String baseSubnet = ipString.substring(0, ipString.lastIndexOf(".") + 1);

            ExecutorService executor = Executors.newFixedThreadPool(20);

            final boolean[] found = {false};

            for (int i = 1; i <= 254; i++) {
                final int lastOctet = i;
                executor.execute(() -> {
                    if (found[0]) return; // stop if already found

                    String testIp = baseSubnet + lastOctet;
                    String url = "http://" + testIp + "/android_api/";

                    Log.d("ServerDetector", "Checking: " + url + "ping.php");

                    if (isApiReachable(url)) {
                        if (!found[0]) {
                            found[0] = true;
                            executor.shutdownNow(); // stop other scans
                            mainHandler.post(() -> listener.onServerFound(url));
                        }
                    }
                });
            }

            executor.shutdown();
            new Thread(() -> {
                try {
                    if (!executor.awaitTermination(20, java.util.concurrent.TimeUnit.SECONDS) && !found[0]) {
                        mainHandler.post(listener::onServerNotFound);
                    }
                } catch (InterruptedException e) {
                    mainHandler.post(listener::onServerNotFound);
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
            mainHandler.post(() -> listener.onDetectionError(e));
        }
    }

    private static boolean isApiReachable(String url) {
        try {
            URL testUrl = new URL(url + "ping.php");
            HttpURLConnection conn = (HttpURLConnection) testUrl.openConnection();
            conn.setConnectTimeout(2000); // increased timeout
            conn.setReadTimeout(2000);
            conn.setRequestMethod("GET");

            int code = conn.getResponseCode();
            return code == 200;
        } catch (Exception e) {
            return false;
        }
    }
}
