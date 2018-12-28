package de.digisocken.pilp_com;

import android.content.Context;
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
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Date;


public class ClockFragment extends Fragment {
    private static final int Y_LIMIT = 200;
    private static final int X_LIMIT = 200;

    private static final String ARG_SECTION_NUMBER = "section_number";
    private int mParam1;
    private TextView msgText;
    private TextView msgText2;

    private SensorManager mSensorManager;
    private Sensor mSensor;
    private MySensorListener sensorListener;
    private Canvas canvas;
    private LinearLayout clockLayout;

    Handler handler = new Handler();
    int delay = 500; //milliseconds

    public ClockFragment() { }

    public static ClockFragment newInstance(int param1) {
        ClockFragment fragment = new ClockFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getInt(ARG_SECTION_NUMBER);
        }
        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null) {
            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        }
        sensorListener = new MySensorListener();
        mSensorManager.registerListener(sensorListener, mSensor, 30000);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_clock, container, false);
        msgText = view.findViewById(R.id.section_clock);
        msgText2 = view.findViewById(R.id.section_clock2);
        clockLayout = view.findViewById(R.id.clockBack);

        Bitmap.Config conf = Bitmap.Config.RGB_565;
        Bitmap bm = Bitmap.createBitmap(X_LIMIT, Y_LIMIT, conf);
        Drawable dr = new BitmapDrawable(getResources(), bm);
        clockLayout.setBackground(dr);
        canvas = new Canvas(bm);

        handler.postDelayed(new Runnable(){
            public void run(){
                msgText.setText(DateFormat.format(getString(R.string.date_format2), new Date()));
                msgText2.setText(DateFormat.format(getString(R.string.time_format), new Date()));
                handler.postDelayed(this, delay);
            }
        }, delay);

        return view;
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
}
