package com.example.wifitester;


import java.util.HashMap;

class AccessPoint
{
    static final String AP_Extension = "TrilaterationAP";
    final String SSID;
    final int[] Location;
    final int maxDB = -8;
    final int minDB = -65;

    AccessPoint(String SSID_In, int[] Location_In)
    {
        SSID=SSID_In;
        Location = Location_In;
    }
    AccessPoint(String SSID_In, int[] Location_In, int maxDb1_In, int minDB1_In)
    {
        SSID=SSID_In;
        Location = Location_In;
    }

    // TODO: change these if you're using the normalized version, set max/min by checking signal strength at each AP
    // TODO: set these via some setup thing
    static private final HashMap<String, AccessPoint> AP_DataBase = new HashMap<String, AccessPoint>() {{
        put("BIO251_A_TrilaterationAP", new AccessPoint("BIO251_A_TrilaterationAP", new int[]{Locating.segments - 1, (Locating.segments - 1)/2, 0},-8, -65));
        put("BIO251_B_TrilaterationAP", new AccessPoint("BIO251_B_TrilaterationAP", new int[]{(Locating.segments - 1)/2, Locating.segments - 1, 0}, -9, -53));
        put("BIO251_C_TrilaterationAP", new AccessPoint("BIO251_C_TrilaterationAP", new int[]{10, 0, 0}));
        put("2WIRE601_2GEXT", new AccessPoint("2WIRE601_2GEXT", new int[]{(Locating.segments - 1)/2, 0, 0}, -8, -51));
        put("WIN-D6OH58RJKSU 4714", new AccessPoint("WIN-D6OH58RJKSU 4714", new int[]{0, 10, 0}));
        put("VDCN-Resident", new AccessPoint("VDCN-Resident", new int[]{0, 10, 0}));
        put("RPiHotspot", new AccessPoint("RPiHotspot", new int[]{0, 10, 0}));
    }};

    static public AccessPoint GetAccessPoint(String SSID_In)
    {
        return(AP_DataBase.get((SSID_In)));
    }

    
/*
    static HashMap<String, AccessPoint> AP_DataBase = {
            new AccessPoint("BIO251_A"+AP_Extension, new int[]{0, 10, 0}),
            new AccessPoint("BIO251_B"+AP_Extension, new int[]{40, 10, 0})
    };*/
}
