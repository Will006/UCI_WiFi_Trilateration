package com.example.wifitester;


import java.util.HashMap;

class AccessPoint
{
    static String AP_Extension = "TrilaterationAP";
    String SSID;
    int Location[];

    AccessPoint(String SSID_In, int Location_In[])
    {
        SSID=SSID_In;
        Location = Location_In;
    }
    static private HashMap<String, AccessPoint> AP_DataBase = new HashMap<String, AccessPoint>() {{
        put("BIO251_A_"+AP_Extension, new AccessPoint("BIO251_A_"+AP_Extension, new int[]{0, 10, 0}));
        put("BIO251_B_"+AP_Extension, new AccessPoint("BIO251_B_"+AP_Extension, new int[]{0, 10, 0}));
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
