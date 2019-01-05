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
    private static final int Y_LIMIT = 100;
    private static final int X_LIMIT = 100;

    private TextView msgText;
    private TextView msgText2;

    private SensorManager mSensorManager;
    private Sensor mSensor;
    private Sensor ambient;
    private MySensorListener sensorListener;
    private Canvas canvas1;
    private Canvas canvas2;
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
                if (level>0) msgText.append("\nHP " + Integer.toString(level) + "/115");
                if (batTemp>-80) msgText2.append("\n" + Integer.toString(batTemp) + "°C");
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

        Bitmap.Config conf = Bitmap.Config.RGB_565;
        Bitmap bm1 = Bitmap.createBitmap(X_LIMIT, Y_LIMIT, conf);
        Drawable dr1 = new BitmapDrawable(getResources(), bm1);
        msgText.setBackground(dr1);
        canvas1 = new Canvas(bm1);

        Bitmap bm2 = Bitmap.createBitmap(X_LIMIT, Y_LIMIT, conf);
        Drawable dr2 = new BitmapDrawable(getResources(), bm2);
        msgText2.setBackground(dr2);
        canvas2 = new Canvas(bm2);

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
        private int lastY1 = 20;
        private int lastY2 = 20;
        private int ticks = 0;

        @Override
        public void onSensorChanged(SensorEvent event) {
            int y = 0;
            int color = getResources().getColor(R.color.colorAccent2);
            Paint p = new Paint();
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                y = (int) Math.abs(2 * event.values[0]);
                p.setColor(color);
                canvas2.drawLine(x2-5, 20+lastY2, x2, 20+y, p);
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
                canvas1.drawLine(x1-5, 20+lastY1, x1, 20+y, p);
                lastY1 = y;
                x1+=5;
            }

            if(x1 > X_LIMIT) {
                ticks++;
                x1=5;
                if (ticks>20) {
                    ticks=0;
                    p.setColor(getResources().getColor(R.color.colorPrimaryDarker));
                    canvas1.drawRect(0, 0, X_LIMIT, Y_LIMIT, p);
                }
            }

            if(x2 > X_LIMIT) {
                ticks++;
                x2=5;
                if (ticks>20) {
                    ticks=0;
                    p.setColor(getResources().getColor(R.color.colorPrimaryDarker));
                    canvas2.drawRect(0, 0, X_LIMIT, Y_LIMIT, p);
                }
            }
            msgText.invalidate();
            msgText2.invalidate();
        }
        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {}
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                toWho(null);
                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                toNews(null);
                return true;

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
