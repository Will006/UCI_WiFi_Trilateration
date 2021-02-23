package com.example.wifitester;

import android.net.wifi.ScanResult;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;



public class Locator {
    final int circleThresh = 300;
    public int[][] voting;
    private final Normalizer normalizer;
    private int votes;

    HashMap<String, AccessPoint> aps;
    //

    public Locator(int size, HashMap<String, AccessPoint> APSet) {
        voting = new int[size][size];
        this.aps = APSet;
        normalizer = new Normalizer();
    }


    // TODO: change to whatever normalization we use at the end
    public double getNormalized(ScanResult scanResult) throws NoMatch {
        return normalizer.normalizedByMeters(scanResult, AccessPoint.GetAccessPoint(scanResult.SSID));
    }

    void vote(ScanResult scanResult) throws NoMatch {
//        double dist = calculateDistanceFeet(scanResult.level, scanResult.frequency);
//        Log.d(scanResult.SSID, "dist is " + dist);
//        Log.d(scanResult.SSID,  "segment is " + dist / voting.length);

        // TODO: remove this once we do a good setup
        int[] apMatrixCell = new int[]{-1, -1};

        // aps will have y = 0
        if(aps.isEmpty())
        {
            return;
        }
        apMatrixCell=aps.get(scanResult.SSID).Location;
        if(apMatrixCell==null)
        {
            return;
        }
        // END
        ++votes;

//        Log.d(scanResult.SSID, "signal is " + scanResult.level);
//        Log.d(scanResult.SSID, "lin db is " + Math.pow(10, scanResult.level / 10.0));

        // linear scale - min gives range starting at 0 for correct setup to / factor
        double normalized = getNormalized(scanResult);

//        Log.d(scanResult.SSID, "normalized is " + normalized);

        // go through and vote
        for (int i = 0; i < voting.length; i++) {
            for (int j = 0; j < voting.length; j++) {
                // TODO: change first param if you want to use signal to feet
                if (isOnCircle(Math.max(normalized, 0.01), apMatrixCell, new int[]{i, j})) {
                    Log.d(scanResult.SSID, "Circle Error " + Math.abs((normalized * normalized) - (sqr(apMatrixCell[0] - i) + sqr(apMatrixCell[1] - j))));
                    voting[i][j] += 1;
                }
            }
        }
    }

    private int sqr(int n) {
        return n * n;
    }

    private boolean isOnCircle(double radius, int[] center, int[] point) {
        return Math.abs((radius * radius) - (sqr(point[0] - center[0]) + sqr(point[1] - center[1]))) < this.circleThresh; // TODO: something with radius
    }

    void clear() {
        voting = new int[voting.length][voting.length];
        votes = 0;
    }
    // TODO: gets the x value, may need to fudge with this
    int getMaxSegment() {
        int max = -1;
        int index = -1;
        for (int i = 0; i < voting.length; i++) {
            for (int j = 0; j < voting.length; j++) {
                if (voting[i][j] > max) {
                    max = voting[i][j];
                    index = i;
                }
            }
//            Integer temp = Arrays.stream(voting[i]).reduce(0, Integer::sum);
        }
        return index;
    }

    int[][] getVoting() {
        return voting.clone();
    }

    int getNumVotes() {
        return votes;
    }
}
