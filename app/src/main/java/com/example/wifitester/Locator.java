package com.example.wifitester;

import android.net.wifi.ScanResult;


import java.util.Arrays;

public class Locator {
    private int[][] voting;
    private final String ap1;
    private final String ap2;

    Locator(int size, String ap1, String ap2) {
        voting = new int[size][size];
        this.ap1 = ap1;
        this.ap2 = ap2;
    }

    void vote(ScanResult scanResult) {
        double dist = calculateDistanceFeet(scanResult.level, scanResult.frequency);

        int[] coords = new int[]{-1, -1};
        // aps will have y = 0
        if (ap1.equals(scanResult.SSID)) {
            coords = new int[]{0, 0};
        }
        if (ap2.equals(scanResult.SSID)) {
            coords = new int[]{voting.length - 1, 0};
        }
        if (Arrays.equals(coords, new int[]{-1, -1})) {
            // fail
            return;
        }
        // go through and vote
        for (int i = 0; i < voting.length; i++) {
            for (int j = 0; j < voting.length; j++) {
                if (isOnCircle((int) Math.round(dist), coords, new int[]{i, j})) {
                    voting[i][j] += 1;
                }
            }
        }
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

    private boolean isOnCircle(int radius, int[] center, int[] point) {
        return sqr(radius) - sqr(point[0] - center[0]) + sqr(point[1] - center[1]) < 4;
    }

    void clear() {
        voting = new int[voting.length][voting.length];
    }

    int getMaxSegment() {
        Integer max = -1;
        int index = -1;
        for (int i = 0; i < voting.length; i++) {
            Integer temp = Arrays.stream(voting[i]).reduce(0, Integer::sum);
            if (temp > max) {
                max = temp;
                index = i;
            }
        }
        return index;
    }

    int[][] getVoting() {
        return voting.clone();
    }
}
