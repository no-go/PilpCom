package de.digisocken.pilp_com;

import android.Manifest;
import android.app.Instrumentation;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.util.Locale;


public class AreaActivity extends AppCompatActivity implements LocationListener {
    static public int GPSREFRESH = 400;
    static final int LOCATION_PERMISSIONS_REQUEST = 23;
    private NotificationReceiver nReceiver;

    private static String PROVIDER;

    public static SharedPreferences pref;

    private TextView posView;

    private MapView map;
    private ImageView swMap;
    private ImageView marker;
    private int updateCount = 0;
    Canvas canvas;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String readMessage = intent.getStringExtra("data");
            new Thread() {
                public void run() {
                    Instrumentation instr = new Instrumentation();
                    if (readMessage.startsWith("u")) instr.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_UP);
                    if (readMessage.startsWith("d")) instr.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_DOWN);
                    if (readMessage.startsWith("l")) instr.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_LEFT);
                    if (readMessage.startsWith("r")) instr.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_RIGHT);
                    if (readMessage.startsWith("t")) instr.sendKeyDownUpSync(KeyEvent.KEYCODE_TAB);
                    if (readMessage.startsWith("s")) instr.sendKeyDownUpSync(KeyEvent.KEYCODE_ENTER);
                }
            }.start();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(PilpApp.BROADCAST_EXIT);
        registerReceiver(nReceiver, filter);
        IntentFilter filter2 = new IntentFilter();
        filter2.addAction(getString(R.string.BROADCASTMSG));
        registerReceiver(receiver, filter2);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(nReceiver);
        unregisterReceiver(receiver);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        nReceiver = new NotificationReceiver();

        pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        setContentView(R.layout.fragment_area);

        map = findViewById(R.id.map);
        swMap = findViewById(R.id.swMap);
        posView = findViewById(R.id.section_pos);
        marker = findViewById(R.id.marker);
        map.setTileSource(TileSourceFactory.MAPNIK);
        //map.setBuiltInZoomControls(true);
        //map.setMultiTouchControls(true);

        PilpApp.locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        int fine = ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        if (fine != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSIONS_REQUEST
            );
        } else {
            PROVIDER = LocationManager.GPS_PROVIDER;
            PilpApp.locationManager.requestLocationUpdates(
                    PROVIDER,
                    GPSREFRESH,
                    0,
                    (android.location.LocationListener) this
            );
        }

        // ------------------------------------------------layoutsize

        int left = PilpApp.getPref("appleft", pref, PilpApp.appleft);
        int top = PilpApp.getPref("apptop", pref, PilpApp.apptop);
        int width = PilpApp.getPref("appwidth", pref, PilpApp.appwidth);

        LinearLayout re = findViewById(R.id.block_area_main);
        re.setPadding(left, top, re.getPaddingRight(), re.getPaddingBottom());

        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams(
                width, LinearLayout.LayoutParams.WRAP_CONTENT
        );
        LinearLayout ll1 = findViewById(R.id.thetabs);
        ll1.setLayoutParams(lp1);

        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(
                width, LinearLayout.LayoutParams.MATCH_PARENT
        );
        FrameLayout ll2 = findViewById(R.id.block_area);
        ll2.setLayoutParams(lp2);




        marker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                float lati =  52.00733f;
                float longi =  8.53507f;
                if (!pref.contains("lati")) {
                    pref.edit().putFloat("lati", lati).commit();
                } else {
                    lati = pref.getFloat("lati", lati);
                }
                if (!pref.contains("longi")) {
                    pref.edit().putFloat("longi", longi).commit();
                } else {
                    longi = pref.getFloat("longi", longi);
                }
                IMapController mapController = map.getController();
                mapController.setZoom(Integer.parseInt(pref.getString("zoomlevel", "17")));
                GeoPoint startPoint = new GeoPoint(lati, longi);
                mapController.setCenter(startPoint);
                Bitmap b = getViewBitmap(map);
                swMap.setImageBitmap(b);
            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {
        IMapController mapController = map.getController();
        mapController.setZoom(Integer.parseInt(pref.getString("zoomlevel", "17")));
        GeoPoint startPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
        if (updateCount == 0) pref.edit().putFloat("lati", (float)location.getLatitude()).apply();
        if (updateCount == 0) pref.edit().putFloat("longi", (float)location.getLongitude()).apply();
        updateCount = (updateCount +1) % 100;
        mapController.setCenter(startPoint);
        Bitmap b = getViewBitmap(map);
        float bear = location.getBearing();
        marker.setRotation(bear);
        swMap.setImageBitmap(b);
        posView.setText(
                String.format( Locale.ENGLISH,
                        "Lat %f\nLong %f\n%.0f km/h",
                        location.getLatitude(),
                        location.getLongitude(),
                        3.6 * location.getSpeed()
                )
        );
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) { }

    @Override
    public void onProviderEnabled(String s) { }

    @Override
    public void onProviderDisabled(String s) { }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSIONS_REQUEST: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    PROVIDER = LocationManager.GPS_PROVIDER;
                    PilpApp.locationManager.requestLocationUpdates(
                            PROVIDER,
                            GPSREFRESH,
                            0,
                            (android.location.LocationListener) this
                    );
                } else {
                    PROVIDER = LocationManager.NETWORK_PROVIDER;
                    PilpApp.locationManager.requestLocationUpdates(
                            PROVIDER,
                            GPSREFRESH,
                            0,
                            (android.location.LocationListener) this
                    );
                }
                return;
            }
        }
    }

    private Bitmap getViewBitmap(View v) {
        v.clearFocus();
        v.setPressed(false);

        boolean willNotCache = v.willNotCacheDrawing();
        v.setWillNotCacheDrawing(false);

        // Reset the drawing cache background color to fully transparent
        // for the duration of this operation
        int color = v.getDrawingCacheBackgroundColor();
        v.setDrawingCacheBackgroundColor(0);

        if (color != 0) {
            v.destroyDrawingCache();
        }
        v.buildDrawingCache();
        Bitmap cacheBitmap = v.getDrawingCache();
        if (cacheBitmap == null) return null;

        Bitmap bitmap = toGrayscale(Bitmap.createBitmap(cacheBitmap));

        // Restore the view
        v.destroyDrawingCache();
        v.setWillNotCacheDrawing(willNotCache);
        v.setDrawingCacheBackgroundColor(color);

        canvas = new Canvas(bitmap);

        // -------------------------------------------try a filter
        Paint p = new Paint();
        ColorMatrix cm = new ColorMatrix();
        if (Locale.getDefault().getLanguage().equals("ha") ) {
            cm.setScale(0.00f, 0.25f, 0.60f, 1);
        } else if (Locale.getDefault().getLanguage().equals("ig") ) {
                cm.setScale(0.00f,0.65f,0.15f,1);
        } else {
            cm.setScale(0.75f,0.45f,0.10f,1);
        }
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(cm);
        p.setColorFilter(filter);
        canvas.drawBitmap(bitmap, 0, 0, p);

        return bitmap;
    }

    public Bitmap toGrayscale(Bitmap bmpOriginal) {
        int width, height;
        height = bmpOriginal.getHeight();
        width = bmpOriginal.getWidth();

        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
        return bmpGrayscale;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                toNews(null);
                return true;
            case KeyEvent.KEYCODE_DPAD_LEFT:
                toMsg(null);
                return true;

            case KeyEvent.KEYCODE_K:
                toClk(null);
                return true;
            case KeyEvent.KEYCODE_I:
                toWho(null);
                return true;
            case KeyEvent.KEYCODE_J:
                toMsg(null);
                return true;
            case KeyEvent.KEYCODE_O:
                toNews(null);
                return true;
            default:
                return super.onKeyUp(keyCode, event);
        }
    }

    public void toClk(View view) {
        Intent intent = new Intent(this, ClockActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
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
        PilpApp.locationManager.removeUpdates(this);
        finishAffinity();
    }
}
