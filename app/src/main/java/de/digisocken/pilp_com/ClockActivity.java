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
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
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
    private Sensor ambient;
    private MySensorListener sensorListener;
    private Canvas canvas;
    private LinearLayout clockLayout;
    private int level = -1;
    private int batTemp = -80;

    Handler handler = new Handler();
    int delay = 500; //milliseconds

    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context ctxt, Intent intent) {
            level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            batTemp = (intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -800)/10) -9;
            level = (int) (((float) level)*1.15);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(PilpApp.BROADCAST_EXIT);
        registerReceiver(nReceiver, filter);
        registerReceiver(mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(nReceiver);
        unregisterReceiver(mBatInfoReceiver);
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
                if (level>0) msgText.append("\n\nHP " + Integer.toString(level) + "/115");
                if (batTemp>-80) msgText2.append("\n\n" + Integer.toString(batTemp) + "°C");
                handler.postDelayed(this, delay);
            }
        }, delay);

        sensorListener = new MySensorListener();
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        if (mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null) {
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

            mSensorManager.registerListener(sensorListener, mSensor, 30000);
        }
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null) {
            ambient = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            mSensorManager.registerListener(sensorListener, ambient, 100000);
        }

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
        private int x1 = 5;
        private int x2 = 5;
        private int lastY1 = 40;
        private int lastY2 = 40;
        private int ticks = 0;

        @Override
        public void onSensorChanged(SensorEvent event) {
            int y = 0;
            int color = getResources().getColor(R.color.colorAccent2);
            Paint p = new Paint();
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                y = (int) Math.abs(2 * event.values[0]);
                p.setColor(color);
                canvas.drawLine(x2-5, 60+lastY2, x2, 60+y, p);
                lastY2 = y;
                x2+=5;
            } else {
                y = Math.round( 10 *(
                        Math.abs(event.values[0]) +
                                Math.abs(event.values[1]) +
                                Math.abs(event.values[2])
                ));
                color = getResources().getColor(R.color.colorPrimaryDark);
                p.setColor(color);
                canvas.drawLine(x1-5, 30+lastY1, x1, 30+y, p);
                lastY1 = y;
                x1+=5;
            }

            if(x1 > X_LIMIT) {
                ticks++;
                x1=5;
                if (ticks>20) {
                    ticks=0;
                    p.setColor(getResources().getColor(R.color.colorPrimaryDarker));
                    canvas.drawRect(0, 0, X_LIMIT, Y_LIMIT, p);
                }
            }

            if(x2 > X_LIMIT) {
                ticks++;
                x2=5;
                if (ticks>20) {
                    ticks=0;
                    p.setColor(getResources().getColor(R.color.colorPrimaryDarker));
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
