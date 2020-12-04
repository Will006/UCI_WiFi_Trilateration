package com.example.wifitester;

import android.animation.ObjectAnimator;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class Locating extends AppCompatActivity {
    private static final int space = 25; // ft
    private static final int segments = 5;
    private static final int ymid = 1300;
    private static final int barlen = 1000;
    private static final int xoffset = 40;
    private final ArrayList<String> arrayList = new ArrayList<>();
    private ArrayAdapter arrayAdapter;
    static int count = 0;
    static int fileCount = 0;
    View dot;
    Locator locator;
    WiFiScanner wiFiScanner;
    ScheduledThreadPoolExecutor e = new ScheduledThreadPoolExecutor(1);


    class DrawView extends View {
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
            for (int i = 0; i <= space / segments; i++) {
                c.drawLine(i * barlen * segments / space + xoffset, ymid - 100, i * barlen * segments / space + xoffset, ymid + 100, paint);
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
        dot.setLayoutParams(new FrameLayout.LayoutParams(barlen / 2 * segments / space, barlen / 2 * segments / space));
        dot.setTranslationY(ymid - barlen / 4 * segments / space);

        Button clear = findViewById(R.id.clear_button);
        Button save = findViewById(R.id.save_button);
        clear.setOnClickListener(view -> {
            locator.clear();
        });
        save.setOnClickListener(view -> {
            writeVoting();
        });

        ListView list = findViewById(R.id.LocatingAPs);
        arrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,
                arrayList);
        list.setAdapter(arrayAdapter);

        String[] AP = new String[]{"RPiHotspot", "RPiHotspot2"};
        locator = new Locator(segments, AP[0], AP[1]);
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        BroadcastReceiver bp = new BroadcastReceiver() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onReceive(Context context, Intent intent) {
                Toast.makeText(getApplicationContext(), "Scanning ... ", Toast.LENGTH_SHORT).show();
                List<ScanResult> results = wifiManager.getScanResults();
                // make sure we release hold
                unregisterReceiver(this);
                // if we got nothing
                if (results.size() == 0) {
                    // early return
                    return;
                }
                arrayList.clear();
                for (ScanResult r : results) {
                    if (Arrays.stream(AP).anyMatch(ap -> ap.equals(r.SSID))) {
                        locator.vote(r);
                        arrayList.add(r.SSID + ": dBm[" + r.level+ "]");
                    }
                }
                arrayList.add(AP[0] + " Max|Min: (" + locator.maxDb1 + "|" + locator.minDb1 + ")");
                arrayList.add(AP[1] + " Max|Min: (" + locator.maxDb2 + "|" + locator.minDb2 + ")");
                arrayAdapter.notifyDataSetChanged();
                ++count;
                if (count == 60) {
                    writeVoting();
                }
//                redrawDot(locator.getMaxSegment());
                h.postDelayed(dotRedraw, 1000);
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
            int sol = r * segmentSize + xoffset - barlen / 4 * segments / space + segmentSize/2;
            Toast.makeText(getApplicationContext(), "Moving dot to " + r, Toast.LENGTH_SHORT).show();
//            dot = findViewById(R.id.dotView);
//            dot.setTranslationX(r);
            ObjectAnimator ani = ObjectAnimator.ofFloat(dot,
                    "translationX",
                    dot.getX(), sol);
            ani.setDuration(1000);
            ani.start();
//            h.postDelayed(this, 5000);
            h.postDelayed(wiFiScanner::scanWifi, 1000);
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
        dotRedraw = () -> {
        };
        writeVoting();
    }
}