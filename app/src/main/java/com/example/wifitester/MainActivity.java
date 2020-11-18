package com.example.wifitester;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.DataSetObserver;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;


public class MainActivity extends AppCompatActivity {
    //AccessPoint test =  new AccessPoint("BIO251_A"+AccessPoint.AP_Extension, new int[]{0, 10, 0});
    private WifiManager wifiManager;
    private final ArrayList<ScanResult> arrayList = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private final DataWriter dataWriter = new DataWriter(this);
    private Double distance = (double) 0;
    private final DataSetObserver observer = new DataSetObserver() {
        private final ArrayList<ScanResult> results = new ArrayList<>();
        @Override
        public void onChanged() {
            super.onChanged();
            ScanResult result = arrayList.get(0);
            recordDataDialog();
            dataWriter.writeData(distance, result.level, calculateDistanceMeters(result.level, result.frequency));
        }

        @Override
        public void onInvalidated() {
            super.onInvalidated();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button buttonScan = findViewById(R.id.ScanButton);
        Button buttonRecord = findViewById(R.id.RecordButton);
        Button buttonStop = findViewById(R.id.StopButton);
        buttonScan.setOnClickListener(view -> scanWifi());
        buttonRecord.setOnClickListener(view -> {
            dataWriter.getFile();
            adapter.registerDataSetObserver(observer);
            buttonRecord.setVisibility(View.GONE);
            buttonStop.setVisibility(View.VISIBLE);
        });
        buttonStop.setOnClickListener(view -> {
            adapter.unregisterDataSetObserver(observer);
            dataWriter.saveData();
            buttonRecord.setVisibility(View.VISIBLE);
            buttonStop.setVisibility(View.GONE);
        });
        buttonStop.setVisibility(View.GONE);


        ListView listView = findViewById(R.id.wifiList);
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        if (!wifiManager.isWifiEnabled()) {
            Toast.makeText(this, "WiFi is disabled ... We need to enable it", Toast.LENGTH_LONG).show();
            wifiManager.setWifiEnabled(true);
        }

        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                (arrayList.stream()
                        .map(scanResult ->
                                scanResult.SSID + ": dB[" + scanResult.level + "], Dist[" + calculateDistanceMeters(scanResult.level, scanResult.frequency) + "m]")
                        .collect(Collectors.toList())));
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

    final BroadcastReceiver wifiReceiver = new BroadcastReceiver() {

        final List<AccessPoint> Visible_APs = new LinkedList<>();
        AccessPoint temp_AP;

        @Override
        public void onReceive(Context context, Intent intent) {
            List<ScanResult> results = wifiManager.getScanResults();
            unregisterReceiver(this);
            for (ScanResult scanResult : results) {
                temp_AP = AccessPoint.GetAccessPoint(scanResult.SSID);
                if (temp_AP != null) {
                    Visible_APs.add(temp_AP);
                    arrayList.add(scanResult);
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

    // https://stackoverflow.com/questions/10903754/input-text-dialog-android
    void recordDataDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter distance");

// Set up the input
        final EditText input = new EditText(this);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setImeOptions(EditorInfo.IME_ACTION_DONE);
        input.setText(String.format(Locale.ENGLISH,"%d", distance.longValue()));
//        input.setOnEditorActionListener((v, actionId, event) -> {
//            if (event != null) {
//                distance = Double.parseDouble(input.getText().toString());
//                return true;
//            }
//            return false;
//        });
        builder.setView(input);

// Set up the buttons
        builder.setPositiveButton("OK", (dialog, which) -> distance = Double.parseDouble(input.getText().toString()));
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();
    }
}
