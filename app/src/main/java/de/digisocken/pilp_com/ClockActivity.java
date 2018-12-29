package de.digisocken.pilp_com;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class ClockActivity  extends AppCompatActivity {
    private NotificationReceiver nReceiver;
    private static final int Y_LIMIT = 200;
    private static final int X_LIMIT = 200;

    private TextView msgText;
    private TextView msgText2;

    private SensorManager mSensorManager;
    private Sensor mSensor;
    private MySensorListener sensorListener;
    private Canvas canvas;
    private LinearLayout clockLayout;

    Handler handler = new Handler();
    int delay = 500; //milliseconds

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(PilpApp.BROADCAST_EXIT);
        registerReceiver(nReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(nReceiver);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        nReceiver = new NotificationReceiver();

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        setContentView(R.layout.fragment_clock);

        handler.postDelayed(new Runnable(){
            public void run(){
                SimpleDateFormat dateFormat = new SimpleDateFormat(getString(R.string.date_format2), Locale.ENGLISH);
                msgText.setText(dateFormat.format(new Date()));
                msgText2.setText(DateFormat.format(getString(R.string.time_format), new Date()));
                handler.postDelayed(this, delay);
            }
        }, delay);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null) {
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        }
        sensorListener = new MySensorListener();
        mSensorManager.registerListener(sensorListener, mSensor, 30000);

        msgText = findViewById(R.id.section_clock);
        msgText2 = findViewById(R.id.section_clock2);
        clockLayout = findViewById(R.id.clockBack);

        Bitmap.Config conf = Bitmap.Config.RGB_565;
        Bitmap bm = Bitmap.createBitmap(X_LIMIT, Y_LIMIT, conf);
        Drawable dr = new BitmapDrawable(getResources(), bm);
        clockLayout.setBackground(dr);
        canvas = new Canvas(bm);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ClockActivity.this, PreferencesActivity.class);
                startActivity(intent);
            }
        });
    }

    class MySensorListener implements SensorEventListener {
        private int x = 5;
        private int lastY = 40;
        private int ticks = 0;

        @Override
        public void onSensorChanged(SensorEvent event) {
            int y = Math.round( 10 *(
                    Math.abs(event.values[0]) +
                            Math.abs(event.values[1]) +
                            Math.abs(event.values[2])
            ));
            Paint p = new Paint();
            p.setColor(getResources().getColor(R.color.colorAccent2));
            canvas.drawLine(x-5, 40+lastY, x, 40+y, p);
            x+=5;
            lastY = y;
            if(x > X_LIMIT) {
                ticks++;
                x=5;
                if (ticks>20) {
                    ticks=0;
                    p.setColor(Color.BLACK);
                    canvas.drawRect(0, 0, X_LIMIT, Y_LIMIT, p);
                }
            }
            clockLayout.invalidate();
        }
        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {}
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_I:
                toWho(null);
                return true;
            case KeyEvent.KEYCODE_J:
                toMsg(null);
                return true;
            case KeyEvent.KEYCODE_M:
                toArea(null);
                return true;
            case KeyEvent.KEYCODE_O:
                toNews(null);
                return true;
            default:
                return super.onKeyUp(keyCode, event);
        }
    }

    public void toWho(View view) {
        Intent intent = new Intent(this, ContactActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }
    public void toMsg(View view) {
        Intent intent = new Intent(this, MsgActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }
    public void toArea(View view) {
        Intent intent = new Intent(this, AreaActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }
    public void toNews(View view) {
        Intent intent = new Intent(this, NewsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }

    class NotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(getLocalClassName(), "broadcast");
            if (intent.getBooleanExtra("EXIT", false)) finishAffinity();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mSensorManager.unregisterListener(sensorListener);
        finishAffinity();
    }
}
