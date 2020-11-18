package com.example.wifitester;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;



public class MainActivity extends AppCompatActivity {
    //AccessPoint test =  new AccessPoint("BIO251_A"+AccessPoint.AP_Extension, new int[]{0, 10, 0});
    private WifiManager wifiManager;
    private ListView listView;
    private Button buttonScan;
    private int size = 0;
    private List<ScanResult> results;
    private ArrayList<String> arrayList = new ArrayList<>();
    private ArrayAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buttonScan = findViewById(R.id.ScanButton);
        buttonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanWifi();
            }
        });

        listView = findViewById(R.id.wifiList);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (!wifiManager.isWifiEnabled()) {
            Toast.makeText(this, "WiFi is disabled ... We need to enable it", Toast.LENGTH_LONG).show();
            wifiManager.setWifiEnabled(true);
        }

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, arrayList);
        listView.setAdapter(adapter);
        scanWifi();
    }
    //https://developer.android.com/reference/android/net/wifi/WifiManager
    //https://developer.android.com/reference/android/net/ConnectivityManager
    /*
    https://stackoverflow.com/questions/16485370/wifi-position-triangulation
    https://developer.android.com/reference/android/net/wifi/WifiManager#EXTRA_NEW_RSSI
    https://developer.android.com/reference/android/net/wifi/WifiManager#RSSI_CHANGED_ACTION

     */
    private void scanWifi() {
        arrayList.clear();
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiManager.startScan();
        Toast.makeText(this, "Scanning WiFi ...", Toast.LENGTH_SHORT).show();
    }

    BroadcastReceiver wifiReceiver = new BroadcastReceiver()
    {

        List<AccessPoint> Visable_APs = new LinkedList<>();
        AccessPoint temp_AP;
        @Override
        public void onReceive(Context context, Intent intent)
        {
            results = wifiManager.getScanResults();
            unregisterReceiver(this);
            for (ScanResult scanResult : results)
            {
                temp_AP = AccessPoint.GetAccessPoint(scanResult.SSID);
                if(temp_AP!=null)
                {
                    Visable_APs.add(temp_AP);
                    double dist = calculateDistanceMeters(scanResult.level,scanResult.frequency);
                    arrayList.add(scanResult.SSID + ": dB[" + scanResult.level+"], Dist["+dist+"m]");
                    adapter.notifyDataSetChanged();
                }
            }
        }
    };

    //https://stackoverflow.com/questions/11217674/how-to-calculate-distance-from-wifi-router-using-signal-strength
    public double calculateDistanceMeters(double signalLevelInDb, double freqInMHz) {
        double exp = (27.55 - (20 * Math.log10(freqInMHz)) + Math.abs(signalLevelInDb)) / 20.0;
        return Math.pow(10.0, exp);
    }
}
