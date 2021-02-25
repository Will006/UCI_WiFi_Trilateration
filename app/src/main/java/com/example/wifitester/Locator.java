package com.example.wifitester;

import android.net.wifi.ScanResult;

import java.util.ArrayList;
import java.util.HashMap;



public class Locator {
    final int circleThresh = 300;
    private int[][][] voting;
    final HashMap<String, AccessPoint> aps;
    private final Normalizer normalizer;
    private int votes;
    private int[] pos;

    public Locator(int size, HashMap<String, AccessPoint> APSet) {
        voting = new int[size][size][size];
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


        // aps will have y = 0
        if(aps.isEmpty())
            return;

        int[] apMatrixCell=aps.get(scanResult.SSID).Location;
        
        if(apMatrixCell==null)
            return;

        // END
        ++votes;

//        Log.d(scanResult.SSID, "signal is " + scanResult.level);
//        Log.d(scanResult.SSID, "lin db is " + Math.pow(10, scanResult.level / 10.0));

        // linear scale - min gives range starting at 0 for correct setup to / factor
        double normalized = getNormalized(scanResult);

//        Log.d(scanResult.SSID, "normalized is " + normalized);

        // go through and vote
        for (int h = 0; h < voting.length; h++) {
            for (int i = 0; i < voting.length; i++) {
                for (int j = 0; j < voting.length; j++) {
                     // TODO: change first param if you want to use signal to feet
                    if (isOnSphere(Math.max(normalized, 0.01), apMatrixCell, new int[]{h, i, j})) {
                        //Log.d(scanResult.SSID, "Circle Error " + Math.abs((normalized * normalized) - (sqr(apMatrixCell[0] - h) + sqr(apMatrixCell[1] - i) + sqr(apMatrixCell[2] - j))));
                        voting[h][i][j] += 1;
                    }
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

    private boolean isOnSphere(double radius, int[] center, int[] point) {
        return Math.abs((radius * radius) - (sqr(point[0] - center[0]) + sqr(point[1] - center[1]) + sqr(point[2] - center[2]))) < this.circleThresh; // TODO: something with radius
    }

    void clear() {
        voting = new int[voting.length][voting.length][voting.length];
        votes = 0;
    }
    // TODO: gets the x value, may need to fudge with this
    int getMaxSegment() {
        int max = -1;
        for (int[][] ints : voting) {
            for (int i = 0; i < voting.length; i++) {
                for (int j = 0; j < voting.length; j++) {
                    if (ints[i][j] > max) {
                        max = ints[i][j];
                    }
                }
            }
//            Integer temp = Arrays.stream(voting[i]).reduce(0, Integer::sum);
        }
        return max;
    }

    void findPos() {
        int max = -1;
        int numOfMax = 0;
        int[] tempPos = new int[] {0,0,0};
        ArrayList<ArrayList<Integer>> maxList = new ArrayList<ArrayList<Integer>>();
        for (int h = 0; h < voting.length; h++) {
            for (int i = 0; i < voting.length; i++) {
                for (int j = 0; j < voting.length; j++) {
                    if (voting[h][i][j] > max) {
                        numOfMax = 1;
                        max = voting[h][i][j];
                        tempPos = new int[] {h, i, j};
                        maxList.clear();
                        maxList.add(new ArrayList<Integer>());
                        maxList.get(numOfMax - 1).add(h);
                        maxList.get(numOfMax - 1).add(j);
                        maxList.get(numOfMax - 1).add(i);
                    }
                    else if ((voting[h][i][j]!=0)&&(voting[h][i][j]==max))
                    {
                        numOfMax++;
                        maxList.add(new ArrayList<Integer>());
                        maxList.get(numOfMax - 1).add(h);
                        maxList.get(numOfMax - 1).add(j);
                        maxList.get(numOfMax - 1).add(i);
                    }
                }
            }
        }

        if(numOfMax > 1) {
            float x = 0;
            float y = 0;
            float z = 0;
            for (int l = 0; l <numOfMax; l++)
            {
                x+=maxList.get(l).get(1);
                y+=maxList.get(l).get(2);
                z+=maxList.get(l).get(0);
            }
            pos = new int[] {(int)x/numOfMax, (int)y/numOfMax, (int)z/numOfMax};
        }
        else {
            pos = tempPos;
        }
    }

    int[][][] getVoting() {
        return voting.clone();
    }

    int getNumVotes() {
        return votes;
    }

    int[] getPos() { return pos; }
}
