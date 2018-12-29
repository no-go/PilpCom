package de.digisocken.pilp_com;

import android.app.Application;
import android.location.LocationManager;

public class PilpApp extends Application {
    public final static String BROADCAST_EXIT = "de.digisocken.pilp_com.EXIT";
    public static LocationManager locationManager;
}
