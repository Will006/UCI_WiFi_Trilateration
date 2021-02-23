package com.example.wifitester;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;

public class DataWriter extends AppCompatActivity {
    private FileOutputStream outfileStream;
    private PrintStream outStream;
//    boolean hasFile;
    private final Context ctx;
    private final String AP;
    private File file;
    DataWriter(Context ctx, String ap) {
        this.ctx = ctx;
        this.AP = ap;
    }

    /**
     * Prepares/allocates the file
     */
    void getFile(String header) {
        String today = (new Date()).toString();
        try {
            file = new File(ctx.getExternalFilesDir(null), today + "_ATLAS_data_" + AP + ".csv");
            outfileStream = new FileOutputStream(file);
            // make it easier to work with by wrapping it in a print stream
            outStream = new PrintStream(outfileStream);
            // init the top row
//            String header = "Logged Distance, Signal Strength, Calculated Distance (m)";
            outStream.println(header);
//            hasFile = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
//            hasFile = false;
        }
    }

    /**
     * writes the data to the csv file
     * @param realDistance in meters
     * @param signalStrength in dBm
     * @param calculatedDistance in meters
     */
    void writeData(double realDistance, double signalStrength, double calculatedDistance) {
        outStream.println(realDistance + "," + signalStrength + "," + calculatedDistance);
    }

    void writeData(String[] args) {
        outStream.println(TextUtils.join(",", args));
    }

    /**
     * Closes and commits the file contents
     */
    void saveData() {
        try {
            sendData();
            outStream.flush();
            outfileStream.flush();
            outfileStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    StringBuilder readFile() {
        //Read text from file
        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
        }
        catch (IOException e) {
            //You'll need to add proper error handling here
        }
        return text;
    }

    void sendData() {
        RequestQueue queue = Volley.newRequestQueue(ctx);
        String url = "http://192.168.1.163:5000/volley";
        JSONObject jsonObject = new JSONObject();
        try {
            Log.d("contents", readFile().toString());
            jsonObject.put("contents", readFile().toString());
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
        JsonObjectRequest request = new JsonObjectRequest(url,
                jsonObject,
                response -> Log.d("response", String.valueOf(response)),
                error -> Log.e("response error", "response failed"));
        queue.add(request);
    }
}
