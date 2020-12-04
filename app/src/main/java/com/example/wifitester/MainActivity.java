package com.example.wifitester;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
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
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    //AccessPoint test =  new AccessPoint("BIO251_A"+AccessPoint.AP_Extension, new int[]{0, 10, 0});
    private WifiManager wifiManager;
    // holds nice formatted values for list
    private final ArrayList<String> arrayList = new ArrayList<>();
    // adapter to hold list state for view
    private ArrayAdapter<String> adapter;
    // a hash map to hold currently available AP's (subset of AP_DataBase)
    private final Map<String, DataWriter> apLogMap = new HashMap<>();
    // var to hold the logged distance over recording
    private Double realDistance = (double) 0;

    // number of data points to get
    final int totalTimes = 200;
    // number of points left
    int times = totalTimes;
    // var to hold scope for the data set callback
    final MainActivity outside = this;
    // state var
    boolean recordMode = false;
    // WiFi helper
    WiFiScanner wiFiScanner;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button buttonScan = findViewById(R.id.ScanButton);
        Button buttonRecWiFi = findViewById(R.id.Record_WiFi);
        Button buttonRecord = findViewById(R.id.RecordButton);
        Button buttonStop = findViewById(R.id.StopButton);
        // utility function to change modes
        Runnable changeMode = () -> {
            recordMode = !recordMode;
            for (Button button : Arrays.asList(buttonScan, buttonRecWiFi, buttonRecord, buttonStop))
                button.setVisibility((int) (button.getVisibility() ^ View.GONE));
        };

        // These are the button callback functions
        buttonScan.setOnClickListener(view -> scanWifi());
        buttonRecWiFi.setOnClickListener(view -> {
            buttonRecWiFi.setEnabled(false);
            times = totalTimes;
            recordDataDialog();
        });
        buttonRecord.setOnClickListener(view -> {
            apLogMap.clear();
            changeMode.run();
        });
        buttonStop.setOnClickListener(view -> {
            for (DataWriter d : apLogMap.values()) {
                d.saveData();
            }
            apLogMap.clear();
            changeMode.run();
        });

        // double ensure these buttons aren't shown
        buttonStop.setVisibility(View.GONE);
        buttonStop.setVisibility(View.GONE);

        // init view
        ListView listView = findViewById(R.id.wifiList);

        // setup helper
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wiFiScanner = new WiFiScanner(wifiReceiver, this);

        // setup view with list update notifications via adapter
        adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                arrayList);
        listView.setAdapter(adapter);
        // do first scan
        scanWifi();
        // added to jump automatically to locating activity
//        Intent intent = new Intent(this, Locating.class);
//        startActivity(intent);
    }

    /**
     * calls OS to scan wifi for us
     * we remove any wifi's in list and notify the user we are scanning (if not recording)
     */
    private void scanWifi() {
        arrayList.clear();
        wiFiScanner.scanWifi();
        if (!recordMode)
            Toast.makeText(this, "Scanning WiFi ...", Toast.LENGTH_SHORT).show();
    }

    /**
     * unnamed broadcast receiver to attach our callbacks once scan is done
     * <p>
     * callback once we get the scan results
     * this handles the recording and showing the received data
     */
    final BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            List<ScanResult> results = wifiManager.getScanResults();
            // make sure we release hold
            unregisterReceiver(this);
            // if we got nothing
            if (results.size() == 0) {
                if (!recordMode)
                    Toast.makeText(outside, "No AP's, check location services?", Toast.LENGTH_SHORT).show();
                else
                    outside.scanWifi();
                // early return, don't clear list in scan mode, and don't count as a sample in record mode
                return;
            }
            // for each result
            for (ScanResult scanResult : results) {
                AccessPoint temp_AP = AccessPoint.GetAccessPoint(scanResult.SSID);
                // if AP in our DB of known AP's
                if (temp_AP != null) {
                    // if in record mode
                    if (outside.recordMode) {
                        // if this is first sight, create a new file for AP and store reference for later
                        if (!apLogMap.containsKey(scanResult.SSID)) {
                            DataWriter d = new DataWriter(outside, scanResult.SSID);
                            apLogMap.put(scanResult.SSID, d);
                            d.getFile("Logged Distance, Signal Strength, Calculated Distance (m)");
                        }
                        // get writer for this AP and write the data
                        DataWriter dataWriter = apLogMap.get(scanResult.SSID);
                        dataWriter.writeData(realDistance, scanResult.level, calculateDistanceMeters(scanResult.level, scanResult.frequency));
                    } else {
                        // add to view list
                        arrayList.add(scanResult.SSID + ": dB[" + scanResult.level + "], Dist[" + calculateDistanceMeters(scanResult.level, scanResult.frequency) + "m]");
                    }
                }
            }
            // if record mode, check number of times
            if (outside.recordMode) {
                if (times > 0) {
                    // reassure the user that we are working
                    if (times % 20 == 0)
                        Toast.makeText(outside, times + " more record(s)", Toast.LENGTH_LONG).show();
                    // don't forget to count
                    --times;
                    // immediately call a scan again (we know this will take awhile so no worries of a race condition)
                    outside.scanWifi();
                } else {
                    // we are done, enable the record button
                    Toast.makeText(outside, "Recording finished!", Toast.LENGTH_LONG).show();
                    findViewById(R.id.Record_WiFi).setEnabled(true);
                }
            } else {
                // we are not recording, refresh list
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.locate_menu) {
            Intent intent = new Intent(this, Locating.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
