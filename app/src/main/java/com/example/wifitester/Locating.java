package com.example.wifitester;

import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.Objects;

public class Locating extends AppCompatActivity {
    private static final int space = 50; // ft
    private static final int segments = 10;
    private static final int ymid = 1300;
    private static final int barlen = 1000;
    private static final int xoffset = 40;
    View dot;
    Locator locator;
    WiFiScanner wiFiScanner;

    class DrawView extends View {
        Paint paint = new Paint();
        Context ctx;

        DrawView(Context ctx) {
            super(ctx);
            this.ctx = ctx;
            paint.setColor(Color.BLACK);
        }

        @Override
        public void onDraw(Canvas c) {
            super.onDraw(c);
            c.drawLine(xoffset, ymid, xoffset+barlen, ymid, paint);
            for (int i = 0; i <= space / segments; i++) {
                c.drawLine(i * barlen*segments/space + xoffset, ymid - 100, i * barlen*segments/space + xoffset, ymid + 100, paint);
            }
        }
    }
    class DotView extends View {
        Paint paint = new Paint();
        Context ctx;

        DotView(Context ctx) {
            super(ctx);
            this.ctx = ctx;
            paint.setColor(Color.BLACK);
        }

        @Override
        public void onDraw(Canvas c) {
            super.onDraw(c);
            Objects.requireNonNull(ContextCompat.getDrawable(ctx, R.drawable.dot)).draw(c);
        }
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            wiFiScanner.scanWifi();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new DrawView(this));
        dot = new DotView(this);
        locator = new Locator(50, "RPiHotspot", "RPiHotspot2");
        BroadcastReceiver bp = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

            }
        };
        wiFiScanner = new WiFiScanner(bp, this);
    }

    void redrawDot(int seg) {
        ObjectAnimator ani = ObjectAnimator.ofInt(dot,
                "translationX",
                seg * barlen*segments/space + xoffset + barlen/2*segments/space);
        ani.setDuration(1000);
        ani.start();
    }
}