package com.example.wifitester;

import android.net.wifi.ScanResult;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

class APInfo {
    public final String name;
    public final int minDB;
    public final int maxDB;

    public APInfo(String name, int minDB, int maxDB) {
        this.name = name;
        this.minDB = minDB;
        this.maxDB = maxDB;
    }
}

public class Locator {
    final int circleThresh = 300;
    private int[][] voting;
    final List<APInfo> aps;
    private final Normalizer normalizer;
    //

    public Locator(int size, String ap1, String ap2) {
        // TODO: change these if you're using the normalized version, set max/min by checking signal strength at each AP
        // TODO: set these via some setup thing
        final int maxDb1 = -26;
        final int minDB1 = -72;
        final int maxDB2 = -20;
        final int minDB2 = -47;

        voting = new int[size][size];
        this.aps = new ArrayList<>();
        this.aps.add(new APInfo(ap1, minDB1, maxDb1));
        this.aps.add(new APInfo(ap2, minDB2, maxDB2));

        normalizer = new Normalizer();
    }

    private APInfo getAPbyName(String apName) throws NoMatch {
        Optional<APInfo> ap = aps.stream().filter((apInfo -> apInfo.name.equals(apName))).findFirst();
        if (ap.isPresent()) {
            return ap.get();
        }
        throw new NoMatch();
    }

    // TODO: change to whatever normalization we use at the end
    public double getNormalized(ScanResult scanResult) throws NoMatch {
        return normalizer.normalizedByMeters(scanResult, getAPbyName(scanResult.SSID));
    }

    void vote(ScanResult scanResult) throws NoMatch {
//        double dist = calculateDistanceFeet(scanResult.level, scanResult.frequency);
//        Log.d(scanResult.SSID, "dist is " + dist);
//        Log.d(scanResult.SSID,  "segment is " + dist / voting.length);

        // TODO: remove this once we do a good setup
        int[] apMatrixCell = new int[]{-1, -1};
        // aps will have y = 0
        if (aps.get(0).name.equals(scanResult.SSID)) {
            apMatrixCell = new int[]{0, 0};
        }
        if (aps.get(1).name.equals(scanResult.SSID)) {
            apMatrixCell = new int[]{voting.length - 1, 0};
        }
        if (Arrays.equals(apMatrixCell, new int[]{-1, -1})) {
            // fail
            return;
        }
        // END

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
}
