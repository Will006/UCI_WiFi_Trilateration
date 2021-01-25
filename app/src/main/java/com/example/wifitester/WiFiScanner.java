package com.example.wifitester;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.widget.Toast;

public class WiFiScanner {
    //https://developer.android.com/reference/android/net/wifi/WifiManager
    //https://developer.android.com/reference/android/net/ConnectivityManager
    /*
    https://stackoverflow.com/questions/16485370/wifi-position-triangulation
    https://developer.android.com/reference/android/net/wifi/WifiManager#EXTRA_NEW_RSSI
    https://developer.android.com/reference/android/net/wifi/WifiManager#RSSI_CHANGED_ACTION

     */

    BroadcastReceiver wifiReceiver;
    Context ctx;
    WiFiScanner(BroadcastReceiver b, Context ctx) {
        wifiReceiver = b;
        this.ctx = ctx;
    }


    /**
     * calls OS to scan wifi for us
     * we remove any wifi's in list and notify the user we are scanning (if not recording)
     */
    public void scanWifi() {
        // get the wifi service
        WifiManager wifiManager = (WifiManager) ctx.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (!wifiManager.isWifiEnabled()) {
            Toast.makeText(ctx, "WiFi is disabled ... We need to enable it", Toast.LENGTH_LONG).show();
            wifiManager.setWifiEnabled(true);
        }
        ctx.registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiManager.startScan();
    }
}
