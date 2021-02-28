package com.example.wifitester;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.os.HandlerCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class Locating extends AppCompatActivity {
    // TODO: play around with these, segment is number of cells, space may be a real world measurement
    private static final int space = 500; // ft
    static final int segments = 100;
    //
    private static final int ymid = 1300;
    private static final int barlen = 1000;
    private static final int xoffset = 40;
    private final ArrayList<String> arrayList = new ArrayList<>();
    private ArrayAdapter arrayAdapter;
    //    static int count = 0;
    static int fileCount = 0;
    static boolean live = true;
    View dot;
    Locator locator;
    WiFiScanner wiFiScanner;


    static class DrawView extends View {
        final Paint paint = new Paint();
//        final Context ctx;

        DrawView(Context ctx) {
            super(ctx, null);
//            this.ctx = ctx;
            paint.setColor(Color.BLACK);
        }

        @Override
        public void onDraw(Canvas c) {
            super.onDraw(c);
            c.drawLine(xoffset, ymid, xoffset + barlen, ymid, paint);
            for (int i = 0; i <= segments; i++) {
                c.drawLine(i * barlen / (float) segments + xoffset, ymid - 100, i * barlen / (float) segments + xoffset, ymid + 100, paint);
            }
        }
    }

//    class DotView extends View {
//        Paint paint = new Paint();
//        Context ctx;
//
//        DotView(Context ctx, AttributeSet attrs) {
//            super(ctx, attrs);
//            this.ctx = ctx;
//            paint.setColor(Color.BLACK);
//        }
//
//        @Override
//        public void onDraw(Canvas c) {
//            super.onDraw(c);
//            Objects.requireNonNull(ContextCompat.getDrawable(ctx, R.drawable.dot)).draw(c);
//        }
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        live = true;
        setContentView(R.layout.activity_locating);
//        DrawView dv = new DrawView(this);
//        int id = View.generateViewId();
//        dv.setId(id);
//        FrameLayout l = findViewById(R.id.loc_parent);
//        l.addView(dv);

        dot = findViewById(R.id.dotView);
        dot.setLayoutParams(new FrameLayout.LayoutParams(barlen / 2 / segments, barlen / 2 / segments));
        dot.setTranslationY(ymid - barlen / 4f / (float) segments);

        Button clear = findViewById(R.id.clear_button);
        Button save = findViewById(R.id.save_button);
        clear.setOnClickListener(view -> {
            locator.clear();
            Toast.makeText(this,
                    "Cleared voting matrix.",
                    Toast.LENGTH_SHORT).show();
            dot.setVisibility(View.GONE);
        });
        save.setOnClickListener(view -> {
            writeVoting();
            Toast.makeText(this,
                    "Dumped matrix to file. Continuing to vote to current matrix.",
                    Toast.LENGTH_SHORT).show();
        });

        ListView list = findViewById(R.id.LocatingAPs);
        arrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                arrayList);
        list.setAdapter(arrayAdapter);

        // TODO: change these for your AP's
        try {
            HashMap<String, AccessPoint> APSet = AccessPoint.GetSubSet("BIO251_A_TrilaterationAP","BIO251_B_TrilaterationAP","2WIRE601_2GEXT","ASUS_18_2G");

            locator = new Locator(segments, APSet);

            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            BroadcastReceiver bp = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
//                Toast.makeText(getApplicationContext(), "Scanning ... ", Toast.LENGTH_SHORT).show();
                    List<ScanResult> results = wifiManager.getScanResults();
                    // make sure we release hold
                    unregisterReceiver(this);
                    // if we got nothing
                    if (results.size() == 0) {
                        // early return
                        wiFiScanner.scanWifi();
                        return;
                    }

                    //if (results.stream().anyMatch(r -> Arrays.stream(APSet.values()).anyMatch(ap -> ap.equals(r.SSID)))) {
                    if (results.stream().anyMatch(r -> APSet.values().stream().anyMatch(ap -> ap.SSID.equals(r.SSID)))) {
                        arrayList.clear();
                        try {
                            for (ScanResult r : results.stream().filter(
                                    r -> APSet.values().stream()
                                            .anyMatch(ap ->
                                                    ap.SSID.equals(r.SSID)))
                                    .toArray(ScanResult[]::new)) {
                                Toast.makeText(getApplicationContext(), "Voting ... " + locator.getNumVotes(), Toast.LENGTH_SHORT).show();
                                locator.vote(r);
                                arrayList.add(r.SSID + ": dBm[" + r.level + "] - normalized distance {" + locator.getNormalized(r) + "}");
                                AccessPoint ap = APSet.get(r.SSID);
                                arrayList.add(ap.SSID + " Max|Min: (" + ap.maxDB + "|" + ap.minDB + ")");
                            }
                            arrayAdapter.notifyDataSetChanged();
                        } catch (NoMatch n) {
                            return;
                        }
                    }
//                ++count;
//                if (count == 20) {
//                    Toast.makeText(getApplicationContext(), "Clearing ... ", Toast.LENGTH_SHORT).show();
//                    locator.clear();
//                    count = 0;
//                }
//                redrawDot(locator.getMaxSegment());
//                h.postDelayed(dotRedraw, 100);
                    dotRedraw.run();
                    if (live)
                        h.post(wiFiScanner::scanWifi);
                }
            };
//        h.postDelayed(dotRedraw, 1000);
            wiFiScanner = new WiFiScanner(bp, this);
//        e.scheduleAtFixedRate(dotRedraw, 5, 1, TimeUnit.SECONDS);
            wiFiScanner.scanWifi();
        } catch (NoMatch noMatch) {
            noMatch.printStackTrace();
            Log.e("foolish", "you done goofed, we don't have that ssid");
            Toast.makeText(this,
                    "You messed up dummy. Locating line 125.",
                    Toast.LENGTH_LONG).show();
        }
    }

    final Handler h = HandlerCompat.createAsync(Looper.getMainLooper());
    final Runnable dotRedraw = new Runnable() {
        @Override
        public void run() {
            dot.setVisibility(View.VISIBLE);
//            int r = 25;
//            int r = new Random().nextInt(25);
            int r = locator.getMaxSegment();
            int segmentSize = barlen / segments;
            int sol = r * segmentSize + xoffset - barlen / 4 * segments / space + segmentSize / 2;
//            Toast.makeText(getApplicationContext(), "Moving dot to " + r, Toast.LENGTH_SHORT).show();
//            dot = findViewById(R.id.dotView);
//            dot.setTranslationX(r);
            //SpringAnimation springAnimation = new SpringAnimation(dot, DynamicAnimation.X, sol);
            //springAnimation.start();
//            ObjectAnimator ani = ObjectAnimator.ofFloat(dot,
//                    "translationX",
//                    0, sol);
//            ani.setDuration(1000);
//            ani.start();
//            h.postDelayed(this, 5000);
//            h.postDelayed(wiFiScanner::scanWifi, 500);
        }
    };

    void writeVoting() {
        //Log.d("bazinga","would start writing here");
        int[] pos = locator.findPosition();
        Log.d("Locator", pos[1] + " " + pos[2]);
        DataWriter d = new DataWriter(this, "votingMatrix" + fileCount);
        fileCount++;
        d.getFile("");
        int[][][] voting = locator.getVoting();
        for (int[] ints : voting[segments / 2]) {
            d.writeData(Arrays.stream(ints).mapToObj(String::valueOf).toArray(String[]::new));
        }
        d.sendData(String.valueOf(new Date().getTime()).substring(0,9), pos);
        d.saveData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        writeVoting();
        live = false;
    }
}