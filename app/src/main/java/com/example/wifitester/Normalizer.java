package com.example.wifitester;

import android.net.wifi.ScanResult;

import java.util.Collection;
import java.util.HashMap;

class NoMatch extends Exception {
}

class NormalizationInfo {
    public final double max;
    public final double min;
    public final double range;

    NormalizationInfo(double max, double min, double range) {
        this.range = range;
        this.max = max;
        this.min = min;
    }
}

public class Normalizer {
    public NormalizationInfo getNormalizationFactor(ScanResult scanResult, Collection<AccessPoint> aps) throws NoMatch {
        for (AccessPoint ap : aps) {
            if (ap.SSID.equals(scanResult.SSID)) {
                double max = Math.pow(10, ap.maxDB / 10.0);
                double min = Math.pow(10, ap.minDB / 10.0);
                double range = max - min;
                return new NormalizationInfo(max, min, range);
            }
        }
        throw new NoMatch();
    }

    double getFactor(int votingSize, double range) {
        return range / votingSize;
    }

    public double normalized(ScanResult scanResult, Collection<AccessPoint> aps, int votingSize) throws NoMatch {
        NormalizationInfo normalizationInfo = getNormalizationFactor(scanResult, aps);
        return Math.max(
                votingSize -
                        (Math.pow(10, scanResult.level / 10.0) - normalizationInfo.min)
                                /
                                getFactor(votingSize, (int) normalizationInfo.range),
                0);
    }

    public double normalizedByMeters(ScanResult scanResult, AccessPoint ap) {
        double meters = calculateDistanceMeters(scanResult.level, scanResult.frequency);
        double max = calculateDistanceMeters(ap.minDB, scanResult.frequency);
        if (meters > max) {
            return max;
        }
        return meters / max * 100;
    }

    //https://stackoverflow.com/questions/11217674/how-to-calculate-distance-from-wifi-router-using-signal-strength
    double calculateDistanceMeters(double signalLevelInDb, double freqInMHz) {
        double exp = (27.55 - (20 * Math.log10(freqInMHz)) + Math.abs(signalLevelInDb)) / 20.0;
        return Math.pow(10.0, exp);
    }

    private double calculateDistanceFeet(double signalLevelInDb, double freqInMHz) {
        return calculateDistanceMeters(signalLevelInDb, freqInMHz) * 3.28084;
    }
}
