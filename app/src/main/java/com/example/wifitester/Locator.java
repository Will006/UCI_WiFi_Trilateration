package com.example.wifitester;

import android.net.wifi.ScanResult;
import android.util.Log;

import java.util.Arrays;

public class Locator {
    private int[][] voting;
    private final String ap1;
    private final String ap2;
    final int maxDb1 = -26;
    final int minDb1 = -72;
    final int maxDb2 = -20;
    final int minDb2 = -47;

    Locator(int size, String ap1, String ap2) {
        voting = new int[size][size];
        this.ap1 = ap1;
        this.ap2 = ap2;
    }

    void vote(ScanResult scanResult) {
        double dist = calculateDistanceFeet(scanResult.level, scanResult.frequency);
        Log.d(scanResult.SSID, "dist is " + dist);
//        Log.d(scanResult.SSID,  "segment is " + dist / voting.length);


        int[] coords = new int[]{-1, -1};
        double range = -1;
        double min = -1;
        // aps will have y = 0
        if (ap1.equals(scanResult.SSID)) {
            coords = new int[]{0, 0};
            double max = Math.pow(10, maxDb1 / 10.0);
            min = Math.pow(10, minDb1 / 10.0);
            range = max - min;
        }
        if (ap2.equals(scanResult.SSID)) {
            coords = new int[]{voting.length - 1, 0};
            // dBm to linear scale
            double max = Math.pow(10, maxDb2 / 10.0);
            min = Math.pow(10, minDb2 / 10.0);
            range = max - min;
        }
        if (Arrays.equals(coords, new int[]{-1, -1})) {
            // fail
            return;
        }

        Log.d(scanResult.SSID, "signal is " + scanResult.level);
        double factor = range / voting.length;
        Log.d(scanResult.SSID, "lin db is " + Math.pow(10, scanResult.level / 10.0));
        // linear scale - min gives range starting at 0 for correct setup to / factor
        double normalized = voting.length - (Math.pow(10, scanResult.level / 10.0) - min) / factor;
        Log.d(scanResult.SSID, "normalized is " + normalized);
        // go through and vote
        for (int i = 0; i < voting.length; i++) {
            for (int j = 0; j < voting.length; j++) {
                if (isOnCircle(Math.max(normalized, 0), coords, new int[]{i, j})) {
                    Log.d(scanResult.SSID, "Circle Error " + Math.abs((normalized * normalized) - (sqr(coords[0] - i) + sqr(coords[1] - j))));
                    voting[i][j] += 1;
                }
            }
        }
    }

    double normalized(ScanResult scanResult) {
        double range = -1;
        double min = -1;
        if (ap1.equals(scanResult.SSID)) {
            double max = Math.pow(10, maxDb1 / 10.0);
            min = Math.pow(10, minDb1 / 10.0);
            range = max - min;
        }
        if (ap2.equals(scanResult.SSID)) {
            double max = Math.pow(10, maxDb2 / 10.0);
            min = Math.pow(10, minDb2 / 10.0);
            range = max - min;
        }
        double factor = range / voting.length;
        return Math.max(voting.length - (Math.pow(10, scanResult.level / 10.0) - min) / factor, 0);
    }

    //https://stackoverflow.com/questions/11217674/how-to-calculate-distance-from-wifi-router-using-signal-strength
    private double calculateDistanceMeters(double signalLevelInDb, double freqInMHz) {
        double exp = (27.55 - (20 * Math.log10(freqInMHz)) + Math.abs(signalLevelInDb)) / 20.0;
        return Math.pow(10.0, exp);
    }

    private double calculateDistanceFeet(double signalLevelInDb, double freqInMHz) {
        return calculateDistanceMeters(signalLevelInDb, freqInMHz) * 3.28084;
    }

    private int sqr(int n) {
        return n * n;
    }

    private boolean isOnCircle(double radius, int[] center, int[] point) {
        return Math.abs((radius * radius) - (sqr(point[0] - center[0]) + sqr(point[1] - center[1]))) < Math.sqrt(radius);
    }

    void clear() {
        voting = new int[voting.length][voting.length];
    }

    int getMaxSegment() {
        Integer max = -1;
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
