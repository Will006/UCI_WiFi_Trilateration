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
    private int[][][] voting;
    final List<APInfo> aps;
    private final Normalizer normalizer;
    private int votes;
    int[] pos;
    //

    public Locator(int size, String ap1, String ap2, String ap3) {
        // TODO: change these if you're using the normalized version, set max/min by checking signal strength at each AP
        // TODO: set these via some setup thing
        final int maxDb1 = -25;
        final int minDB1 = -65;
        final int maxDB2 = -25;
        final int minDB2 = -65;
        final int maxDB3 = -25;
        final int minDB3 = -65;

        voting = new int[size][size][size];
        this.aps = new ArrayList<>();
        this.aps.add(new APInfo(ap1, minDB1, maxDb1));
        this.aps.add(new APInfo(ap2, minDB2, maxDB2));
        this.aps.add(new APInfo(ap3, minDB3, maxDB3));

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
        int[] apMatrixCell = new int[]{voting.length/2, -1,-1};

        // aps will have y = 0
        if (aps.get(0).name.equals(scanResult.SSID)) {
            apMatrixCell = new int[]{voting.length/2,0,0,0};
        }
        if (aps.get(1).name.equals(scanResult.SSID)) {
            apMatrixCell = new int[]{voting.length/2,voting[0].length - 1, 0};
        }
        if (aps.get(2).name.equals(scanResult.SSID)) {
            apMatrixCell = new int[]{voting.length/2,0, voting[0][0].length -1};
        }
        if (Arrays.equals(apMatrixCell, new int[]{-1, -1})) {
            // fail
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
        int index = -1;
        for (int h = 0; h < voting.length; h++) {
            for (int i = 0; i < voting.length; i++) {
                for (int j = 0; j < voting.length; j++) {
                    if (voting[h][i][j] > max) {
                        max = voting[h][i][j];
                        index = i;
                    }
                }
            }
//            Integer temp = Arrays.stream(voting[i]).reduce(0, Integer::sum);
        }
        return max;
    }

    void findPos()
    {
        int max = -1;
        int numOfMax = 0;
        int[] tempPos = new int[] {0,0,0};
        ArrayList<ArrayList<Integer>> maxList = new ArrayList<ArrayList<Integer>>();
        //int[][] maxList = new int[][] {tempPos};
        for (int h = 0; h < voting.length; h++) {
            for (int i = 0; i < voting.length; i++) {
                for (int j = 0; j < voting.length; j++) {
                    if (voting[h][i][j] > max) {
                        numOfMax = 1;
                        max = voting[h][i][j];
                        tempPos = new int[] {voting.length /2, i, j};
                        maxList.clear();
                        maxList.add(new ArrayList<Integer>());
                        maxList.get(numOfMax - 1).add(voting.length / 2);
                        maxList.get(numOfMax - 1).add(i);
                        maxList.get(numOfMax - 1).add(j);
                    }
                    else if ((voting[h][i][j]!=0)&&(voting[h][i][j]==max))
                    {
                        numOfMax++;
                        maxList.add(new ArrayList<Integer>());
                        maxList.get(numOfMax - 1).add(voting.length / 2);
                        maxList.get(numOfMax - 1).add(i);
                        maxList.get(numOfMax - 1).add(j);
                    }
                }
            }
        }

        if(numOfMax > 1) {
            float x = 0;
            float y = 0;
            for (int l = 0; l <numOfMax; l++)
            {
                x+=maxList.get(l).get(1);
                y+=maxList.get(l).get(2);
            }
            pos = new int[] {voting.length/2, (int)x/numOfMax, (int)y/numOfMax};
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
