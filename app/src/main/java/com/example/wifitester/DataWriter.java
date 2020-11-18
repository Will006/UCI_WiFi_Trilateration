package com.example.wifitester;

import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;

public class DataWriter extends AppCompatActivity {
    FileOutputStream outfileStream;
    PrintStream outStream;
    boolean hasFile;

    void getFile() {
        String today = (new Date()).toString();
        try {
            outfileStream = openFileOutput(today + "_ATLAS_data.csv", Context.MODE_PRIVATE);
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
        if (BuildConfig.DEBUG && !hasFile) {
            throw new AssertionError("no file, call getFileFirst");
        }
        outStream.println(realDistance + "," + signalStrength + "," + calculatedDistance);
    }

    void saveData() {
        try {
            outfileStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
