package com.example.wifitester;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.SpringAnimation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class Locating extends AppCompatActivity {
    // TODO: play around with these, segment is number of cells, space may be a real world measurement
    private static final int space = 500; // ft
    private static final int segments = 100;
    //
    private static final int ymid = 1300;
    private static final int barlen = 1000;
    private static final int xoffset = 40;
    private final ArrayList<String> arrayList = new ArrayList<>();
    private ArrayAdapter arrayAdapter;
    static int count = 0;
    static int fileCount = 0;
    static boolean live = true;
    View dot;
    Locator locator;
    WiFiScanner wiFiScanner;
    ScheduledThreadPoolExecutor e = new ScheduledThreadPoolExecutor(1);


    static class DrawView extends View {
        Paint paint = new Paint();
        Context ctx;

        DrawView(Context ctx, AttributeSet attrs) {
            super(ctx, attrs);
            this.ctx = ctx;
            paint.setColor(Color.BLACK);
        }

        @Override
        public void onDraw(Canvas c) {
            super.onDraw(c);
            c.drawLine(xoffset, ymid, xoffset + barlen, ymid, paint);
            for (int i = 0; i <= segments; i++) {
                c.drawLine(i * barlen / segments + xoffset, ymid - 100, i * barlen / segments + xoffset, ymid + 100, paint);
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


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locating);
        DrawView dv = new DrawView(this, null);
        int id = View.generateViewId();
        dv.setId(id);
        FrameLayout l = findViewById(R.id.loc_parent);
        l.addView(dv);

        dot = findViewById(R.id.dotView);
        dot.setLayoutParams(new FrameLayout.LayoutParams(barlen / 2 / segments, barlen / 2 / segments));
        dot.setTranslationY(ymid - barlen / 4 / segments);

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
        String[] AP = new String[]{"RPiHotspot", "vadai7"};
        //
        locator = new Locator(segments, AP[0], AP[1]);
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        BroadcastReceiver bp = new BroadcastReceiver() {
            @RequiresApi(api = Build.VERSION_CODES.O)
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
                if (results.stream().anyMatch(r -> Arrays.stream(AP).anyMatch(ap -> ap.equals(r.SSID)))) {
                    arrayList.clear();
                    try {
                        for (ScanResult r : results.stream().filter(r -> Arrays.stream(AP).anyMatch(ap -> ap.equals(r.SSID))).toArray(ScanResult[]::new)) {
                            Toast.makeText(getApplicationContext(), "Voting ... ", Toast.LENGTH_SHORT).show();
                            locator.vote(r);
                            arrayList.add(r.SSID + ": dBm[" + r.level + "] - normalized distance {" + locator.getNormalized(r) + "}");
                        }
                        arrayList.add(AP[0] + " Max|Min: (" + locator.aps.get(0).maxDB + "|" + locator.aps.get(0).minDB + ")");
                        arrayList.add(AP[1] + " Max|Min: (" + locator.aps.get(1).maxDB + "|" + locator.aps.get(1).minDB + ")");
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

    }

    Handler h = new Handler();
    Runnable dotRedraw = new Runnable() {
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
            SpringAnimation springAnimation = new SpringAnimation(dot, DynamicAnimation.X, sol);
            springAnimation.start();
//            ObjectAnimator ani = ObjectAnimator.ofFloat(dot,
//                    "translationX",
//                    0, sol);
//            ani.setDuration(1000);
//            ani.start();
//            h.postDelayed(this, 5000);
//            h.postDelayed(wiFiScanner::scanWifi, 500);
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.O)
    void writeVoting() {
        DataWriter d = new DataWriter(this, "votingMatrix" + fileCount);
        fileCount++;
        d.getFile("");
        int[][] voting = locator.getVoting();
        for (int[] ints : voting) {
            d.writeData(Arrays.stream(ints).mapToObj(String::valueOf).toArray(String[]::new));
        }
        d.saveData();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onDestroy() {
        super.onDestroy();
        writeVoting();
        live = false;
    }
}