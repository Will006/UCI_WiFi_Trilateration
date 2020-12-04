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
import android.os.Bundle;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Locating extends AppCompatActivity {
    private static final int space = 50; // ft
    private static final int segments = 10;
    private static final int ymid = 1300;
    private static final int barlen = 1000;
    private static final int xoffset = 40;
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

    class DotView extends View {
        Paint paint = new Paint();
        Context ctx;

        DotView(Context ctx, AttributeSet attrs) {
            super(ctx, attrs);
            this.ctx = ctx;
            paint.setColor(Color.BLACK);
        }

        @Override
        public void onDraw(Canvas c) {
            super.onDraw(c);
            Objects.requireNonNull(ContextCompat.getDrawable(ctx, R.drawable.dot)).draw(c);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locating);
        DrawView dv = new DrawView(this, null);
        int id = View.generateViewId();
        dv.setId(id);
        ConstraintLayout l = (ConstraintLayout) findViewById(R.id.loc_parent);
        l.addView(dv);

        dot = findViewById(R.id.dotView);
//        dot = new DotView(this, null);
//        l.addView(dot);
        locator = new Locator(50, "RPiHotspot", "RPiHotspot2");
        String[] AP = new String[]{"RPiHotspot", "RPiHotspot2"};
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        BroadcastReceiver bp = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                List<ScanResult> results = wifiManager.getScanResults();
                // make sure we release hold
                unregisterReceiver(this);
                // if we got nothing
                if (results.size() == 0) {
                    // early return
                    return;
                }
                for (ScanResult r : results) {
                    if (Arrays.stream(AP).anyMatch(ap -> ap.equals(r.SSID))) {
                        locator.vote(r);
                    }
                }
//                redrawDot(locator.getMaxSegment());

            }
        };
        wiFiScanner = new WiFiScanner(bp, this);
        h.postDelayed(dotRedraw, 1000);
        e.scheduleAtFixedRate(dotRedraw, 5, 1, TimeUnit.SECONDS);
//        wiFiScanner.scanWifi();

    }
    Handler h = new Handler();
    Runnable dotRedraw = new Runnable() {
        @Override
        public void run() {
            int r = new Random().nextInt(3000);
            Toast.makeText(getApplicationContext(), "Moving dot to " + r, Toast.LENGTH_SHORT).show();
            dot = findViewById(R.id.dotView);
            dot.setTranslationX(r);
//            ObjectAnimator ani = ObjectAnimator.ofFloat(dot,
//                    "X",
//                    0, r);//seg * barlen*segments/space + xoffset + barlen/2*segments/space);
//        ani.setDuration(1000);
//            ani.start();
            h.postDelayed(this, 1000);
        }
    };

    void redrawDot() {
        Toast.makeText(this, "Moving dot ...", Toast.LENGTH_SHORT).show();
        ObjectAnimator ani = ObjectAnimator.ofFloat(dot,
                "translationX",
                0, new Random().nextInt(2000));//seg * barlen*segments/space + xoffset + barlen/2*segments/space);
        ani.setDuration(1);
        ani.start();
//        dot.setVisibility(View.VISIBLE);
    }
}