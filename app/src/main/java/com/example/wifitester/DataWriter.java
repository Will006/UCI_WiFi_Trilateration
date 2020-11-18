package com.example.wifitester;

import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;

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
    private Context ctx;
    DataWriter(Context ctx) {
        this.ctx = ctx;
    }

    void getFile() {
        String today = (new Date()).toString();
        try {
            File file = new File(ctx.getExternalFilesDir(null), today + "_ATLAS_data.csv");
            outfileStream = new FileOutputStream(file);
            // make it easier to work with by wrapping it in a print stream
            outStream = new PrintStream(outfileStream);
            // init the top row
            String header = "Real Distance, Signal Strength, Calculated Distance";
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

    void saveData() {
        try {
            outStream.flush();
            outfileStream.flush();
            outfileStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
