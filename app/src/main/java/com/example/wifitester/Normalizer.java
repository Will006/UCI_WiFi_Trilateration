package com.example.wifitester;

import android.net.wifi.ScanResult;

import java.util.Collection;
import java.util.HashMap;

import static com.example.wifitester.Locating.segments;

class NoMatch extends Exception {
}

//class NormalizationInfo {
//    public final double max;
//    public final double min;
//    public final double range;
//
//    NormalizationInfo(double max, double min, double range) {
//        this.range = range;
//        this.max = max;
//        this.min = min;
//    }
//}

public class Normalizer {

// --Commented out by Inspection START (1/25/2021 4:21 PM):
//    public NormalizationInfo getNormalizationFactor(ScanResult scanResult, APInfo[] aps) throws NoMatch {
//        for (APInfo ap : aps) {
//            if (ap.name.equals(scanResult.SSID)) {
//                double max = Math.pow(10, ap.maxDB / 10.0);
//                double min = Math.pow(10, ap.minDB / 10.0);
//                double range = max - min;
//                return new NormalizationInfo(max, min, range);
//            }
//        }
//        throw new NoMatch();
//    }
// --Commented out by Inspection STOP (1/25/2021 4:21 PM)

// --Commented out by Inspection START (1/25/2021 4:21 PM):
//    double getFactor(int votingSize, double range) {
//        return range / votingSize;
//    }
// --Commented out by Inspection STOP (1/25/2021 4:21 PM)


// --Commented out by Inspection START (1/25/2021 4:20 PM):
//    public double normalized(ScanResult scanResult, APInfo[] aps, int votingSize) throws NoMatch {
//        NormalizationInfo normalizationInfo = getNormalizationFactor(scanResult, aps);
//        return Math.max(
//                votingSize -
//                        (Math.pow(10, scanResult.level / 10.0) - normalizationInfo.min)
//                                /
//                                getFactor(votingSize, (int) normalizationInfo.range),
//                0);
//    }
// --Commented out by Inspection STOP (1/25/2021 4:20 PM)

    public double normalizedByMeters(ScanResult scanResult, AccessPoint ap) {
        double meters = calculateDistanceMeters(scanResult.level, scanResult.frequency);
        double max = calculateDistanceMeters(ap.minDB, scanResult.frequency);
        double min = calculateDistanceMeters(ap.maxDB, scanResult.frequency);
        if (meters > max) {
            return max;
        }
        return (1-(max - meters) / (max-min)) * segments;
    }

    //https://stackoverflow.com/questions/11217674/how-to-calculate-distance-from-wifi-router-using-signal-strength
    double calculateDistanceMeters(double signalLevelInDb, double freqInMHz) {
        double exp = (27.55 - (20 * Math.log10(freqInMHz)) + Math.abs(signalLevelInDb)) / 20.0;
        return Math.pow(10.0, exp);
    }

// --Commented out by Inspection START (1/25/2021 4:20 PM):
//    private double calculateDistanceFeet(double signalLevelInDb, double freqInMHz) {
//        return calculateDistanceMeters(signalLevelInDb, freqInMHz) * 3.28084;
//    }
// --Commented out by Inspection STOP (1/25/2021 4:20 PM)
}
