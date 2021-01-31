package com.example.wifitester;

import android.content.Context;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;

public class DataWriter extends AppCompatActivity {
    private FileOutputStream outfileStream;
    private PrintStream outStream;
    boolean hasFile;
    private final Context ctx;
    private final String AP;
    File file;
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
            hasFile = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            hasFile = false;
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    void writeData(String[] args) {
        outStream.println(String.join(",", args));
    }

    /**
     * Closes and commits the file contents
     */
    void saveData() {
        try {
            outStream.flush();
            outfileStream.flush();
            outfileStream.close();
//{"name":"Whatever was scanned"}
            JSONObject postData = new JSONObject();

            postData.put("name", 4);


            new PostReq().execute("http://192.168.4.1:5000/", postData.toString());

            /*if(file.exists())
            {
                PostReq post = new PostReq();
                post.UploadData(file.getAbsolutePath());
            }*/
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }
}
