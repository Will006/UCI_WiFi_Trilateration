package com.example.wifitester;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class MainActivity extends AppCompatActivity {
    //AccessPoint test =  new AccessPoint("BIO251_A"+AccessPoint.AP_Extension, new int[]{0, 10, 0});
    private WifiManager wifiManager;
    // holds nice formatted values for list
    private final ArrayList<String> arrayList = new ArrayList<>();
    // holds raw scan values from Android OS
//    private final ArrayList<ScanResult> scanList = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private final Map<String, DataWriter> apLogMap = new HashMap<>();
    private Double realDistance = (double) 0;

    // executor to schedule and run the wifi scan
    ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
    // number of data points to get
    final int totalTimes = 200;
    // number of points left
    int times = totalTimes;
    // var to hold scope for the data set callback
    MainActivity outside = this;
    // state var
    boolean recordMode = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button buttonScan = findViewById(R.id.ScanButton);
        Button buttonRecWiFi = findViewById(R.id.Record_WiFi);
        Button buttonRecord = findViewById(R.id.RecordButton);
        Button buttonStop = findViewById(R.id.StopButton);
        // utility function to change the buttons
        Runnable changeMode = () -> {
            recordMode = !recordMode;
            for (Button button : Arrays.asList(buttonScan, buttonRecWiFi, buttonRecord, buttonStop))
                button.setVisibility((int) (button.getVisibility() ^ View.GONE));
        };
        buttonScan.setOnClickListener(view -> scanWifi());
        buttonRecWiFi.setOnClickListener(view -> {
            buttonRecWiFi.setEnabled(false);
            times = totalTimes;
            recordDataDialog();
        });
        buttonRecord.setOnClickListener(view -> {
//            adapter.registerDataSetObserver(observer);
            apLogMap.clear();
            changeMode.run();
        });
        buttonStop.setOnClickListener(view -> {
//            adapter.unregisterDataSetObserver(observer);
            for (DataWriter d: apLogMap.values()) {
                d.saveData();
            }
            apLogMap.clear();
            changeMode.run();
        });
        buttonStop.setVisibility(View.GONE);
        buttonStop.setVisibility(View.GONE);


        ListView listView = findViewById(R.id.wifiList);
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        if (!wifiManager.isWifiEnabled()) {
            Toast.makeText(this, "WiFi is disabled ... We need to enable it", Toast.LENGTH_LONG).show();
            wifiManager.setWifiEnabled(true);
        }

        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                arrayList);
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
//        scanList.clear();
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiManager.startScan();
        if (!recordMode)
            Toast.makeText(this, "Scanning WiFi ...", Toast.LENGTH_SHORT).show();
    }

    final BroadcastReceiver wifiReceiver = new BroadcastReceiver() {

        final List<AccessPoint> Visible_APs = new LinkedList<>();
        AccessPoint temp_AP;

        @Override
        public void onReceive(Context context, Intent intent) {
            List<ScanResult> results = wifiManager.getScanResults();
            unregisterReceiver(this);
            if (results.size() == 0) {
                if (!recordMode)
                    Toast.makeText(outside, "No AP's, check location services?", Toast.LENGTH_SHORT).show();
                else
                    outside.scanWifi();
                // early return, dont clear list, and dont count as sample
                return;
            }
            for (ScanResult scanResult : results) {
                temp_AP = AccessPoint.GetAccessPoint(scanResult.SSID);
                if (temp_AP != null) {
                    Visible_APs.add(temp_AP);

                    if (outside.recordMode) {
                        if (!apLogMap.containsKey(scanResult.SSID)) {
                            DataWriter d = new DataWriter(outside, scanResult.SSID);
                            apLogMap.put(scanResult.SSID, d);
                            d.getFile();
                        }
                        DataWriter dataWriter = apLogMap.get(scanResult.SSID);
                        dataWriter.writeData(realDistance, scanResult.level, calculateDistanceMeters(scanResult.level, scanResult.frequency));
                    }

                    else {
                        arrayList.add(scanResult.SSID + ": dB[" + scanResult.level + "], Dist[" + calculateDistanceMeters(scanResult.level, scanResult.frequency) + "m]");
                    }
                }
            }
            if (outside.recordMode) {
                if (times > 0) {
                    if (times % 20 ==0)
                        Toast.makeText(outside, times + " more record(s)", Toast.LENGTH_LONG).show();
                    --times;
                    //                executor.schedule(outside::scanWifi, 10, TimeUnit.SECONDS);
                    outside.scanWifi();
                } else {
                    Toast.makeText(outside, "Recording finished!", Toast.LENGTH_LONG).show();
                    findViewById(R.id.Record_WiFi).setEnabled(true);
                }
            }

//            arrayList.addAll(results.stream()
//                    .filter(result -> AccessPoint.GetAccessPoint(result.SSID) != null)
//                    .map(scanResult ->
//                            scanResult.SSID + ": dB[" + scanResult.level + "], Dist[" + calculateDistanceMeters(scanResult.level, scanResult.frequency) + "m]")
//                    .collect(Collectors.toCollection(ArrayList::new)));
            else {
                adapter.notifyDataSetChanged();
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
        input.setText(new DecimalFormat("0.#").format(realDistance));
//        input.setOnEditorActionListener((v, actionId, event) -> {
//            if (event != null) {
//                distance = Double.parseDouble(input.getText().toString());
//                return true;
//            }
//            return false;
//        });
        builder.setView(input);

// Set up the buttons
        builder.setPositiveButton("OK", (dialog, which) -> {
            realDistance = Double.parseDouble(input.getText().toString());
            scanWifi();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();
    }
}
