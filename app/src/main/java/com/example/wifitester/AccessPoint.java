package com.example.wifitester;


import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

class AccessPoint
{
    static final String AP_Extension = "TrilaterationAP";
    final String SSID;
    final int[] Location;
    int maxDB;
    int minDB;

    AccessPoint(AccessPoint AccessPointIn)
    {
        SSID=AccessPointIn.SSID;
        Location = AccessPointIn.Location;
        maxDB = AccessPointIn.maxDB;
        minDB = AccessPointIn.minDB;
    }
    AccessPoint(String SSID_In, int[] Location_In)
    {
        SSID=SSID_In;
        Location = Location_In;
    }
    AccessPoint(String SSID_In, int[] Location_In, int maxDb1_In, int minDB1_In)
    {
        SSID=SSID_In;
        Location = Location_In;
        maxDB = maxDb1_In;
        minDB = minDB1_In;
    }

    static public final List<String> SSIDs = Arrays.asList("BIO251_A_TrilaterationAP","BIO251_B_TrilaterationAP","2WIRE601_2GEXT","THOR");
    // TODO: change these if you're using the normalized version, set max/min by checking signal strength at each AP
    // TODO: set these via some setup thing
    static private final HashMap<String, AccessPoint> AP_DataBase = new HashMap<String, AccessPoint>() {{
        put("BIO251_A_TrilaterationAP", new AccessPoint("BIO251_A_TrilaterationAP", new int[]{Locating.segments/2, 0, 0},-48, -67));
        put("BIO251_B_TrilaterationAP", new AccessPoint("BIO251_B_TrilaterationAP", new int[]{Locating.segments/2,Locating.segments - 1, 0}, -33, -55));
        put("2WIRE601_2GEXT", new AccessPoint("2WIRE601_2GEXT", new int[]{Locating.segments/2,Locating.segments - 1, Locating.segments - 1}, -36, -47));
        put("THOR", new AccessPoint("THOR", new int[]{Locating.segments/2,0, Locating.segments - 1}, -31, -54));
        put("WIN-D6OH58RJKSU 4714", new AccessPoint("WIN-D6OH58RJKSU 4714", new int[]{0, 10, 0}));
        put("VDCN-Resident", new AccessPoint("VDCN-Resident", new int[]{0, 10, 0}));
        put("RPiHotspot", new AccessPoint("RPiHotspot", new int[]{0, 10, 0}));
    }};

    static public HashMap<String, AccessPoint> GetSubSet(List<String> SSIDList) throws NoMatch {
        HashMap<String, AccessPoint> APSubList = new HashMap<String, AccessPoint>();
        for(String SSID : SSIDList)
        {
            APSubList.put(SSID, AP_DataBase.get(SSID));
        }
        if(APSubList.size()==0)
        {
            throw new NoMatch();
        }
        return APSubList;
    }


    static public AccessPoint GetAccessPoint(String SSID_In) {
        return AP_DataBase.get(SSID_In);
    }
    
/*
    static HashMap<String, AccessPoint> AP_DataBase = {
            new AccessPoint("BIO251_A"+AP_Extension, new int[]{0, 10, 0}),
            new AccessPoint("BIO251_B"+AP_Extension, new int[]{40, 10, 0})
    };*/
}
