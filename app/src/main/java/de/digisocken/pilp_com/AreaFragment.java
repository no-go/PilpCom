package de.digisocken.pilp_com;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.util.Locale;


public class AreaFragment extends Fragment implements LocationListener {
    static public int GPSREFRESH = 500;

    static final int MY_PERMISSIONS_REQUEST_READ_GPS = 23;
    private LocationManager locationManager;
    public static SharedPreferences pref;

    private TextView posView;

    private MapView map;
    private ImageView swMap;
    private ImageView marker;
    private int updateCount = 0;
    Canvas canvas;

    private static final String ARG_SECTION_NUMBER = "section_number";
    private int mParam1;

    public AreaFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment AreaFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AreaFragment newInstance(int param1) {
        AreaFragment fragment = new AreaFragment();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_area, container, false);

        map = view.findViewById(R.id.map);
        swMap = view.findViewById(R.id.swMap);
        posView = view.findViewById(R.id.section_pos);
        marker = view.findViewById(R.id.marker);
        map.setTileSource(TileSourceFactory.MAPNIK);
        //map.setBuiltInZoomControls(true);
        //map.setMultiTouchControls(true);

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (!ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(
                        getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_READ_GPS
                );
            }
        }
        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                GPSREFRESH,
                100,
                (android.location.LocationListener) this
        );

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

        return view;
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
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String permissions[],
            int[] grantResults
    ) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_GPS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // ??
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
        cm.setScale(0.75f,0.45f,0.10f,1);
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
}
